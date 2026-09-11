package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.blocks.GemCuttersTable;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.recipe.gct.GemCutterCraftingState;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Real loader cancellation, including populated and input/output inventory item data. */
public final class DevicePlacementCancellation {
    private DevicePlacementCancellation() {}

    public static void run(GameTestHelper helper, Player player, BlockPos parent,
            Function<UseOnContext, InteractionResult> place, AtomicInteger captured,
            BooleanSupplier snapshotsClean, BiConsumer<ItemStack, CompoundTag> itemData) {
        var level = helper.getLevel();
        var item = ContentRegistry.GEMCUTTERS_TABLE_ITEM.get();
        var water = Blocks.WATER.defaultBlockState();
        boolean creative = player.getAbilities().instabuild;
        try {
            player.getAbilities().instabuild = false;
            for (int yaw : new int[]{0, 90, 180, 270})
            for (boolean paid : new boolean[]{false, true}) {
                player.setYRot(yaw);
                var state = ContentRegistry.GEMCUTTERS_TABLE.get().defaultBlockState()
                    .setValue(GemCuttersTable.FACING, player.getDirection().getCounterClockWise());
                var other = GemCuttersTable.connectedPos(parent, state);
                require(level.isEmptyBlock(parent) && level.isEmptyBlock(other), "Device cancellation fixture occupied");
                var crafting = new GemCutterCraftingState();
                crafting.setInput(0, new ItemStack(Items.DIAMOND, 3));
                crafting.setInput(17, new ItemStack(Items.EMERALD, 7));
                if (paid) crafting.setOutput(com.aranaira.arcanearchives.recipe.CraftingCreator.withCreator(
                    new ItemStack(Items.PAPER, 4), new UUID(0, 2), "Output creator"));
                var data = new CompoundTag();
                data.putString("id", "arcanearchives:gemcutters_table");
                data.putUUID("network_owner", new UUID(0, 1));
                data.put("Crafting", crafting.serializeNBT(level.registryAccess()));
                var stack = new ItemStack(item);
                itemData.accept(stack, data);
                var original = stack.copy();
                captured.set(0);
                try {
                    for (var pos : List.of(parent, other)) level.setBlock(pos, water, Block.UPDATE_CLIENTS);
                    var context = new UseOnContext(level, player, InteractionHand.MAIN_HAND, stack,
                        new BlockHitResult(Vec3.atCenterOf(parent), Direction.UP, parent, false));
                    require(place.apply(context) == InteractionResult.FAIL && captured.get() == 2,
                        "Native device cancellation did not capture both parts");
                    require(ItemStack.matches(stack, original), "Cancellation consumed or modified the populated carrier");
                    for (var pos : List.of(parent, other)) {
                        require(level.getBlockState(pos) == water, "Device cancellation lost replaced water");
                        require(level.getBlockEntity(pos) == null, "Cancelled device retained an owned entity");
                    }
                    require(snapshotsClean.getAsBoolean(), "Device cancellation leaked snapshot state");
                    require(level.getEntitiesOfClass(ItemEntity.class, new AABB(parent).inflate(4)).isEmpty(),
                        "Cancelled device duplicated its carrier, inputs or completed output");
                } finally {
                    for (var pos : List.of(parent, other)) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                    level.getEntitiesOfClass(ItemEntity.class, new AABB(parent).inflate(4)).forEach(Entity::discard);
                }
            }
            crystal(helper, player, parent, place, captured, snapshotsClean, itemData);
        } finally {
            player.getAbilities().instabuild = creative;
        }
    }

    private static void crystal(GameTestHelper helper, Player player, BlockPos pos,
            Function<UseOnContext, InteractionResult> place, AtomicInteger captured,
            BooleanSupplier snapshotsClean, BiConsumer<ItemStack, CompoundTag> itemData) {
        var level = helper.getLevel();
        var water = Blocks.WATER.defaultBlockState();
        for (Direction face : Direction.values()) {
            require(level.isEmptyBlock(pos), "Crystal cancellation fixture occupied");
            var stack = new ItemStack(ContentRegistry.MONITORING_CRYSTAL_ITEM.get(), 2);
            var data = new CompoundTag();
            data.putString("id", "arcanearchives:monitoring_crystal");
            data.putUUID("network_owner", new UUID(0, 1));
            itemData.accept(stack, data);
            var original = stack.copy();
            captured.set(0);
            try {
                level.setBlock(pos, water, Block.UPDATE_CLIENTS);
                var context = new UseOnContext(level, player, InteractionHand.MAIN_HAND, stack,
                    new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false));
                require(place.apply(context) == InteractionResult.FAIL && captured.get() == 1,
                    "Native Crystal cancellation did not capture one position: " + face);
                require(ItemStack.matches(stack, original), "Crystal cancellation changed item count/ownership data");
                require(level.getBlockState(pos) == water && level.getBlockEntity(pos) == null,
                    "Crystal cancellation failed to restore unowned water");
                require(snapshotsClean.getAsBoolean(), "Crystal cancellation leaked snapshot state");
                require(level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4)).isEmpty(),
                    "Crystal cancellation duplicated its item");
            } finally {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(4)).forEach(Entity::discard);
            }
        }
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new IllegalStateException(message);
    }
}
