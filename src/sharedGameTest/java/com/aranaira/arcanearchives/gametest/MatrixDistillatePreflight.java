package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.blocks.MatrixDistillate;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.recipe.CraftingCreator;
import com.aranaira.arcanearchives.tileentities.MatrixPartBlockEntity;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Full loaded-footprint rejection and a no-player positive control; no synthetic chunk lifecycle. */
public final class MatrixDistillatePreflight {
    private MatrixDistillatePreflight() {}
    private static void require(boolean ok, String message) { if (!ok) throw new IllegalStateException(message); }

    private static BlockPlaceContext context(GameTestHelper helper, Player player, BlockPos parent, ItemStack stack) {
        return new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack,
            new BlockHitResult(Vec3.atCenterOf(parent), Direction.UP, parent, false));
    }

    private static void rejected(GameTestHelper helper, Player player, BlockPos parent, List<BlockPos> positions, String reason) {
        var level = helper.getLevel();
        require(positions.stream().filter(pos -> !level.isOutsideBuildHeight(pos)).allMatch(level::isEmptyBlock),
            "Preflight space occupied before " + reason);
        var stack = CraftingCreator.withCreator(new ItemStack(ContentRegistry.MATRIX_DISTILLATE_ITEM.get(), 2),
            new UUID(0, 1), "Preflight owner");
        var original = stack.copy();
        require(!ContentRegistry.MATRIX_DISTILLATE_ITEM.get().place(context(helper, player, parent, stack)).consumesAction()
            && ItemStack.matches(stack, original), "Rejected placement modified its item: " + reason);
        for (BlockPos pos : positions) if (!level.isOutsideBuildHeight(pos))
            require(level.isEmptyBlock(pos) && level.getBlockEntity(pos) == null, "Rejected placement left a part: " + reason);
        require(level.getEntitiesOfClass(ItemEntity.class, new AABB(parent).inflate(4),
            e -> e.getItem().is(ContentRegistry.MATRIX_DISTILLATE_ITEM.get())).isEmpty(), "Rejected placement dropped an item: " + reason);
    }

    public static void run(GameTestHelper helper, Player player, BlockPos parent) {
        var level = helper.getLevel();
        var block = ContentRegistry.MATRIX_DISTILLATE.get();
        player.setPos(parent.getX() + 4, parent.getY(), parent.getZ() + 4);
        for (int yaw : new int[]{0, 90, 180, 270}) {
            player.setYRot(yaw);
            var state = block.defaultBlockState().setValue(MatrixDistillate.FACING, player.getDirection().getCounterClockWise());
            var positions = MatrixDistillate.footprint(parent, state);
            for (int part = 0; part < positions.size(); part++) {
                var obstruction = EntityType.PIG.create(level);
                require(obstruction != null, "Could not create collision fixture");
                BlockPos pos = positions.get(part);
                obstruction.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                require(level.addFreshEntity(obstruction), "Could not add collision fixture");
                try { rejected(helper, player, parent, positions, "collision yaw=" + yaw + " part=" + part); }
                finally { obstruction.discard(); }
            }
            boolean mayBuild = player.getAbilities().mayBuild;
            try {
                player.getAbilities().mayBuild = false;
                rejected(helper, player, parent, positions, "build permission yaw=" + yaw);
            } finally { player.getAbilities().mayBuild = mayBuild; }
            var border = level.getWorldBorder();
            var originalBorder = border.createSettings();
            try {
                // Root remains permitted; reject because another occupied part is outside.
                border.setCenter(parent.getX() + 0.5, parent.getZ() + 0.5);
                border.setSize(1);
                require(border.isWithinBounds(parent) && positions.stream().anyMatch(pos -> !border.isWithinBounds(pos)),
                    "Border fixture did not isolate a child part");
                rejected(helper, player, parent, positions, "child outside world border yaw=" + yaw);
            } finally { border.applySettings(originalBorder); }
            BlockPos high = new BlockPos(parent.getX(), level.getMaxBuildHeight() - 2, parent.getZ());
            rejected(helper, player, high, MatrixDistillate.footprint(high, state), "top exceeds build height yaw=" + yaw);
        }
        // Same native item API, no player: collision uses an empty context and all parts share one identity.
        var stack = new ItemStack(ContentRegistry.MATRIX_DISTILLATE_ITEM.get(), 2);
        var automatic = context(helper, null, parent, stack);
        var expected = block.getStateForPlacement(automatic);
        require(expected != null, "No-player positive-control state missing");
        var positions = MatrixDistillate.footprint(parent, expected);
        try {
            require(ContentRegistry.MATRIX_DISTILLATE_ITEM.get().place(automatic).consumesAction() && stack.getCount() == 1,
                "No-player placement failed or consumed the wrong count");
            require(level.getBlockEntity(parent) instanceof MatrixPartBlockEntity, "No-player root missing");
            UUID identity = ((MatrixPartBlockEntity) level.getBlockEntity(parent)).identity();
            for (int part = 0; part < positions.size(); part++) {
                BlockPos pos = positions.get(part);
                require(level.getBlockState(pos) == expected.setValue(MatrixDistillate.PART, part)
                    && level.getBlockEntity(pos) instanceof MatrixPartBlockEntity entity && identity.equals(entity.identity()),
                    "No-player placement lost facing, part or structure identity");
            }
        } finally { level.removeBlock(parent, false); }
        require(positions.stream().allMatch(level::isEmptyBlock), "No-player cleanup left matching parts");
    }
}
