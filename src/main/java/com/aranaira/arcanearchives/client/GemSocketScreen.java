package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.GemSocketMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class GemSocketScreen extends AbstractContainerScreen<GemSocketMenu> {
    private static final ResourceLocation INVENTORY = ContentRegistry.id("textures/gui/player_inv.png");
    private static final ResourceLocation SOCKET = ContentRegistry.id("textures/gui/fabrial.png");
    public GemSocketScreen(GemSocketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 181;
        imageHeight = 145;
    }
    @Override protected void init() {
        super.init();
        Component label = Component.translatable("arcanearchives.gemsocket.recharge");
        Button recharge = new RechargeButton(leftPos + 153, topPos + 2, label,
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0));
        recharge.setTooltip(Tooltip.create(label));
        addRenderableWidget(recharge);
    }
    private static final class RechargeButton extends Button {
        private RechargeButton(int x, int y, Component label, OnPress onPress) {
            super(x, y, 18, 18, label, onPress, narration -> narration.get());
        }
        @Override public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(GuiTextures.select("player_inv"), getX(), getY(), 238, 33, 18, 18, 256, 256);
            if (isHoveredOrFocused()) graphics.renderOutline(getX(), getY(), width, height, 0xFFFFFFFF);
        }
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if <1.21 {
        /*renderBackground(graphics);
        *///?}
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        if (GuiTextures.pretty()) {
            graphics.blit(INVENTORY, leftPos - 32, topPos + 106, 207, 0, 49, 33, 256, 256);
            graphics.blit(INVENTORY, leftPos + 42, topPos + 11, 0, 101, 94, 37, 256, 256);
            graphics.blit(SOCKET, leftPos + 78, topPos, 102, 0, 22, 22, 256, 256);
        } else {
            ResourceLocation slot = GuiTextures.select("single_slot");
            graphics.blit(slot, leftPos - 29, topPos + 109, 32, 0, 28, 28, 256, 256);
            graphics.blit(slot, leftPos + 73, topPos - 5, 0, 0, 32, 32, 256, 256);
        }
        graphics.blit(GuiTextures.select("player_inv"), leftPos, topPos + 44, 0, 0, 181, 101, 256, 256);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}
}
