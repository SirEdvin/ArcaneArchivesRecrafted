package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Identity only: no inventory, ticking machinery, fluid capability or independent network device. */
public final class MatrixPartBlockEntity extends BlockEntity {
    private UUID identity = UUID.randomUUID();

    public MatrixPartBlockEntity(BlockPos pos, BlockState state) {
        super(ContentRegistry.MATRIX_PART_ENTITY.get(), pos, state);
    }
    public UUID identity() { return identity; }
    public void identify(UUID value) { identity = Objects.requireNonNull(value); setChanged(); }

    //? if >=1.21 {
    @Override protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putUUID("matrix_identity", identity);
    }
    @Override protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tag.hasUUID("matrix_identity")) throw new IllegalArgumentException("Missing Matrix part identity");
        identity = tag.getUUID("matrix_identity");
    }
    //?} else {
    /*@Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putUUID("matrix_identity", identity);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (!tag.hasUUID("matrix_identity")) throw new IllegalArgumentException("Missing Matrix part identity");
        identity = tag.getUUID("matrix_identity");
    }
    *///?}
}
