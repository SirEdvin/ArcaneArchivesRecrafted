package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.blocks.GemCuttersTable;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.GemCuttersTableBlockEntity;
import com.aranaira.arcanearchives.tileentities.NetworkOwnedBlockEntity;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Native placement and serialization; ownership is deliberately not local-menu authorization. */
public final class DeviceOwnershipLifecycle {
    public static void run(GameTestHelper helper, Player player, Player fake,
            Function<BlockEntity, CompoundTag> save, BiConsumer<BlockEntity, CompoundTag> load,
            BiConsumer<ItemStack, CompoundTag> itemData, BiConsumer<GameTestHelper, Player> lectern,
            Function<GameTestHelper, net.minecraft.server.level.ServerPlayer> serverPlayer) {
        BrazierPlayerSelectionLifecycle.run(player, fake, itemData);
        StoragePreferencesLifecycle.run(helper, serverPlayer.apply(helper), itemData);
        Release003Lifecycle.run(helper, serverPlayer.apply(helper), save, load);
        BrazierPullLifecycle.run(helper, serverPlayer.apply(helper), save, load);
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        player.setPos(pos.getX() + 4, pos.getY(), pos.getZ() + 4);
        for (var item : new BlockItem[]{ContentRegistry.GEMCUTTERS_TABLE_ITEM.get(), ContentRegistry.MONITORING_CRYSTAL_ITEM.get(), ContentRegistry.BRAZIER_ITEM.get()}) {
            for (Player actor : new Player[]{player, fake, null})
            for (int removal = 0; removal < (item == ContentRegistry.GEMCUTTERS_TABLE_ITEM.get() ? 4 : 2); removal++) {
                boolean creative = actor != null && actor.getAbilities().instabuild;
                if (actor != null) {
                    actor.setYRot(0);
                    actor.setPos(pos.getX() + 4, pos.getY(), pos.getZ() + 4);
                    actor.getAbilities().instabuild = false;
                }
                BlockPos other = pos;
                try {
                    require(level.isEmptyBlock(pos), "Ownership fixture occupied");
                    var stack = new ItemStack(item, 2);
                    var injected = new CompoundTag();
                    injected.putUUID("network_owner", new UUID(0, 1));
                    injected.putString("id", item == ContentRegistry.GEMCUTTERS_TABLE_ITEM.get()
                        ? "arcanearchives:gemcutters_table" : item == ContentRegistry.BRAZIER_ITEM.get()
                            ? "arcanearchives:brazier_of_hoarding" : "arcanearchives:monitoring_crystal");
                    itemData.accept(stack, injected);
                    var expectedStack = stack.copy();
                    expectedStack.setCount(1);
                    var context = new BlockPlaceContext(level, actor, InteractionHand.MAIN_HAND, stack,
                        new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
                    require(item.place(context).consumesAction() && ItemStack.matches(stack, expectedStack),
                        "Ownership placement failed or changed item payment/data");
                    var entity = (NetworkOwnedBlockEntity) level.getBlockEntity(pos);
                    UUID expected = actor == player ? player.getUUID() : null;
                    require(java.util.Objects.equals(expected, entity.networkOwner()), "Wrong native placer ownership");
                    require(com.aranaira.arcanearchives.data.StorageNetworks.visible(level.getServer(), player.getUUID(), true)
                        .contains(entity) == (actor == player), "Device discovery ignored native placement ownership");
                    if (entity instanceof GemCuttersTableBlockEntity table) {
                        other = GemCuttersTable.connectedPos(pos, level.getBlockState(pos));
                        require(level.getBlockEntity(other) == null, "Accessor acquired another network entity");
                        require(table.insertInput(17, new ItemStack(Items.DIAMOND, 7), false).isEmpty(), "Could not seed input");
                        ItemStack offered = new ItemStack(Items.DIAMOND, 64);
                        require(table.acceptRoutingInput(offered, true).getCount() == 7
                            && table.getInput(17).getCount() == 7 && offered.getCount() == 64,
                            "Routed simulation mutated input or used empty slots");
                        require(table.acceptRoutingInput(new ItemStack(Items.EMERALD, 4), false).getCount() == 4
                            && table.getInput(0).isEmpty(), "Routing filled an empty Gem Cutter slot");
                        require(table.acceptRoutingInput(offered, false).getCount() == 7
                            && table.getInput(17).getCount() == 64 && offered.getCount() == 64,
                            "Routed top-up lost its exact remainder or mutated the offered stack");
                        require(table.extractInput(17, 57, false).getCount() == 57, "Could not restore routing fixture input");
                        require(table.routingWeight(offered) == 5000
                            && table.routingWeight(new ItemStack(Items.EMERALD)) == -1,
                            "Gem Cutter routing rank did not require a matching occupied input");
                        int routedRemainder = actor == player ? 7 : 64;
                        require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, pos, player.getUUID(), true, 0, offered, true)
                            .getCount() == routedRemainder && table.getInput(17).getCount() == 7,
                            "Gem Cutter route simulation mutated input or ignored placement ownership");
                        require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, pos, player.getUUID(), true, 0, offered, false)
                            .getCount() == routedRemainder && table.getInput(17).getCount() == (actor == player ? 64 : 7)
                            && table.getInput(0).isEmpty() && offered.getCount() == 64,
                            "Discovered Gem Cutter route lost top-up policy, remainder or placement ownership");
                        if (actor == player)
                            require(table.extractInput(17, 57, false).getCount() == 57, "Could not restore discovered-route input");
                        require(table.stillValid(player), "Ownership changed local table access");
                        if (actor == player) {
                            BlockPos chestPos = pos.above(2);
                            require(level.isEmptyBlock(chestPos), "Mixed Gem Cutter routing fixture occupied");
                            try {
                                level.setBlockAndUpdate(chestPos, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
                                var chest = (com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity) level.getBlockEntity(chestPos);
                                chest.setOwner(player.getUUID());
                                chest.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 7));
                                require(chest.toggleRoutingType(), "Could not enable NO_NEW_STACKS competitor");
                                var single = new ItemStack(Items.DIAMOND);
                                var routes = com.aranaira.arcanearchives.data.BrazierRoutes.collect(level, pos, player.getUUID(), true, 0, single);
                                require(routes.contains(chest) && routes.indexOf(table) >= 0 && routes.indexOf(table) < routes.indexOf(chest),
                                    "Mixed routing competitors were not both eligible in Gem Cutter-first order");
                                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, pos, player.getUUID(), true, 0, single, true).isEmpty()
                                    && table.getInput(17).getCount() == 7 && chest.inventory().getStackInSlot(0).getCount() == 7,
                                    "Mixed Gem Cutter simulation mutated inventories");
                                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, pos, player.getUUID(), true, 0, single, false).isEmpty()
                                    && table.getInput(17).getCount() == 8 && chest.inventory().getStackInSlot(0).getCount() == 7
                                    && single.getCount() == 1,
                                    "NO_NEW_STACKS Chest outranked matching Gem Cutter or offer was mutated");
                                require(table.extractInput(17, 1, false).getCount() == 1, "Could not restore mixed routing input");
                                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, pos, player.getUUID(), true, 0, offered, true).isEmpty()
                                    && table.getInput(17).getCount() == 7 && chest.inventory().getStackInSlot(0).getCount() == 7,
                                    "Mixed overflow simulation mutated inventories or failed to chain capacity");
                                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, pos, player.getUUID(), true, 0, offered, false).isEmpty()
                                    && table.getInput(17).getCount() == 64 && chest.inventory().getStackInSlot(0).getCount() == 14
                                    && table.getInput(0).isEmpty() && offered.getCount() == 64,
                                    "Gem Cutter overflow failed exact Chest remainder conservation");
                                require(table.extractInput(17, 57, false).getCount() == 57, "Could not restore overflow routing input");
                                for (int slot = 0; slot < chest.inventory().getSlots(); slot++)
                                    chest.inventory().setStackInSlot(slot, new ItemStack(Items.DIAMOND, chest.inventory().getSlotLimit(slot)));
                                int chestLimit = chest.inventory().getSlotLimit(0);
                                chest.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, chestLimit - 1));
                                int expectedRemainder = offered.getCount() - (offered.getMaxStackSize() - 7) - 1;
                                var simulated = com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, pos, player.getUUID(), true, 0, offered, true);
                                require(simulated.is(Items.DIAMOND) && simulated.getCount() == expectedRemainder
                                    && table.getInput(17).getCount() == 7 && chest.inventory().getStackInSlot(0).getCount() == chestLimit - 1,
                                    "Capacity-limited mixed simulation lost remainder or mutated stock");
                                var remainder = com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, pos, player.getUUID(), true, 0, offered, false);
                                require(ItemStack.matches(simulated, remainder) && offered.getCount() == 64
                                    && table.getInput(17).getCount() == 64 && chest.inventory().getStackInSlot(0).getCount() == chestLimit,
                                    "Capacity-limited mixed transfer disagreed with exact simulation remainder");
                                simulated.setCount(1);
                                require(remainder.getCount() == expectedRemainder && offered.getCount() == 64,
                                    "Simulated remainder aliased a later result or the offered stack");
                                remainder.setCount(1);
                                require(offered.getCount() == 64 && table.getInput(17).getCount() == 64
                                    && chest.inventory().getStackInSlot(0).getCount() == chestLimit,
                                    "Returned remainder aliased source or destination inventory");
                                require(table.extractInput(17, 57, false).getCount() == 57, "Could not restore partial routing input");
                                require(table.extractInput(17, 7, false).getCount() == 7, "Could not prepare data-sensitive routing input");
                                var distinct = new ItemStack(Items.DIAMOND, 7);
                                var distinctData = new CompoundTag();
                                distinctData.putString("id", "minecraft:chest");
                                distinctData.putString("routing_fixture", "different item data");
                                itemData.accept(distinct, distinctData);
                                require(table.insertInput(17, distinct, false).isEmpty(), "Could not seed data-sensitive input");
                                chest.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, chestLimit - 1));
                                for (boolean simulate : new boolean[]{true, false}) {
                                    var rejected = com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, pos, player.getUUID(), true, 0, offered, simulate);
                                    require(rejected.is(Items.DIAMOND) && rejected.getCount() == offered.getCount() - 1
                                        && ItemStack.matches(table.getInput(17), distinct) && table.getInput(0).isEmpty(),
                                        "High-ranked Gem Cutter merged differing item data or lost fallback remainder");
                                    require(chest.inventory().getStackInSlot(0).getCount() == chestLimit - (simulate ? 1 : 0),
                                        "Data-sensitive fallback mutated simulation or failed execution");
                                }
                                chest.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 7));
                                var simulatedPreference = new com.aranaira.arcanearchives.data.BrazierRouteCache(() -> 1000L);
                                require(simulatedPreference.insert(level, pos, player.getUUID(), true, 0, single, true).isEmpty()
                                    && chest.inventory().getStackInSlot(0).getCount() == 7 && ItemStack.matches(table.getInput(17), distinct),
                                    "Successful cache simulation mutated inventory");
                                table.extractInput(17, 7, false);
                                require(table.insertInput(17, new ItemStack(Items.DIAMOND, 7), false).isEmpty(), "Could not restore plain routing input");
                                require(simulatedPreference.insert(level, pos, player.getUUID(), true, 0, single, false).isEmpty()
                                    && chest.inventory().getStackInSlot(0).getCount() == 8 && table.getInput(17).getCount() == 7,
                                    "Successful simulation did not preserve upstream cache preference over fresh Gem Cutter priority");
                                var removedInputCache = new com.aranaira.arcanearchives.data.BrazierRouteCache(() -> 1000L);
                                require(removedInputCache.insert(level, pos, player.getUUID(), true, 0, single, false).isEmpty()
                                    && table.getInput(17).getCount() == 8, "Could not seed Gem Cutter cache preference");
                                require(table.extractInput(17, 8, false).getCount() == 8, "Could not remove cached matching input");
                                require(removedInputCache.insert(level, pos, player.getUUID(), true, 0, single, false).isEmpty()
                                    && table.getInput(17).isEmpty() && chest.inventory().getStackInSlot(0).getCount() == 9,
                                    "Cached Gem Cutter used an empty slot after its matching input was removed");
                                require(table.insertInput(17, new ItemStack(Items.DIAMOND, 7), false).isEmpty(), "Could not restore removed matching input");
                                require(removedInputCache.insert(level, pos, player.getUUID(), true, 0, single, false).isEmpty()
                                    && table.getInput(17).getCount() == 7 && chest.inventory().getStackInSlot(0).getCount() == 10,
                                    "Restored Gem Cutter input resurrected its evicted cache preference");
                                var dataCache = new com.aranaira.arcanearchives.data.BrazierRouteCache(() -> 1000L);
                                require(dataCache.insert(level, pos, player.getUUID(), true, 0, single, false).isEmpty()
                                    && table.getInput(17).getCount() == 8, "Could not seed data-sensitive Gem Cutter preference");
                                table.extractInput(17, 8, false);
                                require(table.insertInput(17, distinct, false).isEmpty(), "Could not replace cached input data");
                                require(dataCache.insert(level, pos, player.getUUID(), true, 0, single, false).isEmpty()
                                    && ItemStack.matches(table.getInput(17), distinct) && chest.inventory().getStackInSlot(0).getCount() == 11,
                                    "Cached differing-data input merged or prevented authorized fallback");
                                table.extractInput(17, 7, false);
                                require(table.insertInput(17, new ItemStack(Items.DIAMOND, 7), false).isEmpty(), "Could not restore compatible cached input");
                                require(dataCache.insert(level, pos, player.getUUID(), true, 0, single, false).isEmpty()
                                    && table.getInput(17).getCount() == 8 && chest.inventory().getStackInSlot(0).getCount() == 11,
                                    "Data mismatch discarded an otherwise eligible cached Gem Cutter preference");
                                table.extractInput(17, 1, false);
                                var batchInputs = java.util.List.of(new ItemStack(Items.DIAMOND, 3),
                                    new ItemStack(Items.DIAMOND, 64), new ItemStack(Items.DIAMOND, 2));
                                var batchCache = new com.aranaira.arcanearchives.data.BrazierRouteCache(() -> 1000L);
                                require(batchCache.insertBatch(level, pos, player.getUUID(), true, 0, single, batchInputs).isEmpty()
                                    && table.getInput(17).getCount() == 64
                                    && chest.inventory().getStackInSlot(0).getCount() == 11 + 3 + 64 + 2 - (64 - 7),
                                    "Multi-destination batch failed Gem Cutter-first overflow conservation");
                                require(batchInputs.get(0).getCount() == 3 && batchInputs.get(1).getCount() == 64
                                    && batchInputs.get(2).getCount() == 2 && table.getInput(0).isEmpty(),
                                    "Multi-destination batch mutated offers or filled an empty Gem Cutter input");
                                table.extractInput(17, 1, false);
                                require(batchCache.insert(level, pos, player.getUUID(), true, 0, single, false).isEmpty()
                                    && table.getInput(17).getCount() == 64,
                                    "Batch fallback replaced the preference recorded by its first accepted stack");

                                long[] batchClock = {1000L};
                                var partialBatchCache = new com.aranaira.arcanearchives.data.BrazierRouteCache(() -> batchClock[0]);
                                chest.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, chestLimit - 3));
                                require(partialBatchCache.insert(level, pos, player.getUUID(), true, 0, single, true).isEmpty()
                                    && chest.inventory().getStackInSlot(0).getCount() == chestLimit - 3,
                                    "Could not seed lower-ranked batch preference without paying inventory");
                                table.extractInput(17, 57, false);
                                batchClock[0] = 1900L;
                                var partialInputs = java.util.List.of(new ItemStack(Items.DIAMOND, 2),
                                    new ItemStack(Items.DIAMOND, 64), new ItemStack(Items.DIAMOND, 2));
                                var batchRemainder = partialBatchCache.insertBatch(level, pos, player.getUUID(), true, 0, single, partialInputs);
                                require(batchRemainder.size() == 2 && batchRemainder.get(0).getCount() == 64 - (3 - 2) - (64 - 7)
                                    && batchRemainder.get(1).getCount() == 2 && table.getInput(17).getCount() == 64
                                    && chest.inventory().getStackInSlot(0).getCount() == chestLimit,
                                    "Preferred-first partial batch lost exact remainder order across destinations");
                                require(partialInputs.get(0).getCount() == 2 && partialInputs.get(1).getCount() == 64
                                    && partialInputs.get(2).getCount() == 2, "Partial multi-destination batch mutated offers");
                                batchClock[0] = 2100L;
                                table.extractInput(17, 57, false);
                                chest.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, chestLimit - 1));
                                require(partialBatchCache.insert(level, pos, player.getUUID(), true, 0, single, false).isEmpty()
                                    && table.getInput(17).getCount() == 7
                                    && chest.inventory().getStackInSlot(0).getCount() == chestLimit,
                                    "Accepted stack in incomplete batch failed to refresh preferred route past its original expiry");
                                for (int slot = 0; slot < chest.inventory().getSlots(); slot++)
                                    chest.inventory().setStackInSlot(slot, ItemStack.EMPTY);
                                level.setBlockAndUpdate(chestPos, ContentRegistry.RADIANT_TROVE.get().defaultBlockState());
                                var trove = (com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity) level.getBlockEntity(chestPos);
                                trove.setOwner(player.getUUID());
                                trove.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 7));
                                var troveRoutes = com.aranaira.arcanearchives.data.BrazierRoutes.collect(level, pos, player.getUUID(), true, 0, offered);
                                require(troveRoutes.contains(trove) && troveRoutes.indexOf(table) >= 0 && troveRoutes.indexOf(table) < troveRoutes.indexOf(trove),
                                    "Matching Trove outranked Gem Cutter or was not eligible");
                                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, pos, player.getUUID(), true, 0, offered, true).isEmpty()
                                    && table.getInput(17).getCount() == 7 && trove.inventory().getStackInSlot(0).getCount() == 7,
                                    "Gem Cutter/Trove simulation mutated inventories");
                                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, pos, player.getUUID(), true, 0, offered, false).isEmpty()
                                    && table.getInput(17).getCount() == 64 && trove.inventory().getStackInSlot(0).getCount() == 14
                                    && offered.getCount() == 64 && table.getInput(0).isEmpty(),
                                    "Gem Cutter/Trove overflow changed priority or lost items");
                                require(table.extractInput(17, 57, false).getCount() == 57, "Could not restore Trove overflow input");
                                var partialCache = new com.aranaira.arcanearchives.data.BrazierRouteCache(() -> 1000L);
                                require(table.insertInput(17, new ItemStack(Items.DIAMOND, 57), false).isEmpty(), "Could not fill cached-route test input");
                                int troveLimit = trove.inventory().getStackLimit(0, offered);
                                trove.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, troveLimit - 1));
                                var partial = partialCache.insert(level, pos, player.getUUID(), true, 0, offered, false);
                                require(partial.is(Items.DIAMOND) && partial.getCount() == offered.getCount() - 1
                                    && table.getInput(17).getCount() == 64 && trove.inventory().getStackInSlot(0).getCount() == troveLimit,
                                    "Could not establish partially accepted uncached transfer");
                                table.extractInput(17, 57, false);
                                trove.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 7));
                                require(partialCache.insert(level, pos, player.getUUID(), true, 0, single, false).isEmpty()
                                    && table.getInput(17).getCount() == 8 && trove.inventory().getStackInSlot(0).getCount() == 7,
                                    "Partial acceptance incorrectly seeded a lower-priority cache preference");
                                table.extractInput(17, 1, false);
                                long[] cacheTime = {0L};
                                var partialRefresh = new com.aranaira.arcanearchives.data.BrazierRouteCache(() -> cacheTime[0]);
                                require(table.insertInput(17, new ItemStack(Items.DIAMOND, 57), false).isEmpty(), "Could not fill expiry fixture input");
                                require(partialRefresh.insert(level, pos, player.getUUID(), true, 0, single, false).isEmpty()
                                    && trove.inventory().getStackInSlot(0).getCount() == 8, "Could not seed Trove expiry preference");
                                cacheTime[0] = 900L;
                                trove.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, troveLimit - 1));
                                require(partialRefresh.insert(level, pos, player.getUUID(), true, 0, offered, false).getCount() == offered.getCount() - 1,
                                    "Expiry fixture did not produce partial acceptance");
                                cacheTime[0] = 1000L;
                                table.extractInput(17, 57, false);
                                trove.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 7));
                                require(partialRefresh.insert(level, pos, player.getUUID(), true, 0, single, false).isEmpty()
                                    && table.getInput(17).getCount() == 8 && trove.inventory().getStackInSlot(0).getCount() == 7,
                                    "Partial acceptance incorrectly refreshed the expiring Trove preference");
                                table.extractInput(17, 1, false);
                                trove.inventory().setStackInSlot(0, ItemStack.EMPTY);
                            } finally {
                                level.removeBlock(chestPos, false);
                            }
                        }
                    }
                    var saved = save.apply(entity);
                    var restored = (NetworkOwnedBlockEntity) entity.getType().create(pos, entity.getBlockState());
                    restored.setLevel(level);
                    load.accept(restored, saved);
                    if (restored instanceof GemCuttersTableBlockEntity table)
                        require(table.acceptRoutingInput(new ItemStack(Items.DIAMOND, 4), false).getCount() == 4
                            && table.getInput(17).getCount() == 7, "Detached Gem Cutter accepted routed items");
                    require(java.util.Objects.equals(expected, restored.networkOwner()), "Owner failed native serialization");
                    if (restored instanceof GemCuttersTableBlockEntity table)
                        require(table.getInput(17).is(Items.DIAMOND) && table.getInput(17).getCount() == 7,
                            "Ownership serialization lost inputs");
                    var ownerless = saved.copy();
                    ownerless.remove("network_owner");
                    load.accept(restored, ownerless);
                    require(restored.networkOwner() == null && !save.apply(restored).contains("network_owner"),
                        "Old ownerless data acquired an owner");
                    ownerless.putString("network_owner", "malformed");
                    load.accept(restored, ownerless);
                    require(restored.networkOwner() == null, "Malformed identity granted ownership");
                    boolean replacement = removal == 1 || removal == 2;
                    BlockPos removed = removal == 1 || removal == 3 ? pos : other;
                    if (replacement) level.setBlockAndUpdate(removed, Blocks.STONE.defaultBlockState());
                    else level.destroyBlock(removed, true);
                    for (BlockPos part : new BlockPos[]{pos, other}) {
                        require(replacement && part.equals(removed) ? level.getBlockState(part).is(Blocks.STONE)
                            : level.isEmptyBlock(part), "Removal lost foreign replacement or left a device part");
                        require(level.getBlockEntity(part) == null, "Removed device retained a live entity");
                    }
                    require(entity.isRemoved(), "Removed ownership entity remained live");
                    if (entity instanceof GemCuttersTableBlockEntity table)
                        require(!table.stillValid(player), "Removed table still authorized local access");
                    var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3));
                    // The Gem Cutter emits its conserved device in onRemove; Crystal/Brazier use native loot only.
                    int expectedDevices = !replacement || entity instanceof GemCuttersTableBlockEntity ? 1 : 0;
                    require(drops.stream().filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum() == expectedDevices,
                        "Device removal did not conserve its item");
                    int diamonds = drops.stream().filter(e -> e.getItem().is(Items.DIAMOND)).mapToInt(e -> e.getItem().getCount()).sum();
                    require(diamonds == (entity instanceof GemCuttersTableBlockEntity ? 7 : 0), "Device removal lost/duplicated contents");
                } finally {
                    if (actor != null) actor.getAbilities().instabuild = creative;
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    if (!other.equals(pos)) level.setBlockAndUpdate(other, Blocks.AIR.defaultBlockState());
                    level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3)).forEach(Entity::discard);
                }
            }
        }
        outputReplacement(helper, player, fake, save, load);
        GemCutterPlacementCollision.run(helper, player);
        StorageNetworkLifecycle.run(helper, player, save, load);
        TroveHudSync.run(helper, load);
        ManifestInventoryLifecycle.run(helper, player);
        ManifestAcquisitionLifecycle.run(helper, player);
        lectern.accept(helper, player);
        ManifestServerSession.run(helper, serverPlayer.apply(helper), player);
        TomeBookshelfLifecycle.run(helper, serverPlayer);
        TomeConditionsLifecycle.run();
    }

    /** Seed ordinary inventory contents; native menu acquisition is covered separately. */
    private static void outputReplacement(GameTestHelper helper, Player player, Player fake,
            Function<BlockEntity, CompoundTag> save, BiConsumer<BlockEntity, CompoundTag> load) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        var item = ContentRegistry.GEMCUTTERS_TABLE_ITEM.get();
        for (Player actor : new Player[]{player, fake, null})
        for (int removal = 0; removal < 4; removal++) {
            boolean creative = actor != null && actor.getAbilities().instabuild;
            BlockPos other = pos;
            try {
                if (actor != null) {
                    actor.getAbilities().instabuild = false;
                    actor.setYRot(0);
                    actor.setPos(pos.getX() + 4, pos.getY(), pos.getZ() + 4);
                }
                require(level.isEmptyBlock(pos), "Output fixture occupied");
                var offered = new ItemStack(item);
                require(item.place(new BlockPlaceContext(level, actor, InteractionHand.MAIN_HAND, offered,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false))).consumesAction()
                    && offered.isEmpty(), "Could not place output fixture");
                other = GemCuttersTable.connectedPos(pos, level.getBlockState(pos));
                var table = (GemCuttersTableBlockEntity) level.getBlockEntity(pos);
                var staged = new com.aranaira.arcanearchives.recipe.gct.GemCutterCraftingState();
                staged.setInput(0, new ItemStack(Items.DIAMOND));
                staged.setInput(17, new ItemStack(Items.EMERALD, 7));
                UUID originalOwner = new UUID(0, 2);
                var completed = com.aranaira.arcanearchives.recipe.CraftingCreator.withCreator(
                    new ItemStack(Items.PAPER, 4), originalOwner, "Original crafter");
                staged.setOutput(completed);
                var expectedCrafting = staged.serializeNBT(level.registryAccess());
                var saved = save.apply(table);
                saved.put("Crafting", expectedCrafting.copy());
                saved.putUUID("network_owner", originalOwner);
                load.accept(table, saved);

                BlockPos removed = removal < 2 ? pos : other;
                if (removal % 2 == 0) level.destroyBlock(removed, true);
                else level.setBlockAndUpdate(removed, Blocks.STONE.defaultBlockState());
                var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3));
                require(drops.stream().filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum() == 1,
                    "Removal did not drop exactly one empty table");
                require(drops.stream().filter(e -> e.getItem().is(Items.DIAMOND)).mapToInt(e -> e.getItem().getCount()).sum() == 1,
                    "Removal lost remaining inputs or refunded consumed ingredients");
                require(drops.stream().filter(e -> e.getItem().is(Items.EMERALD)).mapToInt(e -> e.getItem().getCount()).sum() == 7,
                    "Removal lost unrelated inputs");
                var outputs = drops.stream().filter(e -> e.getItem().is(Items.PAPER)).toList();
                require(outputs.size() == 1 && ItemStack.matches(completed, outputs.get(0).getItem()),
                    "Removal lost/duplicated completed output or changed its creator");
                require(drops.size() == 4, "Removal produced unexpected refunds");
                var carrier = drops.stream().filter(e -> e.getItem().is(item)).findFirst().orElseThrow().getItem().copy();
                require(ItemStack.matches(carrier, new ItemStack(item)), "Table retained a special data carrier");
                drops.forEach(Entity::discard);
                require(table.isRemoved() && !table.stillValid(player), "Removed output table remained usable");
                if (removal % 2 != 0) {
                    require(level.getBlockState(removed).is(Blocks.STONE), "Output cleanup deleted replacement");
                    level.setBlockAndUpdate(removed, Blocks.AIR.defaultBlockState());
                }
                require(level.isEmptyBlock(pos) && level.isEmptyBlock(other), "Output removal left a part");
                require(item.place(new BlockPlaceContext(level, actor, InteractionHand.MAIN_HAND, carrier,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false))).consumesAction()
                    && carrier.isEmpty(), "Output carrier failed conserved placement");
                var restored = (GemCuttersTableBlockEntity) level.getBlockEntity(pos);
                require(java.util.Objects.equals(actor == player ? player.getUUID() : null, restored.networkOwner()),
                    "Output carrier retained its former owner");
                require(restored.getOutput().isEmpty(), "Re-placed table duplicated its previously dropped output");
                for (int slot = 0; slot < 18; slot++) require(restored.getInput(slot).isEmpty(), "Re-placed table duplicated inputs");
                require(level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3)).isEmpty(),
                    "Output placement emitted free items");
            } finally {
                if (actor != null) actor.getAbilities().instabuild = creative;
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                if (!other.equals(pos)) level.setBlockAndUpdate(other, Blocks.AIR.defaultBlockState());
                level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3)).forEach(Entity::discard);
            }
        }
    }
    private static void require(boolean ok, String message) {
        if (!ok) throw new IllegalStateException(message);
    }
}
