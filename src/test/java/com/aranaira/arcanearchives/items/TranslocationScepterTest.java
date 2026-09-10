package com.aranaira.arcanearchives.items;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.junit.jupiter.api.Test;

class TranslocationScepterTest {
    @Test void bothHandsMatchNativeSneakBypassForEveryScepter() {
        var stacks = List.of(ItemStack.EMPTY, new ItemStack(ContentRegistry.SCEPTER_REVELATION.get()),
            new ItemStack(ContentRegistry.SCEPTER_MANIPULATION.get()), new ItemStack(ContentRegistry.SCEPTER_TRANSLOCATION.get()),
            new ItemStack(net.minecraft.world.item.Items.STONE));
        for (var main : stacks) for (var off : stacks) {
            boolean expected = main.doesSneakBypassUse(null, null, null) && off.doesSneakBypassUse(null, null, null);
            assertEquals(expected, StorageScepterItem.handsBypassSneakUse(main, off));
            assertEquals(expected, StorageScepterItem.handsBypassSneakUse(off, main));
        }
        for (var scepter : stacks.subList(1, 4)) {
            assertTrue(scepter.doesSneakBypassUse(null, null, null));
            assertTrue(StorageScepterItem.handsBypassSneakUse(scepter, ItemStack.EMPTY));
            assertFalse(StorageScepterItem.handsBypassSneakUse(scepter, stacks.get(4)));
        }
    }
    @Test void registeredScepterKeepsIdentityWithoutRevelationActions() {
        var item = ContentRegistry.SCEPTER_TRANSLOCATION.get();
        var stack = new ItemStack(item);
        assertInstanceOf(StorageScepterItem.class, item);
        assertEquals(1, stack.getMaxStackSize());
        assertFalse(item.interact(null, null, null, null));
        var text = new ArrayList<Component>();
        item.appendHoverText(stack, Item.TooltipContext.EMPTY, text, TooltipFlag.NORMAL);
        assertEquals(1, text.size());
        assertEquals("arcanearchives.tooltip.item.scepter_translocation", ((TranslatableContents) text.getFirst().getContents()).getKey());
        assertEquals(net.minecraft.ChatFormatting.GOLD.getColor().intValue(), text.getFirst().getStyle().getColor().getValue());
    }

    @Test void recipeRequiresRevelationAndMaterialInterface() throws Exception {
        try (var stream = getClass().getResourceAsStream("/data/arcanearchives/recipe/scepter_translocation.json")) {
            assertNotNull(stream);
            var id = ResourceLocation.tryParse("arcanearchives:scepter_translocation");
            var data = GemCutterDataRecipe.parse(id, JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject());
            var recipe = data.definition(id);
            assertTrue(recipe.matches(List.of(new ItemStack(ContentRegistry.SCEPTER_REVELATION.get()), new ItemStack(ContentRegistry.MATERIAL_INTERFACE.get()))));
            assertFalse(recipe.matches(List.of(new ItemStack(ContentRegistry.SCEPTER_REVELATION.get()))));
            assertFalse(recipe.matches(List.of(new ItemStack(ContentRegistry.SCEPTER_MANIPULATION.get()), new ItemStack(ContentRegistry.MATERIAL_INTERFACE.get()))));
            assertTrue(recipe.getRecipeOutput().is(ContentRegistry.SCEPTER_TRANSLOCATION.get()));
            assertEquals(1, recipe.getRecipeOutput().getCount());
        }
    }
}
*///?}
