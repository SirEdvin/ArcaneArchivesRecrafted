package com.aranaira.arcanearchives.data;

import com.aranaira.arcanearchives.config.ServerSideConfig;
import com.aranaira.arcanearchives.blocks.RadiantChest;
import com.aranaira.arcanearchives.blocks.RadiantTrove;
import com.aranaira.arcanearchives.blocks.RadiantTank;
import com.aranaira.arcanearchives.types.BlockPosDimension;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;

/** World-wide placement accounting; chunk unload does not release a player's quota. */
public final class StoragePlacementSaveData extends SavedData {
    private record Entry(UUID owner, String type) {}
    private final Map<BlockPosDimension, Entry> entries = new HashMap<>();

    public static StoragePlacementSaveData get(MinecraftServer server) {
        if (!server.isSameThread()) throw new IllegalStateException("Storage placement data requires the server thread");
        //? if >=1.21 {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(
            StoragePlacementSaveData::new, (tag, registries) -> load(tag), net.minecraft.util.datafix.DataFixTypes.LEVEL), "arcanearchives-storage-placements");
        //?} else {
        /*return server.overworld().getDataStorage().computeIfAbsent(StoragePlacementSaveData::load,
            StoragePlacementSaveData::new, "arcanearchives-storage-placements");
        *///?}
    }
    public static String type(Block block) {
        return block instanceof RadiantChest ? "chest" : block instanceof RadiantTrove ? "trove"
            : block instanceof RadiantTank ? "tank" : "";
    }
    public static boolean mayPlace(Player player, Block block) {
        if (player == null || player.level().isClientSide) return true;
        var config = ServerSideConfig.current();
        String type = type(block);
        int limit = switch (type) {
            case "chest" -> config.radiantChestLimit();
            case "trove" -> config.radiantTroveLimit();
            case "tank" -> config.radiantTankLimit();
            default -> 0;
        };
        if (limit == 0 || player.getAbilities().instabuild) return true;
        var server = player.getServer();
        if (get(server).count(server, player.getUUID(), type) < limit) return true;
        player.displayClientMessage(Component.translatable("arcanearchives.error.toomanyplaced", limit, block.getName()), true);
        return false;
    }
    public static void record(BlockEntity entity) {
        if (entity.getLevel() instanceof ServerLevel level && level.getServer().isSameThread()
                && !entity.isRemoved() && level.hasChunkAt(entity.getBlockPos())
                && level.getBlockEntity(entity.getBlockPos()) == entity)
            get(level.getServer()).refresh(level, entity.getBlockPos(), entity);
    }
    public void refresh(ServerLevel level, BlockPos pos, BlockEntity entity) {
        var position = new BlockPosDimension(pos.immutable(), level.dimension());
        String type = entity == null ? "" : type(entity.getBlockState().getBlock());
        UUID owner = entity == null ? null : StorageNetworks.owner(entity);
        Entry next = owner == null || type.isEmpty() ? null : new Entry(owner, type);
        Entry old = next == null ? entries.remove(position) : entries.put(position, next);
        if (!Objects.equals(old, next)) setDirty();
    }
    public long count(MinecraftServer server, UUID owner, String type) {
        // Reconcile only available coordinates. Never request an unloaded chunk to count it.
        for (var pos : java.util.List.copyOf(entries.keySet())) {
            var level = server.getLevel(pos.dimension);
            if (level != null && level.hasChunkAt(pos.pos)) refresh(level, pos.pos, level.getBlockEntity(pos.pos));
        }
        return entries.values().stream().filter(entry -> entry.owner.equals(owner) && entry.type.equals(type)).count();
    }
    public static StoragePlacementSaveData load(CompoundTag tag) {
        var result = new StoragePlacementSaveData();
        for (Tag value : tag.getList("placements", Tag.TAG_COMPOUND)) {
            var entry = (CompoundTag) value;
            String type = entry.getString("type");
            if (!entry.hasUUID("owner") || !java.util.Set.of("chest", "trove", "tank").contains(type))
                throw new IllegalArgumentException("Invalid saved storage placement");
            if (result.entries.put(BlockPosDimension.deserializeNBT(entry), new Entry(entry.getUUID("owner"), type)) != null)
                throw new IllegalArgumentException("Duplicate saved storage placement");
        }
        return result;
    }
    @Override
    //? if >=1.21 {
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
    //?} else {
    /*public CompoundTag save(CompoundTag tag) {
    *///?}
        ListTag values = new ListTag();
        entries.forEach((pos, value) -> {
            var entry = pos.serializeNBT();
            entry.putUUID("owner", value.owner);
            entry.putString("type", value.type);
            values.add(entry);
        });
        tag.put("placements", values);
        return tag;
    }
}
