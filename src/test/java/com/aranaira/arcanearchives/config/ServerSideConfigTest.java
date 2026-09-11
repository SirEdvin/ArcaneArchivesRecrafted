package com.aranaira.arcanearchives.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ServerSideConfigTest {
    @TempDir Path directory;

    @Test
    void retainsUpstreamDefaultsAndRoundTripsCustomValues() {
        assertEquals(new ServerSideConfig(3, 6000, 4, true, true), ServerSideConfig.fromProperties(new Properties()));
        ServerSideConfig custom = new ServerSideConfig(5, 123, 8, false, true);
        assertEquals(custom, ServerSideConfig.fromProperties(custom.toProperties()));
    }

    @Test
    void enforcesOnlyTheUpstreamNumericRange() {
        assertThrows(IllegalArgumentException.class, () -> new ServerSideConfig(3, 6000, 0, true, true));
        assertThrows(IllegalArgumentException.class, () -> new ServerSideConfig(3, 6000, 9, true, true));
        assertDoesNotThrow(() -> new ServerSideConfig(0, -1, 1, true, true));
    }

    @Test
    void completionSoundSwitchesDefaultOnAndRoundTripIndependently() {
        var defaults = ServerSideConfig.fromProperties(new Properties());
        assertTrue(defaults.useSounds());
        assertTrue(defaults.resonatorComplete());
        for (boolean sounds : new boolean[]{false, true}) {
            for (boolean completion : new boolean[]{false, true}) {
                Properties values = new Properties();
                values.setProperty("UseSounds", Boolean.toString(sounds));
                values.setProperty("ResonatorComplete", Boolean.toString(completion));
                var configured = ServerSideConfig.fromProperties(values);
                assertEquals(sounds, configured.useSounds());
                assertEquals(completion, configured.resonatorComplete());
                assertEquals(configured, ServerSideConfig.fromProperties(configured.toProperties()));
                assertEquals(defaults.resonatorTickTime(), configured.resonatorTickTime());
            }
        }
    }

    @Test
    void invalidSoundSwitchesPreserveFileAndPreviousSnapshot() throws Exception {
        ServerSideConfig.initialize(directory);
        var before = ServerSideConfig.current();
        Path file = directory.resolve("arcanearchives/server.properties");
        for (String key : new String[]{"UseSounds", "ResonatorComplete"}) {
            String invalid = key + "=perhaps\n";
            Files.writeString(file, invalid);
            assertThrows(IllegalStateException.class, () -> ServerSideConfig.initialize(directory));
            assertEquals(invalid, Files.readString(file));
            assertEquals(before, ServerSideConfig.current());
        }
    }

    @Test
    void createsDefaultsThenLoadsExistingSettingsWithoutRewritingThem() throws Exception {
        ServerSideConfig.initialize(directory);
        Path file = directory.resolve("arcanearchives/server.properties");
        assertTrue(Files.isRegularFile(file));
        String custom = "# retain this comment\nRadiantMultiplier=7\nBookFromBookshelf=false\n";
        Files.writeString(file, custom);
        ServerSideConfig.initialize(directory);
        assertEquals(7, ServerSideConfig.current().radiantMultiplier());
        assertFalse(ServerSideConfig.current().bookFromBookshelf());
        assertEquals(custom, Files.readString(file));
    }

    @Test
    void invalidSettingsLeaveTheFileAndPreviousSnapshotIntact() throws Exception {
        ServerSideConfig.initialize(directory);
        ServerSideConfig before = ServerSideConfig.current();
        Path file = directory.resolve("arcanearchives/server.properties");
        String invalid = "RadiantMultiplier=9\n";
        Files.writeString(file, invalid);
        assertThrows(IllegalStateException.class, () -> ServerSideConfig.initialize(directory));
        assertEquals(invalid, Files.readString(file));
        assertEquals(before, ServerSideConfig.current());
        Properties properties = new Properties();
        properties.setProperty("BookFromResonator", "maybe");
        assertThrows(IllegalArgumentException.class, () -> ServerSideConfig.fromProperties(properties));
    }
}
