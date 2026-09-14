package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.RadiantTankStorage;
import com.aranaira.arcanearchives.inventory.handlers.SizeUpgradeItemHandler;
import com.aranaira.arcanearchives.inventory.handlers.StorageOptionalUpgrades;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class RadiantTankBlockEntity extends BlockEntity {
    private UUID owner;
    private boolean dropped;
    private final SizeUpgradeItemHandler upgrades = makeUpgrades(true);
    private final StorageOptionalUpgrades optionals = new StorageOptionalUpgrades(this::storageChanged);
    private final RadiantTankStorage inventory = new RadiantTankStorage(this);

    public RadiantTankBlockEntity(BlockPos pos, BlockState state) { super(ContentRegistry.RADIANT_TANK_ENTITY.get(), pos, state); }
    private SizeUpgradeItemHandler makeUpgrades(boolean notify) {
        return new SizeUpgradeItemHandler() {
            @Override public Item getUpgradeForSlot(int slot) {
                return switch (slot) {
                    case 0 -> ContentRegistry.MATRIX_BRACE.get();
                    case 1 -> ContentRegistry.CONTAINMENT_FIELD.get();
                    case 2 -> ContentRegistry.STORAGE_SHAPED_QUARTZ_ITEM.get();
                    default -> throw new IndexOutOfBoundsException(slot);
                };
            }
            @Override public boolean canReduceMultiplierTo(int size) {
                return inventory.storedAmount() <= RadiantTankStorage.capacityFor(size);
            }
            @Override public void onContentsChanged() {
                if (notify) { inventory.updateCapacity(); storageChanged(); }
            }
        };
    }
    public RadiantTankStorage inventory() { return inventory; }
    public SizeUpgradeItemHandler upgrades() { return upgrades; }
    public StorageOptionalUpgrades optionals() { return optionals; }
    public UUID owner() { return owner; }
    public void setOwner(UUID value) { owner = value; setChanged(); com.aranaira.arcanearchives.data.StoragePlacementSaveData.record(this); }
    public boolean isLiveServerStorage() {
        return level != null && !level.isClientSide && !isRemoved() && !dropped
            && level.getServer().isSameThread() && level.getBlockEntity(worldPosition) == this;
    }
    public boolean canUse(Player player) {
        return isLiveServerStorage() && player.level() == level && !player.isSpectator()
            && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64
            && level.mayInteract(player, worldPosition);
    }
    public void storageChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }
    public int comparatorSignal() {
        long amount = inventory.storedAmount();
        return amount == 0 ? 0 : (int) (14D * amount / RadiantTankStorage.capacityFor(upgrades.getUpgradesCount())) + 1;
    }
    public boolean installUpgrade(Player player, InteractionHand hand) {
        if (!canUse(player)) return false;
        ItemStack offered = player.getItemInHand(hand);
        if (offered.getItem() instanceof com.aranaira.arcanearchives.items.DevouringCharmItem) return optionals.install(player, hand);
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            if (!upgrades.isItemValid(slot, offered)) continue;
            ItemStack unit = offered.copy(); unit.setCount(1);
            if (!upgrades.insertItem(slot, unit, false).isEmpty()) return false;
            if (!player.getAbilities().instabuild) offered.shrink(1);
            player.getInventory().setChanged();
            return true;
        }
        return false;
    }
    public void dropPacked() {
        if (!isLiveServerStorage()) return;
        ItemStack stack = new ItemStack(ContentRegistry.RADIANT_TANK_ITEM.get());
        //? if >=1.21 {
        saveToItem(stack, level.registryAccess());
        //?} else {
        /*saveToItem(stack);
        *///?}
        dropped = true;
        Block.popResource(level, worldPosition, stack);
    }
    private void writeState(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("fluid_storage", inventory.writeState(registries));
        tag.put("size_upgrades", upgrades.serializeNBT(registries));
        tag.put("optional_upgrades", optionals.serializeNBT(registries));
        if (owner != null) tag.putUUID("owner", owner);
    }
    private void readState(CompoundTag tag, HolderLookup.Provider registries) {
        if (!tag.contains("fluid_storage")) return;
        SizeUpgradeItemHandler preparedUpgrades = makeUpgrades(false);
        StorageOptionalUpgrades preparedOptionals = new StorageOptionalUpgrades(() -> {});
        if (tag.contains("optional_upgrades")) preparedOptionals.deserializeNBT(registries, tag.getCompound("optional_upgrades"));
        preparedUpgrades.deserializeNBT(registries, tag.getCompound("size_upgrades"));
        RadiantTankStorage prepared = new RadiantTankStorage(this);
        prepared.readState(tag.getCompound("fluid_storage"), registries, preparedUpgrades.getUpgradesCount());
        upgrades.deserializeNBT(registries, tag.getCompound("size_upgrades"));
        optionals.deserializeNBT(registries, preparedOptionals.serializeNBT(registries));
        inventory.readState(tag.getCompound("fluid_storage"), registries, preparedUpgrades.getUpgradesCount());
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        com.aranaira.arcanearchives.data.StoragePlacementSaveData.record(this);
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    //? if >=1.21 {
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries); writeState(tag, registries); return tag;
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries); writeState(tag, registries);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        readState(tag, registries); super.loadAdditional(tag, registries);
    }
    //?} else {
    /*@Override public CompoundTag getUpdateTag() { CompoundTag tag = super.getUpdateTag(); writeState(tag, null); return tag; }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); writeState(tag, null); }
    @Override public void load(CompoundTag tag) { readState(tag, null); super.load(tag); }
    *///?}
    //? if forge {
    /*private final net.minecraftforge.common.util.LazyOptional<net.minecraftforge.fluids.capability.IFluidHandler> capability =
        net.minecraftforge.common.util.LazyOptional.of(() -> inventory);
    @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(
            net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.core.Direction side) {
        if (!isRemoved() && !dropped && capability == net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER) return this.capability.cast();
        return super.getCapability(capability, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); capability.invalidate(); }
    *///?}
}
