package com.aranaira.arcanearchives.inventory;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class AmphoraPickupRecoveryTest {
    @Test void fullInventoryPreservesRecoveredFluidContainerAndOtherSlots() {
        var inventory = new Inventory(null);
        for (int i = 0; i < inventory.items.size(); i++) inventory.setItem(i, new ItemStack(Items.STONE, 64));
        var bucket = new ItemStack(Items.WATER_BUCKET);
        bucket.set(DataComponents.CUSTOM_NAME, Component.literal("Recovered fluid"));
        var original = bucket.copy();
        var remainder = AmphoraFluidStorage.insertRecoveredPickup(inventory, bucket);
        assertTrue(ItemStack.matches(original, remainder));
        assertTrue(inventory.offhand.getFirst().isEmpty());
        assertTrue(inventory.armor.stream().allMatch(ItemStack::isEmpty));
        assertTrue(inventory.items.stream().allMatch(stack -> stack.is(Items.STONE) && stack.getCount() == 64));
    }

    @Test void mergesBeforeEmptySlotsAndPreservesComponents() {
        var inventory = new Inventory(null);
        var bucket = new ItemStack(Items.BUCKET, 3);
        bucket.set(DataComponents.CUSTOM_NAME, Component.literal("Recovered"));
        var existing = bucket.copyWithCount(15);
        inventory.setItem(8, existing);
        inventory.setItem(9, new ItemStack(Items.BUCKET, 15));
        assertTrue(AmphoraFluidStorage.insertRecoveredPickup(inventory, bucket).isEmpty());
        assertEquals(16, inventory.getItem(8).getCount());
        assertEquals(15, inventory.getItem(9).getCount());
        assertEquals(2, inventory.getItem(0).getCount());
        assertEquals(Component.literal("Recovered"), inventory.getItem(0).get(DataComponents.CUSTOM_NAME));
    }
}
*///?}
