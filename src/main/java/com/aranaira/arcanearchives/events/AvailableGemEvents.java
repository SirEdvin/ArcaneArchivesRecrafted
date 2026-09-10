package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.items.AgegleamItem;
import com.aranaira.arcanearchives.items.AvailableGems;
import com.aranaira.arcanearchives.items.SalvegleamItem;
import com.aranaira.arcanearchives.items.StormwayItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class AvailableGemEvents {
    private AvailableGemEvents() {}
    public static void initialize() {
        //? if fabric {
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (Player player : server.getPlayerList().getPlayers()) tick(player);
        });
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.TickEvent.PlayerTickEvent event) -> {
                if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) tick(event.player);
            });
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) -> tick(event.getEntity()));
        *///?}
    }
    private static void tick(Player player) {
        if (!(player.level() instanceof ServerLevel level) || !level.getServer().isSameThread()
                || !player.isAlive() || player.isSpectator()) return;
        for (var stack : AvailableGems.get(player)) {
            if (stack.getItem() instanceof SalvegleamItem gem) gem.tickAvailable(stack, player);
            else if (stack.getItem() instanceof StormwayItem gem) gem.tickAvailable(stack, player);
            else if (stack.getItem() instanceof AgegleamItem gem) gem.tickAvailable(stack, player);
        }
    }
}
