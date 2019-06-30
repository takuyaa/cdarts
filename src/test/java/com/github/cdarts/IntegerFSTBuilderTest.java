package com.github.cdarts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class IntegerFSTBuilderTest {
    FST<Integer> buildFST(List<Map.Entry<String, Integer>> lexicon) {
        final var builder = new IntegerFSTBuilder();
        return builder.build(lexicon.stream().map(entry -> Map.entry(entry.getKey().getBytes(), entry.getValue())));
    }

    @Test
    public void testBuildWithSimpleKeys() {
        final List<Map.Entry<String, Integer>> lexicon = new ArrayList<>();
        lexicon.add(Map.entry("aa", 1));
        lexicon.add(Map.entry("ab", 2));

        final var fst = buildFST(lexicon);
        assertEquals(3, fst.states.size());

        var state1 = fst.initialState;
        assertEquals(state1.isFinal, false);
        assertEquals(state1.getStateOutput(), Optional.empty());
        assertEquals(state1.transitOutput((byte) 'a'), Optional.empty());

        var state2 = state1.transit((byte) 'a').get();
        assertEquals(state2.isFinal, false);
        assertEquals(state2.getStateOutput(), Optional.empty());
        assertEquals(state2.transitOutput((byte) 'a').get(), 1);
        assertEquals(state2.transitOutput((byte) 'b').get(), 2);

        var state3a = state2.transit((byte) 'a').get();
        assertEquals(state3a.isFinal, true);
        assertEquals(state3a.getStateOutput(), Optional.empty());

        var state3b = state2.transit((byte) 'b').get();
        assertEquals(state3a == state3b, true);
    }

    @Test
    public void testBuildWithSimpleKeysAndSameOutputs() {
        final List<Map.Entry<String, Integer>> lexicon = new ArrayList<>();
        lexicon.add(Map.entry("aa", 1));
        lexicon.add(Map.entry("ab", 1));

        var fst = buildFST(lexicon);
        assertEquals(3, fst.states.size());

        var state1 = fst.initialState;
        assertEquals(state1.isFinal, false);
        assertEquals(state1.getStateOutput(), Optional.empty());
        assertEquals(state1.transitOutput((byte) 'a').get(), 1);

        var state2 = state1.transit((byte) 'a').get();
        assertEquals(state2.isFinal, false);
        assertEquals(state2.getStateOutput(), Optional.empty());
        assertEquals(state2.transitOutput((byte) 'a'), Optional.empty());
        assertEquals(state2.transitOutput((byte) 'b'), Optional.empty());

        var state3a = state2.transit((byte) 'a').get();
        assertEquals(state3a.isFinal, true);
        assertEquals(state3a.getStateOutput(), Optional.empty());

        var state3b = state2.transit((byte) 'b').get();
        assertEquals(state3a == state3b, true);
    }

    @Test
    public void testBuildWithKeysHaveSamePrefix() {
        final List<Map.Entry<String, Integer>> lexicon = new ArrayList<>();
        lexicon.add(Map.entry("a", 1));
        lexicon.add(Map.entry("ab", 2));

        var fst = buildFST(lexicon);
        assertEquals(3, fst.states.size());

        var state1 = fst.initialState;
        assertEquals(state1.isFinal, false);
        assertEquals(state1.getStateOutput(), Optional.empty());
        assertEquals(state1.transitOutput((byte) 'a'), Optional.empty());

        var state2 = state1.transit((byte) 'a').get();
        assertEquals(state2.isFinal, true);
        assertEquals(state2.getStateOutput().get(), 1);
        assertEquals(state2.transitOutput((byte) 'b').get(), 2);

        var state3 = state2.transit((byte) 'b').get();
        assertEquals(state3.isFinal, true);
        assertEquals(state3.getStateOutput(), Optional.empty());
    }

    @Test
    public void testBuildWithKeysHaveSamePrefixAndSameOutputs() {
        final List<Map.Entry<String, Integer>> lexicon = new ArrayList<>();
        lexicon.add(Map.entry("a", 1));
        lexicon.add(Map.entry("ab", 1));

        var fst = buildFST(lexicon);
        assertEquals(3, fst.states.size());

        var state1 = fst.initialState;
        assertEquals(state1.isFinal, false);
        assertEquals(state1.getStateOutput(), Optional.empty());
        assertEquals(state1.transitOutput((byte) 'a').get(), 1);

        var state2 = state1.transit((byte) 'a').get();
        assertEquals(state2.isFinal, true);
        assertEquals(state2.getStateOutput(), Optional.empty());
        assertEquals(state2.transitOutput((byte) 'b'), Optional.empty());

        var state3 = state2.transit((byte) 'b').get();
        assertEquals(state3.isFinal, true);
        assertEquals(state3.getStateOutput(), Optional.empty());
    }

    @Test
    public void testBuildWithOutputsHaveSamePrefixAndSuffix() {
        final List<Map.Entry<String, Integer>> lexicon = new ArrayList<>();
        lexicon.add(Map.entry("aaa", 111));
        lexicon.add(Map.entry("aba", 121));

        var fst = buildFST(lexicon);
        assertEquals(4, fst.states.size());

        var state1 = fst.initialState;
        assertEquals(state1.isFinal, false);
        assertEquals(state1.getStateOutput(), Optional.empty());
        assertEquals(state1.transitOutput((byte) 'a'), Optional.empty());

        var state2 = state1.transit((byte) 'a').get();
        assertEquals(state2.isFinal, false);
        assertEquals(state2.getStateOutput(), Optional.empty());
        assertEquals(state2.transitOutput((byte) 'a').get(), 111);
        assertEquals(state2.transitOutput((byte) 'b').get(), 121);

        var state3a = state2.transit((byte) 'a').get();
        assertEquals(state3a.isFinal, false);
        assertEquals(state3a.getStateOutput(), Optional.empty());
        assertEquals(state3a.transitOutput((byte) 'a'), Optional.empty());

        var state3b = state2.transit((byte) 'b').get();
        assertEquals(state3a == state3b, true);

        var state4 = state3a.transit((byte) 'a').get();
        assertEquals(state4.isFinal, true);
        assertEquals(state4.getStateOutput(), Optional.empty());
    }

    @Test
    public void testBuildWithFixedLengthKeys() {
        final List<Map.Entry<String, Integer>> lexicon = new ArrayList<>();
        lexicon.add(Map.entry("apr", 30));
        lexicon.add(Map.entry("aug", 31));
        lexicon.add(Map.entry("dec", 31));
        lexicon.add(Map.entry("feb", 28));
        lexicon.add(Map.entry("jan", 31));
        lexicon.add(Map.entry("jul", 31));
        lexicon.add(Map.entry("jun", 30));
        lexicon.add(Map.entry("may", 31));

        var fst = buildFST(lexicon);
        assertEquals(14, fst.states.size());
    }

    @Test
    public void testBuildComplicatedFST() {
        final List<Map.Entry<String, Integer>> lexicon = new ArrayList<>();
        lexicon.add(Map.entry("mop", 0));
        lexicon.add(Map.entry("moth", 1));
        lexicon.add(Map.entry("pop", 2));
        lexicon.add(Map.entry("star", 3));
        lexicon.add(Map.entry("stop", 4));
        lexicon.add(Map.entry("top", 5));

        var fst = buildFST(lexicon);
        assertEquals(10, fst.states.size());
    }
}
