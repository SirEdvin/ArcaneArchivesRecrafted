package com.aranaira.arcanearchives.items;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.junit.jupiter.api.Test;

class UnimplementedCharmItemTest {
    @Test void registeredItemsRetainBothOriginalWarningStyles() {
        for (Item item : new Item[]{ContentRegistry.OBSTRUCTION_CHARM.get(), ContentRegistry.SERENITY_CHARM.get()}) {
            ItemStack stack = new ItemStack(item);
            ItemStack before = stack.copy();
            var lines = new ArrayList<Component>();
            item.appendHoverText(stack, Item.TooltipContext.EMPTY, lines, TooltipFlag.NORMAL);
            assertEquals(64, stack.getMaxStackSize());
            assertEquals(2, lines.size());
            assertEquals("arcanearchives.tooltip.notimplemented1", ((TranslatableContents) lines.get(0).getContents()).getKey());
            assertEquals("arcanearchives.tooltip.notimplemented2", ((TranslatableContents) lines.get(1).getContents()).getKey());
            assertEquals(ChatFormatting.RED.getColor().intValue(), lines.get(0).getStyle().getColor().getValue());
            assertEquals(ChatFormatting.RED.getColor().intValue(), lines.get(1).getStyle().getColor().getValue());
            assertTrue(lines.get(0).getStyle().isBold());
            assertTrue(lines.get(1).getStyle().isItalic());
            assertTrue(ItemStack.matches(before, stack));
        }
    }

    @Test void packagedRecipesResolveRegisteredOutputs() throws Exception {
        for (String name : new String[]{"obstruction_charm", "serenity_charm"}) {
            try (var stream = getClass().getResourceAsStream("/data/arcanearchives/recipe/" + name + ".json")) {
                assertNotNull(stream);
                var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                var id = ResourceLocation.tryParse("arcanearchives:" + name);
                var recipe = GemCutterDataRecipe.parse(id, json);
                assertTrue(recipe.enabled());
                assertEquals(12, recipe.order());
                var output = recipe.definition(id).getRecipeOutput();
                assertEquals(4, output.getCount());
                assertTrue(output.is(name.equals("obstruction_charm") ? ContentRegistry.OBSTRUCTION_CHARM.get() : ContentRegistry.SERENITY_CHARM.get()));
            }
        }
    }
}
*///?}
