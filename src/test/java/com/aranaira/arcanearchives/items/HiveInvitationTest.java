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
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.Test;

class HiveInvitationTest {
    private static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CAROL = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID DAVE = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static ItemStack letter(UUID author) {
        return CraftingCreator.withCreator(new ItemStack(ContentRegistry.LETTER_INVITATION.get(), 2), author, "Author");
    }
    private static CompoundTag save(HiveSaveData data) { return data.save(new CompoundTag(), null); }

    @Test void staleAuthorRejectedWithoutConsumptionOrMembershipMutation() {
        HiveSaveData data = new HiveSaveData();
        ItemStack oldLetter = letter(ALICE);
        ItemStack before = oldLetter.copy();
        assertEquals("joined", LetterOfInvitationItem.accept(letter(BOB), ALICE, data));
        data.setDirty(false);
        CompoundTag saved = save(data);
        assertEquals("failed", LetterOfInvitationItem.accept(oldLetter, CAROL, data));
        assertTrue(ItemStack.matches(before, oldLetter));
        assertEquals(saved, save(data));
        assertFalse(data.isDirty());
        assertEquals(BOB, data.ownerOf(ALICE));
        assertNull(data.ownerOf(CAROL));
        assertTrue(data.members(ALICE).isEmpty());
        assertEquals(List.of(ALICE), new ArrayList<>(data.members(BOB)));
        HiveSaveData decoded = HiveSaveData.load(saved);
        assertEquals("failed", LetterOfInvitationItem.accept(oldLetter, CAROL, decoded));
        assertEquals(saved, save(decoded));
    }

    @Test void unaffiliatedAndOwningAuthorsJoinInOrderAndPersist() {
        HiveSaveData data = new HiveSaveData();
        ItemStack invitation = letter(ALICE);
        assertEquals("joined", LetterOfInvitationItem.accept(invitation, BOB, data));
        assertEquals(1, invitation.getCount());
        assertTrue(data.isDirty());
        assertEquals("joined", LetterOfInvitationItem.accept(invitation, CAROL, data));
        assertTrue(invitation.isEmpty());
        assertEquals(List.of(BOB, CAROL), new ArrayList<>(data.members(ALICE)));
        assertThrows(UnsupportedOperationException.class, () -> data.members(ALICE).clear());
        HiveSaveData decoded = HiveSaveData.load(save(data));
        assertEquals(save(data), save(decoded));
        assertEquals(ALICE, decoded.ownerOf(ALICE));
        assertEquals(ALICE, decoded.ownerOf(BOB));
        assertEquals("joined", LetterOfInvitationItem.accept(letter(ALICE), DAVE, decoded));
        assertEquals(List.of(BOB, CAROL, DAVE), new ArrayList<>(decoded.members(ALICE)));
    }

    @Test void invalidSelfAndAlreadyAffiliatedRecipientsLeaveLetterAndDataAlone() {
        HiveSaveData data = new HiveSaveData();
        ItemStack self = letter(ALICE);
        assertEquals("yours", LetterOfInvitationItem.accept(self, ALICE, data));
        assertEquals(2, self.getCount());
        assertNull(data.ownerOf(ALICE));
        assertEquals("joined", LetterOfInvitationItem.accept(letter(BOB), CAROL, data));
        CompoundTag before = save(data);
        for (UUID recipient : List.of(BOB, CAROL)) {
            assertEquals("failed", LetterOfInvitationItem.accept(self, recipient, data));
            assertEquals(2, self.getCount());
            assertEquals(before, save(data));
        }
        ItemStack invalid = new ItemStack(ContentRegistry.LETTER_INVITATION.get());
        CustomData.update(DataComponents.CUSTOM_DATA, invalid, tag -> tag.putString("creator_name", "Forged name only"));
        ItemStack copy = invalid.copy();
        assertEquals("invalid", LetterOfInvitationItem.accept(invalid, DAVE, data));
        assertTrue(ItemStack.matches(copy, invalid));
        assertEquals(before, save(data));
        assertEquals("invalid", LetterOfInvitationItem.accept(ItemStack.EMPTY, DAVE, data));
        assertEquals("invalid", LetterOfInvitationItem.accept(
            CraftingCreator.withCreator(new ItemStack(net.minecraft.world.item.Items.PAPER), ALICE, "Alice"), DAVE, data));
    }

    @Test void malformedOrOverlappingSavedMembershipIsNotSilentlyMerged() {
        HiveSaveData data = new HiveSaveData();
        assertTrue(data.acceptInvitation(ALICE, BOB));
        assertTrue(data.acceptInvitation(CAROL, DAVE));
        CompoundTag overlap = save(data);
        ListTag hives = overlap.getList("hive_data", 10);
        hives.getCompound(1).putUUID("owner", BOB);
        assertThrows(IllegalArgumentException.class, () -> HiveSaveData.load(overlap));
        CompoundTag duplicate = save(data);
        var members = duplicate.getList("hive_data", 10).getCompound(0).getList("members", 10);
        members.add(members.getCompound(0).copy());
        assertThrows(IllegalArgumentException.class, () -> HiveSaveData.load(duplicate));
        assertThrows(IllegalArgumentException.class, () -> HiveSaveData.load(new CompoundTag()));
        assertEquals(save(new HiveSaveData()), save(HiveSaveData.load(save(new HiveSaveData()))));
    }

    @Test void registeredRecipeStampsCreatorAndRetainsConditionOverWire() throws Exception {
        var id = ResourceLocation.tryParse("arcanearchives:letter_invitation");
        try (var stream = getClass().getResourceAsStream("/data/arcanearchives/recipe/letter_invitation.json")) {
            assertNotNull(stream);
            var json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("invitation", json.get("hive").getAsString());
            assertEquals(3, json.getAsJsonArray("inputs").get(0).getAsJsonObject().get("count").getAsInt());
            var parsed = GemCutterDataRecipe.parse(id, json);
            var buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                GemCutterDataRecipe.write(buffer, parsed);
                var decoded = GemCutterDataRecipe.read(id, buffer);
                assertFalse(decoded.enabledFor(null)); // No authoritative player must fail closed.
                assertTrue(decoded.enabled());
                var recipe = decoded.definition(id);
                assertTrue(recipe.getRecipeOutput().is(ContentRegistry.LETTER_INVITATION.get()));
                assertFalse(LetterOfInvitationItem.data(recipe.getRecipeOutput()).hasUUID("creator"));
                ItemStack output = recipe.createOutput(ALICE, "Alice");
                assertEquals(ALICE, LetterOfInvitationItem.data(output).getUUID("creator"));
                assertEquals("Alice", LetterOfInvitationItem.data(output).getString("creator_name"));
                assertEquals("joined", LetterOfInvitationItem.accept(output, BOB, new HiveSaveData()));
                assertTrue(output.isEmpty());
            } finally { buffer.release(); }
            for (String bad : List.of("true", "null", "1", "\"unknown\"", "{}")) {
                var changed = json.deepCopy();
                changed.add("hive", JsonParser.parseString(bad));
                assertThrows(com.google.gson.JsonParseException.class, () -> GemCutterDataRecipe.parse(id, changed));
            }
        }
        ItemStack stack = letter(ALICE);
        ItemStack before = stack.copy();
        var item = ContentRegistry.LETTER_INVITATION.get();
        assertEquals(64, item.getUseDuration(stack, null));
        assertEquals(64, stack.getMaxStackSize());
        assertEquals(UseAnim.BOW, item.getUseAnimation(stack));
        var text = new ArrayList<Component>();
        item.appendHoverText(stack, Item.TooltipContext.EMPTY, text, TooltipFlag.NORMAL);
        assertEquals(net.minecraft.ChatFormatting.GOLD.getColor().intValue(), text.getFirst().getStyle().getColor().getValue());
        assertTrue(ItemStack.matches(before, stack));
    }
}
*///?}
