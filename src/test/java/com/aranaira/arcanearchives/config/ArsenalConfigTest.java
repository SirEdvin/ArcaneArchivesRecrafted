package com.aranaira.arcanearchives.config;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ArsenalConfigTest {
    @TempDir Path directory;
    @Test void defaultsOnButPreservesExplicitOptOutAndInvalidFiles() throws Exception {
        var original = ArsenalConfig.current();
        Path file = directory.resolve("arcanearchives/arsenal.properties");
        try {
            ArsenalConfig.initialize(directory);
            assertTrue(ArsenalConfig.current().enableArsenal());
            assertFalse(ArsenalConfig.current().colourblindMode());
            Files.writeString(file, "ColourblindMode=true\n");
            ArsenalConfig.initialize(directory);
            assertTrue(ArsenalConfig.current().enableArsenal());
            assertTrue(ArsenalConfig.current().colourblindMode());
            Files.writeString(file, "EnableArsenal=false\n");
            ArsenalConfig.initialize(directory);
            assertFalse(ArsenalConfig.current().enableArsenal());
            Files.writeString(file, "EnableArsenal=invalid\n");
            assertThrows(IllegalStateException.class, () -> ArsenalConfig.initialize(directory));
            assertEquals("EnableArsenal=invalid\n", Files.readString(file));
        } finally {
            Files.writeString(file, "EnableArsenal=" + original.enableArsenal() + "\nColourblindMode=" + original.colourblindMode());
            ArsenalConfig.initialize(directory);
        }
    }
}
