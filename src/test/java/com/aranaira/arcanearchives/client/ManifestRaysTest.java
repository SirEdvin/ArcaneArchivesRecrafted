package com.aranaira.arcanearchives.client;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ManifestRaysTest {
    @Test void originalDistanceFalloffHasApprovedPositiveMinimum() {
        assertEquals(7F, ManifestRays.width(0));
        assertEquals(7F, ManifestRays.width(10));
        assertEquals(3.5F, ManifestRays.width(40), .00001F);
        assertEquals(1F, ManifestRays.width(70));
        assertEquals(1F, ManifestRays.width(1000));
        float previous = 7F;
        for (int distance = 0; distance <= 100; distance++) {
            float width = ManifestRays.width(distance);
            assertTrue(width >= 1 && width <= previous);
            previous = width;
        }
    }
}
