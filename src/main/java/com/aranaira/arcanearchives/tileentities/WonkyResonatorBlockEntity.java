package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.config.ServerSideConfig;
import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class WonkyResonatorBlockEntity extends BlockEntity {
    private int growth;

    public WonkyResonatorBlockEntity(BlockPos pos, BlockState state) {
        super(ContentRegistry.WONKY_RESONATOR_ENTITY.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;
        setChanged();
        growth = nextGrowth(growth, level.isEmptyBlock(worldPosition.above()), ServerSideConfig.current().resonatorTickTime());
    }

    static int nextGrowth(int current, boolean airAbove, int required) {
        // Upstream resets on the tick AFTER reaching the configured duration; its explosion is only a TODO.
        return !airAbove ? current : current < required ? current + 1 : 0;
    }

    public int progressPercentage() {
        return (int) Math.floor(growth / (double) ServerSideConfig.current().resonatorTickTime() * 100D);
    }

    private void writeState(CompoundTag tag) { tag.putInt("current_tick", growth); }
    private void readState(CompoundTag tag) { if (tag.contains("current_tick")) growth = tag.getInt("current_tick"); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    //? if >=1.21 {
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        writeState(tag);
        return tag;
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        writeState(tag);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        readState(tag);
    }
    //?} else {
    /*@Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeState(tag);
        return tag;
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeState(tag);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        readState(tag);
    }
    *///?}
}
