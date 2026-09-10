package com.aranaira.arcanearchives.recipe.gct;

import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import com.aranaira.arcanearchives.inventory.handlers.GemCutterInputHandler;
import com.aranaira.arcanearchives.recipe.CraftingCreator;
import com.aranaira.arcanearchives.recipe.IngredientStack;
import com.aranaira.arcanearchives.recipe.IngredientsMatcher;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Gem Cutter recipe definition and preview matching; never authorization to craft. */
public final class GCTRecipe {
    private final ResourceLocation name;
    private final ItemStack result;
    private final List<IngredientStack> ingredients;
    private final IngredientsMatcher matcher;
    private final boolean recordsCreator;

    public GCTRecipe(ResourceLocation name, ItemStack result, List<IngredientStack> ingredients) {
        this(name, result, ingredients, false);
    }

    public static GCTRecipe withCreator(ResourceLocation name, ItemStack result, List<IngredientStack> ingredients) {
        return new GCTRecipe(name, result, ingredients, true);
    }

    private GCTRecipe(ResourceLocation name, ItemStack result, List<IngredientStack> ingredients, boolean recordsCreator) {
        this.name = Objects.requireNonNull(name, "name");
        Objects.requireNonNull(result, "result");
        if (result.isEmpty()) throw new IllegalArgumentException("Recipe output cannot be empty");
        this.result = result.copy();
        this.ingredients = List.copyOf(ingredients);
        this.matcher = new IngredientsMatcher(this.ingredients);
        this.recordsCreator = recordsCreator;
    }

    public ResourceLocation getName() {
        return name;
    }

    public ItemStack getRecipeOutput() {
        return result.copy();
    }

    /**
     * Prepare a detached output using server-resolved identity, without consuming or delivering it.
     * This does not check conditions or authorize crafting; never accept identity from a packet.
     */
    public ItemStack createOutput(UUID creator, String displayName) {
        Objects.requireNonNull(creator, "creator");
        Objects.requireNonNull(displayName, "displayName");
        return recordsCreator ? CraftingCreator.withCreator(result, creator, displayName) : result.copy();
    }

    public List<IngredientStack> getIngredients() {
        return ingredients;
    }

    public boolean matches(ExtendedItemStackHandler inventory) {
        return matcher.matches(inventory);
    }

    public boolean matches(List<ItemStack> inventory) {
        return matcher.getMatchingSlots(inventory).isPresent();
    }

    public Optional<int[]> getMatchingSlots(ExtendedItemStackHandler inventory) {
        return matcher.getMatchingSlots(inventory);
    }

    public Optional<int[]> getMatchingSlots(List<ItemStack> inventory) {
        return matcher.getMatchingSlots(inventory);
    }

    /**
     * Input-only commit on the owning server thread; recomputes a complete allocation.
     * The caller must check conditions, persist the mutation and handle consumed stacks
     * and recipe effects. This neither authorizes crafting nor grants output.
     */
    public Optional<List<ItemStack>> consumeIngredients(GemCutterInputHandler inventory) {
        return Objects.requireNonNull(inventory, "inventory").consume(matcher);
    }

    /** Input-only commit with live server conditions checked before matching and before mutation. */
    public Optional<List<ItemStack>> consumeIngredients(GemCutterInputHandler inventory, BooleanSupplier conditions) {
        return Objects.requireNonNull(inventory, "inventory").consume(matcher, conditions);
    }
}
