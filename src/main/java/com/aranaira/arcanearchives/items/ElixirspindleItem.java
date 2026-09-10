package com.aranaira.arcanearchives.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;

public final class ElixirspindleItem extends ArcaneGemItem {
    public ElixirspindleItem() { super("elixirspindle", 5, 20); }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gem = player.getItemInHand(hand);
        GemRecharge.recharge(player, gem);
        return InteractionResultHolder.success(gem);
    }

    public static boolean intercept(LivingEntity entity) {
        if (!(entity instanceof Player player) || !player.isAlive() || player.isSpectator()
                || !(player.getMainHandItem().getItem() instanceof PotionItem)) return false;
        if (!player.level().isClientSide && (!(player.level() instanceof ServerLevel server)
                || !server.getServer().isSameThread())) return false;
        ItemStack potion = player.getMainHandItem();
        boolean cancel = false;
        for (ItemStack gem : AvailableGems.get(player)) {
            if (!(gem.getItem() instanceof ElixirspindleItem) || charge(gem) <= 0) continue;
            cancel = true;
            if (player.level().isClientSide) continue;
            // Only base-potion noninstant effects guard reuse; custom effects do not.
            //? if >=1.21 {
            var contents = potion.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS,
                net.minecraft.world.item.alchemy.PotionContents.EMPTY);
            var base = contents.potion().map(holder -> holder.value().getEffects()).orElse(java.util.List.of());
            boolean active = base.stream().anyMatch(effect -> !effect.getEffect().value().isInstantenous() && player.hasEffect(effect.getEffect()));
            Iterable<MobEffectInstance> effects = contents.getAllEffects();
            //?} else {
            /*var base = net.minecraft.world.item.alchemy.PotionUtils.getPotion(potion).getEffects();
            boolean active = base.stream().anyMatch(effect -> !effect.getEffect().isInstantenous() && player.hasEffect(effect.getEffect()));
            Iterable<MobEffectInstance> effects = net.minecraft.world.item.alchemy.PotionUtils.getMobEffects(potion);
            *///?}
            if (!active) {
                for (MobEffectInstance effect : effects) player.addEffect(new MobEffectInstance(effect));
                GemRecharge.consume(player, gem, 1);
                player.getInventory().setChanged();
            }
        }
        return cancel;
    }
}
