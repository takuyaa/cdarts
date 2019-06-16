package com.github.cdarts;

import java.util.Set;

public class FST {
    final Set<FrozenState> states;
    final FrozenState initialState;

    FST(StatesDict dict, FrozenState initialState) {
        this.states = dict.states();
        this.initialState = initialState;
    }
}
