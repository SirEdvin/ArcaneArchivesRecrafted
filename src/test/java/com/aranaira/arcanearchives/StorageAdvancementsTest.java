package com.aranaira.arcanearchives;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import org.junit.jupiter.api.Test;

class StorageAdvancementsTest {
    @Test void allStorageAdvancementsDecodeWithNativeRegisteredItemsAndClosedParents() throws Exception {
        var names = Set.of("monitoring_crystal", "amphora", "chest", "containment_field", "devouring_charm", "gemcutters_table",
            "material_interface", "matrix_brace", "raw_quartz", "raw_quartz_cluster", "resonator", "root",
            "scepter_manipulation", "scepter_revelation", "shaped_quartz_block", "slivers", "tank", "trove", "workbench");
        var ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        for (String name : names) {
            try (var stream = getClass().getResourceAsStream("/data/arcanearchives/advancement/" + name + ".json")) {
                assertNotNull(stream, name);
                var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                var advancement = Advancement.CODEC.parse(ops, json).getOrThrow();
                assertFalse(advancement.display().orElseThrow().getIcon().isEmpty(), name);
                assertEquals(1, advancement.criteria().size(), name);
                if (name.equals("root")) assertTrue(advancement.isRoot());
                else assertTrue(names.contains(advancement.parent().orElseThrow().getPath()), name);
            }
        }
    }
}
*///?}
