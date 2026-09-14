package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.BrazierPlayerSelection;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Actual native player slots and registered item capability providers, without source payment. */
public final class BrazierPlayerSelectionLifecycle {
    public static void run(Player player, Player other,
            java.util.function.BiConsumer<ItemStack, net.minecraft.nbt.CompoundTag> itemData) {
        var inventory = player.getInventory();
        var saved = inventory.items.stream().map(ItemStack::copy).toList();
        var offhand = player.getOffhandItem().copy();
        int selected = inventory.selected;
        boolean creative = player.getAbilities().instabuild;
        try {
            for (int slot = 0; slot < inventory.items.size(); slot++) inventory.items.set(slot, ItemStack.EMPTY);
            inventory.selected = 2;
            inventory.items.set(2, new ItemStack(Items.DIAMOND, 3));
            inventory.items.set(0, new ItemStack(Items.DIAMOND, 7));
            player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(Items.DIAMOND, 5));
            var distinct = new ItemStack(Items.DIAMOND, 7);
            var data = new net.minecraft.nbt.CompoundTag();
            data.putString("id", "minecraft:chest");
            data.putString("brazier_fixture", "different data");
            itemData.accept(distinct, data);
            var pending = new com.aranaira.arcanearchives.data.PlayerSaveData();
            var large = distinct.copy();
            large.setCount(Integer.MAX_VALUE);
            pending.queueBrazierReturns(List.of(new ItemStack(Items.EMERALD, 130), large), player.level().registryAccess());
            var decoded = pending.decodeBrazierReturns(player.level().registryAccess()).orElseThrow();
            require(decoded.size() == 2 && decoded.get(0).getCount() == 130 && ItemStack.matches(decoded.get(1), large),
                "Pending return codec truncated count, changed item data or reordered the batch");
            decoded.get(1).setCount(1);
            require(pending.decodeBrazierReturns(player.level().registryAccess()).orElseThrow().get(1).getCount() == Integer.MAX_VALUE,
                "Decoded pending return aliased saved recovery state");
            var payload = (net.minecraft.nbt.CompoundTag) pending.brazierPendingReturns();
            pending.setDirty(false);
            boolean refusedOverwrite = false;
            try { pending.queueBrazierReturns(List.of(new ItemStack(Items.COAL)), player.level().registryAccess()); }
            catch (IllegalStateException expected) { refusedOverwrite = true; }
            require(refusedOverwrite && payload.equals(pending.brazierPendingReturns()) && !pending.isDirty(),
                "Another failed batch overwrote or dirtied existing pending returns");
            var freshPending = new com.aranaira.arcanearchives.data.PlayerSaveData();
            boolean refusedPartial = false;
            try { freshPending.queueBrazierReturns(List.of(distinct, ItemStack.EMPTY), player.level().registryAccess()); }
            catch (IllegalArgumentException expected) { refusedPartial = true; }
            require(refusedPartial && !freshPending.hasBrazierPendingReturns() && !freshPending.isDirty(),
                "Failed later entry encoding published an incomplete pending batch");
            for (int invalidCount : new int[]{0, -1}) {
                var broken = payload.copy();
                broken.getList("stacks", 10).getCompound(1).putInt("count", invalidCount);
                pending.setBrazierPendingReturns(broken);
                require(pending.decodeBrazierReturns(player.level().registryAccess()).isEmpty()
                    && pending.brazierPendingReturns().equals(broken), "Invalid later entry partially decoded or erased pending items");
            }
            var unknown = payload.copy();
            unknown.getList("stacks", 10).getCompound(1).getCompound("item").putString("id", "arcanearchives:missing_return_fixture");
            pending.setBrazierPendingReturns(unknown);
            require(pending.decodeBrazierReturns(player.level().registryAccess()).isEmpty()
                && pending.brazierPendingReturns().equals(unknown), "Unavailable item was discarded from pending returns");
            inventory.items.set(0, distinct.copy());
            inventory.items.set(1, new ItemStack(Items.DIAMOND, 4));
            var dataHistory = new BrazierPlayerSelection();
            dataHistory.select(player, 1000, true);
            require(dataHistory.select(player, 1300, true).slots().equals(List.of(2, 1)),
                "Repeat selection matched item identity without data or treated count as identity");
            inventory.items.set(2, distinct.copy());
            var distinctHistory = new BrazierPlayerSelection();
            distinctHistory.select(player, 2000, true);
            require(distinctHistory.select(player, 2300, true).slots().equals(List.of(2, 0)),
                "Data-bearing reference included plain matching-item stacks");
            require(ItemStack.matches(inventory.items.get(0), distinct) && inventory.items.get(1).getCount() == 4,
                "Data-sensitive selection changed inventory data or counts");
            inventory.items.set(2, new ItemStack(Items.DIAMOND, 3));
            inventory.items.set(0, new ItemStack(Items.DIAMOND, 7));
            inventory.items.set(1, ItemStack.EMPTY);
            var history = new BrazierPlayerSelection();
            var first = history.select(player, 1000, true);
            require(first.wasHeld() && first.slots().equals(List.of(2)), "First click did not select only main hand");
            require(history.select(player, 1300, true).slots().equals(List.of(2, 0)), "Repeat boundary lost selected-first source order");
            inventory.items.set(2, ItemStack.EMPTY);
            var emptyRepeat = history.select(player, 1600, true);
            require(!emptyRepeat.wasHeld() && emptyRepeat.slots().equals(List.of(0))
                && emptyRepeat.reference().is(Items.DIAMOND), "Empty-hand repeat lost remembered reference");
            emptyRepeat.reference().setCount(1);
            require(history.select(player, 1900, true).reference().getCount() == 3, "Returned reference mutated stored history");
            require(history.select(player, 2201, true) == null, "Expired repeat reused an empty-hand reference");
            require(inventory.items.get(0).getCount() == 7 && player.getOffhandItem().getCount() == 5,
                "Selection paid inventory or included offhand");
            inventory.items.set(2, new ItemStack(ContentRegistry.RADIANT_TROVE_ITEM.get()));
            var containerHistory = new BrazierPlayerSelection();
            require(containerHistory.select(player, 1000, true).slots().isEmpty(), "Initial selection extracted item-handler container");
            require(containerHistory.select(player, 1300, true).slots().equals(List.of(2)),
                "Repeat selection incorrectly enabled nested extraction or excluded the matching container itself");
            for (var excluded : new net.minecraft.world.item.Item[]{ContentRegistry.SCEPTER_MANIPULATION.get(), ContentRegistry.DEBUG_ORB.get()}) {
                inventory.items.set(2, new ItemStack(excluded));
                require(history.select(player, 4000, true) == null, "Excluded tool became a deposit reference");
            }
            inventory.items.set(2, new ItemStack(ContentRegistry.SCEPTER_REVELATION.get()));
            require(history.select(player, 5000, true) != null, "Manipulation exclusion incorrectly excluded Revelation");
            require(history.select(player, 6000, false) == null, "Missing network still selected sources");
            for (long gap : new long[]{950, 951}) {
                inventory.items.set(2, new ItemStack(Items.DIAMOND, 3));
                var sharedHistory = new BrazierPlayerSelection();
                require(sharedHistory.select(player, 1000, true) != null, "Could not seed multi-player history");
                sharedHistory.select(other, 1000 + gap, true);
                inventory.items.set(2, ItemStack.EMPTY);
                require(sharedHistory.select(player, 1000 + gap + 50, true) == null,
                    "Different player's click failed to interrupt repeat identity");
                var recalled = sharedHistory.select(player, 1000 + gap + 350, true);
                require(gap == 950 ? recalled != null && recalled.reference().is(Items.DIAMOND)
                    && recalled.reference().getCount() == 3 && recalled.slots().equals(List.of(0)) : recalled == null,
                    "Player references crossed identity or ignored the strict 950-ms history-clear boundary");
                require(new BrazierPlayerSelection().select(player, 1000 + gap + 350, true) == null,
                    "Fresh device history inherited another device's remembered reference");
            }
            for (boolean mode : new boolean[]{false, true}) {
                player.getAbilities().instabuild = mode;
                for (int slot = 0; slot < inventory.items.size(); slot++) inventory.items.set(slot, new ItemStack(Items.COBBLESTONE, 64));
                inventory.items.set(0, new ItemStack(Items.EMERALD, 60));
                var delivery = new com.aranaira.arcanearchives.data.PlayerSaveData();
                delivery.queueBrazierReturns(List.of(new ItemStack(Items.EMERALD, 130), distinct), player.level().registryAccess());
                require(!delivery.deliverBrazierReturns(player) && inventory.items.get(0).getCount() == 64,
                    "Partial pending delivery ignored stack capacity");
                var unpaid = delivery.decodeBrazierReturns(player.level().registryAccess()).orElseThrow();
                require(unpaid.size() == 2 && unpaid.get(0).getCount() == 130 - (64 - 60) && ItemStack.matches(unpaid.get(1), distinct),
                    "Partial delivery lost unpaid counts/data or voided creative overflow");
                var unchanged = delivery.brazierPendingReturns();
                delivery.setDirty(false);
                require(!delivery.deliverBrazierReturns(player) && unchanged.equals(delivery.brazierPendingReturns()) && !delivery.isDirty(),
                    "Full-inventory retry changed pending returns");
                for (int slot = 0; slot < 3; slot++) inventory.items.set(slot, ItemStack.EMPTY);
                require(delivery.deliverBrazierReturns(player) && !delivery.hasBrazierPendingReturns()
                    && inventory.items.get(0).getCount() == 64 && inventory.items.get(1).getCount() == 130 - (64 - 60) - 64
                    && ItemStack.matches(inventory.items.get(2), distinct), "Complete delivery failed exact count/data conservation");
                require(delivery.deliverBrazierReturns(player) && inventory.items.get(0).getCount() == 64,
                    "Completed pending return delivered twice");
                delivery.setBrazierPendingReturns(unknown);
                require(!delivery.deliverBrazierReturns(player) && delivery.brazierPendingReturns().equals(unknown)
                    && inventory.items.get(0).getCount() == 64, "Undecodable batch partially delivered or disappeared");
                require(player.getOffhandItem().getCount() == 5, "Pending delivery modified offhand");
            }
        } finally {
            player.getAbilities().instabuild = creative;
            for (int slot = 0; slot < saved.size(); slot++) inventory.items.set(slot, saved.get(slot));
            inventory.selected = selected;
            player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, offhand);
        }
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
