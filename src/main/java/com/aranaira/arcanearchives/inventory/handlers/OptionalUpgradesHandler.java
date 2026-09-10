package com.aranaira.arcanearchives.inventory.handlers;

import com.aranaira.arcanearchives.types.enums.UpgradeType;
import java.util.EnumSet;
import java.util.Objects;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/** Three distinct optional upgrades; concrete item/device bindings supply the type lookup. */
public abstract class OptionalUpgradesHandler extends ExtendedItemStackHandler {
    public OptionalUpgradesHandler() {
        super(3);
    }

    /** Return null for non-upgrades. This lookup must not mutate the supplied stack. */
    protected abstract UpgradeType getUpgradeType(ItemStack stack);

    public boolean hasUpgrade(UpgradeType type) {
        return hasUpgradeExcept(Objects.requireNonNull(type), -1);
    }

    private boolean hasUpgradeExcept(UpgradeType type, int excludedSlot) {
        for (int slot = 0; slot < getSlots(); slot++) {
            ItemStack stack = getStackInSlot(slot);
            if (slot != excludedSlot && !stack.isEmpty() && getUpgradeType(stack) == type) return true;
        }
        return false;
    }

    private UpgradeType optionalType(ItemStack stack) {
        if (stack.isEmpty()) return null;
        UpgradeType type = getUpgradeType(stack);
        return type == UpgradeType.SIZE ? null : type;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        UpgradeType type = optionalType(stack);
        return type != null && !hasUpgrade(type);
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
    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        if (!stack.isEmpty()) {
            UpgradeType type = optionalType(stack);
            if (stack.getCount() != 1 || type == null || hasUpgradeExcept(type, slot)) {
                throw new IllegalArgumentException("Invalid or duplicate optional upgrade");
            }
        }
        super.setStackInSlot(slot, stack);
    }

    @Override
    protected void validateLoadedStacks(NonNullList<ItemStack> loaded) {
        EnumSet<UpgradeType> types = EnumSet.noneOf(UpgradeType.class);
        for (ItemStack stack : loaded) {
            if (stack.isEmpty()) continue;
            UpgradeType type = optionalType(stack);
            if (stack.getCount() != 1 || type == null || !types.add(type)) {
                throw new IllegalArgumentException("Invalid or duplicate saved optional upgrade");
            }
        }
    }

    public int getTotalUpgradesQuantity() {
        int count = 0;
        for (int slot = 0; slot < getSlots(); slot++) {
            if (getIsUpgradePresent(slot)) count++;
        }
        return count;
    }

    public boolean getIsUpgradePresent(int slot) {
        return !getStackInSlot(slot).isEmpty();
    }
}
