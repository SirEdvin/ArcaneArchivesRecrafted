package com.aranaira.arcanearchives.integration.patchouli;

import com.aranaira.arcanearchives.recipe.gct.GCTRecipe;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Read-only book lookup. Deliberately not the catalog's data-sensitive crafting lookup. */
public final class GemCutterBookRecipes {
    private GemCutterBookRecipes() {}

    public static GCTRecipe byOutput(List<GemCutterDataRecipe.Entry> entries, ItemStack output) {
        Objects.requireNonNull(output, "output");
        if (output.isEmpty()) return null;
        for (var entry : entries) {
            if (!entry.recipe().enabled()) continue;
            var definition = entry.recipe().definition(entry.name());
            var candidate = definition.getRecipeOutput();
            // Legacy ItemStack.areItemsEqual checks item/metadata, not count or arbitrary NBT.
            if (output.is(candidate.getItem()) && output.getDamageValue() == candidate.getDamageValue())
                return definition;
        }
        return null;
    }

    public static GCTRecipe byId(List<GemCutterDataRecipe.Entry> entries, ResourceLocation id) {
        Objects.requireNonNull(id, "id");
        for (var entry : entries) {
            if (entry.name().equals(id) && entry.recipe().enabled())
                return entry.recipe().definition(entry.name());
        }
        return null;
    }
}
