package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.items.MurdergleamItem;

public final class MurdergleamEvents {
    private MurdergleamEvents() {}
    public static void initialize() {
        //? if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.entity.player.CriticalHitEvent event) -> {
                if (MurdergleamItem.criticalHit(event.getEntity(), event.isVanillaCritical())) {
                    event.setDamageModifier(1.5F);
                    event.setResult(net.minecraftforge.eventbus.api.Event.Result.ALLOW);
                }
            });
        *///?} else if neoforge {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.entity.player.CriticalHitEvent event) -> {
                if (MurdergleamItem.criticalHit(event.getEntity(), event.isVanillaCritical())) {
                    event.setDamageMultiplier(1.5F);
                    event.setCriticalHit(true);
                }
            });
        *///?}
    }
}
