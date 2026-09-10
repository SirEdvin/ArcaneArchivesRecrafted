package com.aranaira.arcanearchives.client;

//? if fabric {
import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class LotusItemTransformsTest {
    private static void vector(Vector3f actual, float x, float y, float z) {
        assertEquals(x, actual.x(), 0.000001F);
        assertEquals(y, actual.y(), 0.000001F);
        assertEquals(z, actual.z(), 0.000001F);
    }

    @Test void nativeParserPreservesOriginalHandAndInventoryTransforms() throws Exception {
        try (var stream = getClass().getResourceAsStream("/assets/arcanearchives/models/block/celestial_lotus_engine.json")) {
            assertNotNull(stream);
            var transforms = BlockModel.fromStream(new InputStreamReader(stream, StandardCharsets.UTF_8)).getTransforms();
            var first = transforms.getTransform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
            vector(first.scale, 0.25F, 0.25F, 0.25F);
            vector(first.translation, 0, 0, 0);
            vector(first.rotation, 0, 45, 0);
            var third = transforms.getTransform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
            vector(third.scale, 0.25F, 0.25F, 0.25F);
            vector(third.translation, 0.02F, 0.05F, -0.05F);
            vector(third.rotation, 90, 0, 0);
            vector(transforms.getTransform(ItemDisplayContext.GUI).translation, 0, -0.25F, 0);
            for (var context : new ItemDisplayContext[]{ItemDisplayContext.GUI, ItemDisplayContext.GROUND})
                vector(transforms.getTransform(context).scale, 0.25F, 0.25F, 0.25F);
            for (var context : new ItemDisplayContext[]{ItemDisplayContext.FIRST_PERSON_LEFT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND}) {
                var transform = transforms.getTransform(context);
                vector(transform.scale, 1, 1, 1);
                vector(transform.translation, 0, 0, 0);
                vector(transform.rotation, 0, 0, 0);
            }
        }
    }
}
//?}
