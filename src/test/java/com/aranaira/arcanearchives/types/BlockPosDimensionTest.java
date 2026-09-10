package com.aranaira.arcanearchives.types;

import java.util.HashSet;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlockPosDimensionTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void dimensionsParticipateInLocationIdentity() {
        BlockPos pos = new BlockPos(10, -32, 40);
        BlockPosDimension overworld = new BlockPosDimension(pos, Level.OVERWORLD);
        BlockPosDimension same = new BlockPosDimension(pos, Level.OVERWORLD);
        BlockPosDimension nether = new BlockPosDimension(pos, Level.NETHER);
        assertEquals(overworld, same);
        assertEquals(overworld.hashCode(), same.hashCode());
        assertNotEquals(overworld, nether);
        HashSet<BlockPosDimension> locations = new HashSet<>();
        locations.add(overworld);
        locations.add(same);
        locations.add(nether);
        assertEquals(2, locations.size());
        assertNotEquals(overworld, new BlockPosDimension(pos.above(), Level.OVERWORLD));
    }

    @Test
    void mutableConstructorInputCannotInvalidateHashLookup() {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(1, 2, 3);
        BlockPosDimension location = new BlockPosDimension(cursor, Level.OVERWORLD);
        HashSet<BlockPosDimension> locations = new HashSet<>();
        locations.add(location);
        cursor.set(9, 8, 7);
        assertEquals(1, location.getX());
        assertEquals(2, location.getY());
        assertEquals(3, location.getZ());
        assertTrue(locations.contains(new BlockPosDimension(new BlockPos(1, 2, 3), Level.OVERWORLD)));
    }

    @Test
    void freshWorldFormatPreservesCoordinatesWithoutPackedTruncation() {
        BlockPosDimension original = new BlockPosDimension(
            new BlockPos(Integer.MIN_VALUE, Integer.MAX_VALUE, -1), Level.END);
        CompoundTag saved = original.serializeNBT();
        assertEquals("minecraft:the_end", saved.getString("dimension"));
        assertEquals(original, BlockPosDimension.deserializeNBT(saved));
        saved.putInt("x", 0);
        assertEquals(Integer.MIN_VALUE, original.getX());
    }

    @Test
    void unknownNamespacedDimensionIsPreservedWithoutResolution() {
        ResourceLocation id = ResourceLocation.tryParse("missing_mod:unavailable_world");
        BlockPosDimension original = new BlockPosDimension(BlockPos.ZERO, ResourceKey.create(Registries.DIMENSION, id));
        BlockPosDimension restored = BlockPosDimension.deserializeNBT(original.serializeNBT());
        assertEquals(original, restored);
        assertEquals("missing_mod:unavailable_world", restored.dimension.location().toString());
    }

    @Test
    void rejectsMissingMistypedAndNumericLegacyFieldsWithoutDefaults() {
        CompoundTag valid = new BlockPosDimension(BlockPos.ZERO, Level.OVERWORLD).serializeNBT();
        for (String key : new String[]{"x", "y", "z", "dimension"}) {
            CompoundTag missing = valid.copy();
            missing.remove(key);
            assertThrows(IllegalArgumentException.class, () -> BlockPosDimension.deserializeNBT(missing));
            CompoundTag wrongType = valid.copy();
            wrongType.putLong(key, 0L);
            assertThrows(IllegalArgumentException.class, () -> BlockPosDimension.deserializeNBT(wrongType));
        }
        for (String id : new String[]{"", "overworld", "BAD:world", "minecraft:bad world", ":overworld"}) {
            CompoundTag malformed = valid.copy();
            malformed.putString("dimension", id);
            assertThrows(IllegalArgumentException.class, () -> BlockPosDimension.deserializeNBT(malformed));
        }
        CompoundTag numeric = valid.copy();
        numeric.putInt("dimension", 0);
        assertThrows(IllegalArgumentException.class, () -> BlockPosDimension.deserializeNBT(numeric));
        assertEquals(new BlockPosDimension(BlockPos.ZERO, Level.OVERWORLD), BlockPosDimension.deserializeNBT(valid));
        assertThrows(NullPointerException.class, () -> new BlockPosDimension(null, Level.OVERWORLD));
        assertThrows(NullPointerException.class, () -> new BlockPosDimension(BlockPos.ZERO, null));
    }
}
