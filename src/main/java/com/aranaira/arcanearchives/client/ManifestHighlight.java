package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.data.ManifestContents;
import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Original Manifest slot feedback, driven only by confirmed tracking references. */
public final class ManifestHighlight {
    private ManifestHighlight() {}
    private static final float[][] COLORS = {
        {1F, .5F, .5F}, {1F, .75F, .5F}, {1F, 1F, .5F}, {.5F, 1F, .6F},
        {.5F, 1F, 1F}, {.5F, .65F, 1F}, {.8F, .5F, 1F}, {1F, .55F, 1F}
    };

    public static boolean matches(ItemStack stack, List<ManifestContents.Entry> tracking) {
        return !stack.isEmpty() && tracking.stream().anyMatch(entry ->
            ExtendedItemStackHandler.sameItemAndData(stack, entry.stack()));
    }

    public static int color(long dayTime) {
        int time = (int) (dayTime % 96);
        int band = time / 12;
        if (band < 0) return 0xFFFFFFFF;
        float progress = Math.max(0F, (time % 12) / 12F);
        int color = 0xFF;
        for (int channel = 0; channel < 3; channel++) {
            double first = COLORS[band][channel];
            double second = COLORS[(band + 1) % COLORS.length][channel];
            float mixed = (float) (first + (second - first) * progress);
            color = color << 8 | (int) (mixed * 255);
        }
        return color;
    }
}
