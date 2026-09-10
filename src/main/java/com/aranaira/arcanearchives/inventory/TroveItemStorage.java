package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
//? if fabric {
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
//?}

/** Portable storage only: prepare a complete replacement before publishing it. */
public final class TroveItemStorage
    //? if fabric {
    implements SingleSlotStorage<ItemVariant>
    //?} else if forge {
    /*implements net.minecraftforge.items.IItemHandler, net.minecraftforge.common.capabilities.ICapabilityProvider
    *///?} else {
    /*implements net.neoforged.neoforge.items.IItemHandler
    *///?}
{
    private static HolderLookup.Provider registries() { return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY); }
    //? if fabric {
    /** Use ContainerItemContext.ofSingleSlot with InventoryStorage for vanilla inventory slots. */
    public static final net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup<
            net.fabricmc.fabric.api.transfer.v1.storage.Storage<ItemVariant>, ContainerItemContext> ITEM =
        //? if >=1.21 {
        net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.ITEM;
        //?} else {
        /*net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup.get(
            net.minecraft.resources.ResourceLocation.tryParse("arcanearchives:item_storage"),
            net.fabricmc.fabric.api.transfer.v1.storage.Storage.asClass(), ContainerItemContext.class);
        *///?}
    private final ContainerItemContext context;
    public TroveItemStorage(ContainerItemContext context) { this.context = context; }
    private ItemStack container() {
        ItemVariant item = context.getItemVariant();
        return item.isOf(ContentRegistry.RADIANT_TROVE_ITEM.get()) && context.getAmount() == 1
            ? item.toStack() : ItemStack.EMPTY;
    }
    //?} else {
    /*private final ItemStack container;
    public TroveItemStorage(ItemStack container) { this.container = container; }
    private ItemStack container() { return container; }
    *///?}
    //? if forge {
    /*private final net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler> capability =
        net.minecraftforge.common.util.LazyOptional.of(() -> this);
    @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(
            net.minecraftforge.common.capabilities.Capability<T> requested, net.minecraft.core.Direction side) {
        return requested == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER
            ? capability.cast() : net.minecraftforge.common.util.LazyOptional.empty();
    }
    *///?}
    private static CompoundTag data(ItemStack stack) {
        //? if >=1.21 {
        var component = stack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
        return component == null ? new CompoundTag() : component.copyTag();
        //?} else {
        /*CompoundTag tag = BlockItem.getBlockEntityData(stack);
        return tag == null ? new CompoundTag() : tag.copy();
        *///?}
    }
    private static RadiantTroveBlockEntity read(ItemStack stack) {
        if (!stack.is(ContentRegistry.RADIANT_TROVE_ITEM.get()) || stack.getCount() != 1)
            throw new IllegalArgumentException("Trove transfers require one item");
        var trove = new RadiantTroveBlockEntity(BlockPos.ZERO, ContentRegistry.RADIANT_TROVE.get().defaultBlockState());
        //? if >=1.21 {
        trove.loadWithComponents(data(stack), registries());
        //?} else {
        /*trove.load(data(stack));
        *///?}
        return trove;
    }
    private static ItemStack updated(ItemStack source, RadiantTroveBlockEntity trove) {
        CompoundTag tag = data(source);
        if (!tag.contains("inventory")) {
            //? if >=1.21 {
            tag = trove.getUpdateTag(registries());
            //?} else {
            /*tag = trove.getUpdateTag();
            *///?}
            tag.merge(data(source));
        }
        tag.put("inventory", trove.inventory().serializeNBT(registries()));
        tag.put("lock_reference", referenceTag(trove));
        ItemStack result = source.copy();
        BlockItem.setBlockEntityData(result, ContentRegistry.RADIANT_TROVE_ENTITY.get(), tag);
        return result;
    }
    private static CompoundTag referenceTag(RadiantTroveBlockEntity trove) {
        var reference = new ExtendedItemStackHandler(1);
        reference.setStackInSlot(0, trove.lockReference());
        return reference.serializeNBT(registries());
    }
    private static long change(RadiantTroveBlockEntity trove, ItemStack resource, long maximum, boolean insert) {
        ItemStack stored = trove.inventory().getStackInSlot(0);
        if (insert ? !trove.acceptsItem(resource) : !ExtendedItemStackHandler.sameItemAndData(stored, resource)) return 0;
        long moved = Math.min(maximum, insert
            ? RadiantTroveBlockEntity.capacity(resource, trove.upgrades().getUpgradesCount()) - stored.getCount()
            : Math.min(stored.getCount(), stored.getMaxStackSize()));
        if (moved > 0) {
            ItemStack replacement = resource.copy();
            replacement.setCount(Math.toIntExact(stored.getCount() + (insert ? moved : -moved)));
            trove.setTransactionalStack(replacement);
        }
        return insert && trove.optionals().isVoiding() ? maximum : moved;
    }
    //? if fabric {
    @Override public boolean isResourceBlank() { return getResource().isBlank(); }
    @Override public ItemVariant getResource() {
        try { return ItemVariant.of(read(container()).inventory().getStackInSlot(0)); }
        catch (IllegalArgumentException exception) { return ItemVariant.blank(); }
    }
    @Override public long getAmount() {
        try { return read(container()).inventory().getStackInSlot(0).getCount(); }
        catch (IllegalArgumentException exception) { return 0; }
    }
    @Override public long getCapacity() {
        try { var trove = read(container()); return trove.inventory().getStackLimit(0, trove.inventory().getStackInSlot(0)); }
        catch (IllegalArgumentException exception) { return 0; }
    }
    @Override public long insert(ItemVariant resource, long maximum, TransactionContext transaction) {
        return transfer(resource, maximum, transaction, true);
    }
    @Override public long extract(ItemVariant resource, long maximum, TransactionContext transaction) {
        return transfer(resource, maximum, transaction, false);
    }
    private long transfer(ItemVariant resource, long maximum, TransactionContext transaction, boolean insert) {
        StoragePreconditions.notBlankNotNegative(resource, maximum);
        if (maximum == 0) return 0;
        try {
            ItemStack source = container();
            var trove = read(source);
            long moved = change(trove, resource.toStack(), maximum, insert);
            if (moved == 0) return 0;
            return context.exchange(ItemVariant.of(updated(source, trove)), 1, transaction) == 1 ? moved : 0;
        } catch (IllegalArgumentException exception) { return 0; }
    }
    //?} else {
    /*@Override public int getSlots() { return 2; }
    @Override public ItemStack getStackInSlot(int slot) {
        java.util.Objects.checkIndex(slot, 2);
        if (slot == 1) return ItemStack.EMPTY;
        try { return read(container()).inventory().getStackInSlot(0); }
        catch (IllegalArgumentException exception) { return ItemStack.EMPTY; }
    }
    @Override public int getSlotLimit(int slot) {
        java.util.Objects.checkIndex(slot, 2);
        try { var trove = read(container()); return trove.inventory().getStackLimit(0, trove.inventory().getStackInSlot(0)); }
        catch (IllegalArgumentException exception) { return 0; }
    }
    @Override public boolean isItemValid(int slot, ItemStack stack) {
        java.util.Objects.checkIndex(slot, 2);
        try { return !stack.isEmpty() && read(container()).acceptsItem(stack); }
        catch (IllegalArgumentException exception) { return false; }
    }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        java.util.Objects.checkIndex(slot, 2);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        try {
            var trove = read(container());
            int moved = Math.toIntExact(change(trove, stack, stack.getCount(), true));
            if (moved == 0) return stack.copy();
            ItemStack replacement = updated(container(), trove);
            if (!simulate) BlockItem.setBlockEntityData(container, ContentRegistry.RADIANT_TROVE_ENTITY.get(), data(replacement));
            ItemStack remainder = stack.copy();
            remainder.shrink(moved);
            return remainder;
        } catch (IllegalArgumentException exception) { return stack.copy(); }
    }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        java.util.Objects.checkIndex(slot, 2);
        if (amount < 0) throw new IllegalArgumentException("Negative extraction");
        if (amount == 0) return ItemStack.EMPTY;
        try {
            var trove = read(container());
            ItemStack result = trove.inventory().getStackInSlot(0);
            if (result.isEmpty()) return ItemStack.EMPTY;
            int moved = Math.toIntExact(change(trove, result, amount, false));
            if (moved == 0) return ItemStack.EMPTY;
            ItemStack replacement = updated(container(), trove);
            if (!simulate) BlockItem.setBlockEntityData(container, ContentRegistry.RADIANT_TROVE_ENTITY.get(), data(replacement));
            result.setCount(moved);
            return result;
        } catch (IllegalArgumentException exception) { return ItemStack.EMPTY; }
    }
    *///?}
}
