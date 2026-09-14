package com.aranaira.arcanearchives.config;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClientConfigTest {
    @TempDir Path directory;

    @Test void storagePreferencesKeepOriginalDefaultsAndPreserveFiles() throws Exception {
        Path path = directory.resolve("client.properties");
        var defaults = ClientConfig.load(path);
        assertTrue(defaults.trovesDispense());
        assertFalse(defaults.defaultRoutingNoNewItems());
        assertTrue(Files.readString(path).contains("TrovesDispense=true"));
        assertTrue(Files.readString(path).contains("DefaultRoutingNoNewItems=false"));
        for (String text : new String[]{"# preserve\nTrovesDispense=false\nDefaultRoutingNoNewItems=true\n", "UseSounds=false\n"}) {
            Files.writeString(path, text);
            var config = ClientConfig.load(path);
            assertEquals(!text.contains("TrovesDispense=false"), config.trovesDispense());
            assertEquals(text.contains("DefaultRoutingNoNewItems=true"), config.defaultRoutingNoNewItems());
            assertEquals(text, Files.readString(path));
        }
        for (String key : new String[]{"TrovesDispense", "DefaultRoutingNoNewItems"}) {
            String text = key + "=maybe\n";
            Files.writeString(path, text);
            assertThrows(IllegalStateException.class, () -> ClientConfig.load(path));
            assertEquals(text, Files.readString(path));
        }
    }

    @Test void manifestShiftPreferencePreservesOriginalClickPolicyAndFiles() throws Exception {
        Path path = directory.resolve("client.properties");
        assertTrue(ClientConfig.load(path).manifestHoldShift());
        assertTrue(Files.readString(path).contains("ManifestHoldShift=true"));
        for (String text : new String[]{"UseSounds=false\n", "ManifestHoldShift=true\n", "ManifestHoldShift=false\n"}) {
            Files.writeString(path, text);
            var config = ClientConfig.load(path);
            boolean keepOpen = !text.contains("ManifestHoldShift=false");
            assertEquals(keepOpen, config.manifestHoldShift());
            assertEquals(keepOpen, config.closeManifestAfterSelection(0, false));
            assertEquals(!keepOpen, config.closeManifestAfterSelection(0, true));
            assertFalse(config.closeManifestAfterSelection(1, false));
            assertFalse(config.closeManifestAfterSelection(1, true));
            assertFalse(config.closeManifestAfterSelection(2, true));
            assertEquals(text, Files.readString(path));
        }
        Files.writeString(path, "ManifestHoldShift=perhaps\n");
        assertThrows(IllegalStateException.class, () -> ClientConfig.load(path));
        assertEquals("ManifestHoldShift=perhaps\n", Files.readString(path));
    }

    @Test void manifestPresenceDefaultsOnAndPreservesExistingFiles() throws Exception {
        Path path = directory.resolve("client.properties");
        assertTrue(ClientConfig.load(path).manifestPresence());
        assertTrue(Files.readString(path).contains("ManifestPresence=true"));
        for (String text : new String[]{"ManifestPresence=false\n", "UseSounds=false\n"}) {
            Files.writeString(path, text);
            assertEquals(!text.contains("ManifestPresence=false"), ClientConfig.load(path).manifestPresence());
            assertEquals(text, Files.readString(path));
        }
        Files.writeString(path, "ManifestPresence=perhaps\n");
        assertThrows(IllegalStateException.class, () -> ClientConfig.load(path));
        assertEquals("ManifestPresence=perhaps\n", Files.readString(path));
    }

    @Test void manifestDistanceAndGridPreserveDefaultsAndRejectInvalidFiles() throws Exception {
        Path path = directory.resolve("client.properties");
        var defaults = ClientConfig.load(path);
        assertEquals(100, defaults.manifestMaxDistance());
        assertTrue(defaults.disableManifestGrid());
        for (int distance : new int[]{0, 1, 200, Integer.MAX_VALUE}) {
            String text = "ManifestMaxDistance=" + distance + "\nDisableManifestGrid=false\n";
            Files.writeString(path, text);
            assertEquals(distance, ClientConfig.load(path).manifestMaxDistance());
            assertFalse(ClientConfig.load(path).disableManifestGrid());
            assertEquals(text, Files.readString(path));
        }
        for (String text : new String[]{"ManifestMaxDistance=-1", "ManifestMaxDistance=2147483648",
                "ManifestMaxDistance=1.5", "DisableManifestGrid=maybe"}) {
            Files.writeString(path, text);
            assertThrows(IllegalStateException.class, () -> ClientConfig.load(path));
            assertEquals(text, Files.readString(path));
        }
        Files.writeString(path, "OtherSetting=preserved\n");
        assertEquals(100, ClientConfig.load(path).manifestMaxDistance());
        assertTrue(ClientConfig.load(path).disableManifestGrid());
    }

    @Test void manifestSearchOptionsDefaultOffAndPreserveExistingFiles() throws Exception {
        Path path = directory.resolve("client.properties");
        var defaults = ClientConfig.load(path);
        assertFalse(defaults.manifestSearchTermPersistence());
        assertFalse(defaults.manifestJeiSynchronise());
        assertTrue(Files.readString(path).contains("ManifestSearchTermPersistence=false"));
        assertTrue(Files.readString(path).contains("ManifestJeiSynchronise=false"));
        String configured = "# preserve comments\nManifestSearchTermPersistence=true\nManifestJeiSynchronise=true\n";
        Files.writeString(path, configured);
        assertTrue(ClientConfig.load(path).manifestSearchTermPersistence());
        assertTrue(ClientConfig.load(path).manifestJeiSynchronise());
        assertEquals(configured, Files.readString(path));
        Files.writeString(path, "UseSounds=false\n");
        assertFalse(ClientConfig.load(path).manifestSearchTermPersistence());
        assertFalse(ClientConfig.load(path).manifestJeiSynchronise());
        assertEquals("UseSounds=false\n", Files.readString(path));
        for (String key : new String[]{"ManifestSearchTermPersistence", "ManifestJeiSynchronise"}) {
            String invalid = key + "=maybe\n";
            Files.writeString(path, invalid);
            assertThrows(IllegalStateException.class, () -> ClientConfig.load(path));
            assertEquals(invalid, Files.readString(path));
        }
    }

    @Test void newAndMissingSettingsKeepOriginalPrettyDefault() throws Exception {
        Path path = directory.resolve("arcanearchives/client.properties");
        assertTrue(ClientConfig.load(path).usePrettyGUIs());
        assertTrue(Files.readString(path).contains("UsePrettyGUIs=true"));
        Files.writeString(path, "OtherSetting=retained\n");
        assertTrue(ClientConfig.load(path).usePrettyGUIs());
        assertEquals("OtherSetting=retained\n", Files.readString(path));
    }

    @Test void simplePreferenceLoadsWithoutRewritingUserFile() throws Exception {
        Path path = directory.resolve("client.properties");
        String original = "# keep my comment\nUsePrettyGUIs=false\nOtherSetting=retained\n";
        Files.writeString(path, original);
        assertFalse(ClientConfig.load(path).usePrettyGUIs());
        assertEquals(original, Files.readString(path));
    }

    @Test void invalidSettingFailsWithoutReplacingFile() throws Exception {
        Path path = directory.resolve("client.properties");
        Files.writeString(path, "UsePrettyGUIs=perhaps\n");
        assertThrows(IllegalStateException.class, () -> ClientConfig.load(path));
        assertEquals("UsePrettyGUIs=perhaps\n", Files.readString(path));
    }

    @Test void resonatorSoundDefaultsAndOverridesPreserveUserFile() throws Exception {
        Path path = directory.resolve("client.properties");
        var defaults = ClientConfig.load(path);
        assertTrue(defaults.useSounds());
        assertTrue(defaults.resonatorTicking());
        assertEquals(.15F, defaults.resonatorVolume());
        String original = "UseSounds=false\nResonatorTicking=false\nResonatorVolume=0.25\n";
        Files.writeString(path, original);
        var configured = ClientConfig.load(path);
        assertFalse(configured.useSounds());
        assertFalse(configured.resonatorTicking());
        assertEquals(.25F, configured.resonatorVolume());
        assertEquals(original, Files.readString(path));
        Files.writeString(path, "UsePrettyGUIs=false\n");
        assertEquals(.15F, ClientConfig.load(path).resonatorVolume());
    }

    @Test void invalidSoundSettingsFailWithoutReplacingFile() throws Exception {
        Path path = directory.resolve("client.properties");
        for (String setting : new String[]{"UseSounds=perhaps", "ResonatorTicking=perhaps",
                "ResonatorVolume=NaN", "ResonatorVolume=Infinity", "ResonatorVolume=-0.1"}) {
            Files.writeString(path, setting);
            assertThrows(IllegalStateException.class, () -> ClientConfig.load(path), setting);
            assertEquals(setting, Files.readString(path));
        }
        Files.writeString(path, "ResonatorVolume=0");
        assertEquals(0F, ClientConfig.load(path).resonatorVolume());
    }
}
