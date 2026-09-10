package com.aranaira.arcanearchives.inventory.handlers;

import com.aranaira.arcanearchives.recipe.IngredientsMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/** Owned, server-thread-confined inputs. Consumption is not output/remainder delivery. */
public final class GemCutterInputHandler extends ExtendedItemStackHandler {
    public GemCutterInputHandler() {
        super(18);
    }

    @Override
    public int getSlotLimit(int slot) {
        validateSlotIndex(slot);
        return 64;
    }

    @Override
    public int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return super.getStackInSlot(slot).copy();
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateStack(slot, stack);
        super.setStackInSlot(slot, stack);
    }

    private void validateStack(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        if (!stack.isEmpty() && stack.getCount() > getStackLimit(slot, stack)) {
            throw new IllegalArgumentException("Gem Cutter inputs must respect native stack limits");
        }
    }

    @Override
    protected void validateLoadedStacks(NonNullList<ItemStack> loaded) {
        for (int slot = 0; slot < getSlots(); slot++) validateStack(slot, loaded.get(slot));
    }

    /**
     * Recompute rather than accept a preview plan. Returns detached consumed stacks only after
     * a complete commit; the caller must persist changes and handle recipe effects separately.
     * No external callbacks run between writes. Call only on the owning server thread.
     */
    public Optional<List<ItemStack>> consume(IngredientsMatcher matcher) {
        return consume(matcher, () -> true);
    }

    /** Conditions must read current server state without effects, not a cached preview boolean. */
    public Optional<List<ItemStack>> consume(IngredientsMatcher matcher, BooleanSupplier conditions) {
        Objects.requireNonNull(matcher, "matcher");
        Objects.requireNonNull(conditions, "conditions");
        List<ItemStack> expected = stacks.stream().map(ItemStack::copy).toList();
        if (!conditions.getAsBoolean()) return Optional.empty();
        Optional<int[]> allocation = matcher.getMatchingSlots(expected);
        if (allocation.isEmpty()) return Optional.empty();
        int[] amounts = allocation.get();
        List<ItemStack> remaining = new ArrayList<>(getSlots());
        List<ItemStack> consumed = new ArrayList<>();
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stack = expected.get(slot);
            remaining.add(withCount(stack, stack.getCount() - amounts[slot]));
            if (amounts[slot] > 0) consumed.add(withCount(stack, amounts[slot]));
        }
        if (!conditions.getAsBoolean()) return Optional.empty();
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack live = stacks.get(slot);
            ItemStack before = expected.get(slot);
            if (live.getCount() != before.getCount() || !sameItemAndData(live, before)) {
                return Optional.empty();
            }
        }
        List<ItemStack> result = List.copyOf(consumed);
        for (int slot = 0; slot < getSlots(); slot++) stacks.set(slot, remaining.get(slot));
        return Optional.of(result);
    }
}
