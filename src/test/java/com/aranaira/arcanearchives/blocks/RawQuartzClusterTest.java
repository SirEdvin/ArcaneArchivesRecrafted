package com.aranaira.arcanearchives.blocks;

//? if neoforge {
/*import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RawQuartzClusterTest {
    @Test
    void nativePropertiesKeepUpstreamFixedOutlineAndSolidCollisionForEveryFacing() {
        var block = ContentRegistry.RAW_QUARTZ_CLUSTER.get();
        assertEquals(Direction.UP, block.defaultBlockState().getValue(RawQuartzCluster.FACING));
        assertEquals(6, block.getStateDefinition().getPossibleStates().size());
        for (Direction facing : Direction.values()) {
            var state = block.defaultBlockState().setValue(RawQuartzCluster.FACING, facing);
            assertEquals(15, state.getLightEmission());
            assertEquals(1.4F, state.getDestroySpeed(null, BlockPos.ZERO));
            assertTrue(state.requiresCorrectToolForDrops());
            assertFalse(state.hasBlockEntity());
            assertFalse(state.isRandomlyTicking());
            assertTrue(state.canSurvive(null, BlockPos.ZERO));
            var bounds = block.getShape(state, null, BlockPos.ZERO, CollisionContext.empty()).bounds();
            assertEquals(0.2, bounds.minX, 1e-6);
            assertEquals(0, bounds.minY);
            assertEquals(0.2, bounds.minZ, 1e-6);
            assertEquals(0.8, bounds.maxX, 1e-6);
            assertEquals(1, bounds.maxY);
            assertEquals(0.8, bounds.maxZ, 1e-6);
            assertEquals(bounds, state.getCollisionShape(null, BlockPos.ZERO).bounds());
            for (Rotation rotation : Rotation.values())
                assertEquals(rotation.rotate(facing), block.rotate(state, rotation).getValue(RawQuartzCluster.FACING));
            for (Mirror mirror : Mirror.values())
                assertEquals(mirror.mirror(facing), block.mirror(state, mirror).getValue(RawQuartzCluster.FACING));
        }
    }
}
*///?}
