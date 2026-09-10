package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.items.SlaughtergleamItem;

public final class SlaughtergleamEvents {
    private SlaughtergleamEvents() {}
    public static void initialize() {
        //? if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.entity.living.LootingLevelEvent event) -> {
                if (event.getDamageSource() != null) event.setLootingLevel(event.getLootingLevel()
                    + SlaughtergleamItem.lootingBonus(event.getDamageSource().getEntity()));
            });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.entity.living.LivingDeathEvent event) ->
                SlaughtergleamItem.onDeath(event.getEntity(), event.getSource()));
        *///?} else if neoforge {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) ->
                SlaughtergleamItem.onDeath(event.getEntity(), event.getSource()));
        *///?}
    }
}
