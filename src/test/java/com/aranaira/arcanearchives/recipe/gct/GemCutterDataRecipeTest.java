package com.aranaira.arcanearchives.recipe.gct;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GemCutterDataRecipeTest {
    private static final ResourceLocation NAME = ResourceLocation.tryParse("arcanearchives:reload_test");
    private static final String JSON = """
        {"type":"arcanearchives:gem_cutting","order":3,
         "inputs":[{"item":"minecraft:diamond","count":2},{"item":"minecraft:diamond"}],
         "result":{"item":"minecraft:paper","count":2}}
        """;

    @BeforeAll
    static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    @Test
    void kubejsMetadataIsDetachedValidatedAndNeverSynchronized() {
        var json = JsonParser.parseString(JSON).getAsJsonObject();
        json.add("_kubejs_changed_marker", JsonParser.parseString("{\"source\":\"server_scripts:example.js\",\"line\":14}"));
        var recipe = GemCutterDataRecipe.parse(NAME, json);
        assertTrue(json.has("_kubejs_changed_marker"));
        assertEquals(2, recipe.definition(NAME).getRecipeOutput().getCount());
        assertFalse(recipe.definition(NAME).matches(List.of(new ItemStack(Items.DIAMOND, 2))));
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            GemCutterDataRecipe.write(buffer, recipe);
            assertEquals(JsonParser.parseString(JSON), JsonParser.parseString(buffer.readUtf(32767)));
        } finally { buffer.release(); }
        for (String marker : List.of("null", "true", "[]", "{}",
                "{\"source\":4,\"line\":1}", "{\"source\":\"x\",\"line\":1.5}",
                "{\"source\":\"x\",\"line\":-1}", "{\"source\":\"x\",\"line\":1,\"cost\":0}")) {
            json.add("_kubejs_changed_marker", JsonParser.parseString(marker));
            assertThrows(com.google.gson.JsonParseException.class, () -> GemCutterDataRecipe.parse(NAME, json));
        }
        json.add("_kubejs_changed_marker", JsonParser.parseString("{\"source\":\"x\",\"line\":0}"));
        json.addProperty("unknown", true);
        assertThrows(com.google.gson.JsonParseException.class, () -> GemCutterDataRecipe.parse(NAME, json));
    }

    @Test
    void ownedJsonAndWireRoundTripPreserveExactCountedPayment() {
        JsonObject json = JsonParser.parseString(JSON).getAsJsonObject();
        var original = GemCutterDataRecipe.parse(NAME, json);
        json.getAsJsonObject("result").addProperty("count", 64);
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            GemCutterDataRecipe.write(buffer, original);
            var decoded = GemCutterDataRecipe.read(NAME, buffer);
            assertEquals(3, decoded.order());
            assertTrue(decoded.enabled());
            assertEquals(0, buffer.readableBytes());
            var recipe = decoded.definition(NAME);
            assertFalse(recipe.matches(List.of(new ItemStack(Items.DIAMOND, 2))));
            assertTrue(recipe.matches(List.of(new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.DIAMOND))));
            assertEquals(2, recipe.getRecipeOutput().getCount());
            recipe.getRecipeOutput().setCount(64);
            assertEquals(2, decoded.definition(NAME).getRecipeOutput().getCount());
        } finally { buffer.release(); }
    }

    @Test
    void malformedDefinitionsCannotCreateSilentFreeOrSubstitutedRecipes() {
        for (String json : List.of(
                JSON.replace("minecraft:diamond", "arcanearchives:missing"),
                JSON.replace("minecraft:diamond", "minecraft:air"),
                JSON.replace("minecraft:paper", "paper"),
                JSON.replace("\"count\":2", "\"count\":0"),
                JSON.replace("\"count\":2", "\"count\":1.5"),
                JSON.replace("\"count\":2", "\"count\":2147483648"),
                JSON.replace("\"count\":2", "\"count\":\"2\""),
                JSON.replace("\"count\":2", "\"count\":65"),
                JSON.replace("\"item\":\"minecraft:diamond\"", "\"item\":\"minecraft:diamond\",\"tag\":\"minecraft:logs\""),
                JSON.replace("\"order\":3", "\"enabled\":\"false\""),
                JSON.replace("\"order\":3", "\"record_creator\":\"true\""),
                JSON.replace("\"order\":3", "\"record_creator\":1"),
                JSON.replace("\"order\":3", "\"record_creator\":null"),
                JSON.replace("\"order\":3", "\"record_creator\":true,\"creator\":\"forged\""),
                JSON.replace("\"order\":3", "\"conditions\":[]"),
                JSON.replace("\"item\":\"minecraft:paper\"", "\"item\":\"minecraft:paper\",\"components\":{}"))) {
            assertThrows(com.google.gson.JsonParseException.class, () -> GemCutterDataRecipe.parse(NAME, JsonParser.parseString(json).getAsJsonObject()), json);
        }
        var disabled = GemCutterDataRecipe.parse(NAME, JsonParser.parseString(JSON.replace("\"order\":3", "\"enabled\":false")).getAsJsonObject());
        assertFalse(disabled.enabled());
        var emptyTag = GemCutterDataRecipe.parse(NAME, JsonParser.parseString(JSON.replace("\"item\":\"minecraft:diamond\"", "\"tag\":\"arcanearchives:missing\"")).getAsJsonObject());
        assertFalse(emptyTag.definition(NAME).matches(List.of(new ItemStack(Items.DIAMOND, 64))));
    }

    @Test
    void creatorPolicySurvivesWireAndIdentityRebindingWithoutStampingPreviews() {
        for (int policy = 0; policy < 3; policy++) {
            var json = JsonParser.parseString(JSON).getAsJsonObject();
            if (policy != 0) json.addProperty("record_creator", policy == 2);
            var original = GemCutterDataRecipe.parse(NAME, json);
            json.addProperty("record_creator", policy != 2);
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                GemCutterDataRecipe.write(buffer, original);
                var decoded = GemCutterDataRecipe.read(NAME, buffer);
                var rebound = ResourceLocation.tryParse("arcanearchives:rebound");
                var recipe = decoded.definition(rebound);
                assertEquals(rebound, recipe.getName());
                var catalog = new GCTRecipeList();
                catalog.addRecipe(recipe);
                var inputs = new com.aranaira.arcanearchives.inventory.handlers.GemCutterInputHandler();
                inputs.setStackInSlot(0, new ItemStack(Items.DIAMOND, 9));
                for (int player = 1; player <= 2; player++) {
                    var id = new java.util.UUID(0, player);
                    var output = catalog.consumeForCraft(rebound, inputs, id, "Crafter " + player, () -> true).orElseThrow().output();
                    assertEquals(2, output.getCount());
                    assertEquals(policy == 2, data(output).contains("creator"));
                    if (policy == 2) {
                        assertEquals(id, data(output).getUUID("creator"));
                        assertEquals("Crafter " + player, data(output).getString("creator_name"));
                    }
                    assertFalse(data(recipe.getRecipeOutput()).contains("creator"));
                    assertFalse(data(decoded.definition(rebound).getRecipeOutput()).contains("creator"));
                }
                assertEquals(3, inputs.getStackInSlot(0).getCount());
                assertTrue(catalog.consumeForCraft(rebound, inputs, new java.util.UUID(0, 3), "Denied", () -> false).isEmpty());
                assertEquals(3, inputs.getStackInSlot(0).getCount());
                assertEquals(0, buffer.readableBytes());
            } finally { buffer.release(); }
        }
    }

    private static net.minecraft.nbt.CompoundTag data(ItemStack stack) {
        //? if >=1.21 {
        return stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
            net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        //?} else {
        /*return stack.hasTag() ? stack.getTag().copy() : new net.minecraft.nbt.CompoundTag();
        *///?}
    }

    @Test
    void truncatedOrOversizedWireInputIsRejected() {
        var encoded = new FriendlyByteBuf(Unpooled.buffer());
        try {
            GemCutterDataRecipe.write(encoded, GemCutterDataRecipe.parse(NAME, JsonParser.parseString(JSON).getAsJsonObject()));
            var truncated = new FriendlyByteBuf(encoded.copy(0, encoded.readableBytes() - 1));
            try { assertThrows(RuntimeException.class, () -> GemCutterDataRecipe.read(NAME, truncated)); }
            finally { truncated.release(); }
            encoded.clear();
            encoded.writeVarInt(Integer.MAX_VALUE);
            assertThrows(RuntimeException.class, () -> GemCutterDataRecipe.read(NAME, encoded));
        } finally { encoded.release(); }
    }

    //? if neoforge {
    /*@Test
    void nativeRecipeHolderCodecSynchronizesRegisteredSerializerAndIdentity() {
        var registries = net.minecraft.core.RegistryAccess.fromRegistryOfRegistries(
            net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
        var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            var json = JsonParser.parseString(JSON).getAsJsonObject();
            json.addProperty("record_creator", true);
            var nativeRecipe = net.minecraft.world.item.crafting.Recipe.CODEC.parse(
                net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE, registries), json).getOrThrow();
            var holder = new net.minecraft.world.item.crafting.RecipeHolder<>(NAME,
                nativeRecipe);
            net.minecraft.world.item.crafting.RecipeHolder.STREAM_CODEC.encode(buffer, holder);
            var decoded = net.minecraft.world.item.crafting.RecipeHolder.STREAM_CODEC.decode(buffer);
            assertEquals(NAME, decoded.id());
            assertEquals(0, buffer.readableBytes());
            var recipe = assertInstanceOf(GemCutterDataRecipe.class, decoded.value()).definition(decoded.id());
            assertEquals(2, recipe.getRecipeOutput().getCount());
            assertFalse(recipe.matches(List.of(new ItemStack(Items.DIAMOND, 2))));
            assertTrue(recipe.matches(List.of(new ItemStack(Items.DIAMOND, 3))));
            var creator = new java.util.UUID(0, 1);
            assertEquals(creator, data(recipe.createOutput(creator, "Native crafter")).getUUID("creator"));
            assertFalse(data(recipe.getRecipeOutput()).contains("creator"));
        } finally { buffer.release(); }
    }
    *///?}
}
