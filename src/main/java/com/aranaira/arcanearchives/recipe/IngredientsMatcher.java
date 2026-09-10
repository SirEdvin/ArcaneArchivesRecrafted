package com.aranaira.arcanearchives.recipe;

import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

/** Matching only: a preview allocation is never authorization to grant a crafting output. */
public final class IngredientsMatcher {
    private final List<IngredientStack> ingredients;

    public IngredientsMatcher(List<IngredientStack> ingredients) {
        this.ingredients = List.copyOf(ingredients);
    }

    public boolean matches(ExtendedItemStackHandler inventory) {
        return getMatchingSlots(inventory).isPresent();
    }

    /** Returns complete per-slot consumption, or no plan; never a partially satisfied plan. */
    public Optional<int[]> getMatchingSlots(ExtendedItemStackHandler inventory) {
        List<ItemStack> snapshot = new ArrayList<>(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            snapshot.add(inventory.getStackInSlot(slot).copy());
        }
        return getMatchingSlots(snapshot);
    }

    /** Copies caller-owned stacks before invoking ingredient predicates. */
    public Optional<int[]> getMatchingSlots(List<ItemStack> inventory) {
        List<ItemStack> snapshot = inventory.stream().map(ItemStack::copy).toList();
        int[] available = snapshot.stream().mapToInt(ItemStack::getCount).toArray();
        int[] required = ingredients.stream().mapToInt(IngredientStack::getCount).toArray();
        return IngredientAllocation.allocate(available, required,
            (slot, ingredient) -> ingredients.get(ingredient).apply(snapshot.get(slot).copy()));
    }
}
