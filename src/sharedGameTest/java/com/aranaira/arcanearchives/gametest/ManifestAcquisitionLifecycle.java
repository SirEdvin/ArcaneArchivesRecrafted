package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.blocks.GemCuttersTable;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.GemCuttersTableMenu;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import com.aranaira.arcanearchives.tileentities.GemCuttersTableBlockEntity;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Acquire registered network tools from shipped recipes through the native Gem Cutter menu. */
public final class ManifestAcquisitionLifecycle {
    public static void run(GameTestHelper helper, Player player) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        var state = ContentRegistry.GEMCUTTERS_TABLE.get().defaultBlockState();
        var other = GemCuttersTable.connectedPos(pos, state);
        require(level.isEmptyBlock(pos) && level.isEmptyBlock(other), "Manifest acquisition fixture occupied");
        var previousMenu = player.containerMenu;
        var inventory = new ArrayList<ItemStack>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        GemCuttersTableBlockEntity table = null;
        try {
            player.getInventory().clearContent();
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            level.setBlock(other, state.setValue(GemCuttersTable.ACCESSOR, true), Block.UPDATE_CLIENTS);
            table = (GemCuttersTableBlockEntity) level.getBlockEntity(pos);
            player.setPos(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
            var menu = (GemCuttersTableMenu) table.createMenu(1, player.getInventory(), player);
            player.containerMenu = menu;
            MatrixReservoirCrafting.selectRecipe(menu, player, ContentRegistry.MANIFEST.get(), GemCutterDataRecipe.entries(level.getRecipeManager()).size());
            table.insertInput(0, new ItemStack(Items.PAPER), false);
            table.insertInput(1, new ItemStack(Items.BLACK_DYE), false);
            table.insertInput(2, new ItemStack(ContentRegistry.RADIANT_DUST.get()), false);
            require(!menu.clickMenuButton(player, 2) && table.getOutput().isEmpty()
                && table.getInput(0).getCount() == 1 && table.getInput(1).getCount() == 1 && table.getInput(2).getCount() == 1,
                "Manifest crafted with insufficient dust or consumed partial payment");
            table.insertInput(2, new ItemStack(ContentRegistry.RADIANT_DUST.get()), false);
            require(menu.clickMenuButton(player, 2) && table.getOutput().is(ContentRegistry.MANIFEST.get())
                && table.getOutput().getCount() == 1, "Paid Manifest recipe failed");
            require(table.getInput(0).isEmpty() && table.getInput(1).isEmpty() && table.getInput(2).isEmpty(), "Manifest recipe payment changed");
            menu.clicked(0, 0, ClickType.PICKUP, player);
            require(menu.getCarried().is(ContentRegistry.MANIFEST.get()) && menu.getCarried().getCount() == 1
                && table.getOutput().isEmpty(), "Paid Manifest was not extractable exactly once");
            menu.clicked(0, 0, ClickType.PICKUP, player);
            require(menu.getCarried().getCount() == 1 && !menu.clickMenuButton(player, 2), "Manifest extraction fabricated another item");
            menu.setCarried(ItemStack.EMPTY);
            MatrixReservoirCrafting.selectRecipe(menu, player, ContentRegistry.BRAZIER_ITEM.get(), GemCutterDataRecipe.entries(level.getRecipeManager()).size());
            for (var fuel : new net.minecraft.world.item.Item[]{Items.COAL, Items.CHARCOAL}) {
                table.insertInput(0, new ItemStack(ContentRegistry.RADIANT_DUST.get(), 4), false);
                table.insertInput(1, new ItemStack(fuel, 8), false);
                table.insertInput(2, new ItemStack(Items.GOLD_INGOT, 2), false);
                table.insertInput(3, new ItemStack(Items.OAK_LOG, 2), false);
                require(!menu.clickMenuButton(player, 2) && table.getOutput().isEmpty()
                    && table.getInput(0).getCount() == 4 && table.getInput(1).getCount() == 8
                    && table.getInput(2).getCount() == 2 && table.getInput(3).getCount() == 2,
                    "Brazier missing-log craft consumed partial payment");
                table.insertInput(3, new ItemStack(Items.OAK_LOG), false);
                require(menu.clickMenuButton(player, 2) && table.getOutput().is(ContentRegistry.BRAZIER_ITEM.get())
                    && table.getOutput().getCount() == 1, "Paid Brazier recipe rejected coal/charcoal");
                for (int slot = 0; slot < 4; slot++) require(table.getInput(slot).isEmpty(), "Brazier payment changed");
                menu.clicked(0, 0, ClickType.PICKUP, player);
                require(menu.getCarried().is(ContentRegistry.BRAZIER_ITEM.get()) && menu.getCarried().getCount() == 1
                    && table.getOutput().isEmpty(), "Paid Brazier output not extractable exactly once");
                menu.clicked(0, 0, ClickType.PICKUP, player);
                require(menu.getCarried().getCount() == 1 && !menu.clickMenuButton(player, 2), "Brazier extraction fabricated output");
                menu.setCarried(ItemStack.EMPTY);
            }
        } finally {
            player.containerMenu = previousMenu;
            if (table != null) level.removeBlockEntity(pos);
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(other, Blocks.AIR.defaultBlockState());
            for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
