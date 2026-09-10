package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.handlers.SizeUpgradeItemHandler;
import com.aranaira.arcanearchives.inventory.handlers.OptionalUpgradesHandler;
import java.util.function.Predicate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Server-owned ordered capacity upgrades and distinct optional upgrades. */
public final class StorageUpgradeMenu extends AbstractContainerMenu {
    private final Predicate<Player> access;

    public static void open(Player player, SizeUpgradeItemHandler handler, OptionalUpgradesHandler optional, Predicate<Player> access) {
        if (!player.level().isClientSide && access.test(player)) {
            player.openMenu(new SimpleMenuProvider((id, inventory, viewer) ->
                new StorageUpgradeMenu(id, inventory, handler, optional, access), Component.translatable("arcanearchives.gui.storage_upgrades")));
        }
    }
    public StorageUpgradeMenu(int id, Inventory inventory) { this(id, inventory, null, null, player -> true); }
    private StorageUpgradeMenu(int id, Inventory inventory, SizeUpgradeItemHandler handler, OptionalUpgradesHandler optional, Predicate<Player> access) {
        super(ContentRegistry.STORAGE_UPGRADE_MENU.get(), id);
        this.access = access;
        SimpleContainer container = handler == null ? new SimpleContainer(3) : new SimpleContainer(3) {
            @Override public ItemStack getItem(int slot) { return handler.getStackInSlot(slot).copy(); }
            @Override public void setItem(int slot, ItemStack stack) { handler.setStackInSlot(slot, stack); }
            @Override public ItemStack removeItem(int slot, int amount) { return handler.extractItem(slot, Math.min(1, amount), false); }
            @Override public ItemStack removeItemNoUpdate(int slot) { return removeItem(slot, 1); }
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean canPlaceItem(int slot, ItemStack stack) {
                return handler.isItemValid(slot, stack) && handler.resolveUpgradesUntil(slot) && handler.getStackInSlot(slot).isEmpty();
            }
        };
        for (int index = 0; index < 3; index++) {
            final int slot = index;
            addSlot(new Slot(container, index, 57 + index * 25, 8) {
                @Override public boolean mayPlace(ItemStack stack) { return handler == null || container.canPlaceItem(slot, stack); }
                @Override public boolean mayPickup(Player player) { return handler == null || !handler.extractItem(slot, 1, true).isEmpty(); }
                @Override public int getMaxStackSize() { return 1; }
            });
        }
        SimpleContainer optionalContainer = optional == null ? new SimpleContainer(3) : new SimpleContainer(3) {
            @Override public ItemStack getItem(int slot) { return optional.getStackInSlot(slot).copy(); }
            @Override public void setItem(int slot, ItemStack stack) { optional.setStackInSlot(slot, stack); }
            @Override public ItemStack removeItem(int slot, int amount) { return optional.extractItem(slot, Math.min(1, amount), false); }
            @Override public ItemStack removeItemNoUpdate(int slot) { return removeItem(slot, 1); }
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean canPlaceItem(int slot, ItemStack stack) {
                return optional.isItemValid(slot, stack) && optional.getStackInSlot(slot).isEmpty();
            }
        };
        for (int index = 0; index < 3; index++) {
            final int slot = index;
            addSlot(new Slot(optionalContainer, index, 57 + index * 25, 44) {
                @Override public boolean mayPlace(ItemStack stack) { return optional == null || optionalContainer.canPlaceItem(slot, stack); }
                @Override public int getMaxStackSize() { return 1; }
            });
        }
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(inventory, column + row * 9 + 9, 10 + column * 18, 80 + row * 18));
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 10 + column * 18, 138));
    }
    @Override public boolean stillValid(Player player) { return access.test(player); }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        if (!stillValid(player) || slot >= slots.size() || slot < -1 && slot != -999) return;
        super.clicked(slot, button, type, player);
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.hasItem() || !source.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack stack = source.getItem().copy();
        ItemStack original = stack.copy();
        if (index < 6) {
            if (!moveItemStackTo(stack, 6, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            for (int slot = 0; slot < 6 && !stack.isEmpty(); slot++) stack = slots.get(slot).safeInsert(stack);
            if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        }
        source.setByPlayer(stack);
        source.setChanged();
        return original;
    }
}
