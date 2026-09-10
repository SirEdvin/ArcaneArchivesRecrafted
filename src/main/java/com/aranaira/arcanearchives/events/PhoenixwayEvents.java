package com.aranaira.arcanearchives.events;


import com.aranaira.arcanearchives.items.PhoenixwayItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class PhoenixwayEvents {
    private PhoenixwayEvents() {}
    public static void initialize() {
        //? if fabric {
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register(
            (entity, source, amount) -> !protect(entity, source));
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.entity.living.LivingAttackEvent event) -> {
                if (protect(event.getEntity(), event.getSource())) event.setCanceled(true);
            });
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) -> {
                if (protect(event.getEntity(), event.getSource())) event.setCanceled(true);
            });
        *///?}
    }
    public static boolean protect(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof Player player) || !(player.level() instanceof ServerLevel level)
                || !level.getServer().isSameThread() || !player.isAlive() || player.isSpectator()
                || player.hasEffect(MobEffects.FIRE_RESISTANCE)
                || (!source.is(DamageTypes.ON_FIRE) && !source.is(DamageTypes.IN_FIRE))) return false;
        for (var gem : com.aranaira.arcanearchives.items.AvailableGems.get(player)) {
            if (!(gem.getItem() instanceof PhoenixwayItem)) continue;
            // The original >= 0 gate deliberately includes empty gems.
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
            com.aranaira.arcanearchives.items.GemRecharge.consume(player, gem, 12);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            level.playSound(player, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1F, 1F);
            return true;
        }
        return false;
    }
}
