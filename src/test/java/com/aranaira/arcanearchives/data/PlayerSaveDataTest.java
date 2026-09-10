package com.aranaira.arcanearchives.data;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerSaveDataTest {
    @Test
    void startsUnreceivedAndMarksOnlyTheFirstReceiptDirty() {
        PlayerSaveData data = new PlayerSaveData();
        assertFalse(data.hasReceivedBook());
        assertFalse(data.isDirty());
        data.markBookReceived();
        assertTrue(data.hasReceivedBook());
        assertTrue(data.isDirty());
        data.setDirty(false);
        data.markBookReceived();
        assertFalse(data.isDirty());
    }

    @Test
    void retainsReceiptAcrossNbtRoundTripWithoutAPlayerEntity() {
        PlayerSaveData data = new PlayerSaveData();
        data.markBookReceived();
        //? if >=1.21 {
        CompoundTag saved = data.save(new CompoundTag(), null);
        //?} else {
        /*CompoundTag saved = data.save(new CompoundTag());
        *///?}
        assertTrue(saved.getBoolean("received_book"));
        PlayerSaveData restored = PlayerSaveData.load(saved);
        assertTrue(restored.hasReceivedBook());
        assertFalse(restored.isDirty());
        assertFalse(new PlayerSaveData().hasReceivedBook());
    }

    @Test
    void missingReceiptDefaultsToFalse() {
        assertFalse(PlayerSaveData.load(new CompoundTag()).hasReceivedBook());
    }
}
