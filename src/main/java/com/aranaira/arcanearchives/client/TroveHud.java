package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Original looked-at Trove display, using the existing block-entity synchronization. */
public final class TroveHud {
    private TroveHud() {}
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
        var client = Minecraft.getInstance();
        if (client.player == null || client.level == null || client.options.hideGui
            || !(client.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK
            || !(client.level.getBlockEntity(hit.getBlockPos()) instanceof RadiantTroveBlockEntity trove)) return;
        var stored = trove.inventory().getStackInSlot(0);
        var item = trove.optionals().isLocked() ? trove.lockReference() : stored;
        int x = graphics.guiWidth() / 2 - 40;
        int y = graphics.guiHeight() / 2;
        if (!item.isEmpty()) {
            graphics.renderItem(item, x - 40, y);
            var name = item.getHoverName();
            graphics.drawString(client.font, name, x - 19 - client.font.width(name) / 2, y - 11, 0xFFFFFF);
            graphics.drawString(client.font, "x " + TroveHudText.count(stored.getCount()), x - 20, y + 3, 0xFFFFFF);
            int storage = trove.upgrades().getTotalUpgradesQuantity();
            int optional = trove.optionals().getTotalUpgradesQuantity();
            if (storage + optional != 0) drawUpgrades(graphics, x, y + 20, storage + optional,
                storage + (optional > 0 ? "+" + optional : ""));
        } else if (trove.upgrades().getUpgradesCount() != 0) {
            int upgrades = trove.upgrades().getUpgradesCount();
            drawUpgrades(graphics, x, y, upgrades, Integer.toString(upgrades));
        }
    }

    private static void drawUpgrades(GuiGraphics graphics, int x, int y, int total, String value) {
        var font = Minecraft.getInstance().font;
        var text = Component.translatable("arcanearchives.data.gui.radiant_trove." + (total == 1 ? "upgrade" : "upgrades"), value)
            .withStyle(ChatFormatting.GOLD);
        graphics.drawString(font, text, x - 19 - font.width(text) / 2, y, 0xFFFFFF);
    }
}
