package com.aranaira.arcanearchives.data;

import com.aranaira.arcanearchives.tileentities.NetworkOwnedBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantCraftingTableBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantResonatorBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

/** Loaded-device discovery only. Devices own their saved state; Hive visibility is evaluated on every query. */
public final class StorageNetworks {
    // Neither keys nor values keep a stopped world alive. No inventories or chunk tickets are retained here.
    private static final Map<ServerLevel, Map<BlockPos, WeakReference<BlockEntity>>> LEVELS = new WeakHashMap<>();

    private StorageNetworks() {}

    public static UUID owner(BlockEntity entity) {
        if (entity instanceof NetworkOwnedBlockEntity device) return device.networkOwner();
        if (entity instanceof RadiantChestBlockEntity device) return device.owner();
        if (entity instanceof RadiantTroveBlockEntity device) return device.owner();
        if (entity instanceof RadiantTankBlockEntity device) return device.owner();
        if (entity instanceof RadiantCraftingTableBlockEntity device) return device.owner();
        if (entity instanceof RadiantResonatorBlockEntity device) return device.owner();
        return null;
    }

    private static boolean supported(BlockEntity entity) {
        return entity instanceof NetworkOwnedBlockEntity || entity instanceof RadiantChestBlockEntity
            || entity instanceof RadiantTroveBlockEntity || entity instanceof RadiantTankBlockEntity
            || entity instanceof RadiantCraftingTableBlockEntity || entity instanceof RadiantResonatorBlockEntity;
    }

    /** Called after actual chunk installation/removal, never from a detached entity's setLevel. */
    public static void refresh(LevelChunk chunk, BlockPos pos) {
        if (!(chunk.getLevel() instanceof ServerLevel level) || !level.getServer().isSameThread()) return;
        BlockEntity entity = chunk.getBlockEntities().get(pos);
        if (supported(entity)) {
            LEVELS.computeIfAbsent(level, ignored -> new LinkedHashMap<>())
                .put(pos.immutable(), new WeakReference<>(entity));
        } else {
            var entries = LEVELS.get(level);
            if (entries != null) entries.remove(pos);
        }
    }

    /** Rebuild after native chunk loading; asynchronous construction does not touch the server-owned index. */
    public static void loaded(LevelChunk chunk) {
        if (!(chunk.getLevel() instanceof ServerLevel level) || !level.getServer().isSameThread()) return;
        for (BlockPos pos : chunk.getBlockEntities().keySet()) refresh(chunk, pos);
    }

    public static void unloading(LevelChunk chunk) {
        if (!(chunk.getLevel() instanceof ServerLevel level) || !level.getServer().isSameThread()) return;
        var entries = LEVELS.get(level);
        if (entries != null) entries.keySet().removeIf(pos -> chunk.getPos().equals(new net.minecraft.world.level.ChunkPos(pos)));
    }

    /** Current permission snapshot, independent of an open menu or available chunks. Requery before use. */
    public static Set<UUID> audience(MinecraftServer server, UUID requester, boolean personalOnly) {
        Objects.requireNonNull(requester, "requester");
        if (!server.isSameThread()) throw new IllegalStateException("Storage networks require the server thread");
        Set<UUID> owners = new LinkedHashSet<>();
        owners.add(requester);
        if (!personalOnly) {
            var hives = HiveSaveData.get(server);
            UUID hiveOwner = hives.ownerOf(requester);
            if (hiveOwner != null) {
                owners.add(hiveOwner);
                owners.addAll(hives.members(hiveOwner));
            }
        }
        return Set.copyOf(owners);
    }

    /** Return a detached ordered list of live devices, not permission to use them later without revalidation. */
    public static List<BlockEntity> visible(MinecraftServer server, UUID requester, boolean personalOnly) {
        Set<UUID> owners = audience(server, requester, personalOnly);
        List<BlockEntity> result = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            var entries = LEVELS.get(level);
            if (entries == null) continue;
            var iterator = entries.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                BlockEntity entity = entry.getValue().get();
                if (entity == null || entity.isRemoved()) {
                    iterator.remove();
                    continue;
                }
                // Availability can change before a chunk finishes unloading. Never request a missing chunk.
                if (!level.hasChunkAt(entry.getKey())) continue;
                if (level.getBlockEntity(entry.getKey()) != entity) {
                    iterator.remove();
                    continue;
                }
                UUID owner = owner(entity);
                if (owner != null && owners.contains(owner)) result.add(entity);
            }
        }
        return List.copyOf(result);
    }
}
