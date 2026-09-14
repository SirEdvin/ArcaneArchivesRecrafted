package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.data.ManifestContents;
import com.aranaira.arcanearchives.events.ManifestSnapshot;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.ManifestMenu;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Native item data/registries and the actual fragment codec/receiving menu, without a rendering client. */
public final class ManifestTransportLifecycle {
    public static void run(Player player, List<ManifestContents.Entry> source) {
        var inventory = player.getInventory();
        var saved = new ArrayList<ItemStack>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) saved.add(inventory.getItem(slot).copy());
        try {
            inventory.clearContent();
            require(!com.aranaira.arcanearchives.items.ManifestItem.hasManifest(player), "Empty inventory satisfied Manifest presence");
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ContentRegistry.MANIFEST.get()));
            require(!com.aranaira.arcanearchives.items.ManifestItem.hasManifest(player), "Offhand changed original hotkey presence rule");
            for (int slot = 0; slot < inventory.items.size(); slot++) {
                inventory.setItem(slot, new ItemStack(ContentRegistry.MANIFEST.get()));
                require(com.aranaira.arcanearchives.items.ManifestItem.hasManifest(player), "Manifest hotkey missed main slot " + slot);
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        } finally {
            for (int slot = 0; slot < saved.size(); slot++) inventory.setItem(slot, saved.get(slot));
        }
        for (int distance : new int[]{0, 1, 100, 200, Integer.MAX_VALUE}) {
            var request = new com.aranaira.arcanearchives.events.ManifestRequest(3, distance);
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                com.aranaira.arcanearchives.events.ManifestRequest.write(buffer, request);
                require(request.equals(com.aranaira.arcanearchives.events.ManifestRequest.read(buffer))
                    && !buffer.isReadable(), "Manifest distance request lost bounds or data");
            } finally { buffer.release(); }
        }
        reject(() -> new com.aranaira.arcanearchives.events.ManifestRequest(-1, 100), "negative menu ID");
        reject(() -> new com.aranaira.arcanearchives.events.ManifestRequest(3, -1), "negative Manifest distance");
        reject(() -> new com.aranaira.arcanearchives.events.ManifestSelect(-1, 0, 0, 0), "negative tracking menu ID");
        reject(() -> new com.aranaira.arcanearchives.events.ManifestSelect(3, -1, 0, 0), "negative tracking revision");
        reject(() -> new com.aranaira.arcanearchives.events.ManifestSelect(3, 0, -1, 0), "negative tracking index");
        reject(() -> new com.aranaira.arcanearchives.events.ManifestSelect(3, 0, 0, 3), "unknown tracking action");
        var registries = player.level().registryAccess();
        var encoded = ManifestSnapshot.encode(source, registries);
        var decoded = ManifestSnapshot.decode(encoded, registries);
        require(decoded.size() == source.size(), "Manifest codec lost entries");
        for (int index = 0; index < source.size(); index++) {
            var before = source.get(index);
            var after = decoded.get(index);
            require(before.count() == after.count() && before.range() == after.range()
                && before.locations().equals(after.locations()) && ItemStack.matches(before.stack(), after.stack()),
                "Manifest codec lost extended count, components, source or range");
        }
        reject(() -> ManifestSnapshot.decode(Arrays.copyOf(encoded, encoded.length - 1), registries), "truncated snapshot");
        reject(() -> ManifestSnapshot.decode(Arrays.copyOf(encoded, encoded.length + 1), registries), "trailing snapshot bytes");
        reject(() -> new ManifestSnapshot(3, 0, -1, 1, new byte[]{0}), "negative offset");
        reject(() -> new ManifestSnapshot(3, -1, 0, 1, new byte[]{0}), "negative revision");
        reject(() -> new ManifestSnapshot(3, 0, 0, ManifestSnapshot.MAX_BYTES + 1, new byte[]{0}), "oversized snapshot");
        reject(() -> new ManifestSnapshot(3, 0, 0, 1, new byte[]{0, 1}), "overflowing fragment");

        var expanded = new ArrayList<ManifestContents.Entry>();
        for (int index = 0; index < 256; index++) expanded.addAll(source);
        byte[] bytes = ManifestSnapshot.encode(expanded, registries);
        require(bytes.length > ManifestSnapshot.CHUNK_BYTES, "Fragment fixture did not cross packet boundary");
        var menu = new ManifestMenu(3, player.getInventory());
        require(!menu.request(player, -1) && !menu.request(player, Integer.MAX_VALUE), "Detached distance request accepted");
        for (int offset = 0; offset < bytes.length; offset += ManifestSnapshot.CHUNK_BYTES) {
            var message = new ManifestSnapshot(3, 0, offset, bytes.length,
                Arrays.copyOfRange(bytes, offset, Math.min(bytes.length, offset + ManifestSnapshot.CHUNK_BYTES)));
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                ManifestSnapshot.write(buffer, message);
                var wire = ManifestSnapshot.read(buffer);
                require(!buffer.isReadable(), "Manifest fragment codec left trailing bytes");
                menu.receive(wire);
            } finally { buffer.release(); }
            if (offset + ManifestSnapshot.CHUNK_BYTES < bytes.length)
                require(!menu.ready() && menu.entries().isEmpty(), "Partially received Manifest became visible");
        }
        require(menu.ready() && !menu.failed() && menu.entries().size() == expanded.size(), "Fragmented Manifest failed to assemble");
        menu.receive(new ManifestSnapshot(4, 1, 0, 0, new byte[0]));
        require(!menu.failed() && menu.entries().size() == expanded.size(), "Stale container response altered current Manifest");
        menu.receive(new ManifestSnapshot(3, 1, 1, 2, new byte[]{0}));
        require(menu.failed() && menu.entries().isEmpty(), "Out-of-order snapshot retained old contents");
        menu.receive(new ManifestSnapshot(3, 2, 0, encoded.length, encoded));
        require(!menu.failed() && menu.entries().size() == source.size(), "Fresh snapshot did not recover after rejection");
        menu.receive(new ManifestSnapshot(3, 1, 0, 0, new byte[0]));
        require(!menu.failed() && menu.snapshotRevision() == 2, "Older failure replaced a newer snapshot");
        menu.receive(new ManifestSnapshot(3, 3, 0, 0, new byte[0]));
        require(menu.failed() && menu.entries().isEmpty(), "Failed server refresh retained old Hive contents");
        menu.receive(new ManifestSnapshot(3, 2, 0, encoded.length, encoded));
        require(menu.failed() && menu.entries().isEmpty() && menu.snapshotRevision() == 3,
            "Older successful snapshot revived revoked contents");
        menu.receive(new ManifestSnapshot(3, 4, 0, encoded.length, encoded));
        byte[] first = Arrays.copyOfRange(bytes, 0, ManifestSnapshot.CHUNK_BYTES);
        menu.receive(new ManifestSnapshot(3, 5, 0, bytes.length, first));
        require(!menu.ready() && !menu.failed() && menu.entries().isEmpty(),
            "Pending revision retained a selectable listing from its predecessor");
        menu.receive(new ManifestSnapshot(3, 6, 0, bytes.length, first));
        for (int offset = ManifestSnapshot.CHUNK_BYTES; offset < bytes.length; offset += ManifestSnapshot.CHUNK_BYTES) {
            byte[] part = Arrays.copyOfRange(bytes, offset, Math.min(bytes.length, offset + ManifestSnapshot.CHUNK_BYTES));
            menu.receive(new ManifestSnapshot(3, 5, offset, bytes.length, part));
            require(!menu.ready() && !menu.failed() && menu.entries().isEmpty(),
                "Interrupted revision contaminated the current assembly");
            menu.receive(new ManifestSnapshot(3, 6, offset, bytes.length, part));
        }
        require(menu.ready() && !menu.failed() && menu.entries().size() == expanded.size()
            && menu.snapshotRevision() == 6, "Replacement snapshot failed to complete after interrupted refresh");

        var carried = new ItemStack(Items.EMERALD, 3);
        menu.setCarried(carried.copy());
        for (ClickType type : ClickType.values()) for (int slot : new int[]{-999, -1, 0, 1000}) menu.clicked(slot, 0, type, player);
        require(menu.slots.isEmpty() && ItemStack.matches(menu.getCarried(), carried)
            && menu.quickMoveStack(player, 0).isEmpty(), "Manifest allowed inventory manipulation");
        require(!menu.clickMenuButton(player, -1) && !menu.clickMenuButton(player, 1000)
            && !menu.clickMenuButton(player, 0), "Invalid/stale Manifest request accepted");
        for (var hand : InteractionHand.values()) {
            var old = player.getItemInHand(hand);
            try {
                var stack = new ItemStack(ContentRegistry.MANIFEST.get());
                player.setItemInHand(hand, stack);
                var result = stack.use(player.level(), player, hand);
                require(result.getResult().consumesAction() && ItemStack.matches(stack, result.getObject())
                    && stack.getCount() == 1 && stack.getMaxStackSize() == 1, "Manifest use consumed or replaced the item");
            } finally { player.setItemInHand(hand, old); }
        }
    }
    private static void reject(Runnable action, String name) {
        try { action.run(); }
        catch (RuntimeException expected) { return; }
        throw new AssertionError("Accepted " + name);
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
