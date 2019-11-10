package com.github.dagr;

import java.util.Optional;

// TODO rename Transitions, and update here
public class Transition<T> {
    public final byte label;
    public final State<T> nextState;
    public Optional<T> output;

    Transition(byte label, State<T> nextState, Optional<T> output) {
        this.label = label;
        this.nextState = nextState;
        this.output = output;
    }

    // TODO implement equals() and hashCode()
}
