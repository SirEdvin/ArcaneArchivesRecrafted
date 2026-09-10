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

public final class RadiantLantern extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShape VERTICAL = Block.box(5.6, 0, 5.6, 10.4, 16, 10.4);
    private static final VoxelShape NORTH_SOUTH = Block.box(5.6, 5.6, 0, 10.4, 10.4, 16);
    private static final VoxelShape EAST_WEST = Block.box(0, 5.6, 5.6, 16, 10.4, 10.4);
    //? if >=1.21 {
    public static final MapCodec<RadiantLantern> CODEC = simpleCodec(RadiantLantern::new);

    @Override
    public MapCodec<RadiantLantern> codec() { return CODEC; }
    //?}

    public RadiantLantern() {
        this(Properties.of().mapColor(MapColor.NONE).strength(0.3F).lightLevel(state -> 15).noCollission().noOcclusion());
    }

    public RadiantLantern(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction.Axis axis = state.getValue(FACING).getAxis();
        return axis == Direction.Axis.Y ? VERTICAL : axis == Direction.Axis.Z ? NORTH_SOUTH : EAST_WEST;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.device.radiant_lantern").withStyle(ChatFormatting.GOLD));
    }
}
