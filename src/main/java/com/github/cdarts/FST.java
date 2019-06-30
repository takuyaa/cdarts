package com.github.cdarts;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class FST<T> implements Iterable<State<T>> {
    final Set<FrozenState<T>> states;
    final FrozenState<T> initialState;

    FST(StatesDict<T> dict, FrozenState<T> initialState) {
        this.states = dict.states();
        this.initialState = initialState;
    }

    @Override
    public Iterator<State<T>> iterator() {
        HashSet<State<T>> isVisited = new HashSet<>();
        Deque<State<T>> stack = new ArrayDeque<>();
        stack.push(initialState);

        return new Iterator<State<T>>() {
            @Override
            public boolean hasNext() {
                while (true) {
                    var next = stack.peek();
                    if (next == null) {
                        return false;
                    }
                    if (!isVisited.contains(next)) {
                        return true;
                    }
                    // exhoust visited states
                    stack.pop();
                }
            }

            @Override
            public State<T> next() {
                while (true) {
                    if (stack.peek() == null) {
                        return null;
                    }
                    var next = stack.pop();
                    if (!isVisited.contains(next)) {
                        isVisited.add(next);
                        // push to stack in reverse order
                        for (int i = next.transitions.size() - 1; i >= 0; i--) {
                            stack.push(next.transitions.get(i).nextState);
                        }
                        return next;
                    }
                    // skip visited states
                    stack.pop();
                }
            }
        };
    }

    String toDot() {
        final var dot = new StringBuilder(1024);
        dot.append("digraph G {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape=circle fixedsize=true];\n");

        Map<State<T>, Long> ids = new HashMap<>();
        long maxId = 1;
        for (FrozenState<T> state : this.states) {
            if (!ids.containsKey(state)) {
                ids.put(state, maxId++);
            }

            // draw a node
            final var stateOutput = state.getStateOutput();
            if (stateOutput.isPresent() || state.isFinal) {
                final long stateId = ids.get(state);
                dot.append("  \"" + stateId + "\" [label=\"" + stateId);
                if (stateOutput.isPresent()) {
                    // draw state output as node label
                    dot.append("/" + outputToString(stateOutput));
                }
                dot.append("\"");
                if (state.isFinal) {
                    dot.append(" peripheries=2");
                }
                dot.append("];\n");
            }

            // draw edges
            for (Transition<T> transition : state.transitions) {
                final State<T> next = transition.nextState;
                final byte[] label = { transition.label };
                final Optional<T> output = transition.output;
                final long currentStateId = ids.get(state);
                final long nextStateId = ids.get(next);
                if (output.isEmpty()) {
                    dot.append("  \"" + currentStateId + "\" -> \"" + nextStateId + "\" [label=\"" + new String(label)
                            + "\"];\n");
                } else {
                    dot.append("  \"" + currentStateId + "\" -> \"" + nextStateId + "\" [label=\"" + new String(label)
                            + "/" + outputToString(output) + "\"];\n");
                }
            }
        }
        dot.append("}");
        dot.trimToSize();
        return dot.toString();
    }

    private String outputToString(Optional<T> output) {
        if (output.isEmpty()) {
            return "";
        }
        if (output.get() instanceof byte[]) {
            return new String((byte[]) output.get());
        } else {
            return output.get().toString();
        }
    }
}
