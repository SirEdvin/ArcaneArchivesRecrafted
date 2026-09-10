package com.aranaira.arcanearchives.recipe.gct;

import com.aranaira.arcanearchives.recipe.CraftingCreator;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GCTCraftingResultTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    private static CompoundTag save(GCTCraftingResult result) {
        return result.serializeNBT(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    }

    private static GCTCraftingResult load(CompoundTag tag) {
        return GCTCraftingResult.deserializeNBT(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY), tag);
    }

    private static GCTCraftingResult fixture() {
        ItemStack output = CraftingCreator.withCreator(new ItemStack(Items.PAPER, 300), new UUID(0, 1), "Crafter");
        ItemStack tool = new ItemStack(Items.FLINT_AND_STEEL);
        tool.setDamageValue(17);
        return new GCTCraftingResult(output, List.of(new ItemStack(Items.DIAMOND, 64), tool));
    }

    @Test
    void roundTripPreservesOutputCountCreatorAndOrderedConsumedData() {
        GCTCraftingResult original = fixture();
        CompoundTag saved = save(original);
        CompoundTag before = saved.copy();
        GCTCraftingResult restored = load(saved);
        assertEquals(before, saved);
        assertTrue(ItemStack.matches(original.output(), restored.output()));
        assertEquals(2, restored.consumed().size());
        for (int i = 0; i < 2; i++) assertTrue(ItemStack.matches(original.consumed().get(i), restored.consumed().get(i)));
        saved.getCompound("Output").getList("Items", Tag.TAG_COMPOUND).getCompound(0).putInt("ExtendedCount", 1);
        assertEquals(300, restored.output().getCount());
        restored.consumed().get(1).setDamageValue(0);
        assertEquals(17, restored.consumed().get(1).getDamageValue());
    }

    @Test
    void supportsZeroAndFullEighteenSlotConsumption() {
        assertTrue(load(save(new GCTCraftingResult(new ItemStack(Items.PAPER), List.of()))).consumed().isEmpty());
        GCTCraftingResult full = new GCTCraftingResult(new ItemStack(Items.PAPER),
            Collections.nCopies(18, new ItemStack(Items.DIAMOND, 64)));
        assertEquals(18, load(save(full)).consumed().size());
    }

    @Test
    void rejectsMissingWrongTypedAndUnknownVersionData() {
        for (String field : List.of("Version", "Output", "Consumed")) {
            CompoundTag missing = save(fixture());
            missing.remove(field);
            assertThrows(IllegalArgumentException.class, () -> load(missing));
            CompoundTag wrong = save(fixture());
            wrong.putString(field, "invalid");
            assertThrows(IllegalArgumentException.class, () -> load(wrong));
        }
        CompoundTag future = save(fixture());
        future.putInt("Version", 2);
        assertThrows(IllegalArgumentException.class, () -> load(future));
    }

    @Test
    void rejectsEmptyOutputDuplicateSlotsGapsAndOversizedInputs() {
        CompoundTag empty = save(fixture());
        empty.getCompound("Output").put("Items", new ListTag());
        assertThrows(IllegalArgumentException.class, () -> load(empty));
        CompoundTag duplicate = save(fixture());
        ListTag items = duplicate.getCompound("Consumed").getList("Items", Tag.TAG_COMPOUND);
        items.add(items.getCompound(0).copy());
        assertThrows(IllegalArgumentException.class, () -> load(duplicate));
        CompoundTag gap = save(fixture());
        gap.getCompound("Consumed").getList("Items", Tag.TAG_COMPOUND).getCompound(1).putInt("Slot", 2);
        assertThrows(IllegalArgumentException.class, () -> load(gap));
        CompoundTag oversized = save(fixture());
        oversized.getCompound("Consumed").getList("Items", Tag.TAG_COMPOUND).getCompound(1).putInt("ExtendedCount", 2);
        assertThrows(IllegalArgumentException.class, () -> load(oversized));
    }

    @Test
    void constructorRejectsStatesThatCannotComeFromOwnedConsumption() {
        assertThrows(IllegalArgumentException.class, () -> new GCTCraftingResult(ItemStack.EMPTY, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new GCTCraftingResult(new ItemStack(Items.PAPER), List.of(ItemStack.EMPTY)));
        assertThrows(IllegalArgumentException.class, () -> new GCTCraftingResult(new ItemStack(Items.PAPER),
            Collections.nCopies(19, new ItemStack(Items.DIAMOND))));
        assertThrows(IllegalArgumentException.class, () -> new GCTCraftingResult(new ItemStack(Items.PAPER),
            List.of(new ItemStack(Items.DIAMOND, 65))));
    }
}
