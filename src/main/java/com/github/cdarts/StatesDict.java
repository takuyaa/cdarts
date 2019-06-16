package com.github.cdarts;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;

class StatesDict {
    // preserve insetion order for iteration
    final LinkedHashMap<FrozenState, FrozenState> dict = new LinkedHashMap<>();

    FrozenState findMinimized(MutableState state) {
        return member(state).orElseGet(() -> {
            FrozenState r = state.freeze();
            insert(r);
            return r;
        });
    }

    Optional<FrozenState> member(State state) {
        return Optional.ofNullable(dict.get(state));
    }

    void insert(FrozenState state) {
        // state object acts as key and value
        this.dict.put(state, state);
    }

    Set<FrozenState> states() {
        return this.dict.keySet();
    }
}
