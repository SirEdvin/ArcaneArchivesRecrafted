package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.data.HiveSaveData;
import com.aranaira.arcanearchives.data.ManifestTracking;
import com.aranaira.arcanearchives.events.ManifestSelect;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.ManifestMenu;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/** Real server-owned selections and codec/application; not connected client input or marker rendering. */
public final class ManifestTrackingLifecycle {
    public static void run(GameTestHelper helper, ServerPlayer player, net.minecraft.world.entity.player.Player colleague) {
        var level = helper.getLevel();
        var own = helper.absolutePos(new BlockPos(1, 2, 1));
        var shared = helper.absolutePos(new BlockPos(2, 2, 1));
        require(level.isEmptyBlock(own) && level.isEmptyBlock(shared), "Tracking lifecycle fixture occupied");

        UUID owner = colleague.getUUID();
        var hives = HiveSaveData.get(player.server);
        try {
            require(hives.acceptInvitation(owner, player.getUUID()), "Tracking lifecycle could not join Hive");
            for (var pos : new BlockPos[]{own, shared}) {
                level.setBlockAndUpdate(pos, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
                var chest = (RadiantChestBlockEntity) level.getBlockEntity(pos);
                chest.setOwner(pos.equals(own) ? player.getUUID() : owner);
                chest.inventory().setStackInSlot(0, new ItemStack(Items.ENDER_EYE, 3));
            }
            var menu = open(player);
            hoverShortcut(player, menu, own, shared);
            menu = open(player);
            var add = command(menu, 0);
            require(!new ManifestSelect(menu.containerId, menu.snapshotRevision() + 1, add.index(), 0).apply(player),
                "Future tracking command accepted");
            require(roundTrip(add).apply(player), "Decoded tracking command did not add the authorized group");
            var original = ManifestTracking.markers(player);
            require(original.size() == 2, "Tracking did not retain distinct personal and Hive sources");
            original.get(0).stack().setCount(64);
            require(ManifestTracking.markers(player).get(0).stack().getCount() == 1, "Tracking exposed its mutable reference item");
            require(!add.apply(player), "Tracking command bypassed projection throttling");
            player.connection.handleContainerClose(new net.minecraft.network.protocol.game.ServerboundContainerClosePacket(menu.containerId));
            require(player.containerMenu == player.inventoryMenu,
                "Native client close packet did not close the selected Manifest");
            require(ManifestTracking.markers(player).size() == 2, "Menu close cleared authorized tracking");
            require(!roundTrip(add).apply(player), "Selection replay after native close packet was accepted");
            require(hives.resign(player.getUUID()), "Tracking lifecycle could not resign");
            ManifestTracking.tick(player);
            var retained = ManifestTracking.markers(player);
            require(retained.size() == 1 && retained.get(0).position().pos.equals(own),
                "Closed-screen revocation failed to remove only the foreign source");
            require(original.size() == 2, "Tracking rewrote a previously returned snapshot");
            trackingTransport(player, original, retained);
            require(hives.acceptInvitation(owner, player.getUUID()), "Tracking lifecycle could not rejoin");
            ManifestTracking.tick(player);
            require(ManifestTracking.markers(player).size() == 1, "Rejoining revived cleared selections");
            menu = open(player);
            require(command(menu, 0).apply(player) && ManifestTracking.markers(player).size() == 2,
                "Explicit reselection failed after rejoining");
            require(roundTrip(command(menu, 1)).apply(player) && ManifestTracking.markers(player).isEmpty(),
                "Right-click command did not remove its group");
            menu = open(player);
            require(command(menu, 0).apply(player), "Selection before clear failed");
            require(roundTrip(new ManifestSelect(menu.containerId, menu.snapshotRevision(), -1, 2)).apply(player)
                && ManifestTracking.markers(player).isEmpty(), "Clear command retained markers");
            require(!new ManifestSelect(menu.containerId, menu.snapshotRevision(), Integer.MAX_VALUE, 0).apply(player),
                "Out-of-bounds selection accepted");
            menu = open(player);
            require(command(menu, 0).apply(player), "Replacement fixture could not select sources");
            player.closeContainer();
            ((RadiantChestBlockEntity) level.getBlockEntity(own)).inventory().setStackInSlot(0, ItemStack.EMPTY);
            level.setBlockAndUpdate(own, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(own, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
            var replacement = (RadiantChestBlockEntity) level.getBlockEntity(own);
            replacement.setOwner(player.getUUID());
            ManifestTracking.tick(player);
            require(ManifestTracking.markers(player).size() == 2,
                "Authorized replacement was revoked solely because its block-entity instance changed");
            replacement.setOwner(UUID.randomUUID());
            ManifestTracking.tick(player);
            var replaced = ManifestTracking.markers(player);
            require(replaced.size() == 1 && replaced.get(0).position().pos.equals(shared),
                "Inaccessible Arcane replacement retained the old owner's tracking grant");
            replacement.setOwner(player.getUUID());
            ManifestTracking.tick(player);
            require(ManifestTracking.markers(player).size() == 1,
                "Restoring replacement ownership resurrected a revoked selection");
            menu = open(player);
            require(new ManifestSelect(menu.containerId, menu.snapshotRevision(), -1, 2).apply(player), "Replacement cleanup failed");
            var north = own.north();
            require(level.isEmptyBlock(north), "Duplicate-monitor tracking fixture occupied");
            for (var pos : new BlockPos[]{own, shared})
                ((RadiantChestBlockEntity) level.getBlockEntity(pos)).inventory().setStackInSlot(0, ItemStack.EMPTY);
            level.setBlockAndUpdate(own, Blocks.BARREL.defaultBlockState());
            ((net.minecraft.world.Container) level.getBlockEntity(own)).setItem(0, new ItemStack(Items.ENDER_EYE, 3));
            try {
                level.setBlockAndUpdate(shared, ContentRegistry.MONITORING_CRYSTAL.get().defaultBlockState()
                    .setValue(com.aranaira.arcanearchives.blocks.MonitoringCrystal.FACING, net.minecraft.core.Direction.EAST));
                ((com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity) level.getBlockEntity(shared)).recordPlacer(colleague);
                level.setBlockAndUpdate(north, ContentRegistry.MONITORING_CRYSTAL.get().defaultBlockState()
                    .setValue(com.aranaira.arcanearchives.blocks.MonitoringCrystal.FACING, net.minecraft.core.Direction.NORTH));
                ((com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity) level.getBlockEntity(north)).recordPlacer(player);
                menu = open(player);
                require(command(menu, 0).apply(player), "Duplicate-monitor selection failed");
                var monitored = ManifestTracking.markers(player);
                require(monitored.size() == 1 && monitored.get(0).owners().containsAll(java.util.Set.of(owner, player.getUUID())),
                    "Deduplicated monitor location lost an independent authorization grant");
                player.closeContainer();
                level.setBlockAndUpdate(shared, Blocks.AIR.defaultBlockState());
                ManifestTracking.tick(player);
                require(ManifestTracking.markers(player).size() == 1
                    && ManifestTracking.markers(player).get(0).owners().equals(java.util.Set.of(player.getUUID())),
                    "Removed monitor retained its grant or erased an independent personal grant");
                require(hives.resign(player.getUUID()), "Duplicate-monitor fixture could not revoke Hive access");
                ManifestTracking.tick(player);
                monitored = ManifestTracking.markers(player);
                require(monitored.size() == 1 && monitored.get(0).owners().equals(java.util.Set.of(player.getUUID())),
                    "Revocation removed a location still authorized by the viewer's own monitor");
                var crystal = (com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity) level.getBlockEntity(north);
                var originalState = crystal.getBlockState();
                require(crystal.target().equals(own), "Retarget fixture did not initially point at the barrel");
                var rotated = originalState.rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_90);
                level.setBlockAndUpdate(north, rotated);
                require(level.getBlockEntity(north) == crystal && crystal.target().equals(north.relative(
                    rotated.getValue(com.aranaira.arcanearchives.blocks.MonitoringCrystal.FACING).getOpposite())),
                    "Rotated Monitoring Crystal retained its cached original target");
                ManifestTracking.tick(player);
                require(ManifestTracking.markers(player).isEmpty(), "Retargeted last monitor retained its former source grant");
                level.setBlockAndUpdate(north, originalState);
                require(ManifestTracking.markers(player).isEmpty(), "Restoring monitor facing resurrected tracking");
                menu = open(player);
                require(command(menu, 0).apply(player), "Reselection after restoring monitor facing failed");
                player.closeContainer();
                level.setBlockAndUpdate(north, Blocks.AIR.defaultBlockState());
                ManifestTracking.tick(player);
                require(ManifestTracking.markers(player).isEmpty(), "Last monitor removal retained a tracking marker");
                level.setBlockAndUpdate(north, ContentRegistry.MONITORING_CRYSTAL.get().defaultBlockState()
                    .setValue(com.aranaira.arcanearchives.blocks.MonitoringCrystal.FACING, net.minecraft.core.Direction.NORTH));
                ((com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity) level.getBlockEntity(north)).recordPlacer(player);
                require(ManifestTracking.markers(player).isEmpty(), "Reinstalled monitor revived a cleared selection");
                menu = open(player);
                require(command(menu, 0).apply(player), "Reselection after monitor reinstallation failed");
                player.closeContainer();
                ((net.minecraft.world.Container) level.getBlockEntity(own)).clearContent();
                ManifestTracking.tick(player);
                require(ManifestTracking.markers(player).size() == 1,
                    "Empty inventory incorrectly revoked cached location tracking");
                level.setBlockAndUpdate(own, Blocks.AIR.defaultBlockState());
                ManifestTracking.tick(player);
                require(ManifestTracking.markers(player).isEmpty(),
                    "Confirmed loaded source removal retained a tracking marker");
                level.setBlockAndUpdate(own, Blocks.BARREL.defaultBlockState());
                ManifestTracking.tick(player);
                require(ManifestTracking.markers(player).isEmpty(),
                    "Recreated storage automatically revived a removed selection");
                menu = open(player);
                require(new ManifestSelect(menu.containerId, menu.snapshotRevision(), -1, 2).apply(player), "Monitor cleanup failed");
            } finally {
                level.setBlockAndUpdate(north, Blocks.AIR.defaultBlockState());
                if (level.getBlockEntity(own) instanceof net.minecraft.world.Container container) container.clearContent();
            }
        } finally {
            player.closeContainer();
            hives.resign(player.getUUID());
            hives.resign(owner);
            for (var pos : new BlockPos[]{own, shared}) {
                if (level.getBlockEntity(pos) instanceof RadiantChestBlockEntity chest)
                    chest.inventory().setStackInSlot(0, ItemStack.EMPTY);
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
    }


    private static void hoverShortcut(ServerPlayer player, ManifestMenu menu, BlockPos own, BlockPos shared) {
        var bytes = com.aranaira.arcanearchives.events.ManifestHover.encode(new ItemStack(Items.ENDER_EYE, 64), player.level().registryAccess());
        var request = new com.aranaira.arcanearchives.events.ManifestHover(menu.containerId, Integer.MAX_VALUE, false, bytes);
        bytes[0] ^= 1;
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            com.aranaira.arcanearchives.events.ManifestHover.write(buffer, request);
            var decoded = com.aranaira.arcanearchives.events.ManifestHover.read(buffer);
            require(!buffer.isReadable() && java.util.Arrays.equals(request.reference(), decoded.reference()), "Hover codec changed reference");
            var exposed = decoded.reference();
            exposed[0] ^= 1;
            require(java.util.Arrays.equals(request.reference(), decoded.reference()), "Hover request exposed mutable payload");
            require(!decoded.apply(null), "Hover accepted absent sender");
            require(!ManifestTracking.fromHover(player, menu.containerId + 1, 100, true,
                () -> { throw new AssertionError("Wrong-menu hover decoded the reference"); }), "Hover accepted wrong container");
            require(decoded.apply(player), "Native hover did not select current owned/Hive sources");
            require(player.containerMenu == player.inventoryMenu, "Successful unshifted hover did not close its container");
            require(ManifestTracking.markers(player).size() == 2, "Hover failed to select both authorized sources");
            require(!ManifestTracking.fromHover(player, player.containerMenu.containerId, 100, true,
                () -> { throw new AssertionError("Throttled hover decoded the reference"); }), "Hover scan was not throttled");
            for (var pos : new BlockPos[]{own, shared}) require(
                ((RadiantChestBlockEntity) player.level().getBlockEntity(pos)).inventory().getStackInSlot(0).getCount() == 3,
                "Hover selection changed stored inventory");
            var clear = open(player);
            require(new ManifestSelect(clear.containerId, clear.snapshotRevision(), -1, 2).apply(player), "Hover fixture cleanup failed");
        } finally { buffer.release(); }
        boolean rejected = false;
        try { new com.aranaira.arcanearchives.events.ManifestHover(0, 100, true, new byte[16385]); }
        catch (IllegalArgumentException expected) { rejected = true; }
        require(rejected, "Oversized hover payload accepted");
    }

    public static void doubleChest(GameTestHelper helper, ServerPlayer player, net.minecraft.world.entity.player.Player colleague) {
        unavailableAttachment(helper, player);
        var level = helper.getLevel();
        var first = helper.absolutePos(new BlockPos(1, 2, 1));
        var second = first.east();
        var positions = java.util.List.of(first, second, first.north(), second.north());
        require(positions.stream().allMatch(level::isEmptyBlock), "Double-chest tracking fixture occupied");
        var hives = HiveSaveData.get(player.server);
        try {
            require(hives.acceptInvitation(colleague.getUUID(), player.getUUID()), "Double-chest fixture could not join Hive");
            level.setBlock(first, Blocks.CHEST.defaultBlockState()
                .setValue(net.minecraft.world.level.block.ChestBlock.FACING, net.minecraft.core.Direction.NORTH)
                .setValue(net.minecraft.world.level.block.ChestBlock.TYPE, net.minecraft.world.level.block.state.properties.ChestType.LEFT), 2);
            level.setBlock(second, Blocks.CHEST.defaultBlockState()
                .setValue(net.minecraft.world.level.block.ChestBlock.FACING, net.minecraft.core.Direction.NORTH)
                .setValue(net.minecraft.world.level.block.ChestBlock.TYPE, net.minecraft.world.level.block.state.properties.ChestType.RIGHT), 2);
            for (var pos : java.util.List.of(first, second))
                ((net.minecraft.world.Container) level.getBlockEntity(pos)).setItem(0, new ItemStack(Items.ENDER_EYE, 3));
            // Install the foreign observer first so its half supplies the deduplicated descriptor.
            for (var pos : java.util.List.of(second.north(), first.north())) {
                level.setBlockAndUpdate(pos, ContentRegistry.MONITORING_CRYSTAL.get().defaultBlockState()
                    .setValue(com.aranaira.arcanearchives.blocks.MonitoringCrystal.FACING, net.minecraft.core.Direction.NORTH));
                ((com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity) level.getBlockEntity(pos))
                    .recordPlacer(pos.equals(first.north()) ? player : colleague);
            }
            var menu = open(player);
            var add = command(menu, 0);
            require(menu.entries().get(add.index()).count() == 6, "Double-chest tracking projection duplicated or lost contents");
            require(add.apply(player), "Opposite-half tracking selection rejected");
            var selected = ManifestTracking.markers(player);
            require(selected.size() == 1 && selected.get(0).position().pos.equals(second)
                && selected.get(0).owners().equals(java.util.Set.of(player.getUUID(), colleague.getUUID())),
                "Opposite-half monitor did not grant access to the foreign half's selected location");
            player.closeContainer();
            require(hives.resign(player.getUUID()), "Double-chest fixture could not revoke Hive access");
            ManifestTracking.tick(player);
            var retained = ManifestTracking.markers(player);
            require(retained.size() == 1 && retained.get(0).position().equals(selected.get(0).position())
                && retained.get(0).owners().equals(java.util.Set.of(player.getUUID())),
                "Hive revocation removed a double-chest location still accessible through the other half");
            ((net.minecraft.world.Container) level.getBlockEntity(first)).clearContent();
            level.setBlockAndUpdate(first, Blocks.AIR.defaultBlockState());
            require(level.getBlockEntity(second) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity
                && level.getBlockState(second).getValue(net.minecraft.world.level.block.ChestBlock.TYPE)
                    == net.minecraft.world.level.block.state.properties.ChestType.SINGLE,
                "Double-chest split fixture did not retain the selected half as a single chest");
            ManifestTracking.tick(player);
            require(ManifestTracking.markers(player).isEmpty(),
                "Removing the opposite chest half retained a now-inaccessible selected location");
            require(((net.minecraft.world.Container) level.getBlockEntity(second)).getItem(0).getCount() == 3,
                "Tracking reconciliation mutated the surviving chest's inventory");
            menu = open(player);
            require(new ManifestSelect(menu.containerId, menu.snapshotRevision(), -1, 2).apply(player), "Double-chest tracking clear failed");
        } finally {
            player.closeContainer();
            hives.resign(player.getUUID());
            hives.resign(colleague.getUUID());
            for (var pos : java.util.List.of(first, second))
                if (level.getBlockEntity(pos) instanceof net.minecraft.world.Container inventory) inventory.clearContent();
            for (var pos : java.util.List.of(first.north(), second.north(), first, second))
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    private static void unavailableAttachment(GameTestHelper helper, ServerPlayer player) {
        var level = helper.getLevel();
        var target = new BlockPos(65551, level.getMaxBuildHeight() - 3, 65544);
        var monitor = target.west();
        var across = target.east();
        var targetChunk = new net.minecraft.world.level.ChunkPos(target);
        var attachmentChunk = new net.minecraft.world.level.ChunkPos(across);
        level.getChunk(target.getX() >> 4, target.getZ() >> 4);
        require(level.isEmptyBlock(target) && level.isEmptyBlock(monitor), "Chunk-boundary tracking fixture occupied");
        require(!level.hasChunkAt(across), "Chunk-boundary fixture requires an unavailable attachment chunk");
        try {
            level.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.FORCED, targetChunk, 0, targetChunk);
            level.setBlock(target, Blocks.BARREL.defaultBlockState(), 18); // Do not wake neighboring chunks through shape updates.
            var seeded = level.getBlockEntity(target).getType().create(target, level.getBlockState(target));
            ((net.minecraft.world.Container) seeded).setItem(0, new ItemStack(Items.ENDER_EYE, 3));
            level.setBlockEntity(seeded); // Seed detached: vanilla setChanged otherwise wakes comparator-neighbor chunks.
            level.setBlock(monitor, ContentRegistry.MONITORING_CRYSTAL.get().defaultBlockState()
                .setValue(com.aranaira.arcanearchives.blocks.MonitoringCrystal.FACING, net.minecraft.core.Direction.WEST), 18);
            ((com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity) level.getBlockEntity(monitor)).recordPlacer(player);

            require(command(open(player), 0).apply(player), "Chunk-boundary fixture could not select its distant source");
            player.closeContainer();
            require(ManifestTracking.markers(player).size() == 1, "Chunk-boundary fixture did not retain its selection");
            level.setBlock(monitor, Blocks.AIR.defaultBlockState(), 18);
            level.getChunkSource().removeRegionTicket(net.minecraft.server.level.TicketType.UNKNOWN, attachmentChunk, 0, attachmentChunk);
            level.getChunkSource().tick(() -> true, false);
            require(!level.hasChunkAt(across), "Fixture did not release the neighboring chunk's temporary load ticket");
            ManifestTracking.tick(player);
            require(!level.hasChunkAt(across) && ManifestTracking.markers(player).size() == 1,
                "Reconciliation loaded an unavailable attachment chunk or prematurely revoked tracking");
            level.getChunkSource().removeRegionTicket(net.minecraft.server.level.TicketType.FORCED, targetChunk, 0, targetChunk);
            level.getChunkSource().removeRegionTicket(net.minecraft.server.level.TicketType.UNKNOWN, targetChunk, 0, targetChunk);
            level.getChunkSource().tick(() -> true, false);
            require(!level.hasChunkAt(target), "Fixture did not make the selected source chunk unavailable");
            ManifestTracking.tick(player);
            require(ManifestTracking.markers(player).size() == 1 && !level.hasChunkAt(target),
                "Reconciliation loaded the unavailable selected source chunk or erased its cached marker");
            level.getChunkSource().addRegionTicket(net.minecraft.server.level.TicketType.FORCED, targetChunk, 0, targetChunk);
            level.getChunk(target.getX() >> 4, target.getZ() >> 4);
            level.getChunk(across.getX() >> 4, across.getZ() >> 4);
            require(level.hasChunkAt(across), "Chunk-boundary fixture did not make the attachment chunk available");
            ManifestTracking.tick(player);
            require(ManifestTracking.markers(player).isEmpty(), "Reconciliation did not resume after attachment chunk availability");
        } finally {
            try {
                player.closeContainer();
                if (level.getBlockEntity(target) instanceof net.minecraft.world.Container container) container.clearContent();
                level.setBlockAndUpdate(monitor, Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
            } finally {
                level.getChunkSource().removeRegionTicket(net.minecraft.server.level.TicketType.FORCED, targetChunk, 0, targetChunk);
            }
        }
    }

    /** Leadership changes must prune departed owners without dropping remaining members' selections. */
    public static void succession(GameTestHelper helper, ServerPlayer player) {
        var level = helper.getLevel();
        var personal = helper.absolutePos(new BlockPos(1, 2, 1));
        var departed = personal.east();
        var remaining = personal.north();
        var positions = java.util.List.of(personal, departed, remaining);
        require(positions.stream().allMatch(level::isEmptyBlock), "Tracking succession fixture occupied");
        UUID owner = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        var hives = HiveSaveData.get(player.server);
        try {
            require(hives.acceptInvitation(owner, player.getUUID()) && hives.acceptInvitation(owner, member),
                "Tracking succession fixture could not form a three-player Hive");
            for (var pos : positions) {
                level.setBlockAndUpdate(pos, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
                var chest = (RadiantChestBlockEntity) level.getBlockEntity(pos);
                chest.setOwner(pos.equals(personal) ? player.getUUID() : pos.equals(departed) ? owner : member);
                chest.inventory().setStackInSlot(0, new ItemStack(Items.ENDER_EYE, 3));
            }
            require(command(open(player), 0).apply(player) && ManifestTracking.markers(player).size() == 3,
                "Tracking did not select all three Hive owners' sources");
            player.closeContainer();
            require(hives.resign(owner) && player.getUUID().equals(hives.ownerOf(member)),
                "Tracking succession fixture did not promote the oldest member");
            ManifestTracking.tick(player);
            var retained = ManifestTracking.markers(player);
            require(retained.size() == 2 && retained.stream().noneMatch(marker -> marker.position().pos.equals(departed))
                && retained.stream().anyMatch(marker -> marker.position().pos.equals(remaining)),
                "Tracking succession lost a remaining member or retained the departed owner's source");
            require(hives.resign(member) && hives.ownerOf(player.getUUID()) == null,
                "Tracking succession fixture did not dissolve the single-player Hive");
            ManifestTracking.tick(player);
            retained = ManifestTracking.markers(player);
            require(retained.size() == 1 && retained.get(0).position().pos.equals(personal),
                "Hive disbanding did not retain exactly personal tracking");
            player.setShiftKeyDown(false);
            ManifestTracking.clearFromKey(player);
            require(ManifestTracking.markers(player).size() == 1, "Non-sneaking shortcut cleared tracking");
            var menu = open(player);
            player.setShiftKeyDown(true);
            ManifestTracking.clearFromKey(player);
            require(ManifestTracking.markers(player).size() == 1, "Tracking clear shortcut bypassed the active-menu guard");
            player.closeContainer();
            String returnCommand = "execute in " + level.dimension().location() + " run tp @s "
                + player.getX() + " " + player.getY() + " " + player.getZ();
            try {
                player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4),
                    "execute in minecraft:the_nether run tp @s 0 120 0");
                require(player.level().dimension().equals(net.minecraft.world.level.Level.NETHER),
                    "Tracking clear fixture did not enter the Nether");
                player.setShiftKeyDown(true);
                ManifestTracking.clearFromKey(player);
                var otherDimension = ManifestTracking.markers(player);
                require(otherDimension.size() == 1 && otherDimension.get(0).position().dimension.equals(level.dimension())
                    && otherDimension.get(0).position().pos.equals(personal) && player.containerMenu == player.inventoryMenu,
                    "Sneaking shortcut erased tracking in another dimension or opened a menu");
            } finally {
                player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4), returnCommand);
            }
            require(player.level() == level, "Tracking clear fixture did not return to its original dimension");
            player.setShiftKeyDown(true);
            ManifestTracking.clearFromKey(player);
            require(ManifestTracking.markers(player).isEmpty() && player.containerMenu == player.inventoryMenu,
                "Sneaking shortcut did not clear current-dimension tracking without opening a menu");
        } finally {
            player.setShiftKeyDown(false);
            player.closeContainer();
            hives.resign(owner);
            hives.resign(member);
            hives.resign(player.getUUID());
            for (var pos : positions) {
                if (level.getBlockEntity(pos) instanceof RadiantChestBlockEntity chest)
                    chest.inventory().setStackInSlot(0, ItemStack.EMPTY);
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
    }

    /** Restore membership before reading: only an intervening automatic tick can have removed the foreign marker. */
    public static void automaticRevocation(GameTestHelper helper, ServerPlayer player, Runnable finished) {
        var level = helper.getLevel();
        var own = helper.absolutePos(new BlockPos(1, 2, 1));
        var shared = helper.absolutePos(new BlockPos(2, 2, 1));
        UUID owner = UUID.randomUUID();
        var hives = HiveSaveData.get(player.server);
        Runnable release = () -> {
            try {
                player.closeContainer();
                hives.resign(player.getUUID());
                hives.resign(owner);
                for (var pos : new BlockPos[]{own, shared}) {
                    if (level.getBlockEntity(pos) instanceof RadiantChestBlockEntity chest)
                        chest.inventory().setStackInSlot(0, ItemStack.EMPTY);
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            } finally { finished.run(); }
        };
        if (!level.isEmptyBlock(own) || !level.isEmptyBlock(shared)) {
            finished.run();
            throw new AssertionError("Automatic tracking fixture occupied");
        }
        try {
            require(hives.acceptInvitation(owner, player.getUUID()), "Automatic tracking fixture could not join Hive");
            for (var pos : new BlockPos[]{own, shared}) {
                level.setBlockAndUpdate(pos, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
                var chest = (RadiantChestBlockEntity) level.getBlockEntity(pos);
                chest.setOwner(pos.equals(own) ? player.getUUID() : owner);
                chest.inventory().setStackInSlot(0, new ItemStack(Items.ENDER_EYE, 3));
            }
            require(command(open(player), 0).apply(player) && ManifestTracking.markers(player).size() == 2,
                "Automatic tracking fixture did not select both sources");
            player.closeContainer();
            require(hives.resign(player.getUUID()), "Automatic tracking fixture could not revoke access");
            player.setPos(own.getX() + 0.5, own.getY() + 1, own.getZ() + 0.5);
            // Embedded connections are not owned by ServerConnectionListener's normal tick loop.
            // Advance the native listener, which invokes ServerPlayer.doTick and loader player-tick events.
            helper.runAfterDelay(1, () -> {
                try { player.connection.tick(); }
                catch (RuntimeException | Error failure) { release.run(); throw failure; }
            });
            helper.runAfterDelay(2, () -> {
                try {
                    require(player.containerMenu == player.inventoryMenu, "Automatic tracking fixture unexpectedly reopened a menu");
                    require(hives.acceptInvitation(owner, player.getUUID()), "Automatic tracking fixture could not restore access");
                    var retained = ManifestTracking.markers(player);
                    require(retained.size() == 1 && retained.get(0).position().pos.equals(own),
                        "Native tick hook did not prune revoked tracking while the menu was closed");
                } finally { release.run(); }
                require(ManifestTracking.markers(player).isEmpty(), "Disconnected native player retained accessible tracking state");
                helper.succeed();
            });
        } catch (RuntimeException | Error failure) {
            release.run();
            throw failure;
        }
    }

    private static ManifestMenu open(ServerPlayer player) {
        ManifestMenu.open(player);
        var menu = (ManifestMenu) player.containerMenu;
        require(menu.request(player, 0), "Tracking lifecycle could not publish its listing");
        return menu;
    }
    private static ManifestSelect command(ManifestMenu menu, int action) {
        for (int i = 0; i < menu.entries().size(); i++)
            if (menu.entries().get(i).stack().is(Items.ENDER_EYE))
                return new ManifestSelect(menu.containerId, menu.snapshotRevision(), i, action);
        throw new AssertionError("Tracking lifecycle item is missing from listing");
    }
    private static ManifestSelect roundTrip(ManifestSelect message) {
        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            ManifestSelect.write(buffer, message);
            var decoded = ManifestSelect.read(buffer);
            require(message.equals(decoded) && !buffer.isReadable(), "Tracking command codec changed its fields");
            return decoded;
        } finally { buffer.release(); }
    }

    private static void trackingTransport(ServerPlayer player, java.util.List<ManifestTracking.Marker> original,
            java.util.List<ManifestTracking.Marker> retained) {
        var registries = player.level().registryAccess();
        var receiver = new com.aranaira.arcanearchives.events.ManifestSnapshotReceiver(0);
        byte[] before = ManifestTracking.encodeMarkers(original, registries);
        var changedGrants = original.stream().map(marker -> new ManifestTracking.Marker(marker.stack(), marker.position(),
            java.util.Set.of(UUID.randomUUID()))).toList();
        require(java.util.Arrays.equals(before, ManifestTracking.encodeMarkers(changedGrants, registries)),
            "Tracking transport disclosed server ownership grants");
        receiver.receive(new com.aranaira.arcanearchives.events.ManifestSnapshot(0, 1, 0, before.length, before), registries);
        require(receiver.ready() && !receiver.failed() && receiver.entries().size() == original.size(),
            "Tracking receiver lost the selected references");
        for (int i = 0; i < original.size(); i++) {
            var decoded = receiver.entries().get(i);
            var counted = decoded.stack();
            counted.setCount(37);
            require(com.aranaira.arcanearchives.client.ManifestHighlight.matches(counted, receiver.entries()),
                "Confirmed tracking reference did not highlight independently of count");
            require(ItemStack.matches(original.get(i).stack(), decoded.stack()) && decoded.count() == 1
                && decoded.locations().size() == 1 && decoded.locations().get(0).position().equals(original.get(i).position()),
                "Tracking transport changed reference data or coordinates");
        }
        byte[] after = ManifestTracking.encodeMarkers(retained, registries);
        var repeated = new java.util.ArrayList<>(receiver.entries());
        repeated.addAll(receiver.entries());
        var expectedPositions = original.stream().map(marker -> marker.position().pos).collect(java.util.stream.Collectors.toSet());
        require(com.aranaira.arcanearchives.client.ManifestRays.positions(repeated, player.level().dimension()).equals(expectedPositions)
            && com.aranaira.arcanearchives.client.ManifestRays.positions(repeated, net.minecraft.world.level.Level.NETHER).isEmpty(),
            "Ray projection duplicated locations or leaked another dimension's coordinates");
        receiver.receive(new com.aranaira.arcanearchives.events.ManifestSnapshot(0, 2, 0, after.length, after), registries);
        receiver.receive(new com.aranaira.arcanearchives.events.ManifestSnapshot(0, 1, 0, before.length, before), registries);
        require(receiver.entries().size() == retained.size(), "Late tracking snapshot revived revoked sources");
        var retainedPositions = retained.stream().map(marker -> marker.position().pos).collect(java.util.stream.Collectors.toSet());
        require(com.aranaira.arcanearchives.client.ManifestRays.positions(receiver.entries(), player.level().dimension()).equals(retainedPositions),
            "Ray projection retained revoked coordinates after an older snapshot replay");
        receiver.receive(new com.aranaira.arcanearchives.events.ManifestSnapshot(3, 3, 0, before.length, before), registries);
        require(receiver.entries().size() == retained.size() && receiver.snapshotRevision() == 2,
            "Menu snapshot contaminated connection tracking");
        byte[] empty = ManifestTracking.encodeMarkers(java.util.List.of(), registries);
        receiver.receive(new com.aranaira.arcanearchives.events.ManifestSnapshot(0, 3, 0, empty.length, empty), registries);
        require(receiver.ready() && !receiver.failed() && receiver.entries().isEmpty(), "Tracking clear did not replace the client snapshot");
        require(com.aranaira.arcanearchives.client.ManifestRays.positions(receiver.entries(), player.level().dimension()).isEmpty(),
            "Cleared snapshot retained ray positions");
        require(!com.aranaira.arcanearchives.client.ManifestHighlight.matches(original.get(0).stack(), receiver.entries()),
            "Cleared tracking still highlighted a reference");
        receiver.receive(new com.aranaira.arcanearchives.events.ManifestSnapshot(0, 4, 0, before.length, before), registries);
        require(!com.aranaira.arcanearchives.client.ManifestRays.positions(receiver.entries(), player.level().dimension()).isEmpty(),
            "Tracking failure fixture did not first restore visible references");
        receiver.receive(new com.aranaira.arcanearchives.events.ManifestSnapshot(0, 5, 0, 0, new byte[0]), registries);
        receiver.receive(new com.aranaira.arcanearchives.events.ManifestSnapshot(0, 4, 0, before.length, before), registries);
        require(receiver.failed() && receiver.ready()
            && com.aranaira.arcanearchives.client.ManifestRays.positions(receiver.entries(), player.level().dimension()).isEmpty()
            && !com.aranaira.arcanearchives.client.ManifestHighlight.matches(original.get(0).stack(), receiver.entries()),
            "Failed tracking replacement or stale replay left rays/highlights visible");
        receiver.receive(new com.aranaira.arcanearchives.events.ManifestSnapshot(0, 6, 0, after.length, after), registries);
        require(receiver.ready() && !receiver.failed()
            && com.aranaira.arcanearchives.client.ManifestRays.positions(receiver.entries(), player.level().dimension()).equals(retainedPositions),
            "A newer authorized tracking snapshot did not recover after failure");
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.setDamageValue(5);
        var tracked = java.util.List.of(new com.aranaira.arcanearchives.data.ManifestContents.Entry(sword, 1,
            com.aranaira.arcanearchives.data.ManifestContents.Range.IN_RANGE, java.util.List.of()));
        require(com.aranaira.arcanearchives.client.ManifestHighlight.matches(sword, tracked), "Identical item data did not highlight");
        sword.setDamageValue(7);
        require(!com.aranaira.arcanearchives.client.ManifestHighlight.matches(sword, tracked)
            && !com.aranaira.arcanearchives.client.ManifestHighlight.matches(ItemStack.EMPTY, tracked)
            && !com.aranaira.arcanearchives.client.ManifestHighlight.matches(new ItemStack(Items.STICK), tracked),
            "Highlight ignored item identity or item data");
    }


    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
