package com.aranaira.arcanearchives.recipe;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;

/** Conserved counted-ingredient allocation; predicates receive (slot, ingredient) indices. */
public final class IngredientAllocation {
    private IngredientAllocation() {}

    public static Optional<int[]> allocate(int[] available, int[] required,
                                           BiPredicate<Integer, Integer> matches) {
        Objects.requireNonNull(available, "available");
        Objects.requireNonNull(required, "required");
        Objects.requireNonNull(matches, "matches");
        for (int count : available) {
            if (count < 0) throw new IllegalArgumentException("Negative inventory count");
        }
        for (int count : required) {
            if (count <= 0) throw new IllegalArgumentException("Ingredient counts must be positive");
        }
        int slots = available.length;
        int ingredients = required.length;
        int sink = Math.addExact(Math.addExact(slots, ingredients), 1);
        int vertices = Math.addExact(sink, 1);
        int[][] residual = new int[vertices][vertices];
        for (int slot = 0; slot < slots; slot++) {
            residual[0][slot + 1] = available[slot];
            for (int ingredient = 0; ingredient < ingredients; ingredient++) {
                if (available[slot] > 0 && matches.test(slot, ingredient)) {
                    residual[slot + 1][slots + ingredient + 1] = Math.min(available[slot], required[ingredient]);
                }
            }
        }
        for (int ingredient = 0; ingredient < ingredients; ingredient++) {
            residual[slots + ingredient + 1][sink] = required[ingredient];
        }

        // Reverse edges let a later narrow requirement reclaim stock from an earlier broad one.
        int[] parent = new int[vertices];
        int[] queue = new int[vertices];
        while (true) {
            Arrays.fill(parent, -1);
            parent[0] = 0;
            queue[0] = 0;
            int end = 1;
            for (int start = 0; start < end && parent[sink] == -1; start++) {
                int from = queue[start];
                for (int to = 1; to < vertices; to++) {
                    if (parent[to] == -1 && residual[from][to] > 0) {
                        parent[to] = from;
                        queue[end++] = to;
                    }
                }
            }
            if (parent[sink] == -1) break;
            int amount = Integer.MAX_VALUE;
            for (int to = sink; to != 0; to = parent[to]) {
                amount = Math.min(amount, residual[parent[to]][to]);
            }
            for (int to = sink; to != 0; to = parent[to]) {
                int from = parent[to];
                residual[from][to] -= amount;
                residual[to][from] += amount;
            }
        }
        for (int ingredient = 0; ingredient < ingredients; ingredient++) {
            if (residual[slots + ingredient + 1][sink] != 0) return Optional.empty();
        }
        int[] consumption = new int[slots];
        for (int slot = 0; slot < slots; slot++) consumption[slot] = available[slot] - residual[0][slot + 1];
        return Optional.of(consumption);
    }
}
