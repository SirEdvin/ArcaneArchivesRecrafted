package com.aranaira.arcanearchives.data;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Per-device transient cache. A cache entry never grants access or holds a world alive. */
public final class BrazierRouteCache {
    private final Map<Item, Entry> entries = new HashMap<>();
    private final LongSupplier clock;

    public BrazierRouteCache() { this(System::currentTimeMillis); }
    public BrazierRouteCache(LongSupplier clock) { this.clock = java.util.Objects.requireNonNull(clock); }

    public ItemStack insert(ServerLevel level, BlockPos origin, UUID owner, boolean personalOnly,
            int radius, ItemStack offered, boolean simulate) {
        if (!level.getServer().isSameThread()) throw new IllegalStateException("Brazier cache requires the server thread");
        if (offered.isEmpty()) return ItemStack.EMPTY;
        return BrazierRoutes.insert(level, origin, owner, personalOnly, radius, offered,
            preferred(level, origin, owner, personalOnly, radius, offered), simulate, destination -> accepted(offered.getItem(), destination));
    }

    public java.util.List<ItemStack> insertBatch(ServerLevel level, BlockPos origin, UUID owner, boolean personalOnly,
            int radius, ItemStack reference, java.util.List<ItemStack> inputs) {
        if (!level.getServer().isSameThread()) throw new IllegalStateException("Brazier cache requires the server thread");
        return BrazierRoutes.insertBatch(level, origin, owner, personalOnly, radius, reference, inputs,
            preferred(level, origin, owner, personalOnly, radius, reference), destination -> accepted(reference.getItem(), destination));
    }

    //? if fabric {
    public ItemStack insertTransactional(ServerLevel level, BlockPos origin, UUID owner, boolean personalOnly,
            int radius, ItemStack offered, net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
        if (!level.getServer().isSameThread()) throw new IllegalStateException("Brazier cache requires the server thread");
        if (offered.isEmpty()) return ItemStack.EMPTY;
        // Aborted probes may refresh transient preference, just like upstream boolean simulation.
        return BrazierRoutes.insert(level, origin, owner, personalOnly, radius, offered,
            preferred(level, origin, owner, personalOnly, radius, offered), false,
            destination -> accepted(offered.getItem(), destination),
            (destination, stack) -> BrazierRoutes.acceptTransactional(destination, stack, transaction));
    }
    //?}

    private BlockEntity preferred(ServerLevel level, BlockPos origin, UUID owner, boolean personalOnly,
            int radius, ItemStack offered) {
        Item key = offered.getItem();
        Entry previous = entries.get(key);
        BlockEntity preferred = previous == null ? null : previous.destination.get();
        if (previous != null && (!previous.valid(clock.getAsLong())
                || !BrazierRoutes.eligible(level, origin, owner, personalOnly, radius, preferred, offered))) {
            entries.remove(key);
            preferred = null;
        }
        return preferred;
    }

    private void accepted(Item key, BlockEntity destination) {
        long now = clock.getAsLong();
        Entry current = entries.get(key);
        // Upstream refreshes a still-valid entry, rather than replacing its destination on fallback success.
        if (current != null && current.valid(now)) current.insertedAt = now;
        else entries.put(key, new Entry(destination, now));
    }

    private static final class Entry {
        private final WeakReference<BlockEntity> destination;
        private long insertedAt;
        private Entry(BlockEntity destination, long now) {
            this.destination = new WeakReference<>(destination);
            insertedAt = now;
        }
        private boolean valid(long now) { return now - insertedAt < 1000; }
    }
}
