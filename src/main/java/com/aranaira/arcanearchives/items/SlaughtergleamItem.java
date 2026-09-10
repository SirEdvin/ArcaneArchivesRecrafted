package com.aranaira.arcanearchives.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.Level;

public final class SlaughtergleamItem extends ArcaneGemItem {
    public SlaughtergleamItem() { super("slaughtergleam", 30, 150); }
    @Override public boolean hasToggleMode() { return true; }
    private static boolean available(Player player) {
        return player.level() instanceof ServerLevel level && level.getServer().isSameThread()
            && player.isAlive() && !player.isSpectator();
    }
    public static int lootingBonus(Entity attacker) {
        if (!(attacker instanceof Player player) || !available(player)) return 0;
        int bonus = 0;
        for (ItemStack gem : AvailableGems.get(player)) {
            if (gem.getItem() instanceof SlaughtergleamItem && charge(gem) > 0) bonus++;
        }
        return bonus;
    }
    public static void onDeath(LivingEntity victim, DamageSource source) {
        if (!(victim.level() instanceof ServerLevel) || !(source.getEntity() instanceof Player player)
                || !available(player)) return;
        for (ItemStack gem : AvailableGems.get(player)) {
            if (gem.getItem() instanceof SlaughtergleamItem && charge(gem) > 0) {
                GemRecharge.consume(player, gem, 1);
                player.getInventory().setChanged();
            }
        }
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gem = player.getItemInHand(hand);
        GemRecharge.rechargeMaterial(player, gem);
        return InteractionResultHolder.success(gem);
    }
}
