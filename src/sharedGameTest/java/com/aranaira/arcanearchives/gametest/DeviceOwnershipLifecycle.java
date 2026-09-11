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
            BiConsumer<ItemStack, CompoundTag> itemData) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        player.setPos(pos.getX() + 4, pos.getY(), pos.getZ() + 4);
        for (var item : new BlockItem[]{ContentRegistry.GEMCUTTERS_TABLE_ITEM.get(), ContentRegistry.MONITORING_CRYSTAL_ITEM.get()}) {
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
                        ? "arcanearchives:gemcutters_table" : "arcanearchives:monitoring_crystal");
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
                    if (entity instanceof GemCuttersTableBlockEntity table) {
                        other = GemCuttersTable.connectedPos(pos, level.getBlockState(pos));
                        require(level.getBlockEntity(other) == null, "Accessor acquired another network entity");
                        require(table.insertInput(17, new ItemStack(Items.DIAMOND, 7), false).isEmpty(), "Could not seed input");
                        require(table.stillValid(player), "Ownership changed local table access");
                    }
                    var saved = save.apply(entity);
                    var restored = (NetworkOwnedBlockEntity) entity.getType().create(pos, entity.getBlockState());
                    restored.setLevel(level);
                    load.accept(restored, saved);
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
                    // The Gem Cutter emits its conserved device in onRemove; the Crystal uses native loot only.
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
        helper.succeed();
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
