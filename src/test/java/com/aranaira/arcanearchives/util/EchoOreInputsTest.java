package com.aranaira.arcanearchives.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EchoOreInputsTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    @Test void onlyApprovedOreTagFamiliesQualify() {
        for (String id : new String[]{"minecraft:iron_ores", "c:ores", "c:ores/modded", "forge:ores", "forge:ores/modded"})
            assertTrue(EchoOreInputs.isOreTag(ResourceLocation.tryParse(id)), id);
        for (String id : new String[]{"c:raw_materials", "forge:raw_materials/iron", "c:storage_blocks/raw_iron", "other:ores", "c:oreberries"})
            assertFalse(EchoOreInputs.isOreTag(ResourceLocation.tryParse(id)), id);
    }

    @Test void nativeItemTagsSelectSmeltableModdedMembersButNotRawMaterialsOrUnsmeltableMembers() {
        TagKey<Item> ores = TagKey.create(Registries.ITEM, ResourceLocation.tryParse("c:ores/echo_fixture"));
        TagKey<Item> raw = TagKey.create(Registries.ITEM, ResourceLocation.tryParse("c:raw_materials/echo_fixture"));
        Map<TagKey<Item>, List<Holder<Item>>> original = new HashMap<>();
        BuiltInRegistries.ITEM.getTags().forEach(pair -> original.put(pair.getFirst(), pair.getSecond().stream().toList()));
        var rebound = new HashMap<>(original);
        try {
            // Vanilla stand-ins receive an actual mod-defined ore tag; no mock membership predicate.
            rebound.put(ores, List.of(BuiltInRegistries.ITEM.wrapAsHolder(Items.PAPER), BuiltInRegistries.ITEM.wrapAsHolder(Items.STICK)));
            rebound.put(raw, List.of(BuiltInRegistries.ITEM.wrapAsHolder(Items.RAW_IRON)));
            BuiltInRegistries.ITEM.bindTags(rebound);
            ItemStack input = new ItemStack(Items.PAPER, 12);
            var recipes = List.of(recipe(input, new ItemStack(Items.IRON_INGOT)),
                recipe(new ItemStack(Items.RAW_IRON), new ItemStack(Items.IRON_INGOT)));
            assertEquals(Set.of("minecraft:paper"), EchoOreInputs.discover(recipes));
            assertEquals(12, input.getCount());
            assertFalse(EchoOreInputs.isOre(ItemStack.EMPTY));
        } finally {
            original.putIfAbsent(ores, List.of());
            original.putIfAbsent(raw, List.of());
            BuiltInRegistries.ITEM.bindTags(original);
        }
    }

    @Test void blockOnlyOreTagsAreRecognizedThroughTheirBlockItem() {
        var tag = TagKey.create(Registries.BLOCK, ResourceLocation.tryParse("forge:ores/echo_fixture"));
        Map<TagKey<net.minecraft.world.level.block.Block>, List<Holder<net.minecraft.world.level.block.Block>>> original = new HashMap<>();
        BuiltInRegistries.BLOCK.getTags().forEach(pair -> original.put(pair.getFirst(), pair.getSecond().stream().toList()));
        var rebound = new HashMap<>(original);
        try {
            rebound.put(tag, List.of(BuiltInRegistries.BLOCK.wrapAsHolder(net.minecraft.world.level.block.Blocks.STONE)));
            BuiltInRegistries.BLOCK.bindTags(rebound);
            assertTrue(EchoOreInputs.isOre(new ItemStack(Items.STONE)));
            assertEquals(Set.of("minecraft:stone"), EchoOreInputs.discover(List.of(recipe(new ItemStack(Items.STONE), new ItemStack(Items.GLASS)))));
        } finally {
            original.putIfAbsent(tag, List.of());
            BuiltInRegistries.BLOCK.bindTags(original);
        }
    }

    private static SmeltingRecipe recipe(ItemStack input, ItemStack output) {
        //? if >=1.21 {
        return new SmeltingRecipe("", CookingBookCategory.MISC, Ingredient.of(input), output, 0, 200);
        //?} else {
        /*return new SmeltingRecipe(ResourceLocation.tryParse("arcanearchives:echo_fixture"), "", CookingBookCategory.MISC,
            Ingredient.of(input), output, 0, 200);
        *///?}
    }
}
