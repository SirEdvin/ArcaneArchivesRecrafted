package com.aranaira.arcanearchives.client;

import java.util.Objects;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Search lifetime without linkage to optional viewer classes outside their plugins. */
public final class ManifestSearch {
    private record Filter(Supplier<String> read, Consumer<String> write) {}
    private static volatile Filter current;
    private static volatile Filter emi;
    private static Filter preferred() { return emi != null ? emi : current; }
    private static String previousQuery = "";
    private final Filter filter = preferred();
    private final String originalJei = filter == null ? "" : filter.read.get();
    private String query;
    private boolean synchronize;
    private boolean closed;

    public static void bind(Supplier<String> read, Consumer<String> write) {
        current = new Filter(Objects.requireNonNull(read), Objects.requireNonNull(write));
    }
    public static void unbind() { current = null; }
    public static void bindEmi(Supplier<String> read, Consumer<String> write) {
        emi = new Filter(Objects.requireNonNull(read), Objects.requireNonNull(write));
    }
    public static void unbindEmi() { emi = null; }
    public static boolean matchesEnchantment(net.minecraft.world.item.ItemStack stack, String query) {
        if (query.startsWith("@") || !stack.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)) return false;
        //? if >=1.21 {
        var enchantments = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentsForCrafting(stack);
        for (var entry : enchantments.entrySet()) {
            var name = net.minecraft.world.item.enchantment.Enchantment.getFullname(entry.getKey(), entry.getIntValue());
            if (matchesEnchantmentName(query, name.getString())) return true;
        }
        //?} else {
        /*for (var entry : net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantments(stack).entrySet()) {
            if (entry.getKey() != null && matchesEnchantmentName(query,
                    entry.getKey().getFullname(entry.getValue()).getString())) return true;
        }
        *///?}
        return false;
    }
    static boolean matchesEnchantmentName(String query, String name) {
        return !query.startsWith("@") && name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    static boolean matches(String query, String displayName, String path, String modName, String namespace) {
        String filter = query.toLowerCase(Locale.ROOT);
        return filter.startsWith("@")
            ? modName.replace(" ", "").toLowerCase(Locale.ROOT).contains(filter.substring(1))
                || namespace.toLowerCase(Locale.ROOT).contains(filter.substring(1))
            : displayName.toLowerCase(Locale.ROOT).contains(filter)
                || path.toLowerCase(Locale.ROOT).contains(filter);
    }

    public ManifestSearch(boolean persist, boolean synchronize) {
        query = persist ? previousQuery : "";
        this.synchronize = synchronize;
    }
    public String query() { return query; }
    public boolean closed() { return closed; }
    public boolean available() { return !closed && filter != null && filter == preferred(); }
    public boolean synchronizing() { return synchronize; }
    public void edit(String query) {
        if (closed) return;
        this.query = Objects.requireNonNull(query);
        if (synchronize && available()) filter.write.accept(query);
    }
    public Optional<String> copyFromJei() {
        return available() ? Optional.of(filter.read.get()) : Optional.empty();
    }
    public void toggle() {
        if (!available()) return;
        synchronize = !synchronize;
        if (synchronize) filter.write.accept(query);
    }
    public void close() {
        if (closed) return;
        try {
            // Preserve upstream's conditions: enabled on close and nonempty saved query.
            if (synchronize && available() && !originalJei.isEmpty()) filter.write.accept(originalJei);
        } finally {
            previousQuery = query;
            closed = true;
        }
    }
}
