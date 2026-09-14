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

    public static final double THICKNESS = 1D / 16D;

    public record Beam(net.minecraft.world.phys.Vec3 origin, net.minecraft.world.phys.Vec3 target) {}

    public static Set<Beam> beams(List<ManifestContents.Entry> tracking, ResourceKey<Level> dimension,
            net.minecraft.world.phys.Vec3 player) {
        Set<Beam> result = new LinkedHashSet<>();
        for (var entry : tracking) for (var location : entry.locations()) {
            if (!location.position().dimension.equals(dimension)
                    || location.origin() != null && !location.origin().dimension.equals(dimension)) continue;
            var origin = location.origin() == null ? player.add(0, 1, 0)
                : net.minecraft.world.phys.Vec3.atLowerCornerOf(location.origin().pos).add(.5, 1.25, .5);
            result.add(new Beam(origin, net.minecraft.world.phys.Vec3.atCenterOf(location.position().pos)));
        }
        return result;
    }

    public static net.minecraft.world.phys.Vec3[] corners(Beam beam) {
        var direction = beam.target.subtract(beam.origin);
        if (direction.lengthSqr() < 1E-10) return new net.minecraft.world.phys.Vec3[0];
        direction = direction.normalize();
        var axis = Math.abs(direction.y) > .9 ? new net.minecraft.world.phys.Vec3(1, 0, 0)
            : new net.minecraft.world.phys.Vec3(0, 1, 0);
        var u = direction.cross(axis).normalize().scale(THICKNESS / 2);
        var v = direction.cross(u).normalize().scale(THICKNESS / 2);
        return new net.minecraft.world.phys.Vec3[]{
            beam.origin.add(u).add(v), beam.origin.subtract(u).add(v),
            beam.origin.subtract(u).subtract(v), beam.origin.add(u).subtract(v),
            beam.target.add(u).add(v), beam.target.subtract(u).add(v),
            beam.target.subtract(u).subtract(v), beam.target.add(u).subtract(v)};
    }
}
