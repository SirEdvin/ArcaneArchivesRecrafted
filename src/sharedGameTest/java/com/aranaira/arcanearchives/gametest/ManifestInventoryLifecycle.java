package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.blocks.MonitoringCrystal;
import com.aranaira.arcanearchives.data.HiveSaveData;
import com.aranaira.arcanearchives.data.ManifestContents;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.recipe.CraftingCreator;
import com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;

/** Live network inventories and native monitored capabilities; no injected inventory provider or prebuilt entries. */
public final class ManifestInventoryLifecycle {
    public static void run(GameTestHelper helper, Player player) {
        var level = helper.getLevel();
        var hives = HiveSaveData.get(level.getServer());
        UUID owner = player.getUUID();
        UUID member = UUID.randomUUID();
        var first = helper.absolutePos(new BlockPos(1, 2, 1));
        var second = first.east();
        var positions = List.of(first, second, first.above(), second.above(), first.south());
        require(positions.stream().allMatch(level::isEmptyBlock), "Manifest fixture occupied");
        require(hives.ownerOf(owner) == null, "Manifest fixture player already belongs to a Hive");
        player.setPos(first.getX(), first.getY(), first.getZ());
        try {
            level.setBlockAndUpdate(first, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
            level.setBlockAndUpdate(second, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
            var chest = (RadiantChestBlockEntity) level.getBlockEntity(first);
            var other = (RadiantChestBlockEntity) level.getBlockEntity(second);
            chest.setOwner(owner);
            chest.setName("Named fixture");
            other.setOwner(member);
            chest.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 31));
            var tagged = CraftingCreator.withCreator(new ItemStack(Items.DIAMOND, 7), owner, "Crafter");
            chest.inventory().setStackInSlot(53, tagged);
            other.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 11));
            var personal = ManifestContents.collect(player, 100);
            require(personal.size() == 2 && total(personal) == 38, "Manifest lost component identity or included foreign storage");
            require(personal.stream().allMatch(e -> e.locations().stream().allMatch(l -> l.description().equals("Chest: Named fixture")
                && l.position().pos.equals(first) && l.position().dimension.equals(level.dimension()))), "Source location/name lost");
            require(hives.acceptInvitation(owner, member), "Could not form Manifest fixture Hive");
            var shared = ManifestContents.collect(player, 100);
            require(shared.size() == 2 && total(shared) == 49, "Hive Manifest did not aggregate exact item totals");
            var plain = shared.stream().filter(e -> e.count() == 42).findFirst().orElseThrow();
            require(plain.stack().getCount() == 1 && plain.locations().size() == 2, "Count not detached from icon/source locations");
            plain.stack().setCount(0);
            require(plain.stack().is(Items.DIAMOND) && chest.inventory().getStackInSlot(0).getCount() == 31,
                "Manifest icon mutation reached live inventory or snapshot");
            var boundary = ManifestContents.collect(player, 1);
            require(boundary.size() == 3 && boundary.stream().anyMatch(e -> e.range() == ManifestContents.Range.OUT_OF_RANGE && e.count() == 11),
                "Original strict range boundary was not preserved");
            require(ManifestContents.collect(player, 0).stream().allMatch(e -> e.range() == ManifestContents.Range.OUT_OF_RANGE),
                "Zero-radius range classification changed");
            var expandedRange = ManifestContents.collect(player, Integer.MAX_VALUE);
            require(total(expandedRange) == total(shared) && expandedRange.stream().allMatch(e -> e.range() == ManifestContents.Range.IN_RANGE),
                "Large configured range overflowed or changed visible totals");
            require(hives.resign(member) && total(ManifestContents.collect(player, 100)) == 38, "Revoked Hive inventories remained in fresh Manifest");
            require(total(ManifestContents.collect(player, Integer.MAX_VALUE)) == 38, "Large range bypassed Hive revocation");
            chest.inventory().setStackInSlot(0, ItemStack.EMPTY);
            require(total(personal) == 38 && total(ManifestContents.collect(player, 100)) == 7, "Snapshots alias live mutation or fresh reads stay stale");
            chest.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, Integer.MAX_VALUE));
            chest.inventory().setStackInSlot(1, new ItemStack(Items.DIAMOND, 19));
            var large = ManifestContents.collect(player, 100);
            require(total(large) == (long) Integer.MAX_VALUE + 26
                && large.stream().anyMatch(e -> e.count() == (long) Integer.MAX_VALUE + 19), "Manifest total overflowed native stack counts");
            ManifestTransportLifecycle.run(player, large);
            chest.inventory().setStackInSlot(0, ItemStack.EMPTY);
            chest.inventory().setStackInSlot(1, new ItemStack(Items.DIAMOND_SWORD, 2));
            var tools = ManifestContents.collect(player, 100).stream().filter(e -> e.stack().is(Items.DIAMOND_SWORD)).findFirst().orElseThrow();
            require(tools.count() == 2 && tools.locations().size() == 2
                && tools.locations().stream().allMatch(l -> l.count() == 1), "Nonstackable source descriptors changed");
            chest.inventory().setStackInSlot(1, ItemStack.EMPTY);
            chest.inventory().setStackInSlot(53, ItemStack.EMPTY);
            other.inventory().setStackInSlot(0, ItemStack.EMPTY);
            for (var pos : List.of(first, second)) level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

            // Both halves and a second observer of the first half must contribute one underlying inventory.
            level.setBlock(first, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH)
                .setValue(ChestBlock.TYPE, ChestType.LEFT), 2);
            level.setBlock(second, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH)
                .setValue(ChestBlock.TYPE, ChestType.RIGHT), 2);
            ((Container) level.getBlockEntity(first)).setItem(0, new ItemStack(Items.EMERALD, 13));
            ((Container) level.getBlockEntity(second)).setItem(0, new ItemStack(Items.EMERALD, 17));
            monitor(helper, player, first.above(), Direction.UP);
            monitor(helper, player, second.above(), Direction.UP);
            monitor(helper, player, first.south(), Direction.SOUTH);
            var monitored = ManifestContents.collect(player, 100);
            require(monitored.size() == 1 && total(monitored) == 30, "Repeated/double-chest monitoring inflated the Manifest");
            require(monitored.get(0).locations().stream().allMatch(l -> l.description().equals("Monitoring Crystal")), "Wrong monitored descriptor");
            ((Container) level.getBlockEntity(first)).clearContent();
            ((Container) level.getBlockEntity(second)).clearContent();
            level.setBlockAndUpdate(first, Blocks.BARREL.defaultBlockState());
            level.setBlockAndUpdate(second, Blocks.BARREL.defaultBlockState());
            ((Container) level.getBlockEntity(first)).setItem(0, new ItemStack(Items.EMERALD, 5));
            ((Container) level.getBlockEntity(second)).setItem(0, new ItemStack(Items.EMERALD, 9));
            require(total(ManifestContents.collect(player, 100)) == 14, "Distinct replacement inventories were deduplicated together");
            ((Container) level.getBlockEntity(first)).clearContent();
            level.setBlockAndUpdate(first, Blocks.STONE.defaultBlockState());
            require(total(ManifestContents.collect(player, 100)) == 9, "Removed monitored target retained stale contents");
        } finally {
            hives.resign(member);
            hives.resign(owner);
            for (BlockPos pos : positions) {
                var entity = level.getBlockEntity(pos);
                if (entity instanceof Container inventory) inventory.clearContent();
                if (entity instanceof RadiantChestBlockEntity chest)
                    for (int slot = 0; slot < chest.inventory().getSlots(); slot++) chest.inventory().setStackInSlot(slot, ItemStack.EMPTY);
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static void monitor(GameTestHelper helper, Player player, BlockPos pos, Direction face) {
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, ContentRegistry.MONITORING_CRYSTAL.get().defaultBlockState().setValue(MonitoringCrystal.FACING, face));
        ((MonitoringCrystalBlockEntity) level.getBlockEntity(pos)).recordPlacer(player);
    }

    private static long total(List<ManifestContents.Entry> entries) {
        return entries.stream().mapToLong(ManifestContents.Entry::count).sum();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
