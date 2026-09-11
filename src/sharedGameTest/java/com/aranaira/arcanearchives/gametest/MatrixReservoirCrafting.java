package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.blocks.GemCuttersTable;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.GemCuttersTableMenu;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import com.aranaira.arcanearchives.tileentities.GemCuttersTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/** All loaders exercise the shipped recipe through the real table entity and menu, not a replacement catalog. */
public final class MatrixReservoirCrafting {
    private MatrixReservoirCrafting() {}

    public static void run(GameTestHelper helper, Player player, Player second) {
        var offered = com.aranaira.arcanearchives.recipe.CraftingCreator.withCreator(
            new ItemStack(ContentRegistry.RADIANT_TANK_ITEM.get()), new java.util.UUID(0, 1), "Return owner");
        var original = offered.copy();
        var returned = com.aranaira.arcanearchives.recipe.gct.GemCutterFluidRemainders.prepare(offered).orElseThrow();
        require(returned != offered && returned.getCount() == 1
            && com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(original, returned),
            "Empty Tank return lost item data or aliases its input");
        returned.setCount(0);
        require(offered.getCount() == 1
            && com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler.sameItemAndData(original, offered),
            "Return preparation mutated caller-owned Tank");
        require(com.aranaira.arcanearchives.recipe.gct.GemCutterFluidRemainders.prepare(offered.copyWithCount(2)).isEmpty(),
            "Multi-container preparation bypassed the single-unit boundary");
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var state = ContentRegistry.GEMCUTTERS_TABLE.get().defaultBlockState();
        BlockPos other = GemCuttersTable.connectedPos(pos, state);
        require(level.isEmptyBlock(pos) && level.isEmptyBlock(other), "Crafting fixture space is occupied");
        var previousMenu = player.containerMenu;
        GemCuttersTableBlockEntity table = null;
        try {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            level.setBlock(other, state.setValue(GemCuttersTable.ACCESSOR, true), Block.UPDATE_CLIENTS);
            table = (GemCuttersTableBlockEntity) level.getBlockEntity(pos);
            require(table != null, "Native Gem Cutter entity missing");
            player.setPos(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
            var menu = (GemCuttersTableMenu) table.createMenu(1, player.getInventory(), player);
            require(menu != null, "Native table refused valid crafting fixture");
            player.containerMenu = menu;
            selectRecipe(menu, player, ContentRegistry.MATRIX_RESERVOIR_ITEM.get(), GemCutterDataRecipe.entries(level.getRecipeManager()).size());

            Item[] items = {ContentRegistry.MATRIX_BRACE.get(), ContentRegistry.CONTAINMENT_FIELD.get(),
                ContentRegistry.EMPOWERED_QUARTZ.get(), ContentRegistry.RADIANT_TANK_ITEM.get()};
            int[] costs = {2, 1, 6, 10};
            for (int missing = 0; missing < costs.length; missing++) {
                clear(table);
                for (int slot = 0; slot < costs.length; slot++) {
                    int count = costs[slot] - (slot == missing ? 1 : 0);
                    if (count > 0) require(table.insertInput(slot, new ItemStack(items[slot], count), false).isEmpty(), "Input rejected");
                }
                menu.broadcastChanges();
                menu.clickMenuButton(player, 2);
                require(menu.getCarried().isEmpty(), "Recipe crafted with insufficient ingredient " + missing);
                for (int slot = 0; slot < costs.length; slot++)
                    require(table.getInput(slot).getCount() == costs[slot] - (slot == missing ? 1 : 0), "Failed payment consumed inputs");
            }

            clear(table);
            for (int slot = 0; slot < costs.length; slot++)
                require(table.insertInput(slot, new ItemStack(items[slot], costs[slot] - (slot == 3 ? 1 : 0)), false).isEmpty(), "Input rejected");
            require(table.insertInput(17, new ItemStack(Items.DIAMOND, 3), false).isEmpty(), "Unrelated input rejected");
            player.getInventory().setItem(8, new ItemStack(items[3]));
            menu.broadcastChanges();
            require(menu.getSlot(62).getItem().is(ContentRegistry.MATRIX_RESERVOIR_ITEM.get()), "Paid recipe preview missing");
            require(menu.getSlot(62).getItem().getCount() == 1, "Recipe output count changed");

            menu.setCarried(new ItemStack(Items.GOLD_INGOT));
            menu.clicked(0, 0, ClickType.PICKUP, player);
            require(menu.getCarried().is(Items.GOLD_INGOT) && menu.getCarried().getCount() == 1, "Incompatible cursor changed");
            menu.setCarried(ItemStack.EMPTY);
            player.containerMenu = previousMenu;
            menu.clickMenuButton(player, 2);
            require(menu.getCarried().isEmpty(), "Detached menu authorized crafting");
            player.containerMenu = menu;
            player.setPos(pos.getX() + 20, pos.getY(), pos.getZ());
            menu.clickMenuButton(player, 2);
            require(menu.getCarried().isEmpty(), "Out-of-range menu authorized crafting");
            for (int slot = 0; slot < costs.length; slot++)
                require(table.getInput(slot).getCount() == costs[slot] - (slot == 3 ? 1 : 0), "Rejected crafting consumed table inputs");
            require(player.getInventory().getItem(8).getCount() == 1, "Rejected crafting consumed player payment");

            player.setPos(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
            menu.clickMenuButton(player, 2);
            require(table.getOutput().is(ContentRegistry.MATRIX_RESERVOIR_ITEM.get()) && table.getOutput().getCount() == 1
                && menu.getCarried().isEmpty(), "Exact payment did not store one Reservoir");
            // Reopening receives the existing output; it neither crafts nor clears it.
            menu = (GemCuttersTableMenu) table.createMenu(2, player.getInventory(), player);
            player.containerMenu = menu;
            require(menu.getSlot(0).getItem().getCount() == 1, "Reopened menu lost saved output");
            require(!menu.getSlot(0).mayPlace(new ItemStack(Items.GOLD_INGOT)), "Output accepts insertion");
            menu.setCarried(new ItemStack(Items.GOLD_INGOT));
            menu.clicked(0, 0, ClickType.PICKUP, player);
            require(menu.getCarried().is(Items.GOLD_INGOT) && table.getOutput().getCount() == 1,
                "Native click inserted into or replaced output");
            menu.setCarried(ItemStack.EMPTY);
            menu.clicked(0, 0, ClickType.PICKUP, player);
            require(table.getOutput().isEmpty() && menu.getCarried().is(ContentRegistry.MATRIX_RESERVOIR_ITEM.get())
                && menu.getCarried().getCount() == 1, "Native extraction did not transfer existing output");
            // Upstream returns fluid-capable ingredients, including empty Radiant Tanks.
            int returnedTanks = 0;
            int preservedDiamonds = 0;
            for (int slot = 0; slot < 18; slot++) {
                ItemStack remaining = table.getInput(slot);
                require(remaining.isEmpty() || remaining.is(items[3]) || remaining.is(Items.DIAMOND), "Non-container payment was not consumed");
                if (remaining.is(items[3])) returnedTanks += remaining.getCount();
                if (remaining.is(Items.DIAMOND)) preservedDiamonds += remaining.getCount();
            }
            require(returnedTanks == 10, "Empty Tank returns were lost or duplicated");
            require(preservedDiamonds == 3, "Unrelated input changed");
            require(player.getInventory().getItem(8).isEmpty(), "Split player payment not consumed");
            require(table.getInput(17).is(Items.DIAMOND) && table.getInput(17).getCount() == 3, "Unrelated input changed");
            menu.clicked(0, 0, ClickType.PICKUP, player);
            require(menu.getCarried().getCount() == 1 && table.getOutput().isEmpty(), "Repeated extraction duplicated Reservoir");
            menu.setCarried(ItemStack.EMPTY);
            outputSlots(helper, player, table, menu);
            GemCutterMenuSynchronization.run(player, second, table, menu);
            simultaneousViewers(helper, player, second, table, menu);
        } finally {
            player.containerMenu = previousMenu;
            player.getInventory().clearContent();
            if (table != null && !table.isRemoved()) clear(table);
            level.removeBlock(pos, false);
            level.removeBlock(other, false);
        }
        helper.succeed();
    }

    private static void selectRecipe(GemCuttersTableMenu menu, Player player, Item output, int recipeCount) {
        for (int page = 0; page <= recipeCount / 7; page++) {
            for (int slot = 55; slot <= 61; slot++) {
                if (menu.getSlot(slot).getItem().is(output)) {
                    menu.clicked(slot, 0, ClickType.PICKUP, player);
                    return;
                }
            }
            require(menu.clickMenuButton(player, 1), "Recipe page navigation failed");
        }
        throw new AssertionError("Shipped recipe is absent from the native catalog: " + output);
    }

    private static void outputSlots(GameTestHelper helper, Player player, GemCuttersTableBlockEntity table,
            GemCuttersTableMenu menu) {
        clear(table);
        player.getInventory().clearContent();
        var lantern = ContentRegistry.RADIANT_LANTERN_ITEM.get();
        selectRecipe(menu, player, lantern, GemCutterDataRecipe.entries(helper.getLevel().getRecipeManager()).size());
        require(table.insertInput(0, new ItemStack(ContentRegistry.RAW_QUARTZ.get(), 2 * 17), false).isEmpty(), "Quartz rejected");
        require(table.insertInput(1, new ItemStack(Items.GOLD_NUGGET, 17), false).isEmpty(), "Gold rejected");
        for (int craft = 0; craft < 16; craft++) require(menu.clickMenuButton(player, 2), "Paid batch craft failed");
        require(table.getOutput().is(lantern) && table.getOutput().getCount() == 64, "Output did not stack to native capacity");
        require(!menu.clickMenuButton(player, 2), "Full output accepted another craft");
        require(table.getInput(0).getCount() == 2 && table.getInput(1).getCount() == 1, "Full output consumed payment");

        player.getInventory().setItem(2, new ItemStack(Items.GOLD_INGOT));
        menu.clicked(0, 2, ClickType.SWAP, player);
        require(table.getOutput().getCount() == 64 && player.getInventory().getItem(2).is(Items.GOLD_INGOT),
            "Hotbar swap inserted into output");
        menu.setCarried(new ItemStack(lantern, 64));
        menu.clicked(0, 0, ClickType.PICKUP, player);
        require(table.getOutput().getCount() == 64 && menu.getCarried().getCount() == 64, "Full cursor changed output");
        menu.setCarried(ItemStack.EMPTY);
        menu.clicked(0, 1, ClickType.PICKUP, player);
        require(table.getOutput().getCount() == 32 && menu.getCarried().getCount() == 32, "Right-click extraction did not split output");
        player.getInventory().setItem(2, ItemStack.EMPTY);
        menu.clicked(0, 2, ClickType.SWAP, player);
        require(table.getOutput().isEmpty() && player.getInventory().getItem(2).getCount() == 32, "Hotbar extraction lost output");
        menu.clicked(0, 0, ClickType.PICKUP, player);
        require(table.getOutput().isEmpty() && table.getInput(0).getCount() == 2, "Extraction crafted another batch");
        require(menu.clickMenuButton(player, 2) && table.getOutput().getCount() == 4, "Explicit craft did not resume after extraction");
        menu.setCarried(ItemStack.EMPTY);
        for (int slot = 0; slot < 36; slot++) player.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        player.getInventory().setItem(8, new ItemStack(lantern, 63));
        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
        require(table.getOutput().getCount() == 3 && player.getInventory().getItem(8).getCount() == 64,
            "Partial shift extraction lost or duplicated output");
        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
        require(table.getOutput().getCount() == 3, "Full inventory discarded output");
        player.getInventory().setItem(8, ItemStack.EMPTY);
        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);
        require(table.getOutput().isEmpty() && player.getInventory().getItem(8).getCount() == 3,
            "Final extraction lost remainder");
        require(table.getInput(0).isEmpty() && table.getInput(1).isEmpty(), "Payment count changed during extraction");
    }

    /** Two real native menus on one inventory; calls are serialized on the server thread. */
    private static void simultaneousViewers(GameTestHelper helper, Player first, Player second,
            GemCuttersTableBlockEntity table, GemCuttersTableMenu firstMenu) {
        require(!first.getUUID().equals(second.getUUID()), "Viewer fixture needs distinct player identities");
        var previous = second.containerMenu;
        var pos = table.getBlockPos();
        var level = helper.getLevel();
        var lantern = ContentRegistry.RADIANT_LANTERN_ITEM.get();
        second.setPos(first.getX(), first.getY(), first.getZ());
        first.getInventory().clearContent();
        second.getInventory().clearContent();
        clear(table);
        var secondMenu = (GemCuttersTableMenu) table.createMenu(3, second.getInventory(), second);
        require(secondMenu != null, "Second viewer could not open the shared table");
        second.containerMenu = secondMenu;
        try {
            selectRecipe(secondMenu, second, lantern, GemCutterDataRecipe.entries(level.getRecipeManager()).size());
            table.insertInput(0, new ItemStack(ContentRegistry.RAW_QUARTZ.get(), 2), false);
            first.getInventory().setItem(8, new ItemStack(Items.GOLD_NUGGET));
            second.getInventory().setItem(8, new ItemStack(Items.GOLD_NUGGET));
            require(!firstMenu.clickMenuButton(second, 2), "Another viewer submitted this menu's craft action");
            require(firstMenu.clickMenuButton(first, 2), "First viewer's valid payment failed");
            require(!secondMenu.clickMenuButton(second, 2), "Second viewer reused consumed shared ingredients");
            require(first.getInventory().getItem(8).isEmpty() && second.getInventory().getItem(8).getCount() == 1,
                "Failed second craft consumed the wrong viewer's ingredients");
            require(firstMenu.getSlot(0).getItem().getCount() == 4 && secondMenu.getSlot(0).getItem().getCount() == 4,
                "Viewers do not share one completed-output inventory");
            firstMenu.clicked(0, 0, ClickType.PICKUP, second);
            require(firstMenu.getCarried().isEmpty() && table.getOutput().getCount() == 4,
                "Another viewer extracted through this menu");
            firstMenu.clicked(0, 0, ClickType.PICKUP, first);
            // No second-menu broadcast: a delayed click must still read current server inventory.
            secondMenu.clicked(0, 0, ClickType.PICKUP, second);
            require(firstMenu.getCarried().getCount() == 4 && secondMenu.getCarried().isEmpty() && table.getOutput().isEmpty(),
                "Stale second-viewer extraction duplicated output");

            table.insertInput(0, new ItemStack(ContentRegistry.RAW_QUARTZ.get(), 2), false);
            first.getInventory().setItem(8, new ItemStack(Items.GOLD_NUGGET));
            require(firstMenu.clickMenuButton(first, 2), "Second paid batch failed");
            second.containerMenu = previous;
            secondMenu.clicked(0, 0, ClickType.PICKUP, second);
            require(secondMenu.quickMoveStack(second, 0).isEmpty() && !secondMenu.clickMenuButton(second, 2)
                && table.getOutput().getCount() == 4, "Closed menu retained crafting/extraction authority");
            second.containerMenu = secondMenu;
            second.setPos(pos.getX() + 20, pos.getY(), pos.getZ());
            secondMenu.clicked(0, 0, ClickType.PICKUP, second);
            require(secondMenu.getCarried().isEmpty() && table.getOutput().getCount() == 4, "Distant viewer extracted output");
            second.setPos(first.getX(), first.getY(), first.getZ());
            secondMenu.clicked(0, 0, ClickType.PICKUP, second);
            require(firstMenu.getCarried().getCount() == 4 && secondMenu.getCarried().getCount() == 4 && table.getOutput().isEmpty(),
                "Alternating viewers lost or duplicated paid batches");

            table.insertInput(0, new ItemStack(ContentRegistry.RAW_QUARTZ.get(), 2), false);
            first.getInventory().setItem(8, new ItemStack(Items.GOLD_NUGGET));
            require(firstMenu.clickMenuButton(first, 2), "Removal batch failed");
            level.removeBlock(pos, false);
            firstMenu.clicked(0, 0, ClickType.PICKUP, first);
            secondMenu.clicked(0, 0, ClickType.QUICK_MOVE, second);
            require(!firstMenu.clickMenuButton(first, 2) && !secondMenu.clickMenuButton(second, 2), "Removed table still crafted");
            require(firstMenu.getCarried().getCount() == 4 && secondMenu.getCarried().getCount() == 4,
                "Removed table granted extra output to an open viewer");
            var drops = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(3));
            require(drops.stream().filter(e -> e.getItem().is(lantern)).mapToInt(e -> e.getItem().getCount()).sum() == 4,
                "Removal lost or duplicated the unclaimed batch");
        } finally {
            firstMenu.setCarried(ItemStack.EMPTY);
            secondMenu.setCarried(ItemStack.EMPTY);
            second.containerMenu = previous;
            second.getInventory().clearContent();
            level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(3))
                .forEach(net.minecraft.world.entity.Entity::discard);
        }
    }

    private static void clear(GemCuttersTableBlockEntity table) {
        for (int slot = 0; slot < 18; slot++) table.extractInput(slot, 64, false);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
