package com.aranaira.arcanearchives.recipe;

import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/** A counted ingredient, with optional exact stack data as in the upstream recipe API. */
public final class IngredientStack {
    private final Ingredient ingredient;
    private final int count;
    private final Predicate<ItemStack> dataMatches;
    private final TagKey<Item> tag;

    public IngredientStack(Ingredient ingredient, int count) {
        this(ingredient, count, stack -> true);
    }

    public IngredientStack(ItemLike item, int count) {
        this(Ingredient.of(item), count);
    }

    /** Retain tag identity, not Ingredient's cached expansion, across native data-pack reloads. */
    public IngredientStack(TagKey<Item> tag, int count) {
        this(Ingredient.of(Objects.requireNonNull(tag, "tag")), count, stack -> true, tag);
    }

    public IngredientStack(ItemStack stack) {
        this(Ingredient.of(stack.copy()), stack.getCount(), dataPredicate(stack));
    }

    public IngredientStack(Ingredient ingredient, int count, Predicate<ItemStack> dataMatches) {
        this(ingredient, count, dataMatches, null);
    }

    private IngredientStack(Ingredient ingredient, int count, Predicate<ItemStack> dataMatches, TagKey<Item> tag) {
        this.ingredient = Objects.requireNonNull(ingredient, "ingredient");
        if (count <= 0) throw new IllegalArgumentException("Ingredient counts must be positive");
        this.count = count;
        this.dataMatches = Objects.requireNonNull(dataMatches, "dataMatches");
        this.tag = tag;
    }

    private static Predicate<ItemStack> dataPredicate(ItemStack stack) {
        ItemStack template = stack.copy();
        //? if >=1.21 {
        boolean hasData = !template.getComponentsPatch().isEmpty();
        //?} else {
        /*boolean hasData = template.hasTag();
        *///?}
        return candidate -> !hasData || ExtendedItemStackHandler.sameItemAndData(template, candidate);
    }

    public boolean apply(ItemStack stack) {
        return stack != null && !stack.isEmpty()
            && (tag == null ? ingredient.test(stack) : stack.is(tag)) && dataMatches.test(stack);
    }

    public Ingredient getIngredient() {
        return tag == null ? ingredient : Ingredient.of(tag);
    }

    public int getCount() {
        return count;
    }

    /** Current tag membership for commit guards; non-tag definitions have no tag dependency. */
    public List<Item> getTagItems() {
        return tag == null ? List.of() : BuiltInRegistries.ITEM.getTag(tag).stream()
            .flatMap(holders -> holders.stream()).map(holder -> holder.value()).toList();
    }

    public List<ItemStack> getMatchingStacksWithSizes() {
        // Forge-family Ingredient displays synthesize a barrier for empty tags; it is not a match.
        var matches = tag == null ? Arrays.stream(ingredient.getItems())
            : BuiltInRegistries.ITEM.getTag(tag).stream().flatMap(holders -> holders.stream())
                .map(holder -> new ItemStack(holder.value()));
        return matches.map(stack -> {
            ItemStack copy = stack.copy();
            copy.setCount(count);
            return copy;
        }).toList();
    }
}
