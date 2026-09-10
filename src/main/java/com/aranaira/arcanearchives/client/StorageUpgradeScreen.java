package com.aranaira.arcanearchives.client;


import com.aranaira.arcanearchives.inventory.StorageUpgradeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class StorageUpgradeScreen extends AbstractContainerScreen<StorageUpgradeMenu> {
    public StorageUpgradeScreen(StorageUpgradeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 181;
        imageHeight = 168;
    }
    @Override public void render(GuiGraphics graphics, int x, int y, float tick) {
        //? if <1.21 {
        /*renderBackground(graphics);
        *///?}
        super.render(graphics, x, y, tick);
        renderTooltip(graphics, x, y);
    }
    @Override protected void renderBg(GuiGraphics graphics, float tick, int x, int y) {
        graphics.blit(GuiTextures.select("radiant_upgrades"), leftPos + 49, topPos, 0, 0, 82, 32, 256, 256);
        graphics.blit(GuiTextures.select("radiant_upgrades"), leftPos + 49, topPos + 36, 0, 32, 82, 32, 256, 256);
        graphics.blit(GuiTextures.select("player_inv"), leftPos, topPos + 67, 0, 0, 181, 101, 256, 256);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int x, int y) {}
}
