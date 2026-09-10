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
}
