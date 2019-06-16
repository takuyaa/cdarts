package com.github.cdarts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;

public class FSTBuilder {
    public FST build(Stream<Map.Entry<byte[], Integer>> entries) {
        final var statesDict = new StatesDict();
        final List<MutableState> tempStates = new ArrayList<>();

        var lastEntry = entries.reduce(Map.entry(new byte[0], 0), (prev, current) -> {
            final byte[] prevWord = prev.getKey();
            final byte[] currentWord = current.getKey();
            final int currentOutput = current.getValue();

            assert !prevWord.equals(currentWord) : "Multiple output is not supported"; // Throw Exception
            assert compare(prevWord, currentWord) < 0 : "Input keys must be sorted"; // Throw Exception

            // initialize buffer
            while (tempStates.size() <= currentWord.length) {
                tempStates.add(new MutableState());
            }

            final int prefixLengthPlus1 = prefixLength(prevWord, currentWord) + 1;

            // we minimize the states from the suffix of the previous word
            for (int i = prevWord.length; i >= prefixLengthPlus1; i--) {
                final MutableState prevState = tempStates.get(i - 1);
                final FrozenState nextState = statesDict.findMinimized(tempStates.get(i));
                prevState.setTransition(prevWord[i - 1], nextState);
            }
            // this loop initializes the states from the suffix of the previous word
            for (int i = prefixLengthPlus1; i <= currentWord.length; i++) {
                final MutableState prevState = tempStates.get(i - 1);
                final MutableState nextState = tempStates.get(i);
                nextState.clear();
                prevState.setTransition(currentWord[i - 1], nextState);
            }
            // terminate last state for currentWord
            final var lastState = tempStates.get(currentWord.length);
            lastState.isFinal = true;
            lastState.output = OptionalInt.empty();

            OptionalInt currentOutputTail = OptionalInt.of(currentOutput);
            for (int i = 1; i < prefixLengthPlus1; i++) {
                final MutableState prevState = tempStates.get(i - 1);
                final MutableState nextState = tempStates.get(i);

                final OptionalInt prevOutput = prevState.transitOutput(currentWord[i - 1]);
                final OptionalInt commonPrefix = prefix(prevOutput, currentOutputTail);
                final OptionalInt wordSuffix = subtract(prevOutput, commonPrefix);

                prevState.setTransitionOutput(currentWord[i - 1], commonPrefix);
                if (wordSuffix.isPresent()) {
                    // iterate over all transitions from nextState
                    for (Transition transition : nextState.transitions) {
                        transition.output = concat(wordSuffix.getAsInt(), transition.output);
                    }
                }
                if (nextState.isFinal) {
                    if (wordSuffix.isPresent()) {
                        OptionalInt stateOutput = nextState.getStateOutput();
                        nextState.setStateOutput(concat(wordSuffix.getAsInt(), stateOutput));
                    }
                    currentOutputTail = subtract(currentOutputTail, wordSuffix);
                }
            }

            final var lastPrefixState = tempStates.get(prefixLengthPlus1 - 1);
            lastPrefixState.setTransitionOutput(currentWord[prefixLengthPlus1 - 1], currentOutputTail);

            // pass currentWord to next iteration
            return current;
        });

        // here we are minimizing the states of the last word
        final byte[] currentWord = lastEntry.getKey();
        for (int i = currentWord.length; i > 0; i--) {
            final MutableState prevState = tempStates.get(i - 1);
            final FrozenState nextState = statesDict.findMinimized(tempStates.get(i));
            prevState.setTransition(currentWord[i - 1], nextState);
        }
        final FrozenState initialState = statesDict.findMinimized(tempStates.get(0));

        return new FST(statesDict, initialState);
    }

    static int prefixLength(byte[] b1, byte[] b2) {
        final int shorterLength = Math.min(b1.length, b2.length);
        int i = 0;
        while (i < shorterLength) {
            if (b1[i] != b2[i]) {
                return i;
            }
            i++;
        }
        return i;
    }

    static OptionalInt prefix(OptionalInt a, OptionalInt b) {
        if (a.isEmpty() || b.isEmpty()) {
            return OptionalInt.empty();
        }
        return a.getAsInt() == b.getAsInt() ? a : OptionalInt.empty();
    }

    static OptionalInt concat(int a, OptionalInt b) {
        // add outputs
        // if (b.isEmpty()) {
        // return OptionalInt.of(a);
        // }
        // return OptionalInt.of(a + b.getAsInt());

        // adopt former value
        return OptionalInt.of(a);
    }

    static OptionalInt subtract(OptionalInt a, OptionalInt b) {
        if (a.isEmpty()) {
            assert false;
            return OptionalInt.empty();
        }
        // split outputs
        // if (b.isEmpty()) {
        // return a;
        // }
        // return OptionalInt.of(a.getAsInt() - b.getAsInt());

        // ignore subtrahend
        return a;
    }

    static int compare(byte[] b1, byte[] b2) {
        final int shorterLength = Math.min(b1.length, b2.length);
        for (int i = 0; i < shorterLength; i++) {
            if (b1[i] != b2[i]) {
                int i1 = ((int) b1[i]) & 0xFF;
                int i2 = ((int) b2[i]) & 0xFF;
                return i1 - i2;
            }
        }
        return b1.length - b2.length;
    }

    static String translateToDot(Set<FrozenState> states) {
        final var dot = new StringBuilder(1024);
        dot.append("digraph G {\n");
        dot.append("  rankdir = LR;\n");
        dot.append("  node [shape = circle];\n");

        Map<State, Long> ids = new HashMap<>();
        long maxId = 1;
        for (FrozenState state : states) {
            if (!ids.containsKey(state)) {
                ids.put(state, maxId++);
            }

            // draw a node
            if (state.getStateOutput().isPresent() || state.isFinal) {
                dot.append("  \"" + ids.get(state) + "\" [");
                if (state.getStateOutput().isPresent()) {
                    // draw state output as node label
                    dot.append("xlabel = \"" + state.getStateOutput().getAsInt() + "\" ");
                }
                if (state.isFinal) {
                    dot.append("peripheries = 2");
                }
                dot.append("];\n");
            }

            // draw edges
            for (Transition transition : state.transitions) {
                State next = transition.nextState;
                if (!ids.containsKey(next)) {
                    ids.put(state, maxId++);
                }
                byte[] label = { transition.label };
                if (transition.output.isPresent()) {
                    dot.append("  \"" + ids.get(state) + "\" -> \"" + ids.get(next) + "\" [label = \""
                            + new String(label) + "/" + transition.output.getAsInt() + "\"];\n");
                } else {
                    dot.append("  \"" + ids.get(state) + "\" -> \"" + ids.get(next) + "\" [label = \""
                            + new String(label) + "\"];\n");
                }
            }
        }
        dot.append("}\n");
        dot.trimToSize();
        return dot.toString();
    }

    public static void main(String[] args) {
        final List<java.util.Map.Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(Map.entry("abc", 1));
        entries.add(Map.entry("bd", 2));
        entries.add(Map.entry("bde", 3));

        // entries.add(Map.entry("apr", 30));
        // entries.add(Map.entry("aug", 31));
        // entries.add(Map.entry("dec", 31));
        // entries.add(Map.entry("feb", 28));
        // entries.add(Map.entry("jan", 31));
        // entries.add(Map.entry("jul", 31));
        // entries.add(Map.entry("jun", 30));
        // entries.add(Map.entry("may", 31));

        // entries.add(Map.entry("mop", 0));
        // entries.add(Map.entry("moth", 1));
        // entries.add(Map.entry("pop", 2));
        // entries.add(Map.entry("star", 3));
        // entries.add(Map.entry("stop", 4));
        // entries.add(Map.entry("top", 5));

        final FSTBuilder builder = new FSTBuilder();
        final FST fst = builder.build(entries.stream().map(entry -> Map
                .entry(entry.getKey().getBytes(java.nio.charset.StandardCharsets.US_ASCII), entry.getValue())));

        final String dot = FSTBuilder.translateToDot(fst.states);
        System.out.println(dot);
    }
}
