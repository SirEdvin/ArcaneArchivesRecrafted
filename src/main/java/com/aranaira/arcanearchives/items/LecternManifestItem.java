package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.blocks.LecternManifest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Validate the whole footprint before vanilla consumes the item or emits placement success. */
public final class LecternManifestItem extends BlockItem {
    public LecternManifestItem(Block block, Properties properties) { super(block, properties); }

    @Override protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if (!LecternManifest.canPlaceColumn(context)) return false;
        var level = context.getLevel();
        BlockPos parent = context.getClickedPos();
        BlockState[] before = new BlockState[2];
        for (int part = 0; part < 2; part++) {
            BlockPos pos = parent.above(part);
            before[part] = level.getBlockState(pos);
            if (!level.isUnobstructed(state.setValue(LecternManifest.ACCESSOR, part != 0), pos,
                    context.getPlayer() == null ? net.minecraft.world.phys.shapes.CollisionContext.empty()
                        : net.minecraft.world.phys.shapes.CollisionContext.of(context.getPlayer()))) return false;
        }
        boolean complete = false;
        int placedParts = 0;
        try {
            for (int part = 0; part < 2; part++) {
                BlockState placed = state.setValue(LecternManifest.ACCESSOR, part != 0);
                if (!level.setBlock(parent.above(part), placed, Block.UPDATE_CLIENTS)
                        || level.getBlockState(parent.above(part)) != placed) return false;
                placedParts++;
            }
            complete = true;
        } finally {
            if (!complete) {
                boolean[] owned = new boolean[2];
                for (int part = 0; part < 2; part++) {
                    BlockState current = level.getBlockState(parent.above(part));
                    // A later write's callback may already have cleared earlier owned parts.
                    owned[part] = current == state.setValue(LecternManifest.ACCESSOR, part != 0)
                        || (part < placedParts && current.isAir());
                }
                for (int part = 0; part < 2; part++) {
                    BlockPos pos = parent.above(part);
                    if (owned[part] && level.getBlockState(pos) == state.setValue(LecternManifest.ACCESSOR, part != 0))
                        level.removeBlock(pos, false);
                }
                for (int part = 0; part < 2; part++)
                    if (owned[part] && level.getBlockState(parent.above(part)).isAir())
                        level.setBlock(parent.above(part), before[part], Block.UPDATE_CLIENTS);
            }
        }
        for (int part = 0; part < 2; part++) level.updateNeighborsAt(parent.above(part), getBlock());
        return true;
    }
}
