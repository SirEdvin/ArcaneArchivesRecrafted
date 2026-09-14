package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.GemCuttersTableBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

final class GemCutterRoutingTransactions {
    static void run(net.minecraft.gametest.framework.GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        var state = ContentRegistry.GEMCUTTERS_TABLE.get().defaultBlockState();
        var other = com.aranaira.arcanearchives.blocks.GemCuttersTable.connectedPos(pos, state);
        require(level.isEmptyBlock(pos) && level.isEmptyBlock(other), "Transaction fixture occupied");
        try {
            level.setBlock(pos, state, 2);
            level.setBlock(other, state.setValue(com.aranaira.arcanearchives.blocks.GemCuttersTable.ACCESSOR, true), 2);
            var table = (GemCuttersTableBlockEntity) level.getBlockEntity(pos);
            table.insertInput(0, new ItemStack(Items.DIAMOND, 60), false);
            var offered = new ItemStack(Items.DIAMOND, 7);
            mixedRouting(helper, table, offered);
            for (boolean commitOuter : new boolean[]{false, true}) {
                try (var outer = Transaction.openOuter()) {
                    require(table.acceptRoutingInputTransactional(new ItemStack(Items.DIAMOND, 2), outer).isEmpty(), "Outer top-up refused");
                    try (var nested = outer.openNested()) {
                        require(table.acceptRoutingInputTransactional(offered, nested).getCount() == 5, "Nested remainder incorrect");
                    }
                    require(table.getInput(0).getCount() == 62, "Nested abort failed restoration");
                    try (var nested = outer.openNested()) {
                        require(table.acceptRoutingInputTransactional(offered, nested).getCount() == 5, "Nested repeat double-counted capacity");
                        nested.commit();
                    }
                    require(table.getInput(0).getCount() == 64 && table.getInput(1).isEmpty(), "Top-up filled empty input or lost items");
                    if (commitOuter) outer.commit();
                }
                require(table.getInput(0).getCount() == (commitOuter ? 64 : 60), "Outer transaction outcome incorrect");
                require(offered.getCount() == 7 && table.getOutput().isEmpty(), "Transaction changed source/output");
            }
            table.extractInput(0, 4, false);
            var source = new net.minecraft.world.SimpleContainer(new ItemStack(Items.DIAMOND, 7));
            var storage = net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage.of(source, null);
            var variant = net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(Items.DIAMOND);
            for (boolean commit : new boolean[]{false, true}) {
                try (var transaction = Transaction.openOuter()) {
                    var remainder = table.acceptRoutingInputTransactional(source.getItem(0).copy(), transaction);
                    int accepted = source.getItem(0).getCount() - remainder.getCount();
                    require(accepted == 4 && storage.extract(variant, accepted, transaction) == accepted,
                        "Native source could not pay exact accepted amount");
                    require(source.getItem(0).getCount() == 3 && table.getInput(0).getCount() == 64,
                        "In-transaction source/destination conservation failed");
                    if (commit) transaction.commit();
                }
                require(source.getItem(0).getCount() == (commit ? 3 : 7)
                    && table.getInput(0).getCount() == (commit ? 64 : 60),
                    "Native source and Gem Cutter did not share transaction outcome");
            }
            table.extractInput(0, 4, false);
            // Deliberately stale offer: the source has only three items left, but the destination accepts four.
            try (var transaction = Transaction.openOuter()) {
                var remainder = table.acceptRoutingInputTransactional(offered, transaction);
                int accepted = offered.getCount() - remainder.getCount();
                require(accepted == 4 && storage.extract(variant, accepted, transaction) == 3,
                    "Underpayment fixture did not exercise partial source extraction");
                require(source.getItem(0).isEmpty() && table.getInput(0).getCount() == 64,
                    "Underpayment fixture did not mutate both participants");
                // No commit: source underpayment must cancel the entire attempted transfer.
            }
            require(source.getItem(0).getCount() == 3 && table.getInput(0).getCount() == 60,
                "Underpayment abort lost source items or retained unpaid destination items");
            table.insertInput(0, new ItemStack(Items.DIAMOND, 2), false);
            table.insertInput(1, new ItemStack(Items.DIAMOND, 61), false);
            for (boolean commit : new boolean[]{false, true}) {
                try (var transaction = Transaction.openOuter()) {
                    var remainder = table.acceptRoutingInputTransactional(offered, transaction);
                    require(remainder.getCount() == 2 && table.getInput(0).getCount() == 64
                        && table.getInput(1).getCount() == 64 && table.getInput(2).isEmpty(),
                        "Multi-input top-up changed capacity, remainder or empty-slot policy");
                    remainder.setCount(1);
                    require(offered.getCount() == 7, "Multi-input remainder aliased source");
                    if (commit) transaction.commit();
                }
                require(table.getInput(0).getCount() == (commit ? 64 : 62)
                    && table.getInput(1).getCount() == (commit ? 64 : 61),
                    "Multi-input transaction did not restore/commit every changed slot");
            }
        } finally {
            if (level.getBlockEntity(pos) instanceof GemCuttersTableBlockEntity table) {
                table.extractInput(0, 64, false);
                table.extractInput(1, 64, false);
            }
            level.removeBlock(pos, false);
            level.removeBlock(other, false);
            level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(2)).forEach(net.minecraft.world.entity.Entity::discard);
        }
    }
    private static void mixedRouting(net.minecraft.gametest.framework.GameTestHelper helper,
            GemCuttersTableBlockEntity table, ItemStack offer) {
        var level = helper.getLevel();
        var pos = table.getBlockPos().above();
        require(level.isEmptyBlock(pos), "Mixed Gem Cutter fixture occupied");
        var player = RuntimeTestVersion.player(helper);
        table.recordPlacer(player);
        try {
            level.setBlockAndUpdate(pos, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
            var chest = (com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity) level.getBlockEntity(pos);
            chest.setOwner(player.getUUID());
            int capacity = chest.inventory().getStackLimit(0, offer);
            for (int slot = 0; slot < chest.inventory().getSlots(); slot++)
                chest.inventory().setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, capacity));
            chest.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, capacity - 2));
            var cache = new com.aranaira.arcanearchives.data.BrazierRouteCache();
            try (var probe = Transaction.openOuter()) {
                require(cache.insertTransactional(level, pos, player.getUUID(), true, 40,
                    new ItemStack(Items.DIAMOND, 2), probe).isEmpty()
                    && table.getInput(0).getCount() == 62 && chest.inventory().getStackInSlot(0).getCount() == capacity - 2,
                    "Transactional Gem Cutter priority was bypassed");
            }
            for (boolean commit : new boolean[]{false, true}) {
                try (var transaction = Transaction.openOuter()) {
                    require(cache.insertTransactional(level, pos, player.getUUID(), true, 40, offer, transaction).getCount() == 1
                        && table.getInput(0).getCount() == 64 && chest.inventory().getStackInSlot(0).getCount() == capacity,
                        "Gem Cutter/Chest overflow lost exact remainder or destination payment");
                    if (commit) transaction.commit();
                }
                require(table.getInput(0).getCount() == (commit ? 64 : 60)
                    && chest.inventory().getStackInSlot(0).getCount() == (commit ? capacity : capacity - 2)
                    && table.getInput(1).isEmpty() && offer.getCount() == 7,
                    "Mixed Gem Cutter transaction failed shared commit/abort");
            }
            table.extractInput(0, 4, false);
        } finally {
            if (level.getBlockEntity(pos) instanceof com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity chest)
                for (int slot = 0; slot < chest.inventory().getSlots(); slot++) chest.inventory().setStackInSlot(slot, ItemStack.EMPTY);
            level.removeBlock(pos, false);
        }
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
