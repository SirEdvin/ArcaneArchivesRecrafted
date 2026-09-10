package com.aranaira.arcanearchives.recipe.gct;

import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import com.aranaira.arcanearchives.inventory.handlers.GemCutterInputHandler;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Ordered recipe catalog, confined to its owning thread. Indices are not crafting authority. */
public final class GCTRecipeList {
    public static final int RECIPE_PAGE_LIMIT = 7;
    private Map<ResourceLocation, GCTRecipe> recipes = new LinkedHashMap<>();
    private List<GCTRecipe> snapshot;
    private Object generation = new Object();

    public Map<ResourceLocation, GCTRecipe> getRecipes() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(recipes));
    }

    public List<GCTRecipe> getRecipeList() {
        if (snapshot == null) snapshot = List.copyOf(recipes.values());
        return snapshot;
    }

    /** An empty catalog has one empty presentation page. */
    public int pageCount() {
        return size() == 0 ? 1 : (size() - 1) / RECIPE_PAGE_LIMIT + 1;
    }

    /** Detached ordered snapshot; stale or invalid pages contain no recipes. */
    public List<GCTRecipe> getRecipePage(int page) {
        if (page < 0 || page >= pageCount()) return List.of();
        List<GCTRecipe> definitions = getRecipeList();
        int start = page * RECIPE_PAGE_LIMIT;
        int count = Math.min(RECIPE_PAGE_LIMIT, definitions.size() - start);
        return List.copyOf(definitions.subList(start, start + count));
    }

    public int nextPage(int page) {
        int pages = pageCount();
        return page < 0 || page >= pages - 1 ? 0 : page + 1;
    }

    public int previousPage(int page) {
        int pages = pageCount();
        if (page < 0 || page >= pages) return 0;
        return page == 0 ? pages - 1 : page - 1;
    }

    public void addRecipe(GCTRecipe recipe) {
        Objects.requireNonNull(recipe, "recipe");
        recipes.put(recipe.getName(), recipe);
        generation = new Object();
        snapshot = null;
    }

    /** Validate a complete ordered batch before publishing it on the owning server thread. */
    public void replaceAll(List<GCTRecipe> replacements) {
        Map<ResourceLocation, GCTRecipe> prepared = new LinkedHashMap<>();
        for (GCTRecipe recipe : List.copyOf(replacements)) {
            if (prepared.putIfAbsent(recipe.getName(), recipe) != null) {
                throw new IllegalArgumentException("Duplicate recipe: " + recipe.getName());
            }
        }
        recipes = prepared;
        generation = new Object();
        snapshot = null;
    }

    public void removeRecipe(ResourceLocation name) {
        recipes.remove(Objects.requireNonNull(name, "name"));
        generation = new Object();
        snapshot = null;
    }

    /** Replace only an existing name; a missing target must not silently add a recipe. */
    public void replaceRecipe(GCTRecipe recipe) {
        Objects.requireNonNull(recipe, "recipe");
        if (!recipes.containsKey(recipe.getName())) {
            throw new IndexOutOfBoundsException("Recipe '" + recipe.getName() + "' is not contained in the recipe list");
        }
        addRecipe(recipe);
    }

    public void removeRecipe(GCTRecipe recipe) {
        removeRecipe(Objects.requireNonNull(recipe, "recipe").getName());
    }

    public GCTRecipe getRecipe(ResourceLocation name) {
        return recipes.get(name);
    }

    /** Read-only generation guard, not crafting authorization. */
    public BooleanSupplier unchanged() {
        Object expected = generation;
        return () -> generation == expected;
    }

    /**
     * Resolve the current definition and keep it current through input commit on the owning server
     * thread. Any intervening catalog write cancels the attempt, including remove-and-restore.
     * Conditions must read authoritative state without effects. No output is granted.
     */
    public Optional<List<ItemStack>> consumeIngredients(ResourceLocation name, GemCutterInputHandler inventory,
            BooleanSupplier conditions) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(conditions, "conditions");
        GCTRecipe recipe = recipes.get(name);
        if (recipe == null) return Optional.empty();
        Object expectedGeneration = generation;
        return recipe.consumeIngredients(inventory, () -> generation == expectedGeneration &&
            conditions.getAsBoolean() && generation == expectedGeneration);
    }

    /**
     * Prepare output before guarded input consumption. Identity and conditions must be server-resolved.
     * Returns output only on a complete input commit; does not persist, deliver or process remainders.
     */
    public Optional<GCTCraftingResult> consumeForCraft(ResourceLocation name, GemCutterInputHandler inventory,
            UUID creator, String displayName, BooleanSupplier conditions) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(creator, "creator");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(conditions, "conditions");
        GCTRecipe recipe = recipes.get(name);
        if (recipe == null) return Optional.empty();
        ItemStack output = recipe.createOutput(creator, displayName);
        return consumeIngredients(name, inventory, conditions)
            .map(consumed -> new GCTCraftingResult(output, consumed));
    }

    public GCTRecipe getRecipeByOutput(ItemStack output) {
        Objects.requireNonNull(output, "output");
        for (GCTRecipe recipe : recipes.values()) {
            if (ExtendedItemStackHandler.sameItemAndData(output, recipe.getRecipeOutput())) return recipe;
        }
        return null;
    }

    public GCTRecipe getRecipeByIndex(int index) {
        return index < 0 || index >= size() ? null : getRecipeList().get(index);
    }

    public ItemStack getOutputByIndex(int index) {
        GCTRecipe recipe = getRecipeByIndex(index);
        return recipe == null ? ItemStack.EMPTY : recipe.getRecipeOutput();
    }

    public int indexOf(GCTRecipe recipe) {
        return recipe == null ? -1 : getRecipeList().indexOf(recipe);
    }

    public int size() {
        return recipes.size();
    }
}
