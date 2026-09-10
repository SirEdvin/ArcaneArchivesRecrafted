package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.items.ArcaneGemItem;
import com.aranaira.arcanearchives.items.AvailableGems;
import com.aranaira.arcanearchives.items.GemRecharge;
import com.aranaira.arcanearchives.items.GemSocketItem;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class GemSocketMenu extends AbstractContainerMenu {
    private final Inventory inventory;
    private final ItemStack socket;
    private final SimpleContainer contents = new SimpleContainer(1);
    private boolean closed;

    public GemSocketMenu(int id, Inventory inventory) { this(id, inventory, ItemStack.EMPTY); }
    public GemSocketMenu(int id, Inventory inventory, ItemStack socket) {
        super(ContentRegistry.GEM_SOCKET_MENU.get(), id);
        this.inventory = inventory;
        this.socket = socket;
        com.aranaira.arcanearchives.items.WornGemSocket.forget(inventory.player);
        if (!socket.isEmpty()) contents.setItem(0, GemSocketItem.gem(socket, inventory.player.level()));
        contents.addListener(container -> save());
        for (int row = 0; row < 4; row++) for (int col = 0; col < 9; col++)
            addInventorySlot(row == 3 ? col : 9 + row * 9 + col, 10 + col * 18, row == 3 ? 115 : 57 + row * 18);
        addInventorySlot(40, -23, 115);
        addSlot(new Slot(contents, 0, 81, 3) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof ArcaneGemItem; }
            @Override public int getMaxStackSize() { return 1; }
        });
    }
    private void addInventorySlot(int index, int x, int y) {
        addSlot(new Slot(inventory, index, x, y) {
            @Override public boolean mayPickup(Player player) { return !(getItem().getItem() instanceof GemSocketItem); }
            @Override public boolean mayPlace(ItemStack stack) { return !(stack.getItem() instanceof GemSocketItem); }
        });
    }
    public ItemStack gem() { return contents.getItem(0); }
    public void save() {
        if (inventory.player.level().isClientSide || !stillValid(inventory.player)) return;
        GemSocketItem.save(socket, gem(), inventory.player.level());
        com.aranaira.arcanearchives.items.WornGemSocket.changed(inventory.player, socket);
        inventory.setChanged();
    }

    @Override public boolean stillValid(Player player) {
        if (player.level().isClientSide) return !closed;
        if (closed || player != inventory.player || !player.isAlive() || player.isSpectator()
                || player.getServer() == null || !player.getServer().isSameThread()
                || socket.isEmpty() || !(socket.getItem() instanceof GemSocketItem)) return false;
        for (int index = 0; index < inventory.getContainerSize(); index++)
            if (inventory.getItem(index) == socket) return true;
        return com.aranaira.arcanearchives.items.WornGemSocket.socket(player) == socket;
    }
    @Override public void broadcastChanges() { save(); super.broadcastChanges(); }
    @Override public void clicked(int index, int button, ClickType type, Player player) {
        if (!stillValid(player) || index >= slots.size() || getCarried().getItem() instanceof GemSocketItem) return;
        if (index >= 0 && slots.get(index).getItem().getItem() instanceof GemSocketItem) return;
        if (type == ClickType.SWAP && ((button >= 0 && button < 9 && inventory.getItem(button).getItem() instanceof GemSocketItem)
                || (button == 40 && player.getOffhandItem().getItem() instanceof GemSocketItem))) return;
        super.clicked(index, button, type, player);
        save();
    }
    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot.getItem().getItem() instanceof GemSocketItem) && super.canTakeItemForPickAll(stack, slot);
    }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (button != 0 || player.level().isClientSide || player.containerMenu != this || !stillValid(player)) return false;
        for (ItemStack gem : AvailableGems.get(player)) GemRecharge.recharge(player, gem);
        broadcastChanges();
        return true;
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        ItemStack moving = slot.getItem();
        if (!(moving.getItem() instanceof ArcaneGemItem)) return ItemStack.EMPTY;
        ItemStack original = moving.copy();
        if (index == 36) {
            if (!moveItemStackTo(moving, 37, 38, false) && !moveItemStackTo(moving, 27, 36, false))
                moveItemStackTo(moving, 0, 27, false);
        } else if (index == 37) {
            if (!moveItemStackTo(moving, 36, 37, false) && !moveItemStackTo(moving, 27, 36, false))
                moveItemStackTo(moving, 0, 27, false);
        } else if (!moveItemStackTo(moving, 37, 38, false)) moveItemStackTo(moving, 36, 37, false);
        if (moving.getCount() == original.getCount()) return ItemStack.EMPTY;
        if (moving.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, moving);
        save();
        return original;
    }
    @Override public void removed(Player player) {
        save();
        closed = true;
        super.removed(player);
        // Contents already live in the socket: do not also return/drop the menu's view.
    }
}
