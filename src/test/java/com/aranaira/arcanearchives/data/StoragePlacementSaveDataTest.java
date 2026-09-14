package com.aranaira.arcanearchives.data;

import com.aranaira.arcanearchives.types.BlockPosDimension;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StoragePlacementSaveDataTest {
    @Test void preservesSeparateOwnersTypesAndDimensionsWithoutWorldLoading() {
        ListTag entries = new ListTag();
        var owner = UUID.randomUUID();
        entries.add(entry(Level.OVERWORLD, owner, "chest"));
        entries.add(entry(Level.NETHER, owner, "chest"));
        entries.add(entry(Level.END, UUID.randomUUID(), "tank"));
        var tag = new CompoundTag();
        tag.put("placements", entries);
        var ledger = StoragePlacementSaveData.load(tag);
        //? if >=1.21 {
        var saved = ledger.save(new CompoundTag(), null);
        //?} else {
        /*var saved = ledger.save(new CompoundTag());
        *///?}
        assertEquals(3, saved.getList("placements", Tag.TAG_COMPOUND).size());
        assertTrue(saved.getList("placements", Tag.TAG_COMPOUND).containsAll(entries));
        assertFalse(ledger.isDirty());
        assertEquals(saved, roundTrip(saved));
    }
    @Test void rejectsMalformedAndDuplicateCoordinates() {
        var entry = entry(Level.OVERWORLD, UUID.randomUUID(), "chest");
        var entries = new ListTag(); entries.add(entry); entries.add(entry.copy());
        var tag = new CompoundTag(); tag.put("placements", entries);
        assertThrows(IllegalArgumentException.class, () -> StoragePlacementSaveData.load(tag));
        entries.remove(1); entry.remove("owner");
        assertThrows(IllegalArgumentException.class, () -> StoragePlacementSaveData.load(tag));
        entry.putUUID("owner", UUID.randomUUID()); entry.putString("type", "invalid");
        assertThrows(IllegalArgumentException.class, () -> StoragePlacementSaveData.load(tag));
        entry.putString("type", "chest"); entry.remove("dimension");
        assertThrows(IllegalArgumentException.class, () -> StoragePlacementSaveData.load(tag));
    }
    private static CompoundTag roundTrip(CompoundTag tag) {
        //? if >=1.21 {
        return StoragePlacementSaveData.load(tag).save(new CompoundTag(), null);
        //?} else {
        /*return StoragePlacementSaveData.load(tag).save(new CompoundTag());
        *///?}
    }
    private static CompoundTag entry(net.minecraft.resources.ResourceKey<Level> dimension, UUID owner, String type) {
        var tag = new BlockPosDimension(new BlockPos(40000, 80, 50000), dimension).serializeNBT();
        tag.putUUID("owner", owner); tag.putString("type", type);
        return tag;
    }
}
