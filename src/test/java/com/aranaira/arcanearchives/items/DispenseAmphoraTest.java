package com.aranaira.arcanearchives.items;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.Test;

class DispenseAmphoraTest {
    @Test void registeredAmphoraUsesCustomBehaviorInsteadOfDefaultEjection() {
        assertInstanceOf(DispenseAmphora.class, DispenserBlock.DISPENSER_REGISTRY.get(ContentRegistry.RADIANT_AMPHORA.get()));
    }

    @Test void choosesPlacementOnlyForReplaceableNonFluidTargets() {
        assertFalse(DispenseAmphora.picksUp(Blocks.AIR.defaultBlockState()));
        assertFalse(DispenseAmphora.picksUp(Blocks.SHORT_GRASS.defaultBlockState()));
        assertTrue(DispenseAmphora.picksUp(Blocks.WATER.defaultBlockState()));
        assertTrue(DispenseAmphora.picksUp(Blocks.LAVA.defaultBlockState()));
        assertTrue(DispenseAmphora.picksUp(Blocks.STONE.defaultBlockState()));
        assertTrue(DispenseAmphora.picksUp(Blocks.CHEST.defaultBlockState()));
        assertTrue(DispenseAmphora.picksUp(Blocks.OAK_SLAB.defaultBlockState()
            .setValue(BlockStateProperties.WATERLOGGED, true)));
    }
}
*///?}
