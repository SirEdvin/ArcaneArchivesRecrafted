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

    @Test
    void pendingReturnsPreservePayloadAndReceiptWithoutMutableAliases() {
        var data = new PlayerSaveData();
        assertFalse(data.hasBrazierPendingReturns());
        assertNull(data.brazierPendingReturns());
        data.markBookReceived();
        var pending = new CompoundTag();
        pending.putInt("count", Integer.MAX_VALUE);
        var item = new CompoundTag();
        item.putString("id", "arcanearchives:unavailable_item_fixture");
        pending.put("item", item);
        var expected = pending.copy();
        data.setDirty(false);
        data.setBrazierPendingReturns(pending);
        assertTrue(data.isDirty());
        pending.putInt("count", 1);
        ((CompoundTag) data.brazierPendingReturns()).remove("item");
        assertEquals(expected, data.brazierPendingReturns());
        data.setDirty(false);
        data.setBrazierPendingReturns(expected);
        assertFalse(data.isDirty());
        //? if >=1.21 {
        var saved = data.save(new CompoundTag(), null);
        //?} else {
        /*var saved = data.save(new CompoundTag());
        *///?}
        var restored = PlayerSaveData.load(saved);
        saved.getCompound("brazier_pending_returns").remove("item");
        assertEquals(expected, restored.brazierPendingReturns());
        assertTrue(restored.hasReceivedBook());
        assertFalse(restored.isDirty());
        restored.setBrazierPendingReturns(null);
        assertTrue(restored.isDirty());
        assertFalse(restored.hasBrazierPendingReturns());
        //? if >=1.21 {
        restored.save(saved, null);
        //?} else {
        /*restored.save(saved);
        *///?}
        assertFalse(saved.contains("brazier_pending_returns"));
        assertTrue(saved.getBoolean("received_book"));
    }

    @Test
    void unrecognizedPendingPayloadSurvivesWithoutBeingTreatedAsDelivered() {
        var saved = new CompoundTag();
        saved.putString("brazier_pending_returns", "unrecognized future payload");
        var data = PlayerSaveData.load(saved);
        assertTrue(data.hasBrazierPendingReturns());
        //? if >=1.21 {
        var resaved = data.save(new CompoundTag(), null);
        //?} else {
        /*var resaved = data.save(new CompoundTag());
        *///?}
        resaved.remove("received_book");
        assertEquals(saved, resaved);
    }
}
