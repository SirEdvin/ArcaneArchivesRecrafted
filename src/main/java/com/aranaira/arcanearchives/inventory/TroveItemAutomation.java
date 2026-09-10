package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import net.minecraft.world.item.ItemStack;

/** Upstream's empty second slot keeps native hoppers from skipping a full voiding Trove. */
public final class TroveItemAutomation
    //? if forge {
    /*implements net.minecraftforge.items.IItemHandler
    *///?} else if neoforge {
    /*implements net.neoforged.neoforge.items.IItemHandler
    *///?}
{
    private final RadiantTroveBlockEntity trove;
    public TroveItemAutomation(RadiantTroveBlockEntity trove) { this.trove = trove; }
    public int getSlots() { return 2; }
    public ItemStack getStackInSlot(int slot) {
        java.util.Objects.checkIndex(slot, 2);
        return slot == 0 ? trove.inventory().getStackInSlot(0) : ItemStack.EMPTY;
    }
    public int getSlotLimit(int slot) {
        java.util.Objects.checkIndex(slot, 2);
        return trove.inventory().getStackLimit(0, trove.inventory().getStackInSlot(0));
    }
    public boolean isItemValid(int slot, ItemStack stack) {
        java.util.Objects.checkIndex(slot, 2);
        return trove.acceptsItem(stack);
    }
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        java.util.Objects.checkIndex(slot, 2);
        return trove.inventory().insertItem(0, stack, simulate);
    }
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        java.util.Objects.checkIndex(slot, 2);
        return trove.isLiveServerStorage() ? trove.inventory().extractItem(0, amount, simulate) : ItemStack.EMPTY;
    }
}
