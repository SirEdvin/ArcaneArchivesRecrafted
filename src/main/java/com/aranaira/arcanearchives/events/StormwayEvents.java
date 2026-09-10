package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.items.StormwayItem;

public final class StormwayEvents {
    private StormwayEvents() {}
    public static void initialize() {
        //? if fabric {
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            StormwayItem.retaliate(entity, source);
            return true;
        });
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.entity.living.LivingAttackEvent event) -> StormwayItem.retaliate(event.getEntity(), event.getSource()));
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.entity.EntityStruckByLightningEvent event) -> StormwayItem.struck(event.getEntity()));
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) -> StormwayItem.retaliate(event.getEntity(), event.getSource()));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent event) -> StormwayItem.struck(event.getEntity()));
        *///?}
    }
}
