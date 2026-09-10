package com.aranaira.arcanearchives.inventory;

//? if fabric {
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.world.item.ItemStack;

public final class TroveFabricStorage extends SingleStackStorage {
    private final RadiantTroveBlockEntity trove;
    private final net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant<ItemStack> lockSnapshots =
        new net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant<>() {
            @Override protected ItemStack createSnapshot() { return trove.lockReference(); }
            @Override protected void readSnapshot(ItemStack stack) { trove.restoreLockReference(stack); }
        };
    public TroveFabricStorage(RadiantTroveBlockEntity trove) { this.trove = trove; }
    @Override public long insert(ItemVariant variant, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
        net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions.notBlankNotNegative(variant, maxAmount);
        if (!canInsert(variant)) return 0;
        lockSnapshots.updateSnapshots(transaction);
        long inserted = super.insert(variant, maxAmount, transaction);
        return trove.optionals().isVoiding() ? maxAmount : inserted;
    }
    @Override public long extract(ItemVariant variant, long maxAmount, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
        net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions.notBlankNotNegative(variant, maxAmount);
        lockSnapshots.updateSnapshots(transaction);
        return super.extract(variant, maxAmount, transaction);
    }
    @Override protected ItemStack getStack() { return trove.inventory().getStackInSlot(0); }
    @Override protected void setStack(ItemStack stack) { trove.setTransactionalStack(stack); }
    @Override protected int getCapacity(ItemVariant variant) {
        return trove.inventory().getStackLimit(0, variant.isBlank() ? ItemStack.EMPTY : variant.toStack());
    }
    @Override protected boolean canInsert(ItemVariant variant) { return trove.isLiveServerStorage() && trove.acceptsItem(variant.toStack()); }
    @Override protected boolean canExtract(ItemVariant variant) { return trove.isLiveServerStorage(); }
    @Override protected void onFinalCommit() { trove.storageChanged(); }
}
//?}
