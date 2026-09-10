package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.items.DevouringCharmItem;
import java.util.stream.IntStream;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class DevouringCharmMenu extends AbstractContainerMenu {
    private final Inventory inventory;
    private final InteractionHand hand;
    private final ItemStack charm;
    private final SimpleContainer disposal = new SimpleContainer(7);
    private final SimpleContainer filters = new SimpleContainer(6);
    private final DataSlot flipped = DataSlot.standalone();
    private boolean closed;

    public DevouringCharmMenu(int id, Inventory inventory) { this(id, inventory, null); }
    public DevouringCharmMenu(int id, Inventory inventory, InteractionHand hand) {
        super(ContentRegistry.DEVOURING_CHARM_MENU.get(), id);
        this.inventory = inventory;
        this.hand = hand;
        charm = hand == null ? ItemStack.EMPTY : inventory.player.getItemInHand(hand);
        if (!charm.isEmpty()) {
            var saved = DevouringCharmItem.filters(charm, inventory.player.level());
            for (int slot = 0; slot < 6; slot++) filters.setItem(slot, saved.get(slot));
        }
        filters.addListener(container -> {
            if (!inventory.player.level().isClientSide && stillValid(inventory.player)) {
                DevouringCharmItem.saveFilters(charm, IntStream.range(0, 6).mapToObj(filters::getItem).toList(), inventory.player.level());
                inventory.setChanged();
            }
        });
        addDataSlot(flipped);
        for (int row = 0; row < 4; row++) for (int col = 0; col < 9; col++) {
            int index = row == 3 ? col : 9 + row * 9 + col;
            addSlot(new Slot(inventory, index, 10 + col * 18, row == 3 ? 223 : 165 + row * 18) {
                @Override public boolean mayPickup(Player player) { return !(getItem().getItem() instanceof DevouringCharmItem); }
                @Override public boolean mayPlace(ItemStack stack) { return !(stack.getItem() instanceof DevouringCharmItem); }
            });
        }
        addSlot(new Slot(disposal, 0, 82, 71) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean isActive() { return !flipped(); }
            @Override public boolean mayPickup(Player player) { return !flipped(); }
            @Override public boolean mayPlace(ItemStack stack) { return !flipped() && DevouringCharmFluids.accepts(stack); }
            @Override public void set(ItemStack stack) {
                if (!inventory.player.level().isClientSide && stack.getItem() instanceof com.aranaira.arcanearchives.items.ParchtearItem)
                    com.aranaira.arcanearchives.items.ArcaneGemItem.setCharge(stack, com.aranaira.arcanearchives.items.ArcaneGemItem.maximumCharge(stack));
                super.set(inventory.player.level().isClientSide ? stack : DevouringCharmFluids.drain(stack));
            }
        });
        for (int index = 0; index < 6; index++) addSlot(new Slot(disposal, index + 1, 64 + index % 3 * 18, 113 + index / 3 * 18) {
            @Override public boolean isActive() { return !flipped(); }
            @Override public boolean mayPickup(Player player) { return !flipped(); }
            @Override public boolean mayPlace(ItemStack stack) { return !flipped() && !(stack.getItem() instanceof DevouringCharmItem); }
            @Override public void set(ItemStack stack) {
                super.set(inventory.player.level().isClientSide ? stack : DevouringCharmFluids.drain(stack));
            }
        });
        for (int index = 0; index < 6; index++) addSlot(new Slot(filters, index, 58 + index % 3 * 24, 99 + index / 3 * 24) {
            @Override public boolean isActive() { return flipped(); }
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) { return false; }
        });
    }
    public boolean flipped() { return flipped.get() != 0; }
    @Override public boolean stillValid(Player player) {
        if (player.level().isClientSide) return true;
        return !closed && player == inventory.player && player.isAlive() && !player.isSpectator()
            && player.getServer() != null && player.getServer().isSameThread() && hand != null
            && player.getItemInHand(hand) == charm && !charm.isEmpty() && charm.getItem() instanceof DevouringCharmItem;
    }
    @Override public void clicked(int index, int button, ClickType type, Player player) {
        if (!stillValid(player) || index >= 43 || getCarried().getItem() instanceof DevouringCharmItem) return;
        if (index >= 0 && (!slots.get(index).isActive() || slots.get(index).getItem().getItem() instanceof DevouringCharmItem)) return;
        if (type == ClickType.SWAP) {
            if (button >= 0 && button < 9 && inventory.getItem(button).getItem() instanceof DevouringCharmItem) return;
            if (button == 40 && player.getOffhandItem().getItem() instanceof DevouringCharmItem) return;
        }
        super.clicked(index, button, type, player);
    }
    @Override public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != filters && !(slot.getItem().getItem() instanceof DevouringCharmItem)
            && slot.isActive() && super.canTakeItemForPickAll(stack, slot);
    }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (player.containerMenu != this || !stillValid(player)) return false;
        if (button == 0) flipped.set(flipped() ? 0 : 1);
        else if (flipped() && button >= 10 && button < 16) filters.setItem(button - 10, ItemStack.EMPTY);
        else if (flipped() && button >= 20 && button < 26) {
            ItemStack sample = getCarried().copy();
            if (sample.getItem() instanceof DevouringCharmItem) return false;
            if (!sample.isEmpty()) sample.setCount(1);
            filters.setItem(button - 20, sample);
        } else return false;
        broadcastChanges();
        return true;
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || flipped() || index < 0 || index >= 43) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        ItemStack moving = slot.getItem();
        if (moving.isEmpty() || moving.getItem() instanceof DevouringCharmItem) return ItemStack.EMPTY;
        ItemStack original = moving.copy();
        if (index < 36) {
            if (DevouringCharmFluids.accepts(moving)) {
                ItemStack previous = disposal.getItem(0);
                if (!previous.isEmpty()) {
                    // Returning an identical container to the source would make native shift-click loop forever.
                    moveItemStackTo(previous, 0, index, true);
                    if (!previous.isEmpty()) moveItemStackTo(previous, index + 1, 36, true);
                    disposal.setItem(0, previous);
                    if (!previous.isEmpty()) return ItemStack.EMPTY;
                }
                if (!moveItemStackTo(moving, 36, 37, false)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(moving, 37, 43, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(moving, 0, 36, true)) return ItemStack.EMPTY;
        if (moving.getCount() == original.getCount()) return ItemStack.EMPTY;
        if (moving.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        slot.onTake(player, moving);
        return original;
    }
    @Override public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide || closed) return;
        closed = true;
        ItemStack container = disposal.removeItemNoUpdate(0);
        disposal.clearContent();
        if (!container.isEmpty()) {
            player.getInventory().add(container);
            if (!container.isEmpty()) player.drop(container, false);
        }
    }
}
