package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.events.AmphoraToggle;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.items.RadiantAmphoraItem;
import net.minecraft.client.renderer.item.ItemProperties;

public final class AmphoraClient {
    public static void initialize() {
        ItemProperties.register(ContentRegistry.RADIANT_AMPHORA.get(), ContentRegistry.id("amphora_state"),
            (stack, level, entity, seed) -> !RadiantAmphoraItem.linked(stack) ? 0F : RadiantAmphoraItem.filling(stack) ? 0.5F : 1F);
        //? if fabric {
        net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register(
            //? if >=1.21 {
            (stack, context, flag, text) ->
            //?} else {
            /*(stack, flag, text) ->
            *///?}
                RadiantAmphoraItem.appendLinkedTooltip(stack, net.minecraft.client.Minecraft.getInstance().level, text));
        net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback.EVENT.register((client, player, clicks) -> {
            if (clicks > 0 && client.hitResult != null && client.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.MISS
                    && !player.isSpectator() && player.getMainHandItem().is(ContentRegistry.RADIANT_AMPHORA.get())) AmphoraToggle.send();
            return false;
        });
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.entity.player.ItemTooltipEvent event) ->
                RadiantAmphoraItem.appendLinkedTooltip(event.getItemStack(), net.minecraft.client.Minecraft.getInstance().level, event.getToolTip()));
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty event) -> {
                if (event.getEntity().getMainHandItem().is(ContentRegistry.RADIANT_AMPHORA.get())) AmphoraToggle.send();
            });
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) ->
                RadiantAmphoraItem.appendLinkedTooltip(event.getItemStack(), net.minecraft.client.Minecraft.getInstance().level, event.getToolTip()));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty event) -> {
                if (event.getEntity().getMainHandItem().is(ContentRegistry.RADIANT_AMPHORA.get())) AmphoraToggle.send();
            });
        *///?}
    }
    private AmphoraClient() {}
}
