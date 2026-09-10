package com.aranaira.arcanearchives.inventory.handlers;

import com.aranaira.arcanearchives.recipe.IngredientStack;
import com.aranaira.arcanearchives.recipe.IngredientsMatcher;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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

class GemCutterInputHandlerTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void inputsUseOrdinaryCapacityAndDetachedReads() {
        GemCutterInputHandler inventory = new GemCutterInputHandler();
        assertEquals(18, inventory.getSlots());
        assertEquals(6, inventory.insertItem(0, new ItemStack(Items.STONE, 70), true).getCount());
        assertTrue(inventory.getStackInSlot(0).isEmpty());
        assertEquals(6, inventory.insertItem(0, new ItemStack(Items.STONE, 70), false).getCount());
        inventory.getStackInSlot(0).setCount(0);
        assertEquals(64, inventory.getStackInSlot(0).getCount());
        assertEquals(3, inventory.insertItem(1, new ItemStack(Items.IRON_SWORD, 4), false).getCount());
        assertEquals(1, inventory.extractItem(1, 64, false).getCount());
        assertThrows(IllegalArgumentException.class, () -> inventory.setStackInSlot(0, new ItemStack(Items.STONE, 65)));
        assertThrows(IllegalArgumentException.class, () -> inventory.setStackInSlot(1, new ItemStack(Items.IRON_SWORD, 2)));
        assertEquals(64, inventory.getStackInSlot(0).getCount());
    }

    @Test
    void consumesCompleteSplitAllocationAndCannotRepeatIt() {
        GemCutterInputHandler inventory = new GemCutterInputHandler();
        inventory.setStackInSlot(0, new ItemStack(Items.STONE, 2));
        inventory.setStackInSlot(1, new ItemStack(Items.STONE, 3));
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(new IngredientStack(Items.STONE, 4)));
        List<ItemStack> consumed = inventory.consume(matcher).orElseThrow();
        assertEquals(4, consumed.stream().mapToInt(ItemStack::getCount).sum());
        assertTrue(inventory.getStackInSlot(0).isEmpty());
        assertEquals(1, inventory.getStackInSlot(1).getCount());
        assertTrue(inventory.consume(matcher).isEmpty());
        consumed.get(1).setCount(64);
        assertEquals(1, inventory.getStackInSlot(1).getCount());
    }

    @Test
    void overlappingRequirementsReassignWithoutDoubleCounting() {
        GemCutterInputHandler inventory = new GemCutterInputHandler();
        inventory.setStackInSlot(0, new ItemStack(Items.STONE));
        inventory.setStackInSlot(1, new ItemStack(Items.DIRT));
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(
            new IngredientStack(Ingredient.of(Items.STONE, Items.DIRT), 1), new IngredientStack(Items.STONE, 1)));
        assertEquals(2, inventory.consume(matcher).orElseThrow().size());
        assertTrue(inventory.getStackInSlot(0).isEmpty());
        assertTrue(inventory.getStackInSlot(1).isEmpty());
    }

    @Test
    void oldPreviewDoesNotAuthorizeConsumptionAfterInputsChange() {
        GemCutterInputHandler inventory = new GemCutterInputHandler();
        inventory.setStackInSlot(0, new ItemStack(Items.STONE, 2));
        IngredientStack one = new IngredientStack(Items.STONE, 1);
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(one, one));
        assertTrue(matcher.matches(inventory));
        inventory.extractItem(0, 1, false);
        assertTrue(inventory.consume(matcher).isEmpty());
        assertEquals(1, inventory.getStackInSlot(0).getCount());
    }

    @Test
    void reentrantInventoryMutationRejectsCommitWithoutRestoringOldState() {
        GemCutterInputHandler inventory = new GemCutterInputHandler();
        inventory.setStackInSlot(0, new ItemStack(Items.STONE, 2));
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(new IngredientStack(
            Ingredient.of(Items.STONE), 1, stack -> {
                inventory.setStackInSlot(0, new ItemStack(Items.DIRT, 2));
                return true;
            })));
        assertTrue(inventory.consume(matcher).isEmpty());
        assertTrue(inventory.getStackInSlot(0).is(Items.DIRT));
        assertEquals(2, inventory.getStackInSlot(0).getCount());
    }

    @Test
    void throwingPredicateCannotCausePartialConsumption() {
        GemCutterInputHandler inventory = new GemCutterInputHandler();
        inventory.setStackInSlot(0, new ItemStack(Items.STONE));
        inventory.setStackInSlot(1, new ItemStack(Items.DIRT));
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(
            new IngredientStack(Items.STONE, 1),
            new IngredientStack(Ingredient.of(Items.DIRT), 1, stack -> { throw new IllegalStateException("fixture"); })));
        assertThrows(IllegalStateException.class, () -> inventory.consume(matcher));
        assertEquals(1, inventory.getStackInSlot(0).getCount());
        assertEquals(1, inventory.getStackInSlot(1).getCount());
    }

    @Test
    void consumedItemsPreserveDataAndDoNotAliasUnconsumedRemainder() {
        GemCutterInputHandler inventory = new GemCutterInputHandler();
        ItemStack named = new ItemStack(Items.STONE, 3);
        //? if >=1.21 {
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Keep this name"));
        //?} else {
        /*named.setHoverName(Component.literal("Keep this name"));
        *///?}
        inventory.setStackInSlot(0, named);
        inventory.setStackInSlot(1, new ItemStack(Items.STONE, 3));
        ItemStack required = named.copy();
        required.setCount(2);
        List<ItemStack> removed = inventory.consume(new IngredientsMatcher(List.of(new IngredientStack(required)))).orElseThrow();
        assertEquals(1, removed.size());
        assertEquals(2, removed.get(0).getCount());
        assertTrue(ExtendedItemStackHandler.sameItemAndData(named, removed.get(0)));
        assertEquals(1, inventory.getStackInSlot(0).getCount());
        assertEquals(3, inventory.getStackInSlot(1).getCount());
        removed.get(0).setCount(0);
        assertEquals(1, inventory.getStackInSlot(0).getCount());
    }

    @Test
    void persistenceRoundTripAndInvalidLoadAreAtomic() {
        GemCutterInputHandler inventory = new GemCutterInputHandler();
        inventory.setStackInSlot(0, new ItemStack(Items.STONE, 32));
        inventory.setStackInSlot(17, new ItemStack(Items.DIRT, 8));
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        CompoundTag saved = inventory.serializeNBT(registries);
        GemCutterInputHandler restored = new GemCutterInputHandler();
        restored.deserializeNBT(registries, saved);
        assertEquals(32, restored.getStackInSlot(0).getCount());
        assertEquals(8, restored.getStackInSlot(17).getCount());
        restored.setStackInSlot(0, new ItemStack(Items.DIAMOND, 7));
        saved.getList("Items", Tag.TAG_COMPOUND).getCompound(1).putInt("ExtendedCount", 65);
        assertThrows(IllegalArgumentException.class, () -> restored.deserializeNBT(registries, saved));
        assertTrue(restored.getStackInSlot(0).is(Items.DIAMOND));
        assertEquals(7, restored.getStackInSlot(0).getCount());
        assertEquals(8, restored.getStackInSlot(17).getCount());
    }

    @Test
    void candidateMutationCannotAuthorizeConsumptionOfFabricatedData() {
        GemCutterInputHandler inventory = new GemCutterInputHandler();
        inventory.setStackInSlot(0, new ItemStack(Items.STONE, 2));
        inventory.setStackInSlot(17, new ItemStack(Items.DIAMOND, 7));
        ItemStack named = new ItemStack(Items.STONE);
        markCandidate(named);
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(
            new IngredientStack(Ingredient.of(Items.STONE), 1, candidate -> {
                markCandidate(candidate);
                return true;
            }), new IngredientStack(named)));
        assertTrue(inventory.consume(matcher).isEmpty());
        assertEquals(2, inventory.getStackInSlot(0).getCount());
        assertTrue(ExtendedItemStackHandler.sameItemAndData(new ItemStack(Items.STONE), inventory.getStackInSlot(0)));
        assertEquals(7, inventory.getStackInSlot(17).getCount());
        assertTrue(inventory.getStackInSlot(17).is(Items.DIAMOND));
    }

    @Test
    void successfulConsumptionReturnsOriginalDataDespiteCandidateMutation() {
        GemCutterInputHandler inventory = new GemCutterInputHandler();
        inventory.setStackInSlot(0, new ItemStack(Items.STONE, 3));
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(
            new IngredientStack(Ingredient.of(Items.STONE), 1, candidate -> {
                markCandidate(candidate);
                candidate.setCount(0);
                return true;
            }), new IngredientStack(Items.STONE, 1)));
        List<ItemStack> consumed = inventory.consume(matcher).orElseThrow();
        assertEquals(1, consumed.size());
        assertEquals(2, consumed.get(0).getCount());
        assertTrue(ExtendedItemStackHandler.sameItemAndData(new ItemStack(Items.STONE), consumed.get(0)));
        assertEquals(1, inventory.getStackInSlot(0).getCount());
        assertTrue(ExtendedItemStackHandler.sameItemAndData(new ItemStack(Items.STONE), inventory.getStackInSlot(0)));
        markCandidate(consumed.get(0));
        assertTrue(ExtendedItemStackHandler.sameItemAndData(new ItemStack(Items.STONE), inventory.getStackInSlot(0)));
        assertTrue(inventory.consume(matcher).isEmpty());
        assertEquals(1, inventory.getStackInSlot(0).getCount());
    }

    private static void markCandidate(ItemStack stack) {
        //? if >=1.21 {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Predicate mutation"));
        //?} else {
        /*stack.setHoverName(Component.literal("Predicate mutation"));
        *///?}
    }

    @Test
    void failedInitialConditionSkipsMatching() {
        GemCutterInputHandler inventory = new GemCutterInputHandler();
        inventory.setStackInSlot(0, new ItemStack(Items.STONE, 2));
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(new IngredientStack(Ingredient.of(Items.STONE), 1,
            candidate -> { throw new AssertionError("Must not match"); })));
        assertTrue(inventory.consume(matcher, () -> false).isEmpty());
        assertEquals(2, inventory.getStackInSlot(0).getCount());
    }

    @Test
    void conditionChangedDuringMatchingRejectsCommit() {
        GemCutterInputHandler inventory = new GemCutterInputHandler();
        inventory.setStackInSlot(0, new ItemStack(Items.STONE, 2));
        boolean[] permitted = {true};
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(new IngredientStack(Ingredient.of(Items.STONE), 1,
            candidate -> { permitted[0] = false; return true; })));
        assertTrue(inventory.consume(matcher, () -> permitted[0]).isEmpty());
        assertEquals(2, inventory.getStackInSlot(0).getCount());
    }

    @Test
    void finalConditionMutationCannotBypassLiveInventoryCheck() {
        GemCutterInputHandler inventory = new GemCutterInputHandler();
        inventory.setStackInSlot(0, new ItemStack(Items.STONE, 2));
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(new IngredientStack(Items.STONE, 1)));
        int[] checks = {0};
        assertTrue(inventory.consume(matcher, () -> {
            if (++checks[0] == 2) inventory.setStackInSlot(0, new ItemStack(Items.DIRT, 3));
            return true;
        }).isEmpty());
        assertEquals(2, checks[0]);
        assertTrue(inventory.getStackInSlot(0).is(Items.DIRT));
        assertEquals(3, inventory.getStackInSlot(0).getCount());
    }

    @Test
    void throwingFinalConditionLeavesInputUnconsumed() {
        GemCutterInputHandler inventory = new GemCutterInputHandler();
        inventory.setStackInSlot(0, new ItemStack(Items.STONE, 2));
        IngredientsMatcher matcher = new IngredientsMatcher(List.of(new IngredientStack(Items.STONE, 1)));
        int[] checks = {0};
        assertThrows(IllegalStateException.class, () -> inventory.consume(matcher, () -> {
            if (++checks[0] == 2) throw new IllegalStateException("Unavailable authoritative data");
            return true;
        }));
        assertEquals(2, inventory.getStackInSlot(0).getCount());
        assertThrows(NullPointerException.class, () -> inventory.consume(matcher, null));
        assertEquals(1, inventory.consume(matcher, () -> true).orElseThrow().get(0).getCount());
        assertEquals(1, inventory.getStackInSlot(0).getCount());
    }
}
