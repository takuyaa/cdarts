package com.github.cdarts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

public class StateTest {
    @Test
    public void testSameFrozenStatesAreEqual() {
        FrozenState state1;
        FrozenState state2;

        state1 = new FrozenState(false, new Transition[] {}, OptionalInt.empty());
        state2 = new FrozenState(false, new Transition[] {}, OptionalInt.empty());
        assertEquals(state1, state2);

        state1 = new FrozenState(true, new Transition[] {}, OptionalInt.empty());
        state2 = new FrozenState(true, new Transition[] {}, OptionalInt.empty());
        assertEquals(state1, state2);
    }

    @Test
    public void testDifferentFrozenStatesAreNotEqual() {
        FrozenState state1;
        FrozenState state2;

        state1 = new FrozenState(false, new Transition[] {}, OptionalInt.empty());
        state2 = new FrozenState(true, new Transition[] {}, OptionalInt.empty());
        assertNotEquals(state1, state2);

        state1 = new FrozenState(false, new Transition[] {}, OptionalInt.empty());
        state2 = new FrozenState(false,
                new Transition[] { new Transition((byte) 1, new MutableState(), OptionalInt.empty()) },
                OptionalInt.empty());
        assertNotEquals(state1, state2);
    }

    @Test
    public void testHashCodeOfSameFrozenStatesAreEqual() {
        FrozenState state1;
        FrozenState state2;

        state1 = new FrozenState(false, new Transition[] {}, OptionalInt.empty());
        state2 = new FrozenState(false, new Transition[] {}, OptionalInt.empty());
        assertEquals(state1.hashCode(), state2.hashCode());

        state1 = new FrozenState(true, new Transition[] {}, OptionalInt.empty());
        state2 = new FrozenState(true, new Transition[] {}, OptionalInt.empty());
        assertEquals(state1.hashCode(), state2.hashCode());
    }

    @Test
    public void testFrozenStatesAreDifferentIfHashCodeIsDifferent() {
        FrozenState state1;
        FrozenState state2;

        state1 = new FrozenState(false, new Transition[] {}, OptionalInt.empty());
        state2 = new FrozenState(true, new Transition[] {}, OptionalInt.empty());
        if (state1.hashCode() != state2.hashCode()) {
            assertNotEquals(state1, state2);
        }

        state1 = new FrozenState(false, new Transition[] {}, OptionalInt.empty());
        state2 = new FrozenState(false,
                new Transition[] { new Transition((byte) 1, new MutableState(), OptionalInt.empty()) },
                OptionalInt.empty());
        if (state1.hashCode() != state2.hashCode()) {
            assertNotEquals(state1, state2);
        }
    }
}
