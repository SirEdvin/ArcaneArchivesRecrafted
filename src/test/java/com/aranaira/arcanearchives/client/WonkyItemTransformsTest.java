package com.aranaira.arcanearchives.client;

//? if fabric {
import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class WonkyItemTransformsTest {
    private ItemTransforms transforms() throws Exception {
        try (var stream = getClass().getResourceAsStream("/assets/arcanearchives/models/block/wonky_resonator.json")) {
            assertNotNull(stream);
            return BlockModel.fromStream(new InputStreamReader(stream, StandardCharsets.UTF_8)).getTransforms();
        }
    }

    private static void vector(Vector3f actual, float x, float y, float z) {
        assertEquals(x, actual.x(), 0.000001F);
        assertEquals(y, actual.y(), 0.000001F);
        assertEquals(z, actual.z(), 0.000001F);
    }

    @Test void nativeParserRetainsOriginalRightHandTransforms() throws Exception {
        var transforms = transforms();
        var first = transforms.getTransform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
        vector(first.scale, 0.375F, 0.375F, 0.375F);
        vector(first.translation, 0, 0.025F, 0);
        vector(first.rotation, 0, 45, 0);
        var third = transforms.getTransform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        vector(third.scale, 0.375F, 0.375F, 0.375F);
        vector(third.translation, 0, 0.15F, 0);
        vector(third.rotation, 90, 0, 0);
    }

    @Test void unspecifiedLegacyLeftHandsStayIdentityInsteadOfInheritingRight() throws Exception {
        var transforms = transforms();
        for (var context : new ItemDisplayContext[]{ItemDisplayContext.FIRST_PERSON_LEFT_HAND, ItemDisplayContext.THIRD_PERSON_LEFT_HAND}) {
            var transform = transforms.getTransform(context);
            vector(transform.scale, 1, 1, 1);
            vector(transform.translation, 0, 0, 0);
            vector(transform.rotation, 0, 0, 0);
        }
    }
}
//?}
