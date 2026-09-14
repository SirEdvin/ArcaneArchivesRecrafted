package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.data.PlayerSaveData;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.ManifestMenu;
import java.util.function.Function;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Native crafting payment and original one-time receipt, not connected book rendering. */
public final class TomeAcquisitionLifecycle {
    public static void run(GameTestHelper helper, Player player, Function<TransientCraftingContainer, ItemStack> craft) {
        var level = helper.getLevel();
        TomeContentLifecycle.run(level);
        for (var chest : new net.minecraft.world.item.Item[]{Items.CHEST, Items.TRAPPED_CHEST}) {
            var matrix = matrix(player);
            matrix.setItem(0, new ItemStack(chest));
            matrix.setItem(8, new ItemStack(ContentRegistry.RAW_QUARTZ.get()));
            var result = take(player, matrix, craft.apply(matrix));
            require(result.is(ContentRegistry.RADIANT_CHEST_ITEM.get()) && result.getCount() == 1 && matrix.isEmpty(),
                "Radiant Chest shapeless acquisition/payment failed");
        }
        for (var planks : new net.minecraft.world.item.Item[]{Items.OAK_PLANKS, Items.BIRCH_PLANKS}) {
            var matrix = matrix(player);
            for (int slot = 0; slot < 9; slot++) matrix.setItem(slot,
                new ItemStack(slot == 4 ? ContentRegistry.RAW_QUARTZ.get() : planks));
            var result = take(player, matrix, craft.apply(matrix));
            require(result.is(ContentRegistry.RADIANT_CHEST_ITEM.get()) && result.getCount() == 1 && matrix.isEmpty(),
                "Radiant Chest shaped acquisition/payment failed");
        }
        var receipt = PlayerSaveData.get(level.getServer(), player.getUUID());
        require(!receipt.hasReceivedBook(), "Tome fixture needs a fresh receipt");
        require(com.aranaira.arcanearchives.config.ServerSideConfig.current().bookFromResonator(), "Tome fixture needs default grant setting");
        var area = player.getBoundingBox().inflate(2);
        var oldEntities = level.getEntitiesOfClass(ItemEntity.class, area).stream().map(ItemEntity::getUUID).toList();
        try {
            for (int attempt = 0; attempt < 2; attempt++) {
                var matrix = matrix(player);
                matrix.setItem(0, new ItemStack(Items.GOLD_INGOT));
                matrix.setItem(2, new ItemStack(Items.GOLD_INGOT));
                matrix.setItem(1, new ItemStack(Items.IRON_BARS));
                matrix.setItem(4, new ItemStack(Items.WATER_BUCKET));
                for (int slot : new int[]{3, 5, 6, 8}) matrix.setItem(slot, new ItemStack(Items.OAK_LOG));
                var taken = take(player, matrix, craft.apply(matrix));
                require(taken.is(ContentRegistry.RADIANT_RESONATOR_ITEM.get()), "Native resonator acquisition failed");
                require(matrix.getItem(4).is(Items.BUCKET) && matrix.getItem(4).getCount() == 1, "Resonator bucket remainder changed");
                for (int slot : new int[]{0, 1, 2, 3, 5, 6, 7, 8}) require(matrix.getItem(slot).isEmpty(), "Resonator payment changed");
                var drops = level.getEntitiesOfClass(ItemEntity.class, area, e -> !oldEntities.contains(e.getUUID())
                    && e.getItem().is(ContentRegistry.TOME_OF_ARCANA.get()));
                require(receipt.hasReceivedBook() && drops.size() == 1 && drops.get(0).getItem().getCount() == 1
                    && !drops.get(0).hasPickUpDelay(), "Resonator grant missing, delayed or repeated");
            }
            for (int slot : new int[]{0, 8}) {
                var matrix = matrix(player);
                matrix.setItem(slot, new ItemStack(Items.BOOK));
                matrix.setItem(4, new ItemStack(Items.GOLD_NUGGET));
                var tome = take(player, matrix, craft.apply(matrix));
                require(tome.is(ContentRegistry.TOME_OF_ARCANA.get()) && tome.getCount() == 1
                    && tome.getMaxStackSize() == 1 && matrix.isEmpty(), "Tome shapeless recipe/payment changed");
                require(ItemStack.matches(tome, tome.copy()), "Tome copy lost identity binding");
                for (var hand : InteractionHand.values()) {
                    var old = player.getItemInHand(hand);
                    try {
                        player.setItemInHand(hand, tome);
                        var result = tome.use(level, player, hand);
                        require(result.getResult() == InteractionResult.FAIL && result.getObject() == tome
                            && tome.getCount() == 1, "Server Tome use consumed/replaced held stack or opened client code");
                    } finally { player.setItemInHand(hand, old); }
                }
            }
        } finally {
            level.getEntitiesOfClass(ItemEntity.class, area, e -> !oldEntities.contains(e.getUUID())
                && e.getItem().is(ContentRegistry.TOME_OF_ARCANA.get())).forEach(ItemEntity::discard);
        }
    }

    private static TransientCraftingContainer matrix(Player player) {
        return new TransientCraftingContainer(new ManifestMenu(1, player.getInventory()), 3, 3);
    }
    private static ItemStack take(Player player, TransientCraftingContainer matrix, ItemStack output) {
        var result = new ResultContainer();
        result.setItem(0, output);
        var slot = new ResultSlot(player, matrix, result, 0, 0, 0);
        var taken = slot.remove(1);
        slot.onTake(player, taken);
        require(result.isEmpty(), "Native result retained a second output");
        return taken;
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
