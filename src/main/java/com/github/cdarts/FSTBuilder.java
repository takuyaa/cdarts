package com.github.cdarts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class FSTBuilder<T> {
    public FST<T> build(Stream<Map.Entry<byte[], T>> entries) {
        final var statesDict = new StatesDict<T>();
        final List<MutableState<T>> tempStates = new ArrayList<>();

        var lastEntry = entries.reduce(Map.entry(new byte[0], defaultValue()), (prev, current) -> {
            final byte[] prevWord = prev.getKey();
            final byte[] currentWord = current.getKey();
            final T currentOutput = current.getValue();

            assert !prevWord.equals(currentWord) : "Multiple output is not supported"; // Throw Exception
            assert compare(prevWord, currentWord) < 0 : "Input keys must be sorted"; // Throw Exception

            // initialize buffer
            while (tempStates.size() <= currentWord.length) {
                tempStates.add(new MutableState<T>());
            }

            final int prefixLengthPlus1 = prefixLength(prevWord, currentWord) + 1;

            // we minimize the states from the suffix of the previous word
            for (int i = prevWord.length; i >= prefixLengthPlus1; i--) {
                final MutableState<T> prevState = tempStates.get(i - 1);
                final FrozenState<T> nextState = statesDict.findMinimized(tempStates.get(i));
                prevState.setTransition(prevWord[i - 1], nextState);
            }
            // this loop initializes the states from the suffix of the previous word
            for (int i = prefixLengthPlus1; i <= currentWord.length; i++) {
                final MutableState<T> prevState = tempStates.get(i - 1);
                final MutableState<T> nextState = tempStates.get(i);
                nextState.clear();
                prevState.setTransition(currentWord[i - 1], nextState);
            }
            // terminate last state for currentWord
            final var lastState = tempStates.get(currentWord.length);
            lastState.isFinal = true;
            Optional.empty();
            lastState.setStateOutput(Optional.empty());

            Optional<T> currentOutputTail = Optional.of(currentOutput);
            for (int i = 1; i < prefixLengthPlus1; i++) {
                final MutableState<T> prevState = tempStates.get(i - 1);
                final MutableState<T> nextState = tempStates.get(i);

                final Optional<T> prevOutput = prevState.transitOutput(currentWord[i - 1]);
                final Optional<T> commonPrefix = prefix(prevOutput, currentOutputTail);
                final Optional<T> wordSuffix = subtract(prevOutput, commonPrefix);

                prevState.setTransitionOutput(currentWord[i - 1], commonPrefix);
                // iterate over all transitions from nextState
                for (Transition<T> transition : nextState.transitions) {
                    transition.output = concat(wordSuffix, transition.output);
                }
                if (nextState.isFinal) {
                    Optional<T> stateOutput = nextState.getStateOutput();
                    nextState.setStateOutput(concat(wordSuffix, stateOutput));
                }
                currentOutputTail = subtract(currentOutputTail, commonPrefix);
            }

            final var lastPrefixState = tempStates.get(prefixLengthPlus1 - 1);
            lastPrefixState.setTransitionOutput(currentWord[prefixLengthPlus1 - 1], currentOutputTail);

            // pass currentWord to next iteration
            return current;
        });

        // here we are minimizing the states of the last word
        final byte[] currentWord = lastEntry.getKey();
        for (int i = currentWord.length; i > 0; i--) {
            final MutableState<T> prevState = tempStates.get(i - 1);
            final FrozenState<T> nextState = statesDict.findMinimized(tempStates.get(i));
            prevState.setTransition(currentWord[i - 1], nextState);
        }
        final FrozenState<T> initialState = statesDict.findMinimized(tempStates.get(0));

        return new FST<T>(statesDict, initialState);
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

    abstract T defaultValue();

    abstract Optional<T> prefix(Optional<T> a, Optional<T> b);

    abstract Optional<T> concat(Optional<T> a, Optional<T> b);

    abstract Optional<T> subtract(Optional<T> a, Optional<T> b);
}
