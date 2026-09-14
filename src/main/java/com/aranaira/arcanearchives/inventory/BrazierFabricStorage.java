package com.aranaira.arcanearchives.inventory;

//? if fabric {
import com.aranaira.arcanearchives.tileentities.BrazierBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

/** No local item buffer; destination participants own rollback. */
public final class BrazierFabricStorage extends SnapshotParticipant<Boolean> implements Storage<ItemVariant> {
    private final BrazierBlockEntity brazier;
    private boolean soundPending;
    public BrazierFabricStorage(BrazierBlockEntity brazier) { this.brazier = brazier; }
    @Override public long insert(ItemVariant variant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(variant, maxAmount);
        if (maxAmount == 0) return 0;
        int offered = (int) Math.min(Integer.MAX_VALUE, maxAmount);
        long accepted = offered - brazier.insertTransactional(variant.toStack(offered), transaction).getCount();
        if (accepted == maxAmount) {
            updateSnapshots(transaction);
            soundPending = true;
        }
        return accepted;
    }
    @Override public long extract(ItemVariant variant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(variant, maxAmount);
        return 0;
    }
    @Override public boolean supportsExtraction() { return false; }
    @Override public java.util.Iterator<StorageView<ItemVariant>> iterator() { return java.util.Collections.emptyIterator(); }
    @Override protected Boolean createSnapshot() { return soundPending; }
    @Override protected void readSnapshot(Boolean pending) { soundPending = pending; }
    @Override protected void onFinalCommit() {
        boolean play = soundPending;
        soundPending = false;
        if (play) brazier.playAutomationSound();
    }
}
//?}
