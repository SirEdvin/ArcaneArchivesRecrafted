package com.aranaira.arcanearchives.recipe.gct;

import com.aranaira.arcanearchives.inventory.handlers.GemCutterInputHandler;
import com.aranaira.arcanearchives.recipe.CraftingCreator;
import com.aranaira.arcanearchives.recipe.IngredientStack;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GCTRecipeTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static ResourceLocation name() {
        return ResourceLocation.tryParse("arcanearchives:test_recipe");
    }

    @Test
    void recipePagesPreserveSevenEntryOrderAndWrapAtEveryBoundary() {
        GCTRecipeList catalog = new GCTRecipeList();
        List<GCTRecipe> definitions = new ArrayList<>();
        for (int size = 0; size <= 22; size++) {
            if (size > 0) {
                definitions.add(new GCTRecipe(ResourceLocation.tryParse("arcanearchives:page_" + size),
                    new ItemStack(Items.PAPER), List.of()));
            }
            catalog.replaceAll(definitions);
            int pages = Math.max(1, (size + 6) / 7);
            assertEquals(pages, catalog.pageCount());
            List<GCTRecipe> combined = new ArrayList<>();
            for (int page = 0; page < pages; page++) {
                List<GCTRecipe> entries = catalog.getRecipePage(page);
                assertTrue(entries.size() <= 7);
                combined.addAll(entries);
                assertEquals((page + 1) % pages, catalog.nextPage(page));
                assertEquals((page + pages - 1) % pages, catalog.previousPage(page));
                assertThrows(UnsupportedOperationException.class, entries::clear);
            }
            assertEquals(definitions, combined);
            for (int invalid : new int[]{Integer.MIN_VALUE, -1, pages, Integer.MAX_VALUE}) {
                assertTrue(catalog.getRecipePage(invalid).isEmpty());
                assertEquals(0, catalog.nextPage(invalid));
                assertEquals(0, catalog.previousPage(invalid));
            }
        }
    }

    @Test
    void recipePageSnapshotsSurviveReplacementAndStalePagesReset() {
        GCTRecipeList catalog = new GCTRecipeList();
        for (int i = 0; i < 8; i++) {
            catalog.addRecipe(new GCTRecipe(ResourceLocation.tryParse("arcanearchives:page_" + i),
                new ItemStack(Items.PAPER), List.of()));
        }
        List<GCTRecipe> oldPage = catalog.getRecipePage(1);
        GCTRecipe original = oldPage.get(0);
        catalog.replaceAll(List.of(new GCTRecipe(original.getName(), new ItemStack(Items.DIAMOND), List.of())));
        assertEquals(List.of(original), oldPage);
        oldPage.get(0).getRecipeOutput().setCount(0);
        assertTrue(oldPage.get(0).getRecipeOutput().is(Items.PAPER));
        assertTrue(catalog.getRecipePage(0).get(0).getRecipeOutput().is(Items.DIAMOND));
        assertTrue(catalog.getRecipePage(1).isEmpty());
        assertEquals(0, catalog.nextPage(1));
        assertEquals(0, catalog.previousPage(1));
        catalog.replaceAll(List.of());
        assertEquals(1, catalog.pageCount());
        assertEquals(0, catalog.previousPage(0));
        assertTrue(catalog.getRecipePage(0).isEmpty());
        assertEquals(List.of(original), oldPage);
    }

    @Test
    void ownsOutputAndIngredientList() {
        ItemStack output = new ItemStack(Items.DIAMOND, 2);
        List<IngredientStack> ingredients = new ArrayList<>(List.of(new IngredientStack(Items.PAPER, 3)));
        GCTRecipe recipe = new GCTRecipe(name(), output, ingredients);
        output.setCount(1);
        ingredients.clear();
        assertEquals(name(), recipe.getName());
        assertEquals(2, recipe.getRecipeOutput().getCount());
        recipe.getRecipeOutput().setCount(10);
        assertEquals(2, recipe.getRecipeOutput().getCount());
        assertEquals(1, recipe.getIngredients().size());
        assertThrows(UnsupportedOperationException.class, () -> recipe.getIngredients().clear());
    }

    @Test
    void previewConservesRepeatedRequirementsAndDoesNotConsume() {
        IngredientStack ingredient = new IngredientStack(Items.PAPER, 2);
        GCTRecipe recipe = new GCTRecipe(name(), new ItemStack(Items.DIAMOND), List.of(ingredient, ingredient));
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.PAPER, 3));
        assertFalse(recipe.matches(input));
        assertTrue(recipe.getMatchingSlots(input).isEmpty());
        input.setStackInSlot(1, new ItemStack(Items.PAPER));
        assertTrue(recipe.matches(input));
        int[] plan = recipe.getMatchingSlots(input).orElseThrow();
        assertEquals(3, plan[0]);
        assertEquals(1, plan[1]);
        plan[0] = 0;
        assertEquals(3, recipe.getMatchingSlots(input).orElseThrow()[0]);
        assertEquals(3, input.getStackInSlot(0).getCount());
        assertEquals(1, input.getStackInSlot(1).getCount());
    }

    @Test
    void creatorStampingDoesNotRewriteRecipeTemplate() {
        GCTRecipe recipe = new GCTRecipe(name(), new ItemStack(Items.PAPER), List.of());
        ItemStack first = CraftingCreator.withCreator(recipe.getRecipeOutput(), new UUID(0, 1), "First");
        ItemStack second = CraftingCreator.withCreator(recipe.getRecipeOutput(), new UUID(0, 2), "Second");
        assertFalse(com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(first, second));
        assertTrue(com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(new ItemStack(Items.PAPER), recipe.getRecipeOutput()));
    }

    @Test
    void recipeConsumptionRevalidatesRatherThanTrustingPreview() {
        IngredientStack ingredient = new IngredientStack(Items.PAPER, 2);
        GCTRecipe recipe = new GCTRecipe(name(), new ItemStack(Items.DIAMOND), List.of(ingredient, ingredient));
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.PAPER, 3));
        input.setStackInSlot(1, new ItemStack(Items.PAPER));
        assertTrue(recipe.matches(input));
        input.extractItem(1, 1, false);
        assertTrue(recipe.consumeIngredients(input).isEmpty());
        assertEquals(3, input.getStackInSlot(0).getCount());
        input.setStackInSlot(1, new ItemStack(Items.PAPER, 2));
        List<ItemStack> consumed = recipe.consumeIngredients(input).orElseThrow();
        assertEquals(4, consumed.stream().mapToInt(ItemStack::getCount).sum());
        assertEquals(1, input.getStackInSlot(1).getCount());
        assertTrue(input.getStackInSlot(0).isEmpty());
        consumed.get(1).setCount(50);
        assertEquals(1, input.getStackInSlot(1).getCount());
        assertTrue(recipe.consumeIngredients(input).isEmpty());
    }

    @Test
    void consumptionPreservesIngredientDataAndUnrelatedInputs() {
        ItemStack marked = CraftingCreator.withCreator(new ItemStack(Items.PAPER, 2), new UUID(0, 1), "Owner");
        GCTRecipe recipe = new GCTRecipe(name(), new ItemStack(Items.DIAMOND), List.of(new IngredientStack(marked)));
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.PAPER, 10));
        input.setStackInSlot(1, marked);
        List<ItemStack> consumed = recipe.consumeIngredients(input).orElseThrow();
        assertEquals(1, consumed.size());
        assertEquals(2, consumed.get(0).getCount());
        assertTrue(com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(marked, consumed.get(0)));
        assertEquals(10, input.getStackInSlot(0).getCount());
        assertTrue(input.getStackInSlot(1).isEmpty());
        assertEquals(2, marked.getCount());
    }

    @Test
    void emptyRequirementsCommitNothingAndNullInventoryIsRejected() {
        GCTRecipe recipe = new GCTRecipe(name(), new ItemStack(Items.PAPER), List.of());
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.DIAMOND, 3));
        assertTrue(recipe.consumeIngredients(input).orElseThrow().isEmpty());
        assertEquals(3, input.getStackInSlot(0).getCount());
        assertThrows(NullPointerException.class, () -> recipe.consumeIngredients(null));
    }

    @Test
    void rejectsMalformedDefinitionsButPreservesEmptyIngredientRecipes() {
        assertThrows(IllegalArgumentException.class, () -> new GCTRecipe(name(), ItemStack.EMPTY, List.of()));
        assertThrows(NullPointerException.class, () -> new GCTRecipe(null, new ItemStack(Items.PAPER), List.of()));
        assertThrows(NullPointerException.class, () -> new GCTRecipe(name(), new ItemStack(Items.PAPER), null));
        GCTRecipe recipe = new GCTRecipe(name(), new ItemStack(Items.PAPER), List.of());
        assertTrue(recipe.matches(new GemCutterInputHandler()));
    }

    @Test
    void recipeSelectsCreatorStampingWithoutChangingPreviewOrOtherPlayersOutput() {
        ItemStack template = new ItemStack(Items.PAPER, 3);
        GCTRecipe recipe = GCTRecipe.withCreator(name(), template, List.of(new IngredientStack(Items.DIAMOND, 1)));
        UUID firstId = new UUID(0, 1), secondId = new UUID(0, 2);
        ItemStack first = recipe.createOutput(firstId, "First");
        ItemStack second = recipe.createOutput(secondId, "Second");
        assertTrue(com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(
            CraftingCreator.withCreator(template, firstId, "First"), first));
        assertTrue(com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(
            CraftingCreator.withCreator(template, secondId, "Second"), second));
        assertEquals(3, first.getCount());
        assertEquals(3, second.getCount());
        first.setCount(0);
        assertEquals(3, recipe.createOutput(firstId, "First").getCount());
        assertTrue(com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(template, recipe.getRecipeOutput()));
        assertFalse(recipe.matches(new GemCutterInputHandler()));
    }

    @Test
    void ordinaryRecipeOutputStaysUnstampedAndIdentityIsRequiredBeforePreparation() {
        ItemStack template = new ItemStack(Items.PAPER, 2);
        for (GCTRecipe recipe : List.of(new GCTRecipe(name(), template, List.of()),
                GCTRecipe.withCreator(name(), template, List.of()))) {
            assertThrows(NullPointerException.class, () -> recipe.createOutput(null, "Player"));
            assertThrows(NullPointerException.class, () -> recipe.createOutput(new UUID(0, 1), null));
            assertEquals(2, recipe.getRecipeOutput().getCount());
        }
        GCTRecipe ordinary = new GCTRecipe(name(), template, List.of());
        ItemStack output = ordinary.createOutput(new UUID(0, 1), "Player");
        assertTrue(com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(template, output));
        output.setCount(0);
        assertEquals(2, ordinary.getRecipeOutput().getCount());
    }

    @Test
    void hiveMembershipChangeDuringMatchingRejectsRecipeConsumption() {
        UUID player = new UUID(0, 1);
        AtomicReference<UUID> owner = new AtomicReference<>(player);
        GCTRecipe recipe = GCTRecipe.withCreator(name(), new ItemStack(Items.PAPER), List.of(
            new IngredientStack(Ingredient.of(Items.DIAMOND), 1, candidate -> {
                owner.set(new UUID(0, 2));
                return true;
            })));
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.DIAMOND, 3));
        assertTrue(recipe.consumeIngredients(input,
            () -> HiveCraftingConditions.canCraftExpulsion(player, owner.get())).isEmpty());
        assertEquals(3, input.getStackInSlot(0).getCount());
        assertTrue(input.getStackInSlot(0).is(Items.DIAMOND));
    }

    @Test
    void recipeReadsCurrentMembershipAndDoesNotTreatLookupFailureAsNonmembership() {
        UUID player = new UUID(0, 1);
        AtomicReference<UUID> owner = new AtomicReference<>(new UUID(0, 2));
        GCTRecipe recipe = GCTRecipe.withCreator(name(), new ItemStack(Items.PAPER),
            List.of(new IngredientStack(Items.DIAMOND, 1)));
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(0, new ItemStack(Items.DIAMOND, 3));
        assertTrue(recipe.matches(input));
        assertTrue(recipe.consumeIngredients(input,
            () -> HiveCraftingConditions.canCraftInvitation(player, owner.get())).isEmpty());
        assertThrows(IllegalStateException.class, () -> recipe.consumeIngredients(input,
            () -> { throw new IllegalStateException("Membership unavailable"); }));
        assertEquals(3, input.getStackInSlot(0).getCount());
        owner.set(null); // Confirmed nonmembership, unlike the failed lookup above.
        assertEquals(1, recipe.consumeIngredients(input,
            () -> HiveCraftingConditions.canCraftInvitation(player, owner.get())).orElseThrow().get(0).getCount());
        assertEquals(2, input.getStackInSlot(0).getCount());
    }

    @Test
    void zeroIngredientRecipeStillChecksConditionsTwiceAndValidatesArguments() {
        GCTRecipe recipe = new GCTRecipe(name(), new ItemStack(Items.PAPER), List.of());
        GemCutterInputHandler input = new GemCutterInputHandler();
        input.setStackInSlot(17, new ItemStack(Items.DIAMOND, 7));
        int[] checks = {0};
        assertTrue(recipe.consumeIngredients(input, () -> ++checks[0] == 1).isEmpty());
        assertEquals(2, checks[0]);
        assertTrue(recipe.consumeIngredients(input, () -> true).orElseThrow().isEmpty());
        assertThrows(NullPointerException.class, () -> recipe.consumeIngredients(input, null));
        assertThrows(NullPointerException.class, () -> recipe.consumeIngredients(null, () -> true));
        assertEquals(7, input.getStackInSlot(17).getCount());
    }
}
