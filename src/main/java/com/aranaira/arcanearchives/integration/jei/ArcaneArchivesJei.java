package com.aranaira.arcanearchives.integration.jei;

import com.aranaira.arcanearchives.ArcaneArchivesMod;
import com.aranaira.arcanearchives.config.ArsenalConfig;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.integration.ResonatorDisplay;
import com.aranaira.arcanearchives.integration.ViewerHiddenItems;
import com.aranaira.arcanearchives.inventory.RadiantCraftingMenu;
import com.aranaira.arcanearchives.recipe.gct.GCTRecipe;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Optional recipe display and native JEI grid filling; never grants crafted output. */
@JeiPlugin
public final class ArcaneArchivesJei implements IModPlugin {
    private static final RecipeType<GCTRecipe> TYPE = new RecipeType<>(ArcaneArchivesMod.id("gem_cutting"), GCTRecipe.class);
    private static final RecipeType<ResonatorDisplay> RESONATING = new RecipeType<>(ResonatorDisplay.ID, ResonatorDisplay.class);

    @Override public ResourceLocation getPluginUid() { return ArcaneArchivesMod.id("jei"); }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        var hidden = ViewerHiddenItems.items(ArsenalConfig.current().enableArsenal());
        var ingredients = runtime.getIngredientManager();
        var removals = ingredients.getAllIngredients(VanillaTypes.ITEM_STACK).stream()
            .filter(stack -> hidden.contains(stack.getItem())).toList();
        if (!removals.isEmpty()) ingredients.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, removals);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new Category(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ResonatorCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ContentRegistry.GEMCUTTERS_TABLE_ITEM.get()), TYPE);
        registration.addRecipeCatalyst(new ItemStack(ContentRegistry.RADIANT_RESONATOR_ITEM.get()), RESONATING);
        registration.addRecipeCatalyst(new ItemStack(ContentRegistry.RADIANT_CRAFTING_TABLE_ITEM.get()), RecipeTypes.CRAFTING);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // Native grid 1..9 and player inventory 10..45; exclude output 0 and bookmark ghosts 46..48.
        registration.addRecipeTransferHandler(RadiantCraftingMenu.class, ContentRegistry.RADIANT_CRAFTING_TABLE_MENU.get(),
            RecipeTypes.CRAFTING, 1, 9, 10, 36);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(RESONATING, java.util.List.of(ResonatorDisplay.INSTANCE));
        var level = Minecraft.getInstance().level;
        if (level == null) return;
        registration.addRecipes(TYPE, GemCutterDataRecipe.entries(level.getRecipeManager()).stream()
            .filter(entry -> entry.recipe().enabled())
            .map(entry -> entry.recipe().definition(entry.name())).toList());
    }

    public static final class ResonatorCategory implements IRecipeCategory<ResonatorDisplay> {
        private final IDrawable background;
        private final IDrawable icon;
        private final IDrawable diagram;

        public ResonatorCategory(IGuiHelper gui) {
            background = gui.createBlankDrawable(162, 90);
            icon = gui.createDrawableItemStack(new ItemStack(ContentRegistry.RADIANT_RESONATOR_ITEM.get()));
            diagram = gui.createDrawable(ResonatorDisplay.TEXTURE, 0, 0, 22, 62);
        }
        @Override public RecipeType<ResonatorDisplay> getRecipeType() { return RESONATING; }
        @Override public Component getTitle() { return Component.translatable("block.arcanearchives.radiant_resonator"); }
        @Override public IDrawable getBackground() { return background; }
        @Override public IDrawable getIcon() { return icon; }
        @Override public ResourceLocation getRegistryName(ResonatorDisplay recipe) { return ResonatorDisplay.RECIPE_ID; }
        @Override public void setRecipe(IRecipeLayoutBuilder builder, ResonatorDisplay recipe, IFocusGroup focuses) {
            builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(recipe.output());
        }
        @Override public void draw(ResonatorDisplay recipe, mezz.jei.api.gui.ingredient.IRecipeSlotsView slots,
                                   net.minecraft.client.gui.GuiGraphics graphics, double mouseX, double mouseY) {
            diagram.draw(graphics, 70, 0);
            var font = Minecraft.getInstance().font;
            String interval = recipe.interval();
            graphics.drawString(font, interval, (162 - font.width(interval)) / 2, 75, 0xFF000000, false);
        }
    }

    public static final class Category implements IRecipeCategory<GCTRecipe> {
        private final IDrawable background;
        private final IDrawable icon;
        private final IDrawable slot;

        public Category(IGuiHelper gui) {
            background = gui.createBlankDrawable(162, 138);
            icon = gui.createDrawableItemStack(new ItemStack(ContentRegistry.GEMCUTTERS_TABLE_ITEM.get()));
            slot = gui.getSlotDrawable();
        }

        @Override public RecipeType<GCTRecipe> getRecipeType() { return TYPE; }
        @Override public Component getTitle() { return Component.translatable("block.arcanearchives.gemcutters_table"); }
        @Override public IDrawable getBackground() { return background; }
        @Override public IDrawable getIcon() { return icon; }
        @Override public ResourceLocation getRegistryName(GCTRecipe recipe) { return recipe.getName(); }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, GCTRecipe recipe, IFocusGroup focuses) {
            for (int i = 0; i < recipe.getIngredients().size(); i++) {
                builder.addSlot(RecipeIngredientRole.INPUT, i % 9 * 18 + 1, i / 9 * 18 + 1)
                    .setBackground(slot, -1, -1)
                    .addItemStacks(recipe.getIngredients().get(i).getMatchingStacksWithSizes());
            }
            builder.addSlot(RecipeIngredientRole.OUTPUT, 73, 119).setBackground(slot, -1, -1)
                .addItemStack(recipe.getRecipeOutput());
        }
    }
}
