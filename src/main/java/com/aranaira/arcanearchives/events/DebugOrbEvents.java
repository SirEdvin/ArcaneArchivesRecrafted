package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.items.DebugOrbItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

public final class DebugOrbEvents {
    public static void initialize() {
        //? if fabric {
        var phase = net.minecraft.resources.ResourceLocation.tryParse("arcanearchives:debug_orb");
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.addPhaseOrdering(net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE, phase);
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register(phase, (player, level, hand, hit) ->
            DebugOrbItem.inspect(player, level, hand, hit.getBlockPos(), false));
        net.fabricmc.fabric.api.event.player.AttackBlockCallback.EVENT.addPhaseOrdering(net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE, phase);
        net.fabricmc.fabric.api.event.player.AttackBlockCallback.EVENT.register(phase, (player, level, hand, pos, face) ->
            DebugOrbItem.inspect(player, level, hand, pos, true));
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST, false,
            (net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) -> {
                if (event.getAction() != event.getAction().START
                        || event.getUseItem() == net.minecraftforge.eventbus.api.Event.Result.DENY
                        || event.getUseBlock() == net.minecraftforge.eventbus.api.Event.Result.DENY) return;
                if (DebugOrbItem.inspect(event.getEntity(), event.getLevel(), InteractionHand.MAIN_HAND, event.getPos(), true) == InteractionResult.SUCCESS) {
                    event.setUseBlock(net.minecraftforge.eventbus.api.Event.Result.DENY);
                    event.setUseItem(net.minecraftforge.eventbus.api.Event.Result.DENY);
                    event.setCanceled(true);
                }
            });
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, false,
            (net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) -> {
                if (event.getAction() != event.getAction().START
                        || event.getUseItem() == net.neoforged.neoforge.common.util.TriState.FALSE
                        || event.getUseBlock() == net.neoforged.neoforge.common.util.TriState.FALSE) return;
                if (DebugOrbItem.inspect(event.getEntity(), event.getLevel(), InteractionHand.MAIN_HAND, event.getPos(), true) == InteractionResult.SUCCESS) {
                    event.setUseBlock(net.neoforged.neoforge.common.util.TriState.FALSE);
                    event.setUseItem(net.neoforged.neoforge.common.util.TriState.FALSE);
                    event.setCanceled(true);
                }
            });
        *///?}
    }
    private DebugOrbEvents() {}
}
