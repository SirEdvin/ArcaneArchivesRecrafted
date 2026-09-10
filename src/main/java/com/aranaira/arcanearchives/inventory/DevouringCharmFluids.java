package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.items.RadiantAmphoraItem;
import net.minecraft.world.item.ItemStack;

/** Only the live disposal slot calls drain; matching/transfer simulation must never execute it. */
public final class DevouringCharmFluids {
    private DevouringCharmFluids() {}
    public static boolean accepts(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof RadiantAmphoraItem) return false;
        if (stack.getItem() instanceof com.aranaira.arcanearchives.items.ParchtearItem) return true;
        ItemStack copy = stack.copy();
        copy.setCount(1);
        //? if fabric {
        var inventory = new net.minecraft.world.SimpleContainer(copy);
        var slot = net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage.of(inventory, null).getSlot(0);
        return net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext.ofSingleSlot(slot)
            .find(net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.ITEM) != null;
        //?} else if forge {
        /*return copy.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
        *///?} else {
        /*return copy.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM) != null;
        *///?}
    }
    public static ItemStack drain(ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() != 1 || stack.getItem() instanceof RadiantAmphoraItem) return stack;
        ItemStack copy = stack.copy();
        //? if fabric {
        var inventory = new net.minecraft.world.SimpleContainer(copy);
        var slot = net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage.of(inventory, null).getSlot(0);
        var storage = net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext.ofSingleSlot(slot)
            .find(net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.ITEM);
        if (storage == null) return stack;
        var fluids = new java.util.ArrayList<net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant>();
        for (var view : storage) if (!view.isResourceBlank()) fluids.add(view.getResource());
        try (var transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
            for (var fluid : fluids) storage.extract(fluid, Long.MAX_VALUE, transaction);
            transaction.commit();
        }
        return inventory.getItem(0).copy();
        //?} else if forge {
        /*var storage = copy.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (storage == null) return stack;
        var fluids = new java.util.ArrayList<net.minecraftforge.fluids.FluidStack>();
        for (int tank = 0; tank < storage.getTanks(); tank++) fluids.add(storage.getFluidInTank(tank).copy());
        for (var fluid : fluids) if (!fluid.isEmpty()) storage.drain(fluid, net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        return storage.getContainer().copy();
        *///?} else {
        /*var storage = copy.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        if (storage == null) return stack;
        var fluids = new java.util.ArrayList<net.neoforged.neoforge.fluids.FluidStack>();
        for (int tank = 0; tank < storage.getTanks(); tank++) fluids.add(storage.getFluidInTank(tank).copy());
        for (var fluid : fluids) if (!fluid.isEmpty()) storage.drain(fluid, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        return storage.getContainer().copy();
        *///?}
    }
}
