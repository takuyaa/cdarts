package com.github.dagr;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;

class StatesDict<T> {
    // preserve insetion order for iteration
    final LinkedHashMap<FrozenState<T>, FrozenState<T>> dict = new LinkedHashMap<>();

    FrozenState<T> findMinimized(MutableState<T> state) {
        return member(state).orElseGet(() -> {
            FrozenState<T> r = state.freeze();
            insert(r);
            return r;
        });
    }

    Optional<FrozenState<T>> member(State<T> state) {
        return Optional.ofNullable(dict.get(state));
    }

    void insert(FrozenState<T> state) {
        // state object acts as key and value
        this.dict.put(state, state);
    }

    Set<FrozenState<T>> states() {
        return this.dict.keySet();
    }
}
