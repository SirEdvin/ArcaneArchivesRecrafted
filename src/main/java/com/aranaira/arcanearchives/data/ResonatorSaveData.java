package com.aranaira.arcanearchives.data;

import com.aranaira.arcanearchives.types.BlockPosDimension;
import com.aranaira.arcanearchives.tileentities.RadiantResonatorBlockEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
//? if >=1.21 {
import net.minecraft.core.HolderLookup;
import net.minecraft.util.datafix.DataFixTypes;
//?}

/** Placement accounting survives unloaded chunks without loading them to count devices. */
public final class ResonatorSaveData extends SavedData {
    private final Map<BlockPosDimension, UUID> owners = new HashMap<>();

    public static ResonatorSaveData get(MinecraftServer server) {
        if (!server.isSameThread()) throw new IllegalStateException("Resonator data requires the server thread");
        //? if >=1.21 {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(
            ResonatorSaveData::new, (tag, registries) -> load(tag), DataFixTypes.LEVEL), "arcanearchives-resonators");
        //?} else {
        /*return server.overworld().getDataStorage().computeIfAbsent(ResonatorSaveData::load,
            ResonatorSaveData::new, "arcanearchives-resonators");
        *///?}
    }

    public long count(MinecraftServer server, UUID owner) {
        boolean changed = owners.entrySet().removeIf(entry -> {
            var level = server.getLevel(entry.getKey().dimension);
            if (level == null || !level.hasChunkAt(entry.getKey().pos)) return false;
            return !(level.getBlockEntity(entry.getKey().pos) instanceof RadiantResonatorBlockEntity resonator)
                || !entry.getValue().equals(resonator.owner());
        });
        if (changed) setDirty();
        return owners.values().stream().filter(owner::equals).count();
    }

    public void register(BlockPosDimension position, UUID owner) {
        if (!owner.equals(owners.put(position, owner))) setDirty();
    }

    public void remove(BlockPosDimension position) {
        if (owners.remove(position) != null) setDirty();
    }

    private static ResonatorSaveData load(CompoundTag tag) {
        ResonatorSaveData data = new ResonatorSaveData();
        for (Tag value : tag.getList("resonators", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) value;
            if (!entry.hasUUID("owner")) throw new IllegalArgumentException("Missing resonator owner");
            BlockPosDimension position = BlockPosDimension.deserializeNBT(entry);
            if (data.owners.put(position, entry.getUUID("owner")) != null)
                throw new IllegalArgumentException("Duplicate resonator position");
        }
        return data;
    }

    @Override
    //? if >=1.21 {
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
    //?} else {
    /*public CompoundTag save(CompoundTag tag) {
    *///?}
        ListTag list = new ListTag();
        owners.forEach((position, owner) -> {
            CompoundTag entry = position.serializeNBT();
            entry.putUUID("owner", owner);
            list.add(entry);
        });
        tag.put("resonators", list);
        return tag;
    }
}
