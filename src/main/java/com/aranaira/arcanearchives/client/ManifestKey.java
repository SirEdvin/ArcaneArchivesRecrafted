package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.config.ClientConfig;
import com.aranaira.arcanearchives.events.OpenManifest;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class ManifestKey {
    private ManifestKey() {}
    private static KeyMapping key;
    private static java.util.function.Supplier<net.minecraft.world.item.ItemStack> hoveredIngredient = () -> net.minecraft.world.item.ItemStack.EMPTY;
    public static void bindHovered(java.util.function.Supplier<net.minecraft.world.item.ItemStack> lookup) {
        hoveredIngredient = lookup;
    }
    public static boolean trackHovered(int keyCode, int scanCode) {
        Minecraft client = Minecraft.getInstance();
        if (key == null || !key.matches(keyCode, scanCode) || client.screen == null || client.player == null
                || !client.player.isAlive() || client.player.isSpectator() || !client.isWindowActive()) return false;
        var stack = hoveredIngredient.get();
        if (stack.isEmpty() && client.screen instanceof com.aranaira.arcanearchives.mixin.ManifestHoveredSlot container) {
            var slot = container.arcaneArchives$hoveredSlot();
            if (slot != null) stack = slot.getItem();
        }
        if (stack.isEmpty()) return false;
        try {
            com.aranaira.arcanearchives.events.ManifestHover.send(client.player, stack,
                ClientConfig.current().manifestMaxDistance(), net.minecraft.client.gui.screens.Screen.hasShiftDown());
        } catch (RuntimeException oversizedReference) {
            client.player.displayClientMessage(net.minecraft.network.chat.Component.translatable("arcanearchives.gui.manifest.reference_too_large"), false);
        }
        return true;
    }
    private static KeyMapping create() {
        key = new KeyMapping("arcanearchives.gui.keybinds.manifest", InputConstants.UNKNOWN.getValue(), "arcanearchives.gui.keygroup");
        return key;
    }
    public static void initialize() {
        ManifestRayRenderer.initialize();
        //? if fabric {
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
        event.register(create());
    }
    *///?} else if neoforge {
    /*public static void register(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
        event.register(create());
    }
    *///?}
    private static void tick() {
        ManifestClient.tick();
        if (key == null) return;
        Minecraft client = Minecraft.getInstance();
        boolean pressed = false;
        while (key.consumeClick()) pressed = true;
        if (pressed && client.screen == null && client.player != null && client.player.isAlive()
                && !client.player.isSpectator() && client.isWindowActive()) {
            if (!com.aranaira.arcanearchives.items.ManifestItem.hasManifest(client.player)
                    && ClientConfig.current().manifestPresence()) {
                client.player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "arcanearchives.gui.missing_manifest").withStyle(net.minecraft.ChatFormatting.YELLOW), false);
            } else if (client.player.isShiftKeyDown()) com.aranaira.arcanearchives.events.ClearManifestTracking.send();
            else OpenManifest.send();
        }
    }
}
