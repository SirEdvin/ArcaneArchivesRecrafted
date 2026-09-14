package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Native update-packet contents for the HUD; not a connected client fixture. */
public final class TroveHudSync {
    public static void run(GameTestHelper helper, BiConsumer<BlockEntity, CompoundTag> load) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        require(level.isEmptyBlock(pos), "Trove HUD synchronization fixture occupied");
        try {
            level.setBlockAndUpdate(pos, ContentRegistry.RADIANT_TROVE.get().defaultBlockState());
            var live = (RadiantTroveBlockEntity) level.getBlockEntity(pos);
            require(live.troveRoutingScore(new ItemStack(Items.DIAMOND)) == -1,
                "Unassigned Trove acquired a matching routing priority");
            live.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 32768));
            require(live.upgrades().insertItem(0, new ItemStack(ContentRegistry.MATRIX_BRACE.get()), false).isEmpty(),
                "Trove HUD fixture could not install a storage upgrade");
            boolean locked = false;
            for (int slot = 0; slot < live.optionals().getSlots() && !locked; slot++)
                if (live.optionals().isItemValid(slot, new ItemStack(ContentRegistry.RADIANT_KEY.get())))
                    locked = live.optionals().insertItem(slot, new ItemStack(ContentRegistry.RADIANT_KEY.get()), false).isEmpty();
            require(locked && live.optionals().isLocked(), "Trove HUD fixture could not retain a LOCK reference");
            var replica = new RadiantTroveBlockEntity(pos, live.getBlockState());
            replica.setLevel(level); // Registry context only; this copy is not installed as live storage.
            for (int count : new int[]{32768, 0, 7}) {
                live.inventory().setStackInSlot(0, count == 0 ? ItemStack.EMPTY : new ItemStack(Items.DIAMOND, count));
                var packet = live.getUpdatePacket();
                require(packet != null && packet.getPos().equals(pos) && packet.getType() == live.getType(),
                    "Trove HUD update packet lost native block identity");
                load.accept(replica, packet.getTag());
                require(replica.inventory().getStackInSlot(0).getCount() == count
                    && replica.optionals().isLocked() && replica.lockReference().is(Items.DIAMOND)
                    && replica.lockReference().getCount() == 1
                    && replica.upgrades().getTotalUpgradesQuantity() == 1
                    && replica.optionals().getTotalUpgradesQuantity() == 1,
                    "Trove HUD update lost count, LOCK reference or upgrade quantities");
                require(live.inventory().getStackInSlot(0).getCount() == count,
                    "Trove HUD packet serialization mutated live contents");
                require(live.troveRoutingScore(new ItemStack(Items.DIAMOND)) == 4500
                    && live.troveRoutingScore(new ItemStack(Items.EMERALD)) == -1
                    && live.troveRoutingScore(ItemStack.EMPTY) == -1,
                    "Trove priority lost matching identity or empty LOCK reference");
            }
            boolean voiding = false;
            for (int slot = 0; slot < live.optionals().getSlots() && !voiding; slot++) {
                var charm = new ItemStack(ContentRegistry.DEVOURING_CHARM.get());
                if (live.optionals().isItemValid(slot, charm))
                    voiding = live.optionals().insertItem(slot, charm, false).isEmpty();
            }
            require(voiding && live.optionals().isVoiding(), "Routing fixture could not install VOID");
            require(live.troveRoutingScore(new ItemStack(Items.DIAMOND)) == 4700,
                "VOID Trove with space lost original routing preference");
            int capacity = RadiantTroveBlockEntity.capacity(new ItemStack(Items.DIAMOND), live.upgrades().getUpgradesCount());
            live.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, capacity));
            var offered = new ItemStack(Items.DIAMOND, 12);
            require(live.troveRoutingScore(offered) == 4000 && live.inventory().insertItem(0, offered, true).isEmpty()
                && live.inventory().getStackInSlot(0).getCount() == capacity && offered.getCount() == 12,
                "Full VOID Trove score or simulated conservation changed");
        } finally {
            if (level.getBlockEntity(pos) instanceof RadiantTroveBlockEntity live) {
                live.inventory().setStackInSlot(0, ItemStack.EMPTY);
                for (int slot = 0; slot < live.upgrades().getSlots(); slot++) live.upgrades().setStackInSlot(slot, ItemStack.EMPTY);
                for (int slot = 0; slot < live.optionals().getSlots(); slot++) live.optionals().setStackInSlot(slot, ItemStack.EMPTY);
            }
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
