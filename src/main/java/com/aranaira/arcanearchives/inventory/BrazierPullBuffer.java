package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.data.StorageNetworks;
import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import com.aranaira.arcanearchives.tileentities.BrazierBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** One paid output stack. All remote mutation stays on the owning server thread. */
public final class BrazierPullBuffer {
    private final BrazierBlockEntity brazier;
    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private boolean enabled;
    public BrazierPullBuffer(BrazierBlockEntity brazier) { this.brazier = brazier; }
    public boolean enabled() { return enabled; }
    public ItemStack filter() { return items.get(0).copy(); }
    public ItemStack stack() { return items.get(1).copy(); }
    public void changed() {
        brazier.setChanged();
        if (brazier.live()) brazier.getLevel().sendBlockUpdated(brazier.getBlockPos(), brazier.getBlockState(), brazier.getBlockState(), 3);
    }
    public void toggle(Player player) {
        if (!brazier.canConfigure(player)) return;
        enabled = !enabled;
        changed();
        player.displayClientMessage(Component.translatable("arcanearchives.gui.brazier." + (enabled ? "pull_mode" : "deposit_mode")), true);
    }
    public void interact(Player player) {
        if (!brazier.canConfigure(player)) return;
        var held = player.getMainHandItem();
        if (!held.isEmpty() || player.isShiftKeyDown()) {
            var filter = held.copy();
            if (!filter.isEmpty()) filter.setCount(1);
            items.set(0, filter);
            changed();
            player.displayClientMessage(filter.isEmpty() ? Component.translatable("arcanearchives.gui.brazier.filter_empty")
                : Component.translatable("arcanearchives.gui.brazier.filter", filter.getHoverName()), true);
        } else {
            var output = stack();
            RadiantTroveBlockEntity.insertWithdrawal(player.getInventory().items, output);
            items.set(1, output);
            changed();
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
        }
    }
    public ItemStack extract(int amount, boolean simulate) {
        if (amount < 0) throw new IllegalArgumentException("Negative buffer extraction");
        if (!brazier.live() || amount == 0) return ItemStack.EMPTY;
        var result = stack();
        result.setCount(Math.min(amount, result.getCount()));
        if (!simulate && !result.isEmpty()) {
            items.get(1).shrink(result.getCount());
            changed();
        }
        return result;
    }
    /** Fabric snapshot write: no callbacks before the transaction commits. */
    public void restoreTransactional(ItemStack stack) { items.set(1, stack.copy()); }
    public void tick() {
        if (!enabled || !brazier.live() || items.get(0).isEmpty() || brazier.networkOwner() == null) return;
        var level = (ServerLevel) brazier.getLevel();
        var filter = items.get(0);
        var output = items.get(1);
        if (!output.isEmpty() && !ExtendedItemStackHandler.sameItemAndData(filter, output)) return;
        int space = Math.min(64, filter.getMaxStackSize()) - output.getCount();
        if (space <= 0) return;
        var origin = brazier.getBlockPos();
        for (var source : StorageNetworks.visible(level.getServer(), brazier.networkOwner(), brazier.personalOnly())) {
            if (source.getLevel() != level) continue;
            long dx = (long) source.getBlockPos().getX() - origin.getX();
            long dz = (long) source.getBlockPos().getZ() - origin.getZ();
            if (dx * dx + dz * dz > (long) brazier.radius() * brazier.radius()) continue;
            ExtendedItemStackHandler inventory;
            if (source instanceof RadiantChestBlockEntity chest && chest.isLiveServerStorage()) inventory = chest.inventory();
            else if (source instanceof RadiantTroveBlockEntity trove && trove.isLiveServerStorage()) inventory = trove.inventory();
            else continue;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                var stored = inventory.getStackInSlot(slot).copy();
                if (stored.isEmpty() || !ExtendedItemStackHandler.sameItemAndData(stored, filter)) continue;
                int amount = Math.min(space, stored.getCount());
                var paid = stored.copy(); paid.setCount(output.getCount() + amount);
                stored.shrink(amount);
                // Both balances change before neighbor/client notifications can observe them.
                if (source instanceof RadiantChestBlockEntity chest) chest.setTransactionalStack(slot, stored);
                else ((RadiantTroveBlockEntity) source).setTransactionalStack(stored);
                items.set(1, paid);
                brazier.setChanged();
                if (source instanceof RadiantChestBlockEntity chest) chest.storageChanged();
                else ((RadiantTroveBlockEntity) source).storageChanged();
                changed();
                return;
            }
        }
    }
    public void drop() {
        var output = stack();
        items.set(1, ItemStack.EMPTY);
        brazier.setChanged();
        if (!output.isEmpty()) Block.popResource(brazier.getLevel(), brazier.getBlockPos(), output);
    }
    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        var state = new CompoundTag();
        state.putBoolean("enabled", enabled);
        //? if >=1.21 {
        ContainerHelper.saveAllItems(state, items, registries);
        //?} else {
        /*ContainerHelper.saveAllItems(state, items);
        *///?}
        tag.put("pull", state);
    }
    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        var state = tag.getCompound("pull");
        var loaded = NonNullList.withSize(2, ItemStack.EMPTY);
        //? if >=1.21 {
        ContainerHelper.loadAllItems(state, loaded, registries);
        //?} else {
        /*ContainerHelper.loadAllItems(state, loaded);
        *///?}
        if (loaded.get(0).getCount() > 1 || loaded.get(1).getCount() > Math.min(64, loaded.get(1).getMaxStackSize()))
            throw new IllegalArgumentException("Invalid Brazier pull buffer");
        items.set(0, loaded.get(0)); items.set(1, loaded.get(1));
        enabled = state.getBoolean("enabled");
    }
}
