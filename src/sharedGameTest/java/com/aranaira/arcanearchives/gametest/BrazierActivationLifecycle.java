package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.BrazierBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Native callbacks through version adapters; not incoming packets or real client input. */
public final class BrazierActivationLifecycle {
    public static void run(GameTestHelper helper, Player player,
            java.util.function.BiFunction<BlockHitResult, InteractionHand, Boolean> held,
            java.util.function.Function<BlockHitResult, Boolean> empty) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        var target = pos.above(2);
        require(level.isEmptyBlock(pos) && level.isEmptyBlock(target), "Brazier activation fixture occupied");
        var inventory = player.getInventory();
        var before = inventory.items.stream().map(ItemStack::copy).toList();
        var offhand = player.getOffhandItem().copy();
        var pending = com.aranaira.arcanearchives.data.PlayerSaveData.get(level.getServer(), player.getUUID());
        var returns = pending.brazierPendingReturns();
        var previousPosition = player.position();
        try {
            pending.setBrazierPendingReturns(null);
            for (int slot = 0; slot < inventory.items.size(); slot++) inventory.items.set(slot, ItemStack.EMPTY);
            level.setBlockAndUpdate(pos, ContentRegistry.BRAZIER.get().defaultBlockState());
            var brazier = (BrazierBlockEntity) level.getBlockEntity(pos);
            brazier.recordPlacer(player);
            level.setBlockAndUpdate(target, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
            var chest = (RadiantChestBlockEntity) level.getBlockEntity(target);
            chest.setOwner(player.getUUID());
            var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.DIAMOND, 5));
            require(empty.apply(hit) && chest.inventory().getStackInSlot(0).isEmpty(), "Empty callback did not handle no-op or used offhand");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND, 7));
            require(held.apply(hit, InteractionHand.OFF_HAND) && player.getMainHandItem().getCount() == 7
                && player.getOffhandItem().getCount() == 5 && chest.inventory().getStackInSlot(0).isEmpty(),
                "Offhand callback deposited a player source");
            require(held.apply(hit, InteractionHand.MAIN_HAND) && player.getMainHandItem().isEmpty()
                && player.getOffhandItem().getCount() == 5 && chest.inventory().getStackInSlot(0).getCount() == 7,
                "Native main-hand callback failed exact paid deposit");
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                player.setPos(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ContentRegistry.SCEPTER_MANIPULATION.get()));
                boolean shift = player.isShiftKeyDown();
                boolean opened;
                player.setShiftKeyDown(true);
                try { opened = held.apply(hit, InteractionHand.MAIN_HAND); }
                finally { player.setShiftKeyDown(shift); }
                require(opened
                    && player.containerMenu instanceof com.aranaira.arcanearchives.inventory.BrazierMenu,
                    "Native manipulation interaction did not open Brazier configuration");
                require(player.getMainHandItem().is(ContentRegistry.SCEPTER_MANIPULATION.get())
                    && player.getMainHandItem().getCount() == 1 && player.getOffhandItem().getCount() == 5
                    && chest.inventory().getStackInSlot(0).getCount() == 7,
                    "Opening configuration paid or routed player items");
                var menu = (com.aranaira.arcanearchives.inventory.BrazierMenu) player.containerMenu;
                var replica = new com.aranaira.arcanearchives.inventory.BrazierMenu(menu.containerId, player.getInventory());
                require(replica.position() == null, "Uninitialized menu exposed a default block position");
                menu.setSynchronizer(new net.minecraft.world.inventory.ContainerSynchronizer() {
                    @Override public void sendInitialData(net.minecraft.world.inventory.AbstractContainerMenu source,
                            net.minecraft.core.NonNullList<ItemStack> items, ItemStack carried, int[] data) {
                        for (int slot = 0; slot < data.length; slot++) sendDataChange(source, slot, data[slot]);
                    }
                    @Override public void sendSlotChange(net.minecraft.world.inventory.AbstractContainerMenu source, int slot, ItemStack stack) {}
                    @Override public void sendCarriedChange(net.minecraft.world.inventory.AbstractContainerMenu source, ItemStack stack) {}
                    @Override public void sendDataChange(net.minecraft.world.inventory.AbstractContainerMenu source, int slot, int value) {
                        replica.setData(slot, (short) value);
                    }
                });
                require(pos.equals(replica.position()) && replica.radius() == menu.radius(),
                    "Native menu data failed position/settings synchronization across signed-short boundary");
                int initialRadius = brazier.radius();
                new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(menu.containerId, 0)
                    .handle(serverPlayer.connection);
                require(brazier.radius() == initialRadius - 10 && menu.radius() == brazier.radius(),
                    "Native button packet did not update opened configuration");
                require(replica.radius() == menu.radius() && pos.equals(replica.position()),
                    "Radius synchronization changed display location or failed replica update");
                new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(menu.containerId + 1, 0)
                    .handle(serverPlayer.connection);
                require(brazier.radius() == initialRadius - 10, "Wrong-container native packet modified configuration");
                new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(menu.containerId, 2)
                    .handle(serverPlayer.connection);
                require(brazier.personalOnly() && menu.personalOnly(), "Native network button packet failed");
                var exact = new com.aranaira.arcanearchives.events.BrazierRadius(menu.containerId, 37);
                exact.apply(player);
                require(brazier.radius() == 37 && menu.radius() == 37, "Exact-radius request failed opened native session");
                serverPlayer.closeContainer();
                new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(menu.containerId, 0)
                    .handle(serverPlayer.connection);
                new com.aranaira.arcanearchives.events.BrazierRadius(menu.containerId, 90).apply(player);
                require(brazier.radius() == 37, "Closed native configuration accepted further requests");
            }
        } finally {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                    && player.containerMenu instanceof com.aranaira.arcanearchives.inventory.BrazierMenu) serverPlayer.closeContainer();
            player.setPos(previousPosition);
            if (level.getBlockEntity(target) instanceof RadiantChestBlockEntity chest)
                for (int slot = 0; slot < chest.inventory().getSlots(); slot++) chest.inventory().setStackInSlot(slot, ItemStack.EMPTY);
            level.removeBlock(target, false);
            level.removeBlock(pos, false);
            pending.setBrazierPendingReturns(returns);
            for (int slot = 0; slot < before.size(); slot++) inventory.items.set(slot, before.get(slot));
            player.setItemInHand(InteractionHand.OFF_HAND, offhand);
        }
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
