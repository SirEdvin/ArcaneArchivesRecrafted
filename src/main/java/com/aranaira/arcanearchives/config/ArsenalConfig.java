package com.aranaira.arcanearchives.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/** Arsenal gameplay setting and local accessibility preference. Restart to apply. */
public record ArsenalConfig(boolean enableArsenal, boolean colourblindMode) {
    private static ArsenalConfig current = new ArsenalConfig(true, false);
    public static ArsenalConfig current() { return current; }
    public static void initialize(Path directory) {
        MunchstoneConfig.initialize(directory);
        Path path = directory.resolve("arcanearchives/arsenal.properties");
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Properties defaults = new Properties();
                defaults.setProperty("EnableArsenal", "true");
                defaults.setProperty("ColourblindMode", "false");
                try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                    defaults.store(writer, "Arcane Archives Arsenal; enabled by default. Restart to apply.");
                }
            }
            Properties values = new Properties();
            try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); }
            current = new ArsenalConfig(bool(values, "EnableArsenal"), bool(values, "ColourblindMode"));
        } catch (IOException | IllegalArgumentException error) {
            throw new IllegalStateException("Cannot load " + path + "; the file was not replaced", error);
        }
    }
    private static boolean bool(Properties values, String key) {
        String value = values.getProperty(key, key.equals("EnableArsenal") ? "true" : "false");
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) throw new IllegalArgumentException(key + " must be true or false");
        return Boolean.parseBoolean(value);
    }
}
