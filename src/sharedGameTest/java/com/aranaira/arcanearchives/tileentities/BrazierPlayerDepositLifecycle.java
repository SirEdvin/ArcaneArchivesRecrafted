package com.aranaira.arcanearchives.tileentities;

import com.aranaira.arcanearchives.data.PlayerSaveData;
import java.util.ArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Native source payment and inventory returns; only exceptional spawn refusal is injected. */
public final class BrazierPlayerDepositLifecycle {
    public static void run(BrazierBlockEntity brazier, RadiantChestBlockEntity chest, Player player) {
        var inventory = player.getInventory();
        var saved = inventory.items.stream().map(ItemStack::copy).toList();
        int selected = inventory.selected;
        var offhand = player.getOffhandItem().copy();
        var destination = chest.inventory();
        var stock = new ArrayList<ItemStack>();
        for (int slot = 0; slot < destination.getSlots(); slot++) stock.add(destination.getStackInSlot(slot).copy());
        var pending = PlayerSaveData.get(((ServerLevel) player.level()).getServer(), player.getUUID());
        var previous = pending.brazierPendingReturns();
        var spawned = new ArrayList<ItemEntity>();
        try {
            inventory.selected = 0;
            long now = 10000;
            for (int room : new int[]{7, 2, 0}) {
                pending.setBrazierPendingReturns(null);
                for (int slot = 0; slot < inventory.items.size(); slot++) inventory.items.set(slot, new ItemStack(Items.COBBLESTONE, 64));
                inventory.items.set(0, new ItemStack(Items.DIAMOND, 7));
                for (int slot = 0; slot < destination.getSlots(); slot++) destination.setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, destination.getSlotLimit(slot)));
                int capacity = destination.getSlotLimit(0);
                destination.setStackInSlot(0, new ItemStack(Items.DIAMOND, capacity - room));
                require(brazier.deposit(player, now, entity -> { throw new AssertionError("Ordinary player remainder unnecessarily ejected"); }), "Player deposit rejected");
                require(inventory.items.get(0).getCount() == 7 - room && destination.getStackInSlot(0).getCount() == capacity
                    && !pending.hasBrazierPendingReturns(), "Native player payment/selected-slot return lost items");
                now += 1000;
            }
            player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(Items.DIAMOND, 5));
            inventory.items.set(0, new ItemStack(Items.DIAMOND, 3));
            inventory.items.set(1, new ItemStack(Items.DIAMOND, 4));
            int repeatCapacity = destination.getSlotLimit(0);
            destination.setStackInSlot(0, new ItemStack(Items.DIAMOND, repeatCapacity - 7));
            require(brazier.deposit(player, now, entity -> { throw new AssertionError("First repeat-sequence deposit ejected"); })
                && inventory.items.get(0).isEmpty() && inventory.items.get(1).getCount() == 4
                && destination.getStackInSlot(0).getCount() == repeatCapacity - 4,
                "First paid click collected more than the selected source");
            require(brazier.deposit(player, now + 300, entity -> { throw new AssertionError("Empty-hand repeat ejected"); })
                && inventory.items.get(0).isEmpty() && inventory.items.get(1).isEmpty()
                && destination.getStackInSlot(0).getCount() == repeatCapacity && player.getOffhandItem().getCount() == 5,
                "Paid empty-hand repeat lost reference, boundary, payment or offhand exclusion");
            inventory.items.set(1, new ItemStack(Items.DIAMOND, 4));
            require(!brazier.deposit(player, now + 601, entity -> { throw new AssertionError("Expired repeat ejected"); })
                && inventory.items.get(1).getCount() == 4 && destination.getStackInSlot(0).getCount() == repeatCapacity,
                "Expired empty-hand repeat still paid matching inventory");
            inventory.items.set(1, new ItemStack(Items.COBBLESTONE, 64));
            now += 2000;
            for (boolean refusal : new boolean[]{false, true}) {
                pending.setBrazierPendingReturns(null);
                inventory.items.set(0, new ItemStack(Items.DIAMOND, 130));
                int capacity = destination.getSlotLimit(0);
                destination.setStackInSlot(0, new ItemStack(Items.DIAMOND, capacity - 2));
                require(brazier.deposit(player, now, entity -> {
                    spawned.add(entity);
                    return !refusal && player.level().addFreshEntity(entity);
                }), "Oversized player source rejected");
                require(inventory.items.get(0).getCount() == 64 && destination.getStackInSlot(0).getCount() == capacity,
                    "Overflow restored accepted items or lost selected-slot return");
                if (refusal) {
                    require(pending.decodeBrazierReturns(player.level().registryAccess()).orElseThrow().get(0).getCount() == 130 - 2 - 64,
                        "Failed ejection did not persist exact unpaid remainder");
                    require(!brazier.deposit(player, now + 100, entity -> { throw new AssertionError("Pending return spawned again"); })
                        && inventory.items.get(0).getCount() == 64, "Pending returns did not block new payment");
                    inventory.items.set(0, ItemStack.EMPTY);
                    destination.setStackInSlot(0, new ItemStack(Items.DIAMOND, capacity - 7));
                    require(brazier.deposit(player, now + 1000, entity -> { throw new AssertionError("Recovery unexpectedly spawned"); })
                        && !pending.hasBrazierPendingReturns() && inventory.items.get(0).getCount() == 130 - 2 - 64
                        && destination.getStackInSlot(0).getCount() == capacity - 7,
                        "Recovery interaction automatically routed the just-returned items");
                    require(brazier.deposit(player, now + 1100, entity -> { throw new AssertionError("Explicit post-recovery deposit ejected unexpectedly"); })
                        && inventory.items.get(0).getCount() == 130 - 2 - 64 - 7
                        && destination.getStackInSlot(0).getCount() == capacity && !pending.hasBrazierPendingReturns(),
                        "Recovered items remained blocked or were paid incorrectly on the next explicit interaction");
                    require(brazier.deposit(player, now + 1200, entity -> { throw new AssertionError("Full destination ejected ordinary remainder"); })
                        && inventory.items.get(0).getCount() == 130 - 2 - 64 - 7
                        && destination.getStackInSlot(0).getCount() == capacity && !pending.hasBrazierPendingReturns(),
                        "Post-recovery repeat duplicated payment or resurrected cleared pending returns");
                } else require(!pending.hasBrazierPendingReturns() && spawned.get(spawned.size() - 1).isAlive()
                    && spawned.get(spawned.size() - 1).getItem().getCount() == 130 - 2 - 64, "Successful ejection also queued or lost overflow");
                now += 3000;
            }
        } finally {
            spawned.forEach(ItemEntity::discard);
            pending.setBrazierPendingReturns(previous);
            for (int slot = 0; slot < saved.size(); slot++) inventory.items.set(slot, saved.get(slot));
            inventory.selected = selected;
            player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, offhand);
            for (int slot = 0; slot < stock.size(); slot++) destination.setStackInSlot(slot, stock.get(slot));
        }
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
