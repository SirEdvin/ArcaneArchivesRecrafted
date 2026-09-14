package com.aranaira.arcanearchives.data;

import com.aranaira.arcanearchives.inventory.ManifestMenu;
import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity;
import com.aranaira.arcanearchives.types.BlockPosDimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

/** Server-session selections. Cached coordinates survive menu close, not loss of their captured Hive grants. */
public final class ManifestTracking {
    // Values retain neither a player nor a world. A new connection starts with no selections.
    private static final Map<ServerPlayer, List<Marker>> TRACKED = new WeakHashMap<>();
    private static final Map<ServerPlayer, List<Marker>> SENT = new WeakHashMap<>();
    private static final Map<ServerPlayer, Long> LAST_HOVER = new WeakHashMap<>();
    private static final java.util.concurrent.atomic.AtomicLong REVISIONS = new java.util.concurrent.atomic.AtomicLong();

    public record Marker(ItemStack stack, BlockPosDimension position, Set<UUID> owners) {
        public Marker {
            stack = stack.copy();
            stack.setCount(1);
            owners = Set.copyOf(owners);
        }
        @Override public ItemStack stack() { return stack.copy(); }
    }

    private ManifestTracking() {}

    public static void initialize() {
        //? if fabric {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (Player player : server.getPlayerList().getPlayers()) tick(player);
        });
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.TickEvent.PlayerTickEvent event) -> {
                if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) tick(event.player);
            });
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) -> tick(event.getEntity()));
        *///?}
    }

    /** Original sneaking shortcut clears only the sender's current dimension, without opening a menu. */
    public static void clearFromKey(Player viewer) {
        if (!(viewer instanceof ServerPlayer player) || !player.server.isSameThread()
                || player.server.getPlayerList().getPlayer(player.getUUID()) != player
                || !player.isAlive() || player.isSpectator() || !player.isShiftKeyDown()
                || player.containerMenu != player.inventoryMenu) return;
        var markers = TRACKED.get(player);
        if (markers == null) return;
        markers.removeIf(marker -> marker.position().dimension.equals(player.level().dimension()));
        if (markers.isEmpty()) TRACKED.remove(player);
    }

    /** 0 adds, 1 removes the displayed group, 2 clears this player's selections. */
    public static boolean apply(Player viewer, int container, long revision, int index, int action) {
        if (!(viewer instanceof ServerPlayer player) || !player.server.isSameThread()
                || player.server.getPlayerList().getPlayer(player.getUUID()) != player
                || !(player.containerMenu instanceof ManifestMenu menu) || menu.containerId != container
                || !menu.stillValid(player) || revision != menu.snapshotRevision() || revision < 0
                || action < 0 || action > 2) return false;
        if (action == 2) { TRACKED.remove(player); return true; }
        if (index < 0 || index >= menu.entries().size()) return false;
        var displayed = menu.entries().get(index);
        if (displayed.range() == ManifestContents.Range.OTHER_DIMENSION) return false;
        if (action == 1) {
            var markers = TRACKED.get(player);
            if (markers != null) {
                markers.removeIf(marker -> sameItem(marker.stack(), displayed.stack()) && displayed.locations().stream()
                    .anyMatch(location -> location.position().equals(marker.position())));
                if (markers.isEmpty()) TRACKED.remove(player);
            }
            return true;
        }
        var selected = menu.trackingSelection(player, revision, index, displayed.stack());
        if (selected.isEmpty()) return false;
        return add(player, List.of(selected.get()));
    }

    /** Original GUI shortcut: reference only; all locations and permissions are resolved now on the server. */
    public static boolean fromHover(Player viewer, int container, int distance, boolean keepOpen,
            java.util.function.Supplier<ItemStack> reference) {
        if (!(viewer instanceof ServerPlayer player) || !player.server.isSameThread()
                || player.server.getPlayerList().getPlayer(player.getUUID()) != player
                || !player.isAlive() || player.isSpectator() || player.containerMenu.containerId != container
                || !player.containerMenu.stillValid(player) || distance < 0) return false;
        long now = player.server.overworld().getGameTime();
        Long last = LAST_HOVER.get(player);
        if (last != null && now - last < 10) return false;
        LAST_HOVER.put(player, now);
        try {
            ItemStack stack = reference.get();
            if (stack.isEmpty() || stack.getCount() != 1) return false;
            var selected = ManifestContents.collect(player, distance).stream()
                .filter(entry -> entry.range() == ManifestContents.Range.IN_RANGE && sameItem(entry.stack(), stack)).toList();
            if (selected.isEmpty() || !add(player, selected)) return false;
            if (!keepOpen) player.closeContainer();
            return true;
        } catch (RuntimeException malformed) { return false; }
    }

    private static boolean add(ServerPlayer player, List<ManifestContents.Entry> selected) {
        var grants = sourceOwners(player);
        // Resolve the complete group before publishing any marker; never accept client coordinates or owners.
        List<Marker> additions = new ArrayList<>();
        for (var entry : selected) for (var location : entry.locations()) {
            var owners = grants.get(location.position());
            if (owners == null || owners.isEmpty()) return false;
            additions.add(new Marker(entry.stack(), location.position(), owners));
        }
        var markers = TRACKED.computeIfAbsent(player, ignored -> new ArrayList<>());
        for (var addition : additions) {
            markers.removeIf(marker -> marker.position().equals(addition.position()) && sameItem(marker.stack(), addition.stack()));
            markers.add(addition);
        }
        return true;
    }

    /** No inventory scans or chunk loading. Confirmed removals and revoked grants stay removed after menu close. */
    public static void tick(Player viewer) {
        if (!(viewer instanceof ServerPlayer player) || !player.server.isSameThread()) return;
        var markers = TRACKED.get(player);
        if (player.server.getPlayerList().getPlayer(player.getUUID()) != player) {
            TRACKED.remove(player);
            SENT.remove(player);
            LAST_HOVER.remove(player);
            return;
        }
        if (markers == null) { publish(player, List.of()); return; }
        var audience = StorageNetworks.audience(player.server, player.getUUID(), false);
        Map<BlockPosDimension, Set<UUID>> currentGrants = null;
        for (int i = markers.size() - 1; i >= 0; i--) {
            var marker = markers.get(i);
            var remaining = new HashSet<>(marker.owners());
            remaining.retainAll(audience);
            var level = player.server.getLevel(marker.position().dimension);
            if (level != null && level.hasChunkAt(marker.position().pos)) {
                var target = level.getBlockEntity(marker.position().pos);
                UUID currentOwner = target == null ? null : StorageNetworks.owner(target);
                if (target == null || MonitoringCrystalBlockEntity.isArcaneDevice(target)
                        && (currentOwner == null || !marker.owners().contains(currentOwner) || !audience.contains(currentOwner))) {
                    markers.remove(i);
                    continue;
                }
                if (MonitoringCrystalBlockEntity.isArcaneDevice(target)) remaining.retainAll(Set.of(currentOwner));
                else if (monitorAreaAvailable(level, target)) {
                    if (currentGrants == null) currentGrants = sourceOwners(player);
                    remaining.retainAll(currentGrants.getOrDefault(marker.position(), Set.of()));
                }
            }
            if (remaining.equals(marker.owners())) continue;
            if (remaining.isEmpty()) markers.remove(i);
            else markers.set(i, new Marker(marker.stack(), marker.position(), remaining));
        }
        if (markers.isEmpty()) TRACKED.remove(player);
        publish(player, markers);
    }

    /** Tracking sends reference items/locations only, never server-owned grants or live inventory counts. */
    public static byte[] encodeMarkers(List<Marker> markers, net.minecraft.core.HolderLookup.Provider registries) {
        var entries = markers.stream().map(marker -> new ManifestContents.Entry(marker.stack(), 1,
            ManifestContents.Range.IN_RANGE, List.of(new ManifestContents.Location(marker.position(), "", 1)))).toList();
        return com.aranaira.arcanearchives.events.ManifestSnapshot.encode(entries, registries);
    }

    private static void publish(ServerPlayer player, List<Marker> markers) {
        if (markers.equals(SENT.get(player))) return;
        byte[] bytes;
        try { bytes = encodeMarkers(markers, player.level().registryAccess()); }
        catch (RuntimeException error) {
            com.aranaira.arcanearchives.ArcaneArchivesMod.LOGGER.warn("Cannot encode Manifest tracking snapshot", error);
            bytes = new byte[0]; // Fail closed on the client rather than retain stale or partial locations.
        }
        com.aranaira.arcanearchives.events.ManifestSnapshot.sendTracking(player, REVISIONS.updateAndGet(Math::incrementExact), bytes);
        SENT.put(player, List.copyOf(markers));
    }

    public static List<Marker> markers(ServerPlayer player) {
        if (!player.server.isSameThread()) throw new IllegalStateException("Manifest tracking requires the server thread");
        tick(player);
        return List.copyOf(TRACKED.getOrDefault(player, List.of()));
    }

    private static boolean sameItem(ItemStack left, ItemStack right) {
        return ExtendedItemStackHandler.sameItemAndData(left, right);
    }

    /** A missing adjacent monitor is conclusive only when all possible attachment chunks are available. */
    private static boolean monitorAreaAvailable(net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.level.block.entity.BlockEntity target) {
        var pos = target.getBlockPos();
        for (var direction : net.minecraft.core.Direction.values())
            if (!level.hasChunkAt(pos.relative(direction))) return false;
        var state = target.getBlockState();
        if (target instanceof ChestBlockEntity && state.getBlock() instanceof ChestBlock
                && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            var other = pos.relative(ChestBlock.getConnectedDirection(state));
            for (var direction : net.minecraft.core.Direction.values())
                if (!level.hasChunkAt(other.relative(direction))) return false;
        }
        return true;
    }

    /** Capture every currently visible grant, including duplicate monitors and either half of a double chest. */
    private static Map<BlockPosDimension, Set<UUID>> sourceOwners(ServerPlayer player) {
        Map<BlockPosDimension, Set<UUID>> result = new HashMap<>();
        for (var device : StorageNetworks.visible(player.server, player.getUUID(), false)) {
            UUID owner = StorageNetworks.owner(device);
            var target = device instanceof MonitoringCrystalBlockEntity crystal ? crystal.targetTile() : device;
            if (target == null) continue;
            var position = new BlockPosDimension(target.getBlockPos(), target.getLevel().dimension());
            result.computeIfAbsent(position, ignored -> new HashSet<>()).add(owner);
            var state = target.getBlockState();
            if (device instanceof MonitoringCrystalBlockEntity && target instanceof ChestBlockEntity
                    && state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                var adjacent = position.pos.relative(ChestBlock.getConnectedDirection(state));
                if (!target.getLevel().hasChunkAt(adjacent)) continue;
                var other = target.getLevel().getBlockState(adjacent);
                if (other.is(state.getBlock()) && other.getValue(ChestBlock.TYPE) == state.getValue(ChestBlock.TYPE).getOpposite()
                        && other.getValue(ChestBlock.FACING) == state.getValue(ChestBlock.FACING)
                        && target.getLevel().getBlockEntity(adjacent) instanceof ChestBlockEntity)
                    result.computeIfAbsent(new BlockPosDimension(adjacent, position.dimension), ignored -> new HashSet<>()).add(owner);
            }
        }
        return result;
    }
}
