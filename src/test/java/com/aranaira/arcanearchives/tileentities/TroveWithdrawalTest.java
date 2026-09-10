package com.aranaira.arcanearchives.tileentities;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class TroveWithdrawalTest {
    @Test void fillsExistingStacksBeforeEarlierEmptySlots() {
        var slots = NonNullList.withSize(3, ItemStack.EMPTY);
        slots.set(2, new ItemStack(Items.STONE, 60));
        ItemStack remainder = new ItemStack(Items.STONE, 8);
        RadiantTroveBlockEntity.insertWithdrawal(slots, remainder);
        assertTrue(remainder.isEmpty());
        assertEquals(64, slots.get(2).getCount());
        assertEquals(4, slots.get(0).getCount());
        assertTrue(slots.get(1).isEmpty());
    }

    @Test void fullInventoryPreservesOverflowWithoutCreativeSpecialCase() {
        var slots = NonNullList.withSize(2, ItemStack.EMPTY);
        slots.set(0, new ItemStack(Items.STONE, 63));
        slots.set(1, new ItemStack(Items.DIRT, 64));
        ItemStack remainder = new ItemStack(Items.STONE, 64);
        RadiantTroveBlockEntity.insertWithdrawal(slots, remainder);
        assertEquals(64, slots.get(0).getCount());
        assertEquals(63, remainder.getCount());
        RadiantTroveBlockEntity.insertWithdrawal(slots, remainder);
        assertEquals(63, remainder.getCount());
    }

    @Test void preservesComponentIdentityAndNativeStackLimits() {
        var slots = NonNullList.withSize(2, ItemStack.EMPTY);
        slots.set(0, new ItemStack(Items.ENDER_PEARL, 15));
        ItemStack remainder = new ItemStack(Items.ENDER_PEARL, 20);
        remainder.set(DataComponents.CUSTOM_NAME, Component.literal("different"));
        RadiantTroveBlockEntity.insertWithdrawal(slots, remainder);
        assertEquals(15, slots.get(0).getCount());
        assertEquals(16, slots.get(1).getCount());
        assertEquals(4, remainder.getCount());
        assertEquals(Component.literal("different"), slots.get(1).get(DataComponents.CUSTOM_NAME));
    }
}
*///?}
