package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.items.DevouringCharmItem;

public final class DevouringCharmEvents {
    private DevouringCharmEvents() {}
    public static void initialize() {
        //? if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST, false,
            (net.minecraftforge.event.entity.player.EntityItemPickupEvent event) -> {
                if (event.getResult() != net.minecraftforge.eventbus.api.Event.Result.DENY
                        && DevouringCharmItem.voidPickup(event.getEntity(), event.getItem())) event.setCanceled(true);
            });
        *///?} else if neoforge {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,
            (net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre event) -> {
                if (event.canPickup() != net.neoforged.neoforge.common.util.TriState.FALSE
                        && DevouringCharmItem.voidPickup(event.getPlayer(), event.getItemEntity()))
                    event.setCanPickup(net.neoforged.neoforge.common.util.TriState.FALSE);
            });
        *///?}
    }
}
