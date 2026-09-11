package com.aranaira.arcanearchives.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

/** Test-only native callback fault; never included in a production source set or artifact. */
@Mod.EventBusSubscriber(modid = "arcanearchives", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PlacementInterruptionBlock extends Block {
    public static final ResourceLocation ID = new ResourceLocation("arcanearchives_test", "placement_interruption");

    public PlacementInterruptionBlock() { super(Properties.of()); }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(Registries.BLOCK, ID, PlacementInterruptionBlock::new);
    }

    @Override public boolean canBeReplaced(BlockState state, BlockPlaceContext context) { return true; }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!level.isClientSide && (replacement.is(com.aranaira.arcanearchives.init.ContentRegistry.MATRIX_RESERVOIR.get())
                || replacement.is(com.aranaira.arcanearchives.init.ContentRegistry.MATRIX_DISTILLATE.get())))
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        super.onRemove(state, level, pos, replacement, moving);
    }
}
