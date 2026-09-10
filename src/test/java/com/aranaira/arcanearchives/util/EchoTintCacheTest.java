package com.aranaira.arcanearchives.util;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EchoTintCacheTest {
    @TempDir Path directory;

    @Test void missingConfigurationGeneratesAndSurvivesRestart() throws Exception {
        var cache = new EchoTintCache(directory);
        assertFalse(cache.hasColors());
        assertEquals(-1, cache.color("minecraft:iron_ingot"));
        cache.generateInputs(Set.of("minecraft:iron_ore"));
        cache.generateColors(Map.of("minecraft:iron_ingot", 0xff123456));
        var restarted = new EchoTintCache(directory);
        assertEquals(Set.of("minecraft:iron_ore"), restarted.inputs());
        assertEquals(0xff123456, restarted.color("minecraft:iron_ingot"));
    }

    @Test void explicitNonOreAndUnknownIdentifiersAreRetainedWithoutRewriting() throws Exception {
        String inputs = "# custom\nminecraft:sand\nmissing_mod:ore\n";
        String colors = "# hand edited\nminecraft:glass,-1234\nmissing_mod:ingot,7\n";
        Files.writeString(directory.resolve("ores.txt"), inputs);
        Files.writeString(directory.resolve("echo_colors.txt"), colors);
        var cache = new EchoTintCache(directory);
        cache.generateInputs(Set.of("minecraft:iron_ore"));
        cache.generateColors(Map.of("minecraft:glass", 9));
        assertEquals(Set.of("minecraft:sand", "missing_mod:ore"), cache.inputs());
        assertEquals(-1234, cache.color("minecraft:glass"));
        assertEquals(7, cache.color("missing_mod:ingot"));
        assertThrows(UnsupportedOperationException.class, () -> cache.inputs().clear());
        assertEquals(inputs, Files.readString(directory.resolve("ores.txt")));
        assertEquals(colors, Files.readString(directory.resolve("echo_colors.txt")));
    }

    @Test void emptyListsRegenerateButExplicitUntintedColorIsStillAConfiguredCache() throws Exception {
        Files.writeString(directory.resolve("ores.txt"), "# empty\n");
        Files.writeString(directory.resolve("echo_colors.txt"), "\n");
        var cache = new EchoTintCache(directory);
        cache.generateInputs(Set.of("minecraft:gold_ore"));
        cache.generateColors(Map.of("minecraft:gold_ingot", -1));
        var restarted = new EchoTintCache(directory);
        assertTrue(restarted.hasColors());
        assertEquals(Set.of("minecraft:gold_ore"), restarted.inputs());
    }

    @Test void malformedConfigurationFailsWithoutReplacement() throws Exception {
        for (String input : new String[]{"minecraft:iron_ore:0:1", "minecraft:Iron", "iron_ore"}) {
            Files.writeString(directory.resolve("ores.txt"), input);
            assertThrows(IllegalArgumentException.class, () -> new EchoTintCache(directory));
            assertEquals(input, Files.readString(directory.resolve("ores.txt")));
        }
        Files.delete(directory.resolve("ores.txt"));
        for (String color : new String[]{"minecraft:iron_ingot,not-a-number", "12,34", "minecraft:iron_ingot,2,3"}) {
            Files.writeString(directory.resolve("echo_colors.txt"), color);
            assertThrows(IllegalArgumentException.class, () -> new EchoTintCache(directory));
            assertEquals(color, Files.readString(directory.resolve("echo_colors.txt")));
        }
    }

    @Test void unwritableDirectoryStillRetainsGeneratedSessionData() throws Exception {
        Path blocked = directory.resolve("file-not-directory");
        Files.writeString(blocked, "retain");
        var cache = new EchoTintCache(blocked);
        assertThrows(java.io.IOException.class, () -> cache.generateInputs(Set.of("minecraft:iron_ore")));
        assertEquals(Set.of("minecraft:iron_ore"), cache.inputs());
        assertThrows(java.io.IOException.class, () -> cache.generateColors(Map.of("minecraft:iron_ingot", 3)));
        assertEquals(3, cache.color("minecraft:iron_ingot"));
        assertEquals("retain", Files.readString(blocked));
    }

    @Test void originalRgbWindowUsesTruncatedChannelMeansAndIgnoresOutsidePixels() throws Exception {
        var image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 16; x++) for (int y = 0; y < 16; y++) image.setRGB(x, y, 0xffffff);
        for (int x = 6; x <= 9; x++) for (int y = 6; y <= 9; y++) image.setRGB(x, y, 0x102030);
        image.setRGB(6, 6, 0x1f2f3f); // Each channel adds 15 over 16 samples: truncated away.
        Path png = directory.resolve("rgb.png");
        javax.imageio.ImageIO.write(image, "png", png.toFile());
        assertEquals(0xff102030, EchoTintCache.sample(javax.imageio.ImageIO.read(png.toFile())));
    }

    @Test void originalBoundsKeepAvailablePixelsWithoutScalingTheWindow() {
        var image = new BufferedImage(7, 7, BufferedImage.TYPE_INT_RGB);
        image.setRGB(6, 6, 0x123456);
        assertEquals(0xff123456, EchoTintCache.sample(image));
        assertNull(EchoTintCache.sample(new BufferedImage(6, 6, BufferedImage.TYPE_INT_RGB)));
    }

    @Test void unsupportedRasterLayoutsAndMissingImagesRemainUntinted() {
        assertNull(EchoTintCache.sample(null));
        for (int type : new int[]{BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_BYTE_INDEXED, BufferedImage.TYPE_BYTE_GRAY})
            assertNull(EchoTintCache.sample(new BufferedImage(16, 16, type)));
    }

    @Test void opaqueWhiteRemainsAValidCacheEntryRatherThanAMissingSample() {
        var image = new BufferedImage(7, 7, BufferedImage.TYPE_INT_RGB);
        image.setRGB(6, 6, 0xffffff);
        assertEquals(Integer.valueOf(-1), EchoTintCache.sample(image));
    }
}
