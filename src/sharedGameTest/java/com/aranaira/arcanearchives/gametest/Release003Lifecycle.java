package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.config.ServerSideConfig;
import com.aranaira.arcanearchives.data.StoragePlacementSaveData;
import com.aranaira.arcanearchives.data.ManifestTracking;
import com.aranaira.arcanearchives.events.ManifestSnapshot;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantCraftingTableBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantResonatorBlockEntity;
import com.aranaira.arcanearchives.types.BlockPosDimension;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Actual placement, quota bypass/denial, native state and tracking transport on every leaf. */
public final class Release003Lifecycle {
    public static void run(GameTestHelper helper, ServerPlayer player, Function<BlockEntity, CompoundTag> save,
            BiConsumer<BlockEntity, CompoundTag> load) {
        var level = helper.getLevel();
        var root = helper.absolutePos(new BlockPos(1, 2, 1));
        var positions = List.of(root, root.east(), root.above(), root.south());
        for (var pos : positions) require(level.isEmptyBlock(pos), "0.0.3 fixture occupied");
        var original = ServerSideConfig.current().toProperties();
        var arsenal = com.aranaira.arcanearchives.config.ArsenalConfig.current();
        var hand = player.getMainHandItem().copy();
        boolean creative = player.getAbilities().instabuild;
        boolean shift = player.isShiftKeyDown();
        Path config = null;
        try {
            config = Files.createTempDirectory("arcane-003-native-");
            Files.createDirectories(config.resolve("arcanearchives"));
            Files.writeString(config.resolve("arcanearchives/arsenal.properties"), "EnableArsenal=" + arsenal.enableArsenal()
                + "\nColourblindMode=" + arsenal.colourblindMode());
            var values = new java.util.Properties();
            values.putAll(original);
            for (String key : new String[]{"RadiantChestLimit", "RadiantTroveLimit", "RadiantTankLimit"}) values.setProperty(key, "2");
            configure(config, values);
            var ledger = StoragePlacementSaveData.get(player.server);
            var far = new BlockPos(200000, 80, 200000);
            var seed = new CompoundTag();
            var savedEntries = new net.minecraft.nbt.ListTag();
            for (var dimension : List.of(net.minecraft.world.level.Level.OVERWORLD, net.minecraft.world.level.Level.NETHER)) {
                var world = player.server.getLevel(dimension);
                require(world != null && !world.hasChunkAt(far), "Unavailable quota fixture was already loaded");
                var entry = new BlockPosDimension(far, dimension).serializeNBT();
                entry.putUUID("owner", player.getUUID()); entry.putString("type", "chest");
                savedEntries.add(entry);
            }
            seed.put("placements", savedEntries);
            var unavailable = StoragePlacementSaveData.load(seed);
            require(unavailable.count(player.server, player.getUUID(), "chest") == 2, "Unloaded/dimension quotas lost");
            require(unavailable.count(player.server, player.getUUID(), "tank") == 0
                && unavailable.count(player.server, java.util.UUID.randomUUID(), "chest") == 0, "Quota mixed owners/types");
            for (var dimension : List.of(net.minecraft.world.level.Level.OVERWORLD, net.minecraft.world.level.Level.NETHER))
                require(!player.server.getLevel(dimension).hasChunkAt(far), "Quota counting loaded a chunk");
            for (var item : new BlockItem[]{ContentRegistry.RADIANT_CHEST_ITEM.get(), ContentRegistry.RADIANT_TROVE_ITEM.get(), ContentRegistry.RADIANT_TANK_ITEM.get()}) {
                String type = StoragePlacementSaveData.type(item.getBlock());
                player.getAbilities().instabuild = false;
                for (int i = 0; i < 2; i++) require(place(item, player, positions.get(i)), "Below-limit placement failed");
                require(ledger.count(player.server, player.getUUID(), type) == 2, "Placements not counted");
                require(!place(item, player, positions.get(2)) && level.isEmptyBlock(positions.get(2)), "Survival exceeded quota");
                player.getAbilities().instabuild = true;
                require(place(item, player, positions.get(2)), "Creative quota bypass failed");
                require(ledger.count(player.server, player.getUUID(), type) == 3, "Creative placement was not counted");
                player.getAbilities().instabuild = false;
                require(!place(item, player, positions.get(3)), "Returning to survival bypassed quota");
                level.setBlockAndUpdate(positions.get(2), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(positions.get(1), Blocks.AIR.defaultBlockState());
                require(ledger.count(player.server, player.getUUID(), type) == 1, "Removal leaked quota");
                require(place(item, player, positions.get(1)), "Removal did not permit replacement");
                for (var pos : positions) level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                require(ledger.count(player.server, player.getUUID(), type) == 0, "Cleanup leaked quota");
            }
            // Conversion denial is before source mutation and payment.
            require(place(ContentRegistry.RADIANT_CHEST_ITEM.get(), player, positions.get(0)), "Conversion quota seed");
            require(place(ContentRegistry.RADIANT_CHEST_ITEM.get(), player, positions.get(1)), "Conversion quota seed");
            var target = positions.get(3);
            level.setBlockAndUpdate(target, Blocks.CHEST.defaultBlockState());
            var chest = (net.minecraft.world.level.block.entity.ChestBlockEntity) level.getBlockEntity(target);
            chest.setItem(0, new ItemStack(Items.DIAMOND, 7));
            player.setShiftKeyDown(true);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ContentRegistry.RAW_QUARTZ.get(), 2));
            var hit = new BlockHitResult(Vec3.atCenterOf(target), Direction.UP, target, false);
            require(!ContentRegistry.RAW_QUARTZ.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction(), "At-limit conversion accepted");
            require(chest.getItem(0).getCount() == 7 && player.getMainHandItem().getCount() == 2, "Denied conversion lost items");
            chest.clearContent();
            for (var pos : positions) level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            values.setProperty("RadiantChestLimit", "0");
            configure(config, values);
            for (var pos : positions) require(place(ContentRegistry.RADIANT_CHEST_ITEM.get(), player, pos), "Zero was not unlimited");
            for (var pos : positions) level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

            level.setBlockAndUpdate(root, ContentRegistry.RADIANT_CRAFTING_TABLE.get().defaultBlockState());
            var table = (RadiantCraftingTableBlockEntity) level.getBlockEntity(root);
            table.items().set(4, new ItemStack(Items.DIAMOND, 64));
            table.setChanged();
            var copy = new RadiantCraftingTableBlockEntity(root, table.getBlockState());
            copy.setLevel(level);
            load.accept(copy, table.getUpdatePacket().getTag());
            require(copy.items().get(4).getCount() == 64 && copy.items().get(0).isEmpty(), "Table update packet lost visual slots");
            table.items().clear();
            level.setBlockAndUpdate(root, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(root, ContentRegistry.RADIANT_RESONATOR.get().defaultBlockState());
            var resonator = (RadiantResonatorBlockEntity) level.getBlockEntity(root);
            var tag = save.apply(resonator);
            tag.putInt("current_tick", ServerSideConfig.current().resonatorTickTime() / 2);
            load.accept(resonator, tag);
            var client = new RadiantResonatorBlockEntity(root, resonator.getBlockState());
            client.setLevel(level);
            load.accept(client, resonator.getUpdatePacket().getTag());
            require(Math.abs(client.visualProgress() - .5F) < .001F, "Growth packet not server normalized");

            var origin = new BlockPosDimension(root, level.dimension());
            var marker = new ManifestTracking.Marker(new ItemStack(Items.DIAMOND), new BlockPosDimension(root.east(), level.dimension()), Set.of(player.getUUID()), origin);
            var decoded = ManifestSnapshot.decode(ManifestTracking.encodeMarkers(List.of(marker), level.registryAccess()), level.registryAccess());
            require(origin.equals(decoded.get(0).locations().get(0).origin()), "Tracking transport lost lectern origin");
            var first = com.aranaira.arcanearchives.client.ManifestRays.beams(decoded, level.dimension(), Vec3.ZERO);
            var moved = com.aranaira.arcanearchives.client.ManifestRays.beams(decoded, level.dimension(), new Vec3(30, 40, 50));
            require(first.equals(moved), "Lectern beam followed player");
            require(com.aranaira.arcanearchives.integration.ViewerHiddenItems.items(true).containsAll(
                com.aranaira.arcanearchives.integration.ViewerHiddenItems.unfinished()), "Arsenal exposed unfinished items");
        } catch (java.io.IOException error) { throw new AssertionError(error); }
        finally {
            for (var pos : positions) level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            player.getAbilities().instabuild = creative;
            player.setShiftKeyDown(shift);
            player.setItemInHand(InteractionHand.MAIN_HAND, hand);
            if (config != null) {
                try { configure(config, original); }
                catch (java.io.IOException error) { throw new AssertionError(error); }
            }
        }
    }
    private static boolean place(BlockItem item, ServerPlayer player, BlockPos pos) {
        player.setPos(pos.getX() + 2, pos.getY(), pos.getZ() + 2);
        var stack = new ItemStack(item);
        boolean success = item.place(new BlockPlaceContext(player.level(), player, InteractionHand.MAIN_HAND, stack,
            new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false))).consumesAction();
        if (!success) require(stack.getCount() == 1, "Rejected placement consumed item");
        return success;
    }
    private static void configure(Path directory, java.util.Properties properties) throws java.io.IOException {
        try (var writer = Files.newBufferedWriter(directory.resolve("arcanearchives/server.properties"))) { properties.store(writer, "test"); }
        ServerSideConfig.initialize(directory);
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
