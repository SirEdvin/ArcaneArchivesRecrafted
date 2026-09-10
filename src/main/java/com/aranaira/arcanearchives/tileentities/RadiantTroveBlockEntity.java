package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
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

/** Release Trove storage: one item identity, native-sized withdrawals and portable contents. */
public final class RadiantTroveBlockEntity extends BlockEntity {
    private UUID owner;
    private UUID lastPlayer;
    private long lastDeposit;
    private long lastWithdrawal;
    private boolean dropped;
    private boolean spillOnRemoval;
    private boolean transactionalWrite;
    private final SizeUpgradeItemHandler upgrades = makeUpgrades(true);
    private final StorageOptionalUpgrades optionals = new StorageOptionalUpgrades(this::storageChanged, true);
    private final ExtendedItemStackHandler lockReference = new ExtendedItemStackHandler(1);
    private final ExtendedItemStackHandler inventory = new ExtendedItemStackHandler(1) {
        @Override public int getSlotLimit(int slot) { validateSlotIndex(slot); return capacity(ItemStack.EMPTY, upgrades.getUpgradesCount()); }
        @Override public int getStackLimit(int slot, ItemStack stack) { validateSlotIndex(slot); return capacity(stack, upgrades.getUpgradesCount()); }
        @Override public ItemStack getStackInSlot(int slot) { return super.getStackInSlot(slot).copy(); }
        @Override public boolean isItemValid(int slot, ItemStack stack) { validateSlotIndex(slot); return acceptsItem(stack); }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            validateSlotIndex(slot);
            if (!isLiveServerStorage()) return stack.copy();
            boolean matching = acceptsItem(stack);
            ItemStack remainder = super.insertItem(slot, stack, simulate);
            return matching && optionals.isVoiding() ? ItemStack.EMPTY : remainder;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack stack = getStackInSlot(slot);
            return super.extractItem(slot, Math.min(amount, stack.getMaxStackSize()), simulate);
        }
        @Override public void setStackInSlot(int slot, ItemStack stack) {
            if (!stack.isEmpty() && stack.getCount() > getStackLimit(slot, stack)) throw new IllegalArgumentException("Trove capacity exceeded");
            super.setStackInSlot(slot, stack);
        }
        @Override public void onContentsChanged(int slot) { if (!transactionalWrite) storageChanged(); }
    };
    //? if fabric {
    public final com.aranaira.arcanearchives.inventory.TroveFabricStorage fabricStorage = new com.aranaira.arcanearchives.inventory.TroveFabricStorage(this);
    //?}

    public RadiantTroveBlockEntity(BlockPos pos, BlockState state) { super(ContentRegistry.RADIANT_TROVE_ENTITY.get(), pos, state); }

    private SizeUpgradeItemHandler makeUpgrades(boolean notify) {
        return new SizeUpgradeItemHandler() {
            @Override public Item getUpgradeForSlot(int slot) {
                return switch (slot) {
                    case 0 -> ContentRegistry.MATRIX_BRACE.get();
                    case 1 -> ContentRegistry.MATERIAL_INTERFACE.get();
                    case 2 -> ContentRegistry.STORAGE_SHAPED_QUARTZ_ITEM.get();
                    default -> throw new IndexOutOfBoundsException(slot);
                };
            }
            @Override public boolean canReduceMultiplierTo(int size) {
                ItemStack stack = inventory.getStackInSlot(0);
                return stack.getCount() <= capacity(stack, size);
            }
            @Override public void onContentsChanged() { if (notify) storageChanged(); }
        };
    }

    public static int capacity(ItemStack reference, int upgrades) {
        return Math.multiplyExact(Math.multiplyExact(Math.max(64, reference.getMaxStackSize()), 512), upgrades + 1);
    }
    public ExtendedItemStackHandler inventory() { return inventory; }
    public SizeUpgradeItemHandler upgrades() { return upgrades; }
    public StorageOptionalUpgrades optionals() { return optionals; }
    public ItemStack lockReference() { return lockReference.getStackInSlot(0).copy(); }
    public void restoreLockReference(ItemStack stack) {
        ItemStack unit = stack.copy();
        if (!unit.isEmpty()) unit.setCount(1);
        lockReference.setStackInSlot(0, unit);
    }
    private void captureLockReference() {
        if (!optionals.isLocked()) restoreLockReference(ItemStack.EMPTY);
        else if (lockReference().isEmpty()) restoreLockReference(inventory.getStackInSlot(0));
    }
    public boolean acceptsItem(ItemStack stack) {
        ItemStack current = inventory.getStackInSlot(0);
        if (!current.isEmpty()) return ExtendedItemStackHandler.sameItemAndData(current, stack);
        ItemStack reference = lockReference();
        return !optionals.isLocked() || reference.isEmpty() || ExtendedItemStackHandler.sameItemAndData(reference, stack);
    }
    public void setOwner(UUID value) { owner = value; setChanged(); }
    public UUID owner() { return owner; }
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
        captureLockReference();
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }
    public void setTransactionalStack(ItemStack stack) {
        transactionalWrite = true;
        try { inventory.setStackInSlot(0, stack); captureLockReference(); }
        finally { transactionalWrite = false; }
    }
    public int comparatorSignal() { return inventory.calcRedstone(); }

    public boolean installUpgrade(Player player, InteractionHand hand) {
        if (!canUse(player)) return false;
        ItemStack offered = player.getItemInHand(hand);
        if (offered.getItem() instanceof com.aranaira.arcanearchives.items.DevouringCharmItem
                || offered.getItem() instanceof com.aranaira.arcanearchives.items.RadiantKeyItem) return optionals.install(player, hand);
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            if (!upgrades.isItemValid(slot, offered)) continue;
            ItemStack unit = offered.copy();
            unit.setCount(1);
            if (!upgrades.insertItem(slot, unit, false).isEmpty()) return false;
            if (!player.getAbilities().instabuild) offered.shrink(1);
            player.getInventory().setChanged();
            return true;
        }
        return false;
    }

    public void deposit(Player player) {
        if (!canUse(player)) return;
        long now = System.nanoTime();
        boolean doubleClick = player.getUUID().equals(lastPlayer) && now - lastDeposit <= 800_000_000L;
        lastPlayer = player.getUUID();
        lastDeposit = now;
        ItemStack held = player.getMainHandItem();
        ItemStack reference = inventory.getStackInSlot(0);
        if (reference.isEmpty()) reference = held.copy();
        if (reference.isEmpty()) return;
        if (!held.isEmpty() && ExtendedItemStackHandler.sameItemAndData(reference, held)) {
            ItemStack remainder = inventory.insertItem(0, held, false);
            player.setItemInHand(InteractionHand.MAIN_HAND, remainder);
            if (!remainder.isEmpty()) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("arcanearchives.error.trove_insertion_failed.full"), true);
                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();
                return;
            }
        } else if (!held.isEmpty() && !doubleClick) return;
        if (doubleClick) {
            for (int slot = 0; slot < 36; slot++) {
                ItemStack candidate = player.getInventory().getItem(slot);
                if (!candidate.isEmpty() && ExtendedItemStackHandler.sameItemAndData(reference, candidate)) {
                    ItemStack remainder = inventory.insertItem(0, candidate, false);
                    player.getInventory().setItem(slot, remainder);
                    if (!remainder.isEmpty()) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.translatable("arcanearchives.error.trove_insertion_failed.full"), true);
                        break;
                    }
                }
            }
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }

    public void withdraw(Player player) {
        if (!canUse(player)) return;
        if (player.isShiftKeyDown() && player.getMainHandItem().getItem()
                instanceof com.aranaira.arcanearchives.items.StorageScepterItem) return;
        long now = System.nanoTime();
        if (now - lastWithdrawal < 150_000_000L) return;
        lastWithdrawal = now;
        ItemStack stored = inventory.getStackInSlot(0);
        ItemStack result = inventory.extractItem(0, player.isShiftKeyDown() ? 1 : stored.getMaxStackSize(), false);
        if (!result.isEmpty()) {
            insertWithdrawal(player.getInventory().items, result);
            if (!result.isEmpty()) player.drop(result, false);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
    }

    // Unlike Inventory.add, the original main-inventory handler never voids creative overflow.
    static void insertWithdrawal(java.util.List<ItemStack> slots, ItemStack remainder) {
        for (ItemStack present : slots) {
            if (remainder.isEmpty()) return;
            if (present.isEmpty() || !ExtendedItemStackHandler.sameItemAndData(present, remainder)) continue;
            int moved = Math.min(remainder.getCount(), Math.max(0, Math.min(64, present.getMaxStackSize()) - present.getCount()));
            present.grow(moved);
            remainder.shrink(moved);
        }
        for (int slot = 0; slot < slots.size() && !remainder.isEmpty(); slot++) {
            if (slots.get(slot).isEmpty()) slots.set(slot, remainder.split(Math.min(64, remainder.getMaxStackSize())));
        }
    }

    /** Scope the drop mode to the authorized native removal, including failed removals. */
    public boolean removeByPlayer(Player player, java.util.function.BooleanSupplier removal) {
        boolean previous = spillOnRemoval;
        spillOnRemoval = !player.isCreative() && !player.isSpectator() && player.isShiftKeyDown()
            && player.getMainHandItem().getItem() instanceof com.aranaira.arcanearchives.items.StorageScepterItem;
        try { return removal.getAsBoolean(); }
        finally { spillOnRemoval = previous; }
    }

    public void dropPacked() {
        if (!isLiveServerStorage()) return;
        if (spillOnRemoval) {
            dropped = true;
            // Split actual contents, not the LOCK reference: an empty locked Trove must terminate.
            ItemStack contents = inventory.getStackInSlot(0);
            while (!contents.isEmpty()) Block.popResource(level, worldPosition,
                contents.split(Math.min(64, contents.getMaxStackSize())));
            for (int slot = 0; slot < optionals.getSlots(); slot++)
                Block.popResource(level, worldPosition, optionals.getStackInSlot(slot).copy());
            for (int slot = 0; slot < upgrades.getSlots(); slot++)
                Block.popResource(level, worldPosition, upgrades.getStackInSlot(slot).copy());
            Block.popResource(level, worldPosition, new ItemStack(ContentRegistry.RADIANT_TROVE_ITEM.get()));
            return;
        }
        ItemStack stack = new ItemStack(ContentRegistry.RADIANT_TROVE_ITEM.get());
        //? if >=1.21 {
        saveToItem(stack, level.registryAccess());
        //?} else {
        /*saveToItem(stack);
        *///?}
        dropped = true;
        Block.popResource(level, worldPosition, stack);
    }

    private void writeState(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.put("size_upgrades", upgrades.serializeNBT(registries));
        tag.put("optional_upgrades", optionals.serializeNBT(registries));
        tag.put("lock_reference", lockReference.serializeNBT(registries));
        if (owner != null) tag.putUUID("owner", owner);
    }
    private void readState(CompoundTag tag, HolderLookup.Provider registries) {
        if (!tag.contains("inventory")) return;
        SizeUpgradeItemHandler preparedUpgrades = makeUpgrades(false);
        StorageOptionalUpgrades preparedOptionals = new StorageOptionalUpgrades(() -> {}, true);
        if (tag.contains("optional_upgrades")) preparedOptionals.deserializeNBT(registries, tag.getCompound("optional_upgrades"));
        preparedUpgrades.deserializeNBT(registries, tag.getCompound("size_upgrades"));
        ExtendedItemStackHandler prepared = new ExtendedItemStackHandler(1);
        prepared.deserializeNBT(registries, tag.getCompound("inventory"));
        ItemStack stack = prepared.getStackInSlot(0);
        ExtendedItemStackHandler preparedReference = new ExtendedItemStackHandler(1);
        if (tag.contains("lock_reference")) preparedReference.deserializeNBT(registries, tag.getCompound("lock_reference"));
        ItemStack reference = preparedReference.getStackInSlot(0);
        if (reference.getCount() > 1 || preparedOptionals.isLocked() && !reference.isEmpty() && !stack.isEmpty()
                && !ExtendedItemStackHandler.sameItemAndData(reference, stack)) throw new IllegalArgumentException("Invalid saved Trove lock reference");
        if (stack.getCount() > capacity(stack, preparedUpgrades.getUpgradesCount())) throw new IllegalArgumentException("Saved Trove exceeds capacity");
        upgrades.deserializeNBT(registries, tag.getCompound("size_upgrades"));
        optionals.deserializeNBT(registries, preparedOptionals.serializeNBT(registries));
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        restoreLockReference(reference);
        captureLockReference();
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
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
    /*private final net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler> capability =
        net.minecraftforge.common.util.LazyOptional.of(() -> new com.aranaira.arcanearchives.inventory.TroveItemAutomation(this));
    @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(
            net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.core.Direction side) {
        if (!isRemoved() && !dropped && capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER) return this.capability.cast();
        return super.getCapability(capability, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); capability.invalidate(); }
    *///?}
}
