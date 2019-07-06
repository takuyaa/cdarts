package com.github.cdarts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class BytesFSTBuilder extends FSTBuilder<byte[]> {
    @Override
    byte[] defaultValue() {
        return new byte[0];
    }

    @Override
    Optional<byte[]> prefix(Optional<byte[]> a, Optional<byte[]> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return Optional.empty();
        }

        final byte[] a_ = a.get();
        final byte[] b_ = b.get();

        if (a_.length == 0 || b_.length == 0) {
            return Optional.empty();
        }

        final byte[] shorter = a_.length <= b_.length ? a_ : b_;
        int end = 0;
        while (end < shorter.length) {
            if (a_[end] != b_[end]) {
                break;
            }
            end++;
        }
        if (end == 0) {
            return Optional.empty();
        }
        return Optional.of(Arrays.copyOf(shorter, end));
    }

    @Override
    Optional<byte[]> concat(Optional<byte[]> a, Optional<byte[]> b) {
        if (a.isEmpty()) {
            if (b.isPresent() && b.get().length == 0) {
                return Optional.empty();
            }
            return b;
        }
        if (b.isEmpty()) {
            if (a.get().length == 0) {
                return Optional.empty();
            }
            return a;
        }

        final byte[] a_ = a.get();
        final byte[] b_ = b.get();

        if (a_.length == 0 && b_.length == 0) {
            return Optional.empty();
        }

        final byte[] c_ = new byte[a_.length + b_.length];
        System.arraycopy(a_, 0, c_, 0, a_.length);
        System.arraycopy(b_, 0, c_, a_.length, b_.length);
        return Optional.of(c_);
    }

    @Override
    Optional<byte[]> subtract(Optional<byte[]> a, Optional<byte[]> b) {
        if (a.isEmpty()) {
            return Optional.empty();
        }
        if (b.isEmpty()) {
            if (a.get().length == 0) {
                return Optional.empty();
            }
            return a;
        }

        final byte[] a_ = a.get();
        final byte[] b_ = b.get();

        if (a_.length == 0) {
            return Optional.empty();
        }
        if (b_.length == 0) {
            return a;
        }

        int start = 0;
        while (start < a_.length && start < b_.length) {
            if (a_[start] != b_[start]) {
                break;
            }
            start++;
        }
        if (start == a_.length) {
            return Optional.empty();
        }
        return Optional.of(Arrays.copyOfRange(a_, start, a_.length));
    }

    public static void main(String[] args) throws Exception {
        final var entries = new BufferedReader(new InputStreamReader(System.in)).lines().map((String line) -> {
            var columns = line.split(",");
            var key = columns[0].replace("\"", "");
            var value = columns[1].replace("\"", "");
            return Map.entry(key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8));
        });

        final var builder = new BytesFSTBuilder();
        final var fst = builder.build(entries);
        System.out.println(fst.toDot());
    }
}
