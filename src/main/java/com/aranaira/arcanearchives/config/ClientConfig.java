package com.aranaira.arcanearchives.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/** Local presentation settings; never sent to the server. Restart to apply. */
public record ClientConfig(boolean usePrettyGUIs, boolean useSounds, boolean resonatorTicking, float resonatorVolume) {
    private static ClientConfig current = new ClientConfig(true, true, true, .15F);
    public static ClientConfig current() { return current; }

    public static void initialize(Path directory) {
        current = load(directory.resolve("arcanearchives/client.properties"));
    }

    static ClientConfig load(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Properties defaults = new Properties();
                defaults.setProperty("UsePrettyGUIs", "true");
                defaults.setProperty("UseSounds", "true");
                defaults.setProperty("ResonatorTicking", "true");
                defaults.setProperty("ResonatorVolume", "0.15");
                try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                    defaults.store(writer, "Arcane Archives client presentation; restart to apply.");
                }
            }
            Properties values = new Properties();
            try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); }
            float volume = Float.parseFloat(values.getProperty("ResonatorVolume", "0.15"));
            if (!Float.isFinite(volume) || volume < 0F)
                throw new IllegalArgumentException("ResonatorVolume must be finite and nonnegative");
            return new ClientConfig(flag(values, "UsePrettyGUIs"), flag(values, "UseSounds"),
                flag(values, "ResonatorTicking"), volume);
        } catch (IOException | IllegalArgumentException error) {
            throw new IllegalStateException("Cannot load " + path + "; the file was not replaced", error);
        }
    }

    private static boolean flag(Properties values, String key) {
        String value = values.getProperty(key, "true");
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))
            throw new IllegalArgumentException(key + " must be true or false");
        return Boolean.parseBoolean(value);
    }
}
