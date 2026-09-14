package com.aranaira.arcanearchives.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/** Client preferences; storage controls and Manifest distance are sent to the server. Restart to apply. */
public record ClientConfig(boolean usePrettyGUIs, boolean useSounds, boolean resonatorTicking, float resonatorVolume,
                           boolean manifestSearchTermPersistence, boolean manifestJeiSynchronise,
                           int manifestMaxDistance, boolean disableManifestGrid, boolean manifestPresence, boolean manifestHoldShift,
                           boolean trovesDispense, boolean defaultRoutingNoNewItems) {
    private static ClientConfig current = new ClientConfig(true, true, true, .15F, false, false, 100, true, true, true, true, false);
    public ClientConfig {
        if (manifestMaxDistance < 0) throw new IllegalArgumentException("ManifestMaxDistance must be nonnegative");
    }
    public static ClientConfig current() { return current; }

    /** Preserve the original click handler, not its contradictory configuration comment. */
    public boolean closeManifestAfterSelection(int button, boolean shiftDown) {
        return button == 0 && manifestHoldShift != shiftDown;
    }

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
                defaults.setProperty("ManifestSearchTermPersistence", "false");
                defaults.setProperty("ManifestJeiSynchronise", "false");
                defaults.setProperty("ManifestMaxDistance", "100");
                defaults.setProperty("DisableManifestGrid", "true");
                defaults.setProperty("ManifestPresence", "true");
                defaults.setProperty("ManifestHoldShift", "true");
                defaults.setProperty("TrovesDispense", "true");
                defaults.setProperty("DefaultRoutingNoNewItems", "false");
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
                flag(values, "ResonatorTicking"), volume, flag(values, "ManifestSearchTermPersistence", false),
                flag(values, "ManifestJeiSynchronise", false), Integer.parseInt(values.getProperty("ManifestMaxDistance", "100")),
                flag(values, "DisableManifestGrid"), flag(values, "ManifestPresence"), flag(values, "ManifestHoldShift"),
                flag(values, "TrovesDispense"), flag(values, "DefaultRoutingNoNewItems", false));
        } catch (IOException | IllegalArgumentException error) {
            throw new IllegalStateException("Cannot load " + path + "; the file was not replaced", error);
        }
    }

    private static boolean flag(Properties values, String key) {
        return flag(values, key, true);
    }

    private static boolean flag(Properties values, String key, boolean fallback) {
        String value = values.getProperty(key, Boolean.toString(fallback));
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))
            throw new IllegalArgumentException(key + " must be true or false");
        return Boolean.parseBoolean(value);
    }
}
