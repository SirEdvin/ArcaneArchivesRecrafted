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

class QuartzSliverTest {
    @Test
    void sixOutlinesRetainLightNoCollisionAndUnsupportedPlacement() {
        var block = ContentRegistry.QUARTZ_SLIVER.get();
        assertEquals(Direction.DOWN, block.defaultBlockState().getValue(QuartzSliver.FACING));
        assertEquals(6, block.getStateDefinition().getPossibleStates().size());
        for (Direction face : Direction.values()) {
            var state = block.defaultBlockState().setValue(QuartzSliver.FACING, face);
            assertEquals(15, state.getLightEmission());
            assertEquals(0, state.getDestroySpeed(null, BlockPos.ZERO));
            assertFalse(state.requiresCorrectToolForDrops());
            assertTrue(state.isRandomlyTicking());
            assertTrue(state.canSurvive(null, BlockPos.ZERO));
            assertTrue(state.getCollisionShape(null, BlockPos.ZERO).isEmpty());
            var bounds = block.getShape(state, null, BlockPos.ZERO, CollisionContext.empty()).bounds();
            for (Direction.Axis axis : Direction.Axis.values()) {
                boolean along = axis == face.getAxis();
                double start = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 0 : 0.5;
                assertEquals(along ? start : 0.4, bounds.min(axis), 1e-6);
                assertEquals(along ? start + 0.5 : 0.6, bounds.max(axis), 1e-6);
                assertFalse(state.isFaceSturdy(null, BlockPos.ZERO, face));
            }
            for (Rotation rotation : Rotation.values())
                assertEquals(rotation.rotate(face), block.rotate(state, rotation).getValue(QuartzSliver.FACING));
            for (Mirror mirror : Mirror.values())
                assertEquals(mirror.mirror(face), block.mirror(state, mirror).getValue(QuartzSliver.FACING));
        }
    }
}
*///?}
