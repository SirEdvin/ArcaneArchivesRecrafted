package com.aranaira.arcanearchives.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/** Server settings from the pinned upstream ServerSideConfig; no client can mutate these values. */
public record ServerSideConfig(int resonatorLimit, int resonatorTickTime, int radiantMultiplier,
                               boolean bookFromBookshelf, boolean bookFromResonator,
                               int sliverClusterChance, int sliverSingleChance, int sliverMinimum, int sliverMaximum,
                               boolean inWorldChestConversion) {
    public ServerSideConfig(int resonatorLimit, int resonatorTickTime, int radiantMultiplier,
                            boolean bookFromBookshelf, boolean bookFromResonator,
                            int sliverClusterChance, int sliverSingleChance, int sliverMinimum, int sliverMaximum) {
        this(resonatorLimit, resonatorTickTime, radiantMultiplier, bookFromBookshelf, bookFromResonator,
            sliverClusterChance, sliverSingleChance, sliverMinimum, sliverMaximum, true);
    }
    public ServerSideConfig(int resonatorLimit, int resonatorTickTime, int radiantMultiplier,
                            boolean bookFromBookshelf, boolean bookFromResonator) {
        this(resonatorLimit, resonatorTickTime, radiantMultiplier, bookFromBookshelf, bookFromResonator, 20, 40, 8, 24);
    }
    public static final ServerSideConfig DEFAULTS = new ServerSideConfig(3, 6000, 4, true, true);
    private static ServerSideConfig current = DEFAULTS;

    public ServerSideConfig {
        if (sliverClusterChance < 0 || sliverClusterChance > 100 || sliverSingleChance < 0 || sliverSingleChance > 100
            || sliverMinimum < 1 || sliverMaximum < sliverMinimum || sliverMaximum > 64) {
            throw new IllegalArgumentException("Sliver chances must be 0–100 and cluster bounds 1 <= minimum <= maximum <= 64");
        }
        // This is the range specified by upstream's @Config.RangeInt, not a new capacity limit.
        if (radiantMultiplier < 1 || radiantMultiplier > 8) {
            throw new IllegalArgumentException("RadiantMultiplier must be between 1 and 8");
        }
    }

    public static ServerSideConfig current() {
        return current;
    }

    public static void initialize(Path configDirectory) {
        ArsenalConfig.initialize(configDirectory);
        Path path = configDirectory.resolve("arcanearchives/server.properties");
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
                    DEFAULTS.toProperties().store(writer, "Arcane Archives server settings; resonator time is in ticks");
                }
            }
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                Properties properties = new Properties();
                properties.load(reader);
                current = fromProperties(properties);
            }
        } catch (IOException | IllegalArgumentException exception) {
            // Never overwrite a user's invalid file with defaults, especially storage-capacity settings.
            throw new IllegalStateException("Cannot load " + path + "; the file was not replaced", exception);
        }
    }

    public static ServerSideConfig fromProperties(Properties properties) {
        return new ServerSideConfig(
            Integer.parseInt(properties.getProperty("ResonatorLimit", "3")),
            Integer.parseInt(properties.getProperty("ResonatorTickTime", "6000")),
            Integer.parseInt(properties.getProperty("RadiantMultiplier", "4")),
            bool(properties, "BookFromBookshelf"), bool(properties, "BookFromResonator"),
            Integer.parseInt(properties.getProperty("ChanceForSliverCluster", "20")),
            Integer.parseInt(properties.getProperty("ChanceForSliverSingle", "40")),
            Integer.parseInt(properties.getProperty("AmountGeneratedOnSliverClusterMinimum", "8")),
            Integer.parseInt(properties.getProperty("AmountGeneratedOnSliverClusterMaximum", "24")),
            bool(properties, "InWorldChestConversion"));
    }

    private static boolean bool(Properties properties, String key) {
        String value = properties.getProperty(key, "true");
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException(key + " must be true or false");
        }
        return Boolean.parseBoolean(value);
    }

    public Properties toProperties() {
        Properties properties = new Properties();
        properties.setProperty("ResonatorLimit", Integer.toString(resonatorLimit));
        properties.setProperty("ResonatorTickTime", Integer.toString(resonatorTickTime));
        properties.setProperty("RadiantMultiplier", Integer.toString(radiantMultiplier));
        properties.setProperty("BookFromBookshelf", Boolean.toString(bookFromBookshelf));
        properties.setProperty("BookFromResonator", Boolean.toString(bookFromResonator));
        properties.setProperty("InWorldChestConversion", Boolean.toString(inWorldChestConversion));
        properties.setProperty("ChanceForSliverCluster", Integer.toString(sliverClusterChance));
        properties.setProperty("ChanceForSliverSingle", Integer.toString(sliverSingleChance));
        properties.setProperty("AmountGeneratedOnSliverClusterMinimum", Integer.toString(sliverMinimum));
        properties.setProperty("AmountGeneratedOnSliverClusterMaximum", Integer.toString(sliverMaximum));
        return properties;
    }
}
