package com.aranaira.arcanearchives.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.api.ModInitializer;

/** Shared Fabric native fixtures; only item data and mock-player construction are version-specific. */
public final class FabricRuntimeTests implements FabricGameTest, ModInitializer {
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public static void deviceOwnership(GameTestHelper helper) {
        GemCutterRoutingTransactions.run(helper);
        BrazierRoutingTransactions.run(helper);
        DeviceOwnershipLifecycle.run(helper, RuntimeTestVersion.player(helper),
            net.fabricmc.fabric.api.entity.FakePlayer.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "Ownership fixture")),
            RuntimeTestVersion::saveEntity, RuntimeTestVersion::loadEntity, RuntimeTestVersion::itemEntityData,
            RuntimeTestVersion::lectern, GameTestHelper::makeMockServerPlayerInLevel);
    }
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 1200)
    public static void matrixDistillateLifecycle(GameTestHelper helper) {
        MatrixDistillateLifecycle.run(helper, RuntimeTestVersion.player(helper), RuntimeTestVersion::itemEntityData);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public static void matrixReservoirCrafting(GameTestHelper helper) {
        MatrixReservoirCrafting.run(helper, RuntimeTestVersion.player(helper), RuntimeTestVersion.player(helper));
    }

    @Override public void onInitialize() {
        net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.BLOCK,
            PlacementInterruptionBlock.ID, new PlacementInterruptionBlock());
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public static void matrixReservoirPlacement(GameTestHelper helper) {
        var level = helper.getLevel();
        var parent = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        var block = com.aranaira.arcanearchives.init.ContentRegistry.MATRIX_RESERVOIR.get();
        var item = com.aranaira.arcanearchives.init.ContentRegistry.MATRIX_RESERVOIR_ITEM.get();
        var player = RuntimeTestVersion.player(helper);
        player.setPos(parent.getX() + 4, parent.getY(), parent.getZ() + 4);
        var deniedStack = new net.minecraft.world.item.ItemStack(item, 2);
        for (var target : new net.minecraft.core.BlockPos[]{parent,
                new net.minecraft.core.BlockPos(parent.getX(), level.getMaxBuildHeight() - 2, parent.getZ())}) {
            boolean originalPermission = player.getAbilities().mayBuild;
            try {
                // First case is adventure-style permission denial; second is an overflowing footprint.
                player.getAbilities().mayBuild = !target.equals(parent);
                var denied = new net.minecraft.world.item.context.BlockPlaceContext(level, player,
                    net.minecraft.world.InteractionHand.MAIN_HAND, deniedStack,
                    new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(target),
                        net.minecraft.core.Direction.UP, target, false));
                if (item.place(denied).consumesAction() || deniedStack.getCount() != 2
                        || !level.isEmptyBlock(target) || !level.isEmptyBlock(target.above())) {
                    helper.fail("Denied or out-of-bounds footprint changed blocks or consumed an item"); return;
                }
            } finally {
                player.getAbilities().mayBuild = originalPermission;
            }
        }
        for (int removed = 0; removed < 3; removed++) {
            var stack = new net.minecraft.world.item.ItemStack(item, 2);
            RuntimeTestVersion.rootOverride(stack);
            var context = new net.minecraft.world.item.context.BlockPlaceContext(level, player,
                net.minecraft.world.InteractionHand.MAIN_HAND, stack,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(parent),
                    net.minecraft.core.Direction.UP, parent, false));
            level.setBlockAndUpdate(parent.above(2), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            if (item.place(context).consumesAction() || stack.getCount() != 2 || !level.isEmptyBlock(parent)) {
                helper.fail("Blocked column consumed an item or partially placed"); return;
            }
            level.removeBlock(parent.above(2), false);
            if (!item.place(context).consumesAction() || stack.getCount() != 1) {
                helper.fail("Valid reservoir placement failed or consumed wrong count"); return;
            }
            for (int part = 0; part < 3; part++) {
                var state = level.getBlockState(parent.above(part));
                if (!state.is(block) || state.getValue(com.aranaira.arcanearchives.blocks.MatrixReservoir.PART) != part) {
                    helper.fail("Reservoir part identity was not placed"); return;
                }
            }
            level.setBlockAndUpdate(parent.above(removed), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            for (int part = 0; part < 3; part++) {
                if (part == removed ? !level.getBlockState(parent.above(part)).is(net.minecraft.world.level.block.Blocks.STONE)
                        : !level.isEmptyBlock(parent.above(part))) {
                    helper.fail("Replacement cleanup removed an unrelated block or left a stale part"); return;
                }
            }
            level.removeBlock(parent.above(removed), false);
            if (!item.place(context).consumesAction() || !stack.isEmpty()) {
                helper.fail("Second placement did not consume the remaining item"); return;
            }
            level.destroyBlock(parent.above(removed), true);
            var drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(parent).inflate(3), entity -> entity.getItem().is(item));
            if (drops.stream().mapToInt(entity -> entity.getItem().getCount()).sum() != 1) {
                helper.fail("Breaking a reservoir part did not conserve exactly one block item"); return;
            }
            drops.forEach(net.minecraft.world.entity.Entity::discard);
            for (int part = 0; part < 3; part++) if (!level.isEmptyBlock(parent.above(part))) {
                helper.fail("Breaking left a reservoir part behind"); return;
            }
        }
        // Two touching columns have distinct parent identities even though every block has the same ID.
        var neighboringStack = new net.minecraft.world.item.ItemStack(item, 2);
        for (var target : new net.minecraft.core.BlockPos[]{parent, parent.above(3)}) {
            var context = new net.minecraft.world.item.context.BlockPlaceContext(level, player,
                net.minecraft.world.InteractionHand.MAIN_HAND, neighboringStack,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(target),
                    net.minecraft.core.Direction.UP, target, false));
            if (!item.place(context).consumesAction()) {
                helper.fail("Touching independent column could not be placed"); return;
            }
        }
        level.removeBlock(parent.above(2), false);
        for (int part = 0; part < 3; part++) {
            var neighbor = level.getBlockState(parent.above(3 + part));
            if (!level.isEmptyBlock(parent.above(part)) || !neighbor.is(block)
                    || neighbor.getValue(com.aranaira.arcanearchives.blocks.MatrixReservoir.PART) != part) {
                helper.fail("Column cleanup damaged a touching independent reservoir"); return;
            }
        }
        level.removeBlock(parent.above(3), false);
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public static void matrixReservoirInterruptedPlacement(GameTestHelper helper) {
        var level = helper.getLevel();
        var parent = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        var player = RuntimeTestVersion.player(helper);
        player.setPos(parent.getX() + 4, parent.getY(), parent.getZ() + 4);
        var item = com.aranaira.arcanearchives.init.ContentRegistry.MATRIX_RESERVOIR_ITEM.get();
        var fault = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(PlacementInterruptionBlock.ID);
        if (!(fault instanceof PlacementInterruptionBlock)) {
            helper.fail("Placement interruption fixture was not registered"); return;
        }
        var water = net.minecraft.world.level.block.Blocks.WATER.defaultBlockState();
        for (int failedPart = 0; failedPart < 3; failedPart++) {
            var stack = new net.minecraft.world.item.ItemStack(item, 2);
            RuntimeTestVersion.markItem(stack);
            var original = stack.copy();
            for (int part = 0; part < 3; part++)
                level.setBlock(parent.above(part), part == failedPart ? fault.defaultBlockState() : water,
                    net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            var context = new net.minecraft.world.item.context.BlockPlaceContext(level, player,
                net.minecraft.world.InteractionHand.MAIN_HAND, stack,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(parent),
                    net.minecraft.core.Direction.UP, parent, false));
            try {
                if (item.place(context).consumesAction() || !net.minecraft.world.item.ItemStack.matches(stack, original)) {
                    helper.fail("Interrupted placement consumed or modified its item"); return;
                }
                for (int part = 0; part < 3; part++) {
                    var expected = part == failedPart ? net.minecraft.world.level.block.Blocks.STONE.defaultBlockState() : water;
                    if (level.getBlockState(parent.above(part)) != expected) {
                        helper.fail("Rollback lost a replaced state or overwrote the callback's block; failed part " + failedPart);
                        return;
                    }
                }
                if (!level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(parent).inflate(3), entity -> entity.getItem().is(item)).isEmpty()) {
                    helper.fail("Interrupted placement duplicated a reservoir item drop"); return;
                }
            } finally {
                for (int part = 0; part < 3; part++) level.removeBlock(parent.above(part), false);
            }
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public static void matrixReservoirPlacementPreflight(GameTestHelper helper) {
        var level = helper.getLevel();
        var parent = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        for (int part = 0; part < 3; part++) if (!level.isEmptyBlock(parent.above(part))) {
            helper.fail("Preflight fixture is not initially empty: " + level.getBlockState(parent.above(part))); return;
        }
        var item = com.aranaira.arcanearchives.init.ContentRegistry.MATRIX_RESERVOIR_ITEM.get();
        var stack = new net.minecraft.world.item.ItemStack(item, 2);
        var player = RuntimeTestVersion.player(helper);
        player.setPos(parent.getX() + 4, parent.getY(), parent.getZ() + 4);
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(parent),
            net.minecraft.core.Direction.UP, parent, false);
        var context = new net.minecraft.world.item.context.BlockPlaceContext(level, player,
            net.minecraft.world.InteractionHand.MAIN_HAND, stack, hit);
        for (int blockedPart = 0; blockedPart < 3; blockedPart++) {
            var obstruction = net.minecraft.world.entity.EntityType.PIG.create(level);
            if (obstruction == null) { helper.fail("Could not create collision fixture"); return; }
            obstruction.setPos(parent.getX() + 0.5, parent.getY() + blockedPart, parent.getZ() + 0.5);
            level.addFreshEntity(obstruction);
            try {
                if (item.place(context).consumesAction() || stack.getCount() != 2) {
                    helper.fail("Entity-obstructed footprint consumed an item; part " + blockedPart); return;
                }
                for (int part = 0; part < 3; part++) if (!level.isEmptyBlock(parent.above(part))) {
                    helper.fail("Entity-obstructed footprint partially placed"); return;
                }
            } finally {
                obstruction.discard();
            }
        }
        var border = level.getWorldBorder();
        var originalBorder = border.createSettings();
        try {
            border.setCenter(parent.getX() + 20, parent.getZ() + 20);
            border.setSize(4);
            if (border.isWithinBounds(parent) || item.place(context).consumesAction() || stack.getCount() != 2) {
                helper.fail("Out-of-border placement was accepted or consumed an item"); return;
            }
            for (int part = 0; part < 3; part++) if (!level.isEmptyBlock(parent.above(part))) {
                helper.fail("Out-of-border placement partially placed"); return;
            }
        } finally {
            border.applySettings(originalBorder);
        }
        // No-player callers use an empty collision context; valid placement must still work.
        var automated = new net.minecraft.world.item.context.BlockPlaceContext(level, null,
            net.minecraft.world.InteractionHand.MAIN_HAND, stack, hit);
        try {
            if (!item.place(automated).consumesAction() || stack.getCount() != 1) {
                helper.fail("Unobstructed no-player placement failed or consumed the wrong count"); return;
            }
            for (int part = 0; part < 3; part++) {
                var state = level.getBlockState(parent.above(part));
                if (!state.is(com.aranaira.arcanearchives.init.ContentRegistry.MATRIX_RESERVOIR.get())
                        || state.getValue(com.aranaira.arcanearchives.blocks.MatrixReservoir.PART) != part) {
                    helper.fail("No-player placement lost column identity"); return;
                }
            }
        } finally {
            level.removeBlock(parent, false);
        }
        helper.succeed();
    }

}
