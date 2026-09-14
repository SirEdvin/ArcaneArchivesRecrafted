package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.items.TomeOfArcanaItem;
import com.aranaira.arcanearchives.integration.patchouli.GemCutterBookRecipes;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.api.PatchouliAPI;

/** Inspect every actual dev-classpath book resource against the native registries/recipe manager. */
public final class TomeContentLifecycle {
    public static void run(ServerLevel level) {
        try {
            var anchor = TomeContentLifecycle.class.getResource(
                "/assets/arcanearchives/patchouli_books/tome_arcana/en_us/categories/blocks.json");
            require(anchor != null, "Missing native test book resources");
            Path root = Path.of(anchor.toURI()).getParent().getParent();
            int entries = 0;
            int recipes = 0;
            var bookItem = PatchouliAPI.get().getBookStack(TomeOfArcanaItem.BOOK);
            require(!bookItem.isEmpty(), "Patchouli did not register the Tome book definition");
            try (var paths = Files.walk(root)) {
                for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
                    var json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                    checkComponents(json);
                    if (!json.has("pages")) continue;
                    entries++;
                    require(json.getAsJsonArray("pages").size() > 0, "Empty Tome topic: " + path);
                    validItem(json.get("icon").getAsString());
                    for (var element : json.getAsJsonArray("pages")) {
                        var page = element.getAsJsonObject();
                        boolean enabled = !page.has("flag") || PatchouliAPI.get().getConfigFlag(page.get("flag").getAsString());
                        if (page.has("output")) {
                            recipes++;
                            var stack = validItem(page.get("output").getAsString());
                            if (enabled) require(GemCutterBookRecipes.byOutput(GemCutterDataRecipe.entries(level.getRecipeManager()), stack) != null,
                                "Tome output has no enabled native Gem Cutter recipe: " + stack);
                        }
                        for (String key : new String[]{"recipe", "recipe2"}) {
                            if (!page.has(key)) continue;
                            recipes++;
                            require(level.getRecipeManager().byKey(ResourceLocation.tryParse(page.get(key).getAsString())).isPresent(),
                                "Tome points to missing crafting recipe: " + page.get(key));
                        }
                    }
                }
            }
            require(entries == 53 && recipes == 43, "Incomplete native Tome resource traversal: " + entries + "/" + recipes);
        } catch (Exception exception) {
            throw new AssertionError("Native Tome content verification failed", exception);
        }
    }

    private static void checkComponents(JsonElement element) {
        if (element.isJsonArray()) {
            for (var child : element.getAsJsonArray()) checkComponents(child);
        } else if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has("type") && object.get("type").getAsString().equals("patchouli:item"))
                validItem(object.get("item").getAsString());
            for (var child : object.entrySet()) checkComponents(child.getValue());
        }
    }

    private static ItemStack validItem(String value) {
        var id = ResourceLocation.tryParse(value);
        require(id != null && BuiltInRegistries.ITEM.containsKey(id), "Unregistered Tome item: " + value);
        var stack = new ItemStack(BuiltInRegistries.ITEM.get(id));
        require(!stack.isEmpty(), "Empty Tome icon: " + value);
        return stack;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
