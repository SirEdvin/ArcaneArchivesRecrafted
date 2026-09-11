package com.aranaira.arcanearchives.config;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClientConfigTest {
    @TempDir Path directory;

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
