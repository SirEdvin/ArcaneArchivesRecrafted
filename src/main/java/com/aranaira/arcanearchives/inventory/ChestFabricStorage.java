package com.aranaira.arcanearchives.inventory;

//? if fabric {
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.world.item.ItemStack;

public final class ChestFabricStorage extends CombinedStorage<ItemVariant, SingleStackStorage> implements SlottedStorage<ItemVariant> {
    public ChestFabricStorage(RadiantChestBlockEntity chest) { super(slots(chest)); }

    private static List<SingleStackStorage> slots(RadiantChestBlockEntity chest) {
        List<SingleStackStorage> slots = new ArrayList<>();
        for (int index = 0; index < chest.inventory().getSlots(); index++) {
            final int slot = index;
            slots.add(new SingleStackStorage() {
                @Override protected ItemStack getStack() { return chest.inventory().getStackInSlot(slot).copy(); }
                @Override protected void setStack(ItemStack stack) { chest.setTransactionalStack(slot, stack); }
                @Override protected int getCapacity(ItemVariant variant) {
                    return variant.isBlank() ? chest.inventory().getSlotLimit(slot) : chest.inventory().getStackLimit(slot, variant.toStack());
                }
                @Override protected boolean canInsert(ItemVariant variant) { return chest.isLiveServerStorage(); }
                @Override protected boolean canExtract(ItemVariant variant) { return chest.isLiveServerStorage(); }
                @Override protected void onFinalCommit() { chest.storageChanged(); }
            });
        }
        return slots;
    }

    @Override public int getSlotCount() { return parts.size(); }
    @Override public SingleSlotStorage<ItemVariant> getSlot(int slot) { return parts.get(slot); }
}
//?}
