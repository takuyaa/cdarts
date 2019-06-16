package com.github.cdarts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FST {
    final Set<FrozenState> states;
    final FrozenState initialState;

    FST(StatesDict dict, FrozenState initialState) {
        this.states = dict.states();
        this.initialState = initialState;
    }

    String translateToDot() {
        final var dot = new StringBuilder(1024);
        dot.append("digraph G {\n");
        dot.append("  rankdir = LR;\n");
        dot.append("  node [shape = circle];\n");

        Map<State, Long> ids = new HashMap<>();
        long maxId = 1;
        for (FrozenState state : this.states) {
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
}
