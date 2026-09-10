package com.aranaira.arcanearchives.integration.patchouli;

import com.aranaira.arcanearchives.recipe.CraftingCreator;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import com.google.gson.JsonParser;
import java.util.List;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GemCutterBookRecipesTest {
    @BeforeAll
    static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    private static GemCutterDataRecipe.Entry entry(String name, String extra, int count) {
        var id = ResourceLocation.tryParse("arcanearchives:" + name);
        var json = JsonParser.parseString("""
            {"inputs":[{"item":"minecraft:diamond","count":%d}],
             "result":{"item":"minecraft:paper","count":2}%s}
            """.formatted(count, extra)).getAsJsonObject();
        return new GemCutterDataRecipe.Entry(id, GemCutterDataRecipe.parse(id, json));
    }

    @Test
    void outputLookupUsesFirstEnabledItemMatchAndIgnoresCreatorAndCount() {
        var disabled = entry("disabled", ",\"enabled\":false", 1);
        var first = entry("first", "", 3);
        var second = entry("second", "", 7);
        ItemStack marked = CraftingCreator.withCreator(new ItemStack(Items.PAPER, 40), new UUID(0, 1), "Author");
        var recipe = GemCutterBookRecipes.byOutput(List.of(disabled, first, second), marked);
        assertNotNull(recipe);
        assertEquals(first.name(), recipe.getName());
        assertEquals(3, recipe.getIngredients().get(0).getCount());
        assertEquals(40, marked.getCount());
    }

    @Test
    void keyLookupUsesHolderIdentityRatherThanSerializedPlaceholder() {
        var original = entry("original", "", 4);
        var rebound = new GemCutterDataRecipe.Entry(ResourceLocation.tryParse("arcanearchives:rebound"), original.recipe());
        assertNull(GemCutterBookRecipes.byId(List.of(rebound), original.name()));
        assertEquals(rebound.name(), GemCutterBookRecipes.byId(List.of(rebound), rebound.name()).getName());
    }

    @Test
    void missingDisabledAndEmptyRequestsDoNotInventRecipes() {
        var disabled = entry("disabled", ",\"enabled\":false", 1);
        assertNull(GemCutterBookRecipes.byId(List.of(disabled), disabled.name()));
        assertNull(GemCutterBookRecipes.byOutput(List.of(disabled), new ItemStack(Items.PAPER)));
        assertNull(GemCutterBookRecipes.byOutput(List.of(entry("present", "", 1)), ItemStack.EMPTY));
        assertNull(GemCutterBookRecipes.byOutput(List.of(entry("present", "", 1)), new ItemStack(Items.GOLD_INGOT)));
        assertNull(GemCutterBookRecipes.byId(List.of(), disabled.name()));
    }

    @Test
    void displayCostsAndOutputsAreDetachedAndCountsAreNotClamped() {
        var entry = entry("large", "", 256);
        var recipe = GemCutterBookRecipes.byId(List.of(entry), entry.name());
        var costs = recipe.getIngredients().get(0).getMatchingStacksWithSizes();
        assertEquals(256, costs.get(0).getCount());
        costs.get(0).setCount(1);
        recipe.getRecipeOutput().setCount(1);
        var reread = GemCutterBookRecipes.byId(List.of(entry), entry.name());
        assertEquals(256, reread.getIngredients().get(0).getMatchingStacksWithSizes().get(0).getCount());
        assertEquals(2, reread.getRecipeOutput().getCount());
    }

    @Test
    void freshRecipeSnapshotsReplaceAndRemoveDisplayedDefinitions() {
        var old = entry("reload", "", 2);
        var replacement = entry("reload", "", 5);
        assertEquals(2, GemCutterBookRecipes.byId(List.of(old), old.name()).getIngredients().get(0).getCount());
        assertEquals(5, GemCutterBookRecipes.byId(List.of(replacement), old.name()).getIngredients().get(0).getCount());
        assertNull(GemCutterBookRecipes.byId(List.of(), old.name()));
    }

    @Test
    void hiveRecipePreviewDoesNotRequireOrForgePlayerAuthorization() {
        var entry = entry("invitation", ",\"hive\":\"invitation\",\"record_creator\":true", 3);
        assertFalse(entry.recipe().enabledFor(null));
        var preview = GemCutterBookRecipes.byOutput(List.of(entry), new ItemStack(Items.PAPER));
        assertNotNull(preview);
        assertTrue(ItemStack.matches(new ItemStack(Items.PAPER, 2), preview.getRecipeOutput()));
        assertFalse(entry.recipe().enabledFor(null));
    }
}
