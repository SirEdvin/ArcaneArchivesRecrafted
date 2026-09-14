package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Native packets carry ordinary-sized display stacks; separate data slots carry extended counts. */
public final class RadiantChestMenu extends AbstractContainerMenu {
    public static final int MAX_NAME_LENGTH = 32; // Original GuiTextField default.
    private final RadiantChestBlockEntity chest;
    private final DataSlot routingMode = DataSlot.standalone();
    private final DataSlot[] nameCharacters = new DataSlot[MAX_NAME_LENGTH];
    private final SimpleContainer display = new SimpleContainer(54);
    private final int[] counts = new int[54];
    private final Set<Integer> dragSlots = new LinkedHashSet<>();
    private int dragMode = -1;

    public RadiantChestMenu(int id, Inventory inventory) { this(id, inventory, null); }

    public RadiantChestMenu(int id, Inventory inventory, RadiantChestBlockEntity chest) {
        super(ContentRegistry.RADIANT_CHEST_MENU.get(), id);
        this.chest = chest;
        addDataSlot(routingMode);
        // Native menu data slots are 16-bit, exactly one UTF-16 code unit each.
        for (int index = 0; index < nameCharacters.length; index++)
            nameCharacters[index] = addDataSlot(DataSlot.standalone());
        for (int slot = 0; slot < 54; slot++) {
            final int index = slot;
            addSlot(new Slot(display, slot, 16 + slot % 9 * 18, 16 + slot / 9 * 18));
            for (int half = 0; half < 2; half++) {
                final int shift = half * 16;
                addDataSlot(new DataSlot() {
                    @Override public int get() { return counts[index] >>> shift & 65535; }
                    @Override public void set(int value) {
                        counts[index] = (counts[index] & ~(65535 << shift)) | ((value & 65535) << shift);
                    }
                });
            }
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlot(new Slot(inventory, col + row * 9 + 9, 16 + col * 18, 142 + row * 18));
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 16 + col * 18, 200));
        updateDisplay();
    }

    public int count(int slot) { return counts[slot]; }
    public boolean noNewStacks() { return routingMode.get() == 1; }

    @Override public boolean clickMenuButton(Player player, int button) {
        if (button != 0 || chest == null || player.containerMenu != this || player.isSpectator()
                || !chest.isLiveServerStorage() || !stillValid(player)
                || !player.level().mayInteract(player, chest.getBlockPos())) return false;
        if (!chest.toggleRoutingType()) return false;
        broadcastChanges();
        return true;
    }

    public String chestName() {
        var result = new StringBuilder();
        for (DataSlot value : nameCharacters) {
            char character = (char) (value.get() & 65535);
            if (character == 0) break;
            result.append(character);
        }
        return result.toString();
    }

    public void rename(Player player, String name) {
        if (chest == null || player.containerMenu != this || player.isSpectator()
                || !chest.isLiveServerStorage() || !stillValid(player)
                || !player.level().mayInteract(player, chest.getBlockPos()) || name.length() > MAX_NAME_LENGTH) return;
        //? if >=1.21 {
        if (!net.minecraft.util.StringUtil.filterText(name).equals(name)) return;
        //?} else {
        /*if (!net.minecraft.SharedConstants.filterText(name).equals(name)) return;
        *///?}
        chest.setName(name);
        broadcastChanges();
    }

    private void updateDisplay() {
        if (chest == null) return;
        routingMode.set(chest.noNewStacks() ? 1 : 0);
        String name = chest.chestName();
        for (int index = 0; index < nameCharacters.length; index++)
            nameCharacters[index].set(index < name.length() ? name.charAt(index) : 0);
        for (int slot = 0; slot < 54; slot++) {
            ItemStack stack = chest.inventory().getStackInSlot(slot).copy();
            counts[slot] = stack.getCount();
            if (!stack.isEmpty()) stack.setCount(1);
            display.setItem(slot, stack);
        }
    }

    @Override public void broadcastChanges() { updateDisplay(); super.broadcastChanges(); }
    @Override public void broadcastFullState() { updateDisplay(); super.broadcastFullState(); }
    @Override public boolean stillValid(Player player) { return chest == null || chest.stillValid(player); }

    private ItemStack stack(int index) {
        return index < 54 ? chest.inventory().getStackInSlot(index).copy() : slots.get(index).getItem().copy();
    }

    private int capacity(int index, ItemStack stack) {
        return index < 54 ? chest.inventory().getStackLimit(index, stack) : slots.get(index).getMaxStackSize(stack);
    }

    private void put(int index, ItemStack stack) {
        if (index < 54) chest.inventory().setStackInSlot(index, stack);
        else slots.get(index).set(stack);
    }

    private ItemStack insert(int index, ItemStack offered) {
        if (index < 54) return chest.inventory().insertItem(index, offered, false);
        return slots.get(index).safeInsert(offered);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (chest == null || !stillValid(player) || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        ItemStack original = stack(index);
        ItemStack remainder = original.copy();
        int start = index < 54 ? 54 : 0;
        int end = index < 54 ? slots.size() : 54;
        for (int pass = 0; pass < 2; pass++) for (int target = start; target < end && !remainder.isEmpty(); target++) {
            ItemStack existing = stack(target);
            if (pass == 0 ? !existing.isEmpty() && ExtendedItemStackHandler.sameItemAndData(existing, remainder) : existing.isEmpty())
                remainder = insert(target, remainder);
        }
        if (remainder.getCount() == original.getCount()) return ItemStack.EMPTY;
        put(index, remainder);
        return original;
    }

    @Override
    public void clicked(int index, int button, ClickType type, Player player) {
        if (chest == null) return; // Server owns every click, including extended drag distributions.
        if (!stillValid(player)) return;
        if (index >= slots.size() || index < -1 && index != -999) return;
        if (type == ClickType.QUICK_CRAFT) {
            int stage = getQuickcraftHeader(button);
            int mode = getQuickcraftType(button);
            if (stage == 0) {
                dragSlots.clear();
                dragMode = isValidQuickcraftType(mode, player) ? mode : -1;
            } else if (stage == 1 && dragMode == mode && index >= 0 && index < slots.size() && !getCarried().isEmpty()) {
                ItemStack existing = stack(index);
                if ((existing.isEmpty() || ExtendedItemStackHandler.sameItemAndData(existing, getCarried()))
                        && existing.getCount() < capacity(index, getCarried()) && slots.get(index).mayPlace(getCarried())
                        && (mode == 2 || getCarried().getCount() > dragSlots.size())) dragSlots.add(index);
            } else if (stage == 2 && dragMode == mode && !getCarried().isEmpty()) {
                int share = mode == 0 && !dragSlots.isEmpty() ? getCarried().getCount() / dragSlots.size() : 1;
                for (int target : dragSlots) {
                    ItemStack existing = stack(target);
                    if (!existing.isEmpty() && !ExtendedItemStackHandler.sameItemAndData(existing, getCarried())) continue;
                    int amount = mode == 2 ? capacity(target, getCarried()) - existing.getCount() : Math.min(share, getCarried().getCount());
                    ItemStack offered = getCarried().copy();
                    offered.setCount(Math.max(0, amount));
                    ItemStack remainder = insert(target, offered);
                    if (mode != 2) getCarried().shrink(amount - remainder.getCount());
                }
                dragSlots.clear();
                dragMode = -1;
            } else {
                dragSlots.clear();
                dragMode = -1;
            }
        } else {
            dragSlots.clear();
            dragMode = -1;
            if (type == ClickType.QUICK_MOVE) {
                quickMoveStack(player, index);
            } else if (type == ClickType.PICKUP_ALL && !getCarried().isEmpty()) {
                for (int step = 0; step < slots.size() && getCarried().getCount() < getCarried().getMaxStackSize(); step++) {
                    int target = button == 0 ? step : slots.size() - 1 - step;
                    ItemStack existing = stack(target);
                    if (!existing.isEmpty() && ExtendedItemStackHandler.sameItemAndData(existing, getCarried())) {
                        int amount = Math.min(existing.getCount(), getCarried().getMaxStackSize() - getCarried().getCount());
                        existing.shrink(amount);
                        getCarried().grow(amount);
                        put(target, existing);
                    }
                }
            } else if (index < 0 || index >= 54) {
                super.clicked(index, button, type, player);
            } else {
                ItemStack existing = stack(index);
                ItemStack carried = getCarried();
                if (type == ClickType.PICKUP && (button == 0 || button == 1)) {
                    if (carried.isEmpty()) {
                        int count = Math.min(existing.getCount(), existing.getMaxStackSize());
                        setCarried(existing.split(button == 0 ? count : (count + 1) / 2));
                        put(index, existing);
                    } else if (existing.isEmpty() || ExtendedItemStackHandler.sameItemAndData(existing, carried)) {
                        ItemStack offered = carried.copy();
                        offered.setCount(button == 0 ? carried.getCount() : 1);
                        carried.shrink(offered.getCount() - insert(index, offered).getCount());
                    } else if (existing.getCount() <= existing.getMaxStackSize() && carried.getCount() <= capacity(index, carried)) {
                        put(index, carried);
                        setCarried(existing);
                    }
                } else if (type == ClickType.SWAP && (button >= 0 && button < 9 || button == 40)) {
                    ItemStack hotbar = player.getInventory().getItem(button).copy();
                    if (hotbar.isEmpty()) {
                        player.getInventory().setItem(button, existing.split(existing.getMaxStackSize()));
                        put(index, existing);
                    } else if (hotbar.getCount() <= capacity(index, hotbar)) {
                        put(index, hotbar);
                        player.getInventory().setItem(button, existing.split(existing.getMaxStackSize()));
                        while (!existing.isEmpty()) {
                            ItemStack part = existing.split(existing.getMaxStackSize());
                            if (!player.getInventory().add(part)) player.drop(part, true);
                        }
                    }
                } else if (type == ClickType.CLONE && player.getAbilities().instabuild && carried.isEmpty() && !existing.isEmpty()) {
                    existing.setCount(existing.getMaxStackSize());
                    setCarried(existing);
                } else if (type == ClickType.THROW && carried.isEmpty() && (button == 0 || button == 1)) {
                    ItemStack dropped = existing.split(button == 0 ? 1 : existing.getCount());
                    put(index, existing);
                    while (!dropped.isEmpty()) player.drop(dropped.split(dropped.getMaxStackSize()), true);
                }
            }
        }
        broadcastChanges();
    }
}
