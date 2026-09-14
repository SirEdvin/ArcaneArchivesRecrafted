package com.aranaira.arcanearchives.client;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ManifestRaysTest {
    @Test void solidBeamHasConstantWorldThicknessIncludingVerticalAndDiagonal() {
        var start = new net.minecraft.world.phys.Vec3(2, 3, 4);
        for (var end : new net.minecraft.world.phys.Vec3[]{start.add(0, 20, 0), start.add(1000, 0, 0), start.add(4, 5, 6)}) {
            var corners = ManifestRays.corners(new ManifestRays.Beam(start, end));
            assertEquals(8, corners.length);
            assertEquals(1D / 16, corners[0].distanceTo(corners[1]), 1E-9);
            assertEquals(1D / 16, corners[1].distanceTo(corners[2]), 1E-9);
            assertEquals(start.distanceTo(end), corners[0].distanceTo(corners[4]), 1E-9);
            for (var corner : corners) assertTrue(Double.isFinite(corner.x) && Double.isFinite(corner.y) && Double.isFinite(corner.z));
        }
        assertEquals(0, ManifestRays.corners(new ManifestRays.Beam(start, start)).length);
    }
}
