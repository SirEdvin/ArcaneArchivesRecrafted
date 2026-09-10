package com.aranaira.arcanearchives.config;

import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Original ordered block-ID/feed entries, loaded at startup; absent optional blocks are inert. */
public final class MunchstoneConfig {
    private static Map<String, Integer> values = Map.of();
    private MunchstoneConfig() {}
    public static int food(String id) { return values.getOrDefault(id, 0); }
    public static void initialize(Path directory) {
        Path path = directory.resolve("arcanearchives/munchstone.json");
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                try (var defaults = MunchstoneConfig.class.getResourceAsStream("/data/arcanearchives/munchstone_defaults.json")) {
                    if (defaults == null) throw new IllegalStateException("Missing Munchstone defaults");
                    Files.copy(defaults, path);
                }
            }
            Map<String, Integer> parsed = new LinkedHashMap<>();
            for (var value : JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonArray()) {
                String[] entry = value.getAsString().split(",", -1);
                if (entry.length != 2) continue;
                String id = entry[0].trim();
                if (!id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) continue;
                try {
                    int food = Integer.parseInt(entry[1].trim());
                    if (food > 0 && !id.equals("minecraft:air")) parsed.putIfAbsent(id, food);
                } catch (NumberFormatException ignored) {}
            }
            values = Map.copyOf(parsed);
        } catch (java.io.IOException | RuntimeException error) {
            throw new IllegalStateException("Cannot load " + path + "; the file was not replaced", error);
        }
    }
}
