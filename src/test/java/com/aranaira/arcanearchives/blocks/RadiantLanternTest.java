package com.aranaira.arcanearchives.blocks;

//? if neoforge {
/*import com.aranaira.arcanearchives.init.ContentRegistry;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RadiantLanternTest {
    @Test
    void allSixStatesRetainLightOutlineNoCollisionAndRotation() {
        var block = ContentRegistry.RADIANT_LANTERN.get();
        assertEquals(6, block.getStateDefinition().getPossibleStates().size());
        for (Direction facing : Direction.values()) {
            var state = block.defaultBlockState().setValue(RadiantLantern.FACING, facing);
            assertEquals(15, state.getLightEmission());
            assertEquals(0.3F, state.getDestroySpeed(null, BlockPos.ZERO));
            assertFalse(state.requiresCorrectToolForDrops());
            assertFalse(state.hasBlockEntity());
            assertTrue(state.canSurvive(null, BlockPos.ZERO));
            assertTrue(state.getCollisionShape(null, BlockPos.ZERO).isEmpty());
            var bounds = block.getShape(state, null, BlockPos.ZERO, CollisionContext.empty()).bounds();
            for (Direction.Axis axis : Direction.Axis.values()) {
                assertEquals(axis == facing.getAxis() ? 0 : 0.35, bounds.min(axis), 1e-6);
                assertEquals(axis == facing.getAxis() ? 1 : 0.65, bounds.max(axis), 1e-6);
            }
            for (Rotation rotation : Rotation.values()) {
                assertEquals(rotation.rotate(facing), block.rotate(state, rotation).getValue(RadiantLantern.FACING));
            }
            for (Mirror mirror : Mirror.values()) {
                assertEquals(mirror.mirror(facing), block.mirror(state, mirror).getValue(RadiantLantern.FACING));
            }
        }
    }

    @Test
    void packagedNativeModelRotationsPreserveLegacyOrderedMatrices() throws Exception {
        try (var stream = getClass().getResourceAsStream("/assets/arcanearchives/blockstates/radiant_lantern.json")) {
            assertNotNull(stream);
            var variants = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("variants");
            for (Direction facing : Direction.values()) {
                var json = variants.getAsJsonObject("facing=" + facing.getName());
                var actual = net.minecraft.client.resources.model.BlockModelRotation.by(json.get("x").getAsInt(), json.get("y").getAsInt()).getRotation().getMatrix();
                var expected = new org.joml.Matrix4f();
                if (facing == Direction.DOWN) expected.rotateX((float) Math.PI);
                else if (facing != Direction.UP) {
                    float degrees = facing == Direction.SOUTH ? 0 : facing == Direction.EAST ? 90 : facing == Direction.NORTH ? 180 : 270;
                    expected.rotateY((float) Math.toRadians(degrees)).rotateX((float) Math.PI / 2);
                }
                for (var vector : java.util.List.of(new org.joml.Vector3f(1, 0, 0), new org.joml.Vector3f(0, 1, 0), new org.joml.Vector3f(0, 0, 1))) {
                    var reference = expected.transformDirection(new org.joml.Vector3f(vector));
                    var transformed = actual.transformDirection(new org.joml.Vector3f(vector));
                    assertTrue(reference.distance(transformed) < 1e-5, facing.toString());
                }
            }
        }
    }
}
*///?}
