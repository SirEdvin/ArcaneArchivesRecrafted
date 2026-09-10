package com.aranaira.arcanearchives.items;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.aranaira.arcanearchives.data.HiveSaveData;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.recipe.CraftingCreator;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class HiveResignationTest {
    private static final UUID OWNER = new UUID(0, 1), FIRST = new UUID(0, 2), SECOND = new UUID(0, 3), THIRD = new UUID(0, 4);
    private static ItemStack letter(UUID creator) {
        return CraftingCreator.withCreator(new ItemStack(ContentRegistry.LETTER_RESIGNATION.get(), 2), creator, "Creator");
    }
    private static CompoundTag save(HiveSaveData data) { return data.save(new CompoundTag(), null); }
    private static HiveSaveData hive(UUID... members) {
        HiveSaveData data = new HiveSaveData();
        for (UUID member : members) assertTrue(data.acceptInvitation(OWNER, member));
        return data;
    }
    @Test void ownerDeparturePromotesOldestAndPreservesRemainingOrderAcrossPersistence() {
        var data = HiveSaveData.load(save(hive(FIRST, SECOND, THIRD)));
        ItemStack letter = letter(OWNER);
        assertEquals("left", LetterOfResignationItem.resign(letter, OWNER, data));
        assertEquals(1, letter.getCount());
        assertNull(data.ownerOf(OWNER));
        assertEquals(FIRST, data.ownerOf(FIRST));
        assertEquals(FIRST, data.ownerOf(SECOND));
        assertEquals(List.of(SECOND, THIRD), new ArrayList<>(data.members(FIRST)));
        data = HiveSaveData.load(save(data));
        assertEquals("left", LetterOfResignationItem.resign(letter(FIRST), FIRST, data));
        assertEquals(SECOND, data.ownerOf(THIRD));
        assertEquals(List.of(THIRD), new ArrayList<>(data.members(SECOND)));
    }
    @Test void ordinaryResignationPreservesOwnerAndBothTwoPersonDeparturesDisband() {
        var data = hive(FIRST, SECOND);
        assertEquals("left", LetterOfResignationItem.resign(letter(FIRST), FIRST, data));
        assertEquals(OWNER, data.ownerOf(SECOND));
        assertNull(data.ownerOf(FIRST));
        for (UUID leaving : List.of(OWNER, FIRST)) {
            data = hive(FIRST);
            assertEquals("left", LetterOfResignationItem.resign(letter(leaving), leaving, data));
            assertNull(data.ownerOf(OWNER));
            assertNull(data.ownerOf(FIRST));
            assertEquals(save(new HiveSaveData()), save(HiveSaveData.load(save(data))));
        }
    }
    @Test void invalidUnauthorizedAndRepeatedResignationsDoNotConsumeOrMutate() {
        var data = hive(FIRST);
        CompoundTag before = save(data);
        data.setDirty(false);
        ItemStack wrong = letter(OWNER);
        assertEquals("leaving_failed", LetterOfResignationItem.resign(wrong, FIRST, data));
        assertEquals(2, wrong.getCount());
        ItemStack absent = letter(THIRD);
        assertEquals("left_failed", LetterOfResignationItem.resign(absent, THIRD, data));
        assertEquals(2, absent.getCount());
        assertEquals("invalid", LetterOfResignationItem.resign(new ItemStack(ContentRegistry.LETTER_RESIGNATION.get()), FIRST, data));
        assertEquals("invalid", LetterOfResignationItem.resign(ItemStack.EMPTY, FIRST, data));
        assertEquals("invalid", LetterOfResignationItem.resign(CraftingCreator.withCreator(
            new ItemStack(ContentRegistry.LETTER_INVITATION.get()), FIRST, "First"), FIRST, data));
        assertFalse(data.isDirty());
        assertEquals(before, save(data));
        ItemStack valid = letter(FIRST);
        assertEquals("left", LetterOfResignationItem.resign(valid, FIRST, data));
        assertEquals("left_failed", LetterOfResignationItem.resign(valid, FIRST, data));
        assertEquals(1, valid.getCount());
    }
    @Test void oldInvitationBecomesEligibleAfterItsAuthorResigns() {
        var data = hive(FIRST, SECOND);
        ItemStack invitation = CraftingCreator.withCreator(new ItemStack(ContentRegistry.LETTER_INVITATION.get()), FIRST, "First");
        assertEquals("failed", LetterOfInvitationItem.accept(invitation, THIRD, data));
        assertEquals("left", LetterOfResignationItem.resign(letter(FIRST), FIRST, data));
        assertEquals("joined", LetterOfInvitationItem.accept(invitation, THIRD, data));
        assertEquals(FIRST, data.ownerOf(THIRD));
        assertEquals(OWNER, data.ownerOf(SECOND));
    }
    @Test void registeredRecipePreservesResignationConditionAndCreatorAcrossWire() throws Exception {
        try (var stream = getClass().getResourceAsStream("/data/arcanearchives/recipe/letter_resignation.json")) {
            assertNotNull(stream);
            var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("resignation", json.get("hive").getAsString());
            var id = ResourceLocation.tryParse("arcanearchives:letter_resignation");
            var parsed = GemCutterDataRecipe.parse(id, json);
            var buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                GemCutterDataRecipe.write(buffer, parsed);
                var decoded = GemCutterDataRecipe.read(id, buffer);
                assertFalse(decoded.enabledFor(null));
                var output = decoded.definition(id).createOutput(FIRST, "First");
                assertEquals(FIRST, LetterItem.data(output).getUUID("creator"));
                assertEquals("left", LetterOfResignationItem.resign(output, FIRST, hive(FIRST)));
                assertTrue(output.isEmpty());
            } finally { buffer.release(); }
        }
        var stack = letter(FIRST);
        assertEquals(64, stack.getMaxStackSize());
        assertEquals(64, stack.getItem().getUseDuration(stack, null));
        assertEquals(net.minecraft.world.item.UseAnim.BOW, stack.getItem().getUseAnimation(stack));
    }
}
*///?}
