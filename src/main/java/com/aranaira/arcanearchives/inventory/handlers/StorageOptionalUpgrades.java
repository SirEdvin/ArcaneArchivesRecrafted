package com.aranaira.arcanearchives.inventory.handlers;

import com.aranaira.arcanearchives.items.DevouringCharmItem;
import com.aranaira.arcanearchives.types.enums.UpgradeType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Optional upgrades currently implemented for both Troves and Tanks. */
public final class StorageOptionalUpgrades extends OptionalUpgradesHandler {
    private final Runnable changed;
    private final boolean supportsLock;
    public StorageOptionalUpgrades(Runnable changed) { this(changed, false); }
    public StorageOptionalUpgrades(Runnable changed, boolean supportsLock) { this.changed = changed; this.supportsLock = supportsLock; }
    @Override protected UpgradeType getUpgradeType(ItemStack stack) {
        if (stack.getItem() instanceof DevouringCharmItem) return UpgradeType.VOID;
        return supportsLock && stack.getItem() instanceof com.aranaira.arcanearchives.items.RadiantKeyItem ? UpgradeType.LOCK : null;
    }
    @Override public ItemStack getStackInSlot(int slot) { return super.getStackInSlot(slot).copy(); }
    @Override public void onContentsChanged(int slot) { changed.run(); }
    public boolean isVoiding() { return hasUpgrade(UpgradeType.VOID); }
    public boolean isLocked() { return hasUpgrade(UpgradeType.LOCK); }

    /** The owning device must check player access before installation. */
    public boolean install(Player player, InteractionHand hand) {
        ItemStack offered = player.getItemInHand(hand);
        for (int slot = 0; slot < getSlots(); slot++) {
            if (!isItemValid(slot, offered)) continue;
            ItemStack unit = offered.copy();
            unit.setCount(1);
            if (!insertItem(slot, unit, false).isEmpty()) continue;
            if (!player.getAbilities().instabuild) offered.shrink(1);
            player.getInventory().setChanged();
            return true;
        }
        return false;
    }
}
