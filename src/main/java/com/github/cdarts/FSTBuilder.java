package com.github.cdarts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
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
            lastState.setStateOutput(OptionalInt.empty());

            OptionalInt currentOutputTail = OptionalInt.of(currentOutput);
            for (int i = 1; i < prefixLengthPlus1; i++) {
                final MutableState prevState = tempStates.get(i - 1);
                final MutableState nextState = tempStates.get(i);

                final OptionalInt prevOutput = prevState.transitOutput(currentWord[i - 1]);
                final OptionalInt commonPrefix = prefix(prevOutput, currentOutputTail);
                final OptionalInt wordSuffix = subtract(prevOutput, commonPrefix);

                prevState.setTransitionOutput(currentWord[i - 1], commonPrefix);
                // iterate over all transitions from nextState
                for (Transition transition : nextState.transitions) {
                    transition.output = concat(wordSuffix, transition.output);
                }
                if (nextState.isFinal) {
                    OptionalInt stateOutput = nextState.getStateOutput();
                    nextState.setStateOutput(concat(wordSuffix, stateOutput));
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

    static OptionalInt concat(OptionalInt a, OptionalInt b) {
        if (a.isEmpty()) {
            return OptionalInt.empty();
        }
        if (b.isEmpty()) {
            return a;
        }
        if (a.equals(b)) {
            return a;
        }
        // add outputs
        // return OptionalInt.of(a + b.getAsInt());

        // adopt former value
        return a;
    }

    static OptionalInt subtract(OptionalInt a, OptionalInt b) {
        if (a.isEmpty()) {
            assert false;
            return OptionalInt.empty();
        }
        if (b.isEmpty()) {
            return a;
        }
        if (a.equals(b)) {
            return OptionalInt.empty();
        }
        // split outputs
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

        final String dot = fst.translateToDot();
        System.out.println(dot);
    }
}
