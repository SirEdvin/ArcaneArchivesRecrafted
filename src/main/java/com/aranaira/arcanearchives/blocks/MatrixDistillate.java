package com.aranaira.arcanearchives.blocks;

import com.aranaira.arcanearchives.tileentities.MatrixPartBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Registered prototype: nine occupied positions and original presentation, no processing or fluid storage. */
public final class MatrixDistillate extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 8);
    private final java.util.Set<java.util.UUID> removing = new java.util.HashSet<>();

    public MatrixDistillate() {
        this(Properties.of().mapColor(net.minecraft.world.level.material.MapColor.NONE).strength(0F)
            .noOcclusion().lightLevel(state -> 15).pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK));
    }
    public MatrixDistillate(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PART, 0));
    }
    //? if >=1.21 {
    public static final com.mojang.serialization.MapCodec<MatrixDistillate> CODEC = simpleCodec(MatrixDistillate::new);
    @Override public com.mojang.serialization.MapCodec<MatrixDistillate> codec() { return CODEC; }
    //?}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, PART); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MatrixPartBlockEntity(pos, state); }

    public static BlockPos offset(BlockState state, int part) {
        int cell = part == 0 ? 1 : part == 1 ? 0 : part;
        Direction facing = state.getValue(FACING);
        Direction axis = facing.getAxis().isHorizontal() ? facing.getOpposite() : Direction.WEST;
        return BlockPos.ZERO.relative(axis, cell % 3 - 1).above(cell / 3);
    }
    public static List<BlockPos> footprint(BlockPos parent, BlockState state) {
        List<BlockPos> result = new ArrayList<>(9);
        for (int part = 0; part < 9; part++) result.add(parent.offset(offset(state, part)));
        return result;
    }
    public static boolean canPlace(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        var player = context.getPlayer();
        for (BlockPos pos : footprint(context.getClickedPos(), state)) {
            if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos) || !level.hasChunkAt(pos)
                || (player != null && (!level.mayInteract(player, pos)
                    || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())))) return false;
            if (!level.getBlockState(pos).canBeReplaced(BlockPlaceContext.at(context, pos, context.getClickedFace()))) return false;
        }
        return true;
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getCounterClockWise());
        return canPlace(context, state) ? state : null;
    }
    @Override public RenderShape getRenderShape(BlockState state) { return state.getValue(PART) == 0 ? RenderShape.MODEL : RenderShape.INVISIBLE; }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return state.setValue(FACING, mirror.mirror(state.getValue(FACING))); }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        if (level.isClientSide) return;
        BlockState original = defaultBlockState().setValue(FACING,
            (placer == null ? Direction.NORTH : placer.getDirection()).getCounterClockWise());
        // Item block-state/BE data must not re-identify or reorient only the root after footprint placement.
        BlockPos sibling = pos.offset(offset(original, 1));
        if (level.hasChunkAt(sibling) && level.getBlockEntity(sibling) instanceof MatrixPartBlockEntity part
                && level.getBlockEntity(pos) instanceof MatrixPartBlockEntity root) {
            root.identify(part.identity());
            level.setBlock(pos, original, Block.UPDATE_CLIENTS);
        }
    }
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level instanceof ServerLevel server) {
            boolean restoring = false;
            //? if forge || neoforge {
            /*restoring = level.restoringBlockSnapshots;
            *///?}
            if (!restoring && level.getBlockEntity(pos) instanceof MatrixPartBlockEntity part) {
                BlockPos parent = pos.subtract(offset(state, state.getValue(PART)));
                var identity = part.identity();
                if (removing.add(identity)) {
                    try {
                        for (BlockPos sibling : footprint(parent, state)) {
                            // Loaded-footprint scope (0124): never request missing chunks or queue retries.
                            if (!sibling.equals(pos) && !server.isOutsideBuildHeight(sibling) && server.hasChunkAt(sibling)
                                    && server.getBlockEntity(sibling) instanceof MatrixPartBlockEntity other
                                    && identity.equals(other.identity())) server.removeBlock(sibling, false);
                        }
                    } finally { removing.remove(identity); }
                }
            }
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
    @Override
    //? if >=1.21 {
    public void appendHoverText(net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.Item.TooltipContext context,
            List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.BlockGetter context,
            List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
    *///?}
        tooltip.add(net.minecraft.network.chat.Component.translatable("arcanearchives.tooltip.notimplemented1")
            .withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD));
        tooltip.add(net.minecraft.network.chat.Component.translatable("arcanearchives.tooltip.notimplemented2")
            .withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.ITALIC));
    }
}
