package com.aranaira.arcanearchives.client;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ManifestHighlightTest {
    @Test void originalPaletteRepeatsEveryNinetySixTicks() {
        int[] colors = {0xFFFF7F7F, 0xFFFFBF7F, 0xFFFFFF7F, 0xFF7FFF99,
            0xFF7FFFFF, 0xFF7FA5FF, 0xFFCC7FFF, 0xFFFF8CFF};
        for (int band = 0; band < colors.length; band++) {
            assertEquals(colors[band], ManifestHighlight.color(band * 12));
            assertEquals(colors[band], ManifestHighlight.color(band * 12 + 96));
        }
        for (int tick = 0; tick < 96; tick++) {
            assertEquals(ManifestHighlight.color(tick), ManifestHighlight.color(tick + 96));
            assertEquals(255, ManifestHighlight.color(tick) >>> 24);
        }
    }

    @Test void interpolationAndNegativeTimePreserveOriginalClamping() {
        assertEquals(0xFFFF9F7F, ManifestHighlight.color(6));
        assertEquals(0xFFFF7F7F, ManifestHighlight.color(-1));
        assertEquals(0xFFFFFFFF, ManifestHighlight.color(-12));
        assertEquals(0xFFFF7F7F, ManifestHighlight.color(-96));
    }
}
