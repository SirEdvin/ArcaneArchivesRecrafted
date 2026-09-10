package com.aranaira.arcanearchives.inventory;

import com.aranaira.arcanearchives.recipe.IngredientStack;
import com.aranaira.arcanearchives.recipe.gct.GCTRecipe;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import com.aranaira.arcanearchives.recipe.gct.GCTRecipeList;
import com.aranaira.arcanearchives.recipe.gct.GemCutterCraftingState;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GemCuttersTableCraftingTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static GCTRecipe recipe(IngredientStack... ingredients) {
        return new GCTRecipe(ResourceLocation.tryParse("arcanearchives:paid"), new ItemStack(Items.PAPER, 2), List.of(ingredients));
    }

    private static List<GemCutterDataRecipe.Entry> reloaded(String output, int cost, boolean enabled) {
        var name = ResourceLocation.tryParse("arcanearchives:reload_paid");
        var json = com.google.gson.JsonParser.parseString("{\"enabled\":" + enabled
            + ",\"inputs\":[{\"item\":\"minecraft:diamond\",\"count\":" + cost
            + "}],\"result\":{\"item\":\"minecraft:" + output + "\",\"count\":2}}").getAsJsonObject();
        return List.of(new GemCutterDataRecipe.Entry(name, GemCutterDataRecipe.parse(name, json)));
    }

    @Test
    void openMenuRefreshesChangedDisabledAndRemovedRecipesWithoutReopeningOrFreePayment() {
        var live = new java.util.concurrent.atomic.AtomicReference<>(reloaded("paper", 1, true));
        var f = new Fixture(live::get);
        f.state.setInput(0, new ItemStack(Items.DIAMOND, 6));
        f.menu.broadcastChanges();
        assertTrue(f.menu.getSlot(0).getItem().is(Items.PAPER));
        live.set(reloaded("gold_ingot", 2, true));
        f.pickup(); // A click first discovering a changed definition only refreshes presentation.
        assertTrue(f.menu.getCarried().isEmpty());
        assertEquals(6, f.state.getInput(0).getCount());
        assertTrue(f.menu.getSlot(0).getItem().is(Items.GOLD_INGOT));
        f.pickup();
        assertEquals(4, f.state.getInput(0).getCount());
        assertTrue(f.menu.getCarried().is(Items.GOLD_INGOT));
        assertEquals(2, f.menu.getCarried().getCount());
        live.set(reloaded("gold_ingot", 2, false));
        f.menu.broadcastChanges();
        f.pickup();
        assertTrue(f.menu.getSlot(0).getItem().isEmpty());
        assertEquals(4, f.state.getInput(0).getCount());
        live.set(List.of());
        f.menu.broadcastChanges();
        assertTrue(f.menu.getSlot(61).getItem().isEmpty());
        f.pickup();
        assertEquals(4, f.state.getInput(0).getCount());
        assertEquals(2, f.menu.getCarried().getCount());
    }

    @Test
    void nativeDefinitionChangeAtFinalAccessCheckCancelsPaymentAndRecovers() {
        var live = new java.util.concurrent.atomic.AtomicReference<>(reloaded("paper", 1, true));
        var f = new Fixture(live::get);
        f.state.setInput(0, new ItemStack(Items.DIAMOND, 4));
        int[] calls = {0};
        f.access = () -> {
            if (++calls[0] == 2) live.set(reloaded("paper", 3, true));
            return true;
        };
        f.pickup();
        assertTrue(f.menu.getCarried().isEmpty());
        assertEquals(4, f.state.getInput(0).getCount());
        f.access = () -> true;
        f.pickup();
        assertEquals(1, f.state.getInput(0).getCount());
        assertEquals(2, f.menu.getCarried().getCount());
    }

    @Test
    void paidPickupConsumesRepeatedCostsAcrossTableAndMainInventoryExactlyOnce() {
        var f = new Fixture(recipe(new IngredientStack(Items.DIAMOND, 2), new IngredientStack(Items.DIAMOND, 1)));
        f.state.setInput(0, new ItemStack(Items.DIAMOND, 2));
        f.player.setItem(40, new ItemStack(Items.DIAMOND));
        f.pickup();
        assertTrue(f.menu.getCarried().isEmpty());
        assertEquals(2, f.state.getInput(0).getCount());
        f.player.setItem(8, new ItemStack(Items.DIAMOND));
        f.pickup();
        assertEquals(2, f.menu.getCarried().getCount());
        assertTrue(f.menu.getCarried().is(Items.PAPER));
        assertTrue(f.state.getInput(0).isEmpty());
        assertTrue(f.player.getItem(8).isEmpty());
        assertEquals(1, f.player.getItem(40).getCount());
        assertEquals(1, f.inputs.dirty);
        assertTrue(f.state.pendingResult().isEmpty());
        f.pickup();
        assertEquals(2, f.menu.getCarried().getCount());
        assertEquals(1, f.inputs.dirty);
    }

    @Test
    void fullOrDifferentCursorRejectsEntireCraftAndRightClickTakesWholeOutput() {
        var f = new Fixture(recipe(new IngredientStack(Items.DIAMOND, 3)));
        f.state.setInput(17, new ItemStack(Items.DIAMOND, 6));
        for (ItemStack cursor : List.of(new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.PAPER, 63))) {
            f.menu.setCarried(cursor);
            f.pickup();
            assertEquals(6, f.state.getInput(17).getCount());
            assertEquals(cursor.getCount(), f.menu.getCarried().getCount());
        }
        f.menu.setCarried(new ItemStack(Items.PAPER, 62));
        f.menu.clicked(0, 1, ClickType.PICKUP, null);
        assertEquals(64, f.menu.getCarried().getCount());
        assertEquals(3, f.state.getInput(17).getCount());
        f.pickup();
        assertEquals(3, f.state.getInput(17).getCount());
    }

    @Test
    void shiftCraftUsesSpaceFreedByPaymentAndPreservesCursor() {
        var f = new Fixture(recipe(new IngredientStack(Items.DIAMOND, 3)));
        for (int slot = 0; slot < 36; slot++) f.player.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        f.state.setInput(0, new ItemStack(Items.DIAMOND, 2));
        f.player.setItem(8, new ItemStack(Items.DIAMOND));
        f.menu.setCarried(new ItemStack(Items.GOLD_INGOT, 7));
        f.menu.clicked(0, 0, ClickType.QUICK_MOVE, null);
        assertTrue(f.state.getInput(0).isEmpty());
        assertTrue(f.player.getItem(8).is(Items.PAPER));
        assertEquals(2, f.player.getItem(8).getCount());
        assertEquals(7, f.menu.getCarried().getCount());
        for (int slot = 0; slot < 36; slot++) if (slot != 8) assertEquals(64, f.player.getItem(slot).getCount());
    }

    @Test
    void shiftCraftNeverDeliversPartialOutputOrConsumesForInsufficientRoom() {
        var f = new Fixture(recipe(new IngredientStack(Items.DIAMOND, 3)));
        f.state.setInput(0, new ItemStack(Items.DIAMOND, 6));
        for (int slot = 0; slot < 36; slot++) f.player.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        f.player.setItem(8, new ItemStack(Items.PAPER, 63));
        assertTrue(f.menu.quickMoveStack(null, 0).isEmpty());
        assertEquals(63, f.player.getItem(8).getCount());
        assertEquals(6, f.state.getInput(0).getCount());
        f.player.setItem(8, new ItemStack(Items.PAPER, 62));
        assertEquals(2, f.menu.quickMoveStack(null, 0).getCount());
        assertEquals(64, f.player.getItem(8).getCount());
        assertEquals(3, f.state.getInput(0).getCount());
        assertTrue(f.menu.getCarried().isEmpty());
    }

    @Test
    void unsupportedClicksCannotGrantOutputEvenWithPaidBackend() {
        var f = new Fixture(recipe(new IngredientStack(Items.DIAMOND, 3)));
        f.state.setInput(0, new ItemStack(Items.DIAMOND, 6));
        for (ClickType type : ClickType.values()) {
            for (int button : new int[]{0, 1, 2, 40, Integer.MAX_VALUE}) {
                if ((type == ClickType.PICKUP || type == ClickType.QUICK_MOVE) && button < 2) continue;
                f.menu.clicked(0, button, type, null);
            }
        }
        assertEquals(6, f.state.getInput(0).getCount());
        assertTrue(f.menu.getCarried().isEmpty());
        assertTrue(f.player.isEmpty());
        assertFalse(f.menu.getSlot(0).mayPickup(null));
    }

    @Test
    void finalAccessFailureAndExceptionLeavePaymentIntactAndGuardRecovers() {
        var f = new Fixture(recipe(new IngredientStack(Items.DIAMOND, 3)));
        f.state.setInput(0, new ItemStack(Items.DIAMOND, 3));
        int[] calls = {0};
        f.access = () -> ++calls[0] < 2;
        f.pickup();
        assertEquals(2, calls[0]);
        assertEquals(3, f.state.getInput(0).getCount());
        assertTrue(f.menu.getCarried().isEmpty());
        calls[0] = 0;
        f.access = () -> { if (++calls[0] == 2) throw new IllegalStateException("lookup failed"); return true; };
        assertThrows(IllegalStateException.class, f::pickup);
        assertEquals(3, f.state.getInput(0).getCount());
        assertTrue(f.menu.getCarried().isEmpty());
        f.access = () -> true;
        f.pickup();
        assertTrue(f.state.getInput(0).isEmpty());
        assertEquals(2, f.menu.getCarried().getCount());
    }

    @Test
    void finalPlayerMutationOrDeviceInvalidationCannotAuthorizeCraft() {
        for (boolean invalidate : new boolean[]{false, true}) {
            var f = new Fixture(recipe(new IngredientStack(Items.DIAMOND, 3)));
            f.state.setInput(0, new ItemStack(Items.DIAMOND, 2));
            f.player.setItem(0, new ItemStack(Items.DIAMOND));
            int[] calls = {0};
            f.access = () -> {
                if (++calls[0] == 2) {
                    if (invalidate) f.inputs.valid = false;
                    else f.player.setItem(0, new ItemStack(Items.GOLD_INGOT));
                }
                return true;
            };
            f.pickup();
            assertEquals(2, f.state.getInput(0).getCount());
            assertEquals(1, f.player.getItem(0).getCount());
            assertTrue(f.menu.getCarried().isEmpty());
            assertEquals(0, f.inputs.dirty);
        }
    }

    @Test
    void catalogRemoveRestoreDuringMatchingCancelsAndLaterCleanAttemptSucceeds() {
        Fixture[] fixture = {null};
        boolean[] armed = {false};
        var definition = recipe(new IngredientStack(Ingredient.of(Items.DIAMOND), 3, candidate -> {
            if (armed[0]) {
                armed[0] = false;
                var catalog = fixture[0].catalog;
                var original = catalog.getRecipe(ResourceLocation.tryParse("arcanearchives:paid"));
                catalog.removeRecipe(original);
                catalog.addRecipe(original);
            }
            return true;
        }));
        var f = fixture[0] = new Fixture(definition);
        f.state.setInput(0, new ItemStack(Items.DIAMOND, 3));
        armed[0] = true;
        f.pickup();
        assertEquals(3, f.state.getInput(0).getCount());
        assertTrue(f.menu.getCarried().isEmpty());
        f.pickup();
        assertTrue(f.state.getInput(0).isEmpty());
        assertEquals(2, f.menu.getCarried().getCount());
    }

    @Test
    void reentrantMenuCraftTransferAndNavigationCannotDuplicatePayment() {
        Fixture[] fixture = {null};
        var f = fixture[0] = new Fixture(recipe(new IngredientStack(Ingredient.of(Items.DIAMOND), 3, candidate -> {
            fixture[0].menu.clicked(0, 0, ClickType.PICKUP, null);
            assertTrue(fixture[0].menu.quickMoveStack(null, 0).isEmpty());
            assertTrue(fixture[0].menu.quickMoveStack(null, 37).isEmpty());
            assertFalse(fixture[0].menu.clickMenuButton(null, 0));
            return true;
        })));
        f.state.setInput(0, new ItemStack(Items.DIAMOND, 3));
        // Call the output bridge directly: clicked also refreshes previews after the guarded craft.
        assertEquals(2, f.menu.quickMoveStack(null, 0).getCount());
        assertTrue(f.state.getInput(0).isEmpty());
        assertEquals(2, f.player.getItem(8).getCount());
        assertEquals(1, f.inputs.dirty);
    }

    @Test
    void stagedResultBlocksImmediateCraftAndStateReplacementCannotUseOldInputs() {
        var f = new Fixture(recipe(new IngredientStack(Items.DIAMOND, 3)));
        f.state.setInput(0, new ItemStack(Items.DIAMOND, 6));
        assertTrue(f.state.craft(f.catalog, ResourceLocation.tryParse("arcanearchives:paid"), new UUID(0, 1), "Crafter", () -> true));
        f.pickup();
        assertEquals(3, f.state.getInput(0).getCount());
        assertTrue(f.menu.getCarried().isEmpty());
        assertEquals(2, f.state.pendingResult().orElseThrow().output().getCount());
        f.state = new GemCutterCraftingState();
        f.state.setInput(0, new ItemStack(Items.DIAMOND, 3));
        GemCutterCraftingState old = f.state;
        int[] calls = {0};
        f.access = () -> {
            if (++calls[0] == 2) {
                f.state = new GemCutterCraftingState();
                f.state.setInput(0, new ItemStack(Items.DIAMOND, 3));
            }
            return true;
        };
        f.pickup();
        assertEquals(3, old.getInput(0).getCount());
        assertEquals(3, f.state.getInput(0).getCount());
        assertTrue(f.menu.getCarried().isEmpty());
        f.access = () -> true;
        f.pickup();
        assertTrue(f.state.getInput(0).isEmpty());
        assertEquals(3, old.getInput(0).getCount());
        assertEquals(2, f.menu.getCarried().getCount());
    }

    @Test
    void toolAndFluidCostsReturnBothAfterSuccessfulPayment() {
        for (var item : List.of(Items.FLINT_AND_STEEL)) {
            var f = new Fixture(recipe(new IngredientStack(Items.WATER_BUCKET, 1), new IngredientStack(item, 1)));
            f.state.setInput(0, new ItemStack(Items.WATER_BUCKET));
            f.state.setInput(17, new ItemStack(item));
            f.pickup();
            assertTrue(f.state.getInput(0).is(Items.BUCKET));
            assertTrue(f.state.getInput(1).is(item));
            assertEquals(1, f.state.getInput(1).getDamageValue());
            assertTrue(f.state.getInput(17).isEmpty());
            assertEquals(1, f.state.getInput(0).getCount());
            assertEquals(2, f.menu.getCarried().getCount());
            assertEquals(1, f.inputs.dirty);
        }
    }

    @Test
    void ordinaryToolsArePaidAsIngredientsNotDamagedOrReturned() {
        for (var item : List.of(Items.DIAMOND_SWORD, Items.IRON_SWORD, Items.IRON_PICKAXE, Items.BOW)) {
            var f = new Fixture(recipe(new IngredientStack(item, 2)));
            var offered = new ItemStack(item);
            offered.setDamageValue(10);
            f.state.setInput(0, offered);
            f.player.setItem(8, offered.copy());
            f.menu.setCarried(new ItemStack(Items.PAPER, 64));
            f.pickup();
            assertEquals(10, f.state.getInput(0).getDamageValue());
            assertEquals(10, f.player.getItem(8).getDamageValue());
            assertEquals(0, f.inputs.dirty);
            f.menu.setCarried(ItemStack.EMPTY);
            f.pickup();
            assertTrue(f.state.getInput(0).isEmpty());
            assertTrue(f.player.getItem(8).isEmpty());
            assertEquals(2, f.menu.getCarried().getCount());
            assertTrue(f.menu.getCarried().is(Items.PAPER));
            assertEquals(1, f.inputs.dirty);
            assertEquals(1, offered.getCount());
            assertEquals(10, offered.getDamageValue());
            f.pickup();
            assertEquals(2, f.menu.getCarried().getCount());
        }
    }

    @Test
    void flintDamageIsPaidOnceAndBreaksOnlyOnSuccessfulFinalUse() {
        for (boolean finalUse : new boolean[]{false, true}) {
            var f = new Fixture(recipe(new IngredientStack(Items.FLINT_AND_STEEL, 1)));
            var tool = new ItemStack(Items.FLINT_AND_STEEL);
            int damage = finalUse ? tool.getMaxDamage() - 1 : 10;
            tool.setDamageValue(damage);
            f.player.setItem(8, tool.copy());
            f.menu.setCarried(new ItemStack(Items.PAPER, 64));
            f.pickup();
            assertEquals(damage, f.player.getItem(8).getDamageValue());
            assertEquals(0, f.inputs.dirty);
            f.menu.setCarried(ItemStack.EMPTY);
            int[] calls = {0};
            f.access = () -> ++calls[0] < 2;
            f.pickup();
            assertEquals(damage, f.player.getItem(8).getDamageValue());
            assertTrue(f.menu.getCarried().isEmpty());
            f.access = () -> true;
            f.pickup();
            assertTrue(f.player.isEmpty());
            assertEquals(2, f.menu.getCarried().getCount());
            assertEquals(damage, tool.getDamageValue());
            if (finalUse) {
                assertEquals(18, f.state.countEmptyInputs());
                f.pickup();
                assertEquals(2, f.menu.getCarried().getCount());
            } else {
                assertTrue(f.state.getInput(0).is(Items.FLINT_AND_STEEL));
                assertEquals(damage + 1, f.state.getInput(0).getDamageValue());
            }
        }
    }

    @Test
    void flintPreparationPreservesCreativeUnbreakableAndOwnedData() {
        var tool = com.aranaira.arcanearchives.recipe.CraftingCreator.withCreator(
            new ItemStack(Items.FLINT_AND_STEEL), new UUID(0, 1), "Tool owner");
        tool.setDamageValue(10);
        var creative = GemCuttersTableMenu.prepareFlint(tool, true).orElseThrow();
        assertEquals(10, creative.getDamageValue());
        creative.setDamageValue(20);
        assertEquals(10, tool.getDamageValue());
        var damaged = GemCuttersTableMenu.prepareFlint(tool, false).orElseThrow();
        damaged.setDamageValue(10);
        assertTrue(com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(tool, damaged));
        //? if >=1.21 {
        tool.set(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true));
        //?} else {
        /*tool.getOrCreateTag().putBoolean("Unbreakable", true);
        *///?}
        assertEquals(10, GemCuttersTableMenu.prepareFlint(tool, false).orElseThrow().getDamageValue());
        assertTrue(GemCuttersTableMenu.prepareFlint(new ItemStack(Items.FLINT_AND_STEEL, 2), false).isEmpty());
        assertTrue(GemCuttersTableMenu.prepareFlint(new ItemStack(Items.IRON_SWORD), false).isEmpty());
        assertTrue(GemCuttersTableMenu.prepareFlint(ItemStack.EMPTY, false).isEmpty());
    }

    @Test
    void enchantedFlintCannotBePaidWithSimplifiedDamageHandling() {
        var tool = new ItemStack(Items.FLINT_AND_STEEL);
        //? if >=1.21 {
        tool.enchant(net.minecraft.data.registries.VanillaRegistries.createLookup()
            .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING), 1);
        //?} else {
        /*tool.enchant(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING, 1);
        *///?}
        assertTrue(tool.isEnchanted());
        assertTrue(GemCuttersTableMenu.prepareFlint(tool, false).isEmpty());
        var f = new Fixture(recipe(new IngredientStack(Items.FLINT_AND_STEEL, 1)));
        f.state.setInput(0, tool);
        f.pickup();
        assertTrue(f.menu.getCarried().isEmpty());
        assertTrue(f.state.getInput(0).isEnchanted());
        assertEquals(0, f.state.getInput(0).getDamageValue());
        assertEquals(0, f.inputs.dirty);
    }

    @Test
    void fluidReturnsMergeIntoTableBeforeEmptySlotsAndPayBothInventories() {
        var f = new Fixture(recipe(new IngredientStack(Items.WATER_BUCKET, 1), new IngredientStack(Items.LAVA_BUCKET, 1)));
        f.state.setInput(17, new ItemStack(Items.WATER_BUCKET));
        f.state.setInput(4, new ItemStack(Items.BUCKET, 15));
        f.player.setItem(8, new ItemStack(Items.LAVA_BUCKET));
        f.menu.setCarried(new ItemStack(Items.PAPER, 64));
        f.pickup();
        assertTrue(f.state.getInput(17).is(Items.WATER_BUCKET));
        assertTrue(f.player.getItem(8).is(Items.LAVA_BUCKET));
        assertEquals(15, f.state.getInput(4).getCount());
        assertEquals(0, f.inputs.dirty);
        f.menu.setCarried(ItemStack.EMPTY);
        f.pickup();
        assertTrue(f.state.getInput(17).isEmpty());
        assertTrue(f.player.isEmpty());
        assertEquals(16, f.state.getInput(4).getCount());
        assertTrue(f.state.getInput(0).is(Items.BUCKET));
        assertEquals(1, f.state.getInput(0).getCount());
        assertEquals(2, f.menu.getCarried().getCount());
        f.pickup();
        assertEquals(2, f.menu.getCarried().getCount());
        assertEquals(1, f.inputs.dirty);
    }

    @Test
    void fluidReturnsFallBackToPlayerAndRejectInsufficientCombinedDeliverySpace() {
        var f = new Fixture(recipe(new IngredientStack(Items.WATER_BUCKET, 1)));
        for (int slot = 0; slot < 18; slot++) f.state.setInput(slot, new ItemStack(Items.DIAMOND, 64));
        for (int slot = 0; slot < 36; slot++) f.player.setItem(slot, new ItemStack(Items.DIAMOND, 64));
        f.player.setItem(8, new ItemStack(Items.WATER_BUCKET));
        f.menu.quickMoveStack(null, 0);
        assertTrue(f.player.getItem(8).is(Items.WATER_BUCKET));
        assertEquals(0, f.inputs.dirty);
        f.pickup();
        assertTrue(f.player.getItem(8).is(Items.BUCKET));
        assertEquals(1, f.player.getItem(8).getCount());
        assertEquals(2, f.menu.getCarried().getCount());
        for (int slot = 0; slot < 18; slot++) assertEquals(64, f.state.getInput(slot).getCount());
    }

    @Test
    void finalAccessRejectionDoesNotDrainLiveFluidContainers() {
        var f = new Fixture(recipe(new IngredientStack(Items.WATER_BUCKET, 1)));
        f.state.setInput(0, new ItemStack(Items.WATER_BUCKET));
        int[] calls = {0};
        f.access = () -> ++calls[0] < 2;
        f.pickup();
        assertTrue(f.state.getInput(0).is(Items.WATER_BUCKET));
        assertTrue(f.menu.getCarried().isEmpty());
        assertTrue(f.player.isEmpty());
        assertEquals(0, f.inputs.dirty);
        f.access = () -> true;
        f.pickup();
        assertTrue(f.state.getInput(0).is(Items.BUCKET));
        assertEquals(2, f.menu.getCarried().getCount());
    }

    @Test
    void throwingReturnPreparationLeavesPaymentUntouchedAndGuardRecovers() {
        var state = new GemCutterCraftingState();
        var player = new Inventory(null);
        var inputs = new Inputs(() -> state);
        var catalog = new GCTRecipeList();
        catalog.addRecipe(recipe(new IngredientStack(Items.WATER_BUCKET, 1)));
        var fail = new java.util.concurrent.atomic.AtomicBoolean(true);
        var menu = new GemCuttersTableMenu(null, 1, player, inputs, catalog, () -> state, () -> true,
            stack -> true, null, stack -> {
                if (fail.getAndSet(false)) throw new IllegalStateException("Return callback failed");
                return java.util.Optional.of(new ItemStack(Items.BUCKET));
            });
        state.setInput(0, new ItemStack(Items.WATER_BUCKET));
        assertThrows(IllegalStateException.class, () -> menu.clicked(0, 0, ClickType.PICKUP, null));
        assertTrue(state.getInput(0).is(Items.WATER_BUCKET));
        assertTrue(menu.getCarried().isEmpty());
        assertEquals(0, inputs.dirty);
        menu.clicked(0, 0, ClickType.PICKUP, null);
        assertTrue(state.getInput(0).is(Items.BUCKET));
        assertEquals(2, menu.getCarried().getCount());
    }

    @Test
    void stackedContainerPaymentPreparesEveryUnitAndRejectsPartialReturns() {
        var state = new GemCutterCraftingState();
        var player = new Inventory(null);
        var inputs = new Inputs(() -> state);
        var catalog = new GCTRecipeList();
        catalog.addRecipe(recipe(new IngredientStack(Items.HONEY_BOTTLE, 3)));
        int[] calls = {0};
        boolean[] fail = {true};
        // Explicit return-policy fixture, not a claim that a native honey fluid is registered.
        var menu = new GemCuttersTableMenu(null, 1, player, inputs, catalog, () -> state, () -> true,
            stack -> { assertEquals(1, stack.getCount()); return true; }, null, stack -> {
                assertEquals(1, stack.getCount());
                if (++calls[0] == 2 && fail[0]) return java.util.Optional.empty();
                stack.setCount(0);
                return java.util.Optional.of(new ItemStack(Items.GLASS_BOTTLE));
            });
        state.setInput(0, new ItemStack(Items.HONEY_BOTTLE, 5));
        for (int slot = 1; slot < 18; slot++) state.setInput(slot, new ItemStack(Items.DIAMOND, 64));
        for (int slot = 0; slot < 36; slot++) player.setItem(slot, new ItemStack(Items.DIAMOND, 64));
        menu.clicked(0, 0, ClickType.PICKUP, null);
        assertEquals(2, calls[0]);
        assertEquals(5, state.getInput(0).getCount());
        assertEquals(0, inputs.dirty);
        fail[0] = false;
        calls[0] = 0;
        menu.clicked(0, 0, ClickType.PICKUP, null);
        assertEquals(3, calls[0]);
        assertEquals(5, state.getInput(0).getCount());
        assertTrue(menu.getCarried().isEmpty());
        assertEquals(0, inputs.dirty);
        state.setInput(17, ItemStack.EMPTY);
        calls[0] = 0;
        menu.clicked(0, 0, ClickType.PICKUP, null);
        assertEquals(3, calls[0]);
        assertTrue(state.getInput(0).is(Items.HONEY_BOTTLE));
        assertEquals(2, state.getInput(0).getCount());
        assertTrue(state.getInput(17).is(Items.GLASS_BOTTLE));
        assertEquals(3, state.getInput(17).getCount());
        assertEquals(2, menu.getCarried().getCount());
        assertEquals(1, inputs.dirty);
        menu.clicked(0, 0, ClickType.PICKUP, null);
        assertEquals(3, calls[0]);
        assertEquals(2, menu.getCarried().getCount());
    }

    @Test
    void tagChangeDuringMatchingCannotAuthorizePaymentAgainstOldMembership() {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.tryParse("arcanearchives:test_paid_reload"));
        Map<TagKey<Item>, List<Holder<Item>>> original = snapshotTags();
        Map<TagKey<Item>, List<Holder<Item>>> bindings = new HashMap<>(original);
        boolean[] armed = {false};
        try {
            bindings.put(tag, List.of(BuiltInRegistries.ITEM.wrapAsHolder(Items.DIAMOND)));
            BuiltInRegistries.ITEM.bindTags(bindings);
            var f = new Fixture(recipe(new IngredientStack(tag, 1), new IngredientStack(Ingredient.of(Items.GOLD_INGOT), 1, stack -> {
                if (armed[0]) {
                    armed[0] = false;
                    bindings.put(tag, List.of());
                    BuiltInRegistries.ITEM.bindTags(bindings);
                }
                return true;
            })));
            f.state.setInput(0, new ItemStack(Items.DIAMOND));
            f.player.setItem(0, new ItemStack(Items.GOLD_INGOT));
            armed[0] = true;
            f.pickup();
            assertEquals(1, f.state.getInput(0).getCount());
            assertEquals(1, f.player.getItem(0).getCount());
            assertTrue(f.menu.getCarried().isEmpty());
            assertEquals(0, f.inputs.dirty);
        } finally {
            original.putIfAbsent(tag, List.of());
            BuiltInRegistries.ITEM.bindTags(original);
        }
    }

    //? if neoforge {
    /*@Test
    void nativeFluidPreparationOwnsCopiesAndRefusesUnsupportedContainers() {
        for (var item : List.of(Items.WATER_BUCKET, Items.LAVA_BUCKET)) {
            var offered = com.aranaira.arcanearchives.recipe.CraftingCreator.withCreator(
                new ItemStack(item), new java.util.UUID(0, 1), "Container owner");
            var before = offered.copy();
            var result = com.aranaira.arcanearchives.recipe.gct.GemCutterFluidRemainders.prepare(offered).orElseThrow();
            assertTrue(result.is(Items.BUCKET));
            assertEquals(1, result.getCount());
            assertTrue(com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(before, offered));
            assertEquals(1, offered.getCount());
            result.setCount(0);
            assertTrue(offered.is(item));
            assertTrue(com.aranaira.arcanearchives.recipe.gct.GemCutterFluidRemainders.prepare(new ItemStack(item, 2)).isEmpty());
        }
        for (var item : List.of(Items.PAPER, Items.BUCKET, Items.FLINT_AND_STEEL))
            assertTrue(com.aranaira.arcanearchives.recipe.gct.GemCutterFluidRemainders.prepare(new ItemStack(item)).isEmpty());
    }

    @Test
    void nativeRawStorageRecipesConserveNineQuartzAndBlockProperties() throws Exception {
        var registries = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        for (String name : List.of("storage_raw_quartz", "destorage_rawquartz")) {
            boolean packing = name.equals("storage_raw_quartz");
            try (var stream = getClass().getResourceAsStream("/data/arcanearchives/recipe/" + name + ".json")) {
                assertNotNull(stream);
                var json = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
                var recipe = (net.minecraft.world.item.crafting.ShapelessRecipe) net.minecraft.world.item.crafting.Recipe.CODEC.parse(
                    net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE, registries), json).getOrThrow();
                var stacks = new java.util.ArrayList<ItemStack>();
                for (int slot = 0; slot < (packing ? 9 : 1); slot++) {
                    stacks.add(new ItemStack(packing ? ContentRegistry.RAW_QUARTZ.get() : ContentRegistry.STORAGE_RAW_QUARTZ_ITEM.get()));
                }
                var input = net.minecraft.world.item.crafting.CraftingInput.of(packing ? 3 : 1, packing ? 3 : 1, stacks);
                assertTrue(recipe.matches(input, null));
                var output = recipe.assemble(input, registries);
                assertTrue(output.is(packing ? ContentRegistry.STORAGE_RAW_QUARTZ_ITEM.get() : ContentRegistry.RAW_QUARTZ.get()));
                assertEquals(packing ? 1 : 9, output.getCount());
                stacks.set(0, ItemStack.EMPTY);
                assertFalse(recipe.matches(net.minecraft.world.item.crafting.CraftingInput.of(packing ? 3 : 1, packing ? 3 : 1, stacks), null));
            }
        }
        var state = ContentRegistry.STORAGE_RAW_QUARTZ.get().defaultBlockState();
        assertEquals(15, state.getLightEmission());
        assertEquals(1.7F, state.getDestroySpeed(null, net.minecraft.core.BlockPos.ZERO));
        assertTrue(state.requiresCorrectToolForDrops());
        assertFalse(state.hasBlockEntity());
    }

    @Test
    void registeredRawQuartzRecipesPayExactCostsAndRejectNetherQuartz() {
        for (String name : List.of("radiant_dust", "shaped_quartz")) {
            int cost = name.equals("radiant_dust") ? 1 : 2;
            int count = name.equals("radiant_dust") ? 2 : 1;
            Item output = name.equals("radiant_dust") ? ContentRegistry.RADIANT_DUST.get() : ContentRegistry.SHAPED_QUARTZ.get();
            var f = new Fixture(packagedRecipe(name));
            f.state.setInput(0, new ItemStack(Items.QUARTZ, cost));
            f.pickup();
            assertTrue(f.menu.getCarried().isEmpty());
            assertEquals(cost, f.state.getInput(0).getCount());
            f.state.setInput(0, new ItemStack(ContentRegistry.RAW_QUARTZ.get(), cost - 1));
            f.pickup();
            assertTrue(f.menu.getCarried().isEmpty());
            f.state.setInput(0, new ItemStack(ContentRegistry.RAW_QUARTZ.get()));
            f.player.setItem(8, new ItemStack(ContentRegistry.RAW_QUARTZ.get(), cost - 1));
            f.pickup();
            assertTrue(f.menu.getCarried().is(output));
            assertEquals(count, f.menu.getCarried().getCount());
            assertEquals(18, f.state.countEmptyInputs());
            assertTrue(f.player.isEmpty());
            f.pickup();
            assertEquals(count, f.menu.getCarried().getCount());
        }
    }

    @Test
    void paidShapedQuartzFeedsNativeStorageRoundTripWithoutChangingMaterialOrQuantity() throws Exception {
        var f = new Fixture(packagedRecipe("shaped_quartz"));
        f.state.setInput(0, new ItemStack(ContentRegistry.RAW_QUARTZ.get(), 9));
        f.player.setItem(8, new ItemStack(ContentRegistry.RAW_QUARTZ.get(), 9));
        for (int craft = 1; craft <= 9; craft++) {
            f.pickup();
            assertTrue(f.menu.getCarried().is(ContentRegistry.SHAPED_QUARTZ.get()));
            assertEquals(craft, f.menu.getCarried().getCount());
            assertEquals(18 - 2 * craft, f.state.getInput(0).getCount() + f.player.getItem(8).getCount());
        }
        f.pickup();
        assertEquals(9, f.menu.getCarried().getCount());
        assertEquals(18, f.state.countEmptyInputs());
        assertTrue(f.player.isEmpty());

        var registries = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var recipes = new HashMap<String, net.minecraft.world.item.crafting.ShapelessRecipe>();
        for (String name : List.of("storage_shaped_quartz", "destorage_shapedquartz")) {
            try (var stream = getClass().getResourceAsStream("/data/arcanearchives/recipe/" + name + ".json")) {
                assertNotNull(stream);
                var json = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
                recipes.put(name, (net.minecraft.world.item.crafting.ShapelessRecipe) net.minecraft.world.item.crafting.Recipe.CODEC.parse(
                    net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE, registries), json).getOrThrow());
            }
        }
        var packing = recipes.get("storage_shaped_quartz");
        var unpacking = recipes.get("destorage_shapedquartz");
        var quartz = f.menu.getCarried().copy();
        for (int cycle = 0; cycle < 20; cycle++) {
            var grid = new java.util.ArrayList<ItemStack>();
            for (int slot = 0; slot < 9; slot++) grid.add(quartz.split(1));
            assertTrue(quartz.isEmpty());
            var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, grid);
            assertTrue(packing.matches(input, null));
            var block = packing.assemble(input, registries);
            assertTrue(block.is(ContentRegistry.STORAGE_SHAPED_QUARTZ_ITEM.get()));
            assertEquals(1, block.getCount());
            for (Item wrong : List.of(ContentRegistry.RAW_QUARTZ.get(), Items.QUARTZ, Items.AIR)) {
                grid.set(0, new ItemStack(wrong));
                assertFalse(packing.matches(net.minecraft.world.item.crafting.CraftingInput.of(3, 3, grid), null));
            }
            var packed = net.minecraft.world.item.crafting.CraftingInput.of(1, 1, List.of(block));
            assertTrue(unpacking.matches(packed, null));
            quartz = unpacking.assemble(packed, registries);
            assertTrue(quartz.is(ContentRegistry.SHAPED_QUARTZ.get()));
            assertEquals(9, quartz.getCount());
            assertFalse(unpacking.matches(net.minecraft.world.item.crafting.CraftingInput.of(1, 1,
                List.of(new ItemStack(ContentRegistry.STORAGE_RAW_QUARTZ_ITEM.get()))), null));
        }
        var state = ContentRegistry.STORAGE_SHAPED_QUARTZ.get().defaultBlockState();
        assertEquals(15, state.getLightEmission());
        assertEquals(1.7F, state.getDestroySpeed(null, net.minecraft.core.BlockPos.ZERO));
        assertTrue(state.requiresCorrectToolForDrops());
        assertFalse(state.hasBlockEntity());
    }

    @Test
    void packagedTableRecipeMatchesNativeGridAndRejectsMissingOrWrongQuartz() throws Exception {
        Map<TagKey<Item>, List<Holder<Item>>> original = snapshotTags();
        Map<TagKey<Item>, List<Holder<Item>>> bindings = new HashMap<>(original);
        Map<String, Item> defaults = Map.of("diorite_or_marble", Items.DIORITE, "glass_panes", Items.GLASS_PANE,
            "paper", Items.PAPER, "workbenches", Items.CRAFTING_TABLE, "logs", Items.OAK_LOG);
        try (var stream = getClass().getResourceAsStream("/data/arcanearchives/recipe/gemcutters_table.json")) {
            assertNotNull(stream);
            defaults.forEach((name, item) -> bindings.put(TagKey.create(Registries.ITEM, ContentRegistry.id("ingredients/" + name)),
                List.of(BuiltInRegistries.ITEM.wrapAsHolder(item))));
            BuiltInRegistries.ITEM.bindTags(bindings);
            var registries = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
            var json = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
            var recipe = (net.minecraft.world.item.crafting.ShapedRecipe) net.minecraft.world.item.crafting.Recipe.CODEC.parse(
                net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE, registries), json).getOrThrow();
            var grid = new java.util.ArrayList<ItemStack>();
            for (Item item : List.of(Items.DIORITE, Items.GLASS_PANE, Items.PAPER, Items.OAK_LOG, Items.CRAFTING_TABLE,
                    Items.OAK_LOG, ContentRegistry.RAW_QUARTZ.get(), Items.OAK_LOG, ContentRegistry.RAW_QUARTZ.get())) {
                grid.add(new ItemStack(item));
            }
            var input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, grid);
            assertTrue(recipe.matches(input, null));
            ItemStack output = recipe.assemble(input, registries);
            assertTrue(output.is(ContentRegistry.GEMCUTTERS_TABLE_ITEM.get()));
            assertEquals(1, output.getCount());
            output.setCount(10);
            assertEquals(1, recipe.assemble(input, registries).getCount());
            java.util.Collections.swap(grid, 0, 2);
            assertTrue(recipe.matches(net.minecraft.world.item.crafting.CraftingInput.of(3, 3, grid), null));
            grid.set(6, new ItemStack(Items.QUARTZ));
            assertFalse(recipe.matches(net.minecraft.world.item.crafting.CraftingInput.of(3, 3, grid), null));
            grid.set(6, ItemStack.EMPTY);
            assertFalse(recipe.matches(net.minecraft.world.item.crafting.CraftingInput.of(3, 3, grid), null));
        } finally {
            defaults.keySet().forEach(name -> original.putIfAbsent(TagKey.create(Registries.ITEM, ContentRegistry.id("ingredients/" + name)), List.of()));
            BuiltInRegistries.ITEM.bindTags(original);
        }
    }

    @Test
    void registeredProgressionRecipesDeliverRegisteredOutputsUsingRealLoaderLookup() {
        Map<TagKey<Item>, List<Holder<Item>>> original = snapshotTags();
        Map<TagKey<Item>, List<Holder<Item>>> bindings = new HashMap<>(original);
        Map<String, Item> aliases = Map.of("dust_redstone", Items.REDSTONE, "ingot_gold", Items.GOLD_INGOT, "nugget_gold", Items.GOLD_NUGGET);
        try {
            aliases.forEach((name, item) -> bindings.put(TagKey.create(Registries.ITEM, ContentRegistry.id("ingredients/" + name)),
                List.of(BuiltInRegistries.ITEM.wrapAsHolder(item))));
            BuiltInRegistries.ITEM.bindTags(bindings);
            var definition = packagedRecipe("scintillating_inlay");
            var f = new Fixture(definition);
            f.state.setInput(0, new ItemStack(ContentRegistry.RADIANT_DUST.get(), 3));
            f.player.setItem(8, new ItemStack(ContentRegistry.RADIANT_DUST.get(), 3));
            f.state.setInput(1, new ItemStack(Items.REDSTONE, 8));
            f.player.setItem(7, new ItemStack(Items.REDSTONE, 4));
            f.player.setItem(6, new ItemStack(Items.GOLD_INGOT));
            f.state.setInput(17, new ItemStack(Items.GOLD_NUGGET, 6));
            f.pickup();
            assertTrue(f.menu.getCarried().is(ContentRegistry.SCINTILLATING_INLAY.get()));
            assertEquals(1, f.menu.getCarried().getCount());
            assertEquals(18, f.state.countEmptyInputs());
            assertTrue(f.player.isEmpty());
            assertEquals(1, f.inputs.dirty);
            f.pickup();
            assertEquals(1, f.menu.getCarried().getCount());
            var lantern = new Fixture(packagedRecipe("radiant_lantern"));
            lantern.state.setInput(0, new ItemStack(ContentRegistry.RAW_QUARTZ.get()));
            lantern.player.setItem(8, new ItemStack(ContentRegistry.RAW_QUARTZ.get()));
            lantern.pickup();
            assertTrue(lantern.menu.getCarried().isEmpty());
            assertEquals(1, lantern.state.getInput(0).getCount());
            lantern.player.setItem(7, new ItemStack(Items.GOLD_NUGGET));
            lantern.pickup();
            assertTrue(lantern.menu.getCarried().is(ContentRegistry.RADIANT_LANTERN_ITEM.get()));
            assertEquals(4, lantern.menu.getCarried().getCount());
            assertTrue(lantern.player.isEmpty());
            assertEquals(18, lantern.state.countEmptyInputs());
            lantern.pickup();
            assertEquals(4, lantern.menu.getCarried().getCount());
            for (String name : List.of("material_interface", "containment_field", "matrix_brace")) {
                int amount = name.equals("material_interface") ? 1 : 2;
                Item result = switch (name) {
                    case "material_interface" -> ContentRegistry.MATERIAL_INTERFACE.get();
                    case "containment_field" -> ContentRegistry.CONTAINMENT_FIELD.get();
                    default -> ContentRegistry.MATRIX_BRACE.get();
                };
                var component = new Fixture(packagedRecipe(name));
                component.state.setInput(17, new ItemStack(ContentRegistry.SCINTILLATING_INLAY.get()));
                if (!name.equals("matrix_brace")) component.player.setItem(8, new ItemStack(ContentRegistry.SHAPED_QUARTZ.get(), amount));
                component.player.setItem(7, new ItemStack(Items.GOLD_INGOT, amount - 1));
                component.pickup();
                assertTrue(component.menu.getCarried().isEmpty());
                assertEquals(1, component.state.getInput(17).getCount());
                component.player.setItem(7, new ItemStack(Items.GOLD_INGOT, amount));
                assertTrue(component.menu.quickMoveStack(null, 0).is(result));
                assertEquals(18, component.state.countEmptyInputs());
                assertTrue(component.player.getItem(8).is(result));
                assertEquals(1, component.player.getItem(8).getCount());
                assertTrue(component.player.getItem(7).isEmpty());
                assertTrue(component.menu.getCarried().isEmpty());
                assertTrue(component.menu.quickMoveStack(null, 0).isEmpty());
            }
        } finally {
            aliases.keySet().forEach(name -> original.putIfAbsent(TagKey.create(Registries.ITEM, ContentRegistry.id("ingredients/" + name)), List.of()));
            BuiltInRegistries.ITEM.bindTags(original);
        }
    }
    *///?}

    //? if neoforge {
    /*@Test
    void nativeRecipeManagerReplacementUpdatesTheSameMenuAndKeepsSelectedIdentity() {
        var registries = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var manager = new net.minecraft.world.item.crafting.RecipeManager(registries);
        var first = reloaded("paper", 1, true).get(0);
        manager.replaceRecipes(List.of(new net.minecraft.world.item.crafting.RecipeHolder<>(first.name(), first.recipe())));
        var f = new Fixture(() -> GemCutterDataRecipe.entries(manager));
        f.state.setInput(0, new ItemStack(Items.DIAMOND, 8));
        f.pickup();
        assertEquals(7, f.state.getInput(0).getCount());
        assertEquals(2, f.menu.getCarried().getCount());
        var second = reloaded("paper", 3, true).get(0);
        manager.replaceRecipes(List.of(new net.minecraft.world.item.crafting.RecipeHolder<>(second.name(), second.recipe())));
        f.pickup();
        assertEquals(7, f.state.getInput(0).getCount());
        f.pickup();
        assertEquals(4, f.state.getInput(0).getCount());
        assertEquals(4, f.menu.getCarried().getCount());
        manager.replaceRecipes(List.of());
        f.menu.broadcastChanges();
        f.pickup();
        assertTrue(f.menu.getSlot(0).getItem().isEmpty());
        assertEquals(4, f.state.getInput(0).getCount());
        assertEquals(4, f.menu.getCarried().getCount());
    }

    private static GCTRecipe packagedRecipe(String name) {
        try (var stream = GemCuttersTableCraftingTest.class.getResourceAsStream("/data/arcanearchives/recipe/" + name + ".json")) {
            assertNotNull(stream);
            var json = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
            var registries = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
            var decoded = net.minecraft.world.item.crafting.Recipe.CODEC.parse(
                net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE, registries), json).getOrThrow();
            return ((com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe) decoded).definition(ContentRegistry.id(name));
        } catch (java.io.IOException failure) {
            throw new java.io.UncheckedIOException(failure);
        }
    }
    *///?}

    private static Map<TagKey<Item>, List<Holder<Item>>> snapshotTags() {
        Map<TagKey<Item>, List<Holder<Item>>> tags = new HashMap<>();
        BuiltInRegistries.ITEM.getTags().forEach(pair -> tags.put(pair.getFirst(), pair.getSecond().stream().toList()));
        return tags;
    }

    private static final class Fixture {
        GemCutterCraftingState state = new GemCutterCraftingState();
        final Inventory player = new Inventory(null);
        final GCTRecipeList catalog = new GCTRecipeList();
        final Inputs inputs = new Inputs(() -> state);
        final GemCuttersTableMenu menu;
        BooleanSupplier access = () -> true;

        Fixture(Supplier<List<GemCutterDataRecipe.Entry>> recipes) {
            // Reload fixtures use plain diamonds/paper and an explicit non-fluid policy on all leaves.
            menu = new GemCuttersTableMenu(null, 1, player, inputs, catalog, () -> state, () -> access.getAsBoolean(),
                stack -> stack.getItem() instanceof net.minecraft.world.item.FlintAndSteelItem || stack.getItem().hasCraftingRemainingItem(), recipes);
        }

        Fixture(GCTRecipe recipe) {
            catalog.addRecipe(recipe);
            // Fabric plain JUnit has no Transfer API ItemVariantCache mixin. This fixture supplies
            // discovery/return policy doubles; loader-aware NeoForge exercises native lookup and drain.
            //? if fabric {
            menu = new GemCuttersTableMenu(null, 1, player, inputs, catalog, () -> state, () -> access.getAsBoolean(),
                stack -> stack.getItem() instanceof net.minecraft.world.item.FlintAndSteelItem || stack.getItem().hasCraftingRemainingItem(),
                null, stack -> stack.getCount() == 1 && (stack.is(Items.WATER_BUCKET) || stack.is(Items.LAVA_BUCKET))
                    ? java.util.Optional.of(new ItemStack(Items.BUCKET)) : java.util.Optional.empty());
            //?} else {
            /*menu = new GemCuttersTableMenu(null, 1, player, inputs, catalog, () -> state, () -> access.getAsBoolean());
            *///?}
        }

        void pickup() { menu.clicked(0, 0, ClickType.PICKUP, null); }
    }

    private static final class Inputs extends SimpleContainer {
        final Supplier<GemCutterCraftingState> state;
        boolean valid = true;
        int dirty;
        Inputs(Supplier<GemCutterCraftingState> state) { super(18); this.state = state; }
        @Override
        public ItemStack getItem(int slot) { return state.get().getInput(slot); }
        @Override
        public boolean stillValid(Player player) { return valid; }
        @Override
        public void setChanged() { dirty++; }
    }
}
