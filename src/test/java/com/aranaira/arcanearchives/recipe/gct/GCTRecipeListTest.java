package com.aranaira.arcanearchives.recipe.gct;

import com.aranaira.arcanearchives.recipe.CraftingCreator;
import com.aranaira.arcanearchives.recipe.IngredientStack;
import com.aranaira.arcanearchives.inventory.handlers.GemCutterInputHandler;
import java.util.List;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GCTRecipeListTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static GCTRecipe recipe(String name) {
        return new GCTRecipe(ResourceLocation.tryParse("arcanearchives:" + name), new ItemStack(Items.PAPER, 2), List.of());
    }

    @Test
    void browsingPagesDuringCommitDoesNotInvalidateCatalogGeneration() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe paid = new GCTRecipe(recipe("paid").getName(), new ItemStack(Items.PAPER, 2),
            List.of(new IngredientStack(Items.DIAMOND, 1)));
        catalog.addRecipe(paid);
        for (int i = 0; i < 8; i++) catalog.addRecipe(recipe("display_" + i));
        GemCutterInputHandler inputs = new GemCutterInputHandler();
        inputs.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
        int[] checks = {0};
        GCTCraftingResult result = catalog.consumeForCraft(paid.getName(), inputs, new UUID(0, 1), "Crafter", () -> {
            checks[0]++;
            assertEquals(2, catalog.pageCount());
            assertEquals(7, catalog.getRecipePage(0).size());
            assertEquals(2, catalog.getRecipePage(1).size());
            assertTrue(catalog.getRecipePage(Integer.MAX_VALUE).isEmpty());
            assertEquals(1, catalog.nextPage(0));
            assertEquals(1, catalog.previousPage(0));
            assertThrows(UnsupportedOperationException.class, () -> catalog.getRecipePage(0).clear());
            catalog.getRecipePage(0).get(0).getRecipeOutput().setCount(0);
            return true;
        }).orElseThrow();
        assertEquals(2, checks[0]);
        assertEquals(3, inputs.getStackInSlot(0).getCount());
        assertEquals(2, result.output().getCount());
        assertTrue(result.output().is(Items.PAPER));
        assertEquals(1, result.consumed().get(0).getCount());
        assertEquals(2, paid.getRecipeOutput().getCount());
    }

    @Test
    void replacementKeepsOrderAndInvalidatesCachedList() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe first = recipe("first"), second = recipe("second"), replacement = recipe("first");
        catalog.addRecipe(first);
        catalog.addRecipe(second);
        List<GCTRecipe> old = catalog.getRecipeList();
        catalog.addRecipe(replacement);
        assertEquals(List.of(first, second), old);
        assertEquals(List.of(replacement, second), catalog.getRecipeList());
        assertSame(replacement, catalog.getRecipe(first.getName()));
        assertEquals(-1, catalog.indexOf(first));
        assertEquals(0, catalog.indexOf(replacement));
        catalog.removeRecipe(first);
        assertEquals(List.of(second), catalog.getRecipeList());
        catalog.addRecipe(replacement);
        assertEquals(List.of(second, replacement), catalog.getRecipeList());
    }

    @Test
    void wholeCatalogReplacementPublishesOrderedDetachedSnapshots() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe oldRecipe = recipe("old"), first = recipe("first"), second = recipe("second");
        catalog.addRecipe(oldRecipe);
        var oldList = catalog.getRecipeList();
        var oldMap = catalog.getRecipes();
        var replacements = new java.util.ArrayList<>(List.of(second, first));
        catalog.replaceAll(replacements);
        replacements.clear();
        assertEquals(List.of(second, first), catalog.getRecipeList());
        assertNull(catalog.getRecipe(oldRecipe.getName()));
        assertEquals(List.of(oldRecipe), oldList);
        assertSame(oldRecipe, oldMap.get(oldRecipe.getName()));
        var installed = catalog.getRecipeList();
        catalog.replaceAll(List.of());
        assertEquals(0, catalog.size());
        assertEquals(List.of(second, first), installed);
    }

    @Test
    void invalidBatchPreservesCatalogAndCachedSnapshot() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe original = recipe("original"), replacement = recipe("replacement");
        catalog.addRecipe(original);
        var before = catalog.getRecipeList();
        assertThrows(NullPointerException.class, () -> catalog.replaceAll(null));
        assertThrows(NullPointerException.class, () -> catalog.replaceAll(java.util.Arrays.asList(replacement, null)));
        assertThrows(IllegalArgumentException.class, () -> catalog.replaceAll(List.of(replacement, recipe("replacement"))));
        assertThrows(IllegalArgumentException.class, () -> catalog.replaceAll(List.of(replacement, replacement)));
        assertSame(before, catalog.getRecipeList());
        assertEquals(List.of(original), catalog.getRecipeList());
    }

    @Test
    void batchPublicationCancelsInFlightCraftEvenWithSameDefinitions() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe original = new GCTRecipe(recipe("original").getName(), new ItemStack(Items.PAPER),
            List.of(new IngredientStack(Items.DIAMOND, 1)));
        catalog.addRecipe(original);
        GemCutterInputHandler inputs = new GemCutterInputHandler();
        inputs.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
        assertTrue(catalog.consumeForCraft(original.getName(), inputs, new UUID(0, 1), "Crafter", () -> {
            catalog.replaceAll(List.of(original));
            return true;
        }).isEmpty());
        assertEquals(4, inputs.getStackInSlot(0).getCount());
        assertTrue(catalog.consumeForCraft(original.getName(), inputs, new UUID(0, 1), "Crafter", () -> {
            assertThrows(IllegalArgumentException.class, () -> catalog.replaceAll(List.of(original, original)));
            return true;
        }).isPresent());
        assertEquals(3, inputs.getStackInSlot(0).getCount());
    }

    @Test
    void publicSnapshotsCannotBypassCatalogWrites() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe first = recipe("first");
        catalog.addRecipe(first);
        var map = catalog.getRecipes();
        var list = catalog.getRecipeList();
        assertThrows(UnsupportedOperationException.class, map::clear);
        assertThrows(UnsupportedOperationException.class, list::clear);
        assertThrows(UnsupportedOperationException.class, () -> map.entrySet().iterator().next().setValue(recipe("second")));
        catalog.removeRecipe(first.getName());
        assertEquals(1, map.size());
        assertEquals(List.of(first), list);
        assertEquals(0, catalog.size());
        assertTrue(catalog.getRecipeList().isEmpty());
    }

    @Test
    void missingIndicesAndDetachedOutputsMatchUpstream() {
        GCTRecipeList catalog = new GCTRecipeList();
        catalog.addRecipe(recipe("first"));
        for (int index : new int[] {-1, 1, Integer.MAX_VALUE}) {
            assertNull(catalog.getRecipeByIndex(index));
            assertTrue(catalog.getOutputByIndex(index).isEmpty());
        }
        catalog.getOutputByIndex(0).setCount(8);
        assertEquals(2, catalog.getOutputByIndex(0).getCount());
        assertNull(catalog.getRecipe(recipe("missing").getName()));
        assertEquals(-1, catalog.indexOf(null));
    }

    @Test
    void outputLookupIgnoresCountButNotDataAndChoosesFirst() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe first = recipe("first"), second = recipe("second");
        catalog.addRecipe(first);
        catalog.addRecipe(second);
        assertSame(first, catalog.getRecipeByOutput(new ItemStack(Items.PAPER, 40)));
        ItemStack marked = CraftingCreator.withCreator(new ItemStack(Items.PAPER), new UUID(0, 1), "Creator");
        assertNull(catalog.getRecipeByOutput(marked));
        GCTRecipe special = new GCTRecipe(recipe("special").getName(), marked, List.of());
        catalog.addRecipe(special);
        assertSame(special, catalog.getRecipeByOutput(marked));
        assertNull(catalog.getRecipeByOutput(ItemStack.EMPTY));
    }

    @Test
    void invalidWritesLeaveCatalogUnchangedAndInstancesAreIndependent() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe first = recipe("first");
        catalog.addRecipe(first);
        assertThrows(NullPointerException.class, () -> catalog.addRecipe(null));
        assertThrows(NullPointerException.class, () -> catalog.removeRecipe((GCTRecipe) null));
        assertThrows(NullPointerException.class, () -> catalog.removeRecipe((ResourceLocation) null));
        assertEquals(List.of(first), catalog.getRecipeList());
        assertEquals(0, new GCTRecipeList().size());
    }

    @Test
    void checkedReplacementPreservesOrderAndOldSnapshots() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe first = recipe("first"), second = recipe("second"), replacement = recipe("first");
        catalog.addRecipe(first);
        catalog.addRecipe(second);
        var oldList = catalog.getRecipeList();
        var oldMap = catalog.getRecipes();
        catalog.replaceRecipe(replacement);
        assertEquals(List.of(replacement, second), catalog.getRecipeList());
        assertSame(replacement, catalog.getRecipe(first.getName()));
        assertEquals(List.of(first, second), oldList);
        assertSame(first, oldMap.get(first.getName()));
        assertEquals(-1, catalog.indexOf(first));
        assertEquals(0, catalog.indexOf(replacement));
    }

    @Test
    void rejectedReplacementDoesNotAddOrInvalidateSnapshot() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe first = recipe("first");
        catalog.addRecipe(first);
        var snapshot = catalog.getRecipeList();
        assertThrows(IndexOutOfBoundsException.class, () -> catalog.replaceRecipe(recipe("missing")));
        assertThrows(NullPointerException.class, () -> catalog.replaceRecipe(null));
        assertSame(snapshot, catalog.getRecipeList());
        assertEquals(1, catalog.size());
        assertSame(first, catalog.getRecipeByIndex(0));
        catalog.removeRecipe(first);
        assertThrows(IndexOutOfBoundsException.class, () -> catalog.replaceRecipe(first));
        assertEquals(0, catalog.size());
    }

    @Test
    void consumptionResolvesCurrentRecipeAndRejectsMissingNames() {
        GCTRecipeList catalog = new GCTRecipeList();
        ResourceLocation name = recipe("first").getName();
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
        assertTrue(catalog.consumeIngredients(name, input, () -> { throw new AssertionError(); }).isEmpty());
        catalog.addRecipe(new GCTRecipe(name, new ItemStack(Items.PAPER), List.of(new IngredientStack(Items.DIAMOND, 1))));
        catalog.replaceRecipe(new GCTRecipe(name, new ItemStack(Items.PAPER), List.of(new IngredientStack(Items.DIAMOND, 3))));
        assertTrue(catalog.consumeIngredients(name, input, () -> false).isEmpty());
        assertEquals(3, catalog.consumeIngredients(name, input, () -> true).orElseThrow().get(0).getCount());
        assertEquals(1, input.getStackInSlot(0).getCount());
        assertThrows(NullPointerException.class, () -> catalog.consumeIngredients(null, input, () -> true));
        assertThrows(NullPointerException.class, () -> catalog.consumeIngredients(name, null, () -> true));
        assertThrows(NullPointerException.class, () -> catalog.consumeIngredients(name, input, null));
    }

    @Test
    void replacementDuringMatchingCannotConsumeOldRecipeCosts() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe replacement = recipe("first");
        GCTRecipe original = new GCTRecipe(replacement.getName(), new ItemStack(Items.PAPER), List.of(
            new IngredientStack(Ingredient.of(Items.DIAMOND), 1, candidate -> {
                catalog.replaceRecipe(replacement);
                return true;
            })));
        catalog.addRecipe(original);
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
        assertTrue(catalog.consumeIngredients(original.getName(), input, () -> true).isEmpty());
        assertEquals(4, input.getStackInSlot(0).getCount());
        assertSame(replacement, catalog.getRecipe(original.getName()));
    }

    @Test
    void removalInsideFinalConditionRejectsEvenZeroIngredientCommit() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe recipe = recipe("first");
        catalog.addRecipe(recipe);
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
        int[] checks = {0};
        assertTrue(catalog.consumeIngredients(recipe.getName(), input, () -> {
            if (++checks[0] == 2) catalog.removeRecipe(recipe);
            return true;
        }).isEmpty());
        assertEquals(2, checks[0]);
        assertEquals(4, input.getStackInSlot(0).getCount());
        assertEquals(0, catalog.size());
    }


    @Test
    void craftedOutputRequiresCompleteConsumptionAndKeepsDetachedRemainderInputs() {
        GCTRecipeList catalog = new GCTRecipeList();
        ResourceLocation name = recipe("crafted").getName();
        UUID creator = new UUID(0, 1);
        GCTRecipe definition = GCTRecipe.withCreator(name, new ItemStack(Items.PAPER, 2),
            List.of(new IngredientStack(Items.DIAMOND, 3)));
        catalog.addRecipe(definition);
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
        assertTrue(catalog.consumeForCraft(name, input, creator, "Crafter", () -> false).isEmpty());
        assertEquals(4, input.getStackInSlot(0).getCount());
        assertThrows(NullPointerException.class, () -> catalog.consumeForCraft(name, input, null, "Crafter", () -> true));
        assertEquals(4, input.getStackInSlot(0).getCount());
        GCTCraftingResult result = catalog.consumeForCraft(name, input, creator, "Crafter", () -> true).orElseThrow();
        assertTrue(ItemStack.matches(definition.createOutput(creator, "Crafter"), result.output()));
        assertEquals(3, result.consumed().get(0).getCount());
        result.output().setCount(40);
        result.consumed().get(0).setCount(40);
        assertEquals(2, result.output().getCount());
        assertEquals(3, result.consumed().get(0).getCount());
        assertEquals(1, input.getStackInSlot(0).getCount());
        assertTrue(catalog.consumeForCraft(name, input, creator, "Crafter", () -> true).isEmpty());
        assertEquals(1, input.getStackInSlot(0).getCount());
    }

    @Test
    void outputBridgeRejectsReplacementAndThrowingConditionsBeforeConsumption() {
        GCTRecipeList catalog = new GCTRecipeList();
        ResourceLocation name = recipe("crafted").getName();
        UUID creator = new UUID(0, 1);
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
        assertTrue(catalog.consumeForCraft(name, input, creator, "Crafter", () -> {
            throw new AssertionError("Missing recipes must not evaluate conditions");
        }).isEmpty());
        catalog.addRecipe(new GCTRecipe(name, new ItemStack(Items.PAPER),
            List.of(new IngredientStack(Items.DIAMOND, 1))));
        assertThrows(IllegalStateException.class, () -> catalog.consumeForCraft(name, input, creator, "Crafter", () -> {
            throw new IllegalStateException("Unavailable authoritative data");
        }));
        assertEquals(4, input.getStackInSlot(0).getCount());
        assertTrue(catalog.consumeForCraft(name, input, creator, "Crafter", () -> {
            catalog.replaceRecipe(new GCTRecipe(name, new ItemStack(Items.GOLD_INGOT), List.of()));
            return true;
        }).isEmpty());
        assertEquals(4, input.getStackInSlot(0).getCount());
        assertTrue(ItemStack.matches(new ItemStack(Items.GOLD_INGOT),
            catalog.consumeForCraft(name, input, creator, "Crafter", () -> true).orElseThrow().output()));
    }

    @Test
    void removingAndRestoringSameRecipeCannotEvadeCommitGuard() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe recipe = new GCTRecipe(recipe("first").getName(), new ItemStack(Items.PAPER),
            List.of(new IngredientStack(Items.DIAMOND, 1)));
        catalog.addRecipe(recipe);
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
        assertTrue(catalog.consumeIngredients(recipe.getName(), input, () -> {
            catalog.removeRecipe(recipe);
            catalog.addRecipe(recipe);
            return true;
        }).isEmpty());
        assertSame(recipe, catalog.getRecipe(recipe.getName()));
        assertEquals(4, input.getStackInSlot(0).getCount());
    }

    @Test
    void outputIsWithheldWhenFinalConditionFailsOrThrows() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe recipe = new GCTRecipe(recipe("late_condition").getName(), new ItemStack(Items.PAPER),
            List.of(new IngredientStack(Items.DIAMOND, 1)));
        catalog.addRecipe(recipe);
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
        int[] checks = {0};
        assertTrue(catalog.consumeForCraft(recipe.getName(), input, new UUID(0, 1), "Crafter",
            () -> ++checks[0] == 1).isEmpty());
        assertEquals(2, checks[0]);
        assertEquals(4, input.getStackInSlot(0).getCount());
        checks[0] = 0;
        assertThrows(IllegalStateException.class, () -> catalog.consumeForCraft(recipe.getName(), input,
            new UUID(0, 1), "Crafter", () -> {
                if (++checks[0] == 2) throw new IllegalStateException("Membership lookup failed at commit");
                return true;
            }));
        assertEquals(2, checks[0]);
        assertEquals(4, input.getStackInSlot(0).getCount());
    }

    @Test
    void outputIsWithheldWhenMatchingMutatesLiveInputData() {
        GCTRecipeList catalog = new GCTRecipeList();
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
        input.setStackInSlot(17, new ItemStack(Items.GOLD_INGOT, 7));
        ItemStack changed = CraftingCreator.withCreator(new ItemStack(Items.DIAMOND, 4), new UUID(0, 2), "Other");
        GCTRecipe recipe = new GCTRecipe(recipe("live_mutation").getName(), new ItemStack(Items.PAPER), List.of(
            new IngredientStack(Ingredient.of(Items.DIAMOND), 1, candidate -> {
                input.setStackInSlot(0, changed);
                return true;
            })));
        catalog.addRecipe(recipe);
        assertTrue(catalog.consumeForCraft(recipe.getName(), input, new UUID(0, 1), "Crafter", () -> true).isEmpty());
        assertTrue(ItemStack.matches(changed, input.getStackInSlot(0)));
        assertTrue(ItemStack.matches(new ItemStack(Items.GOLD_INGOT, 7), input.getStackInSlot(17)));
    }

    @Test
    void outputIsWithheldOnFinalConditionCatalogRestore() {
        GCTRecipeList catalog = new GCTRecipeList();
        GCTRecipe recipe = new GCTRecipe(recipe("late_restore").getName(), new ItemStack(Items.PAPER),
            List.of(new IngredientStack(Items.DIAMOND, 1)));
        catalog.addRecipe(recipe);
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
        int[] checks = {0};
        assertTrue(catalog.consumeForCraft(recipe.getName(), input, new UUID(0, 1), "Crafter", () -> {
            if (++checks[0] == 2) {
                catalog.removeRecipe(recipe);
                catalog.addRecipe(recipe);
            }
            return true;
        }).isEmpty());
        assertEquals(2, checks[0]);
        assertSame(recipe, catalog.getRecipe(recipe.getName()));
        assertEquals(4, input.getStackInSlot(0).getCount());
    }

    @Test
    void restoreDuringMatchingAndUnrelatedWritesAlsoCancelCommit() {
        GCTRecipeList catalog = new GCTRecipeList();
        ResourceLocation name = recipe("first").getName();
        GCTRecipe original = new GCTRecipe(name, new ItemStack(Items.PAPER), List.of(
            new IngredientStack(Ingredient.of(Items.DIAMOND), 1, candidate -> {
                GCTRecipe current = catalog.getRecipe(name);
                catalog.replaceRecipe(recipe("first"));
                catalog.replaceRecipe(current);
                return true;
            })));
        catalog.addRecipe(original);
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
        assertTrue(catalog.consumeIngredients(name, input, () -> true).isEmpty());
        assertSame(original, catalog.getRecipe(name));
        assertEquals(4, input.getStackInSlot(0).getCount());
        catalog.replaceRecipe(recipe("first"));
        assertTrue(catalog.consumeIngredients(name, input, () -> {
            catalog.addRecipe(recipe("unrelated"));
            return true;
        }).isEmpty());
        assertTrue(catalog.consumeIngredients(name, input, () -> true).isPresent());
        assertEquals(4, input.getStackInSlot(0).getCount());
    }
}
