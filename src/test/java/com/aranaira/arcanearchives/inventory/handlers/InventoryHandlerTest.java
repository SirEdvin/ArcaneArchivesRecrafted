package com.aranaira.arcanearchives.inventory.handlers;

import com.aranaira.arcanearchives.config.ServerSideConfig;
import java.nio.file.Path;
import java.util.Random;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
//? if >=1.21 {
import net.minecraft.core.component.DataComponents;
//?}
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class InventoryHandlerTest {
    @TempDir Path directory;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void defaults() {
        ServerSideConfig.initialize(directory);
    }

    @Test
    void partialInsertionAndSimulationConserveCounts() {
        ExtendedItemStackHandler inventory = new ExtendedItemStackHandler(1);
        ItemStack input = new ItemStack(Items.STONE, 300);
        assertEquals(44, inventory.insertItem(0, input, true).getCount());
        assertTrue(inventory.getStackInSlot(0).isEmpty());
        assertEquals(44, inventory.insertItem(0, input, false).getCount());
        assertEquals(300, input.getCount());
        assertEquals(256, inventory.getStackInSlot(0).getCount());
        assertEquals(15, inventory.calcRedstone());
        ItemStack simulated = inventory.extractItem(0, 256, true);
        simulated.setCount(1);
        assertEquals(256, inventory.getStackInSlot(0).getCount());
        assertEquals(256, inventory.extractItem(0, 1000, false).getCount());
        assertEquals(0, inventory.calcRedstone());
    }

    @Test
    void unstackableItemsStillExtractOneAtATime() {
        ExtendedItemStackHandler inventory = new ExtendedItemStackHandler(1);
        assertTrue(inventory.insertItem(0, new ItemStack(Items.IRON_SWORD, 4), false).isEmpty());
        assertEquals(1, inventory.extractItem(0, 64, false).getCount());
        assertEquals(3, inventory.getStackInSlot(0).getCount());
    }

    @Test
    void rejectsInvalidInputsWithoutMutation() {
        ExtendedItemStackHandler inventory = new ExtendedItemStackHandler(1);
        inventory.insertItem(0, new ItemStack(Items.STONE, 7), false);
        assertThrows(IllegalArgumentException.class, () -> inventory.extractItem(0, -1, false));
        assertThrows(IndexOutOfBoundsException.class, () -> inventory.extractItem(-1, 1, false));
        assertThrows(IndexOutOfBoundsException.class, () -> inventory.insertItem(1, new ItemStack(Items.STONE), false));
        assertTrue(inventory.extractItem(0, 0, false).isEmpty());
        assertEquals(7, inventory.getStackInSlot(0).getCount());
    }

    @Test
    void itemDataDistinguishesStacksAndSurvivesExtendedSerialization() {
        ExtendedItemStackHandler inventory = new ExtendedItemStackHandler(2);
        ItemStack named = namedStone();
        named.setCount(256);
        inventory.insertItem(0, named, false);
        assertEquals(1, inventory.insertItem(0, new ItemStack(Items.STONE), false).getCount());
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        CompoundTag saved = inventory.serializeNBT(registries);
        ExtendedItemStackHandler restored = new ExtendedItemStackHandler(2);
        restored.deserializeNBT(registries, saved);
        assertEquals(256, restored.getStackInSlot(0).getCount());
        assertTrue(ExtendedItemStackHandler.sameItemAndData(named, restored.getStackInSlot(0)));
        assertTrue(restored.getStackInSlot(1).isEmpty());
    }

    @Test
    void malformedSavedInventoryDoesNotPartiallyReplaceLiveContents() {
        ExtendedItemStackHandler inventory = new ExtendedItemStackHandler(2);
        inventory.insertItem(0, new ItemStack(Items.STONE, 7), false);
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        CompoundTag saved = inventory.serializeNBT(registries);
        ListTag entries = saved.getList("Items", Tag.TAG_COMPOUND);
        entries.add(entries.getCompound(0).copy());
        assertThrows(IllegalArgumentException.class, () -> inventory.deserializeNBT(registries, saved));
        assertEquals(7, inventory.getStackInSlot(0).getCount());
    }

    @Test
    void randomizedTransfersConserveInventoryPlusRemainders() {
        ExtendedItemStackHandler inventory = new ExtendedItemStackHandler(1);
        Random random = new Random(0xAACA);
        int expected = 0;
        for (int iteration = 0; iteration < 1000; iteration++) {
            int amount = random.nextInt(600);
            boolean simulate = random.nextBoolean();
            if (random.nextBoolean()) {
                ItemStack remainder = inventory.insertItem(0, new ItemStack(Items.STONE, amount), simulate);
                if (!simulate) expected += amount - remainder.getCount();
            } else {
                ItemStack extracted = inventory.extractItem(0, amount, simulate);
                if (!simulate) expected -= extracted.getCount();
            }
            assertEquals(expected, inventory.getStackInSlot(0).getCount());
            assertTrue(expected >= 0 && expected <= 256);
        }
    }

    @Test
    void upgradesCannotBeExtractedFromEmptyOrConsumedByOccupiedSlots() {
        SizeUpgradeItemHandler upgrades = upgrades();
        assertTrue(upgrades.extractItem(0, 1, false).isEmpty());
        assertTrue(upgrades.insertItem(0, namedStone(), false).isEmpty());
        assertEquals(2, upgrades.insertItem(0, new ItemStack(Items.STONE, 2), false).getCount());
        assertTrue(ExtendedItemStackHandler.sameItemAndData(namedStone(), upgrades.extractItem(0, 1, false)));
        assertTrue(upgrades.extractItem(0, 1, false).isEmpty());
    }

    @Test
    void upgradesRetainPrerequisitesReverseRemovalAndSimulation() {
        SizeUpgradeItemHandler upgrades = upgrades();
        assertEquals(1, upgrades.insertItem(1, new ItemStack(Items.DIRT), false).getCount());
        upgrades.insertItem(0, new ItemStack(Items.STONE), false);
        assertTrue(upgrades.insertItem(1, new ItemStack(Items.DIRT), true).isEmpty());
        assertFalse(upgrades.hasUpgrade(1));
        upgrades.insertItem(1, new ItemStack(Items.DIRT), false);
        upgrades.insertItem(2, new ItemStack(Items.COBBLESTONE), false);
        assertEquals(9, upgrades.getUpgradesCount());
        assertEquals(3, upgrades.getTotalUpgradesQuantity());
        assertTrue(upgrades.extractItem(0, 1, false).isEmpty());
        assertTrue(upgrades.extractItem(1, 1, false).isEmpty());
        assertEquals(1, upgrades.extractItem(2, 1, true).getCount());
        assertTrue(upgrades.hasUpgrade(2));
        upgrades.extractItem(2, 1, false);
        upgrades.extractItem(1, 1, false);
        upgrades.extractItem(0, 1, false);
        assertEquals(0, upgrades.getTotalUpgradesQuantity());
        assertThrows(IllegalArgumentException.class, () -> upgrades.extractItem(0, -1, false));
        assertThrows(IndexOutOfBoundsException.class, () -> upgrades.extractItem(-1, 1, false));
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "1, 2", "2, 5"})
    void removalChecksPostRemovalCapacityIncludingFirstUpgrade(int slot, int remainingSize) {
        int[] contents = {remainingSize + 2};
        int[] checkedSize = {-1};
        int[] changes = {0};
        SizeUpgradeItemHandler upgrades = new SizeUpgradeItemHandler() {
            @Override public Item getUpgradeForSlot(int index) {
                return switch (index) { case 0 -> Items.STONE; case 1 -> Items.DIRT; case 2 -> Items.COBBLESTONE;
                    default -> throw new IndexOutOfBoundsException(index); };
            }
            @Override public boolean canReduceMultiplierTo(int size) {
                checkedSize[0] = size;
                return contents[0] <= size + 1;
            }
            @Override public void onContentsChanged() { changes[0]++; }
        };
        for (int index = 0; index <= slot; index++) {
            upgrades.insertItem(index, new ItemStack(upgrades.getUpgradeForSlot(index)), false);
        }
        changes[0] = 0;
        int previousSize = upgrades.getUpgradesCount();
        assertTrue(upgrades.extractItem(slot, 1, true).isEmpty());
        assertEquals(remainingSize, checkedSize[0]);
        assertTrue(upgrades.extractItem(slot, 1, false).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> upgrades.setStackInSlot(slot, ItemStack.EMPTY));
        assertEquals(previousSize, upgrades.getUpgradesCount());
        assertTrue(upgrades.hasUpgrade(slot));
        assertEquals(0, changes[0]);

        contents[0] = remainingSize + 1;
        ItemStack simulated = upgrades.extractItem(slot, 1, true);
        assertEquals(upgrades.getUpgradeForSlot(slot), simulated.getItem());
        simulated.setCount(0);
        assertTrue(upgrades.hasUpgrade(slot));
        assertEquals(0, changes[0]);
        assertEquals(1, upgrades.extractItem(slot, 1, false).getCount());
        assertEquals(remainingSize, upgrades.getUpgradesCount());
        assertFalse(upgrades.hasUpgrade(slot));
        assertEquals(1, changes[0]);

        upgrades.insertItem(slot, new ItemStack(upgrades.getUpgradeForSlot(slot)), false);
        upgrades.setStackInSlot(slot, ItemStack.EMPTY);
        assertEquals(remainingSize, upgrades.getUpgradesCount());
        assertFalse(upgrades.hasUpgrade(slot));
    }

    @Test
    void stackedInsertionFillsMatchingSlotsBeforeEarlierEmptySlots() {
        ExtendedItemStackHandler inventory = new ExtendedItemStackHandler(3);
        inventory.setStackInSlot(1, new ItemStack(Items.STONE, 250));
        ItemStack input = new ItemStack(Items.STONE, 10);
        assertTrue(inventory.insertItemStacked(input, true).isEmpty());
        assertTrue(inventory.getStackInSlot(0).isEmpty());
        assertEquals(250, inventory.getStackInSlot(1).getCount());
        assertTrue(inventory.insertItemStacked(input, false).isEmpty());
        assertEquals(256, inventory.getStackInSlot(1).getCount());
        assertEquals(4, inventory.getStackInSlot(0).getCount());
        assertTrue(inventory.getStackInSlot(2).isEmpty());
        assertEquals(10, input.getCount());
    }

    @Test
    void stackedInsertionPreservesDataAndRejectedRemainders() {
        ExtendedItemStackHandler inventory = new ExtendedItemStackHandler(2);
        inventory.setStackInSlot(0, new ItemStack(Items.STONE, 1));
        ItemStack input = namedStone();
        input.setCount(300);
        ItemStack preview = inventory.insertItemStacked(input, true);
        assertEquals(44, preview.getCount());
        preview.setCount(1);
        assertEquals(300, input.getCount());
        assertTrue(inventory.getStackInSlot(1).isEmpty());
        ItemStack remainder = inventory.insertItemStacked(input, false);
        assertEquals(44, remainder.getCount());
        assertTrue(ExtendedItemStackHandler.sameItemAndData(input, remainder));
        assertTrue(ExtendedItemStackHandler.sameItemAndData(input, inventory.getStackInSlot(1)));
        remainder.setCount(0);
        assertEquals(256, inventory.getStackInSlot(1).getCount());
        assertEquals(1, inventory.getStackInSlot(0).getCount());
        assertEquals(300, inventory.insertItemStacked(input, false).getCount());
        assertTrue(inventory.insertItemStacked(ItemStack.EMPTY, false).isEmpty());
        assertThrows(NullPointerException.class, () -> inventory.insertItemStacked(null, false));
    }

    @Test
    void stackedInsertionRespectsNativeAndUpgradeSlotPolicies() {
        GemCutterInputHandler inputs = new GemCutterInputHandler();
        for (int slot = 0; slot < inputs.getSlots(); slot++) {
            inputs.setStackInSlot(slot, new ItemStack(Items.STONE, 64));
        }
        inputs.setStackInSlot(0, ItemStack.EMPTY);
        assertEquals(3, inputs.insertItemStacked(new ItemStack(Items.IRON_SWORD, 4), true).getCount());
        assertTrue(inputs.getStackInSlot(0).isEmpty());
        assertEquals(3, inputs.insertItemStacked(new ItemStack(Items.IRON_SWORD, 4), false).getCount());
        assertEquals(1, inputs.getStackInSlot(0).getCount());
        SizeUpgradeItemHandler upgrades = upgrades();
        assertEquals(1, upgrades.insertItemStacked(new ItemStack(Items.DIRT), false).getCount());
        assertEquals(1, upgrades.insertItemStacked(new ItemStack(Items.STONE, 2), false).getCount());
        assertTrue(upgrades.insertItemStacked(new ItemStack(Items.DIRT), false).isEmpty());
        assertTrue(upgrades.hasUpgrade(0));
        assertTrue(upgrades.hasUpgrade(1));
    }

    private static SizeUpgradeItemHandler upgrades() {
        return new SizeUpgradeItemHandler() {
            @Override public Item getUpgradeForSlot(int slot) {
                return switch (slot) { case 0 -> Items.STONE; case 1 -> Items.DIRT; case 2 -> Items.COBBLESTONE;
                    default -> throw new IndexOutOfBoundsException(slot); };
            }
            @Override public boolean canReduceMultiplierTo(int size) { return true; }
        };
    }

    private static ItemStack namedStone() {
        ItemStack stack = new ItemStack(Items.STONE);
        //? if >=1.21 {
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Preserve me"));
        //?} else {
        /*stack.setHoverName(Component.literal("Preserve me"));
        *///?}
        return stack;
    }
}
