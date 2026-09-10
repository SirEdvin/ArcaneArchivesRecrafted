package com.aranaira.arcanearchives.inventory;

import net.minecraft.SharedConstants;
import com.aranaira.arcanearchives.recipe.IngredientStack;
import com.aranaira.arcanearchives.recipe.gct.GCTRecipe;
import com.aranaira.arcanearchives.recipe.gct.GCTRecipeList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GemCuttersTableMenuTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void releaseLayoutKeepsDisplaySlotsUnavailable() {
        var menu = new GemCuttersTableMenu(null, 1, new Inventory(null), new OwnedInputs());
        assertEquals(62, menu.slots.size());
        assertEquals(95, menu.getSlot(0).x);
        assertEquals(18, menu.getSlot(0).y);
        assertEquals(105, menu.getSlot(37).y);
        assertEquals(123, menu.getSlot(54).y);
        for (int slot : new int[]{0, 55, 56, 57, 58, 59, 60, 61}) {
            assertFalse(menu.getSlot(slot).mayPlace(new ItemStack(Items.DIAMOND)));
            assertFalse(menu.getSlot(slot).mayPickup(null));
            assertFalse(menu.getSlot(slot).isActive());
            assertTrue(menu.quickMoveStack(null, slot).isEmpty());
            assertDoesNotThrow(() -> menu.clicked(slot, 2, ClickType.CLONE, null));
        }
    }

    @Test
    void quickMoveWritesDetachedMergesAndReturnsToReversePlayerOrder() {
        Inventory player = new Inventory(null);
        OwnedInputs inputs = new OwnedInputs();
        var menu = new GemCuttersTableMenu(null, 1, player, inputs);
        inputs.setItem(0, new ItemStack(Items.DIAMOND, 60));
        player.setItem(9, new ItemStack(Items.DIAMOND, 10));
        assertEquals(10, menu.quickMoveStack(null, 1).getCount());
        assertTrue(player.getItem(9).isEmpty());
        assertEquals(64, inputs.getItem(0).getCount());
        assertEquals(6, inputs.getItem(1).getCount());
        assertEquals(6, menu.quickMoveStack(null, 38).getCount());
        assertEquals(6, player.getItem(8).getCount());
        assertTrue(inputs.getItem(1).isEmpty());
        inputs.getItem(0).setCount(1);
        assertEquals(64, inputs.getItem(0).getCount());
    }

    @Test
    void fullInputsConserveSurplusAndNativeNonstackableLimit() {
        Inventory player = new Inventory(null);
        OwnedInputs inputs = new OwnedInputs();
        var menu = new GemCuttersTableMenu(null, 1, player, inputs);
        for (int slot = 0; slot < 18; slot++) inputs.setItem(slot, new ItemStack(Items.DIAMOND, 64));
        player.setItem(9, new ItemStack(Items.DIAMOND, 10));
        inputs.setItem(17, new ItemStack(Items.DIAMOND, 63));
        menu.quickMoveStack(null, 1);
        assertEquals(9, player.getItem(9).getCount());
        assertEquals(64, inputs.getItem(17).getCount());
        assertTrue(menu.quickMoveStack(null, 1).isEmpty());
        player.setItem(10, new ItemStack(Items.IRON_SWORD));
        assertTrue(menu.quickMoveStack(null, 2).isEmpty());
        inputs.setItem(17, ItemStack.EMPTY);
        menu.quickMoveStack(null, 2);
        assertEquals(1, inputs.getItem(17).getCount());
        assertTrue(player.getItem(10).isEmpty());
    }

    @Test
    void invalidatedOwnerAndMalformedIndicesCannotTransfer() {
        Inventory player = new Inventory(null);
        OwnedInputs inputs = new OwnedInputs();
        var menu = new GemCuttersTableMenu(null, 1, player, inputs);
        player.setItem(9, new ItemStack(Items.DIAMOND, 10));
        for (int index : new int[]{Integer.MIN_VALUE, -1, 62, Integer.MAX_VALUE}) {
            assertTrue(menu.quickMoveStack(null, index).isEmpty());
            if (index != -1) assertDoesNotThrow(() -> menu.clicked(index, 0, ClickType.QUICK_MOVE, null));
        }
        inputs.valid = false;
        assertFalse(menu.stillValid(null));
        assertTrue(menu.quickMoveStack(null, 1).isEmpty());
        menu.clicked(1, 0, ClickType.PICKUP, null);
        assertEquals(10, player.getItem(9).getCount());
        assertTrue(inputs.isEmpty());
    }

    @Test
    void recipePreviewUsesConservedTableAndMainPlayerInputsWithoutGrantingItems() {
        var player = new Inventory(null);
        var inputs = new OwnedInputs();
        var catalog = new GCTRecipeList();
        catalog.addRecipe(new GCTRecipe(ResourceLocation.tryParse("arcanearchives:paid"), new ItemStack(Items.PAPER, 2),
            List.of(new IngredientStack(Items.DIAMOND, 2), new IngredientStack(Items.DIAMOND, 1))));
        inputs.setItem(0, new ItemStack(Items.DIAMOND, 2));
        player.setItem(40, new ItemStack(Items.DIAMOND));
        var menu = new GemCuttersTableMenu(null, 1, player, inputs, catalog);
        assertTrue(menu.getSlot(0).getItem().isEmpty()); // Offhand is not the upstream UP/main inventory.
        player.setItem(8, new ItemStack(Items.DIAMOND));
        menu.broadcastChanges();
        assertEquals(2, menu.getSlot(0).getItem().getCount());
        assertEquals(2, menu.getSlot(61).getItem().getCount());
        for (int slot : new int[]{0, 61}) {
            for (ClickType type : ClickType.values()) {
                for (int button : new int[]{0, 1, 2, 40, Integer.MAX_VALUE}) menu.clicked(slot, button, type, null);
            }
            assertFalse(menu.getSlot(slot).mayPickup(null));
            assertFalse(menu.getSlot(slot).mayPlace(new ItemStack(Items.GOLD_INGOT)));
            assertTrue(menu.quickMoveStack(null, slot).isEmpty());
        }
        assertTrue(menu.getCarried().isEmpty());
        assertEquals(2, inputs.getItem(0).getCount());
        assertEquals(1, player.getItem(8).getCount());
        assertEquals(1, player.getItem(40).getCount());
        menu.getSlot(0).getItem().setCount(50);
        menu.broadcastChanges();
        assertEquals(2, menu.getSlot(0).getItem().getCount());
        player.setItem(8, ItemStack.EMPTY);
        menu.broadcastChanges();
        assertTrue(menu.getSlot(0).getItem().isEmpty());
    }

    @Test
    void serverRecipeSelectionPagesAndReplacementNeverOverwriteInventories() {
        var player = new Inventory(null);
        var inputs = new OwnedInputs();
        var catalog = new GCTRecipeList();
        for (int index = 0; index < 9; index++) catalog.addRecipe(new GCTRecipe(
            ResourceLocation.tryParse("arcanearchives:display_" + index), new ItemStack(Items.PAPER, index + 1), List.of()));
        player.setItem(0, new ItemStack(Items.DIAMOND, 7));
        inputs.setItem(0, new ItemStack(Items.GOLD_INGOT, 5));
        var menu = new GemCuttersTableMenu(null, 1, player, inputs, catalog);
        assertEquals(1, menu.getSlot(61).getItem().getCount());
        assertEquals(7, menu.getSlot(55).getItem().getCount());
        menu.clicked(55, 0, ClickType.PICKUP, null);
        assertEquals(7, menu.getSlot(0).getItem().getCount());
        assertTrue(menu.clickMenuButton(null, 1));
        assertEquals(8, menu.getSlot(61).getItem().getCount());
        assertEquals(9, menu.getSlot(60).getItem().getCount());
        assertTrue(menu.getSlot(59).getItem().isEmpty());
        menu.clicked(60, 1, ClickType.PICKUP, null);
        assertEquals(9, menu.getSlot(0).getItem().getCount());
        assertFalse(menu.clickMenuButton(null, Integer.MAX_VALUE));
        var replacement = new GCTRecipe(ResourceLocation.tryParse("arcanearchives:display_8"), new ItemStack(Items.EMERALD), List.of());
        catalog.replaceAll(List.of(replacement));
        menu.broadcastChanges();
        assertTrue(menu.getSlot(0).getItem().is(Items.EMERALD));
        assertTrue(menu.getSlot(61).getItem().is(Items.EMERALD));
        assertTrue(menu.getSlot(60).getItem().isEmpty());
        assertTrue(menu.clickMenuButton(null, 0));
        catalog.removeRecipe(replacement);
        menu.broadcastChanges();
        assertTrue(menu.getSlot(0).getItem().isEmpty());
        assertEquals(7, player.getItem(0).getCount());
        assertEquals(5, inputs.getItem(0).getCount());
        assertTrue(menu.getCarried().isEmpty());
    }

    @Test
    void passiveClientAndInvalidAccessCannotComputeOrChangeRecipeSelection() {
        var player = new Inventory(null);
        var inputs = new OwnedInputs();
        var client = new GemCuttersTableMenu(null, 1, player, inputs, null);
        client.getSlot(61).set(new ItemStack(Items.PAPER)); // Native synchronization, not a local catalog.
        client.broadcastChanges();
        client.clicked(61, 0, ClickType.PICKUP, null);
        assertTrue(client.getSlot(61).getItem().is(Items.PAPER));
        assertTrue(client.getCarried().isEmpty());
        assertFalse(client.clickMenuButton(null, 1));
        var catalog = new GCTRecipeList();
        catalog.addRecipe(new GCTRecipe(ResourceLocation.tryParse("arcanearchives:preview"), new ItemStack(Items.PAPER), List.of()));
        var server = new GemCuttersTableMenu(null, 1, player, inputs, catalog);
        inputs.valid = false;
        server.broadcastChanges();
        assertTrue(server.getSlot(0).getItem().isEmpty());
        assertFalse(server.clickMenuButton(null, 0));
        server.clicked(61, 0, ClickType.PICKUP, null);
        assertTrue(server.getCarried().isEmpty());
    }

    @Test
    void openMenuAndIngredientDisplayFollowNativeTagRebinding() {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.tryParse("arcanearchives:test_menu_reload"));
        Map<TagKey<Item>, List<Holder<Item>>> original = new HashMap<>();
        BuiltInRegistries.ITEM.getTags().forEach(pair -> original.put(pair.getFirst(), pair.getSecond().stream().toList()));
        Map<TagKey<Item>, List<Holder<Item>>> rebound = new HashMap<>(original);
        try {
            rebound.put(tag, List.of(BuiltInRegistries.ITEM.wrapAsHolder(Items.DIAMOND)));
            BuiltInRegistries.ITEM.bindTags(rebound);
            var ingredient = new IngredientStack(tag, 3);
            var catalog = new GCTRecipeList();
            catalog.addRecipe(new GCTRecipe(ResourceLocation.tryParse("arcanearchives:reload"), new ItemStack(Items.PAPER), List.of(ingredient)));
            var inputs = new OwnedInputs();
            var player = new Inventory(null);
            inputs.setItem(0, new ItemStack(Items.DIAMOND, 2));
            player.setItem(8, new ItemStack(Items.DIAMOND));
            var menu = new GemCuttersTableMenu(null, 1, player, inputs, catalog);
            assertTrue(menu.getSlot(0).getItem().is(Items.PAPER));
            assertTrue(ingredient.getMatchingStacksWithSizes().get(0).is(Items.DIAMOND));

            rebound.put(tag, List.of(BuiltInRegistries.ITEM.wrapAsHolder(Items.GOLD_INGOT)));
            BuiltInRegistries.ITEM.bindTags(rebound);
            menu.broadcastChanges();
            assertTrue(menu.getSlot(0).getItem().isEmpty());
            assertEquals(2, inputs.getItem(0).getCount());
            assertEquals(1, player.getItem(8).getCount());
            assertFalse(ingredient.apply(new ItemStack(Items.DIAMOND)));
            assertTrue(ingredient.getIngredient().test(new ItemStack(Items.GOLD_INGOT)));
            var shown = ingredient.getMatchingStacksWithSizes().get(0);
            assertTrue(shown.is(Items.GOLD_INGOT));
            assertEquals(3, shown.getCount());
            shown.setCount(40);
            assertEquals(3, ingredient.getMatchingStacksWithSizes().get(0).getCount());
            inputs.setItem(0, new ItemStack(Items.GOLD_INGOT, 2));
            player.setItem(8, new ItemStack(Items.GOLD_INGOT));
            menu.broadcastChanges();
            assertTrue(menu.getSlot(0).getItem().is(Items.PAPER));

            rebound.put(tag, List.of());
            BuiltInRegistries.ITEM.bindTags(rebound);
            menu.broadcastChanges();
            assertTrue(menu.getSlot(0).getItem().isEmpty());
            assertTrue(ingredient.getMatchingStacksWithSizes().isEmpty());
            assertTrue(menu.getCarried().isEmpty());
        } finally {
            original.putIfAbsent(tag, List.of());
            BuiltInRegistries.ITEM.bindTags(original);
        }
    }

    private static final class OwnedInputs extends SimpleContainer {
        boolean valid = true;
        OwnedInputs() { super(18); }
        @Override
        public ItemStack getItem(int slot) { return super.getItem(slot).copy(); }
        @Override
        public boolean stillValid(Player player) { return valid; }
    }
}
