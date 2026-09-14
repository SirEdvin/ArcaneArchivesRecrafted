package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.BrazierItemAutomation;
import com.aranaira.arcanearchives.tileentities.BrazierBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

public final class BrazierPullLifecycle {
    public static void run(GameTestHelper helper, Player player, Function<BlockEntity, CompoundTag> save,
            BiConsumer<BlockEntity, CompoundTag> load) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 3, 1));
        var sourcePos = pos.east();
        var hopperPos = pos.below();
        for (var p : new BlockPos[]{pos, sourcePos, hopperPos}) require(level.isEmptyBlock(p), "Occupied pull fixture");
        var hand = player.getMainHandItem().copy();
        boolean shift = player.isShiftKeyDown();
        var position = player.position();
        try {
            player.setPos(pos.getX(), pos.getY(), pos.getZ() + 2);
            player.setShiftKeyDown(false);
            level.setBlockAndUpdate(pos, ContentRegistry.BRAZIER.get().defaultBlockState());
            var brazier = (BrazierBlockEntity) level.getBlockEntity(pos);
            brazier.recordPlacer(player);
            level.setBlockAndUpdate(sourcePos, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
            var chest = (RadiantChestBlockEntity) level.getBlockEntity(sourcePos);
            chest.setOwner(player.getUUID());
            chest.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 100));
            var scepter = ContentRegistry.SCEPTER_MANIPULATION.get();
            scepter.interact(player, InteractionHand.MAIN_HAND, level, pos);
            require(brazier.pull().enabled(), "Scepter did not switch mode");
            brazier.pull().tick();
            require(brazier.pull().stack().isEmpty() && chest.inventory().getStackInSlot(0).getCount() == 100, "Filterless extraction");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND, 12));
            brazier.deposit(player);
            require(player.getMainHandItem().getCount() == 12 && brazier.pull().filter().getCount() == 1, "Filter consumed source");
            require(brazier.insert(new ItemStack(Items.DIAMOND), false).getCount() == 1, "Pull mode accepted deposit");
            chest.setOwner(java.util.UUID.randomUUID());
            brazier.pull().tick();
            require(brazier.pull().stack().isEmpty(), "Pulled from unauthorized owner");
            chest.setOwner(player.getUUID());
            brazier.configure(0, false); brazier.pull().tick();
            require(brazier.pull().stack().isEmpty(), "Pulled outside radius");
            brazier.configure(150, false); brazier.pull().tick();
            require(brazier.pull().stack().getCount() == 64 && chest.inventory().getStackInSlot(0).getCount() == 36, "Pull failed conservation");
            brazier.pull().tick();
            require(chest.inventory().getStackInSlot(0).getCount() == 36, "Full buffer kept extracting");
            var restored = new BrazierBlockEntity(pos, brazier.getBlockState()); restored.setLevel(level);
            load.accept(restored, save.apply(brazier));
            require(restored.pull().enabled() && restored.pull().filter().is(Items.DIAMOND) && restored.pull().stack().getCount() == 64, "Pull state did not persist");
            player.setShiftKeyDown(true); player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            brazier.deposit(player);
            require(brazier.pull().filter().isEmpty() && brazier.pull().stack().getCount() == 64, "Clearing filter lost output");
            player.setShiftKeyDown(false);
            scepter.interact(player, InteractionHand.MAIN_HAND, level, pos);
            require(!brazier.pull().enabled(), "Scepter did not restore deposit mode");
            // Real native hopper tick exercises Fabric Transfer / Forge and NeoForge capabilities.
            level.setBlockAndUpdate(hopperPos, Blocks.HOPPER.defaultBlockState());
            var hopper = (HopperBlockEntity) level.getBlockEntity(hopperPos);
            var ticker = ((EntityBlock) Blocks.HOPPER).getTicker(level, hopper.getBlockState(), BlockEntityType.HOPPER);
            ticker.tick(level, hopperPos, hopper.getBlockState(), hopper);
            int received = 0;
            for (int i = 0; i < hopper.getContainerSize(); i++) received += hopper.getItem(i).getCount();
            require(received > 0 && received + brazier.pull().stack().getCount() == 64, "Native hopper failed buffered extraction");
            hopper.clearContent();
            var automation = new BrazierItemAutomation(brazier);
            int remaining = brazier.pull().stack().getCount();
            require(automation.extractItem(0, 7, true).getCount() == 7 && brazier.pull().stack().getCount() == remaining, "Simulation mutated buffer");
            require(automation.extractItem(0, 64, false).getCount() == remaining && brazier.pull().stack().isEmpty(), "Output drain failed");
            chest.inventory().setStackInSlot(0, ItemStack.EMPTY);
            level.setBlockAndUpdate(sourcePos, ContentRegistry.RADIANT_TROVE.get().defaultBlockState());
            var trove = (RadiantTroveBlockEntity) level.getBlockEntity(sourcePos);
            trove.setOwner(player.getUUID()); trove.inventory().setStackInSlot(0, new ItemStack(Items.EMERALD, 9));
            scepter.interact(player, InteractionHand.MAIN_HAND, level, pos);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.EMERALD)); brazier.deposit(player);
            brazier.pull().tick();
            require(trove.inventory().getStackInSlot(0).isEmpty() && brazier.pull().stack().getCount() == 9, "Trove extraction failed");
            var before = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(1));
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            var drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(1));
            drops.removeAll(before);
            require(drops.stream().filter(e -> e.getItem().is(Items.EMERALD)).mapToInt(e -> e.getItem().getCount()).sum() == 9, "Removal lost or duplicated buffer");
            drops.forEach(net.minecraft.world.entity.Entity::discard);
        } finally {
            for (var p : new BlockPos[]{pos, sourcePos, hopperPos}) level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
            player.setItemInHand(InteractionHand.MAIN_HAND, hand); player.setShiftKeyDown(shift); player.setPos(position.x, position.y, position.z);
        }
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
