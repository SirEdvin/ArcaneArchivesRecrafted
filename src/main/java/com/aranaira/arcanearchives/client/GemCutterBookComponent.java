package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.integration.patchouli.GemCutterBookRecipes;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import java.util.function.UnaryOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.IVariable;

/** Patchouli API-only custom component; instantiated only by client book templates. */
public final class GemCutterBookComponent implements ICustomComponent {
    private static final ResourceLocation BACKGROUND = ResourceLocation.tryParse(
        "arcanearchives:textures/gui/guidebook/recipe_gct.png");
    private static final float SCALE = 0.85F;
    // Patchouli deserializes these template variables. All runtime state must remain transient.
    private String recipe;
    private String output;
    private transient ResourceLocation recipeId;
    private transient ItemStack targetOutput;
    private transient int x;
    private transient int y;

    @Override
    //? if >=1.21 {
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup, net.minecraft.core.HolderLookup.Provider registries) {
    //?} else {
    /*public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
    *///?}
        if ((recipe == null) == (output == null))
            throw new IllegalArgumentException("Gem Cutter book component needs exactly one recipe or output selector");
        recipeId = null;
        targetOutput = ItemStack.EMPTY;
        if (recipe != null) {
            //? if >=1.21 {
            String name = lookup.apply(IVariable.wrap(recipe, registries)).asString();
            //?} else {
            /*String name = lookup.apply(IVariable.wrap(recipe)).asString();
            *///?}
            recipeId = name.contains(":") ? ResourceLocation.tryParse(name) : null;
            if (recipeId == null) throw new IllegalArgumentException("Invalid Gem Cutter recipe identifier: " + name);
        } else {
            //? if >=1.21 {
            targetOutput = lookup.apply(IVariable.wrap(output, registries)).as(ItemStack.class).copy();
            //?} else {
            /*targetOutput = lookup.apply(IVariable.wrap(output)).as(ItemStack.class).copy();
            *///?}
        }
    }

    @Override
    public void build(int componentX, int componentY, int pageNum) {
        x = componentX;
        y = componentY;
    }

    @Override
    public void render(GuiGraphics graphics, IComponentRenderContext context, float pticks, int mouseX, int mouseY) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        // Resolve current synchronized recipes and live tag alternatives, never stale per-book copies.
        var entries = GemCutterDataRecipe.entries(level.getRecipeManager());
        var definition = resolve(entries);
        if (definition == null) return;
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(SCALE, SCALE, 1);
            graphics.blit(BACKGROUND, 0, 0, 0, 0, 127, 44, 256, 256);
        } finally {
            graphics.pose().popPose();
        }
        var ingredients = definition.getIngredients();
        for (int slot = 0; slot < ingredients.size(); slot++) {
            var alternatives = ingredients.get(slot).getMatchingStacksWithSizes();
            if (!alternatives.isEmpty()) {
                var stack = alternatives.get(Math.floorMod(context.getTicksInBook() / 20, alternatives.size()));
                renderItem(graphics, context, mouseX, mouseY, slot % 4 * 22 + 3, slot / 4 * 22 + 3, stack);
            }
        }
        renderItem(graphics, context, mouseX, mouseY, 107, 14, definition.getRecipeOutput());
    }

    private void renderItem(GuiGraphics graphics, IComponentRenderContext context, int mouseX, int mouseY,
            int itemX, int itemY, ItemStack stack) {
        int drawX = x + Math.round(itemX * SCALE);
        int drawY = y + Math.round(itemY * SCALE);
        // Patchouli subtracts the book origin itself. Keep its mouse coordinates unchanged,
        // and scale artwork around the page-local item position rather than scaling the mouse.
        boolean hovered = context.isAreaHovered(mouseX, mouseY, drawX, drawY,
            Math.round(16 * SCALE), Math.round(16 * SCALE));
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(drawX, drawY, 0);
            graphics.pose().scale(SCALE, SCALE, 1);
            graphics.pose().translate(-drawX, -drawY, 0);
            context.renderItemStack(graphics, drawX, drawY, hovered ? mouseX : Integer.MIN_VALUE,
                hovered ? mouseY : Integer.MIN_VALUE, stack);
        } finally {
            graphics.pose().popPose();
        }
    }

    com.aranaira.arcanearchives.recipe.gct.GCTRecipe resolve(java.util.List<GemCutterDataRecipe.Entry> entries) {
        return recipeId == null ? GemCutterBookRecipes.byOutput(entries, targetOutput)
            : GemCutterBookRecipes.byId(entries, recipeId);
    }
}
