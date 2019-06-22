package com.github.cdarts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class StateTest {
    List<Transition<Integer>> createEmptyTransition() {
        return new ArrayList<Transition<Integer>>();
    }

    List<Transition<Integer>> createTransition(Transition<Integer> transition) {
        var list = new ArrayList<Transition<Integer>>();
        list.add(transition);
        return list;
    }

    @Test
    public void testSameFrozenStatesAreEqual() {
        FrozenState<Integer> state1;
        FrozenState<Integer> state2;

        state1 = new FrozenState<Integer>(false, createEmptyTransition(), Optional.empty());
        state2 = new FrozenState<Integer>(false, createEmptyTransition(), Optional.empty());
        assertEquals(state1, state2);

        state1 = new FrozenState<Integer>(true, createEmptyTransition(), Optional.empty());
        state2 = new FrozenState<Integer>(true, createEmptyTransition(), Optional.empty());
        assertEquals(state1, state2);
    }

    @Test
    public void testDifferentFrozenStatesAreNotEqual() {
        FrozenState<Integer> state1;
        FrozenState<Integer> state2;

        state1 = new FrozenState<Integer>(false, createEmptyTransition(), Optional.empty());
        state2 = new FrozenState<Integer>(true, createEmptyTransition(), Optional.empty());
        assertNotEquals(state1, state2);

        FrozenState<Integer> anotherState = new FrozenState<Integer>(false, createEmptyTransition(), Optional.empty());
        state1 = new FrozenState<Integer>(false, createEmptyTransition(), Optional.empty());
        state2 = new FrozenState<Integer>(false,
                createTransition(new Transition<Integer>((byte) 1, anotherState, Optional.empty())), Optional.empty());
        assertNotEquals(state1, state2);
    }

    @Test
    public void testHashCodeOfSameFrozenStatesAreEqual() {
        FrozenState<Integer> state1;
        FrozenState<Integer> state2;

        state1 = new FrozenState<Integer>(false, createEmptyTransition(), Optional.empty());
        state2 = new FrozenState<Integer>(false, createEmptyTransition(), Optional.empty());
        assertEquals(state1.hashCode(), state2.hashCode());

        state1 = new FrozenState<Integer>(true, createEmptyTransition(), Optional.empty());
        state2 = new FrozenState<Integer>(true, createEmptyTransition(), Optional.empty());
        assertEquals(state1.hashCode(), state2.hashCode());
    }

    @Test
    public void testFrozenStatesAreDifferentIfHashCodeIsDifferent() {
        FrozenState<Integer> state1;
        FrozenState<Integer> state2;

        state1 = new FrozenState<Integer>(false, createEmptyTransition(), Optional.empty());
        state2 = new FrozenState<Integer>(true, createEmptyTransition(), Optional.empty());
        if (state1.hashCode() != state2.hashCode()) {
            assertNotEquals(state1, state2);
        }

        FrozenState<Integer> anotherState = new FrozenState<Integer>(false, createEmptyTransition(), Optional.empty());
        state1 = new FrozenState<Integer>(false, createEmptyTransition(), Optional.empty());
        state2 = new FrozenState<Integer>(false,
                createTransition(new Transition<Integer>((byte) 1, anotherState, Optional.empty())), Optional.empty());
        if (state1.hashCode() != state2.hashCode()) {
            assertNotEquals(state1, state2);
        }
    }
}
