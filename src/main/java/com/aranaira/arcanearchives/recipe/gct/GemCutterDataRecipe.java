package com.aranaira.arcanearchives.recipe.gct;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.recipe.IngredientStack;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
//? if >=1.21 {
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeInput;
//?} else {
/*import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
*///?}

/** Native data/synchronization only; the menu's conserved commit remains the crafting authority. */
//? if >=1.21 {
public final class GemCutterDataRecipe implements Recipe<RecipeInput> {
//?} else {
/*public final class GemCutterDataRecipe implements Recipe<Container> {
*///?}
    private static final ResourceLocation UNNAMED = ResourceLocation.tryParse("arcanearchives:unnamed");
    private final ResourceLocation name;
    private final JsonObject data;
    private final GCTRecipe definition;
    private final int order;
    private final boolean enabled;
    private final boolean arsenal;
    private final boolean recordsCreator;
    private final String hiveCondition;

    private GemCutterDataRecipe(ResourceLocation name, JsonObject source) {
        this.name = name;
        this.data = source.deepCopy();
        if (data.toString().length() > 32767) throw new IllegalArgumentException("Gem cutting recipe exceeds wire limit");
        fields(data, "type", "inputs", "result", "order", "enabled", "record_creator", "arsenal", "hive");
        if (data.has("hive") && (!data.get("hive").isJsonPrimitive()
                || !data.getAsJsonPrimitive("hive").isString()
                || !Set.of("invitation", "resignation", "expulsion").contains(data.get("hive").getAsString())))
            throw new IllegalArgumentException("Unsupported Hive condition");
        hiveCondition = data.has("hive") ? data.get("hive").getAsString() : "";
        if (data.has("type") && !identifier(data.get("type")).equals(ResourceLocation.tryParse("arcanearchives:gem_cutting")))
            throw new IllegalArgumentException("Wrong gem cutting recipe type");
        order = data.has("order") ? integer(data.get("order")) : 0;
        if (data.has("enabled") && (!data.get("enabled").isJsonPrimitive() || !data.getAsJsonPrimitive("enabled").isBoolean()))
            throw new IllegalArgumentException("enabled must be boolean");
        enabled = !data.has("enabled") || data.get("enabled").getAsBoolean();
        if (data.has("arsenal") && (!data.get("arsenal").isJsonPrimitive() || !data.getAsJsonPrimitive("arsenal").isBoolean()))
            throw new IllegalArgumentException("arsenal must be boolean");
        arsenal = data.has("arsenal") && data.get("arsenal").getAsBoolean();
        if (data.has("record_creator") && (!data.get("record_creator").isJsonPrimitive() || !data.getAsJsonPrimitive("record_creator").isBoolean()))
            throw new IllegalArgumentException("record_creator must be boolean");
        recordsCreator = data.has("record_creator") && data.get("record_creator").getAsBoolean();
        JsonObject result = data.getAsJsonObject("result");
        fields(result, "item", "count");
        ItemStack output = new ItemStack(item(result.get("item")), count(result));
        if (output.getCount() > Math.min(64, output.getMaxStackSize())) throw new IllegalArgumentException("Output exceeds native stack limit");
        var inputs = data.getAsJsonArray("inputs");
        if (inputs.size() > 54) throw new IllegalArgumentException("Too many counted inputs");
        List<IngredientStack> costs = new ArrayList<>();
        for (JsonElement element : inputs) {
            JsonObject cost = element.getAsJsonObject();
            fields(cost, "item", "tag", "count");
            if (cost.has("item") == cost.has("tag")) throw new IllegalArgumentException("Input requires exactly one item or tag");
            costs.add(cost.has("tag") ? new IngredientStack(TagKey.create(Registries.ITEM, identifier(cost.get("tag"))), count(cost))
                : new IngredientStack(item(cost.get("item")), count(cost)));
        }
        definition = recordsCreator ? GCTRecipe.withCreator(name, output, costs) : new GCTRecipe(name, output, costs);
    }

    public static GemCutterDataRecipe parse(ResourceLocation name, JsonObject source) {
        try {
            return new GemCutterDataRecipe(java.util.Objects.requireNonNull(name), java.util.Objects.requireNonNull(source));
        } catch (RuntimeException failure) {
            // 1.20's native manager catches JSON/argument errors, not arbitrary runtime exceptions.
            throw new com.google.gson.JsonParseException("Invalid gem cutting recipe " + name + ": " + failure.getMessage(), failure);
        }
    }

    private static void fields(JsonObject object, String... allowed) {
        if (object == null || !Set.of(allowed).containsAll(object.keySet())) throw new IllegalArgumentException("Unsupported recipe fields");
    }

    private static ResourceLocation identifier(JsonElement value) {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) throw new IllegalArgumentException("Expected namespaced identifier");
        String text = value.getAsString();
        ResourceLocation id = text.contains(":") ? ResourceLocation.tryParse(text) : null;
        if (id == null) throw new IllegalArgumentException("Invalid identifier: " + text);
        return id;
    }

    private static Item item(JsonElement value) {
        ResourceLocation id = identifier(value);
        if (!BuiltInRegistries.ITEM.containsKey(id) || BuiltInRegistries.ITEM.get(id) == Items.AIR)
            throw new IllegalArgumentException("Unknown or empty item: " + id);
        return BuiltInRegistries.ITEM.get(id);
    }

    private static int integer(JsonElement value) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) throw new IllegalArgumentException("Expected integer");
        return value.getAsBigDecimal().intValueExact();
    }

    private static int count(JsonObject value) {
        int count = value.has("count") ? integer(value.get("count")) : 1;
        if (count <= 0) throw new IllegalArgumentException("Count must be positive");
        return count;
    }

    public int order() { return order; }
    public record Entry(ResourceLocation name, GemCutterDataRecipe recipe) {}

    public static List<Entry> entries(net.minecraft.world.item.crafting.RecipeManager manager) {
        //? if >=1.21 {
        return manager.getAllRecipesFor(ContentRegistry.GEM_CUTTING_TYPE.get()).stream()
            .map(holder -> new Entry(holder.id(), holder.value()))
        //?} else {
        /*return manager.getAllRecipesFor(ContentRegistry.GEM_CUTTING_TYPE.get()).stream()
            .map(recipe -> new Entry(recipe.getId(), recipe))
        *///?}
            .sorted(java.util.Comparator.comparingInt((Entry entry) -> entry.recipe().order())
                .thenComparing(entry -> entry.name().toString())).toList();
    }
    public boolean enabled() { return enabled && (!arsenal || com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()); }
    public boolean enabledFor(net.minecraft.world.entity.player.Player player) {
        if (!enabled()) return false;
        if (hiveCondition.isEmpty()) return true;
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)
                || !serverPlayer.server.isSameThread() || !player.isAlive() || player.isSpectator()) return false;
        var hiveOwner = com.aranaira.arcanearchives.data.HiveSaveData.get(serverPlayer.server).ownerOf(player.getUUID());
        return switch (hiveCondition) {
            case "invitation" -> HiveCraftingConditions.canCraftInvitation(player.getUUID(), hiveOwner);
            case "resignation" -> HiveCraftingConditions.canCraftResignation(player.getUUID(), hiveOwner);
            case "expulsion" -> HiveCraftingConditions.canCraftExpulsion(player.getUUID(), hiveOwner);
            default -> false;
        };
    }
    public GCTRecipe definition(ResourceLocation id) {
        return recordsCreator ? GCTRecipe.withCreator(id, definition.getRecipeOutput(), definition.getIngredients())
            : new GCTRecipe(id, definition.getRecipeOutput(), definition.getIngredients());
    }

    public static void write(FriendlyByteBuf buffer, GemCutterDataRecipe recipe) {
        buffer.writeUtf(recipe.data.toString(), 32767);
    }

    public static GemCutterDataRecipe read(ResourceLocation name, FriendlyByteBuf buffer) {
        return parse(name, JsonParser.parseString(buffer.readUtf(32767)).getAsJsonObject());
    }

    //? if >=1.21 {
    @Override
    public boolean matches(RecipeInput input, Level level) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < input.size(); slot++) stacks.add(input.getItem(slot));
        return enabled() && definition.matches(stacks);
    }
    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) { return definition.getRecipeOutput(); }
    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) { return definition.getRecipeOutput(); }
    //?} else {
    /*@Override
    public boolean matches(Container input, Level level) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < input.getContainerSize(); slot++) stacks.add(input.getItem(slot));
        return enabled() && definition.matches(stacks);
    }
    @Override
    public ItemStack assemble(Container input, RegistryAccess registries) { return definition.getRecipeOutput(); }
    @Override
    public ItemStack getResultItem(RegistryAccess registries) { return definition.getRecipeOutput(); }
    @Override
    public ResourceLocation getId() { return name; }
    *///?}
    @Override
    public boolean canCraftInDimensions(int width, int height) { return false; }
    @Override
    public boolean isSpecial() { return true; }
    @Override
    public RecipeSerializer<?> getSerializer() { return ContentRegistry.GEM_CUTTING_SERIALIZER.get(); }
    @Override
    public RecipeType<?> getType() { return ContentRegistry.GEM_CUTTING_TYPE.get(); }

    public static final class Type implements RecipeType<GemCutterDataRecipe> {
        @Override
        public String toString() { return "arcanearchives:gem_cutting"; }
    }

    public static final class Serializer implements RecipeSerializer<GemCutterDataRecipe> {
        //? if >=1.21 {
        private static final MapCodec<GemCutterDataRecipe> CODEC = MapCodec.assumeMapUnsafe(Codec.PASSTHROUGH.flatXmap(dynamic -> {
            try {
                return DataResult.success(parse(UNNAMED, dynamic.convert(JsonOps.INSTANCE).getValue().getAsJsonObject()));
            } catch (RuntimeException failure) {
                return DataResult.error(() -> "Invalid gem cutting recipe: " + failure.getMessage());
            }
        }, recipe -> DataResult.success(new Dynamic<>(JsonOps.INSTANCE, recipe.data.deepCopy()))));
        @Override
        public MapCodec<GemCutterDataRecipe> codec() { return CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GemCutterDataRecipe> streamCodec() {
            return StreamCodec.of(GemCutterDataRecipe::write, buffer -> read(UNNAMED, buffer));
        }
        //?} else {
        /*@Override
        public GemCutterDataRecipe fromJson(ResourceLocation name, JsonObject data) { return parse(name, data); }
        @Override
        public GemCutterDataRecipe fromNetwork(ResourceLocation name, FriendlyByteBuf buffer) { return read(name, buffer); }
        @Override
        public void toNetwork(FriendlyByteBuf buffer, GemCutterDataRecipe recipe) { write(buffer, recipe); }
        *///?}
    }
}
