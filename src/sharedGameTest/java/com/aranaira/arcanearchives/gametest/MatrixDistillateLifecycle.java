package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.blocks.MatrixDistillate;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.MatrixPartBlockEntity;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class MatrixDistillateLifecycle {
    private MatrixDistillateLifecycle() {}
    private static void require(boolean ok, String message) { if (!ok) throw new IllegalStateException(message); }

    private static ItemStack place(GameTestHelper helper, Player player, BlockPos parent) {
        ItemStack stack = new ItemStack(ContentRegistry.MATRIX_DISTILLATE_ITEM.get(), 2);
        var context = new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack,
            new BlockHitResult(Vec3.atCenterOf(parent), Direction.UP, parent, false));
        require(ContentRegistry.MATRIX_DISTILLATE_ITEM.get().place(context).consumesAction() && stack.getCount() == 1,
            "Distillate placement failed or consumed the wrong item count");
        return stack;
    }

    public static void run(GameTestHelper helper, Player player,
            java.util.function.BiConsumer<ItemStack, net.minecraft.nbt.CompoundTag> itemEntityData) {
        var level = helper.getLevel();
        var block = ContentRegistry.MATRIX_DISTILLATE.get();
        BlockPos parent = helper.absolutePos(new BlockPos(1, 2, 1));
        player.setPos(parent.getX() + 4, parent.getY(), parent.getZ() + 4);
        for (int yaw : new int[]{0, 90, 180, 270}) {
            player.setYRot(yaw);
            var expected = block.defaultBlockState().setValue(MatrixDistillate.FACING, player.getDirection().getCounterClockWise());
            List<BlockPos> positions = MatrixDistillate.footprint(parent, expected);
            require(positions.stream().distinct().count() == 9, "Footprint did not contain nine distinct positions");
            for (int removed = 0; removed < 9; removed++) {
                require(positions.stream().allMatch(level::isEmptyBlock), "Occupied Distillate fixture");
                level.setBlockAndUpdate(positions.get(removed), Blocks.STONE.defaultBlockState());
                var denied = new ItemStack(ContentRegistry.MATRIX_DISTILLATE_ITEM.get(), 2);
                var context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, denied,
                    new BlockHitResult(Vec3.atCenterOf(parent), Direction.DOWN, parent, false));
                require(!ContentRegistry.MATRIX_DISTILLATE_ITEM.get().place(context).consumesAction() && denied.getCount() == 2,
                    "Blocked footprint consumed an item");
                level.removeBlock(positions.get(removed), false);
                require(positions.stream().allMatch(level::isEmptyBlock), "Blocked footprint partially placed");
                place(helper, player, parent);
                UUID id = ((MatrixPartBlockEntity) level.getBlockEntity(parent)).identity();
                for (int part = 0; part < 9; part++) {
                    require(level.getBlockState(positions.get(part)) == expected.setValue(MatrixDistillate.PART, part), "Wrong facing or part identity");
                    require(level.getBlockEntity(positions.get(part)) instanceof MatrixPartBlockEntity entity && id.equals(entity.identity()),
                        "Parts did not share their persisted identity");
                }
                level.destroyBlock(positions.get(removed), true);
                require(positions.stream().allMatch(level::isEmptyBlock), "Breaking a part left stale loaded pieces");
                var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(parent).inflate(4), e -> e.getItem().is(ContentRegistry.MATRIX_DISTILLATE_ITEM.get()));
                require(drops.stream().mapToInt(e -> e.getItem().getCount()).sum() == 1, "Distillate destruction did not produce exactly one item");
                drops.forEach(net.minecraft.world.entity.Entity::discard);
                place(helper, player, parent);
                level.setBlockAndUpdate(positions.get(removed), Blocks.STONE.defaultBlockState());
                for (BlockPos pos : positions)
                    require(pos.equals(positions.get(removed)) ? level.getBlockState(pos).is(Blocks.STONE) : level.isEmptyBlock(pos),
                        "Replacement cleanup lost foreign stone or left a matching part");
                level.removeBlock(positions.get(removed), false);
            }
            place(helper, player, parent);
            BlockPos foreign = positions.get(1);
            UUID foreignIdentity = UUID.randomUUID();
            ((MatrixPartBlockEntity) level.getBlockEntity(foreign)).identify(foreignIdentity);
            level.removeBlock(parent, false);
            for (BlockPos pos : positions)
                require(pos.equals(foreign) ? level.getBlockEntity(pos) instanceof MatrixPartBlockEntity entity
                    && foreignIdentity.equals(entity.identity()) : level.isEmptyBlock(pos),
                    "Loaded cleanup removed another identity or left a matching part");
            level.removeBlock(foreign, false);
            require(level.getEntitiesOfClass(ItemEntity.class, new AABB(parent).inflate(4),
                e -> e.getItem().is(ContentRegistry.MATRIX_DISTILLATE_ITEM.get())).isEmpty(),
                "Non-dropping replacement cleanup produced an item");
        }
        interruptedPlacement(helper, player, parent);
        MatrixDistillatePreflight.run(helper, player, parent);
        itemIdentity(helper, player, parent, itemEntityData);
        helper.succeed();
    }

    private static void itemIdentity(GameTestHelper helper, Player player, BlockPos parent,
            java.util.function.BiConsumer<ItemStack, net.minecraft.nbt.CompoundTag> itemEntityData) {
        var level = helper.getLevel();
        var item = ContentRegistry.MATRIX_DISTILLATE_ITEM.get();
        for (int yaw : new int[]{0, 90, 180, 270}) {
            player.setYRot(yaw);
            var expected = ContentRegistry.MATRIX_DISTILLATE.get().defaultBlockState()
                .setValue(MatrixDistillate.FACING, player.getDirection().getCounterClockWise());
            var positions = MatrixDistillate.footprint(parent, expected);
            for (boolean malformed : new boolean[]{false, true}) {
                require(positions.stream().allMatch(level::isEmptyBlock), "Item identity fixture occupied");
                var stack = new ItemStack(item, 2);
                var injected = new UUID(0, 1);
                var tag = new net.minecraft.nbt.CompoundTag();
                tag.putString("id", "arcanearchives:matrix_part");
                if (malformed) tag.putString("matrix_identity", "not a UUID");
                else tag.putUUID("matrix_identity", injected);
                itemEntityData.accept(stack, tag);
                var remaining = stack.copy();
                remaining.setCount(1);
                try {
                    var context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, stack,
                        new BlockHitResult(Vec3.atCenterOf(parent), Direction.UP, parent, false));
                    require(item.place(context).consumesAction() && ItemStack.matches(stack, remaining),
                        "Item identity placement failed or changed payment/data");
                    var identity = ((MatrixPartBlockEntity) level.getBlockEntity(parent)).identity();
                    require(!injected.equals(identity), "Item supplied the placed structure identity");
                    for (int part = 0; part < positions.size(); part++)
                        require(level.getBlockState(positions.get(part)) == expected.setValue(MatrixDistillate.PART, part)
                            && level.getBlockEntity(positions.get(part)) instanceof MatrixPartBlockEntity entity
                            && identity.equals(entity.identity()), "Item data split structure identity");
                    level.destroyBlock(positions.get(8), true);
                    require(positions.stream().allMatch(level::isEmptyBlock), "Item-data placement orphaned parts on removal");
                    var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(parent).inflate(4), e -> e.getItem().is(item));
                    require(drops.stream().mapToInt(e -> e.getItem().getCount()).sum() == 1,
                        "Item-data placement did not conserve its destruction drop");
                } finally {
                    for (BlockPos pos : positions) level.setBlock(pos, Blocks.AIR.defaultBlockState(),
                        net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                    level.getEntitiesOfClass(ItemEntity.class, new AABB(parent).inflate(4), e -> e.getItem().is(item))
                        .forEach(net.minecraft.world.entity.Entity::discard);
                }
            }
        }
    }

    private static void interruptedPlacement(GameTestHelper helper, Player player, BlockPos parent) {
        var level = helper.getLevel();
        var item = ContentRegistry.MATRIX_DISTILLATE_ITEM.get();
        var fault = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(PlacementInterruptionBlock.ID);
        require(fault instanceof PlacementInterruptionBlock, "Placement fault fixture not registered");
        var water = Blocks.WATER.defaultBlockState();
        for (int yaw : new int[]{0, 90, 180, 270}) {
            player.setYRot(yaw);
            var state = ContentRegistry.MATRIX_DISTILLATE.get().defaultBlockState()
                .setValue(MatrixDistillate.FACING, player.getDirection().getCounterClockWise());
            var positions = MatrixDistillate.footprint(parent, state);
            for (int interrupted = 0; interrupted < positions.size(); interrupted++) {
                require(positions.stream().allMatch(level::isEmptyBlock), "Interrupted-placement space occupied");
                var stack = com.aranaira.arcanearchives.recipe.CraftingCreator.withCreator(
                    new ItemStack(item, 2), new UUID(0, 1), "Interrupted placement owner");
                var original = stack.copy();
                try {
                    for (int part = 0; part < positions.size(); part++)
                        level.setBlock(positions.get(part), part == interrupted ? fault.defaultBlockState() : water,
                            net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                    var context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, stack,
                        new BlockHitResult(Vec3.atCenterOf(parent), Direction.UP, parent, false));
                    require(!item.place(context).consumesAction() && ItemStack.matches(stack, original),
                        "Interrupted Distillate placement consumed or modified its item");
                    for (int part = 0; part < positions.size(); part++) {
                        var expected = part == interrupted ? Blocks.STONE.defaultBlockState() : water;
                        require(level.getBlockState(positions.get(part)) == expected,
                            "Distillate rollback lost water or changed callback replacement: yaw=" + yaw + ", part=" + interrupted);
                        require(level.getBlockEntity(positions.get(part)) == null, "Rollback left a Matrix part entity");
                    }
                    require(level.getEntitiesOfClass(ItemEntity.class, new AABB(parent).inflate(4),
                        e -> e.getItem().is(item)).isEmpty(), "Interrupted placement produced an item drop");
                } finally {
                    // removeBlock preserves a fluid's legacy state; explicitly clear fixture water.
                    for (BlockPos pos : positions)
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                }
            }
        }
    }

}
