package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

/** The shaped quartz block is also the third storage-capacity upgrade. */
public final class StorageUpgradeBlockItem extends BlockItem {
    public StorageUpgradeBlockItem(Block block, Properties properties) { super(block, properties); }
    @Override public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof RadiantTroveBlockEntity trove) {
            if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
            if (trove.installUpgrade(context.getPlayer(), context.getHand())) return InteractionResult.CONSUME;
            return InteractionResult.FAIL;
        }
        if (context.getPlayer() != null && context.getLevel().getBlockEntity(context.getClickedPos())
                instanceof com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity tank) {
            if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
            if (tank.installUpgrade(context.getPlayer(), context.getHand())) return InteractionResult.CONSUME;
            return InteractionResult.FAIL;
        }
        return super.useOn(context);
    }
}
