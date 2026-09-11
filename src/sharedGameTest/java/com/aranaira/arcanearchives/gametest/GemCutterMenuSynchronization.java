package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.GemCuttersTableMenu;
import com.aranaira.arcanearchives.tileentities.GemCuttersTableBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Native synchronization callbacks and receiving menu, not a network/socket or rendered-client test. */
final class GemCutterMenuSynchronization {
    private GemCutterMenuSynchronization() {}

    static void run(Player player, Player receivingPlayer, GemCuttersTableBlockEntity table, GemCuttersTableMenu server) {
        player.getInventory().clearContent();
        table.insertInput(0, new ItemStack(ContentRegistry.RAW_QUARTZ.get(), 2), false);
        player.getInventory().setItem(8, new ItemStack(Items.GOLD_NUGGET));
        server.broadcastChanges();
        // The public client constructor has no crafting state/catalog. Its inventory must not alias a server player's.
        var client = new GemCuttersTableMenu(server.containerId, new Inventory(receivingPlayer));
        var relay = new Relay(client);
        server.setSynchronizer(relay);
        try {
            require(relay.initial == 1, "Native initial synchronization did not run");
            sameContents(server, client);
            require(client.getSlot(0).getItem().isEmpty(), "Preview was synchronized as completed output");
            require(client.getSlot(62).getItem().is(ContentRegistry.RADIANT_LANTERN_ITEM.get())
                && client.getSlot(62).getItem().getCount() == 4, "Selected paid preview was not synchronized");
            require(!client.getSlot(0).mayPlace(new ItemStack(Items.DIAMOND)), "Receiving output permits insertion");
            require(!client.clickMenuButton(receivingPlayer, 2), "Receiving menu independently authorized crafting");
            client.clicked(62, 0, ClickType.PICKUP, receivingPlayer);
            client.clicked(55, 0, ClickType.CLONE, receivingPlayer);
            require(client.getCarried().isEmpty(), "Receiving preview supplied extractable items");

            require(server.clickMenuButton(player, 2), "Native synchronized craft failed");
            require(relay.slots > 0, "Craft did not emit native slot changes");
            sameContents(server, client);
            require(client.getSlot(0).getItem().getCount() == 4 && client.getSlot(37).getItem().isEmpty()
                && client.getSlot(36).getItem().isEmpty() && client.getSlot(62).getItem().isEmpty(),
                "Craft deltas did not update output, table payment, player payment and preview together");

            // A newly constructed receiving menu must recover a completed batch from the full snapshot alone.
            client = new GemCuttersTableMenu(server.containerId, new Inventory(receivingPlayer));
            relay = new Relay(client);
            server.setSynchronizer(relay);
            require(relay.initial == 1 && client.getSlot(0).getItem().getCount() == 4, "Reopened snapshot lost output");
            sameContents(server, client);
            server.clicked(0, 1, ClickType.PICKUP, player);
            server.broadcastChanges();
            require(relay.carried > 0 && client.getCarried().getCount() == 2 && client.getSlot(0).getItem().getCount() == 2,
                "Partial extraction failed to synchronize cursor and remaining output");
            sameContents(server, client);
            server.clicked(0, 0, ClickType.PICKUP, player);
            server.broadcastChanges();
            require(client.getCarried().getCount() == 4 && client.getSlot(0).getItem().isEmpty(),
                "Final extraction failed to clear receiving output");
            require(!server.clickMenuButton(player, 2), "Extraction unexpectedly replenished payment");
            sameContents(server, client);
        } finally {
            server.setSynchronizer(null);
            server.setCarried(ItemStack.EMPTY);
        }
    }

    private static void sameContents(GemCuttersTableMenu server, GemCuttersTableMenu client) {
        require(server.slots.size() == client.slots.size(), "Server/receiving slot layouts differ");
        for (int slot = 0; slot < server.slots.size(); slot++)
            require(ItemStack.matches(server.getSlot(slot).getItem(), client.getSlot(slot).getItem()),
                "Receiving slot differs from server: " + slot);
        require(ItemStack.matches(server.getCarried(), client.getCarried()), "Receiving cursor differs from server");
    }

    private static final class Relay implements ContainerSynchronizer {
        private final GemCuttersTableMenu client;
        private int initial;
        private int slots;
        private int carried;

        private Relay(GemCuttersTableMenu client) { this.client = client; }

        @Override
        public void sendInitialData(AbstractContainerMenu server, NonNullList<ItemStack> items, ItemStack cursor, int[] data) {
            initial++;
            client.initializeContents(server.getStateId(), items.stream().map(ItemStack::copy).toList(), cursor.copy());
            for (int index = 0; index < data.length; index++) client.setData(index, data[index]);
        }

        @Override
        public void sendSlotChange(AbstractContainerMenu server, int slot, ItemStack stack) {
            slots++;
            client.setItem(slot, server.getStateId(), stack.copy());
        }

        @Override
        public void sendCarriedChange(AbstractContainerMenu server, ItemStack stack) {
            carried++;
            client.setCarried(stack.copy());
        }

        @Override
        public void sendDataChange(AbstractContainerMenu server, int id, int value) { client.setData(id, value); }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
