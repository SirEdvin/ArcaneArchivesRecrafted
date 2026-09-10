package com.aranaira.arcanearchives.data;

import java.util.UUID;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
//? if >=1.21 {
import net.minecraft.core.HolderLookup;
import net.minecraft.util.datafix.DataFixTypes;
//?}

/** Upstream per-player tome receipt, owned by the overworld rather than the player entity. */
public final class PlayerSaveData extends SavedData {
    public static final String PREFIX = "ArcaneArchives-PlayerSavedData-";
    private boolean receivedBook;

    public static PlayerSaveData get(MinecraftServer server, UUID player) {
        if (!server.isSameThread()) {
            throw new IllegalStateException("Player receipt data must be accessed on the server thread");
        }
        String name = PREFIX + Objects.requireNonNull(player, "player");
        //? if >=1.21 {
        return server.overworld().getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(PlayerSaveData::new, (tag, registries) -> load(tag), DataFixTypes.LEVEL), name);
        //?} else {
        /*return server.overworld().getDataStorage().computeIfAbsent(PlayerSaveData::load, PlayerSaveData::new, name);
        *///?}
    }

    public boolean hasReceivedBook() {
        return receivedBook;
    }

    public void markBookReceived() {
        if (!receivedBook) {
            receivedBook = true;
            setDirty();
        }
    }

    public static PlayerSaveData load(CompoundTag tag) {
        PlayerSaveData data = new PlayerSaveData();
        data.receivedBook = tag.getBoolean("received_book");
        return data;
    }

    @Override
    //? if >=1.21 {
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
    //?} else {
    /*public CompoundTag save(CompoundTag tag) {
    *///?}
        tag.putBoolean("received_book", receivedBook);
        return tag;
    }
}
