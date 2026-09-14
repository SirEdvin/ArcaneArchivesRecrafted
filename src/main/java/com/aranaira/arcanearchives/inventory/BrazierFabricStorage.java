package com.aranaira.arcanearchives.inventory;

//? if fabric {
import com.aranaira.arcanearchives.tileentities.BrazierBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

/** Direct transactional deposits and an extraction-only paid output view. */
public final class BrazierFabricStorage extends SnapshotParticipant<Boolean> implements Storage<ItemVariant> {
    private final BrazierBlockEntity brazier;
    private final net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage output =
        new net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage() {
            @Override protected net.minecraft.world.item.ItemStack getStack() { return brazier.pull().stack(); }
            @Override protected void setStack(net.minecraft.world.item.ItemStack stack) { brazier.pull().restoreTransactional(stack); }
            @Override protected int getCapacity(ItemVariant variant) { return variant.isBlank() ? 64 : Math.min(64, variant.toStack().getMaxStackSize()); }
            @Override protected boolean canInsert(ItemVariant variant) { return false; }
            @Override protected boolean canExtract(ItemVariant variant) { return brazier.live(); }
            @Override protected void onFinalCommit() { brazier.pull().changed(); }
        };
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
        return output.extract(variant, maxAmount, transaction);
    }
    @Override public boolean supportsExtraction() { return brazier.live() && !brazier.pull().stack().isEmpty(); }
    @Override public boolean supportsInsertion() { return !brazier.pull().enabled(); }
    @Override public java.util.Iterator<StorageView<ItemVariant>> iterator() {
        return supportsExtraction() ? java.util.List.<StorageView<ItemVariant>>of(output).iterator() : java.util.Collections.emptyIterator();
    }
    @Override protected Boolean createSnapshot() { return soundPending; }
    @Override protected void readSnapshot(Boolean pending) { soundPending = pending; }
    @Override protected void onFinalCommit() {
        boolean play = soundPending;
        soundPending = false;
        if (play) brazier.playAutomationSound();
    }
}
//?}
