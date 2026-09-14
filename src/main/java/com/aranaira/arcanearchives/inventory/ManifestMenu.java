package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.ArcaneArchivesMod;
import com.aranaira.arcanearchives.data.StorageNetworks;
import com.aranaira.arcanearchives.data.ManifestContents;
import com.aranaira.arcanearchives.events.ManifestSnapshot;
import com.aranaira.arcanearchives.init.ContentRegistry;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

/** Read-only locator: no storage slots, carried-stack operations, or remote extraction. */
public final class ManifestMenu extends AbstractContainerMenu {
    // Native container IDs wrap. Never reuse a published revision across menu instances in this process.
    private static final java.util.concurrent.atomic.AtomicLong REVISIONS = new java.util.concurrent.atomic.AtomicLong();
    private final Player viewer;
    private Set<UUID> audience;
    private long lastRefresh = Long.MIN_VALUE;
    private long lastSelection = Long.MIN_VALUE;
    private long snapshotRevision = -1;
    private int maxDistance = 100;
    private List<ManifestContents.Entry> entries = List.of();
    private final com.aranaira.arcanearchives.events.ManifestSnapshotReceiver receiver;
    private boolean failed;
    private boolean ready;

    public ManifestMenu(int id, Inventory inventory) {
        super(ContentRegistry.MANIFEST_MENU.get(), id);
        viewer = inventory.player;
        receiver = new com.aranaira.arcanearchives.events.ManifestSnapshotReceiver(id);
    }
    public static void open(Player player) {
        if (!player.level().isClientSide && player.isAlive())
            player.openMenu(new SimpleMenuProvider((id, inventory, viewer) -> new ManifestMenu(id, inventory),
                Component.translatable("item.arcanearchives.manifest")));
    }
    public static void openFromKey(Player player) {
        // Presence is an original client preference, not permission to see another player's storage.
        // Never replace an active container or accept a request off the server thread.
        if (player instanceof ServerPlayer serverPlayer && serverPlayer.server.isSameThread()
                && player.containerMenu == player.inventoryMenu && !player.isSpectator()
                && !player.isShiftKeyDown()) open(player);
    }
    @Override public boolean stillValid(Player player) { return player == viewer && player.isAlive(); }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {}

    @Override public void broadcastChanges() {
        super.broadcastChanges();
        if (audience == null || !(viewer instanceof ServerPlayer player) || player.containerMenu != this || !stillValid(player)) return;
        Set<UUID> current = currentAudience(player);
        // Initial collection waits for the viewer's settings; only membership changes rescan on ticks.
        if (!current.equals(audience)) refresh(player, current);
    }
    @Override public boolean clickMenuButton(Player player, int button) {
        return button == 0 && request(player, maxDistance);
    }
    public boolean request(Player player, int distance) {
        if (!(player instanceof ServerPlayer serverPlayer) || player.containerMenu != this || !stillValid(player)
                || !serverPlayer.server.isSameThread() || distance < 0) return false;
        long tick = player.level().getGameTime();
        if (lastRefresh != Long.MIN_VALUE && tick - lastRefresh < 10) return false;
        maxDistance = distance;
        refresh(serverPlayer, currentAudience(serverPlayer));
        return true;
    }
    private static Set<UUID> currentAudience(ServerPlayer player) {
        return StorageNetworks.audience(player.server, player.getUUID(), false);
    }
    /** Resolve a displayed selection from server data, never client-supplied coordinates. */
    public java.util.Optional<ManifestContents.Entry> trackingSelection(Player player, long revision, int index, ItemStack reference) {
        if (!(player instanceof ServerPlayer serverPlayer) || !serverPlayer.server.isSameThread()
                || player.containerMenu != this || !stillValid(player) || audience == null
                || revision != snapshotRevision || index < 0 || index >= entries.size() || reference.isEmpty()
                || !currentAudience(serverPlayer).equals(audience)) return java.util.Optional.empty();
        var selected = entries.get(index);
        if (selected.range() == ManifestContents.Range.OTHER_DIMENSION
                || !com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(
                    selected.stack(), reference)) return java.util.Optional.empty();
        long tick = player.level().getGameTime();
        if (lastSelection != Long.MIN_VALUE && tick - lastSelection < 10) return java.util.Optional.empty();
        lastSelection = tick;
        for (var current : ManifestContents.collect(player, maxDistance)) {
            if (current.range() == selected.range() && current.count() == selected.count()
                    && current.locations().equals(selected.locations())
                    && com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(
                        current.stack(), selected.stack())) return java.util.Optional.of(current);
        }
        return java.util.Optional.empty();
    }
    private void refresh(ServerPlayer player, Set<UUID> current) {
        audience = current;
        lastRefresh = player.level().getGameTime();
        byte[] snapshot;
        try {
            var collected = ManifestContents.collect(player, maxDistance);
            snapshot = ManifestSnapshot.encode(collected, player.level().registryAccess());
            entries = collected;
        } catch (RuntimeException error) {
            ArcaneArchivesMod.LOGGER.warn("Cannot encode Manifest snapshot", error);
            // Never silently truncate or leave a revoked Hive's old contents displayed.
            snapshot = new byte[0];
            entries = List.of();
        }
        snapshotRevision = REVISIONS.updateAndGet(Math::incrementExact);
        ManifestSnapshot.send(player, containerId, snapshotRevision, snapshot);
    }

    public List<ManifestContents.Entry> entries() { return entries; }
    public long snapshotRevision() { return snapshotRevision; }
    public boolean failed() { return failed; }
    public boolean ready() { return ready; }
    public void receive(ManifestSnapshot fragment) {
        if (fragment.containerId() != containerId || fragment.revision() < snapshotRevision) return;
        receiver.receive(fragment, viewer.level().registryAccess());
        entries = receiver.entries();
        snapshotRevision = receiver.snapshotRevision();
        failed = receiver.failed();
        ready = receiver.ready();
    }
}
