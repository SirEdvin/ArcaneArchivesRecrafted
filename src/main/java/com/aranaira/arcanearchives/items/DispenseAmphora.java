package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.inventory.AmphoraFluidStorage;
//? if >=1.21 {
import net.minecraft.core.dispenser.BlockSource;
//?} else {
/*import net.minecraft.core.BlockSource;
*///?}
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Original automatic pickup/place selection; never eject the linked item or alter its mode. */
public final class DispenseAmphora implements DispenseItemBehavior {
    static boolean picksUp(BlockState target) {
        return !target.canBeReplaced() || !target.getFluidState().isEmpty();
    }

    @Override public ItemStack dispense(BlockSource source, ItemStack stack) {
        //? if >=1.21 {
        var level = source.level();
        var origin = source.pos();
        var facing = source.state().getValue(DispenserBlock.FACING);
        //?} else {
        /*var level = source.getLevel();
        var origin = source.getPos();
        var facing = source.getBlockState().getValue(DispenserBlock.FACING);
        *///?}
        var pos = origin.relative(facing);
        if (!level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos)
                || level.isOutsideBuildHeight(pos)) return stack;
        boolean pickup = picksUp(level.getBlockState(pos));
        var tank = RadiantAmphoraItem.target(stack);
        boolean moved = tank != null && AmphoraFluidStorage.worldTransfer(stack, tank, level, pos,
            facing.getOpposite(), null, InteractionHand.MAIN_HAND, pickup);
        // Upstream intentionally gives pickup feedback even when nothing was collected.
        if (pickup || moved) {
            level.levelEvent(1000, origin, 0);
            level.levelEvent(2000, origin, facing.get3DDataValue());
        }
        return stack;
    }
}
