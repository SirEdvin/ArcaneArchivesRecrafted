package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.RadiantChestMenu;
import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class RadiantChestBlockEntity extends BlockEntity implements MenuProvider {
    private final ExtendedItemStackHandler inventory = new ExtendedItemStackHandler(54) {
        @Override public void onContentsChanged(int slot) {
            if (!transactionalWrite) storageChanged();
        }
    };
    private UUID owner;
    private String name = "";
    private boolean noNewStacks;
    private ItemStack displayStack = ItemStack.EMPTY;
    private Direction displayFacing = Direction.NORTH;
    private boolean dropped;
    private boolean transactionalWrite;
    //? if fabric {
    public final com.aranaira.arcanearchives.inventory.ChestFabricStorage fabricStorage =
        new com.aranaira.arcanearchives.inventory.ChestFabricStorage(this);
    //?}

    public boolean isLiveServerStorage() {
        return level != null && !level.isClientSide && !isRemoved() && !dropped
            && level.getServer().isSameThread() && level.getBlockEntity(worldPosition) == this;
    }

    public void storageChanged() {
        setChanged();
        if (level != null && !level.isClientSide) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
    }

    public void setTransactionalStack(int slot, ItemStack stack) {
        transactionalWrite = true;
        try { inventory.setStackInSlot(slot, stack); }
        finally { transactionalWrite = false; }
    }

    public RadiantChestBlockEntity(BlockPos pos, BlockState state) {
        super(ContentRegistry.RADIANT_CHEST_ENTITY.get(), pos, state);
    }

    public ExtendedItemStackHandler inventory() { return inventory; }
    public UUID owner() { return owner; }
    public void setOwner(UUID value) { owner = value; setChanged(); }
    public void setName(String value) { name = value; setChanged(); }
    public String chestName() { return name; }
    public boolean noNewStacks() { return noNewStacks; }

    /** Original packed-item ranking, distinct from component-sensitive insertion acceptance. */
    public int routingWeight(ItemStack offered) {
        if (offered.isEmpty()) return -1;
        int occupied = 0;
        long matching = 0;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stored = inventory.getStackInSlot(slot);
            if (stored.isEmpty()) continue;
            occupied++;
            if (stored.is(offered.getItem())) matching += stored.getCount();
        }
        if (noNewStacks) return matching == 0 ? -1 : 4999;
        if (matching == 0) return occupied;
        int stackSize = offered.getMaxStackSize() == 1 ? 1 : offered.getMaxStackSize()
            * com.aranaira.arcanearchives.config.ServerSideConfig.current().radiantMultiplier();
        return (int) Math.ceil((double) matching / ((long) stackSize * inventory.getSlots()) * 1000 + 500);
    }

    /** Caller must authorize configuration access; this guard rejects detached/client mutation. */
    public boolean toggleRoutingType() {
        if (!isLiveServerStorage()) return false;
        noNewStacks = !noNewStacks;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }

    public ItemStack displayStack() { return displayStack.copy(); }
    public Direction displayFacing() { return displayFacing; }

    void setDisplay(ItemStack stack, Direction facing) {
        displayStack = stack.copy();
        displayFacing = facing;
        setChanged();
        if (level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /** Use the server's held stacks, never a client-supplied display item. */
    public boolean editDisplay(Player player, Direction facing) {
        if (level == null || player.isSpectator() || !stillValid(player) || !level.mayInteract(player, worldPosition)) return false;
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        ItemStack offered;
        if (main.is(ContentRegistry.SCEPTER_MANIPULATION.get())) offered = off;
        else if (off.is(ContentRegistry.SCEPTER_MANIPULATION.get())) offered = main;
        else return false;
        if (offered.isEmpty() && !player.isShiftKeyDown()) return false;
        if (!level.isClientSide && isLiveServerStorage())
            setDisplay(offered, offered.isEmpty() ? displayFacing : facing);
        return true;
    }

    public boolean stillValid(Player player) {
        return level != null && !isRemoved() && !dropped && player.level() == level
            && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64;
    }

    @Override public Component getDisplayName() {
        return name.isEmpty() ? Component.translatable("block.arcanearchives.radiant_chest") : Component.literal(name);
    }

    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return !level.isClientSide && stillValid(player) ? new RadiantChestMenu(id, inventory, this) : null;
    }

    public int comparatorSignal() { return inventory.calcRedstone(); }

    public void dropContents() {
        if (level == null || level.isClientSide || dropped) return;
        dropped = true;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot).copy();
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
            while (!stack.isEmpty()) Block.popResource(level, worldPosition, stack.split(stack.getMaxStackSize()));
        }
    }

    private void writeState(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("inventory", inventory.serializeNBT(registries));
        if (owner != null) tag.putUUID("owner", owner);
        writeVisualState(tag, registries);
    }

    private void writeVisualState(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("routingType", noNewStacks ? 1 : 0);
        tag.putString("chestName", name);
        tag.putInt("displayFacing", displayFacing.get3DDataValue());
        if (!displayStack.isEmpty()) {
            //? if >=1.21 {
            tag.put("displayStack", displayStack.save(registries));
            //?} else {
            /*tag.put("displayStack", displayStack.save(new CompoundTag()));
            *///?}
        } else tag.remove("displayStack");
    }

    private void readState(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("inventory")) inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        name = tag.getString("chestName");
        noNewStacks = tag.getInt("routingType") == 1;
        displayFacing = tag.contains("displayFacing") ? Direction.from3DDataValue(tag.getInt("displayFacing")) : Direction.NORTH;
        //? if >=1.21 {
        displayStack = tag.contains("displayStack") ? ItemStack.parse(registries, tag.getCompound("displayStack")).orElse(ItemStack.EMPTY) : ItemStack.EMPTY;
        //?} else {
        /*displayStack = tag.contains("displayStack") ? ItemStack.of(tag.getCompound("displayStack")) : ItemStack.EMPTY;
        *///?}
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    //? if >=1.21 {
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        writeVisualState(tag, registries);
        return tag;
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        writeState(tag, registries);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        readState(tag, registries);
    }
    //?} else {
    /*@Override public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        writeVisualState(tag, null);
        return tag;
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeState(tag, null);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        readState(tag, null);
    }
    *///?}

    //? if forge {
    /*private final net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler> capability =
        net.minecraftforge.common.util.LazyOptional.of(() -> inventory);
    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(
            net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.core.Direction side) {
        if (!isRemoved() && capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER)
            return this.capability.cast();
        return super.getCapability(capability, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); capability.invalidate(); }
    *///?}
}
