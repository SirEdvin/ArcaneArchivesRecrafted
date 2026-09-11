package com.aranaira.arcanearchives.recipe.gct;

import com.aranaira.arcanearchives.recipe.IngredientStack;
import com.aranaira.arcanearchives.recipe.CraftingCreator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class GemCutterCraftingStateTest {
    private static final UUID PLAYER = new UUID(0, 1);

    @TempDir
    Path directory;

    private void writeTag(Path file, CompoundTag tag) throws IOException {
        try (DataOutputStream output = new DataOutputStream(Files.newOutputStream(file))) {
            NbtIo.write(tag, output);
        }
    }

    private CompoundTag readTag(Path file) throws IOException {
        try (DataInputStream input = new DataInputStream(Files.newInputStream(file))) {
            return NbtIo.read(input);
        }
    }

    @Test
    void outputAndInputsCommitTogetherAndInvalidOrStaleOutputCannotConsumeInputs() {
        var state = new GemCutterCraftingState();
        state.setInput(0, new ItemStack(Items.DIAMOND, 4));
        state.setOutput(new ItemStack(Items.PAPER, 60));
        var before = state.inputSnapshot();
        var remaining = before.stream().map(ItemStack::copy).toList();
        remaining.get(0).shrink(2);
        var saved = save(state);
        assertThrows(IllegalArgumentException.class, () -> state.commitCraft(before, remaining,
            state.getOutput(), new ItemStack(Items.PAPER, 65), () -> true));
        assertEquals(saved, save(state));
        assertFalse(state.commitCraft(before, remaining, new ItemStack(Items.PAPER, 59),
            new ItemStack(Items.PAPER, 62), () -> true));
        assertEquals(saved, save(state));
        assertThrows(IllegalStateException.class, () -> state.commitCraft(before, remaining,
            state.getOutput(), new ItemStack(Items.PAPER, 62), () -> { state.setOutput(ItemStack.EMPTY); return true; }));
        assertEquals(saved, save(state));
        assertTrue(state.commitCraft(before, remaining, state.getOutput(), new ItemStack(Items.PAPER, 62), () -> true));
        assertEquals(2, state.getInput(0).getCount());
        assertEquals(62, state.getOutput().getCount());
        remaining.get(0).setCount(0);
        assertEquals(2, state.getInput(0).getCount());
    }

    @Test
    void savedOutputIsOrdinaryDetachedInventoryWithoutJournalOrInputInsertion() {
        var state = new GemCutterCraftingState();
        var output = CraftingCreator.withCreator(new ItemStack(Items.PAPER, 4), PLAYER, "Crafter");
        state.setOutput(output);
        for (int slot = 0; slot < 18; slot++) state.setInput(slot, new ItemStack(Items.DIAMOND, 64));
        assertEquals(4, state.insertInputStacked(output, false).getCount());
        assertEquals(4, state.acceptRoutingInput(output, false).getCount());
        assertEquals(4, state.getOutput().getCount());
        var saved = save(state);
        assertEquals(java.util.Set.of("Inputs", "Output"), saved.getAllKeys());
        var restored = new GemCutterCraftingState();
        load(restored, saved);
        assertTrue(ItemStack.matches(output, restored.getOutput()));
        output.setCount(0);
        restored.getOutput().setCount(0);
        assertEquals(saved, save(restored));
    }

    @Test
    void inputSnapshotsPreserveSlotOrderAndDetachAcrossCraftAndLoad() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        state.setInput(0, new ItemStack(Items.DIAMOND, 4));
        ItemStack tagged = CraftingCreator.withCreator(new ItemStack(Items.PAPER, 7), PLAYER, "Crafter");
        state.setInput(17, tagged);
        List<ItemStack> before = state.inputSnapshot();
        assertEquals(18, before.size());
        for (int slot = 1; slot < 17; slot++) assertTrue(before.get(slot).isEmpty());
        assertTrue(ItemStack.matches(tagged, before.get(17)));
        assertThrows(UnsupportedOperationException.class, before::clear);
        assertTrue(craft(state, catalog(false), () -> true));
        assertEquals(4, before.get(0).getCount());
        assertEquals(3, state.inputSnapshot().get(0).getCount());
        CompoundTag committed = save(state);
        before.get(0).setCount(0);
        before.get(17).setCount(0);
        state.inputSnapshot().get(17).setCount(1);
        assertEquals(committed, save(state));
        GemCutterCraftingState restored = new GemCutterCraftingState();
        load(restored, committed);
        List<ItemStack> loaded = restored.inputSnapshot();
        for (int slot = 0; slot < 18; slot++) assertTrue(ItemStack.matches(state.getInput(slot), loaded.get(slot)));
        assertTrue(!restored.getOutput().isEmpty());
    }

    @Test
    void diskRoundTripRestoresJointStateWithoutRecrafting() throws IOException {
        GemCutterCraftingState state = new GemCutterCraftingState();
        state.setInput(0, new ItemStack(Items.DIAMOND, 4));
        state.setInput(17, new ItemStack(Items.GOLD_INGOT, 7));
        assertTrue(craft(state, catalog(false), () -> true));
        CompoundTag expected = save(state);
        Path file = directory.resolve("crafting.nbt");
        writeTag(file, expected);
        GemCutterCraftingState restored = new GemCutterCraftingState();
        load(restored, readTag(file));
        assertEquals(expected, save(restored));
        assertEquals(3, restored.getInput(0).getCount());
        assertEquals(7, restored.getInput(17).getCount());
        assertEquals(2, restored.getOutput().getCount());
        assertFalse(craft(restored, catalog(false), () -> { throw new AssertionError("Output result must survive disk load"); }));
        writeTag(file, save(restored));
        assertEquals(expected, readTag(file));
    }

    @Test
    void truncatedDiskRecordCannotReplaceLiveState() throws IOException {
        GemCutterCraftingState state = new GemCutterCraftingState();
        state.setInput(0, new ItemStack(Items.DIAMOND, 4));
        assertTrue(craft(state, catalog(false), () -> true));
        CompoundTag before = save(state);
        Path file = directory.resolve("truncated.nbt");
        writeTag(file, before);
        byte[] complete = Files.readAllBytes(file);
        for (int length : new int[]{0, 1, complete.length / 2, complete.length - 1}) {
            byte[] truncated = Arrays.copyOf(complete, length);
            Files.write(file, truncated);
            Throwable failure = assertThrows(Exception.class, () -> load(state, readTag(file)));
            while (failure.getCause() != null) failure = failure.getCause();
            assertInstanceOf(EOFException.class, failure);
            assertEquals(before, save(state));
            assertArrayEquals(truncated, Files.readAllBytes(file));
        }
    }

    @Test
    void validNbtWithInvalidOutputRecordCannotPartiallyLoadFromDisk() throws IOException {
        GemCutterCraftingState live = new GemCutterCraftingState();
        live.setInput(0, new ItemStack(Items.DIAMOND, 4));
        assertTrue(craft(live, catalog(false), () -> true));
        CompoundTag before = save(live);
        CompoundTag invalid = before.copy();
        invalid.put("Inputs", save(new GemCutterCraftingState()).getCompound("Inputs"));
        invalid.getCompound("Output").putInt("Size", 2);
        Path file = directory.resolve("invalid.nbt");
        writeTag(file, invalid);
        assertThrows(IllegalArgumentException.class, () -> load(live, readTag(file)));
        assertEquals(before, save(live));
        assertEquals(invalid, readTag(file));
    }

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static ResourceLocation name() {
        return ResourceLocation.tryParse("arcanearchives:staged");
    }

    private static GCTRecipeList catalog(boolean free) {
        GCTRecipeList catalog = new GCTRecipeList();
        catalog.addRecipe(GCTRecipe.withCreator(name(), new ItemStack(Items.PAPER, 2),
            free ? List.of() : List.of(new IngredientStack(Items.DIAMOND, 1))));
        return catalog;
    }

    private static boolean craft(GemCutterCraftingState state, GCTRecipeList catalog, BooleanSupplier condition) {
        // This fixture tests the inventory commit, not menu recipe authorization.
        if (!state.getOutput().isEmpty()) return false;
        GCTRecipe recipe = catalog.getRecipe(name());
        if (recipe == null) return false;
        var before = state.inputSnapshot();
        var allocation = recipe.getMatchingSlots(before);
        if (allocation.isEmpty()) return false;
        var remaining = before.stream().map(ItemStack::copy).toList();
        for (int slot = 0; slot < remaining.size(); slot++) remaining.get(slot).shrink(allocation.get()[slot]);
        return state.commitCraft(before, remaining, ItemStack.EMPTY, recipe.createOutput(PLAYER, "Crafter"), condition);
    }

    private static CompoundTag save(GemCutterCraftingState state) {
        return state.serializeNBT(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    }

    private static void load(GemCutterCraftingState state, CompoundTag tag) {
        state.deserializeNBT(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), tag);
    }

    @Test
    void jointRoundTripRetainsDeductedInputsAndBlocksRepeatCraft() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        state.setInput(0, new ItemStack(Items.DIAMOND, 4));
        state.setInput(17, new ItemStack(Items.GOLD_INGOT, 7));
        GCTRecipeList catalog = catalog(false);
        assertTrue(craft(state, catalog, () -> true));
        CompoundTag saved = save(state);
        GemCutterCraftingState restored = new GemCutterCraftingState();
        load(restored, saved);
        assertEquals(saved, save(restored));
        assertEquals(3, restored.getInput(0).getCount());
        assertEquals(7, restored.getInput(17).getCount());
        assertEquals(2, restored.getOutput().getCount());
        assertTrue(ItemStack.matches(state.getOutput(), restored.getOutput()));
        assertFalse(craft(restored, catalog, () -> { throw new AssertionError("Output must gate conditions"); }));
        restored.getInput(0).setCount(0);
        restored.getOutput().setCount(0);
        assertEquals(saved, save(restored));
    }

    @Test
    void liveEmptyCountTracksTransfersCraftAndLoadWithoutCountingOutputOutput() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        assertEquals(18, state.inputSlots());
        assertEquals(18, state.countEmptyInputs());
        state.insertInputStacked(new ItemStack(Items.DIAMOND, 65), true);
        assertEquals(18, state.countEmptyInputs());
        state.insertInputStacked(new ItemStack(Items.DIAMOND, 65), false);
        assertEquals(16, state.countEmptyInputs());
        state.extractInput(0, 64, true);
        assertEquals(16, state.countEmptyInputs());
        state.extractInput(0, 64, false);
        assertEquals(17, state.countEmptyInputs());
        assertTrue(craft(state, catalog(false), () -> true));
        assertEquals(18, state.countEmptyInputs());
        state.setInput(17, new ItemStack(Items.PAPER));
        state.getInput(17).setCount(0);
        assertEquals(17, state.countEmptyInputs());
        GemCutterCraftingState restored = new GemCutterCraftingState();
        load(restored, save(state));
        assertEquals(17, restored.countEmptyInputs());
        assertEquals(18, restored.inputSlots());
        assertTrue(!restored.getOutput().isEmpty());
        restored.setInput(17, ItemStack.EMPTY);
        assertEquals(18, restored.countEmptyInputs());
        for (int slot = 0; slot < restored.inputSlots(); slot++) {
            restored.setInput(slot, new ItemStack(Items.IRON_SWORD));
        }
        assertEquals(0, restored.countEmptyInputs());
    }

    @Test
    void routingOnlyTopsUpExistingMatchesAndPreservesOutputAcrossLoad() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        assertTrue(craft(state, catalog(true), () -> true));
        state.setInput(1, new ItemStack(Items.DIAMOND, 63));
        state.setInput(17, new ItemStack(Items.DIAMOND, 60));
        ItemStack offered = new ItemStack(Items.DIAMOND, 10);
        CompoundTag before = save(state);
        ItemStack simulated = state.acceptRoutingInput(offered, true);
        assertEquals(5, simulated.getCount());
        simulated.setCount(0);
        assertEquals(before, save(state));
        ItemStack remainder = state.acceptRoutingInput(offered, false);
        assertEquals(5, remainder.getCount());
        assertEquals(64, state.getInput(1).getCount());
        assertEquals(64, state.getInput(17).getCount());
        assertTrue(state.getInput(0).isEmpty());
        remainder.setCount(0);
        assertEquals(10, offered.getCount());
        assertEquals(before.getCompound("Output"), save(state).getCompound("Output"));
        GemCutterCraftingState restored = new GemCutterCraftingState();
        load(restored, save(state));
        assertEquals(save(state), save(restored));
        assertFalse(craft(restored, catalog(true), () -> true));
    }

    @Test
    void routingRejectsUnseededDifferentDataAndNativeFullInputsWithoutMutation() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        ItemStack tagged = CraftingCreator.withCreator(new ItemStack(Items.PAPER, 10), PLAYER, "Crafter");
        state.setInput(0, new ItemStack(Items.PAPER, 60));
        state.setInput(1, new ItemStack(Items.IRON_SWORD));
        CompoundTag before = save(state);
        for (boolean simulate : List.of(true, false)) {
            assertTrue(ItemStack.matches(tagged, state.acceptRoutingInput(tagged, simulate)));
            assertEquals(3, state.acceptRoutingInput(new ItemStack(Items.IRON_SWORD, 3), simulate).getCount());
            assertEquals(10, state.acceptRoutingInput(new ItemStack(Items.DIAMOND, 10), simulate).getCount());
            assertTrue(state.acceptRoutingInput(ItemStack.EMPTY, simulate).isEmpty());
            assertThrows(NullPointerException.class, () -> state.acceptRoutingInput(null, simulate));
            assertEquals(before, save(state));
        }
        state.setInput(17, CraftingCreator.withCreator(new ItemStack(Items.PAPER, 62), PLAYER, "Crafter"));
        ItemStack remainder = state.acceptRoutingInput(tagged, false);
        assertEquals(8, remainder.getCount());
        assertTrue(ItemStack.matches(CraftingCreator.withCreator(new ItemStack(Items.PAPER, 8), PLAYER, "Crafter"), remainder));
        assertEquals(60, state.getInput(0).getCount());
        assertEquals(64, state.getInput(17).getCount());
    }

    @Test
    void stackedInsertionPrefersMatchingSlotsAndPreservesOutputResult() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        assertTrue(craft(state, catalog(true), () -> true));
        state.setInput(17, new ItemStack(Items.DIAMOND, 60));
        CompoundTag before = save(state);
        ItemStack offered = new ItemStack(Items.DIAMOND, 10);
        assertTrue(state.insertInputStacked(offered, true).isEmpty());
        assertEquals(before, save(state));
        assertTrue(state.insertInputStacked(offered, false).isEmpty());
        assertEquals(64, state.getInput(17).getCount());
        assertEquals(6, state.getInput(0).getCount());
        assertEquals(10, offered.getCount());
        assertEquals(before.getCompound("Output"), save(state).getCompound("Output"));
        GemCutterCraftingState restored = new GemCutterCraftingState();
        load(restored, save(state));
        assertEquals(save(state), save(restored));
        assertFalse(craft(restored, catalog(true), () -> true));
    }

    @Test
    void stackedInsertionReturnsDetachedOverflowAndRejectsNullWithoutMutation() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        for (int slot = 0; slot < 18; slot++) state.setInput(slot, new ItemStack(Items.DIAMOND, 64));
        state.setInput(17, new ItemStack(Items.DIAMOND, 63));
        CompoundTag before = save(state);
        ItemStack offered = new ItemStack(Items.DIAMOND, 10);
        ItemStack simulated = state.insertInputStacked(offered, true);
        assertEquals(9, simulated.getCount());
        simulated.setCount(0);
        assertEquals(before, save(state));
        assertThrows(NullPointerException.class, () -> state.insertInputStacked(null, false));
        assertEquals(before, save(state));
        ItemStack remainder = state.insertInputStacked(offered, false);
        assertEquals(9, remainder.getCount());
        remainder.setCount(0);
        assertEquals(64, state.getInput(17).getCount());
        assertEquals(10, offered.getCount());
    }

    @Test
    void stackedInsertionKeepsDifferentItemDataSeparate() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        ItemStack tagged = CraftingCreator.withCreator(new ItemStack(Items.PAPER, 10), PLAYER, "Crafter");
        ItemStack expected = tagged.copy();
        state.setInput(0, new ItemStack(Items.PAPER, 60));
        for (int slot = 1; slot < 17; slot++) state.setInput(slot, new ItemStack(Items.DIAMOND, 64));
        CompoundTag before = save(state);
        assertTrue(state.insertInputStacked(tagged, true).isEmpty());
        assertEquals(before, save(state));
        assertTrue(state.insertInputStacked(tagged, false).isEmpty());
        assertTrue(ItemStack.matches(new ItemStack(Items.PAPER, 60), state.getInput(0)));
        assertTrue(ItemStack.matches(expected, state.getInput(17)));
        tagged.setCount(0);
        assertTrue(ItemStack.matches(expected, state.getInput(17)));
        GemCutterCraftingState restored = new GemCutterCraftingState();
        load(restored, save(state));
        assertEquals(save(state), save(restored));
    }

    @Test
    void stackedNonstackableOverflowPreservesCountAndDamage() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        for (int slot = 0; slot < 17; slot++) state.setInput(slot, new ItemStack(Items.DIAMOND, 64));
        ItemStack offered = new ItemStack(Items.IRON_SWORD, 3);
        offered.setDamageValue(7);
        CompoundTag before = save(state);
        ItemStack simulated = state.insertInputStacked(offered, true);
        assertEquals(2, simulated.getCount());
        assertEquals(7, simulated.getDamageValue());
        simulated.setDamageValue(20);
        assertEquals(before, save(state));
        ItemStack remainder = state.insertInputStacked(offered, false);
        assertEquals(2, remainder.getCount());
        assertEquals(7, remainder.getDamageValue());
        assertEquals(1, state.getInput(17).getCount());
        assertEquals(7, state.getInput(17).getDamageValue());
        remainder.setDamageValue(30);
        assertEquals(7, offered.getDamageValue());
        assertEquals(3, offered.getCount());
        assertEquals(7, state.getInput(17).getDamageValue());
        CompoundTag full = save(state);
        assertEquals(3, state.insertInputStacked(offered, false).getCount());
        assertEquals(full, save(state));
    }

    @Test
    void malformedOutputDoesNotReplaceValidLiveInputsOrResult() {
        GemCutterCraftingState live = new GemCutterCraftingState();
        live.setInput(0, new ItemStack(Items.DIAMOND, 4));
        assertTrue(craft(live, catalog(false), () -> true));
        CompoundTag before = save(live);
        CompoundTag broken = before.copy();
        broken.put("Inputs", save(new GemCutterCraftingState()).getCompound("Inputs"));
        broken.getCompound("Output").putInt("Size", 2);
        assertThrows(IllegalArgumentException.class, () -> load(live, broken));
        assertEquals(before, save(live));
        broken.putString("Output", "not an inventory");
        assertThrows(IllegalArgumentException.class, () -> load(live, broken));
        assertEquals(before, save(live));
        broken.remove("Output");
        assertThrows(IllegalArgumentException.class, () -> load(live, broken));
        assertEquals(before, save(live));
    }

    @Test
    void idleRoundTripAndZeroCostCraftStillRespectOutputState() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        CompoundTag idle = save(state);
        load(state, idle);
        assertTrue(state.getOutput().isEmpty());
        assertFalse(craft(state, catalog(true), () -> false));
        assertEquals(idle, save(state));
        assertTrue(craft(state, catalog(true), () -> true));
        assertFalse(craft(state, catalog(true), () -> true));
        GemCutterCraftingState restored = new GemCutterCraftingState();
        load(restored, save(state));
        assertFalse(craft(restored, catalog(true), () -> true));
    }

    @Test
    void outputCraftSurvivesCatalogReplacementAndRemovalAcrossDiskLoad() throws IOException {
        GemCutterCraftingState state = new GemCutterCraftingState();
        state.setInput(0, new ItemStack(Items.DIAMOND, 4));
        GCTRecipeList catalog = catalog(false);
        assertTrue(craft(state, catalog, () -> true));
        CompoundTag committed = save(state);
        ItemStack originalOutput = state.getOutput();
        catalog.replaceAll(List.of(new GCTRecipe(name(), new ItemStack(Items.GOLD_INGOT, 64), List.of())));
        assertFalse(craft(state, catalog, () -> { throw new AssertionError("Output must gate replacement conditions"); }));
        assertEquals(committed, save(state));
        Path file = directory.resolve("catalog-replaced.nbt");
        writeTag(file, save(state));
        catalog.replaceAll(List.of());
        GemCutterCraftingState restored = new GemCutterCraftingState();
        load(restored, readTag(file));
        assertEquals(committed, save(restored));
        assertTrue(ItemStack.matches(originalOutput, restored.getOutput()));
        assertEquals(3, restored.getInput(0).getCount());
        assertEquals(2, restored.getOutput().getCount());
        assertFalse(craft(restored, catalog, () -> { throw new AssertionError("Removed recipe must not discard output output"); }));
        assertEquals(committed, save(restored));
    }

    @Test
    void callbacksCannotReenterCraftLoadSaveOrInputWritesAndGuardRecovers() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        state.setInput(0, new ItemStack(Items.DIAMOND, 4));
        GCTRecipeList catalog = catalog(false);
        CompoundTag before = save(state);
        List<Runnable> attempts = List.of(
            () -> craft(state, catalog, () -> true),
            () -> load(state, before),
            () -> save(state),
            () -> state.insertInput(0, new ItemStack(Items.DIAMOND), false),
            () -> state.extractInput(0, 1, false),
            () -> state.insertInput(0, new ItemStack(Items.DIAMOND), true),
            () -> state.extractInput(0, 1, true),
            () -> state.insertInputStacked(new ItemStack(Items.DIAMOND), false),
            () -> state.insertInputStacked(new ItemStack(Items.DIAMOND), true),
            () -> state.acceptRoutingInput(new ItemStack(Items.DIAMOND), false),
            () -> state.acceptRoutingInput(new ItemStack(Items.DIAMOND), true),
            () -> state.setInput(0, ItemStack.EMPTY));
        for (Runnable attempt : attempts) {
            assertThrows(IllegalStateException.class, () -> craft(state, catalog, () -> {
                attempt.run();
                return true;
            }));
            assertEquals(before, save(state));
        }
        assertTrue(craft(state, catalog, () -> true));
    }

    @Test
    void failedFinalConditionAndMissingRecipeNeverCommitOutput() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        state.setInput(0, new ItemStack(Items.DIAMOND, 4));
        CompoundTag before = save(state);
        assertFalse(craft(state, new GCTRecipeList(), () -> { throw new AssertionError("Missing recipe"); }));
        int[] checks = {0};
        assertFalse(craft(state, catalog(false), () -> { checks[0]++; return false; }));
        assertEquals(1, checks[0]);
        assertEquals(before, save(state));
    }

    @Test
    void transfersConserveNativeCapacityAndSimulationIsDetached() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        state.setInput(0, new ItemStack(Items.DIAMOND, 60));
        ItemStack offered = new ItemStack(Items.DIAMOND, 10);
        CompoundTag before = save(state);
        ItemStack rejected = state.insertInput(0, offered, true);
        assertEquals(6, rejected.getCount());
        rejected.setCount(0);
        state.extractInput(0, 60, true).setCount(0);
        assertEquals(before, save(state));
        assertEquals(6, state.insertInput(0, offered, false).getCount());
        assertEquals(10, offered.getCount());
        assertEquals(64, state.getInput(0).getCount());
        assertEquals(64, state.extractInput(0, 100, false).getCount());
        assertTrue(state.getInput(0).isEmpty());
        assertEquals(1, state.insertInput(1, new ItemStack(Items.IRON_SWORD, 2), false).getCount());
        assertEquals(1, state.extractInput(1, 100, false).getCount());
    }

    @Test
    void invalidTransfersLeaveStateUnchanged() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        state.setInput(0, new ItemStack(Items.DIAMOND, 4));
        CompoundTag before = save(state);
        assertThrows(IndexOutOfBoundsException.class, () -> state.insertInput(18, new ItemStack(Items.DIAMOND), false));
        assertThrows(IndexOutOfBoundsException.class, () -> state.extractInput(-1, 1, false));
        assertThrows(IllegalArgumentException.class, () -> state.extractInput(0, -1, false));
        assertThrows(NullPointerException.class, () -> state.insertInput(0, null, false));
        assertTrue(state.extractInput(0, 0, false).isEmpty());
        assertEquals(3, state.insertInput(0, new ItemStack(Items.GOLD_INGOT, 3), false).getCount());
        assertEquals(before, save(state));
    }

    @Test
    void transfersWhileOutputCannotAlterOutputDataOrEnableAnotherCraft() {
        GemCutterCraftingState state = new GemCutterCraftingState();
        state.insertInput(0, new ItemStack(Items.DIAMOND, 4), false);
        GCTRecipeList catalog = catalog(false);
        assertTrue(craft(state, catalog, () -> true));
        CompoundTag output = save(state).getCompound("Output").copy();
        assertEquals(3, state.extractInput(0, 64, false).getCount());
        state.insertInput(0, new ItemStack(Items.DIAMOND, 64), false);
        assertFalse(craft(state, catalog, () -> true));
        assertEquals(output, save(state).getCompound("Output"));
        GemCutterCraftingState restored = new GemCutterCraftingState();
        load(restored, save(state));
        assertEquals(64, restored.getInput(0).getCount());
        assertEquals(output, save(restored).getCompound("Output"));
        assertFalse(craft(restored, catalog, () -> true));
    }
}
