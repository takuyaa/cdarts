package com.github.cdarts;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

abstract class State {
    boolean isFinal;
    Transition[] transitions;
    OptionalInt output;

    Optional<State> transit(byte label) {
        for (Transition transition : transitions) {
            if (label == transition.label) {
                return Optional.ofNullable(transition.nextState);
            }
        }
        return Optional.empty();
    }

    OptionalInt getStateOutput() {
        return this.output;
    }

    OptionalInt transitOutput(byte label) {
        for (Transition transition : transitions) {
            if (label == transition.label) {
                return transition.output;
            }
        }
        return OptionalInt.empty();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof State)) {
            return false;
        }
        State other = (State) obj;
        if (this.transitions.length != other.transitions.length) {
            return false;
        }
        for (Transition transition : this.transitions) {
            OptionalInt output = other.transitOutput(transition.label);
            if (!output.equals(transition.output)) {
                return false;
            }
            Optional<State> next = other.transit(transition.label);
            if (next.isEmpty()) {
                return false;
            }
            // we check these 2 next states are the same Java object
            State nextState = next.get();
            assert other instanceof FrozenState ? nextState instanceof FrozenState : true;
            assert this instanceof FrozenState ? transition.nextState instanceof FrozenState : true;
            if (nextState != transition.nextState) {
                return false;
            }
        }
        if (this.isFinal != other.isFinal) {
            return false;
        }
        if (!this.output.equals(other.output)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = result * PRIME + (this.isFinal ? 1231 : 1237);
        result = result * PRIME + this.output.hashCode();
        for (Transition transition : this.transitions) {
            result = result * PRIME + transition.label;
            result = result * PRIME + transition.nextState.hashCode();
            result = result * PRIME + transition.output.hashCode();
        }
        return result;
    }
}

class FrozenState extends State {
    // TODO make FrozenTransition class, and use it here
    FrozenState(boolean isFinal, Transition[] transitions, OptionalInt output) {
        this.isFinal = isFinal;
        this.transitions = transitions;
        this.output = output;
    }
}

class MutableState extends State {
    MutableState() {
        this.isFinal = false;
        this.transitions = new Transition[0];
        this.output = OptionalInt.empty();
    }

    MutableState(boolean isFinal, Transition[] transitions, OptionalInt output) {
        this.isFinal = isFinal;
        this.transitions = transitions;
        this.output = output;
    }

    void clear() {
        this.isFinal = false;
        this.transitions = new Transition[0];
        this.output = OptionalInt.empty();
    }

    FrozenState freeze() {
        return new FrozenState(this.isFinal, Arrays.copyOf(this.transitions, this.transitions.length), this.output);
    }

    void setTransition(byte label, State nextState) {
        final int currentArrayLength = this.transitions.length;
        for (int i = 0; i < currentArrayLength; i++) {
            Transition existingTransition = this.transitions[i];
            if (existingTransition.label == label) {
                // if the same label transition exists, replace it
                this.transitions[i] = new Transition(label, nextState, existingTransition.output);
                return;
            }
        }
        Transition[] newTransitions = Arrays.copyOf(this.transitions, currentArrayLength + 1);
        newTransitions[currentArrayLength] = new Transition(label, nextState, OptionalInt.empty());
        this.transitions = newTransitions;
    }

    void setStateOutput(OptionalInt output) {
        this.output = output;
    }

    void setTransitionOutput(byte label, OptionalInt output) {
        for (Transition transition : transitions) {
            if (label == transition.label) {
                transition.output = output;
                return;
            }
        }
    }
}

// TODO rename Transitions, and update here
class Transition {
    final byte label;
    final State nextState;
    OptionalInt output;

    Transition(byte label, State nextState, OptionalInt output) {
        this.label = label;
        this.nextState = nextState;
        this.output = output;
    }

    // TODO implement equals() and hashCode()
}
