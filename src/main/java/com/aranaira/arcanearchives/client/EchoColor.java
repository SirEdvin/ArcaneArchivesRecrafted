package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.items.EchoItem;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

/** Original ClientProxy's middle-layer tint priority. */
public final class EchoColor {
    public static int color(ItemStack stack, int layer, ItemColor colors) {
        if (layer != 1) return -1;
        ItemStack contained = EchoItem.itemFromEcho(stack);
        if (contained.isEmpty()) return -1;

        for (int index = 0; index < 3; index++) {
            int tint = colors.getColor(contained, index);
            if (tint != -1) return tint;
        }
        return EchoColorCache.color(contained);
    }

    private EchoColor() {}
}
