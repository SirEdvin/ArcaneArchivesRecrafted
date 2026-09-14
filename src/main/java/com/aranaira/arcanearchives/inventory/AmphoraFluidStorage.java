package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.items.RadiantAmphoraItem;
import com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
//? if fabric {

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.BucketPickup;
//?} else if forge {
/*import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

*///?} else {
/*import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

*///?}

/** Explicit live operations: copied item containers must not proxy remote Tank mutation. */
public final class AmphoraFluidStorage {
    private AmphoraFluidStorage() {}

    private static void recoverPickup(ItemStack bucket, Player player, ServerLevel level, BlockPos pos) {
        if (player != null) {
            bucket = insertRecoveredPickup(player.getInventory(), bucket);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
        if (!bucket.isEmpty()) net.minecraft.world.level.block.Block.popResource(level, pos, bucket);
    }

    // Inventory.add may erase creative overflow. Native slots retain every unaccepted item.
    static ItemStack insertRecoveredPickup(net.minecraft.world.entity.player.Inventory inventory, ItemStack remainder) {
        for (boolean empty : new boolean[]{false, true}) {
            for (int index = 0; index < inventory.items.size(); index++) {
                if (remainder.isEmpty()) return remainder;
                if (inventory.getItem(index).isEmpty() == empty)
                    remainder = new net.minecraft.world.inventory.Slot(inventory, index, 0, 0).safeInsert(remainder);
            }
        }
        return remainder;
    }

    public static boolean interactWithTank(Player player, InteractionHand hand, RadiantTankBlockEntity physical) {
        var remote = RadiantAmphoraItem.target(player.getItemInHand(hand));
        if (!physical.canUse(player) || remote == null || remote == physical
                || !remote.getLevel().mayInteract(player, remote.getBlockPos())) return false;
        //? if fabric {
        return StorageUtil.move(physical.inventory(), remote.inventory(), fluid -> true, Long.MAX_VALUE, null) > 0
            || StorageUtil.move(remote.inventory(), physical.inventory(), fluid -> true, Long.MAX_VALUE, null) > 0;
        //?} else {
        /*return !FluidUtil.tryFluidTransfer(remote.inventory(), physical.inventory(), Integer.MAX_VALUE, true).isEmpty()
            || !FluidUtil.tryFluidTransfer(physical.inventory(), remote.inventory(), Integer.MAX_VALUE, true).isEmpty();
        *///?}
    }

    public static boolean worldTransfer(ItemStack stack, RadiantTankBlockEntity tank, ServerLevel level,
            BlockPos pos, Direction side, Player player, InteractionHand hand, boolean filling) {
        if (!tank.isLiveServerStorage() || !level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos)
                || level.isOutsideBuildHeight(pos)) return false;
        var storage = tank.inventory();
        //? if fabric {
        if (filling) {
            var state = level.getBlockState(pos);
            if (state.getBlock() instanceof BucketPickup pickup && state.getFluidState().isSource()) {
                FluidVariant fluid = FluidVariant.of(state.getFluidState().getType());
                try (Transaction transaction = Transaction.openOuter()) {
                    if (storage.insert(fluid, FluidConstants.BUCKET, transaction) != FluidConstants.BUCKET) return false;
                    //? if >=1.21 {
                    ItemStack bucket = pickup.pickupBlock(player, level, pos, state);
                    //?} else {
                    /*ItemStack bucket = pickup.pickupBlock(level, pos, state);
                    *///?}
                    if (bucket.isEmpty()) return false;
                    if (bucket.getCount() != 1 || !com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(
                            bucket, new ItemStack(fluid.getFluid().getBucket()))) {
                        // Preserve an unexpected modded pickup instead of inventing its fluid identity.
                        recoverPickup(bucket, player, level, pos);
                        return false;
                    }
                    transaction.commit();
                }
                pickup.getPickupSound().ifPresent(sound -> level.playSound(null, pos, sound,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1F, 1F));
                level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.FLUID_PICKUP, pos);
                return true;
            }
            var source = FluidStorage.SIDED.find(level, pos, side);
            return source != storage && StorageUtil.move(source, storage, fluid -> true, Long.MAX_VALUE, null) > 0;
        }
        FluidVariant fluid = storage.getResource();
        if (fluid.isBlank() || !(fluid.getFluid().getBucket() instanceof BucketItem bucket)) return false;
        // Only variants represented by this native bucket can be placed without discarding metadata.
        if (!fluid.equals(FluidVariant.of(fluid.getFluid()))) return false;
        try (Transaction transaction = Transaction.openOuter()) {
            if (storage.extract(fluid, FluidConstants.BUCKET, transaction) != FluidConstants.BUCKET) return false;
            if (!bucket.emptyContents(player, level, pos, null)) return false;
            transaction.commit();
            return true;
        }
        //?} else {
        /*if (filling) {
            var state = level.getBlockState(pos);
            IFluidHandler source;
            if (state.getBlock() instanceof net.minecraft.world.level.block.BucketPickup pickup) {
                if (!state.getFluidState().isSource()) return false;
                FluidStack planned = new FluidStack(state.getFluidState().getType(), 1000);
                if (storage.fill(planned, IFluidHandler.FluidAction.SIMULATE) != 1000) return false;
                //? if forge {
                /^ItemStack bucket = pickup.pickupBlock(level, pos, state);
                ^///?} else {
                ItemStack bucket = pickup.pickupBlock(player, level, pos, state);
                //?}
                if (bucket.isEmpty()) return false;
                FluidStack actual = FluidUtil.getFluidContained(bucket).orElse(FluidStack.EMPTY);
                if (bucket.getCount() != 1 || actual.isEmpty()
                        || storage.fill(actual, IFluidHandler.FluidAction.SIMULATE) != actual.getAmount()) {
                    recoverPickup(bucket, player, level, pos);
                    return false;
                }
                storage.fill(actual, IFluidHandler.FluidAction.EXECUTE);
                pickup.getPickupSound().ifPresent(sound -> level.playSound(null, pos, sound,
                    net.minecraft.sounds.SoundSource.BLOCKS, 1F, 1F));
                level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.FLUID_PICKUP, pos);
                return true;
            } else source = FluidUtil.getFluidHandler(level, pos, side).orElse(null);
            boolean transferred = source != null && source != storage && !FluidUtil.tryFluidTransfer(storage, source, Integer.MAX_VALUE, true).isEmpty();
            if (transferred) {
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BUCKET_FILL, net.minecraft.sounds.SoundSource.BLOCKS, 1F, 1F);
                level.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.FLUID_PICKUP, pos);
            }
            return transferred;
        }
        FluidStack fluid = storage.drain(1000, IFluidHandler.FluidAction.SIMULATE);
        return fluid.getAmount() == 1000 && FluidUtil.tryPlaceFluid(player, level, hand, pos, storage, fluid);
        *///?}
    }
}
