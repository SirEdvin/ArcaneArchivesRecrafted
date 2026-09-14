package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.data.BrazierRouteCache;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class BrazierRoutingTransactions {
    static void run(net.minecraft.gametest.framework.GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        var other = pos.east();
        require(level.isEmptyBlock(pos) && level.isEmptyBlock(other), "Routing transaction fixture occupied");
        try {
            level.setBlockAndUpdate(pos, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
            level.setBlockAndUpdate(other, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
            var first = (RadiantChestBlockEntity) level.getBlockEntity(pos);
            var second = (RadiantChestBlockEntity) level.getBlockEntity(other);
            var player = RuntimeTestVersion.player(helper);
            var owner = player.getUUID();
            first.setOwner(owner);
            second.setOwner(owner);
            verifyDevice(helper, first, second, player);
            int capacity = first.inventory().getStackLimit(0, new ItemStack(Items.DIAMOND));
            for (var chest : new RadiantChestBlockEntity[]{first, second}) {
                for (int slot = 0; slot < chest.inventory().getSlots(); slot++)
                    chest.inventory().setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, capacity));
            }
            first.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, capacity - 2));
            second.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, capacity - 3));
            var cache = new BrazierRouteCache();
            var offer = new ItemStack(Items.DIAMOND, 7);
            for (boolean commit : new boolean[]{false, true}) {
                try (var outer = Transaction.openOuter()) {
                    require(cache.insertTransactional(level, pos, owner, true, 40, offer, outer).getCount() == 2,
                        "Transactional route lost exact multi-destination remainder");
                    require(first.inventory().getStackInSlot(0).getCount() == capacity
                        && second.inventory().getStackInSlot(0).getCount() == capacity, "Route did not fill both destinations");
                    try (var nested = outer.openNested()) {
                        require(first.fabricStorage.getSlot(0).extract(
                            net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(Items.DIAMOND), 1, nested) == 1,
                            "Ordinary native destination access failed within routing transaction");
                    }
                    require(first.inventory().getStackInSlot(0).getCount() == capacity,
                        "Nested ordinary-access abort did not restore routed state");
                    if (commit) outer.commit();
                }
                require(first.inventory().getStackInSlot(0).getCount() == (commit ? capacity : capacity - 2)
                    && second.inventory().getStackInSlot(0).getCount() == (commit ? capacity : capacity - 3)
                    && offer.getCount() == 7, "Multi-destination outcome lost stock or mutated offer");
            }
            for (int slot = 0; slot < second.inventory().getSlots(); slot++) second.inventory().setStackInSlot(slot, ItemStack.EMPTY);
            level.setBlockAndUpdate(other, ContentRegistry.RADIANT_TROVE.get().defaultBlockState());
            var trove = (com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity) level.getBlockEntity(other);
            trove.setOwner(owner);
            int troveCapacity = trove.inventory().getStackLimit(0, offer);
            first.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, capacity - 2));
            trove.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, troveCapacity - 3));
            for (boolean commit : new boolean[]{false, true}) {
                try (var outer = Transaction.openOuter()) {
                    require(cache.insertTransactional(level, pos, owner, true, 40, offer, outer).getCount() == 2,
                        "Mixed Chest/Trove route lost exact partial remainder");
                    require(first.inventory().getStackInSlot(0).getCount() == capacity
                        && trove.inventory().getStackInSlot(0).getCount() == troveCapacity,
                        "Mixed route did not use both destination adapters");
                    if (commit) outer.commit();
                }
                require(first.inventory().getStackInSlot(0).getCount() == (commit ? capacity : capacity - 2)
                    && trove.inventory().getStackInSlot(0).getCount() == (commit ? troveCapacity : troveCapacity - 3)
                    && offer.getCount() == 7, "Mixed destination transaction failed commit/rollback conservation");
            }
            trove.optionals().setStackInSlot(0, new ItemStack(ContentRegistry.DEVOURING_CHARM.get()));
            var source = new net.minecraft.world.SimpleContainer(offer.copy());
            var sourceStorage = net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage.of(source, null);
            for (boolean commit : new boolean[]{false, true}) {
                try (var transaction = Transaction.openOuter()) {
                    require(cache.insertTransactional(level, pos, owner, true, 40, offer, transaction).isEmpty(),
                        "Full VOID Trove rejected transactional overflow");
                    require(sourceStorage.extract(net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(offer),
                        offer.getCount(), transaction) == offer.getCount(), "VOID source payment was not exact");
                    require(trove.inventory().getStackInSlot(0).getCount() == troveCapacity
                        && first.inventory().getStackInSlot(0).getCount() == capacity,
                        "VOID acceptance incorrectly increased stored count");
                    if (commit) transaction.commit();
                }
                require(source.getItem(0).getCount() == (commit ? 0 : 7)
                    && trove.inventory().getStackInSlot(0).getCount() == troveCapacity,
                    "VOID abort/commit lost source or changed full storage");
            }
            trove.optionals().setStackInSlot(0, ItemStack.EMPTY);
            trove.inventory().setStackInSlot(0, ItemStack.EMPTY);
            trove.optionals().setStackInSlot(0, new ItemStack(ContentRegistry.RADIANT_KEY.get()));
            require(trove.lockReference().isEmpty(), "Empty LOCK fixture already has a reference");
            for (boolean commit : new boolean[]{false, true}) {
                try (var transaction = Transaction.openOuter()) {
                    require(cache.insertTransactional(level, pos, owner, true, 40, offer, transaction).getCount() == 7,
                        "Routing filled an empty unbound Trove");
                    require(trove.fabricStorage.insert(net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(offer), 1, transaction) == 1,
                        "Ordinary native insertion failed to seed transactional LOCK");
                    require(cache.insertTransactional(level, pos, owner, true, 40, offer, transaction).isEmpty()
                        && trove.lockReference().is(Items.DIAMOND) && trove.lockReference().getCount() == 1,
                        "Transactional first deposit failed LOCK capture");
                    require(cache.insertTransactional(level, pos, owner, true, 40,
                        new ItemStack(Items.GOLD_INGOT, 3), transaction).getCount() == 3,
                        "Tentative LOCK accepted a different item");
                    if (commit) transaction.commit();
                }
                require(trove.inventory().getStackInSlot(0).getCount() == (commit ? 8 : 0)
                    && (commit ? trove.lockReference().is(Items.DIAMOND) : trove.lockReference().isEmpty()),
                    "LOCK transaction failed to restore/commit both stock and reference");
            }
        } finally {
            for (var target : new BlockPos[]{pos, other}) {
                if (level.getBlockEntity(target) instanceof RadiantChestBlockEntity chest)
                    for (int slot = 0; slot < chest.inventory().getSlots(); slot++) chest.inventory().setStackInSlot(slot, ItemStack.EMPTY);
                if (level.getBlockEntity(target) instanceof com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity trove) {
                    for (int slot = 0; slot < trove.optionals().getSlots(); slot++) trove.optionals().setStackInSlot(slot, ItemStack.EMPTY);
                    trove.inventory().setStackInSlot(0, ItemStack.EMPTY);
                }
                level.removeBlock(target, false);
            }
        }
    }
    private static void verifyDevice(net.minecraft.gametest.framework.GameTestHelper helper,
            RadiantChestBlockEntity first, RadiantChestBlockEntity second, net.minecraft.world.entity.player.Player player) {
        var level = helper.getLevel();
        var pos = first.getBlockPos().above();
        require(level.isEmptyBlock(pos), "Fabric Brazier fixture occupied");
        try {
            level.setBlockAndUpdate(pos, ContentRegistry.BRAZIER.get().defaultBlockState());
            var brazier = (com.aranaira.arcanearchives.tileentities.BrazierBlockEntity) level.getBlockEntity(pos);
            brazier.recordPlacer(player);
            verifySound(brazier, first, second);
            var variant = net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(Items.DIAMOND);
            for (int side = -1; side < net.minecraft.core.Direction.values().length; side++) {
                var storage = net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.find(level, pos,
                    side < 0 ? null : net.minecraft.core.Direction.values()[side]);
                require(storage == brazier.fabricStorage && !storage.supportsExtraction() && !storage.iterator().hasNext(),
                    "Fabric native lookup missing or exposed a local input buffer");
                for (boolean commit : new boolean[]{false, true}) {
                    try (var transaction = Transaction.openOuter()) {
                        require(storage.insert(variant, 7, transaction) == 7 && storage.extract(variant, 7, transaction) == 0,
                            "Native Fabric insertion/extraction contract failed");
                        if (commit) transaction.commit();
                    }
                    require(first.inventory().getStackInSlot(0).getCount() + second.inventory().getStackInSlot(0).getCount()
                        == (commit ? 7 : 0), "Native Fabric lookup transaction failed conservation");
                }
                first.inventory().setStackInSlot(0, ItemStack.EMPTY);
                second.inventory().setStackInSlot(0, ItemStack.EMPTY);
            }
            try (var transaction = Transaction.openOuter()) {
                require(brazier.fabricStorage.insert(variant, 0, transaction) == 0, "Zero request accepted items");
                boolean rejected = false;
                try { brazier.fabricStorage.insert(variant, -1, transaction); }
                catch (IllegalArgumentException expected) { rejected = true; }
                require(rejected, "Negative request was not rejected");
                long accepted = brazier.fabricStorage.insert(variant, Long.MAX_VALUE, transaction);
                long stored = 0;
                for (var chest : new RadiantChestBlockEntity[]{first, second})
                    for (int slot = 0; slot < chest.inventory().getSlots(); slot++) stored += chest.inventory().getStackInSlot(slot).getCount();
                require(accepted > 0 && accepted <= Integer.MAX_VALUE && accepted == stored,
                    "Long native request overflowed or misreported destination payment");
            }
            for (var chest : new RadiantChestBlockEntity[]{first, second})
                for (int slot = 0; slot < chest.inventory().getSlots(); slot++)
                    require(chest.inventory().getStackInSlot(slot).isEmpty(), "Large-request abort retained tentative stock");
            level.removeBlock(pos, false);
            try (var transaction = Transaction.openOuter()) {
                require(brazier.fabricStorage.insert(variant, 7, transaction) == 0, "Removed Fabric Brazier retained authority");
                transaction.commit();
            }
        } finally {
            level.removeBlock(pos, false);
        }
    }
    private static void verifySound(com.aranaira.arcanearchives.tileentities.BrazierBlockEntity brazier,
            RadiantChestBlockEntity first, RadiantChestBlockEntity second) {
        // Test-only observation of the real sound gate; no production accessor or audio-device claim.
        try {
            var sound = com.aranaira.arcanearchives.tileentities.BrazierBlockEntity.class.getDeclaredField("lastSound");
            sound.setAccessible(true);
            long previous = sound.getLong(brazier);
            var config = com.aranaira.arcanearchives.config.ServerSideConfig.current();
            require(config.useSounds() && config.brazierPickup(), "Sound transaction fixture requires enabled sound gates");
            try {
                for (boolean nestedCommit : new boolean[]{false, true}) for (boolean outerCommit : new boolean[]{false, true}) {
                    sound.setLong(brazier, 0);
                    try (var outer = Transaction.openOuter()) {
                        try (var nested = outer.openNested()) {
                            require(brazier.fabricStorage.insert(net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(Items.DIAMOND),
                                7, nested) == 7, "Sound transaction fixture did not fully insert");
                            if (nestedCommit) nested.commit();
                        }
                        require(sound.getLong(brazier) == 0, "Sound fired before outer transaction closed");
                        if (outerCommit) outer.commit();
                    }
                    require((sound.getLong(brazier) > 0) == (nestedCommit && outerCommit),
                        "Sound did not follow both nested and outer commit outcomes");
                    first.inventory().setStackInSlot(0, ItemStack.EMPTY);
                    second.inventory().setStackInSlot(0, ItemStack.EMPTY);
                }
                sound.setLong(brazier, 0);
                var variant = net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(Items.DIAMOND);
                try (var transaction = Transaction.openOuter()) {
                    long accepted = brazier.fabricStorage.insert(variant, Long.MAX_VALUE, transaction);
                    require(accepted > 0 && accepted < Long.MAX_VALUE, "Partial-sound fixture did not partially insert");
                    transaction.commit();
                }
                require(sound.getLong(brazier) == 0, "Committed partial acceptance triggered absorption sound");
                try (var transaction = Transaction.openOuter()) {
                    require(brazier.fabricStorage.insert(variant, 7, transaction) == 0,
                        "Refused-sound fixture still had capacity");
                    require(brazier.fabricStorage.insert(variant, 0, transaction) == 0, "Zero-sound fixture accepted items");
                    transaction.commit();
                }
                require(sound.getLong(brazier) == 0, "Refused/zero acceptance triggered absorption sound");
            } finally {
                sound.setLong(brazier, previous);
                for (var chest : new RadiantChestBlockEntity[]{first, second})
                    for (int slot = 0; slot < chest.inventory().getSlots(); slot++) chest.inventory().setStackInSlot(slot, ItemStack.EMPTY);
            }
        } catch (ReflectiveOperationException exception) { throw new AssertionError("Cannot observe native sound throttle", exception); }
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
