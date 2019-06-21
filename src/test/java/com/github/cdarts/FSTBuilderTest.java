package com.github.cdarts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        entries.add(Map.entry("abc", 1));
        entries.add(Map.entry("bd", 2));
        entries.add(Map.entry("bde", 3));

        var fst = buildFST(entries);
        assertEquals(6, fst.states.size());
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
