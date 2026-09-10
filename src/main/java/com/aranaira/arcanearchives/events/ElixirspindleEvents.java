package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.items.ElixirspindleItem;

public final class ElixirspindleEvents {
    private ElixirspindleEvents() {}
    public static void initialize() {
        //? if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Start event) -> {
                if (ElixirspindleItem.intercept(event.getEntity())) event.setCanceled(true);
            });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Tick event) -> {
                if (ElixirspindleItem.intercept(event.getEntity())) event.setCanceled(true);
            });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Stop event) -> {
                if (ElixirspindleItem.intercept(event.getEntity())) event.setCanceled(true);
            });
        *///?} else if neoforge {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Start event) -> {
                if (ElixirspindleItem.intercept(event.getEntity())) event.setCanceled(true);
            });
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Tick event) -> {
                if (ElixirspindleItem.intercept(event.getEntity())) event.setCanceled(true);
            });
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Stop event) -> {
                if (ElixirspindleItem.intercept(event.getEntity())) event.setCanceled(true);
            });
        *///?}
    }
}
