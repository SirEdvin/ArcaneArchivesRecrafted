package com.aranaira.arcanearchives.client;


import com.aranaira.arcanearchives.inventory.GemCuttersTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

import net.minecraft.world.entity.player.Inventory;

public final class GemCuttersTableScreen extends AbstractContainerScreen<GemCuttersTableMenu> {


    public GemCuttersTableScreen(GemCuttersTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 206;
        imageHeight = 254;
    }

    @Override
    protected void init() {
        super.init();
        for (int direction = 0; direction < 2; direction++) {
            int action = direction;
            String key = direction == 0 ? "arcanearchives.gem_cutter.previous_page" : "arcanearchives.gem_cutter.next_page";
            addRenderableWidget(Button.builder(Component.literal(direction == 0 ? "<" : ">"), button -> {
                if (minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action);
            }).bounds(leftPos + (direction == 0 ? 26 : 170), topPos + 70, 12, 18)
                .tooltip(Tooltip.create(Component.translatable(key)))
                .createNarration(message -> Component.translatable(key)).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if <1.21 {
        /*renderBackground(graphics);
        *///?}
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GuiTextures.select("gemcutterstable"), leftPos, topPos, 0, 0, 206, 256, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // The upstream artwork owns its labels; the screen title remains available to narration.
    }
}
