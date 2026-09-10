package com.aranaira.arcanearchives.recipe;

import java.util.Random;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IngredientAllocationTest {
    @Test
    void doesNotCountOneItemTwice() {
        assertTrue(IngredientAllocation.allocate(new int[]{1}, new int[]{1, 1}, (s, i) -> true).isEmpty());
        assertArrayEquals(new int[]{2}, IngredientAllocation.allocate(new int[]{2}, new int[]{1, 1},
            (s, i) -> true).orElseThrow());
    }

    @Test
    void reassignsBroadMatchesToSatisfyNarrowRequirements() {
        assertArrayEquals(new int[]{1, 1}, IngredientAllocation.allocate(new int[]{1, 1}, new int[]{1, 1},
            (s, i) -> i == 0 || s == 0).orElseThrow());
    }

    @Test
    void combinesSplitStacksAndLeavesUnrelatedStockAlone() {
        assertArrayEquals(new int[]{2, 3, 0}, IngredientAllocation.allocate(new int[]{2, 4, 9}, new int[]{5},
            (s, i) -> s < 2).orElseThrow());
    }

    @Test
    void handlesCountsWhoseTotalExceedsIntegerRange() {
        int[] counts = {Integer.MAX_VALUE, Integer.MAX_VALUE};
        assertArrayEquals(counts, IngredientAllocation.allocate(counts, counts, (s, i) -> true).orElseThrow());
        assertArrayEquals(new int[]{Integer.MAX_VALUE, 0}, IngredientAllocation.allocate(counts,
            new int[]{Integer.MAX_VALUE}, (s, i) -> true).orElseThrow());
    }

    @Test
    void handlesEmptyInventoryAndRecipe() {
        assertTrue(IngredientAllocation.allocate(new int[0], new int[]{1}, (s, i) -> true).isEmpty());
        assertArrayEquals(new int[]{0}, IngredientAllocation.allocate(new int[]{5}, new int[0],
            (s, i) -> { throw new AssertionError("No ingredient exists"); }).orElseThrow());
        assertTrue(IngredientAllocation.allocate(new int[]{0}, new int[]{1},
            (s, i) -> { throw new AssertionError("Empty slots must not match"); }).isEmpty());
    }

    @Test
    void rejectsMalformedCountsBeforeCallingPredicates() {
        assertThrows(IllegalArgumentException.class, () -> IngredientAllocation.allocate(new int[]{-1},
            new int[]{1}, (s, i) -> { throw new AssertionError(); }));
        for (int invalid : new int[]{0, -1}) {
            assertThrows(IllegalArgumentException.class, () -> IngredientAllocation.allocate(new int[]{1},
                new int[]{invalid}, (s, i) -> { throw new AssertionError(); }));
        }
    }

    @Test
    void agreesWithExhaustiveSmallAllocationOracleWithoutMutatingInputs() {
        Random random = new Random(0xAACA005L);
        for (int trial = 0; trial < 2000; trial++) {
            int[] stock = {random.nextInt(3), random.nextInt(3), random.nextInt(3)};
            int[] needed = {1 + random.nextInt(2), 1 + random.nextInt(2), 1 + random.nextInt(2)};
            boolean[][] matches = new boolean[3][3];
            for (int s = 0; s < 3; s++) for (int i = 0; i < 3; i++) matches[s][i] = random.nextBoolean();
            int[] stockBefore = stock.clone();
            int[] neededBefore = needed.clone();
            boolean expected = possible(stock.clone(), needed.clone(), matches, 0);
            var allocation = IngredientAllocation.allocate(stock, needed, (s, i) -> matches[s][i]);
            assertEquals(expected, allocation.isPresent(), "trial " + trial);
            assertArrayEquals(stockBefore, stock);
            assertArrayEquals(neededBefore, needed);
            if (allocation.isPresent()) {
                int[] consumed = allocation.orElseThrow();
                int total = 0;
                for (int s = 0; s < 3; s++) {
                    assertTrue(consumed[s] >= 0 && consumed[s] <= stock[s]);
                    total += consumed[s];
                }
                assertEquals(needed[0] + needed[1] + needed[2], total);
                assertTrue(possible(consumed.clone(), needed.clone(), matches, 0));
            }
        }
    }

    private static boolean possible(int[] stock, int[] needed, boolean[][] matches, int ingredient) {
        if (ingredient == needed.length) return true;
        if (needed[ingredient] == 0) return possible(stock, needed, matches, ingredient + 1);
        for (int slot = 0; slot < stock.length; slot++) {
            if (stock[slot] == 0 || !matches[slot][ingredient]) continue;
            stock[slot]--;
            needed[ingredient]--;
            boolean result = possible(stock, needed, matches, ingredient);
            stock[slot]++;
            needed[ingredient]++;
            if (result) return true;
        }
        return false;
    }
}
