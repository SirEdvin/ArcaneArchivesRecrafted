package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.blocks.GemCuttersTable;
import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Native collision rejection, with parent controls and cleanup even on failure. */
public final class GemCutterPlacementCollision {
    private GemCutterPlacementCollision() {}

    public static void run(GameTestHelper helper, Player player) {
        var level = helper.getLevel();
        var parent = helper.absolutePos(new BlockPos(1, 2, 1));
        var item = ContentRegistry.GEMCUTTERS_TABLE_ITEM.get();
        var failures = new java.util.ArrayList<String>();
        boolean creative = player.getAbilities().instabuild;
        float yawBefore = player.getYRot();
        try {
            player.getAbilities().instabuild = false;
            player.setPos(parent.getX() + 4, parent.getY(), parent.getZ() + 4);
            for (int yaw : new int[]{0, 90, 180, 270}) {
                player.setYRot(yaw);
                var state = ContentRegistry.GEMCUTTERS_TABLE.get().defaultBlockState()
                    .setValue(GemCuttersTable.FACING, player.getDirection().getCounterClockWise());
                var other = GemCuttersTable.connectedPos(parent, state);
                for (var occupied : new BlockPos[]{parent, other}) {
                    if (!level.isEmptyBlock(parent) || !level.isEmptyBlock(other))
                        throw new IllegalStateException("Gem Cutter collision fixture occupied");
                    var pig = EntityType.PIG.create(level);
                    if (pig == null) throw new IllegalStateException("Missing collision fixture entity");
                    pig.setPos(occupied.getX() + .5, occupied.getY(), occupied.getZ() + .5);
                    try {
                        if (!level.addFreshEntity(pig)) throw new IllegalStateException("Cannot add collision fixture");
                        var stack = new ItemStack(item, 2);
                        var original = stack.copy();
                        var context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, stack,
                            new BlockHitResult(Vec3.atCenterOf(parent), Direction.UP, parent, false));
                        var result = item.place(context);
                        if (result.consumesAction() || !ItemStack.matches(stack, original)
                                || !level.isEmptyBlock(parent) || !level.isEmptyBlock(other)
                                || level.getBlockEntity(parent) != null
                                || !level.getEntitiesOfClass(ItemEntity.class, new AABB(parent).inflate(3)).isEmpty())
                            failures.add("yaw=" + yaw + " part=" + (occupied.equals(parent) ? "parent" : "accessor")
                                + " result=" + result + " remaining=" + stack.getCount());
                    } finally {
                        pig.discard();
                        level.setBlockAndUpdate(parent, Blocks.AIR.defaultBlockState());
                        level.setBlockAndUpdate(other, Blocks.AIR.defaultBlockState());
                        level.getEntitiesOfClass(ItemEntity.class, new AABB(parent).inflate(3)).forEach(Entity::discard);
                    }
                }
                // The added child check must not reject a valid two-part placement.
                try {
                    var stack = new ItemStack(item, 2);
                    var context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, stack,
                        new BlockHitResult(Vec3.atCenterOf(parent), Direction.UP, parent, false));
                    if (!item.place(context).consumesAction() || stack.getCount() != 1
                            || level.getBlockState(parent) != state
                            || level.getBlockState(other) != state.setValue(GemCuttersTable.ACCESSOR, true))
                        failures.add("Unobstructed placement failed yaw=" + yaw);
                } finally {
                    level.setBlockAndUpdate(parent, Blocks.AIR.defaultBlockState());
                    level.setBlockAndUpdate(other, Blocks.AIR.defaultBlockState());
                    level.getEntitiesOfClass(ItemEntity.class, new AABB(parent).inflate(3)).forEach(Entity::discard);
                }
            }
        } finally {
            player.getAbilities().instabuild = creative;
            player.setYRot(yawBefore);
        }
        if (!failures.isEmpty()) throw new IllegalStateException("Gem Cutter collision rejection failed: " + failures);
    }
}
