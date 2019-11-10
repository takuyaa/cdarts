package com.github.dagr;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class State<T> {
    public boolean isFinal;
    public List<Transition<T>> transitions;
    public Optional<T> output;

    public Optional<State<T>> transit(byte label) {
        for (Transition<T> transition : transitions) {
            if (label == transition.label) {
                return Optional.ofNullable(transition.nextState);
            }
        }
        return Optional.empty();
    }

    public Optional<T> getStateOutput() {
        return this.output;
    }

    public Optional<T> transitOutput(byte label) {
        for (Transition<T> transition : transitions) {
            if (label == transition.label) {
                return transition.output;
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof State)) {
            return false;
        }
        try {
            @SuppressWarnings("unchecked")
            final State<T> other = (State<T>) obj;

            if (this.transitions.size() != other.transitions.size()) {
                return false;
            }
            for (Transition<T> transition : this.transitions) {
                Optional<T> output = other.transitOutput(transition.label);
                if (!output.equals(transition.output)) {
                    return false;
                }
                Optional<State<T>> next = other.transit(transition.label);
                if (next.isEmpty()) {
                    return false;
                }
                // we check these 2 next states are the same Java object
                State<T> nextState = next.get();
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
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = result * PRIME + (this.isFinal ? 1231 : 1237);
        result = result * PRIME + this.output.hashCode();
        for (Transition<T> transition : this.transitions) {
            result = result * PRIME + transition.label;
            result = result * PRIME + transition.nextState.hashCode();
            result = result * PRIME + transition.output.hashCode();
        }
        return result;
    }
}

class FrozenState<T> extends State<T> {
    // TODO make FrozenTransition class, and use it here
    FrozenState(boolean isFinal, List<Transition<T>> transitions, Optional<T> output) {
        // assert that all next states from FrozenState are instances of FrozenState
        assert transitions.stream().allMatch(t -> t.nextState instanceof FrozenState);
        this.isFinal = isFinal;
        this.transitions = transitions;
        this.output = output;
    }
}

class MutableState<T> extends State<T> {
    MutableState() {
        this.isFinal = false;
        this.transitions = new ArrayList<Transition<T>>(0);
        this.output = Optional.empty();
    }

    MutableState(boolean isFinal, List<Transition<T>> transitions, Optional<T> output) {
        this.isFinal = isFinal;
        this.transitions = transitions;
        this.output = output;
    }

    void clear() {
        this.isFinal = false;
        this.transitions = new ArrayList<Transition<T>>(0);
        this.output = Optional.empty();
    }

    FrozenState<T> freeze() {
        return new FrozenState<T>(this.isFinal, new ArrayList<Transition<T>>(this.transitions), this.output);
    }

    void setTransition(byte label, State<T> nextState) {
        for (int i = 0; i < this.transitions.size(); i++) {
            Transition<T> existingTransition = this.transitions.get(i);
            if (existingTransition.label == label) {
                // if the same label transition exists, replace it
                this.transitions.set(i, new Transition<T>(label, nextState, existingTransition.output));
                return;
            }
        }
        this.transitions.add(new Transition<T>(label, nextState, Optional.empty()));
    }

    void setStateOutput(Optional<T> output) {
        assert this.isFinal;
        this.output = output;
    }

    void setTransitionOutput(byte label, Optional<T> output) {
        for (Transition<T> transition : transitions) {
            if (label == transition.label) {
                transition.output = output;
                return;
            }
        }
    }
}
