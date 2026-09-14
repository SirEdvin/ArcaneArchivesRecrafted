package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.inventory.ManifestMenu;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Native registered server player with a test connection, not a rendering/remote client. */
public final class ManifestServerSession {
    public static void run(GameTestHelper helper, ServerPlayer player, net.minecraft.world.entity.player.Player colleague) {
        try {
            require(player.server.getPlayerList().getPlayer(player.getUUID()) == player,
                "Manifest session fixture is not registered with the native server");
            BrazierActivationLifecycle.run(helper, player,
                (hit, hand) -> player.gameMode.useItemOn(player, player.level(), player.getItemInHand(hand), hand, hit).consumesAction(),
                hit -> player.gameMode.useItemOn(player, player.level(), player.getMainHandItem(),
                    net.minecraft.world.InteractionHand.MAIN_HAND, hit).consumesAction());
            enchantmentSearch(player);
            advancements(player);
            trackingSelection(helper, player);
            ManifestTrackingLifecycle.run(helper, player, colleague);
            ManifestTrackingLifecycle.doubleChest(helper, player, colleague);
            ManifestTrackingLifecycle.succession(helper, player);
            player.setShiftKeyDown(true);
            ManifestMenu.openFromKey(player);
            require(player.containerMenu == player.inventoryMenu, "Sneaking hotkey opened a Manifest");
            player.setShiftKeyDown(false);
            ManifestMenu.openFromKey(player);
            require(player.containerMenu instanceof ManifestMenu, "Native server hotkey did not open the Manifest");
            var menu = (ManifestMenu) player.containerMenu;
            ManifestMenu.openFromKey(player);
            require(player.containerMenu == menu, "Repeated hotkey replaced an active menu");
            require(!menu.request(player, -1), "Native server accepted a negative distance");
            require(menu.request(player, 100), "Initial native Manifest request was rejected");
            require(!menu.request(player, 200), "Native Manifest bypassed its same-tick refresh limit");
            var detached = new ManifestMenu(menu.containerId, player.getInventory());
            require(!detached.request(player, 100), "Detached menu with matching ID accepted a request");
            helper.runAfterDelay(10, () -> {
                try {
                    require(menu.request(player, 200), "Native Manifest did not recover after the refresh interval");
                    require(!menu.clickMenuButton(player, 0), "Manual refresh bypassed the request throttle");
                    player.closeContainer();
                    require(!menu.request(player, 100), "Closed native Manifest accepted a stale request");
                    ManifestMenu.openFromKey(player);
                    require(player.containerMenu instanceof ManifestMenu && player.containerMenu != menu,
                        "Native Manifest did not reopen after close");
                    require(!menu.request(player, 100), "Reopening revived the previous menu's authority");
                } catch (RuntimeException | Error failure) {
                    cleanup(player);
                    throw failure;
                }
                ManifestTrackingLifecycle.automaticRevocation(helper, player, () -> cleanup(player));
            });
        } catch (RuntimeException | Error failure) {
            cleanup(player);
            throw failure;
        }
    }

    private static void cleanup(ServerPlayer player) {
        player.closeContainer();
        player.server.getPlayerList().remove(player);
        player.connection.disconnect(Component.literal("Manifest fixture complete"));
    }

    private static void trackingSelection(GameTestHelper helper, ServerPlayer player) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        require(level.isEmptyBlock(pos), "Tracking selection fixture occupied");
        var owner = java.util.UUID.randomUUID();
        var hives = com.aranaira.arcanearchives.data.HiveSaveData.get(player.server);
        try {
            require(hives.acceptInvitation(owner, player.getUUID()), "Tracking fixture could not join Hive");
            level.setBlockAndUpdate(pos, com.aranaira.arcanearchives.init.ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
            var chest = (com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity) level.getBlockEntity(pos);
            chest.setOwner(owner);
            var reference = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENDER_EYE, 3);
            chest.inventory().setStackInSlot(0, reference);
            ManifestMenu.open(player);
            var menu = (ManifestMenu) player.containerMenu;
            require(menu.trackingSelection(player, menu.snapshotRevision(), 0, reference).isEmpty(), "Uninitialized Manifest authorized tracking");
            require(menu.request(player, 0), "Tracking fixture listing request failed");
            int index = -1;
            for (int i = 0; i < menu.entries().size(); i++)
                if (menu.entries().get(i).stack().is(reference.getItem())) index = i;
            require(index >= 0, "Server did not retain the published tracking selection");
            require(menu.entries().get(index).range() == com.aranaira.arcanearchives.data.ManifestContents.Range.OUT_OF_RANGE,
                "Tracking fixture did not exercise original same-dimension out-of-range selection");
            require(menu.trackingSelection(player, menu.snapshotRevision(), -1, reference).isEmpty()
                && menu.trackingSelection(player, menu.snapshotRevision(), menu.entries().size(), reference).isEmpty()
                && menu.trackingSelection(player, menu.snapshotRevision() - 1, index, reference).isEmpty()
                && menu.trackingSelection(player, menu.snapshotRevision() + 1, index, reference).isEmpty()
                && menu.trackingSelection(player, menu.snapshotRevision(), index, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIRT)).isEmpty(),
                "Invalid tracking selection accepted");
            require(hives.resign(player.getUUID()), "Tracking fixture could not revoke access");
            require(menu.trackingSelection(player, menu.snapshotRevision(), index, reference).isEmpty(), "Revoked Hive selection accepted");
            require(hives.acceptInvitation(owner, player.getUUID()), "Tracking fixture could not restore access");
            require(menu.trackingSelection(player, menu.snapshotRevision(), index, reference).isPresent(), "Current Hive tracking selection rejected");
            require(menu.trackingSelection(player, menu.snapshotRevision(), index, reference).isEmpty(), "Tracking selection bypassed rate limit");
            long oldRevision = menu.snapshotRevision();
            int oldContainer = menu.containerId;
            for (int attempt = 0; attempt < 100; attempt++) {
                ManifestMenu.open(player);
                if (player.containerMenu.containerId == oldContainer) break;
            }
            var reused = (ManifestMenu) player.containerMenu;
            require(reused != menu && reused.containerId == oldContainer,
                "Native container counter did not reuse the fixture ID");
            require(reused.request(player, 0), "Reused-ID Manifest did not publish its own snapshot");
            require(reused.trackingSelection(player, oldRevision, index, reference).isEmpty(),
                "Old selection regained authority after native container ID reuse");
            require(reused.trackingSelection(player, reused.snapshotRevision(), index, reference).isPresent(),
                "Current selection failed after native container ID reuse");
            player.closeContainer();
            require(menu.trackingSelection(player, menu.snapshotRevision(), index, reference).isEmpty(), "Closed menu authorized a new selection");
        } finally {
            player.closeContainer();
            hives.resign(player.getUUID());
            hives.resign(owner);
            if (level.getBlockEntity(pos) instanceof com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity chest)
                chest.inventory().setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY);
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        }
    }

    private static void enchantmentSearch(ServerPlayer player) {
        boolean legacy = net.minecraft.SharedConstants.getCurrentVersion().getName().startsWith("1.20.");
        for (String item : new String[] {"enchanted_book", "diamond_sword"}) {
            player.getInventory().clearContent();
            String data = legacy ? (item.equals("enchanted_book") ? "{StoredEnchantments:" : "{Enchantments:")
                + "[{id:\"minecraft:sharpness\",lvl:5s}]}"
                : "[minecraft:" + (item.equals("enchanted_book") ? "stored_enchantments" : "enchantments")
                    + "={levels:{\"minecraft:sharpness\":5}}]";
            player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withPermission(4),
                "give @s minecraft:" + item + data + " 1");
            var stack = player.getInventory().getItem(0);
            require(!stack.isEmpty(), "Native enchantment fixture command did not create an item");
            boolean book = item.equals("enchanted_book");
            require(stack.is(book ? net.minecraft.world.item.Items.ENCHANTED_BOOK : net.minecraft.world.item.Items.DIAMOND_SWORD)
                && (book || stack.isEnchanted()), "Native parser did not create the requested enchanted fixture");
            var before = stack.copy();
            require(com.aranaira.arcanearchives.client.ManifestSearch.matchesEnchantment(stack, "sharpness v") == book,
                "Native enchantment search did not distinguish stored books from enchanted tools");
            require(!com.aranaira.arcanearchives.client.ManifestSearch.matchesEnchantment(stack, "sharpness iv")
                && !com.aranaira.arcanearchives.client.ManifestSearch.matchesEnchantment(stack, "@sharpness"),
                "Native enchantment search ignored level or mod-query boundaries");
            require(net.minecraft.world.item.ItemStack.matches(before, stack), "Manifest search mutated the native item");
        }
        require(!com.aranaira.arcanearchives.client.ManifestSearch.matchesEnchantment(
            new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK), "sharpness"),
            "Empty enchanted book acquired a nonexistent search match");
        player.getInventory().clearContent();
    }

    private static void advancements(ServerPlayer player) {
        var inventory = player.getInventory();
        inventory.clearContent();
        player.inventoryMenu.broadcastChanges();
        var initial = savedAdvancements(player);
        require(!initial.has("arcanearchives:manifest") && !initial.has("arcanearchives:lectern"),
            "Fresh native player already has Manifest advancement progress");
        inventory.setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND));
        player.inventoryMenu.broadcastChanges();
        var unrelated = savedAdvancements(player);
        require(!unrelated.has("arcanearchives:manifest") && !unrelated.has("arcanearchives:lectern"),
            "Unrelated inventory change awarded the Manifest branch");
        inventory.setItem(0, new net.minecraft.world.item.ItemStack(com.aranaira.arcanearchives.init.ContentRegistry.MANIFEST.get()));
        player.inventoryMenu.broadcastChanges();
        var manifest = savedAdvancements(player);
        require(done(manifest, "manifest") && !manifest.has("arcanearchives:lectern"),
            "Manifest acquisition did not award only its own advancement");
        var obtained = manifest.getAsJsonObject("arcanearchives:manifest").getAsJsonObject("criteria").get("book");
        inventory.setItem(0, net.minecraft.world.item.ItemStack.EMPTY);
        player.inventoryMenu.broadcastChanges();
        var removed = savedAdvancements(player);
        require(done(removed, "manifest") && obtained.equals(removed.getAsJsonObject("arcanearchives:manifest")
            .getAsJsonObject("criteria").get("book")), "Removing the Manifest lost or rewrote its earned progress");
        player.getAdvancements().reload(player.server.getAdvancements());
        var reloaded = savedAdvancements(player);
        require(removed.equals(reloaded), "Native advancement reload changed the saved progress");
        inventory.setItem(0, new net.minecraft.world.item.ItemStack(com.aranaira.arcanearchives.init.ContentRegistry.LECTERN_MANIFEST_ITEM.get()));
        player.inventoryMenu.broadcastChanges();
        var lectern = savedAdvancements(player);
        require(done(lectern, "manifest") && done(lectern, "lectern"),
            "Lectern acquisition did not persist the completed branch");
        inventory.clearContent();
        player.inventoryMenu.broadcastChanges();
        player.getAdvancements().reload(player.server.getAdvancements());
        require(lectern.equals(savedAdvancements(player)),
            "Completed advancement branch did not survive native reload with an empty inventory");
        require(!savedAdvancements(player).has("arcanearchives:bonfire"), "Unrelated inventory awarded Hoarders");
        inventory.setItem(0, new net.minecraft.world.item.ItemStack(com.aranaira.arcanearchives.init.ContentRegistry.BRAZIER_ITEM.get()));
        player.inventoryMenu.broadcastChanges();
        var bonfire = savedAdvancements(player);
        require(done(bonfire, "bonfire"), "Brazier acquisition did not award Hoarders");
        inventory.clearContent();
        player.inventoryMenu.broadcastChanges();
        player.getAdvancements().reload(player.server.getAdvancements());
        require(bonfire.equals(savedAdvancements(player)), "Hoarders progress did not survive removal/save/reload");
    }

    private static com.google.gson.JsonObject savedAdvancements(ServerPlayer player) {
        player.getAdvancements().save();
        var path = player.server.getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_ADVANCEMENTS_DIR)
            .resolve(player.getUUID() + ".json");
        try {
            return com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(path)).getAsJsonObject();
        } catch (java.io.IOException error) {
            throw new java.io.UncheckedIOException("Cannot read native fixture advancement save", error);
        }
    }

    private static boolean done(com.google.gson.JsonObject saved, String name) {
        var progress = saved.getAsJsonObject("arcanearchives:" + name);
        return progress != null && progress.get("done").getAsBoolean()
            && progress.getAsJsonObject("criteria").has("book");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
