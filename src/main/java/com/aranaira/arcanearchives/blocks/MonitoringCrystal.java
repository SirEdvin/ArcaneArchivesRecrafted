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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
//? if >=1.21 {
import com.mojang.serialization.MapCodec;
//?}

public final class MonitoringCrystal extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    //? if >=1.21 {
    public static final MapCodec<MonitoringCrystal> CODEC = simpleCodec(MonitoringCrystal::new);
    @Override public MapCodec<MonitoringCrystal> codec() { return CODEC; }
    //?}
    public MonitoringCrystal() { this(Properties.of().strength(.8F).lightLevel(state -> 15).noCollission().noOcclusion()); }
    public MonitoringCrystal(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity(pos, state);
    }
    @Override public void setPlacedBy(net.minecraft.world.level.Level level, BlockPos pos, BlockState state,
            net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity crystal)
            crystal.recordPlacer(placer);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getClickedFace()); }
    @Override public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state, Mirror mirror) { return state.setValue(FACING, mirror.mirror(state.getValue(FACING))); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Legacy AxisAlignedBB normalized its endpoint pairs, including the overhanging attachment.
        return switch (state.getValue(FACING)) {
            case UP -> Shapes.box(.37, -.1, .35, .61, .04, .64);
            case DOWN -> Shapes.box(.37, .94, .35, .61, 1.1, .64);
            case SOUTH -> Shapes.box(.35, .35, -.1, .61, .64, .04);
            case NORTH -> Shapes.box(.39, .35, .94, .63, .64, 1.1);
            case EAST -> Shapes.box(-.1, .35, .39, .05, .63, .63);
            case WEST -> Shapes.box(.95, .35, .37, 1.1, .63, .62);
        };
    }
    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> text, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, BlockGetter context, List<Component> text, TooltipFlag flag) {
    *///?}
        text.add(Component.translatable("arcanearchives.tooltip.device.monitoring_crystal").withStyle(ChatFormatting.GOLD));
    }
}
