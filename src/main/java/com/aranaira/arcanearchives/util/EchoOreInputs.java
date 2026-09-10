package com.aranaira.arcanearchives.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;

/** Approved modern equivalent of default ore-dictionary furnace-input discovery. */
public final class EchoOreInputs {
    public static Set<String> discover(List<SmeltingRecipe> recipes) {
        Set<String> result = new LinkedHashSet<>();
        for (var recipe : recipes) {
            for (var ingredient : recipe.getIngredients()) {
                for (ItemStack stack : ingredient.getItems()) {
                    if (isOre(stack)) result.add(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                }
            }
        }
        return result;
    }

    public static boolean isOre(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem().builtInRegistryHolder().tags().anyMatch(tag -> isOreTag(tag.location()))
            || stack.getItem() instanceof BlockItem block
                && block.getBlock().builtInRegistryHolder().tags().anyMatch(tag -> isOreTag(tag.location()));
    }

    static boolean isOreTag(ResourceLocation id) {
        String namespace = id.getNamespace(), path = id.getPath();
        return (namespace.equals("c") || namespace.equals("forge"))
                && (path.equals("ores") || path.startsWith("ores/"))
            || namespace.equals("minecraft") && path.endsWith("_ores");
    }

    private EchoOreInputs() {}
}
