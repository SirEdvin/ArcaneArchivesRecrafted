package com.aranaira.arcanearchives.inventory.handlers;

import com.aranaira.arcanearchives.types.enums.UpgradeType;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OptionalUpgradesHandlerTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void insertionRetainsSingleItemSlotsAndRejectsRepeatedTypes() {
        OptionalUpgradesHandler inventory = inventory();
        assertEquals(3, inventory.getSlots());
        assertEquals(1, inventory.getSlotLimit(0));
        ItemStack input = new ItemStack(Items.STONE, 3);
        assertEquals(2, inventory.insertItem(0, input, false).getCount());
        assertEquals(3, input.getCount());
        assertTrue(inventory.hasUpgrade(UpgradeType.VOID));
        assertEquals(1, inventory.getTotalUpgradesQuantity());
        assertEquals(1, inventory.insertItem(1, new ItemStack(Items.COBBLESTONE), false).getCount());
        assertEquals(1, inventory.insertItem(0, new ItemStack(Items.STONE), false).getCount());
        assertEquals(1, inventory.insertItem(1, new ItemStack(Items.DIAMOND), false).getCount());
        assertEquals(1, inventory.insertItem(1, new ItemStack(Items.APPLE), false).getCount());
        assertTrue(inventory.insertItem(1, new ItemStack(Items.DIRT), false).isEmpty());
        assertTrue(inventory.insertItem(2, new ItemStack(Items.GRAVEL), false).isEmpty());
        assertEquals(3, inventory.getTotalUpgradesQuantity());
    }

    @Test
    void simulationAndRejectedInputsDoNotMutateInventory() {
        OptionalUpgradesHandler inventory = inventory();
        ItemStack input = new ItemStack(Items.STONE, 2);
        ItemStack remainder = inventory.insertItem(2, input, true);
        remainder.setCount(0);
        assertEquals(0, inventory.getTotalUpgradesQuantity());
        assertEquals(2, input.getCount());
        inventory.insertItem(2, input, false);
        ItemStack simulated = inventory.extractItem(2, 64, true);
        assertEquals(1, simulated.getCount());
        simulated.setCount(0);
        assertTrue(inventory.getIsUpgradePresent(2));
        assertThrows(IndexOutOfBoundsException.class, () -> inventory.insertItem(-1, input, false));
        assertThrows(IndexOutOfBoundsException.class, () -> inventory.getIsUpgradePresent(3));
        assertThrows(IllegalArgumentException.class, () -> inventory.extractItem(2, -1, false));
        assertTrue(inventory.extractItem(2, 0, false).isEmpty());
        assertEquals(1, inventory.extractItem(2, 64, false).getCount());
        assertFalse(inventory.hasUpgrade(UpgradeType.VOID));
        assertTrue(inventory.extractItem(2, 1, false).isEmpty());
    }

    @Test
    void directWritesCannotBypassTypeAndQuantityRules() {
        OptionalUpgradesHandler inventory = inventory();
        ItemStack input = new ItemStack(Items.STONE);
        inventory.setStackInSlot(0, input);
        input.setCount(0);
        assertTrue(inventory.getIsUpgradePresent(0));
        inventory.setStackInSlot(0, new ItemStack(Items.COBBLESTONE));
        assertTrue(inventory.getStackInSlot(0).is(Items.COBBLESTONE));
        assertThrows(IllegalArgumentException.class, () -> inventory.setStackInSlot(1, new ItemStack(Items.STONE)));
        assertThrows(IllegalArgumentException.class, () -> inventory.setStackInSlot(0, new ItemStack(Items.STONE, 2)));
        assertThrows(IllegalArgumentException.class, () -> inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND)));
        assertThrows(IllegalArgumentException.class, () -> inventory.setStackInSlot(0, new ItemStack(Items.APPLE)));
        assertEquals(1, inventory.getTotalUpgradesQuantity());
        assertTrue(inventory.getStackInSlot(0).is(Items.COBBLESTONE));
        inventory.setStackInSlot(0, ItemStack.EMPTY);
        inventory.setStackInSlot(1, new ItemStack(Items.STONE));
        assertTrue(inventory.hasUpgrade(UpgradeType.VOID));
    }

    @Test
    void persistencePreservesGapsAndRejectsMalformedUpgradesAtomically() {
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        OptionalUpgradesHandler source = inventory();
        source.insertItem(2, new ItemStack(Items.STONE), false);
        CompoundTag saved = source.serializeNBT(registries);
        OptionalUpgradesHandler restored = inventory();
        restored.deserializeNBT(registries, saved);
        assertFalse(restored.getIsUpgradePresent(0));
        assertTrue(restored.getIsUpgradePresent(2));
        assertTrue(restored.hasUpgrade(UpgradeType.VOID));

        CompoundTag duplicate = saved.copy();
        var entries = duplicate.getList("Items", Tag.TAG_COMPOUND);
        CompoundTag other = entries.getCompound(0).copy();
        other.putInt("Slot", 0);
        entries.add(other);
        assertThrows(IllegalArgumentException.class, () -> restored.deserializeNBT(registries, duplicate));
        assertEquals(1, restored.getTotalUpgradesQuantity());
        assertTrue(restored.getIsUpgradePresent(2));

        CompoundTag oversized = saved.copy();
        oversized.getList("Items", Tag.TAG_COMPOUND).getCompound(0).putInt("ExtendedCount", 2);
        assertThrows(IllegalArgumentException.class, () -> restored.deserializeNBT(registries, oversized));
        for (var item : new net.minecraft.world.item.Item[]{Items.APPLE, Items.DIAMOND}) {
            ExtendedItemStackHandler invalid = new ExtendedItemStackHandler(3);
            invalid.setStackInSlot(0, new ItemStack(item));
            CompoundTag invalidSaved = invalid.serializeNBT(registries);
            assertThrows(IllegalArgumentException.class, () -> restored.deserializeNBT(registries, invalidSaved));
            assertEquals(1, restored.getTotalUpgradesQuantity());
            assertTrue(restored.getStackInSlot(2).is(Items.STONE));
        }
    }

    private static OptionalUpgradesHandler inventory() {
        return new OptionalUpgradesHandler() {
            @Override protected UpgradeType getUpgradeType(ItemStack stack) {
                if (stack.is(Items.STONE) || stack.is(Items.COBBLESTONE)) return UpgradeType.VOID;
                if (stack.is(Items.DIRT)) return UpgradeType.LOCK;
                if (stack.is(Items.GRAVEL)) return UpgradeType.MUTE;
                if (stack.is(Items.DIAMOND)) return UpgradeType.SIZE;
                return null;
            }
        };
    }
}
