package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.config.ArsenalConfig;
import com.aranaira.arcanearchives.events.OpenGemSocket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class GemSocketKey {
    private GemSocketKey() {}
    private static KeyMapping key;
    private static KeyMapping create() {
        key = new KeyMapping("arcanearchives.gui.keybinds.socket", InputConstants.UNKNOWN.getValue(), "arcanearchives.gui.keygroup");
        return key;
    }
    public static void initialize() {
        //? if fabric {
        if (ArsenalConfig.current().enableArsenal())
            net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper.registerKeyBinding(create());
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.event.TickEvent.ClientTickEvent event) -> {
                if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) tick();
            });
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.client.event.ClientTickEvent.Post event) -> tick());
        *///?}
    }
    //? if forge {
    /*public static void register(net.minecraftforge.client.event.RegisterKeyMappingsEvent event) {
        if (ArsenalConfig.current().enableArsenal()) event.register(create());
    }
    *///?} else if neoforge {
    /*public static void register(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
        if (ArsenalConfig.current().enableArsenal()) event.register(create());
    }
    *///?}
    private static void tick() {
        if (key == null) return;
        Minecraft client = Minecraft.getInstance();
        boolean pressed = false;
        while (key.consumeClick()) pressed = true;
        if (pressed && client.screen == null && client.player != null && client.player.isAlive()
                && !client.player.isSpectator() && client.isWindowActive()) OpenGemSocket.send();
    }
}
