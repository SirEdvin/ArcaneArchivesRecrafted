package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.blocks.MatrixDistillate;
import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Shared assertions; loader wrappers supply the real native hook and player-scoped cancellation listener. */
public final class MatrixDistillateCancellation {
    private MatrixDistillateCancellation() {}
    private static void require(boolean ok, String message) { if (!ok) throw new IllegalStateException(message); }

    public static void run(GameTestHelper helper, Player player, BlockPos parent,
            Function<UseOnContext, InteractionResult> place, AtomicInteger captured, BooleanSupplier snapshotsClean) {
        var level = helper.getLevel();
        var item = ContentRegistry.MATRIX_DISTILLATE_ITEM.get();
        var water = Blocks.WATER.defaultBlockState();
        for (int yaw : new int[]{0, 90, 180, 270}) {
            player.setYRot(yaw);
            var state = ContentRegistry.MATRIX_DISTILLATE.get().defaultBlockState()
                .setValue(MatrixDistillate.FACING, player.getDirection().getCounterClockWise());
            var positions = MatrixDistillate.footprint(parent, state);
            require(positions.stream().allMatch(level::isEmptyBlock), "Cancellation fixture occupied");
            var stack = com.aranaira.arcanearchives.recipe.CraftingCreator.withCreator(
                new ItemStack(item, 2), new UUID(0, 1), "Cancelled placement owner");
            var original = stack.copy();
            captured.set(0);
            try {
                for (BlockPos pos : positions) level.setBlock(pos, water, Block.UPDATE_CLIENTS);
                var context = new UseOnContext(level, player, InteractionHand.MAIN_HAND, stack,
                    new BlockHitResult(Vec3.atCenterOf(parent), Direction.UP, parent, false));
                require(place.apply(context) == InteractionResult.FAIL && captured.get() == positions.size(),
                    "Native Distillate cancellation did not capture all nine positions");
                require(ItemStack.matches(stack, original), "Cancellation changed Distillate item count/data");
                for (BlockPos pos : positions) {
                    require(level.getBlockState(pos) == water, "Cancellation did not restore water");
                    require(level.getBlockEntity(pos) == null, "Cancellation left a Matrix part entity");
                }
                require(snapshotsClean.getAsBoolean(), "Cancellation leaked native snapshot state");
                require(level.getEntitiesOfClass(ItemEntity.class, new AABB(parent).inflate(4),
                    e -> e.getItem().is(item)).isEmpty(), "Cancellation produced a Distillate item drop");
            } finally {
                for (BlockPos pos : positions) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }
}
