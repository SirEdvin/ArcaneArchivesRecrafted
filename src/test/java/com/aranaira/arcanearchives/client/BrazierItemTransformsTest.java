package com.aranaira.arcanearchives.client;

//? if fabric {
import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class BrazierItemTransformsTest {
    private static void vector(Vector3f actual, float x, float y, float z) {
        assertEquals(x, actual.x(), 0.000001F);
        assertEquals(y, actual.y(), 0.000001F);
        assertEquals(z, actual.z(), 0.000001F);
    }

    @Test void nativeParserPreservesBothHandsAndInventoryTransforms() throws Exception {
        try (var stream = getClass().getResourceAsStream("/assets/arcanearchives/models/block/brazier_of_hoarding.json")) {
            assertNotNull(stream);
            var transforms = BlockModel.fromStream(new InputStreamReader(stream, StandardCharsets.UTF_8)).getTransforms();
            for (var context : new ItemDisplayContext[]{ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, ItemDisplayContext.FIRST_PERSON_LEFT_HAND}) {
                var transform = transforms.getTransform(context);
                vector(transform.rotation, 0, 45, 0);
                vector(transform.translation, 0, 0.025F, 0);
                vector(transform.scale, 0.375F, 0.375F, 0.375F);
            }
            for (var context : new ItemDisplayContext[]{ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND}) {
                var transform = transforms.getTransform(context);
                vector(transform.rotation, 90, 0, 0);
                vector(transform.translation, 0, 0.15F, 0);
                vector(transform.scale, 0.375F, 0.375F, 0.375F);
            }
            var gui = transforms.getTransform(ItemDisplayContext.GUI);
            vector(gui.translation, 0.02F, 0, 0);
            vector(gui.scale, 0.65F, 0.65F, 0.65F);
            var expectedRotation = new org.joml.Quaternionf()
                .rotateY((float) Math.toRadians(45)).rotateX((float) Math.toRadians(20)).rotateZ((float) Math.toRadians(20));
            var parsedRotation = new org.joml.Quaternionf().rotationXYZ(
                (float) Math.toRadians(gui.rotation.x()), (float) Math.toRadians(gui.rotation.y()),
                (float) Math.toRadians(gui.rotation.z()));
            for (var axis : new Vector3f[]{new Vector3f(1, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1)}) {
                var expected = expectedRotation.transform(new Vector3f(axis));
                vector(parsedRotation.transform(new Vector3f(axis)), expected.x(), expected.y(), expected.z());
            }
            var ground = transforms.getTransform(ItemDisplayContext.GROUND);
            vector(ground.rotation, 0, 0, 0);
            vector(ground.translation, 0, 0, 0);
            vector(ground.scale, 0.25F, 0.25F, 0.25F);
        }
    }
}
//?}
