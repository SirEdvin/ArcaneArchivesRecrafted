package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.tileentities.BrazierBlockEntity;
import net.minecraft.world.item.ItemStack;

/** Virtual insertion-only slots. Approved 0146 deliberately omits the modifiable setter. */
public final class BrazierItemAutomation
    //? if forge {
    /*implements net.minecraftforge.items.IItemHandler
    *///?} else if neoforge {
    /*implements net.neoforged.neoforge.items.IItemHandler
    *///?}
{
    private final BrazierBlockEntity brazier;
    public BrazierItemAutomation(BrazierBlockEntity brazier) { this.brazier = brazier; }
    public int getSlots() { return 999; }
    public int getSlotLimit(int slot) { java.util.Objects.checkIndex(slot, getSlots()); return 999; }
    public ItemStack getStackInSlot(int slot) { java.util.Objects.checkIndex(slot, getSlots()); return slot == 0 && brazier.live() ? brazier.pull().stack() : ItemStack.EMPTY; }
    public boolean isItemValid(int slot, ItemStack stack) { java.util.Objects.checkIndex(slot, getSlots()); return !brazier.pull().enabled(); }
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        java.util.Objects.checkIndex(slot, getSlots());
        return slot == 0 ? brazier.pull().extract(amount, simulate) : ItemStack.EMPTY;
    }
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        java.util.Objects.checkIndex(slot, getSlots());
        return brazier.insertAutomated(stack, simulate);
    }
}
