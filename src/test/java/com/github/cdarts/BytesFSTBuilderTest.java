package com.github.cdarts;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class BytesFSTBuilderTest {
    FST<byte[]> buildFST(List<Map.Entry<String, String>> lexicon) {
        final var builder = new BytesFSTBuilder();
        return builder.build(lexicon.stream().map(entry -> Map.entry(entry.getKey().getBytes(StandardCharsets.US_ASCII),
                entry.getValue().getBytes(StandardCharsets.US_ASCII))));
    }

    @Test
    public void testBuildWithSimpleKeys() {
        final List<Map.Entry<String, String>> lexicon = new ArrayList<>();
        lexicon.add(Map.entry("aa", "1"));
        lexicon.add(Map.entry("ab", "2"));

        final var fst = buildFST(lexicon);
        assertEquals(3, fst.states.size());

        var next = fst.initialState.transit((byte) 'a').get();
        assertEquals(fst.initialState.transitOutput((byte) 'a'), Optional.empty());
        assertEquals(next.getStateOutput(), Optional.empty());

        var finalStateA = next.transit((byte) 'a').get();
        assertEquals(finalStateA.isFinal, true);
        assertEquals(finalStateA.getStateOutput(), Optional.empty());
        assertArrayEquals(next.transitOutput((byte) 'a').get(), "1".getBytes(StandardCharsets.US_ASCII));

        var finalStateB = next.transit((byte) 'b').get();
        assertEquals(finalStateA == finalStateB, true);
        assertArrayEquals(next.transitOutput((byte) 'b').get(), "2".getBytes(StandardCharsets.US_ASCII));
    }

    @Test
    public void testBuildWithSimpleKeysAndSameOutputs() {
        final List<Map.Entry<String, String>> lexicon = new ArrayList<>();
        lexicon.add(Map.entry("aa", "1"));
        lexicon.add(Map.entry("ab", "1"));

        var fst = buildFST(lexicon);
        assertEquals(3, fst.states.size());

        var next = fst.initialState.transit((byte) 'a').get();
        var nextOutput = fst.initialState.transitOutput((byte) 'a').get();
        assertArrayEquals(nextOutput, "1".getBytes(StandardCharsets.US_ASCII));
        assertEquals(next.getStateOutput(), Optional.empty());

        var finalStateA = next.transit((byte) 'a').get();
        assertEquals(finalStateA.isFinal, true);
        assertEquals(finalStateA.getStateOutput(), Optional.empty());
        assertEquals(next.transitOutput((byte) 'a'), Optional.empty());

        var finalStateB = next.transit((byte) 'b').get();
        assertEquals(finalStateA == finalStateB, true);
        assertEquals(next.transitOutput((byte) 'b'), Optional.empty());
    }

    @Test
    public void testBuildWithKeysHaveSamePrefix() {
        final List<Map.Entry<String, String>> lexicon = new ArrayList<>();
        lexicon.add(Map.entry("a", "1"));
        lexicon.add(Map.entry("ab", "2"));

        var fst = buildFST(lexicon);
        assertEquals(3, fst.states.size());

        var stateA = fst.initialState.transit((byte) 'a').get();
        assertEquals(fst.initialState.transitOutput((byte) 'a'), Optional.empty());
        assertEquals(stateA.isFinal, true);
        assertArrayEquals(stateA.getStateOutput().get(), "1".getBytes(StandardCharsets.US_ASCII));

        var stateB = stateA.transit((byte) 'b').get();
        assertArrayEquals(stateA.transitOutput((byte) 'b').get(), "2".getBytes(StandardCharsets.US_ASCII));
        assertEquals(stateB.isFinal, true);
        assertEquals(stateB.getStateOutput(), Optional.empty());
    }

    @Test
    public void testBuildWithKeysHaveSamePrefixAndSameOutputs() {
        final List<Map.Entry<String, String>> lexicon = new ArrayList<>();
        lexicon.add(Map.entry("a", "1"));
        lexicon.add(Map.entry("ab", "1"));

        var fst = buildFST(lexicon);
        assertEquals(3, fst.states.size());

        var stateA = fst.initialState.transit((byte) 'a').get();
        assertArrayEquals(fst.initialState.transitOutput((byte) 'a').get(), "1".getBytes(StandardCharsets.US_ASCII));
        assertEquals(stateA.isFinal, true);
        assertEquals(stateA.getStateOutput(), Optional.empty());

        var stateB = stateA.transit((byte) 'b').get();
        assertEquals(stateA.transitOutput((byte) 'b'), Optional.empty());
        assertEquals(stateB.isFinal, true);
        assertEquals(stateB.getStateOutput(), Optional.empty());
    }

    @Test
    public void testBuildWithFixedLengthKeys() {
        final List<Map.Entry<String, String>> lexicon = new ArrayList<>();
        lexicon.add(Map.entry("apr", "30"));
        lexicon.add(Map.entry("aug", "31"));
        lexicon.add(Map.entry("dec", "31"));
        lexicon.add(Map.entry("feb", "28"));
        lexicon.add(Map.entry("jan", "31"));
        lexicon.add(Map.entry("jul", "31"));
        lexicon.add(Map.entry("jun", "30"));
        lexicon.add(Map.entry("may", "31"));

        var fst = buildFST(lexicon);
        assertEquals(14, fst.states.size());
    }

    @Test
    public void testBuildComplicatedFST() {
        final List<Map.Entry<String, String>> lexicon = new ArrayList<>();
        lexicon.add(Map.entry("mop", "0"));
        lexicon.add(Map.entry("moth", "1"));
        lexicon.add(Map.entry("pop", "2"));
        lexicon.add(Map.entry("star", "3"));
        lexicon.add(Map.entry("stop", "4"));
        lexicon.add(Map.entry("top", "5"));

        var fst = buildFST(lexicon);
        assertEquals(10, fst.states.size());
    }
}
