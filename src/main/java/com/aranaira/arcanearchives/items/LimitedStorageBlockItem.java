package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.data.StoragePlacementSaveData;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

/** One placement guard shared by chest, portable trove and portable tank items. */
public class LimitedStorageBlockItem extends BlockItem {
    public LimitedStorageBlockItem(Block block, Properties properties) { super(block, properties); }
    @Override public InteractionResult place(BlockPlaceContext context) {
        if (!StoragePlacementSaveData.mayPlace(context.getPlayer(), getBlock())) return InteractionResult.FAIL;
        return super.place(context);
    }
}
