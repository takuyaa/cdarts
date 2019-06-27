package com.github.cdarts;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class FST<T> {
    final Set<FrozenState<T>> states;
    final FrozenState<T> initialState;

    FST(StatesDict<T> dict, FrozenState<T> initialState) {
        this.states = dict.states();
        this.initialState = initialState;
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
                dot.append("  \"" + ids.get(state) + "\" [label=\"" + ids.get(state));
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
                if (!ids.containsKey(next)) {
                    ids.put(state, maxId++);
                }
                final byte[] label = { transition.label };
                final var output = transition.output;
                if (output.isEmpty()) {
                    dot.append("  \"" + ids.get(state) + "\" -> \"" + ids.get(next) + "\" [label=\"" + new String(label)
                            + "\"];\n");
                    continue;
                }
                dot.append("  \"" + ids.get(state) + "\" -> \"" + ids.get(next) + "\" [label=\"" + new String(label)
                        + "/" + outputToString(output) + "\"];\n");
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
