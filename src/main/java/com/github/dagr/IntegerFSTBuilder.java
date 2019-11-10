package com.github.dagr;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class IntegerFSTBuilder extends FSTBuilder<Integer> {
    @Override
    Integer defaultValue() {
        return 0;
    }

    @Override
    Optional<Integer> prefix(Optional<Integer> a, Optional<Integer> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return Optional.empty();
        }
        return a.get() == b.get() ? a : Optional.empty();
    }

    @Override
    Optional<Integer> concat(Optional<Integer> a, Optional<Integer> b) {
        if (a.isEmpty()) {
            return b;
        }
        if (b.isEmpty()) {
            return a;
        }
        if (a.equals(b)) {
            return a;
        }
        // adopt former value
        return a;
    }

    @Override
    Optional<Integer> subtract(Optional<Integer> a, Optional<Integer> b) {
        if (a.isEmpty()) {
            return Optional.empty();
        }
        if (b.isEmpty()) {
            return a;
        }
        if (a.equals(b)) {
            return Optional.empty();
        }
        // ignore subtrahend
        return a;
    }

    public static void main(String[] args) throws Exception {
        final var entries = new BufferedReader(new InputStreamReader(System.in)).lines().map((String line) -> {
            var columns = line.split(",");
            var key = columns[0].replace("\"", "");
            var value = Integer.parseInt(columns[1].replace("\"", ""));
            return Map.entry(key.getBytes(StandardCharsets.UTF_8), value);
        });

        final var builder = new IntegerFSTBuilder();
        final var fst = builder.build(entries);
        System.out.println(fst.toDot());
    }
}
