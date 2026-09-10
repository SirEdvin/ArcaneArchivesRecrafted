package com.aranaira.arcanearchives.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/** Local presentation settings; never sent to the server. Restart to apply. */
public record ClientConfig(boolean usePrettyGUIs) {
    private static ClientConfig current = new ClientConfig(true);
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
                try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                    defaults.store(writer, "Arcane Archives client presentation; restart to apply.");
                }
            }
            Properties values = new Properties();
            try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) { values.load(reader); }
            String value = values.getProperty("UsePrettyGUIs", "true");
            if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))
                throw new IllegalArgumentException("UsePrettyGUIs must be true or false");
            return new ClientConfig(Boolean.parseBoolean(value));
        } catch (IOException | IllegalArgumentException error) {
            throw new IllegalStateException("Cannot load " + path + "; the file was not replaced", error);
        }
    }
}
