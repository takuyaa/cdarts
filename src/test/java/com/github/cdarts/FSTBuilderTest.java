package com.github.cdarts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

public class FSTBuilderTest {
    FST buildFST(List<Map.Entry<String, Integer>> entries) {
        final FSTBuilder builder = new FSTBuilder();
        return builder.build(entries.stream()
                .map(entry -> Map.entry(entry.getKey().getBytes(StandardCharsets.US_ASCII), entry.getValue())));
    }

    @Test
    public void testBuildWithSimpleKeys() {
        final List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(Map.entry("aa", 1));
        entries.add(Map.entry("ab", 2));

        final var fst = buildFST(entries);
        assertEquals(3, fst.states.size());

        var next = fst.initialState.transit((byte) 'a').get();
        assertEquals(fst.initialState.transitOutput((byte) 'a'), OptionalInt.empty());
        assertEquals(next.getStateOutput(), OptionalInt.empty());

        var finalStateA = next.transit((byte) 'a').get();
        assertEquals(finalStateA.isFinal, true);
        assertEquals(finalStateA.getStateOutput(), OptionalInt.empty());
        assertEquals(next.transitOutput((byte) 'a').getAsInt(), 1);

        var finalStateB = next.transit((byte) 'b').get();
        assertEquals(finalStateA == finalStateB, true);
        assertEquals(next.transitOutput((byte) 'b').getAsInt(), 2);
    }

    @Test
    public void testBuildWithSimpleKeysAndSameOutputs() {
        final List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(Map.entry("aa", 1));
        entries.add(Map.entry("ab", 1));

        var fst = buildFST(entries);
        assertEquals(3, fst.states.size());

        var next = fst.initialState.transit((byte) 'a').get();
        var nextOutput = fst.initialState.transitOutput((byte) 'a').getAsInt();
        assertEquals(nextOutput, 1);
        assertEquals(next.getStateOutput(), OptionalInt.empty());

        var finalStateA = next.transit((byte) 'a').get();
        assertEquals(finalStateA.isFinal, true);
        assertEquals(finalStateA.getStateOutput(), OptionalInt.empty());
        assertEquals(next.transitOutput((byte) 'a'), OptionalInt.empty());

        var finalStateB = next.transit((byte) 'b').get();
        assertEquals(finalStateA == finalStateB, true);
        assertEquals(next.transitOutput((byte) 'b'), OptionalInt.empty());
    }

    @Test
    public void testBuildWithKeysHaveSamePrefix() {
        final List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(Map.entry("a", 1));
        entries.add(Map.entry("ab", 2));

        var fst = buildFST(entries);
        assertEquals(3, fst.states.size());

        var stateA = fst.initialState.transit((byte) 'a').get();
        assertEquals(fst.initialState.transitOutput((byte) 'a'), OptionalInt.empty());
        assertEquals(stateA.isFinal, true);
        assertEquals(stateA.getStateOutput().getAsInt(), 1);

        var stateB = stateA.transit((byte) 'b').get();
        assertEquals(stateA.transitOutput((byte) 'b').getAsInt(), 2);
        assertEquals(stateB.isFinal, true);
        assertEquals(stateB.getStateOutput(), OptionalInt.empty());
    }

    @Test
    public void testBuildWithKeysHaveSamePrefixAndSameOutputs() {
        final List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(Map.entry("a", 1));
        entries.add(Map.entry("ab", 1));

        var fst = buildFST(entries);
        assertEquals(3, fst.states.size());

        var stateA = fst.initialState.transit((byte) 'a').get();
        assertEquals(fst.initialState.transitOutput((byte) 'a').getAsInt(), 1);
        assertEquals(stateA.isFinal, true);
        assertEquals(stateA.getStateOutput(), OptionalInt.empty());

        var stateB = stateA.transit((byte) 'b').get();
        assertEquals(stateA.transitOutput((byte) 'b'), OptionalInt.empty());
        assertEquals(stateB.isFinal, true);
        assertEquals(stateB.getStateOutput(), OptionalInt.empty());
    }

    @Test
    public void testBuildWithFixedLengthKeys() {
        final List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(Map.entry("apr", 30));
        entries.add(Map.entry("aug", 31));
        entries.add(Map.entry("dec", 31));
        entries.add(Map.entry("feb", 28));
        entries.add(Map.entry("jan", 31));
        entries.add(Map.entry("jul", 31));
        entries.add(Map.entry("jun", 30));
        entries.add(Map.entry("may", 31));

        var fst = buildFST(entries);
        assertEquals(14, fst.states.size());
    }

    @Test
    public void testBuildComplicatedFST() {
        final List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(Map.entry("mop", 0));
        entries.add(Map.entry("moth", 1));
        entries.add(Map.entry("pop", 2));
        entries.add(Map.entry("star", 3));
        entries.add(Map.entry("stop", 4));
        entries.add(Map.entry("top", 5));

        var fst = buildFST(entries);
        assertEquals(10, fst.states.size());
    }
}
