package com.aranaira.arcanearchives.items;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;

public final class MindspindleItem extends ArcaneGemItem {
    public MindspindleItem() { super("mindspindle", 800, 3600); }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gem = player.getItemInHand(hand);
        GemRecharge.recharge(player, gem);
        return InteractionResultHolder.success(gem);
    }

    public static int amplifyPickup(Player player, int value) {
        if (!(player.level() instanceof ServerLevel level) || !level.getServer().isSameThread()
                || !player.isAlive() || player.isSpectator() || value <= 0) return value;
        for (ItemStack gem : AvailableGems.get(player)) {
            if (gem.getItem() instanceof OrderstoneItem) {
                setCharge(gem, charge(gem) + Math.min(value, maximumCharge(gem) - charge(gem)));
                AvailableGems.changed(player);
            }
            if (gem.getItem() instanceof MindspindleItem && charge(gem) > 0) {
                int cost = value;
                value = Math.round(value * 1.5F);
                GemRecharge.consume(player, gem, cost);
                player.getInventory().setChanged();
            }
        }
        return value;
    }
}
