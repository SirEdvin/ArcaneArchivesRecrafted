package com.aranaira.arcanearchives.types;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Stable location identity, including the dimension, for fresh-world storage links. */
public final class BlockPosDimension {
    public final BlockPos pos;
    public final ResourceKey<Level> dimension;

    public BlockPosDimension(BlockPos pos, ResourceKey<Level> dimension) {
        this.pos = Objects.requireNonNull(pos, "pos").immutable();
        this.dimension = Objects.requireNonNull(dimension, "dimension");
        if (!dimension.registry().equals(Registries.DIMENSION.location())) {
            throw new IllegalArgumentException("Expected a dimension registry key");
        }
    }

    public int getX() { return pos.getX(); }
    public int getY() { return pos.getY(); }
    public int getZ() { return pos.getZ(); }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", getX());
        tag.putInt("y", getY());
        tag.putInt("z", getZ());
        tag.putString("dimension", dimension.location().toString());
        return tag;
    }

    /** Decode identity only; a syntactically valid key need not name a currently available world. */
    public static BlockPosDimension deserializeNBT(CompoundTag tag) {
        if (!tag.contains("x", Tag.TAG_INT) || !tag.contains("y", Tag.TAG_INT)
            || !tag.contains("z", Tag.TAG_INT) || !tag.contains("dimension", Tag.TAG_STRING)) {
            throw new IllegalArgumentException("Invalid dimension-aware position fields");
        }
        String name = tag.getString("dimension");
        ResourceLocation id = ResourceLocation.tryParse(name);
        if (id == null || !id.toString().equals(name)) {
            throw new IllegalArgumentException("Expected a fully namespaced dimension identifier");
        }
        return new BlockPosDimension(new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z")),
            ResourceKey.create(Registries.DIMENSION, id));
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof BlockPosDimension that
            && pos.equals(that.pos) && dimension.equals(that.dimension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, dimension);
    }
}
