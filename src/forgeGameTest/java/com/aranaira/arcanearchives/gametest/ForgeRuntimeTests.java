package com.aranaira.arcanearchives.gametest;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.reporting.legacy.xml.LegacyXmlReportGeneratingListener;

/** Runs the existing JUnit assertions after Forge transformation, mod registration and world startup. */
@GameTestHolder("arcanearchives_test")
@PrefixGameTestTemplate(false)
public final class ForgeRuntimeTests {
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void deviceOwnership(GameTestHelper helper) {
        DeviceOwnershipLifecycle.run(helper, helper.makeMockPlayer(),
            net.minecraftforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "Ownership fixture")),
            entity -> entity.saveWithFullMetadata(), (entity, tag) -> entity.load(tag),
            (stack, tag) -> stack.getOrCreateTag().put("BlockEntityTag", tag.copy()));
    }
    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void matrixDistillateLifecycle(GameTestHelper helper) {
        MatrixDistillateLifecycle.run(helper, helper.makeMockPlayer(),
            (stack, tag) -> stack.getOrCreateTag().put("BlockEntityTag", tag.copy()));
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void matrixReservoirCrafting(GameTestHelper helper) {
        MatrixReservoirCrafting.run(helper, helper.makeMockPlayer(), helper.makeMockPlayer());
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void matrixReservoirPlacement(GameTestHelper helper) {
        var level = helper.getLevel();
        var parent = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        var block = com.aranaira.arcanearchives.init.ContentRegistry.MATRIX_RESERVOIR.get();
        var item = com.aranaira.arcanearchives.init.ContentRegistry.MATRIX_RESERVOIR_ITEM.get();
        var player = helper.makeMockPlayer();
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
            var stateTag = new net.minecraft.nbt.CompoundTag();
            stateTag.putString("part", "2");
            stack.getOrCreateTag().put("BlockStateTag", stateTag);
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

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void matrixReservoirPlacementCancellation(GameTestHelper helper) {
        var level = helper.getLevel();
        var parent = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        var player = helper.makeMockPlayer();
        player.setPos(parent.getX() + 4, parent.getY(), parent.getZ() + 4);
        var item = com.aranaira.arcanearchives.init.ContentRegistry.MATRIX_RESERVOIR_ITEM.get();
        var stack = new net.minecraft.world.item.ItemStack(item, 2);
        stack.getOrCreateTag().putString("fixture", "must survive cancellation");
        var original = stack.copy();
        var water = net.minecraft.world.level.block.Blocks.WATER.defaultBlockState();
        for (int part = 0; part < 3; part++)
            level.setBlock(parent.above(part), water, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        var context = new net.minecraft.world.item.context.UseOnContext(level, player,
            net.minecraft.world.InteractionHand.MAIN_HAND, stack,
            new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(parent),
                net.minecraft.core.Direction.UP, parent, false));
        var captured = new java.util.concurrent.atomic.AtomicInteger();
        java.util.function.Consumer<net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent> cancel = event -> {
            if (event.getEntity() == player) {
                captured.set(event instanceof net.minecraftforge.event.level.BlockEvent.EntityMultiPlaceEvent multi
                    ? multi.getReplacedBlockSnapshots().size() : 1);
                event.setCanceled(true);
            }
        };
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(cancel);
        try {
            var result = net.minecraftforge.common.ForgeHooks.onPlaceItemIntoWorld(context);
            if (result != net.minecraft.world.InteractionResult.FAIL || captured.get() != 3
                    || !net.minecraft.world.item.ItemStack.matches(stack, original)) {
                helper.fail("Cancelled native multi-place did not preserve its item or capture all parts"); return;
            }
            for (int part = 0; part < 3; part++) if (level.getBlockState(parent.above(part)) != water) {
                helper.fail("Cancelled placement did not restore a replaced fluid block"); return;
            }
            if (level.captureBlockSnapshots || level.restoringBlockSnapshots || !level.capturedBlockSnapshots.isEmpty()) {
                helper.fail("Cancellation leaked Forge snapshot state"); return;
            }
            if (!level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                    new net.minecraft.world.phys.AABB(parent).inflate(3), entity -> entity.getItem().is(item)).isEmpty()) {
                helper.fail("Cancelled placement duplicated a reservoir item drop"); return;
            }
            for (int part = 0; part < 3; part++) level.setBlock(parent.above(part),
                net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            MatrixDistillateCancellation.run(helper, player, parent,
                net.minecraftforge.common.ForgeHooks::onPlaceItemIntoWorld, captured,
                () -> !level.captureBlockSnapshots && !level.restoringBlockSnapshots && level.capturedBlockSnapshots.isEmpty());
            DevicePlacementCancellation.run(helper, player, parent,
                net.minecraftforge.common.ForgeHooks::onPlaceItemIntoWorld, captured,
                () -> !level.captureBlockSnapshots && !level.restoringBlockSnapshots && level.capturedBlockSnapshots.isEmpty(),
                (carrier, tag) -> carrier.getOrCreateTag().put("BlockEntityTag", tag.copy()));
        } finally {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(cancel);
            for (int part = 0; part < 3; part++) level.removeBlock(parent.above(part), false);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void matrixReservoirInterruptedPlacement(GameTestHelper helper) {
        var level = helper.getLevel();
        var parent = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        var player = helper.makeMockPlayer();
        player.setPos(parent.getX() + 4, parent.getY(), parent.getZ() + 4);
        var item = com.aranaira.arcanearchives.init.ContentRegistry.MATRIX_RESERVOIR_ITEM.get();
        var fault = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(PlacementInterruptionBlock.ID);
        if (!(fault instanceof PlacementInterruptionBlock)) {
            helper.fail("Placement interruption fixture was not registered"); return;
        }
        var water = net.minecraft.world.level.block.Blocks.WATER.defaultBlockState();
        for (int failedPart = 0; failedPart < 3; failedPart++) {
            var stack = new net.minecraft.world.item.ItemStack(item, 2);
            stack.getOrCreateTag().putString("fixture", "must survive interrupted placement");
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

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void matrixReservoirPlacementPreflight(GameTestHelper helper) {
        var level = helper.getLevel();
        var parent = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        var item = com.aranaira.arcanearchives.init.ContentRegistry.MATRIX_RESERVOIR_ITEM.get();
        var stack = new net.minecraft.world.item.ItemStack(item, 2);
        var player = helper.makeMockPlayer();
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

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void sharedAssertions(GameTestHelper helper) {
        var roots = Arrays.stream(System.getProperty("arcanearchives.test.classes").split(File.pathSeparator))
            .map(Path::of).collect(Collectors.toSet());
        var request = LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectClasspathRoots(roots))
            .configurationParameter("junit.jupiter.execution.parallel.enabled", "false")
            .build();
        var summary = new SummaryGeneratingListener();
        var output = new PrintWriter(System.out, true);
        var reports = new LegacyXmlReportGeneratingListener(Path.of(System.getProperty("arcanearchives.test.reports")), output);
        var config = LauncherConfig.builder().enableTestEngineAutoRegistration(false)
            .addTestEngines(new JupiterTestEngine()).build();
        Thread thread = Thread.currentThread();
        ClassLoader previous = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(ForgeRuntimeTests.class.getClassLoader());
            LauncherFactory.create(config).execute(request, summary, reports);
        } finally {
            thread.setContextClassLoader(previous);
        }
        var result = summary.getSummary();
        result.printTo(output);
        result.printFailuresTo(output);
        if (result.getTestsFoundCount() == 0 || result.getTotalFailureCount() != 0
                || result.getTestsSucceededCount() != result.getTestsFoundCount()) {
            helper.fail("Shared Forge assertions failed or were not executed; inspect forge-runtime XML reports");
            return;
        }
        helper.succeed();
    }
}
