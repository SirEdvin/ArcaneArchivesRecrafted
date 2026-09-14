package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.blocks.LecternManifest;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.ManifestMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Native acquisition, two-part placement and ordinary removal; not connected-client acceptance. */
public final class ManifestLecternLifecycle {
    public static void run(GameTestHelper helper, Player player,
            java.util.function.Function<TransientCraftingContainer, ItemStack> craft,
            java.util.function.Predicate<BlockHitResult> interact) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        var item = ContentRegistry.LECTERN_MANIFEST_ITEM.get();
        var block = ContentRegistry.LECTERN_MANIFEST.get();
        boolean creative = player.getAbilities().instabuild;
        var previousPosition = player.position();
        float yaw = player.getYRot();
        try {
            player.getAbilities().instabuild = false;
            player.setPos(pos.getX() + 4, pos.getY(), pos.getZ() + 4);
            for (int rotation : new int[]{0, 90, 180, 270}) for (int removed : new int[]{0, 1}) {
                require(level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above()), "Lectern fixture occupied");
                player.setYRot(rotation);
                var stack = new ItemStack(item, 2);
                var context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, stack,
                    new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
                require(item.place(context).consumesAction() && stack.getCount() == 1, "Lectern placement/payment failed");
                var bottom = level.getBlockState(pos);
                var top = level.getBlockState(pos.above());
                require(bottom.is(block) && top.is(block) && !bottom.getValue(LecternManifest.ACCESSOR)
                    && top.getValue(LecternManifest.ACCESSOR)
                    && bottom.getValue(LecternManifest.FACING) == player.getDirection().getCounterClockWise()
                    && top.getValue(LecternManifest.FACING) == bottom.getValue(LecternManifest.FACING),
                    "Lectern parts or original yaw placement changed");
                require(level.getBlockEntity(pos) == null && level.getBlockEntity(pos.above()) == null,
                    "Public lectern acquired a storage owner/entity");
                for (BlockPos part : new BlockPos[]{pos, pos.above()}) {
                    var hit = new BlockHitResult(Vec3.atCenterOf(part), Direction.UP, part, false);
                    require(interact.test(hit), "Lectern part interaction failed");
                }
                level.destroyBlock(pos.above(removed), true);
                require(level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above()), "Broken lectern left its companion");
                var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3));
                require(drops.stream().filter(e -> e.getItem().is(item)).mapToInt(e -> e.getItem().getCount()).sum() == 1,
                    "Lectern break duplicated/lost its item");
                drops.forEach(Entity::discard);
            }
            level.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
            var blocked = new ItemStack(item, 2);
            require(!item.place(new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, blocked,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false))).consumesAction()
                && blocked.getCount() == 2 && level.isEmptyBlock(pos) && level.getBlockState(pos.above()).is(Blocks.STONE),
                "Blocked lectern placement paid or overwrote an occupied upper block");
        } finally {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
            level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3)).forEach(Entity::discard);
            player.getAbilities().instabuild = creative;
            player.setPos(previousPosition.x, previousPosition.y, previousPosition.z);
            player.setYRot(yaw);
        }
        for (var plank : new net.minecraft.world.item.Item[]{Items.OAK_PLANKS, Items.CRIMSON_PLANKS}) {
            var matrix = new TransientCraftingContainer(new ManifestMenu(1, player.getInventory()), 3, 3);
            matrix.setItem(1, new ItemStack(ContentRegistry.MANIFEST.get()));
            for (int slot : new int[]{3, 5, 6, 8}) matrix.setItem(slot, new ItemStack(Items.STICK));
            matrix.setItem(4, new ItemStack(plank));
            var output = craft.apply(matrix);
            require(output.is(item) && output.getCount() == 1, "Original lectern recipe/tags did not produce one lectern");
            var result = new net.minecraft.world.inventory.ResultContainer();
            result.setItem(0, output);
            var resultSlot = new net.minecraft.world.inventory.ResultSlot(player, matrix, result, 0, 0, 0);
            var taken = resultSlot.remove(1);
            resultSlot.onTake(player, taken);
            require(taken.is(item) && taken.getCount() == 1 && matrix.isEmpty() && result.isEmpty(),
                "Native lectern crafting extraction did not consume exactly one recipe");
        }
        TomeAcquisitionLifecycle.run(helper, player, craft);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
