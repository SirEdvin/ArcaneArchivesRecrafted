package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.items.ArcaneGemItem;
import com.aranaira.arcanearchives.items.SwitchgleamItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public final class SwitchgleamEvents {
    private SwitchgleamEvents() {}
    public static void initialize() {
        //? if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity event) -> {
                if (event.getEntityLiving() instanceof EnderMan && suppress(event.getEntityLiving())) event.setCanceled(true);
            });
        *///?} else if neoforge {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.entity.EntityTeleportEvent.EnderEntity event) -> {
                if (event.getEntityLiving() instanceof EnderMan && suppress(event.getEntityLiving())) event.setCanceled(true);
            });
        *///?}
    }
    public static boolean suppress(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel level) || !level.getServer().isSameThread() || !target.isAlive()) return false;
        AABB bounds = new AABB(target.getX() - 10.5, target.getY() - 10.5, target.getZ() - 10.5,
            target.getX() + 10.5, target.getY() + 10.5, target.getZ() + 10.5);
        boolean cancel = false;
        for (Player player : level.getEntitiesOfClass(Player.class, bounds)) {
            if (!player.isAlive() || player.isSpectator()) continue;
            for (var gem : com.aranaira.arcanearchives.items.AvailableGems.get(player)) {
                if (!(gem.getItem() instanceof SwitchgleamItem)) continue;
                // The pinned event handler has no toggle/charge gate and credits every available gem.
                ArcaneGemItem.setCharge(gem, ArcaneGemItem.charge(gem) + 3);
                com.aranaira.arcanearchives.items.AvailableGems.changed(player);
                cancel = true;
            }
        }
        return cancel;
    }
}
