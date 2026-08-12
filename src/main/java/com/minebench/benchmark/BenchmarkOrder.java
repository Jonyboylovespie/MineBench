package com.minebench.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Deterministic balanced-block ordering for multi-configuration runs. */
public final class BenchmarkOrder {
    private BenchmarkOrder() {
    }

    public static List<String> abba(List<String> labels, int blocks) {
        if (labels == null || labels.isEmpty() || blocks <= 0) {
            return List.of();
        }
        List<String> order = new ArrayList<>();
        for (int block = 0; block < blocks; block++) {
            if ((block & 1) == 0) {
                order.addAll(labels);
                List<String> reverse = new ArrayList<>(labels);
                Collections.reverse(reverse);
                order.addAll(reverse);
            } else {
                List<String> reverse = new ArrayList<>(labels);
                Collections.reverse(reverse);
                order.addAll(reverse);
                order.addAll(labels);
            }
        }
        return List.copyOf(order);
    }

    public static List<String> randomizedBlocks(List<String> labels, int blocks, long seed) {
        if (labels == null || labels.isEmpty() || blocks <= 0) {
            return List.of();
        }
        Random random = new Random(seed);
        List<String> order = new ArrayList<>();
        for (int block = 0; block < blocks; block++) {
            List<String> shuffled = new ArrayList<>(labels);
            Collections.shuffle(shuffled, random);
            order.addAll(shuffled);
        }
        return List.copyOf(order);
    }
}
