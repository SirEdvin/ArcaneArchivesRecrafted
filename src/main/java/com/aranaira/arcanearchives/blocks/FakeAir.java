package com.aranaira.arcanearchives.blocks;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.FakeAirBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Passable, replaceable temporary air that refuses liquid until its original cooldown expires. */
public final class FakeAir extends Block implements EntityBlock, LiquidBlockContainer {
    // Non-air keeps otherwise empty chunk sections from hiding this temporary fluid barrier.
    public FakeAir() { this(Properties.of().replaceable().noCollission().noOcclusion().noLootTable()); }
    public FakeAir(Properties properties) { super(properties); }
    //? if >=1.21 {
    public static final com.mojang.serialization.MapCodec<FakeAir> CODEC = simpleCodec(FakeAir::new);
    @Override public com.mojang.serialization.MapCodec<FakeAir> codec() { return CODEC; }
    //?}
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return Shapes.empty(); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FakeAirBlockEntity(pos, state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide || type != ContentRegistry.FAKE_AIR_ENTITY.get() ? null
            : (world, pos, block, entity) -> { if (entity instanceof FakeAirBlockEntity air) air.tick(); };
    }
    @Override
    //? if >=1.21 {
    public boolean canPlaceLiquid(Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) { return false; }
    //?} else {
    /*public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) { return false; }
    *///?}
    @Override public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) { return false; }
}
