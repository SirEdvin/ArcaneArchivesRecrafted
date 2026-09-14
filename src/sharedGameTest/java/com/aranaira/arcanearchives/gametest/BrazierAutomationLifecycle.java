package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.BrazierItemAutomation;
import com.aranaira.arcanearchives.tileentities.BrazierBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Native capability lookup and manually advanced native hopper ticks; not world-loop scheduling. */
public final class BrazierAutomationLifecycle {
    public static void run(GameTestHelper helper, net.minecraft.world.entity.player.Player player,
            java.util.function.BiFunction<BrazierBlockEntity, Direction, BrazierItemAutomation> lookup) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        var destination = pos.east();
        require(level.isEmptyBlock(pos) && level.isEmptyBlock(destination), "Automation fixture occupied");
        try {
            level.setBlockAndUpdate(pos, ContentRegistry.BRAZIER.get().defaultBlockState());
            level.setBlockAndUpdate(destination, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
            var brazier = (BrazierBlockEntity) level.getBlockEntity(pos);
            var chest = (RadiantChestBlockEntity) level.getBlockEntity(destination);
            var owner = player.getUUID();
            chest.setOwner(owner);
            var retained = lookup.apply(brazier, null);
            require(retained != null && retained.insertItem(0, new ItemStack(Items.DIAMOND, 7), false).getCount() == 7,
                "Ownerless automation consumed items");
            brazier.recordPlacer(player);
            for (int side = -1; side < Direction.values().length; side++) {
                var handler = lookup.apply(brazier, side < 0 ? null : Direction.values()[side]);
                require(handler != null, "Missing sided/unsided automation capability");
                verify(brazier, chest, handler);
            }

            var detached = new BrazierBlockEntity(pos, brazier.getBlockState());
            detached.setLevel(level);
            detached.recordPlacer(player);
            require(new BrazierItemAutomation(detached).insertItem(0, new ItemStack(Items.DIAMOND, 7), false).getCount() == 7,
                "Detached automation gained routing authority");
            level.removeBlock(pos, false);
            require(retained.insertItem(0, new ItemStack(Items.DIAMOND, 7), false).getCount() == 7,
                "Retained removed-device capability consumed items");
            level.setBlockAndUpdate(pos, ContentRegistry.BRAZIER.get().defaultBlockState());
            var replacement = (BrazierBlockEntity) level.getBlockEntity(pos);
            replacement.recordPlacer(player);
            require(replacement != brazier && retained.insertItem(0, new ItemStack(Items.DIAMOND, 7), false).getCount() == 7
                && chest.inventory().getStackInSlot(0).isEmpty(), "Old capability gained authority from same-owner replacement");
            var fresh = lookup.apply(replacement, null);
            require(fresh != null && fresh != retained && fresh.insertItem(0, new ItemStack(Items.DIAMOND, 7), false).isEmpty()
                && chest.inventory().getStackInSlot(0).getCount() == 7, "Fresh replacement capability failed exact transfer");
            chest.inventory().setStackInSlot(0, ItemStack.EMPTY);
        } finally {
            level.removeBlock(pos, false);
            level.removeBlock(destination, false);
        }
    }

    public static void verifyHopper(BrazierBlockEntity brazier, RadiantChestBlockEntity chest) {
        var level = brazier.getLevel();
        var pos = brazier.getBlockPos().above();
        require(level.isEmptyBlock(pos), "Hopper fixture occupied");
        var inventory = chest.inventory();
        var previous = new java.util.ArrayList<ItemStack>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) previous.add(inventory.getStackInSlot(slot).copy());
        try {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.HOPPER.defaultBlockState()
                .setValue(net.minecraft.world.level.block.HopperBlock.FACING, Direction.DOWN));
            var hopper = (net.minecraft.world.level.block.entity.HopperBlockEntity) level.getBlockEntity(pos);
            for (int room : new int[]{7, 2, 0}) {
                for (int slot = 0; slot < inventory.getSlots(); slot++)
                    inventory.setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, inventory.getStackLimit(slot, new ItemStack(Items.COBBLESTONE))));
                int capacity = inventory.getStackLimit(0, new ItemStack(Items.DIAMOND));
                inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, capacity - room));
                hopper.clearContent();
                hopper.setItem(0, new ItemStack(Items.DIAMOND, 7));
                for (int tick = 0; tick < 80; tick++)
                    net.minecraft.world.level.block.entity.HopperBlockEntity.pushItemsTick(level, pos, level.getBlockState(pos), hopper);
                require(hopper.getItem(0).getCount() == 7 - room && inventory.getStackInSlot(0).getCount() == capacity,
                    "Native hopper payment lost or duplicated items at capacity " + room);
                for (int slot = 1; slot < hopper.getContainerSize(); slot++)
                    require(hopper.getItem(slot).isEmpty(), "Native hopper created extra source stacks");
            }
            hopper.clearContent();
        } finally {
            if (level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.HopperBlockEntity hopper) hopper.clearContent();
            level.removeBlock(pos, false);
            for (int slot = 0; slot < previous.size(); slot++) inventory.setStackInSlot(slot, previous.get(slot));
        }
    }

    public static void verify(BrazierBlockEntity brazier, RadiantChestBlockEntity chest, BrazierItemAutomation handler) {
        var inventory = chest.inventory();
        var previous = new java.util.ArrayList<ItemStack>();
        for (int slot = 0; slot < inventory.getSlots(); slot++) previous.add(inventory.getStackInSlot(slot).copy());
        try {
            require(handler.getSlots() == 999 && handler.getSlotLimit(998) == 999, "Virtual slot contract changed");
            for (int room : new int[]{7, 2, 0}) {
                for (int slot = 0; slot < inventory.getSlots(); slot++)
                    inventory.setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, inventory.getStackLimit(slot, new ItemStack(Items.COBBLESTONE))));
                int capacity = inventory.getStackLimit(0, new ItemStack(Items.DIAMOND));
                inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, capacity - room));
                var source = new ItemStack(Items.DIAMOND, 7);
                for (int repeat = 0; repeat < 2; repeat++) {
                    var simulated = handler.insertItem(998, source, true);
                    require(simulated.getCount() == 7 - room && inventory.getStackInSlot(0).getCount() == capacity - room
                        && source.getCount() == 7, "Simulation mutated stock/source or double-counted capacity");
                }
                var remainder = handler.insertItem(0, source, false);
                require(remainder.getCount() == 7 - room && source.getCount() == 7
                    && inventory.getStackInSlot(0).getCount() == capacity, "Automation failed exact transfer conservation");
                source.shrink(source.getCount() - remainder.getCount());
                require(source.getCount() == 7 - room, "Caller remainder payment was incorrect");
                if (!remainder.isEmpty()) remainder.setCount(1);
                require(source.getCount() == 7 - room && inventory.getStackInSlot(0).getCount() == capacity,
                    "Automation remainder aliased source or destination");
                require(handler.getStackInSlot(0).isEmpty() && handler.getStackInSlot(998).isEmpty()
                    && handler.extractItem(0, 64, false).isEmpty() && handler.extractItem(998, 64, true).isEmpty(),
                    "Virtual automation exposed a buffer or extraction");
            }
        } finally {
            for (int slot = 0; slot < previous.size(); slot++) inventory.setStackInSlot(slot, previous.get(slot));
        }
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
