package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.blocks.MatrixDistillate;
import com.aranaira.arcanearchives.tileentities.MatrixPartBlockEntity;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** One conserved nine-position placement; all parts share one freshly generated structure identity. */
public final class MatrixDistillateItem extends BlockItem {
    public MatrixDistillateItem(Block block, Properties properties) { super(block, properties); }

    @Override protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack stack, BlockState state) {
        // Identity-only parts belong to this placement. Importing item NBT here can throw after
        // the footprint commits but before vanilla consumes its item or calls setPlacedBy.
        return false;
    }

    private static CompoundTag entityData(Level level, BlockPos pos) {
        var entity = level.getBlockEntity(pos);
        if (entity == null) return null;
        //? if >=1.21 {
        return entity.saveWithFullMetadata(level.registryAccess());
        //?} else {
        /*return entity.saveWithFullMetadata();
        *///?}
    }
    @Override protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if (!MatrixDistillate.canPlace(context, state)) return false;
        var level = context.getLevel();
        var positions = MatrixDistillate.footprint(context.getClickedPos(), state);
        BlockState[] before = new BlockState[9];
        CompoundTag[] data = new CompoundTag[9];
        for (int part = 0; part < 9; part++) {
            BlockPos pos = positions.get(part);
            before[part] = level.getBlockState(pos);
            data[part] = entityData(level, pos);
            if (!level.isUnobstructed(state.setValue(MatrixDistillate.PART, part), pos,
                    context.getPlayer() == null ? net.minecraft.world.phys.shapes.CollisionContext.empty()
                        : net.minecraft.world.phys.shapes.CollisionContext.of(context.getPlayer()))) return false;
        }
        UUID identity = UUID.randomUUID();
        boolean complete = false;
        int placedParts = 0;
        try {
            for (int part = 0; part < 9; part++) {
                BlockPos pos = positions.get(part);
                BlockState placed = state.setValue(MatrixDistillate.PART, part);
                if (level.getBlockState(pos) != before[part] || !java.util.Objects.equals(data[part], entityData(level, pos))) return false;
                if (!level.setBlock(pos, placed, Block.UPDATE_CLIENTS) || level.getBlockState(pos) != placed) return false;
                placedParts++;
                if (!(level.getBlockEntity(pos) instanceof MatrixPartBlockEntity entity)) return false;
                entity.identify(identity);
            }
            complete = true;
        } finally {
            if (!complete) {
                boolean[] owned = new boolean[9];
                for (int part = 0; part < 9; part++) {
                    BlockPos pos = positions.get(part);
                    owned[part] = level.getBlockState(pos) == state.setValue(MatrixDistillate.PART, part)
                        && level.getBlockEntity(pos) instanceof MatrixPartBlockEntity entity && identity.equals(entity.identity())
                        || part < placedParts && level.getBlockState(pos).isAir();
                }
                for (int part = 0; part < 9; part++) {
                    BlockPos pos = positions.get(part);
                    if (owned[part] && level.getBlockEntity(pos) instanceof MatrixPartBlockEntity entity && identity.equals(entity.identity()))
                        level.removeBlock(pos, false);
                }
                for (int part = 0; part < 9; part++) {
                    BlockPos pos = positions.get(part);
                    if (owned[part] && level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, before[part], Block.UPDATE_CLIENTS);
                        if (data[part] != null && level.getBlockEntity(pos) != null) {
                            //? if >=1.21 {
                            level.getBlockEntity(pos).loadWithComponents(data[part], level.registryAccess());
                            //?} else {
                            /*level.getBlockEntity(pos).load(data[part]);
                            *///?}
                            level.getBlockEntity(pos).setChanged();
                        }
                    }
                }
            }
        }
        for (BlockPos pos : positions) level.updateNeighborsAt(pos, getBlock());
        return true;
    }
}
