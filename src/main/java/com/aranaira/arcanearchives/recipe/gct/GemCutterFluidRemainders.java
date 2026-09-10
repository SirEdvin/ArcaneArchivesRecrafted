package com.aranaira.arcanearchives.recipe.gct;

import java.util.Optional;
import net.minecraft.world.item.ItemStack;

/** Prepare a single-container return on detached state; never mutate a player or device here. */
public final class GemCutterFluidRemainders {
    private GemCutterFluidRemainders() {}

    /** Empty optional means unsupported, not permission to discard the consumed container. */
    public static Optional<ItemStack> prepare(ItemStack consumed) {
        if (consumed.isEmpty() || consumed.getCount() != 1
                || consumed.getItem() instanceof net.minecraft.world.item.FlintAndSteelItem) return Optional.empty();
        ItemStack copy = consumed.copy();
        //? if fabric {
        var inventory = new net.minecraft.world.SimpleContainer(copy);
        var slot = net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage.of(inventory, null).getSlot(0);
        var context = net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext.ofSingleSlot(slot);
        var storage = context.find(net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.ITEM);
        if (storage == null) return Optional.empty();
        var views = storage.iterator();
        if (!views.hasNext()) return Optional.empty();
        var view = views.next();
        if (views.hasNext() || view.isResourceBlank() || view.getAmount() <= 0) return Optional.empty();
        long amount = view.getAmount();
        try (var transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction.openOuter()) {
            if (storage.extract(view.getResource(), amount, transaction) != amount) return Optional.empty();
            transaction.commit();
        }
        ItemStack result = inventory.getItem(0).copy();
        //?} else if forge {
        /*var handler = copy.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        if (handler == null || handler.getTanks() != 1) return Optional.empty();
        var fluid = handler.getFluidInTank(0).copy();
        if (fluid.isEmpty()) return Optional.empty();
        var drained = handler.drain(fluid.copy(), net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        if (drained.getAmount() != fluid.getAmount() || !drained.isFluidEqual(fluid)) return Optional.empty();
        ItemStack result = handler.getContainer().copy();
        *///?} else {
        /*var handler = copy.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        if (handler == null || handler.getTanks() != 1) return Optional.empty();
        var fluid = handler.getFluidInTank(0).copy();
        if (fluid.isEmpty()) return Optional.empty();
        var drained = handler.drain(fluid.copy(), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        if (drained.getAmount() != fluid.getAmount()
                || !net.neoforged.neoforge.fluids.FluidStack.isSameFluidSameComponents(drained, fluid)) return Optional.empty();
        ItemStack result = handler.getContainer().copy();
        *///?}
        if (!result.isEmpty() && result.getCount() > Math.min(64, result.getMaxStackSize())) return Optional.empty();
        return Optional.of(result);
    }
}
