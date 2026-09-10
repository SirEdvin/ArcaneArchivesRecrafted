package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
//? if fabric {
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
//?} else if forge {
/*import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
*///?} else {
/*import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
*///?}

/** Item-local transfers only. Decode and encode before publishing any change. */
public final class TankItemFluidStorage
    //? if fabric {
    implements SingleSlotStorage<FluidVariant>
    //?} else {
    /*implements IFluidHandlerItem
    *///?}
    //? if forge {
    /*, net.minecraftforge.common.capabilities.ICapabilityProvider
    *///?}
{
    //? if forge {
    /*private final net.minecraftforge.common.util.LazyOptional<IFluidHandlerItem> capability =
        net.minecraftforge.common.util.LazyOptional.of(() -> this);
    @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(
            net.minecraftforge.common.capabilities.Capability<T> requested, net.minecraft.core.Direction side) {
        return requested == net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER_ITEM
            ? capability.cast() : net.minecraftforge.common.util.LazyOptional.empty();
    }
    *///?}
    // Item capability contexts provide no Level. Unknown registry-dependent data fails closed.
    private static HolderLookup.Provider registries() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }
    //? if fabric {
    private final ContainerItemContext context;
    public TankItemFluidStorage(ContainerItemContext context) { this.context = context; }
    private ItemStack container() {
        var item = context.getItemVariant();
        return item.isOf(ContentRegistry.RADIANT_TANK_ITEM.get()) && context.getAmount() == 1
            ? item.toStack() : ItemStack.EMPTY;
    }
    //?} else {
    /*private final ItemStack container;
    public TankItemFluidStorage(ItemStack container) { this.container = container; }
    private ItemStack container() { return container; }
    @Override public ItemStack getContainer() { return container; }
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
    private static RadiantTankBlockEntity read(ItemStack stack) {
        if (!stack.is(ContentRegistry.RADIANT_TANK_ITEM.get()) || stack.getCount() != 1)
            throw new IllegalArgumentException("Tank transfers require one item");
        var tank = new RadiantTankBlockEntity(BlockPos.ZERO, ContentRegistry.RADIANT_TANK.get().defaultBlockState());
        //? if >=1.21 {
        tank.loadWithComponents(data(stack), registries());
        //?} else {
        /*tank.load(data(stack));
        *///?}
        return tank;
    }
    private static ItemStack updated(ItemStack source, RadiantTankBlockEntity tank) {
        CompoundTag tag = data(source);
        // New items need the same complete storage schema as packed blocks.
        if (!tag.contains("fluid_storage")) {
            //? if >=1.21 {
            tag = tank.getUpdateTag(registries());
            //?} else {
            /*tag = tank.getUpdateTag();
            *///?}
            tag.merge(data(source));
        }
        tag.put("fluid_storage", tank.inventory().writeState(registries()));
        ItemStack result = source.copy();
        BlockItem.setBlockEntityData(result, ContentRegistry.RADIANT_TANK_ENTITY.get(), tag);
        return result;
    }
    private static long capacity(RadiantTankBlockEntity tank) {
        return RadiantTankStorage.capacityFor(tank.upgrades().getUpgradesCount());
    }
    //? if fabric {
    @Override public boolean isResourceBlank() { return getResource().isBlank(); }
    @Override public FluidVariant getResource() {
        try { return read(container()).inventory().getResource(); }
        catch (IllegalArgumentException exception) { return FluidVariant.blank(); }
    }
    @Override public long getAmount() {
        try { return read(container()).inventory().storedAmount(); }
        catch (IllegalArgumentException exception) { return 0; }
    }
    @Override public long getCapacity() {
        try { return capacity(read(container())); }
        catch (IllegalArgumentException exception) { return 0; }
    }
    @Override public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        return transfer(resource, maxAmount, transaction, true);
    }
    @Override public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        return transfer(resource, maxAmount, transaction, false);
    }
    private long transfer(FluidVariant resource, long maximum, TransactionContext transaction, boolean insert) {
        StoragePreconditions.notBlankNotNegative(resource, maximum);
        if (maximum == 0) return 0;
        try {
            ItemStack source = container();
            var tank = read(source);
            var fluid = tank.inventory();
            if ((!insert || fluid.amount > 0) && !fluid.variant.equals(resource)) return 0;
            long moved = Math.min(maximum, insert ? capacity(tank) - fluid.amount : fluid.amount);
            if (moved == 0) return 0;
            fluid.amount += insert ? moved : -moved;
            fluid.variant = fluid.amount == 0 ? FluidVariant.blank() : resource;
            ItemStack result = updated(source, tank);
            return context.exchange(ItemVariant.of(result), 1, transaction) == 1 ? moved : 0;
        } catch (IllegalArgumentException exception) { return 0; }
    }
    //?} else {
    /*@Override public int getTanks() { return 1; }
    @Override public FluidStack getFluidInTank(int index) {
        if (index != 0) throw new IndexOutOfBoundsException(index);
        try { return read(container()).inventory().getFluid().copy(); }
        catch (IllegalArgumentException exception) { return FluidStack.EMPTY; }
    }
    @Override public int getTankCapacity(int index) {
        if (index != 0) throw new IndexOutOfBoundsException(index);
        try { return Math.toIntExact(capacity(read(container()))); }
        catch (IllegalArgumentException exception) { return 0; }
    }
    @Override public boolean isFluidValid(int index, FluidStack resource) {
        return index == 0 && !resource.isEmpty();
    }
    private void publish(ItemStack replacement) {
        BlockItem.setBlockEntityData(container, ContentRegistry.RADIANT_TANK_ENTITY.get(), data(replacement));
    }
    @Override public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) return 0;
        try {
            var tank = read(container());
            FluidStack stored = tank.inventory().getFluid();
            if (!stored.isEmpty() && !stored.isFluidEqual(resource)) return 0;
            int moved = Math.min(resource.getAmount(), Math.toIntExact(capacity(tank) - stored.getAmount()));
            if (moved <= 0) return 0;
            FluidStack result = resource.copy();
            result.setAmount(stored.getAmount() + moved);
            tank.inventory().setFluid(result);
            ItemStack replacement = updated(container, tank);
            if (action.execute()) publish(replacement);
            return moved;
        } catch (IllegalArgumentException exception) { return 0; }
    }
    @Override public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !getFluidInTank(0).isFluidEqual(resource)) return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }
    @Override public FluidStack drain(int maximum, FluidAction action) {
        if (maximum <= 0) return FluidStack.EMPTY;
        try {
            var tank = read(container());
            FluidStack stored = tank.inventory().getFluid().copy();
            int moved = Math.min(maximum, stored.getAmount());
            if (moved == 0) return FluidStack.EMPTY;
            FluidStack result = stored.copy();
            result.setAmount(moved);
            stored.shrink(moved);
            tank.inventory().setFluid(stored);
            ItemStack replacement = updated(container, tank);
            if (action.execute()) publish(replacement);
            return result;
        } catch (IllegalArgumentException exception) { return FluidStack.EMPTY; }
    }
    *///?}
}
