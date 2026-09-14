package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.data.HiveSaveData;
import com.aranaira.arcanearchives.data.StorageNetworks;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

/** Native installation/removal and current Hive membership, without manually populating the index. */
public final class StorageNetworkLifecycle {
    public static void run(GameTestHelper helper, net.minecraft.world.entity.player.Player player,
            java.util.function.Function<net.minecraft.world.level.block.entity.BlockEntity, net.minecraft.nbt.CompoundTag> save,
            java.util.function.BiConsumer<net.minecraft.world.level.block.entity.BlockEntity, net.minecraft.nbt.CompoundTag> load) {
        var level = helper.getLevel();
        require(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getKey(ContentRegistry.BRAZIER_ABSORB.get())
            .toString().equals("arcanearchives:brazier.absorb"), "Brazier absorption sound missing from native registry");
        var server = level.getServer();
        var hives = HiveSaveData.get(server);
        UUID owner = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        UUID successor = UUID.randomUUID();
        var first = helper.absolutePos(new BlockPos(1, 2, 1));
        var second = first.above();
        require(level.isEmptyBlock(first) && level.isEmptyBlock(second), "Network fixture occupied");
        var brazierDestination = first.east();
        require(level.isEmptyBlock(brazierDestination), "Brazier destination fixture occupied");
        try {
            level.setBlockAndUpdate(first, ContentRegistry.BRAZIER.get().defaultBlockState());
            level.setBlockAndUpdate(brazierDestination, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
            var brazier = (com.aranaira.arcanearchives.tileentities.BrazierBlockEntity) level.getBlockEntity(first);
            var destination = (RadiantChestBlockEntity) level.getBlockEntity(brazierDestination);
            destination.setOwner(owner);
            var offered = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND, 7);
            require(brazier.radius() == 150 && !brazier.personalOnly(), "Brazier initial defaults changed");
            require(brazier.insert(offered, false).getCount() == 7 && destination.inventory().getStackInSlot(0).isEmpty(),
                "Ownerless Brazier routed items");
            var state = save.apply(brazier);
            state.putUUID("network_owner", owner);
            load.accept(brazier, state);
            brazier.configure(40, true);
            var saved = save.apply(brazier);
            var center = net.minecraft.world.phys.Vec3.atCenterOf(first);
            var effects = new com.aranaira.arcanearchives.client.BrazierRanges();
            effects.toggle(brazier, level);
            effects.tick(level);
            require(effects.state(brazier) != null && effects.state(brazier).age() == 1,
                "Installed range owner did not retain a ticking effect");
            var drawn = new java.util.ArrayList<net.minecraft.world.phys.AABB>();
            effects.forEach(level, (device, effect) -> drawn.add(device.rangeBounds(effect.age(), .5F)));
            require(drawn.equals(java.util.List.of(brazier.rangeBounds(1, .5F))) && effects.state(brazier).age() == 1,
                "Range draw traversal lost geometry or advanced animation time");
            brazier.configure(90, true);
            drawn.clear();
            effects.forEach(level, (device, effect) -> drawn.add(device.rangeBounds(effect.age(), .5F)));
            require(drawn.equals(java.util.List.of(brazier.rangeBounds(1, .5F))) && effects.state(brazier).age() == 1,
                "Range draw traversal failed live-radius update");
            brazier.configure(40, true);
            effects.forEach(null, (device, effect) -> { throw new AssertionError("Worldless draw submitted a range effect"); });
            effects.tick(null);
            require(effects.state(brazier) == null, "World loss retained range effect");
            effects.toggle(brazier, level);
            var rangeState = new com.aranaira.arcanearchives.client.BrazierRangeState();
            require(!rangeState.showing(), "Range effect enabled by default");
            rangeState.toggle();
            for (int tick = 0; tick < 20 * 60 * 10; tick++) rangeState.tick(true);
            require(rangeState.showing() && rangeState.age() == 20 * 60 * 10,
                "Range effect expired before original update boundary");
            rangeState.tick(true);
            require(!rangeState.showing(), "Range effect survived its lifetime");
            rangeState.toggle();
            require(rangeState.showing() && rangeState.age() == 0, "Range re-enable did not restart animation");
            rangeState.tick(false);
            require(!rangeState.showing(), "Invalid owner retained range effect");
            rangeState.toggle();
            rangeState.toggle();
            rangeState.tick(true);
            require(!rangeState.showing() && rangeState.age() == 0, "Hidden effect kept ticking or resurrected");
            require(brazier.rangeBounds(0, 0).equals(new net.minecraft.world.phys.AABB(center, center).inflate(.01)),
                "Range animation did not originate at block center");
            var fullRange = new net.minecraft.world.phys.AABB(first).inflate(brazier.radius(), 255, brazier.radius()).inflate(.01);
            require(brazier.rangeBounds(20, 0).equals(fullRange) && brazier.rangeBounds(12000, .5F).equals(fullRange),
                "Range animation full extent or clamping changed");
            var midway = brazier.rangeBounds(9, 1);
            require(Math.abs(midway.getXsize() - ((brazier.radius() * 2 + 1) * .5 + .02)) < 1e-6
                && Math.abs(midway.getYsize() - ((255 * 2 + 1) * .5 + .02)) < 1e-6
                && midway.getCenter().distanceToSqr(center) < 1e-12, "Range animation interpolation changed");
            require(saved.getInt("range") == 40 && saved.getBoolean("subnetwork"), "Brazier settings not saved");
            var replica = new com.aranaira.arcanearchives.tileentities.BrazierBlockEntity(first, brazier.getBlockState());
            replica.setLevel(level);
            load.accept(replica, saved);
            require(replica.radius() == 40 && replica.personalOnly() && owner.equals(replica.networkOwner()),
                "Brazier settings/ownership failed native serialization round trip");
            for (int radius : new int[]{0, 37, 300}) {
                brazier.configure(radius, false);
                var update = brazier.getUpdatePacket();
                require(update != null && update.getPos().equals(first) && update.getType() == brazier.getType(),
                    "Brazier settings packet lost native block identity");
                load.accept(replica, update.getTag());
                require(replica.radius() == radius && !replica.personalOnly(),
                    "Brazier settings update failed replica radius/network synchronization");
            }
            brazier.configure(40, true);
            load.accept(replica, brazier.getUpdatePacket().getTag());
            require(replica.personalOnly() && replica.radius() == 40, "Brazier update did not restore personal mode");
            require(replica.insert(offered, false).getCount() == 7 && destination.inventory().getStackInSlot(0).isEmpty(),
                "Detached Brazier routed through live destinations");
            var previousPosition = player.position();
            try {
                player.setPos(first.getX() + .5, first.getY() + .5, first.getZ() + .5);
                require(!player.getUUID().equals(owner) && brazier.canConfigure(player),
                    "Nearby non-owner lost local configuration access");
                require(!replica.canConfigure(player), "Detached replica granted configuration access");
                var previousMenu = player.containerMenu;
                var menu = new com.aranaira.arcanearchives.inventory.BrazierMenu(99, player.getInventory(), brazier);
                try {
                    require(!menu.clickMenuButton(player, 0), "Inactive configuration menu accepted action");
                    player.containerMenu = menu;
                    require(menu.radius() == 40 && menu.personalOnly(), "Configuration initial values missing");
                    require(menu.setRadius(player, 35) && menu.radius() == 35 && menu.personalOnly(),
                        "Exact configuration radius changed network mode or rounded value");
                    require(menu.setRadius(player, Integer.MIN_VALUE) && menu.radius() == 0
                        && menu.setRadius(player, Integer.MAX_VALUE) && menu.radius() == 300,
                        "Exact configuration radius bounds failed");
                    require(menu.setRadius(player, 40), "Exact configuration radius restore failed");
                    var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                    try {
                        var request = new com.aranaira.arcanearchives.events.BrazierRadius(menu.containerId, 37);
                        com.aranaira.arcanearchives.events.BrazierRadius.write(buffer, request);
                        var decoded = com.aranaira.arcanearchives.events.BrazierRadius.read(buffer);
                        require(decoded.equals(request) && buffer.readableBytes() == 0, "Radius packet round trip failed");
                        decoded.apply(player);
                        require(menu.radius() == 37, "Radius packet failed active menu dispatch");
                        new com.aranaira.arcanearchives.events.BrazierRadius(menu.containerId + 1, 90).apply(player);
                        require(menu.radius() == 37, "Wrong-menu radius packet changed settings");
                        menu.setRadius(player, 40);
                    } finally { buffer.release(); }
                    require(menu.clickMenuButton(player, 0) && menu.radius() == 30
                        && menu.clickMenuButton(player, 1) && menu.radius() == 40,
                        "Configuration radius buttons did not step by ten");
                    require(menu.clickMenuButton(player, 2) && !menu.personalOnly() && !brazier.personalOnly(),
                        "Configuration network toggle not synchronized");
                    require(!menu.clickMenuButton(player, -1) && !menu.clickMenuButton(player, 3)
                        && brazier.radius() == 40, "Invalid configuration action mutated settings");
                    brazier.configure(0, false);
                    require(menu.clickMenuButton(player, 0) && menu.radius() == 0, "Menu radius underflow");
                    brazier.configure(300, false);
                    require(menu.clickMenuButton(player, 1) && menu.radius() == 300, "Menu radius overflow");
                    player.setPos(first.getX() + 9.5, first.getY() + .5, first.getZ() + .5);
                    require(!menu.clickMenuButton(player, 0) && brazier.radius() == 300,
                        "Distant configuration action mutated device");
                    require(!menu.setRadius(player, 50) && brazier.radius() == 300,
                        "Distant exact-radius action mutated device");
                    player.setPos(first.getX() + .5, first.getY() + .5, first.getZ() + .5);
                    player.containerMenu = previousMenu;
                    require(!menu.clickMenuButton(player, 0), "Closed configuration session accepted action");
                    require(!menu.setRadius(player, 50) && brazier.radius() == 300,
                        "Closed configuration session accepted exact radius");
                } finally {
                    player.containerMenu = previousMenu;
                    brazier.configure(40, true);
                }
                player.setPos(first.getX() + 8.5, first.getY() + .5, first.getZ() + .5);
                require(brazier.canConfigure(player), "Exact eight-block configuration boundary rejected");
                player.setPos(first.getX() + 8.6, first.getY() + .5, first.getZ() + .5);
                require(!brazier.canConfigure(player), "Distant player retained configuration access");
            } finally { player.setPos(previousPosition); }
            require(brazier.insert(offered, true).isEmpty() && destination.inventory().getStackInSlot(0).isEmpty(),
                "Installed Brazier simulation mutated destination");
            require(brazier.insert(offered, false).isEmpty() && offered.getCount() == 7
                && destination.inventory().getStackInSlot(0).getCount() == 7, "Installed Brazier transfer lost items");
            com.aranaira.arcanearchives.tileentities.BrazierCollisionLifecycle.run(brazier, destination);
            BrazierAutomationLifecycle.verify(brazier, destination, new com.aranaira.arcanearchives.inventory.BrazierItemAutomation(brazier));
            BrazierAutomationLifecycle.verifyHopper(brazier, destination);
            com.aranaira.arcanearchives.tileentities.BrazierPlayerDepositLifecycle.run(brazier, destination, player);
            brazier.configure(-10, false);
            require(brazier.radius() == 0, "Brazier minimum range not clamped");
            brazier.configure(400, false);
            require(brazier.radius() == 300, "Brazier maximum range not clamped");
            var retainedMenu = new com.aranaira.arcanearchives.inventory.BrazierMenu(100, player.getInventory(), brazier);
            var replacementEffects = new com.aranaira.arcanearchives.client.BrazierRanges();
            replacementEffects.toggle(brazier, level);
            require(replacementEffects.state(brazier) != null, "Replacement range fixture not enabled");
            level.removeBlock(first, false);
            require(brazier.insert(offered, false).getCount() == 7 && destination.inventory().getStackInSlot(0).getCount() == 7,
                "Removed Brazier retained routing access");
            effects.forEach(level, (device, effect) -> { throw new AssertionError("Removed range submitted before cleanup tick"); });
            effects.tick(level);
            require(effects.state(brazier) == null, "Removed device retained range effect");
            effects.toggle(brazier, level);
            require(effects.state(brazier) == null, "Removed device started a new range effect");
            var previousMenu = player.containerMenu;
            var replacementTestPosition = player.position();
            try {
                player.setPos(first.getX() + .5, first.getY() + .5, first.getZ() + .5);
                player.containerMenu = retainedMenu;
                require(!retainedMenu.stillValid(player) && !retainedMenu.clickMenuButton(player, 0),
                    "Removed Brazier retained configuration access");
                level.setBlockAndUpdate(first, ContentRegistry.BRAZIER.get().defaultBlockState());
                var replacement = (com.aranaira.arcanearchives.tileentities.BrazierBlockEntity) level.getBlockEntity(first);
                load.accept(replacement, saved);
                replacementEffects.tick(level);
                require(replacementEffects.state(brazier) == null && replacementEffects.state(replacement) == null,
                    "Same-position replacement inherited the removed device's range effect");
                replacementEffects.toggle(brazier, level);
                require(replacementEffects.state(brazier) == null, "Old device reactivated after replacement");
                replacementEffects.toggle(replacement, level);
                replacementEffects.tick(level);
                require(replacementEffects.state(replacement) != null && replacementEffects.state(replacement).age() == 1,
                    "Replacement could not start an independent range effect");
                replacementEffects.toggle(replacement, level);
                require(replacementEffects.state(replacement) == null, "Explicit hiding retained replacement range effect");
                require(replacement.canConfigure(player) && owner.equals(replacement.networkOwner()),
                    "Replacement configuration fixture unavailable");
                require(!retainedMenu.stillValid(player) && !retainedMenu.clickMenuButton(player, 2)
                    && !retainedMenu.setRadius(player, 90)
                    && replacement.radius() == 40 && replacement.personalOnly(),
                    "Old configuration session modified replacement Brazier");
                var freshMenu = new com.aranaira.arcanearchives.inventory.BrazierMenu(101, player.getInventory(), replacement);
                player.containerMenu = freshMenu;
                require(freshMenu.stillValid(player) && freshMenu.clickMenuButton(player, 0)
                    && replacement.radius() == 30, "Fresh replacement configuration session failed");
            } finally {
                player.containerMenu = previousMenu;
                player.setPos(replacementTestPosition);
            }
        } finally {
            level.removeBlock(first, false);
            level.removeBlock(brazierDestination, false);
        }
        try {
            level.setBlockAndUpdate(first, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
            level.setBlockAndUpdate(second, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
            var chest = (RadiantChestBlockEntity) level.getBlockEntity(first);
            var other = (RadiantChestBlockEntity) level.getBlockEntity(second);
            require(StorageNetworks.visible(server, owner, false).isEmpty(), "Ownerless device became visible");
            chest.setOwner(owner);
            other.setOwner(member);
            require(StorageNetworks.visible(server, owner, false).equals(java.util.List.of(chest)), "Installed chest missing from personal network");
            require(StorageNetworks.visible(server, member, false).equals(java.util.List.of(other)), "Unrelated player's storage leaked");
            // A detached deserialization candidate must not replace the actual installed device in discovery.
            var detached = new RadiantChestBlockEntity(first, chest.getBlockState());
            detached.setLevel(level);
            detached.setOwner(owner);
            require(!chest.noNewStacks() && chest.toggleRoutingType() && chest.noNewStacks(),
                "Chest did not toggle its default ANY routing mode");
            var routingSave = save.apply(chest);
            require(routingSave.getInt("routingType") == 1, "Chest routing mode missing from native save");
            load.accept(detached, routingSave);
            require(detached.noNewStacks() && !detached.toggleRoutingType() && detached.noNewStacks(),
                "Chest lost saved routing mode or allowed detached configuration");
            var packet = chest.getUpdatePacket();
            require(packet != null && packet.getTag().getInt("routingType") == 1,
                "Chest update packet omitted routing configuration");
            routingSave.remove("routingType");
            load.accept(detached, routingSave);
            require(!detached.noNewStacks(), "Old chest data did not default to ANY routing");
            require(chest.toggleRoutingType() && !chest.noNewStacks(), "Chest routing toggle did not return to ANY");
            var diamond = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND);
            require(chest.routingWeight(diamond) == 0, "Empty Chest did not receive lowest ordinary priority");
            chest.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.EMERALD));
            require(chest.routingWeight(diamond) == 1, "Unmatched Chest priority did not count occupied slots");
            chest.inventory().setStackInSlot(1, diamond);
            require(chest.routingWeight(diamond) == 501, "Matching Chest lost original weighted preference");
            require(chest.toggleRoutingType() && chest.routingWeight(diamond) == 4999
                && chest.routingWeight(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL)) == -1,
                "NO_NEW_STACKS did not rank existing items and exclude absent ones");
            require(chest.routingWeight(net.minecraft.world.item.ItemStack.EMPTY) == -1
                && chest.inventory().getStackInSlot(1).getCount() == 1 && diamond.getCount() == 1,
                "Chest weight calculation mutated inventory or accepted an empty offer");
            chest.inventory().setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY);
            chest.inventory().setStackInSlot(1, net.minecraft.world.item.ItemStack.EMPTY);
            require(chest.toggleRoutingType(), "Could not restore Chest routing fixture");
            var sword = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD);
            var wornSword = sword.copy();
            wornSword.setDamageValue(12);
            chest.inventory().setStackInSlot(0, wornSword);
            require(chest.routingWeight(sword) > 500 && chest.routingWeight(sword) == chest.routingWeight(wornSword),
                "Chest ranking incorrectly treated tool durability as a legacy item subtype");
            chest.inventory().setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY);
            var menu = new com.aranaira.arcanearchives.inventory.RadiantChestMenu(87, player.getInventory(), chest);
            var previous = player.containerMenu;
            var position = player.position();
            try {
                player.setPos(first.getX() + .5, first.getY(), first.getZ() + .5);
                require(!menu.clickMenuButton(player, 0), "Detached menu changed Chest routing");
                player.containerMenu = menu;
                require(!menu.clickMenuButton(player, -1) && !menu.clickMenuButton(player, 1) && !chest.noNewStacks(),
                    "Invalid menu command changed Chest routing");
                require(menu.clickMenuButton(player, 0) && chest.noNewStacks() && menu.noNewStacks(),
                    "Current Chest menu did not toggle and synchronize routing");
                player.setPos(first.getX() + 20, first.getY(), first.getZ());
                require(!menu.clickMenuButton(player, 0) && chest.noNewStacks(), "Distant menu changed Chest routing");
                player.setPos(first.getX() + .5, first.getY(), first.getZ() + .5);
                require(menu.clickMenuButton(player, 0) && !menu.noNewStacks(), "Menu routing toggle did not return to ANY");
            } finally {
                player.containerMenu = previous;
                player.setPos(position.x, position.y, position.z);
            }
            require(StorageNetworks.visible(server, owner, false).equals(java.util.List.of(chest)), "Detached entity replaced live discovery");
            require(hives.acceptInvitation(owner, member), "Could not form fixture Hive");
            var offer = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND);
            require(com.aranaira.arcanearchives.data.BrazierRoutes.collect(level, first, owner, false, 0, offer).size() == 2,
                "Brazier horizontal radius incorrectly counted height or omitted Hive storage");
            require(com.aranaira.arcanearchives.data.BrazierRoutes.collect(level, first, owner, true, 0, offer)
                .equals(java.util.List.of(chest)), "Personal Brazier routes included Hive devices");
            require(com.aranaira.arcanearchives.data.BrazierRoutes.eligible(level, first.offset(3, 0, 4), owner, false, 5, chest, offer)
                && !com.aranaira.arcanearchives.data.BrazierRoutes.eligible(level, first.offset(3, 0, 4), owner, false, 4, chest, offer)
                && !com.aranaira.arcanearchives.data.BrazierRoutes.eligible(level, first.offset(0, 0, 6), owner, false, 5, chest, offer)
                && !com.aranaira.arcanearchives.data.BrazierRoutes.eligible(level, first, owner, false, 5, detached, offer),
                "Brazier eligibility lost X/Z boundary or live-instance validation");
            var sharedAudience = StorageNetworks.audience(server, member, false);
            var trovePos = second.above();
            require(level.isEmptyBlock(trovePos), "Mixed routing fixture occupied");
            try {
                level.setBlockAndUpdate(trovePos, ContentRegistry.RADIANT_TROVE.get().defaultBlockState());
                var trove = (com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity) level.getBlockEntity(trovePos);
                trove.setOwner(owner);
                trove.inventory().setStackInSlot(0, offer);
                other.inventory().setStackInSlot(0, offer);
                require(other.toggleRoutingType()
                    && com.aranaira.arcanearchives.data.BrazierRoutes.collect(level, first, owner, false, 0, offer)
                        .equals(java.util.List.of(other, trove, chest)), "Mixed destination ranking lost Chest/Trove priority");
                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, first, owner, false, 0, offer, false).isEmpty()
                    && other.inventory().getStackInSlot(0).getCount() == 2
                    && trove.inventory().getStackInSlot(0).getCount() == 1,
                    "NO_NEW_STACKS Chest did not outrank matching Trove during insertion");
                require(other.toggleRoutingType(), "Could not restore ordinary Chest routing");
                require(com.aranaira.arcanearchives.data.BrazierRoutes.collect(level, first, owner, false, 0, offer).get(0) == trove
                    && com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, first, owner, false, 0, offer, false).isEmpty()
                    && trove.inventory().getStackInSlot(0).getCount() == 2
                    && other.inventory().getStackInSlot(0).getCount() == 2 && chest.inventory().getStackInSlot(0).isEmpty(),
                    "Matching Trove did not outrank ordinary Chests during insertion");
            } finally {
                if (level.getBlockEntity(trovePos) instanceof com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity trove)
                    trove.inventory().setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY);
                level.setBlockAndUpdate(trovePos, Blocks.AIR.defaultBlockState());
                other.inventory().setStackInSlot(0, net.minecraft.world.item.ItemStack.EMPTY);
                if (other.noNewStacks()) other.toggleRoutingType();
            }
            int limit = chest.inventory().getStackLimit(0, offer);
            try {
                for (int slot = 0; slot < chest.inventory().getSlots(); slot++) {
                    chest.inventory().setStackInSlot(slot, new net.minecraft.world.item.ItemStack(offer.getItem(), limit));
                    other.inventory().setStackInSlot(slot, new net.minecraft.world.item.ItemStack(offer.getItem(), limit));
                }
                chest.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 3));
                other.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 5));
                var deposit = new net.minecraft.world.item.ItemStack(offer.getItem(), 12);
                require(other.toggleRoutingType()
                    && com.aranaira.arcanearchives.data.BrazierRoutes.collect(level, first, owner, false, 0, offer)
                        .equals(java.util.List.of(other, chest)), "Fresh routes did not prioritize NO_NEW_STACKS over ordinary Chest weight");
                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, first, owner, true, 0, deposit, true).getCount() == 9
                    && other.inventory().getStackInSlot(0).getCount() == limit - 5,
                    "Personal-only simulation counted a preferred Hive destination's capacity");
                var single = new net.minecraft.world.item.ItemStack(offer.getItem());
                var clock = new java.util.concurrent.atomic.AtomicLong();
                var fallbackClock = new java.util.concurrent.atomic.AtomicLong();
                var fallbackCache = new com.aranaira.arcanearchives.data.BrazierRouteCache(fallbackClock::get);
                require(fallbackCache.insert(level, first, owner, true, 0, single, true).isEmpty(),
                    "Could not seed fallback-refresh cache");
                chest.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit));
                fallbackClock.set(999);
                require(fallbackCache.insert(level, first, owner, false, 0, single, false).isEmpty()
                    && other.inventory().getStackInSlot(0).getCount() == limit - 4
                    && chest.inventory().getStackInSlot(0).getCount() == limit,
                    "Full preferred destination blocked fresh fallback insertion");
                chest.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 3));
                fallbackClock.set(1998);
                require(fallbackCache.insert(level, first, owner, false, 0, single, false).isEmpty()
                    && chest.inventory().getStackInSlot(0).getCount() == limit - 2
                    && other.inventory().getStackInSlot(0).getCount() == limit - 4,
                    "Fallback success replaced or failed to refresh the original valid cache entry");
                chest.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 3));
                other.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 5));
                var cache = new com.aranaira.arcanearchives.data.BrazierRouteCache(clock::get);
                require(cache.insert(level, first, owner, true, 0, single, true).isEmpty(), "Could not seed simulated route cache");
                for (long time : new long[]{999, 1998}) {
                    clock.set(time);
                    require(cache.insert(level, first, owner, false, 0, single, false).isEmpty()
                        && chest.inventory().getStackInSlot(0).getCount() == limit - 2
                        && other.inventory().getStackInSlot(0).getCount() == limit - 5,
                        "Unexpired or refreshed cache lost preferred routing priority");
                    chest.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 3));
                }
                clock.set(2998);
                var rangeCache = new com.aranaira.arcanearchives.data.BrazierRouteCache(clock::get);
                var ownershipCache = new com.aranaira.arcanearchives.data.BrazierRouteCache(clock::get);
                require(ownershipCache.insert(level, first, owner, false, 0, single, true).isEmpty(),
                    "Could not seed ownership-revocation cache");
                try {
                    other.setOwner(successor); // Not yet a Hive member; the same installed block remains loaded.
                    require(ownershipCache.insert(level, first, owner, false, 0, single, false).isEmpty()
                        && chest.inventory().getStackInSlot(0).getCount() == limit - 2
                        && other.inventory().getStackInSlot(0).getCount() == limit - 5,
                        "Cached insertion ignored the destination's current inaccessible owner");
                    other.setOwner(member);
                    require(ownershipCache.insert(level, first, owner, false, 0, single, false).isEmpty()
                        && chest.inventory().getStackInSlot(0).getCount() == limit - 1
                        && other.inventory().getStackInSlot(0).getCount() == limit - 5,
                        "Restored destination ownership resurrected the revoked cache preference");
                } finally {
                    other.setOwner(member);
                    chest.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 3));
                }
                var modeCache = new com.aranaira.arcanearchives.data.BrazierRouteCache(clock::get);
                var replacementCache = new com.aranaira.arcanearchives.data.BrazierRouteCache(clock::get);
                require(replacementCache.insert(level, first, owner, false, 0, single, true).isEmpty(),
                    "Could not seed native-replacement cache");
                var replacementChest = new RadiantChestBlockEntity(second, other.getBlockState());
                replacementChest.setLevel(level);
                load.accept(replacementChest, save.apply(other));
                replacementChest.setOwner(successor);
                try {
                    level.setBlockEntity(replacementChest);
                    require(level.getBlockEntity(second) == replacementChest, "Native replacement fixture did not install");
                    require(replacementCache.insert(level, first, owner, false, 0, single, false).isEmpty()
                        && chest.inventory().getStackInSlot(0).getCount() == limit - 2
                        && other.inventory().getStackInSlot(0).getCount() == limit - 5
                        && replacementChest.inventory().getStackInSlot(0).getCount() == limit - 5,
                        "Cached transfer mutated an old instance or its inaccessible replacement");
                } finally {
                    level.setBlockEntity(other);
                    chest.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 3));
                }
                require(modeCache.insert(level, first, owner, false, 0, single, true).isEmpty(), "Could not seed routing-mode cache");
                try {
                    for (int slot = 0; slot < other.inventory().getSlots(); slot++)
                        other.inventory().setStackInSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
                    require(modeCache.insert(level, first, owner, false, 0, single, false).isEmpty()
                        && chest.inventory().getStackInSlot(0).getCount() == limit - 2
                        && other.inventory().getStackInSlot(0).isEmpty(),
                        "Cached NO_NEW_STACKS destination accepted a now-absent item");
                    other.inventory().setStackInSlot(0, single);
                    require(modeCache.insert(level, first, owner, false, 0, single, false).isEmpty()
                        && chest.inventory().getStackInSlot(0).getCount() == limit - 1
                        && other.inventory().getStackInSlot(0).getCount() == 1,
                        "Restored item stock resurrected an evicted routing-mode preference");
                } finally {
                    chest.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 3));
                    for (int slot = 0; slot < other.inventory().getSlots(); slot++)
                        other.inventory().setStackInSlot(slot, new net.minecraft.world.item.ItemStack(offer.getItem(), slot == 0 ? limit - 5 : limit));
                }
                require(rangeCache.insert(level, first.east(), owner, true, 1, single, true).isEmpty(),
                    "Could not seed radius-boundary cache");
                require(rangeCache.insert(level, first.east(), owner, false, 0, single, false).getCount() == 1
                    && chest.inventory().getStackInSlot(0).getCount() == limit - 3
                    && other.inventory().getStackInSlot(0).getCount() == limit - 5,
                    "Reduced radius allowed cached insertion or lost rejected input");
                require(rangeCache.insert(level, first.east(), owner, false, 1, single, false).isEmpty()
                    && other.inventory().getStackInSlot(0).getCount() == limit - 4
                    && chest.inventory().getStackInSlot(0).getCount() == limit - 3,
                    "Restored radius resurrected an evicted cache instead of fresh priority");
                other.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 5));
                require(cache.insert(level, first, owner, false, 0, single, false).isEmpty()
                    && other.inventory().getStackInSlot(0).getCount() == limit - 4,
                    "Cache did not expire at the original 1000-ms boundary");
                require(cache.insert(level, first, owner, true, 0, single, false).isEmpty()
                    && chest.inventory().getStackInSlot(0).getCount() == limit - 2
                    && other.inventory().getStackInSlot(0).getCount() == limit - 4,
                    "Cached Hive destination survived personal-mode revocation");
                chest.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 3));
                other.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 5));
                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, first, owner, false, 0, deposit, chest, true).getCount() == 4,
                    "Preferred route simulation counted the same capacity twice");
                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, first, owner, true, 0, deposit, other, true).getCount() == 9,
                    "Preferred Hive route bypassed personal-only eligibility");
                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, first, owner, false, 0, single, chest, false).isEmpty()
                    && chest.inventory().getStackInSlot(0).getCount() == limit - 2
                    && other.inventory().getStackInSlot(0).getCount() == limit - 5,
                    "Eligible preferred route lost priority to a higher fresh weight");
                chest.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 3));
                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, first, owner, false, 0, single, false).isEmpty()
                    && other.inventory().getStackInSlot(0).getCount() == limit - 4
                    && chest.inventory().getStackInSlot(0).getCount() == limit - 3,
                    "Fresh deposit ignored preferred destination order");
                other.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 5));
                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, first, owner, true, 0, single, false).isEmpty()
                    && chest.inventory().getStackInSlot(0).getCount() == limit - 2
                    && other.inventory().getStackInSlot(0).getCount() == limit - 5,
                    "Personal-only deposit wrote to a preferred Hive destination");
                chest.inventory().setStackInSlot(0, new net.minecraft.world.item.ItemStack(offer.getItem(), limit - 3));
                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, first, owner, false, 0, deposit, true).getCount() == 4
                    && chest.inventory().getStackInSlot(0).getCount() == limit - 3
                    && other.inventory().getStackInSlot(0).getCount() == limit - 5,
                    "Brazier simulation mutated destinations or lost partial remainder");
                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, first, owner, false, 0, deposit, false).getCount() == 4
                    && chest.inventory().getStackInSlot(0).getCount() == limit
                    && other.inventory().getStackInSlot(0).getCount() == limit && deposit.getCount() == 12,
                    "Brazier deposit did not conserve split acceptance and offered input");
                require(com.aranaira.arcanearchives.data.BrazierRoutes.insert(level, first, owner, false, 0, deposit, false).getCount() == 12,
                    "Full destinations lost a rejected Brazier deposit");
            } finally {
                for (int slot = 0; slot < chest.inventory().getSlots(); slot++) {
                    chest.inventory().setStackInSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
                    other.inventory().setStackInSlot(slot, net.minecraft.world.item.ItemStack.EMPTY);
                }
                if (other.noNewStacks()) other.toggleRoutingType();
            }
            require(sharedAudience.equals(java.util.Set.of(owner, member)), "Shared permission snapshot omitted a Hive owner");
            require(StorageNetworks.audience(server, member, true).equals(java.util.Set.of(member)),
                "Personal permission snapshot included another owner");
            try {
                sharedAudience.clear();
                throw new AssertionError("Permission snapshot was mutable");
            } catch (UnsupportedOperationException expected) { }
            require(StorageNetworks.visible(server, member, false).containsAll(java.util.List.of(chest, other))
                && StorageNetworks.visible(server, member, false).size() == 2, "Hive storage was not shared exactly once");
            require(StorageNetworks.visible(server, member, true).equals(java.util.List.of(other)), "Personal mode included Hive devices");
            require(hives.acceptInvitation(owner, successor) && hives.resign(owner), "Could not exercise succession");
            require(StorageNetworks.audience(server, successor, false).equals(java.util.Set.of(member, successor)),
                "Current permissions retained the departed owner");
            require(sharedAudience.equals(java.util.Set.of(owner, member)), "Later membership mutated an earlier permission snapshot");
            require(StorageNetworks.visible(server, successor, false).equals(java.util.List.of(other)), "Departed owner's storage survived succession");
            require(StorageNetworks.visible(server, owner, false).equals(java.util.List.of(chest)), "Departed owner retained Hive access");
            require(!com.aranaira.arcanearchives.data.BrazierRoutes.eligible(level, first, owner, false, 300, other, offer),
                "Cached destination eligibility retained a departed owner's Hive access");
            require(hives.resign(successor), "Could not disband fixture Hive");
            require(StorageNetworks.audience(server, successor, false).equals(java.util.Set.of(successor)),
                "Closed-menu-independent permissions retained a disbanded Hive");
            require(StorageNetworks.visible(server, successor, false).isEmpty(), "Revoked player retained storage visibility");
            other.setOwner(owner);
            require(StorageNetworks.visible(server, owner, true).size() == 2, "Live ownership change was cached");
            level.setBlockAndUpdate(first, Blocks.STONE.defaultBlockState());
            require(StorageNetworks.visible(server, owner, false).equals(java.util.List.of(other)), "Removed chest retained discovery");
            level.setBlockAndUpdate(first, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
            var replacement = (RadiantChestBlockEntity) level.getBlockEntity(first);
            replacement.setOwner(owner);
            require(!StorageNetworks.visible(server, owner, false).contains(chest)
                && StorageNetworks.visible(server, owner, false).contains(replacement), "Replacement retained stale identity");
            // Exercise the real removal/reinstallation path independently of block placement.
            level.removeBlockEntity(first);
            require(!StorageNetworks.visible(server, owner, false).contains(replacement), "Native entity removal retained discovery");
            level.setBlockEntity(replacement);
            require(StorageNetworks.visible(server, owner, false).contains(replacement), "Native entity reinstallation was not discovered");
        } finally {
            hives.resign(successor);
            hives.resign(member);
            hives.resign(owner);
            level.setBlockAndUpdate(first, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(second, Blocks.AIR.defaultBlockState());
        }
        require(StorageNetworks.visible(server, owner, false).isEmpty(), "Fixture cleanup left network devices");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
