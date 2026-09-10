package com.aranaira.arcanearchives.items;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.aranaira.arcanearchives.data.HiveSaveData;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.recipe.CraftingCreator;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class HiveExpulsionTest {
    private static final UUID OWNER = new UUID(0, 1), FIRST = new UUID(0, 2), SECOND = new UUID(0, 3), OTHER = new UUID(0, 4);
    private static ItemStack writ(UUID author, UUID target) {
        var result = CraftingCreator.withCreator(new ItemStack(ContentRegistry.WRIT_EXPULSION.get(), 2), author, "Author");
        result.set(DataComponents.CUSTOM_NAME, Component.literal("Target"));
        WritOfExpulsionItem.bindTarget(result, name -> target);
        return result;
    }
    private static HiveSaveData hive() {
        var data = new HiveSaveData();
        assertTrue(data.acceptInvitation(OWNER, FIRST));
        assertTrue(data.acceptInvitation(OWNER, SECOND));
        return data;
    }
    private static CompoundTag save(HiveSaveData data) { return data.save(new CompoundTag(), null); }

    @Test void bindingPreservesCreatorAndOriginalFailedRenameSemantics() {
        ItemStack stack = writ(OWNER, FIRST);
        assertEquals(OWNER, LetterItem.data(stack).getUUID("creator"));
        assertEquals(FIRST, LetterItem.data(stack).getUUID("expel"));
        assertEquals("Target", LetterItem.data(stack).getString("expel_name"));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Unresolved"));
        ItemStack before = stack.copy();
        WritOfExpulsionItem.bindTarget(stack, name -> null);
        assertTrue(ItemStack.matches(before, stack));
        stack.remove(DataComponents.CUSTOM_NAME);
        WritOfExpulsionItem.bindTarget(stack, name -> { fail("No lookup without a custom name"); return null; });
        assertEquals(FIRST, LetterItem.data(stack).getUUID("expel"));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("Second"));
        WritOfExpulsionItem.bindTarget(stack, name -> SECOND);
        assertEquals(SECOND, LetterItem.data(stack).getUUID("expel"));
        assertEquals("Second", LetterItem.data(stack).getString("expel_name"));
        assertEquals(2, stack.getCount());
    }
    @Test void currentOwnerCanExpelAndSelfExpulsionPreservesSuccession() {
        var data = hive();
        ItemStack stack = writ(OWNER, FIRST);
        assertEquals("expelled", WritOfExpulsionItem.expel(stack, OWNER, data));
        assertEquals(1, stack.getCount());
        assertNull(data.ownerOf(FIRST));
        assertEquals(OWNER, data.ownerOf(SECOND));
        assertEquals(save(data), save(HiveSaveData.load(save(data))));
        data = hive();
        assertEquals("expelled", WritOfExpulsionItem.expel(writ(OWNER, OWNER), OWNER, data));
        assertNull(data.ownerOf(OWNER));
        assertEquals(FIRST, data.ownerOf(SECOND));
        assertEquals(List.of(SECOND), new ArrayList<>(data.members(FIRST)));
        assertEquals("expelled", WritOfExpulsionItem.expel(writ(FIRST, SECOND), FIRST, data));
        assertNull(data.ownerOf(FIRST));
        assertNull(data.ownerOf(SECOND));
    }
    @Test void staleWrongAuthorForeignAndMissingTargetsAreRejectedWithoutConsumption() {
        var data = hive();
        for (ItemStack stack : List.of(writ(OTHER, FIRST), writ(OWNER, OTHER))) {
            ItemStack before = stack.copy();
            CompoundTag saved = save(data);
            assertEquals("expel_no_permission", WritOfExpulsionItem.expel(stack, OWNER, data));
            assertTrue(ItemStack.matches(before, stack));
            assertEquals(saved, save(data));
        }
        ItemStack named = writ(OWNER, FIRST);
        assertEquals("expel_no_permission", WritOfExpulsionItem.expel(named, FIRST, data));
        assertEquals(2, named.getCount());
        assertTrue(data.resign(FIRST));
        assertTrue(data.acceptInvitation(OTHER, FIRST));
        CompoundTag before = save(data);
        assertEquals("expel_no_permission", WritOfExpulsionItem.expel(named, OWNER, data));
        assertEquals(before, save(data));
        assertEquals(2, named.getCount());
        assertEquals("invalid", WritOfExpulsionItem.expel(new ItemStack(ContentRegistry.WRIT_EXPULSION.get()), OWNER, data));
        assertEquals("expel_unnamed", WritOfExpulsionItem.expel(CraftingCreator.withCreator(
            new ItemStack(ContentRegistry.WRIT_EXPULSION.get()), OWNER, "Owner"), OWNER, data));
    }
    @Test void recipeWireKeepsOwnerConditionAndCreatesAnUnboundAttributedWrit() throws Exception {
        try (var stream = getClass().getResourceAsStream("/data/arcanearchives/recipe/writ_expulsion.json")) {
            assertNotNull(stream);
            var json = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("expulsion", json.get("hive").getAsString());
            var id = net.minecraft.resources.ResourceLocation.tryParse("arcanearchives:writ_expulsion");
            var parsed = GemCutterDataRecipe.parse(id, json);
            var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                GemCutterDataRecipe.write(buffer, parsed);
                var decoded = GemCutterDataRecipe.read(id, buffer);
                assertFalse(decoded.enabledFor(null));
                var stack = decoded.definition(id).createOutput(OWNER, "Owner");
                assertTrue(stack.is(ContentRegistry.WRIT_EXPULSION.get()));
                assertEquals(OWNER, LetterItem.data(stack).getUUID("creator"));
                assertFalse(LetterItem.data(stack).hasUUID("expel"));
                assertEquals(64, stack.getItem().getUseDuration(stack, null));
            } finally { buffer.release(); }
        }
    }
}
*///?}
