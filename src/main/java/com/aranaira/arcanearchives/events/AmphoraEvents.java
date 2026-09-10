package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.items.RadiantAmphoraItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;


public final class AmphoraEvents {
    static void started(MinecraftServer server) {
        RadiantAmphoraItem.serverStarted(server);
    }
    public static void initialize() {
        //? if fabric {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(AmphoraEvents::started);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(RadiantAmphoraItem::serverStopped);

        net.fabricmc.fabric.api.event.player.AttackBlockCallback.EVENT.register((player, level, hand, pos, face) -> {
            if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown()
                    && level.mayInteract(player, pos) && player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) <= 36)
                RadiantAmphoraItem.toggle(player);
            return InteractionResult.PASS;
        });
        AmphoraToggle.initialize();
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.server.ServerStartedEvent event) -> started(event.getServer()));
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.server.ServerStoppedEvent event) -> RadiantAmphoraItem.serverStopped(event.getServer()));
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST, false,
            (net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) -> {
                if (event.getAction() == event.getAction().START && event.getEntity().isShiftKeyDown()
                        && event.getUseItem() != net.minecraftforge.eventbus.api.Event.Result.DENY
                        && event.getLevel().mayInteract(event.getEntity(), event.getPos())) RadiantAmphoraItem.toggle(event.getEntity());
            });
        AmphoraToggle.initialize();
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStartedEvent event) -> started(event.getServer()));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent event) -> RadiantAmphoraItem.serverStopped(event.getServer()));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST, false,
            (net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event) -> {
                if (event.getAction() == event.getAction().START && event.getEntity().isShiftKeyDown()
                        && event.getUseItem() != net.neoforged.neoforge.common.util.TriState.FALSE
                        && event.getLevel().mayInteract(event.getEntity(), event.getPos())) RadiantAmphoraItem.toggle(event.getEntity());
            });
        *///?}
    }
    private AmphoraEvents() {}
}
