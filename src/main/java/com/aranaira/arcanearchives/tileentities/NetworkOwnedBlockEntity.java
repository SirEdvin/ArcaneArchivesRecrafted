package com.aranaira.arcanearchives.tileentities;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Persisted placement ownership only; not a network index or a local-menu access restriction. */
public abstract class NetworkOwnedBlockEntity extends BlockEntity {
    private UUID networkOwner;

    protected NetworkOwnedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public UUID networkOwner() { return networkOwner; }

    /** Called after native item data import. Never trust an item's old owner or claim ownerless saves on use. */
    public void recordPlacer(LivingEntity placer) {
        if (level == null || level.isClientSide || isRemoved() || !level.getServer().isSameThread())
            throw new IllegalStateException("Device ownership requires its live server thread");
        boolean fake;
        //? if fabric {
        fake = placer instanceof net.fabricmc.fabric.api.entity.FakePlayer;
        //?} else if forge {
        /*fake = placer instanceof net.minecraftforge.common.util.FakePlayer;
        *///?} else {
        /*fake = placer instanceof net.neoforged.neoforge.common.util.FakePlayer;
        *///?}
        networkOwner = placer instanceof Player && !fake ? placer.getUUID() : null;
        setChanged();
    }

    //? if >=1.21 {
    @Override protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (networkOwner != null) tag.putUUID("network_owner", networkOwner);
    }
    @Override protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        networkOwner = tag.hasUUID("network_owner") ? tag.getUUID("network_owner") : null;
    }
    //?} else {
    /*@Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (networkOwner != null) tag.putUUID("network_owner", networkOwner);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        networkOwner = tag.hasUUID("network_owner") ? tag.getUUID("network_owner") : null;
    }
    *///?}
}
