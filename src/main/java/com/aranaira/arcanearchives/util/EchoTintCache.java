package com.aranaira.arcanearchives.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Client-owned persistent Echo colors. No Minecraft client classes are loaded here. */
public final class EchoTintCache {
    private final Path directory;
    private final Set<String> inputs = new LinkedHashSet<>();
    private final Map<String, Integer> colors = new LinkedHashMap<>();

    public EchoTintCache(Path directory) throws IOException {
        this.directory = directory;
        for (String line : lines(directory.resolve("ores.txt"))) {
            if (line.isEmpty() || line.startsWith("#")) continue;
            inputs.add(identifier(line));
        }
        for (String line : lines(directory.resolve("echo_colors.txt"))) {
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] fields = line.split(",", -1);
            if (fields.length != 2) throw new IllegalArgumentException("Expected item ID,color: " + line);
            colors.put(identifier(fields[0].trim()), Integer.parseInt(fields[1].trim()));
        }
    }

    private static java.util.List<String> lines(Path path) throws IOException {
        return Files.exists(path) ? Files.readAllLines(path, StandardCharsets.UTF_8).stream().map(String::trim).toList()
            : java.util.List.of();
    }

    private static String identifier(String value) {
        if (!value.matches("[a-z0-9_.-]+:[a-z0-9/._-]+"))
            throw new IllegalArgumentException("Expected modern namespaced item ID: " + value);
        return value;
    }

    public Set<String> inputs() { return java.util.Collections.unmodifiableSet(inputs); }
    public boolean hasColors() { return !colors.isEmpty(); }
    public int color(String item) { return colors.getOrDefault(item, -1); }

    public void generateInputs(Set<String> generated) throws IOException {
        if (!inputs.isEmpty()) return;
        for (String item : generated) identifier(item);
        inputs.addAll(generated);
        Files.createDirectories(directory);
        var lines = new java.util.ArrayList<String>();
        lines.add("# Modern namespaced item IDs, one per line. Restart to apply. Empty list regenerates from smeltable ore tags.");
        lines.addAll(generated);
        Files.write(directory.resolve("ores.txt"), lines, StandardCharsets.UTF_8);
    }

    public void generateColors(Map<String, Integer> generated) throws IOException {
        if (hasColors()) return;
        for (String item : generated.keySet()) identifier(item);
        colors.putAll(generated);
        Files.createDirectories(directory);
        Files.write(directory.resolve("echo_colors.txt"), generated.entrySet().stream()
            .map(entry -> entry.getKey() + "," + entry.getValue()).toList(), StandardCharsets.UTF_8);
    }

    /** Null means unavailable; opaque white (-1) is a valid sampled, persistable color. */
    public static Integer sample(BufferedImage image) {
        if (image == null || image.getRaster().getNumBands() != 3) return null;
        int red = 0, green = 0, blue = 0, count = 0;
        for (int x = 6; x <= 9 && x < image.getWidth(); x++) {
            for (int y = 6; y <= 9 && y < image.getHeight(); y++) {
                int[] pixel = image.getRaster().getPixel(x, y, (int[]) null);
                red += pixel[0];
                green += pixel[1];
                blue += pixel[2];
                count++;
            }
        }
        return count == 0 ? null : Integer.valueOf(0xff000000 | (red / count << 16) | (green / count << 8) | blue / count);
    }
}
