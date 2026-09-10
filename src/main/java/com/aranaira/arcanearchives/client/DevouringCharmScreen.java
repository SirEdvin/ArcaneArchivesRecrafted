package com.aranaira.arcanearchives.client;


import com.aranaira.arcanearchives.inventory.DevouringCharmMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public final class DevouringCharmScreen extends AbstractContainerScreen<DevouringCharmMenu> {

    public DevouringCharmScreen(DevouringCharmMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 181;
        imageHeight = 300;
    }
    @Override protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("arcanearchives.devouring.flip"),
            button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0))
            .bounds(leftPos, topPos + 60, 25, 15).build());
    }
    @Override protected void slotClicked(Slot slot, int index, int button, ClickType type) {
        if (index >= 43 && index < 49) {
            if (menu.flipped()) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index - 43 + (button == 2 ? 10 : 20));
            return;
        }
        super.slotClicked(slot, index, button, type);
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if <1.21 {
        /*renderBackground(graphics);
        *///?}
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GuiTextures.select("devouring_charm"), leftPos + 25, topPos + 58, menu.flipped() ? 126 : 0, menu.flipped() ? 127 : 0, 130, 129, 256, 256);
        graphics.blit(GuiTextures.select("player_inv"), leftPos, topPos + 151, 0, 0, 181, 101, 256, 256);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawWordWrap(font, Component.translatable(menu.flipped()
            ? "arcanearchives.devouring.filters" : "arcanearchives.devouring.disposal"),
            28, menu.flipped() ? 64 : 91, 126, menu.flipped() ? 0x333333 : 0xAA0000);
        if (menu.flipped()) graphics.drawWordWrap(font, Component.translatable("arcanearchives.devouring.filter_controls"), 5, 8, 170, 0xFFFFFF);
    }
}
