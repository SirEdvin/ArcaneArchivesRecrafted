package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.items.SalvegleamItem;

public final class SalvegleamEvents {
    private SalvegleamEvents() {}
    public static void initialize() {
        GemToggle.initialize();
        //? if fabric {
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register(SalvegleamItem::onAnimalKilled);
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST, false,
            (net.minecraftforge.event.entity.living.LivingDeathEvent event) -> SalvegleamItem.onAnimalKilled(event.getEntity(), event.getSource()));
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, false,
            (net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) -> SalvegleamItem.onAnimalKilled(event.getEntity(), event.getSource()));
        *///?}
    }
}
