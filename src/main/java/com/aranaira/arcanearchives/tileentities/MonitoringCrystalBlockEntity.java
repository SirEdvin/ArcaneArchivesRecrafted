package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.blocks.MonitoringCrystal;
import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Registered inventory attachment; network membership is integrated separately. */
public final class MonitoringCrystalBlockEntity extends NetworkOwnedBlockEntity {
    private BlockPos target;
    public MonitoringCrystalBlockEntity(BlockPos pos, BlockState state) { super(ContentRegistry.MONITORING_CRYSTAL_ENTITY.get(), pos, state); }
    public BlockPos target() {
        if (target == null) target = worldPosition.relative(getBlockState().getValue(MonitoringCrystal.FACING).getOpposite());
        return target;
    }
    public static boolean isArcaneDevice(BlockEntity tile) {
        return tile instanceof RadiantChestBlockEntity || tile instanceof RadiantTroveBlockEntity
            || tile instanceof RadiantTankBlockEntity || tile instanceof RadiantResonatorBlockEntity
            || tile instanceof RadiantCraftingTableBlockEntity || tile instanceof GemCuttersTableBlockEntity
            || tile instanceof MonitoringCrystalBlockEntity;
    }
    public BlockEntity targetTile() {
        if (level == null || isRemoved() || !level.hasChunkAt(target())) return null;
        BlockEntity tile = level.getBlockEntity(target());
        return tile == null || tile.isRemoved() || isArcaneDevice(tile) ? null : tile;
    }
    // No capability is exposed on this attachment: Manifest consumers must query the target.
    //? if fabric {
    public net.fabricmc.fabric.api.transfer.v1.storage.Storage<net.fabricmc.fabric.api.transfer.v1.item.ItemVariant> inventory() {
        BlockEntity tile = targetTile();
        return tile == null ? null : net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.find(level, target(), null);
    }
    //?} else if forge {
    /*public net.minecraftforge.items.IItemHandler inventory() {
        BlockEntity tile = targetTile();
        if (tile == null) return null;
        var capability = tile.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
        return capability != null ? capability : tile instanceof net.minecraft.world.Container container
            ? new net.minecraftforge.items.wrapper.InvWrapper(container) : null;
    }
    *///?} else {
    /*public net.neoforged.neoforge.items.IItemHandler inventory() {
        BlockEntity tile = targetTile();
        if (tile == null) return null;
        var capability = level.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK, target(), null);
        return capability != null ? capability : tile instanceof net.minecraft.world.Container container
            ? new net.neoforged.neoforge.items.wrapper.InvWrapper(container) : null;
    }
    *///?}
}
