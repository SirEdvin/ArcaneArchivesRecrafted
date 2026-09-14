package com.aranaira.arcanearchives.integration.emi;

import com.aranaira.arcanearchives.ArcaneArchivesMod;
import com.aranaira.arcanearchives.config.ArsenalConfig;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.integration.ResonatorDisplay;
import com.aranaira.arcanearchives.integration.ViewerHiddenItems;
import com.aranaira.arcanearchives.inventory.RadiantCraftingMenu;
import com.aranaira.arcanearchives.recipe.gct.GCTRecipe;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

/** Optional recipe display and standard EMI crafting-grid integration. */
@EmiEntrypoint
public final class ArcaneArchivesEmi implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        com.aranaira.arcanearchives.client.ManifestSearch.bindEmi(
            dev.emi.emi.api.EmiApi::getSearchText, dev.emi.emi.api.EmiApi::setSearchText);
        var hidden = ViewerHiddenItems.items(ArsenalConfig.current().enableArsenal());
        registry.removeEmiStacks(stack -> hidden.contains(stack.getItemStack().getItem()));
        registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(ContentRegistry.RADIANT_CRAFTING_TABLE_ITEM.get()));
        registry.addRecipeHandler(ContentRegistry.RADIANT_CRAFTING_TABLE_MENU.get(), new CraftingTransfer());
        var resonator = EmiStack.of(ContentRegistry.RADIANT_RESONATOR_ITEM.get());
        var resonating = new EmiRecipeCategory(ResonatorDisplay.ID, resonator);
        registry.addCategory(resonating);
        registry.addWorkstation(resonating, resonator);
        registry.addRecipe(new Resonating(resonating));
        var workstation = EmiStack.of(ContentRegistry.GEMCUTTERS_TABLE_ITEM.get());
        var category = new EmiRecipeCategory(ArcaneArchivesMod.id("gem_cutting"), workstation) {
            @Override
            public Component getName() {
                return Component.translatable("block.arcanearchives.gemcutters_table");
            }
        };
        registry.addCategory(category);
        registry.addWorkstation(category, workstation);
        for (var entry : GemCutterDataRecipe.entries(registry.getRecipeManager())) {
            if (entry.recipe().enabled() && !hidden.contains(entry.recipe().definition(entry.name()).getRecipeOutput().getItem()))
                registry.addRecipe(new Display(category, entry.recipe().definition(entry.name())));
        }
    }

    private record Resonating(EmiRecipeCategory getCategory) implements EmiRecipe {
        @Override public ResourceLocation getId() { return ResonatorDisplay.RECIPE_ID; }
        @Override public List<EmiIngredient> getInputs() { return List.of(); }
        @Override public List<EmiStack> getOutputs() { return List.of(EmiStack.of(ResonatorDisplay.INSTANCE.output())); }
        @Override public boolean supportsRecipeTree() { return false; }
        @Override public int getDisplayWidth() { return 162; }
        @Override public int getDisplayHeight() { return 90; }
        @Override public void addWidgets(WidgetHolder widgets) {
            widgets.addTexture(ResonatorDisplay.TEXTURE, 70, 0, 22, 62, 0, 0);
            widgets.addDrawable(0, 75, 162, 10, (graphics, mouseX, mouseY, delta) -> {
                var font = net.minecraft.client.Minecraft.getInstance().font;
                String interval = ResonatorDisplay.INSTANCE.interval();
                graphics.drawString(font, interval, (162 - font.width(interval)) / 2, 0, 0xFF000000, false);
            });
        }
    }

    private static final class CraftingTransfer implements StandardRecipeHandler<RadiantCraftingMenu> {
        // Match EMI's native crafting-table ranges; output and bookmark ghosts are not ingredient sources.
        @Override public List<Slot> getInputSources(RadiantCraftingMenu menu) {
            return new ArrayList<>(menu.slots.subList(1, 46));
        }
        @Override public List<Slot> getCraftingSlots(RadiantCraftingMenu menu) {
            return new ArrayList<>(menu.slots.subList(1, 10));
        }
        @Override public Slot getOutputSlot(RadiantCraftingMenu menu) { return menu.getSlot(0); }
        @Override public boolean supportsRecipe(EmiRecipe recipe) {
            return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree();
        }
    }

    public static final class Display implements EmiRecipe {
        private final EmiRecipeCategory category;
        private final ResourceLocation id;
        private final List<EmiIngredient> inputs;
        private final List<EmiStack> outputs;

        public Display(EmiRecipeCategory category, GCTRecipe recipe) {
            this.category = category;
            id = recipe.getName();
            inputs = recipe.getIngredients().stream().map(cost -> EmiIngredient.of(
                cost.getMatchingStacksWithSizes().stream().map(EmiStack::of).toList(), cost.getCount())).toList();
            outputs = List.of(EmiStack.of(recipe.getRecipeOutput()));
        }

        @Override public EmiRecipeCategory getCategory() { return category; }
        @Override public ResourceLocation getId() { return id; }
        @Override public List<EmiIngredient> getInputs() { return inputs; }
        @Override public List<EmiStack> getOutputs() { return outputs; }
        @Override public int getDisplayWidth() { return 162; }
        @Override public int getDisplayHeight() { return Math.max(1, (inputs.size() + 8) / 9) * 18 + 30; }

        @Override
        public void addWidgets(WidgetHolder widgets) {
            for (int i = 0; i < inputs.size(); i++)
                widgets.addSlot(inputs.get(i), i % 9 * 18, i / 9 * 18);
            widgets.addSlot(outputs.get(0), 68, getDisplayHeight() - 26).large(true).recipeContext(this);
        }
    }
}
