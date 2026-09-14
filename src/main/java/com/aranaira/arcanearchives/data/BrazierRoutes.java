package com.aranaira.arcanearchives.data;

import com.aranaira.arcanearchives.tileentities.GemCuttersTableBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Current destination selection; callers must revalidate cached entries immediately before insertion. */
public final class BrazierRoutes {
    private BrazierRoutes() {}

    public static boolean eligible(ServerLevel level, BlockPos origin, UUID owner, boolean personalOnly,
            int radius, BlockEntity destination, ItemStack offered) {
        return eligibleWeight(level, origin, owner, personalOnly, radius, destination, offered) >= 0;
    }

    private static int eligibleWeight(ServerLevel level, BlockPos origin, UUID owner, boolean personalOnly,
            int radius, BlockEntity destination, ItemStack offered) {
        if (!level.getServer().isSameThread()) throw new IllegalStateException("Brazier routing requires the server thread");
        if (owner == null || radius < 0 || radius > 300 || destination == null || destination.isRemoved()
                || destination.getLevel() != level || !level.hasChunkAt(destination.getBlockPos())) return -1;
        BlockPos target = destination.getBlockPos();
        long dx = (long) target.getX() - origin.getX();
        long dz = (long) target.getZ() - origin.getZ();
        if (dx * dx + dz * dz > (long) radius * radius || level.getBlockEntity(target) != destination) return -1;
        UUID destinationOwner = StorageNetworks.owner(destination);
        return destinationOwner != null && StorageNetworks.audience(level.getServer(), owner, personalOnly).contains(destinationOwner)
            ? weight(destination, offered) : -1;
    }

    public static List<BlockEntity> collect(ServerLevel level, BlockPos origin, UUID owner, boolean personalOnly,
            int radius, ItemStack offered) {
        if (owner == null) return List.of();
        return StorageNetworks.visible(level.getServer(), owner, personalOnly).stream()
            .map(destination -> Map.entry(destination, eligibleWeight(level, origin, owner, personalOnly, radius, destination, offered)))
            .filter(entry -> entry.getValue() >= 0)
            .sorted(Map.Entry.<BlockEntity, Integer>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .toList();
    }

    /** One fresh-route deposit; the caller remains responsible for paying its source exactly once. */
    public static ItemStack insert(ServerLevel level, BlockPos origin, UUID owner, boolean personalOnly,
            int radius, ItemStack offered, boolean simulate) {
        return insert(level, origin, owner, personalOnly, radius, offered, null, simulate);
    }

    /** The caller supplies only a time-valid cached destination; current eligibility is never cached. */
    public static ItemStack insert(ServerLevel level, BlockPos origin, UUID owner, boolean personalOnly,
            int radius, ItemStack offered, BlockEntity preferred, boolean simulate) {
        return insert(level, origin, owner, personalOnly, radius, offered, preferred, simulate, destination -> {});
    }

    static ItemStack insert(ServerLevel level, BlockPos origin, UUID owner, boolean personalOnly,
            int radius, ItemStack offered, BlockEntity preferred, boolean simulate,
            java.util.function.Consumer<BlockEntity> accepted) {
        return insert(level, origin, owner, personalOnly, radius, offered, preferred, simulate, accepted,
            (destination, stack) -> accept(destination, stack, simulate));
    }

    static ItemStack insert(ServerLevel level, BlockPos origin, UUID owner, boolean personalOnly,
            int radius, ItemStack offered, BlockEntity preferred, boolean simulate,
            java.util.function.Consumer<BlockEntity> accepted,
            java.util.function.BiFunction<BlockEntity, ItemStack, ItemStack> transfer) {
        if (offered.isEmpty()) return ItemStack.EMPTY;
        ItemStack remainder = offered.copy();
        if (eligible(level, origin, owner, personalOnly, radius, preferred, remainder))
            remainder = transfer.apply(preferred, remainder);
        if (remainder.isEmpty()) { accepted.accept(preferred); return remainder; }
        for (BlockEntity destination : collect(level, origin, owner, personalOnly, radius, offered)) {
            if (remainder.isEmpty()) break;
            // A simulation does not reserve capacity: never simulate the preferred destination twice.
            if (destination == preferred && simulate) continue;
            if (!eligible(level, origin, owner, personalOnly, radius, destination, remainder)) continue;
            remainder = transfer.apply(destination, remainder);
            if (remainder.isEmpty()) accepted.accept(destination);
        }
        return remainder.copy();
    }

    /** Real source batch: a remainder advances the whole remaining batch to the next route. */
    static List<ItemStack> insertBatch(ServerLevel level, BlockPos origin, UUID owner, boolean personalOnly,
            int radius, ItemStack reference, List<ItemStack> inputs, BlockEntity preferred,
            java.util.function.Consumer<BlockEntity> accepted) {
        var remaining = new java.util.ArrayList<ItemStack>();
        for (ItemStack input : inputs) if (!input.isEmpty()) remaining.add(input.copy());
        if (remaining.isEmpty()) return remaining;
        if (eligible(level, origin, owner, personalOnly, radius, preferred, reference))
            acceptBatch(level, origin, owner, personalOnly, radius, reference, remaining, preferred, accepted);
        if (!remaining.isEmpty()) {
            for (BlockEntity destination : collect(level, origin, owner, personalOnly, radius, reference)) {
                acceptBatch(level, origin, owner, personalOnly, radius, reference, remaining, destination, accepted);
                if (remaining.isEmpty()) break;
            }
        }
        return remaining;
    }

    private static void acceptBatch(ServerLevel level, BlockPos origin, UUID owner, boolean personalOnly,
            int radius, ItemStack reference, List<ItemStack> remaining, BlockEntity destination,
            java.util.function.Consumer<BlockEntity> accepted) {
        var iterator = remaining.listIterator();
        while (iterator.hasNext()) {
            if (!eligible(level, origin, owner, personalOnly, radius, destination, reference)) return;
            ItemStack result = accept(destination, iterator.next(), false);
            if (!result.isEmpty()) {
                iterator.set(result.copy());
                return;
            }
            iterator.remove();
            accepted.accept(destination);
        }
    }

    private static ItemStack accept(BlockEntity destination, ItemStack offered, boolean simulate) {
        if (destination instanceof RadiantChestBlockEntity chest && chest.isLiveServerStorage())
            return chest.inventory().insertItemStacked(offered, simulate);
        if (destination instanceof RadiantTroveBlockEntity trove && trove.isLiveServerStorage())
            return trove.inventory().insertItem(0, offered, simulate);
        if (destination instanceof GemCuttersTableBlockEntity table) return table.acceptRoutingInput(offered, simulate);
        return offered.copy();
    }

    //? if fabric {
    static ItemStack acceptTransactional(BlockEntity destination, ItemStack offered,
            net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext transaction) {
        if (destination instanceof GemCuttersTableBlockEntity table)
            return table.acceptRoutingInputTransactional(offered, transaction);
        var variant = net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(offered);
        ItemStack remainder = offered.copy();
        if (destination instanceof RadiantChestBlockEntity chest && chest.isLiveServerStorage()) {
            for (int pass = 0; pass < 2 && !remainder.isEmpty(); pass++) {
                for (int slot = 0; slot < chest.inventory().getSlots() && !remainder.isEmpty(); slot++) {
                    ItemStack stored = chest.inventory().getStackInSlot(slot);
                    if (pass == 0 ? !stored.isEmpty()
                            && com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(stored, remainder)
                            : stored.isEmpty())
                        remainder.shrink((int) chest.fabricStorage.getSlot(slot).insert(variant, remainder.getCount(), transaction));
                }
            }
        } else if (destination instanceof RadiantTroveBlockEntity trove && trove.isLiveServerStorage()) {
            remainder.shrink((int) trove.fabricStorage.insert(variant, remainder.getCount(), transaction));
        }
        return remainder;
    }
    //?}

    private static int weight(BlockEntity destination, ItemStack offered) {
        if (offered.isEmpty()) return -1;
        if (destination instanceof RadiantChestBlockEntity chest) return chest.routingWeight(offered);
        if (destination instanceof GemCuttersTableBlockEntity table) return table.routingWeight(offered);
        if (destination instanceof RadiantTroveBlockEntity trove) {
            int score = trove.troveRoutingScore(offered);
            if (score != -1) return score;
            // Original NO_NEW_STACKS fallback uses packed item identity, not insertion's data equality.
            ItemStack stored = trove.inventory().getStackInSlot(0);
            return !stored.isEmpty() && stored.is(offered.getItem()) ? 4999 : -1;
        }
        return -1; // Manifest inventories and monitoring targets are not automatically routing destinations.
    }
}
