package com.aranaira.arcanearchives.recipe;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.world.item.ItemStack;
//? if >=1.21 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//?}

/** Creator stamping used by upstream Gem Cutter recipes; this is not ownership authorization. */
public final class CraftingCreator {
    private CraftingCreator() {}

    /** Copy the recipe output before adding the original creator fields. */
    public static ItemStack withCreator(ItemStack output, UUID creator, String displayName) {
        Objects.requireNonNull(output, "output");
        Objects.requireNonNull(creator, "creator");
        Objects.requireNonNull(displayName, "displayName");
        if (output.isEmpty()) throw new IllegalArgumentException("Cannot stamp an empty crafting output");
        ItemStack result = output.copy();
        //? if >=1.21 {
        CustomData.update(DataComponents.CUSTOM_DATA, result, tag -> {
            tag.putUUID("creator", creator);
            tag.putString("creator_name", displayName);
        });
        //?} else {
        /*result.getOrCreateTag().putUUID("creator", creator);
        result.getOrCreateTag().putString("creator_name", displayName);
        *///?}
        return result;
    }
}
