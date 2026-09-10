package com.aranaira.arcanearchives.recipe;

import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
//? if >=1.21 {
import net.minecraft.core.component.DataComponents;
//?}
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IngredientsMatcherTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void repeatedIngredientInstancesRemainSeparateRequirements() {
        IngredientStack ingredient = new IngredientStack(Items.STONE, 1);
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(ingredient, ingredient));
        ExtendedItemStackHandler inventory = new ExtendedItemStackHandler(1);
        inventory.setStackInSlot(0, new ItemStack(Items.STONE));
        assertFalse(matcher.matches(inventory));
        inventory.setStackInSlot(0, new ItemStack(Items.STONE, 2));
        assertArrayEquals(new int[]{2}, matcher.getMatchingSlots(inventory).orElseThrow());
        assertEquals(2, inventory.getStackInSlot(0).getCount());
        assertTrue(matcher.matches(inventory));
    }

    @Test
    void broadIngredientDoesNotStealTheOnlyNarrowMatch() {
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(
            new IngredientStack(Ingredient.of(Items.STONE, Items.DIRT), 1),
            new IngredientStack(Items.STONE, 1)));
        ExtendedItemStackHandler inventory = new ExtendedItemStackHandler(2);
        inventory.setStackInSlot(0, new ItemStack(Items.STONE));
        inventory.setStackInSlot(1, new ItemStack(Items.DIRT));
        assertArrayEquals(new int[]{1, 1}, matcher.getMatchingSlots(inventory).orElseThrow());
        assertEquals(1, inventory.getStackInSlot(0).getCount());
        assertEquals(1, inventory.getStackInSlot(1).getCount());
    }

    @Test
    void exactDataIsCopiedAndMatchedButPlainIngredientsAcceptNamedItems() {
        ItemStack template = namedStone("required");
        IngredientStack exact = new IngredientStack(template);
        rename(template, "changed after construction");
        assertTrue(exact.apply(namedStone("required")));
        assertFalse(exact.apply(template));
        assertFalse(exact.apply(new ItemStack(Items.STONE)));
        assertTrue(new IngredientStack(new ItemStack(Items.STONE)).apply(template));
        template.setCount(0);
        assertTrue(exact.apply(namedStone("required")));
        assertFalse(exact.apply(ItemStack.EMPTY));
        assertFalse(exact.apply(null));
    }

    @Test
    void repeatedCallsRecomputeAgainstCurrentInventory() {
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(new IngredientStack(Items.STONE, 4)));
        ExtendedItemStackHandler inventory = new ExtendedItemStackHandler(2);
        inventory.setStackInSlot(0, new ItemStack(Items.STONE, 2));
        inventory.setStackInSlot(1, new ItemStack(Items.STONE, 3));
        int[] plan = matcher.getMatchingSlots(inventory).orElseThrow();
        assertArrayEquals(new int[]{2, 2}, plan);
        plan[0] = 99;
        assertArrayEquals(new int[]{2, 2}, matcher.getMatchingSlots(inventory).orElseThrow());
        inventory.setStackInSlot(1, ItemStack.EMPTY);
        assertFalse(matcher.matches(inventory));
    }

    private static ItemStack namedStone(String name) {
        ItemStack stack = new ItemStack(Items.STONE);
        rename(stack, name);
        return stack;
    }

    @Test
    void predicateMutationCannotFabricateDataForAnotherRequirement() {
        IngredientStack mutating = new IngredientStack(Ingredient.of(Items.STONE), 1, candidate -> {
            rename(candidate, "fabricated");
            return true;
        });
        IngredientStack exact = new IngredientStack(namedStone("fabricated"));
        ItemStack input = new ItemStack(Items.STONE, 2);
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(mutating, exact));
        assertTrue(matcher.getMatchingSlots(List.of(input)).isEmpty());
        assertTrue(ExtendedItemStackHandler.sameItemAndData(new ItemStack(Items.STONE), input));
        assertEquals(2, input.getCount());
    }

    @Test
    void predicateMutationCannotHideOtherwiseValidRequirements() {
        IngredientStack mutating = new IngredientStack(Ingredient.of(Items.STONE), 1, candidate -> {
            candidate.setCount(0);
            return true;
        });
        ItemStack input = new ItemStack(Items.STONE, 2);
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(mutating, new IngredientStack(Items.STONE, 1)));
        assertArrayEquals(new int[]{2}, matcher.getMatchingSlots(List.of(input)).orElseThrow());
        assertEquals(2, input.getCount());
    }

    private static void rename(ItemStack stack, String name) {
        //? if >=1.21 {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        //?} else {
        /*stack.setHoverName(Component.literal(name));
        *///?}
    }
}
