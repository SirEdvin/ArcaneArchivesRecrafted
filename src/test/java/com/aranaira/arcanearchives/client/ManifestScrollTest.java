package com.aranaira.arcanearchives.client;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ManifestScrollTest {
    @Test void thumbDragSnapsToSixPixelsAndClampsOutsideTrack() {
        var scroll = new ManifestScroll();
        scroll.reset(180);
        scroll.drag(81);
        assertEquals(-102, scroll.y(0));
        assertEquals(77, scroll.thumb());
        scroll.drag(-100);
        assertEquals(0, scroll.thumb());
        assertEquals(0, scroll.y(0));
        scroll.drag(1000);
        assertEquals(150, scroll.thumb());
        assertEquals(-scroll.maximum(), scroll.y(0));
        scroll.move(-6);
        assertTrue(scroll.thumb() < 150);
        scroll.reset(81);
        scroll.drag(81);
        assertEquals(0, scroll.thumb());
        assertEquals(0, scroll.y(0));
        scroll.reset(0);
        scroll.drag(1000);
        assertEquals(0, scroll.thumb());
    }

    @Test void fractionalRowsShareDrawingAndHitGeometry() {
        var scroll = new ManifestScroll();
        scroll.reset(180);
        scroll.move(6);
        assertEquals(0, scroll.first());
        assertEquals(-6, scroll.y(0));
        assertEquals(0, scroll.index(0, 11));
        assertEquals(9, scroll.index(0, 12));
        assertEquals(81, scroll.index(0, 161));
        scroll.move(12);
        assertEquals(9, scroll.first());
        assertEquals(0, scroll.y(9));
        assertEquals(9, scroll.index(0, 0));
    }

    @Test void pagesClampAndResetAfterFiltering() {
        var scroll = new ManifestScroll();
        scroll.reset(180);
        scroll.move(162);
        assertEquals(81, scroll.first());
        scroll.move(162);
        assertEquals(99, scroll.first());
        assertEquals(179, scroll.index(161, 161));
        scroll.move(-162);
        scroll.move(-162);
        assertEquals(0, scroll.first());
        scroll.move(6);
        scroll.reset(1);
        assertEquals(0, scroll.y(0));
        assertEquals(0, scroll.maximum());
        assertEquals(-1, scroll.index(18, 0));
    }

    @Test void emptyAndPartialLastRowsNeverExposeMissingCells() {
        var scroll = new ManifestScroll();
        assertEquals(-1, scroll.index(0, 0));
        scroll.reset(82);
        scroll.move(162);
        assertEquals(18, scroll.maximum());
        assertEquals(81, scroll.index(0, 161));
        assertEquals(-1, scroll.index(18, 161));
        assertEquals(-1, scroll.index(-1, 0));
        assertEquals(-1, scroll.index(162, 0));
        assertEquals(-1, scroll.index(0, -1));
        assertEquals(-1, scroll.index(0, 162));
    }
}
