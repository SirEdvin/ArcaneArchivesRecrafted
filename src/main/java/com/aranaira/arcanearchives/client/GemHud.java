package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.items.ArcaneGemItem;
import com.aranaira.arcanearchives.items.AvailableGems;
import com.aranaira.arcanearchives.items.GemRecharge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Native rendering of the pinned RenderGemcasting HUD geometry and sprites. */
public final class GemHud {
    private static final ResourceLocation TEXTURE = ContentRegistry.id("textures/gui/fabrial.png");
    private GemHud() {}
    public static void initialize() {
        //? if fabric {
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register((graphics, ticks) -> draw(graphics));
        //?} else if forge {
        /*net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(
            (net.minecraftforge.client.event.RenderGuiEvent.Post event) -> draw(event.getGuiGraphics()));
        *///?} else {
        /*net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
            (net.neoforged.neoforge.client.event.RenderGuiEvent.Post event) -> draw(event.getGuiGraphics()));
        *///?}
    }
    private static void draw(GuiGraphics graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        try {
            for (ItemStack stack : AvailableGems.get(client.player)) {
                boolean left = stack == client.player.getOffhandItem();
                boolean socket = stack != client.player.getMainHandItem() && !left;
                drawGem(graphics, stack, left, socket);
            }
        } finally {
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();
        }
    }
    private static void drawGem(GuiGraphics graphics, ItemStack stack, boolean left, boolean socket) {
        if (!(stack.getItem() instanceof ArcaneGemItem gem)) return;
        int spriteY = switch (BuiltInRegistries.ITEM.getKey(gem).getPath()) {
            case "agegleam", "cleansegleam", "murdergleam", "salvegleam", "slaughtergleam", "switchgleam" -> 133;
            case "orderstone", "munchstone" -> 148;
            case "mindspindle", "elixirspindle" -> 163;
            case "parchtear", "rivertear", "mountaintear" -> 178;
            case "phoenixway", "stormway" -> 193;
            default -> -1;
        };
        int color = GemRecharge.gemColor(gem);
        if (spriteY < 0 || color == 0) return;
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() / 2;
        int barY = y + (socket ? 14 : 2);
        boolean toggle = gem.hasToggleMode();
        blit(graphics, x + 8 - (left ? (toggle ? 48 : 41) : 0), barY,
            0, toggle ? (left ? 144 : 134) : 154, 33, 9);
        int fillY = 164 + (color - 1) * 4;
        if (toggle && ArcaneGemItem.isToggledOn(stack))
            blit(graphics, x + (left ? -37 : 35), barY + 3, 3, fillY, 3, 3);
        blit(graphics, x + 8 - (left ? 31 : 0) + (socket ? (toggle ? 35 : 28) : 0),
            y - 16 + (socket ? 23 : 0), 33 + (left ? 15 : 0), spriteY, 16, 16);
        int charge = ArcaneGemItem.charge(stack);
        int maximum = ArcaneGemItem.maximumCharge(stack);
        int amount = maximum > 0 ? (int) (charge / (float) maximum * 20) : 0;
        if (charge > 0 && amount == 0) amount = 1;
        if (amount > 0)
            blit(graphics, x + 11 - (left ? 41 : 0) + (left ? 20 - amount : 0), barY + 3, 3, fillY, amount, 3);
    }
    private static void blit(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        graphics.blit(TEXTURE, x, y, u, v, width, height, 256, 256);
    }
}
