package com.aranaira.arcanearchives.inventory.handlers;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** The three ordered Tank/Trove size upgrades, with proposal 0004 conservation guards. */
public abstract class SizeUpgradeItemHandler extends ExtendedItemStackHandler {
    protected SizeUpgradeItemHandler() {
        super(3);
    }

    public abstract Item getUpgradeForSlot(int slot);

    public boolean canReduceMultiplierTo(int size) {
        return false;
    }

    @Override
    public int getSlotLimit(int slot) {
        validateSlotIndex(slot);
        return 1;
    }

    @Override
    public int getStackLimit(int slot, ItemStack stack) {
        return getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        return !stack.isEmpty() && stack.getItem() == getUpgradeForSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        validateSlotIndex(slot);
        if (!getStackInSlot(slot).isEmpty() || !resolveUpgradesUntil(slot)) return stack.copy();
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        validateSlotIndex(slot);
        if (amount < 0) throw new IllegalArgumentException("Extraction amount must not be negative");
        if (amount != 1 || getStackInSlot(slot).isEmpty()) return ItemStack.EMPTY;
        for (int later = slot + 1; later < getSlots(); later++) {
            if (!getStackInSlot(later).isEmpty()) return ItemStack.EMPTY;
        }
        int remainingSize = slot == 0 ? 0 : getUpgradesCount(slot - 1);
        if (!canReduceMultiplierTo(remainingSize)) return ItemStack.EMPTY;
        return super.extractItem(slot, 1, simulate);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        ItemStack existing = getStackInSlot(slot);
        if (stack.isEmpty()) {
            if (!existing.isEmpty() && extractItem(slot, 1, false).isEmpty()) {
                throw new IllegalArgumentException("Cannot remove an upgrade required by later upgrades or capacity");
            }
        } else if (stack.getCount() == 1 && isItemValid(slot, stack) && resolveUpgradesUntil(slot)) {
            if (!existing.isEmpty() && !sameItemAndData(existing, stack)) {
                throw new IllegalArgumentException("Cannot replace an occupied upgrade slot");
            }
            super.setStackInSlot(slot, stack);
        } else {
            throw new IllegalArgumentException("Invalid size upgrade assignment");
        }
    }

    public boolean resolveUpgradesUntil(int slot) {
        validateSlotIndex(slot);
        return slot == 0 || resolveUpgradesIncluding(slot - 1);
    }

    public boolean resolveUpgradesIncluding(int slot) {
        validateSlotIndex(slot);
        for (int index = 0; index <= slot; index++) {
            if (getStackInSlot(index).isEmpty()) return false;
        }
        return true;
    }

    /** Upstream Matrix Brace, Containment Field/Material Interface, and shaped storage add 2, 3, 4. */
    public int getUpgradesCount(int slot) {
        validateSlotIndex(slot);
        int total = 0;
        for (int index = 0; index <= slot; index++) total += index + 2;
        return total;
    }

    public int getUpgradesCount() {
        int total = 0;
        for (int slot = 0; slot < getSlots() && resolveUpgradesIncluding(slot); slot++) total += slot + 2;
        return total;
    }

    public boolean getIsUpgradePresent(int slot) {
        return !getStackInSlot(slot).isEmpty();
    }

    public boolean hasUpgrade(int slot) {
        return getIsUpgradePresent(slot);
    }

    public int getTotalUpgradesQuantity() {
        int total = 0;
        for (int slot = 0; slot < getSlots(); slot++) if (getIsUpgradePresent(slot)) total++;
        return total;
    }

    @Override
    protected void validateLoadedStacks(NonNullList<ItemStack> loaded) {
        boolean gap = false;
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stack = loaded.get(slot);
            if (stack.isEmpty()) {
                gap = true;
            } else if (gap || stack.getCount() != 1 || !isItemValid(slot, stack)) {
                throw new IllegalArgumentException("Invalid saved size upgrade sequence");
            }
        }
    }

    @Override
    public void onContentsChanged(int slot) {
        onContentsChanged();
    }

    public void onContentsChanged() {}
}
