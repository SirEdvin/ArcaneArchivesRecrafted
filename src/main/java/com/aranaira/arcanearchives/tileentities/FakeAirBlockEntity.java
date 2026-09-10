package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class FakeAirBlockEntity extends BlockEntity {
    // Like upstream, this transient countdown restarts when the block entity is reloaded.
    private int remaining = 200;
    public FakeAirBlockEntity(BlockPos pos, BlockState state) { super(ContentRegistry.FAKE_AIR_ENTITY.get(), pos, state); }
    public void tick() {
        if (level != null && !level.isClientSide && --remaining <= 0) level.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), 3);
    }
}
