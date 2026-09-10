package com.aranaira.arcanearchives.blocks;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
//? if >=1.21 {
import com.mojang.serialization.MapCodec;
//?}

public final class QuartzSliver extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShape UP = Block.box(6.4, 0, 6.4, 9.6, 8, 9.6);
    private static final VoxelShape DOWN = Block.box(6.4, 8, 6.4, 9.6, 16, 9.6);
    private static final VoxelShape SOUTH = Block.box(6.4, 6.4, 0, 9.6, 9.6, 8);
    private static final VoxelShape NORTH = Block.box(6.4, 6.4, 8, 9.6, 9.6, 16);
    private static final VoxelShape EAST = Block.box(0, 6.4, 6.4, 8, 9.6, 9.6);
    private static final VoxelShape WEST = Block.box(8, 6.4, 6.4, 16, 9.6, 9.6);
    //? if >=1.21 {
    public static final MapCodec<QuartzSliver> CODEC = simpleCodec(QuartzSliver::new);
    @Override public MapCodec<QuartzSliver> codec() { return CODEC; }
    //?}

    public QuartzSliver() {
        this(Properties.of().mapColor(MapColor.NONE).strength(0).lightLevel(state -> 15).noCollission().noOcclusion().randomTicks());
    }

    public QuartzSliver(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (facing == Direction.UP) return UP;
        if (facing == Direction.DOWN) return DOWN;
        if (facing == Direction.SOUTH) return SOUTH;
        if (facing == Direction.NORTH) return NORTH;
        return facing == Direction.EAST ? EAST : WEST;
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.item.quartz_sliver").withStyle(ChatFormatting.GOLD));
    }
}
