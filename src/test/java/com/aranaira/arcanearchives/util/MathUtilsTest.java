package com.aranaira.arcanearchives.util;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Locale;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MathUtilsTest {
    @ParameterizedTest
    @CsvSource({"0, 9, 0, 0", "1, 9, 0, 1", "9, 9, 1, 1", "10, 9, 1, 2", "81, 9, 9, 9"})
    void preservesManifestScrollDivision(int value, int divisor, int floor, int ceiling) {
        assertEquals(floor, MathUtils.intDivisionFloor(value, divisor));
        assertEquals(ceiling, MathUtils.intDivisionCeiling(value, divisor));
    }

    @Test
    void retainsJavaDivisionRatherThanChangingLegacyNegativeSemantics() {
        assertEquals(-1, MathUtils.intDivisionFloor(-5, 3));
        assertEquals(-1, MathUtils.intDivisionCeiling(-5, 3));
        assertThrows(ArithmeticException.class, () -> MathUtils.intDivisionFloor(1, 0));
        assertThrows(ArithmeticException.class, () -> MathUtils.intDivisionCeiling(1, 0));
    }

    @Test
    void preservesHudCountThresholdsAndSuffixes() {
        Locale previous = Locale.getDefault(Locale.Category.FORMAT);
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.US);
            assertEquals("0", MathUtils.format(0));
            assertEquals("-1024", MathUtils.format(-1024));
            assertEquals("1023", MathUtils.format(1023));
            assertEquals("1.0k", MathUtils.format(1024));
            assertEquals("1.5k", MathUtils.format(1536));
            assertEquals("1024.0k", MathUtils.format(1048575));
            assertEquals("1.0m", MathUtils.format(1L << 20));
            assertEquals("1.0g", MathUtils.format(1L << 30));
            assertEquals("1.0t", MathUtils.format(1L << 40));
            assertEquals("1.0p", MathUtils.format(1L << 50));
            assertEquals("1.0e", MathUtils.format(1L << 60));
            assertEquals("8.0e", MathUtils.format(Long.MAX_VALUE));
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, previous);
        }
    }

    @Test
    void preservesLocaleSensitiveHudFormatting() {
        Locale previous = Locale.getDefault(Locale.Category.FORMAT);
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.GERMANY);
            assertEquals("1,5k", MathUtils.format(1536));
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, previous);
        }
    }

    @Test
    void preservesGoldenLegacyPositionBytesInBothDirections() {
        checkPosition(0, 0, 0, 0x0000000000000000L);
        checkPosition(1, 2, 3, 0x0000004008000003L);
        checkPosition(-1, -1, -1, 0xffffffffffffffffL);
        checkPosition(-30000000, -64, 30000000, 0x8d8f203f01c9c380L);
        checkPosition(33554431, 2047, 33554431, 0x7fffffdffdffffffL);
        checkPosition(-33554432, -2048, -33554432, 0x8000002002000000L);
    }

    @Test
    void preservesCoordinateTruncationAndSignedBitWrapping() {
        assertEquals(MathUtils.vec3dToLong(new Vec3(1, -2, 3)),
            MathUtils.vec3dToLong(new Vec3(1.9, -2.9, 3.9)));
        Vec3 decoded = MathUtils.vec3dFromLong(MathUtils.vec3dToLong(new Vec3(33554432, 2048, 33554432)));
        assertEquals(-33554432, decoded.x);
        assertEquals(-2048, decoded.y);
        assertEquals(-33554432, decoded.z);
    }

    @Test
    void roundTripsSignedWorldCoordinates() {
        Random random = new Random(0xAA1201L);
        for (int i = 0; i < 1000; i++) {
            Vec3 position = new Vec3(random.nextInt(60000001) - 30000000,
                random.nextInt(4096) - 2048, random.nextInt(60000001) - 30000000);
            Vec3 decoded = MathUtils.vec3dFromLong(MathUtils.vec3dToLong(position));
            assertEquals(position.x, decoded.x);
            assertEquals(position.y, decoded.y);
            assertEquals(position.z, decoded.z);
        }
    }

    private static void checkPosition(int x, int y, int z, long packed) {
        assertEquals(packed, MathUtils.vec3dToLong(new Vec3(x, y, z)));
        Vec3 decoded = MathUtils.vec3dFromLong(packed);
        assertEquals(x, decoded.x);
        assertEquals(y, decoded.y);
        assertEquals(z, decoded.z);
    }
}
