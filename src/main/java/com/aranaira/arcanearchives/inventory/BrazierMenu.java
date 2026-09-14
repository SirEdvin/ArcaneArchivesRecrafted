package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.BrazierBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/** Configuration only: no inventory slots, source payment or remote item access. */
public final class BrazierMenu extends AbstractContainerMenu {
    private final Player viewer;
    private final BrazierBlockEntity brazier;
    private final SimpleContainerData settings = new SimpleContainerData(7);
    public BrazierMenu(int id, Inventory inventory) { this(id, inventory, null); }
    public BrazierMenu(int id, Inventory inventory, BrazierBlockEntity brazier) {
        super(ContentRegistry.BRAZIER_MENU.get(), id);
        viewer = inventory.player;
        this.brazier = brazier;
        addDataSlots(settings);
        if (brazier != null) {
            long position = brazier.getBlockPos().asLong();
            // Four unsigned words survive the signed-short native 1.20 data packet.
            for (int word = 0; word < 4; word++) settings.set(2 + word, (int) (position >>> (word * 16)) & 0xffff);
            settings.set(6, 1); // Publish readiness after all position words.
        }
        refreshSettings();
    }
    public int radius() { return settings.get(0); }
    /** Display-only location; never used to resolve the server mutation target. */
    public net.minecraft.core.BlockPos position() {
        if (settings.get(6) != 1) return null;
        long position = 0;
        for (int word = 0; word < 4; word++) position |= (long) (settings.get(2 + word) & 0xffff) << (word * 16);
        return net.minecraft.core.BlockPos.of(position);
    }
    public static void open(Player player, BrazierBlockEntity brazier) {
        if (brazier.canConfigure(player)) player.openMenu(new net.minecraft.world.SimpleMenuProvider(
            (id, inventory, viewer) -> new BrazierMenu(id, inventory, brazier),
            net.minecraft.network.chat.Component.translatable("block.arcanearchives.brazier_of_hoarding")));
    }
    public boolean personalOnly() { return settings.get(1) != 0; }
    private void refreshSettings() {
        if (brazier != null) {
            settings.set(0, brazier.radius());
            settings.set(1, brazier.personalOnly() ? 1 : 0);
        }
    }
    @Override public boolean stillValid(Player player) {
        return player == viewer && (brazier != null ? brazier.canConfigure(player) : player.level().isClientSide);
    }
    @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {}
    @Override public void broadcastChanges() { refreshSettings(); super.broadcastChanges(); }
    private boolean canChange(Player player) {
        return brazier != null && player.containerMenu == this && stillValid(player);
    }
    public boolean setRadius(Player player, int radius) {
        if (!canChange(player)) return false;
        brazier.configure(radius, brazier.personalOnly());
        broadcastChanges();
        return true;
    }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (!canChange(player)) return false;
        switch (button) {
            case 0 -> brazier.configure(brazier.radius() - 10, brazier.personalOnly());
            case 1 -> brazier.configure(brazier.radius() + 10, brazier.personalOnly());
            case 2 -> brazier.configure(brazier.radius(), !brazier.personalOnly());
            default -> { return false; }
        }
        broadcastChanges();
        return true;
    }
}
