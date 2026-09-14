package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.data.BrazierRouteCache;
import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Direct deposits or filtered network pulls into a bounded output buffer. */
public final class BrazierBlockEntity extends NetworkOwnedBlockEntity {
    private final com.aranaira.arcanearchives.inventory.BrazierPullBuffer pull = new com.aranaira.arcanearchives.inventory.BrazierPullBuffer(this);
    public com.aranaira.arcanearchives.inventory.BrazierPullBuffer pull() { return pull; }
    private BrazierRouteCache routes = new BrazierRouteCache();
    private BrazierPlayerSelection playerSelection = new BrazierPlayerSelection();
    private int radius = 150;
    private boolean personalOnly;
    private static final String REJECTED = "arcanearchives:brazier_rejected";
    private long lastSound;

    //? if fabric {
    public final com.aranaira.arcanearchives.inventory.BrazierFabricStorage fabricStorage =
        new com.aranaira.arcanearchives.inventory.BrazierFabricStorage(this);
    public ItemStack insertTransactional(ItemStack offered,
            net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
        if (pull.enabled() || !live() || networkOwner() == null) return offered.copy();
        return routes.insertTransactional((ServerLevel) level, worldPosition, networkOwner(), personalOnly, radius, offered, transaction);
    }
    public void playAutomationSound() {
        if (live() && pickupSoundDue(System.currentTimeMillis(), com.aranaira.arcanearchives.config.ServerSideConfig.current()))
            level.playSound(null, worldPosition, ContentRegistry.BRAZIER_ABSORB.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1F, 1F);
    }
    //?}

    public BrazierBlockEntity(BlockPos pos, BlockState state) { super(ContentRegistry.BRAZIER_ENTITY.get(), pos, state); }
    public int radius() { return radius; }
    public boolean personalOnly() { return personalOnly; }

    /** Original range-particle geometry; never used for routing eligibility. */
    public net.minecraft.world.phys.AABB rangeBounds(int age, float partialTick) {
        float scale = Math.min((age + partialTick) / 20, 1);
        double horizontal = (radius + .5) * scale;
        double vertical = (255 + .5) * scale;
        var center = net.minecraft.world.phys.Vec3.atCenterOf(worldPosition);
        return new net.minecraft.world.phys.AABB(center.x - horizontal, center.y - vertical, center.z - horizontal,
            center.x + horizontal, center.y + vertical, center.z + horizontal).inflate(.01);
    }

    public boolean live() {
        return level instanceof ServerLevel server && server.getServer().isSameThread() && !isRemoved()
            && server.hasChunkAt(worldPosition) && server.getBlockEntity(worldPosition) == this;
    }

    public void configure(int radius, boolean personalOnly) {
        if (!live()) throw new IllegalStateException("Brazier configuration requires its installed server instance");
        this.radius = Math.max(0, Math.min(300, radius));
        this.personalOnly = personalOnly;
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    /** Approved 0147: local configuration is not restricted to the network owner. */
    public boolean canConfigure(net.minecraft.world.entity.player.Player player) {
        return live() && player.level() == level && !player.isSpectator()
            && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64
            && level.mayInteract(player, worldPosition);
    }

    public ItemStack insert(ItemStack offered, boolean simulate) {
        if (pull.enabled() || !live() || networkOwner() == null) return offered.copy();
        return routes.insert((ServerLevel) level, worldPosition, networkOwner(), personalOnly, radius, offered, simulate);
    }

    public ItemStack insertAutomated(ItemStack offered, boolean simulate) {
        ItemStack remainder = insert(offered, simulate);
        if (!simulate && !offered.isEmpty() && remainder.isEmpty()
                && pickupSoundDue(System.currentTimeMillis(), com.aranaira.arcanearchives.config.ServerSideConfig.current()))
            level.playSound(null, worldPosition, ContentRegistry.BRAZIER_ABSORB.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1F, 1F);
        return remainder;
    }

    //? if forge {
    /*private final net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler> automation =
        net.minecraftforge.common.util.LazyOptional.of(() -> new com.aranaira.arcanearchives.inventory.BrazierItemAutomation(this));
    @Override public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(
            net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.core.Direction side) {
        if (!isRemoved() && capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER) return automation.cast();
        return super.getCapability(capability, side);
    }
    @Override public void invalidateCaps() { super.invalidateCaps(); automation.invalidate(); }
    *///?}

    public void absorb(net.minecraft.world.entity.item.ItemEntity source) {
        absorb(source, entity -> level.addFreshEntity(entity));
    }

    /** Real collected player-source batch; the caller still owns source payment and remainder delivery. */
    public java.util.List<ItemStack> insertBatch(ItemStack reference, java.util.List<ItemStack> inputs) {
        if (pull.enabled() || !live() || networkOwner() == null) return inputs.stream().map(ItemStack::copy).toList();
        return routes.insertBatch((ServerLevel) level, worldPosition, networkOwner(), personalOnly, radius, reference, inputs);
    }

    /** The spawn boundary is injectable for cancellation/conservation tests. */
    public boolean deposit(net.minecraft.world.entity.player.Player player) {
        return deposit(player, System.currentTimeMillis(), entity -> level.addFreshEntity(entity));
    }

    boolean deposit(net.minecraft.world.entity.player.Player player, long now,
            java.util.function.Predicate<net.minecraft.world.entity.item.ItemEntity> spawn) {
        if (!live() || player.level() != level || player.isSpectator() || !level.mayInteract(player, worldPosition)) return false;
        if (pull.enabled()) { pull.interact(player); return true; }
        var pending = com.aranaira.arcanearchives.data.PlayerSaveData.get(((ServerLevel) level).getServer(), player.getUUID());
        // Recovery consumes this interaction; returned items need a separate explicit deposit.
        if (pending.hasBrazierPendingReturns()) return pending.deliverBrazierReturns(player);
        var selected = playerSelection.select(player, now, networkOwner() != null);
        if (selected == null) return false;
        var inventory = player.getInventory();
        for (int slot : selected.slots()) {
            if (!com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(
                    inventory.items.get(slot), selected.reference())) return false;
        }
        var inputs = new java.util.ArrayList<ItemStack>();
        for (int slot : selected.slots()) inputs.add(inventory.removeItem(slot, inventory.items.get(slot).getCount()));
        if (!inputs.isEmpty() && pickupSoundDue(now, com.aranaira.arcanearchives.config.ServerSideConfig.current()))
            level.playSound(null, worldPosition, ContentRegistry.BRAZIER_ABSORB.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1F, 1F);
        var remainders = insertBatch(selected.reference(), inputs);
        var failed = new java.util.ArrayList<ItemStack>();
        boolean held = selected.wasHeld();
        for (var remainder : remainders) {
            RadiantTroveBlockEntity.insertWithdrawal(held
                ? inventory.items.subList(inventory.selected, inventory.selected + 1) : inventory.items, remainder);
            held = false;
            if (!remainder.isEmpty() && !spawn.test(rejectedItem(remainder))) {
                // Approved 0145: retry inventory, then persist only unpaid/unspawned items.
                RadiantTroveBlockEntity.insertWithdrawal(inventory.items, remainder);
                if (!remainder.isEmpty()) failed.add(remainder);
            }
        }
        pending.queueBrazierReturns(failed, level.registryAccess());
        inventory.setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
        return true;
    }

    /** The spawn boundary is injectable for cancellation/conservation tests. */
    void absorb(net.minecraft.world.entity.item.ItemEntity source,
            java.util.function.Predicate<net.minecraft.world.entity.item.ItemEntity> spawn) {
        if (pull.enabled() || !live() || source.level() != level || !source.isAlive() || source.getTags().contains(REJECTED)) return;
        ItemStack remainder = insert(source.getItem(), false);
        if (pickupSoundDue(System.currentTimeMillis(), com.aranaira.arcanearchives.config.ServerSideConfig.current()))
            level.playSound(null, worldPosition, ContentRegistry.BRAZIER_ABSORB.get(), net.minecraft.sounds.SoundSource.BLOCKS, 1F, 1F);
        if (!remainder.isEmpty()) {
            var rejected = rejectedItem(remainder);
            if (!spawn.test(rejected)) {
                // Approved 0144: retain only unpaid items, never the accepted portion.
                source.setItem(remainder);
                source.setPickUpDelay(20);
                source.addTag(REJECTED);
                return;
            }
        }
        source.discard();
    }

    private net.minecraft.world.entity.item.ItemEntity rejectedItem(ItemStack remainder) {
        var rejected = new net.minecraft.world.entity.item.ItemEntity(level,
            worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5, remainder);
        double z = Math.min(.4F, Math.min((level.random.nextFloat() - .5F) * .6F, .2F));
        double x = Math.min(.4F, Math.min((level.random.nextFloat() - .5F) * .6F, .2F));
        rejected.setDeltaMovement(x, rejected.getDeltaMovement().y, z);
        rejected.setPickUpDelay(20);
        rejected.addTag(REJECTED);
        return rejected;
    }

    boolean pickupSoundDue(long now, com.aranaira.arcanearchives.config.ServerSideConfig config) {
        if (!config.useSounds() || !config.brazierPickup() || now - lastSound < 300) return false;
        lastSound = now;
        return true;
    }

    @Override public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
    //? if >=1.21 {
    @Override public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
    //?} else {
    /*@Override public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
    *///?}
        writeSettings(tag);
        //? if >=1.21 {
        pull.save(tag, registries);
        //?} else {
        /*pull.save(tag, null);
        *///?}
        return tag;
    }

    private void writeSettings(CompoundTag tag) {
        tag.putInt("range", radius);
        tag.putBoolean("subnetwork", personalOnly);
    }
    private void readSettings(CompoundTag tag) {
        radius = tag.contains("range") ? Math.max(0, Math.min(300, tag.getInt("range"))) : 150;
        personalOnly = tag.getBoolean("subnetwork");
        routes = new BrazierRouteCache();
        playerSelection = new BrazierPlayerSelection();
    }
    //? if >=1.21 {
    @Override protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        writeSettings(tag);
        pull.save(tag, registries);
    }
    @Override protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        readSettings(tag);
        pull.load(tag, registries);
    }
    //?} else {
    /*@Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeSettings(tag);
        pull.save(tag, null);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        readSettings(tag);
        pull.load(tag, null);
    }
    *///?}
}
