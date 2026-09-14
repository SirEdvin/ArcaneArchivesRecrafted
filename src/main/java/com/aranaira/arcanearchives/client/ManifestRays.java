package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.data.ManifestContents;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Projection of confirmed tracking state; never queries client inventories or chunks. */
public final class ManifestRays {
    private ManifestRays() {}

    public static Set<BlockPos> positions(List<ManifestContents.Entry> tracking, ResourceKey<Level> dimension) {
        Set<BlockPos> result = new LinkedHashSet<>();
        for (var entry : tracking) for (var location : entry.locations()) {
            var position = location.position();
            if (position.dimension.equals(dimension)) result.add(position.pos);
        }
        return result;
    }

    public static float width(double distance) {
        float normalized = Math.max(0F, Math.min(1F, ((float) distance - 10F) / 60F));
        return Math.max(1F, (1F - (normalized * .7F + .3F)) * 10F);
    }
}
