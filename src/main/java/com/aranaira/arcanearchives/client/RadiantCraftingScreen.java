package com.aranaira.arcanearchives.client;


import com.aranaira.arcanearchives.inventory.RadiantCraftingMenu;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

public final class RadiantCraftingScreen extends AbstractContainerScreen<RadiantCraftingMenu> {

    public RadiantCraftingScreen(RadiantCraftingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 206;
        imageHeight = 203;
    }
    @Override protected void slotClicked(Slot slot, int index, int mouseButton, ClickType type) {
        if (index >= 46 && index < 49) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index - 46 + (hasShiftDown() ? 3 : 0));
            return;
        }
        super.slotClicked(slot, index, mouseButton, type);
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if <1.21 {
        /*renderBackground(graphics);
        *///?}
        super.render(graphics, mouseX, mouseY, partialTick);
        for (int index = 46; index < 49; index++) {
            Slot slot = menu.slots.get(index);
            if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                graphics.renderTooltip(font, List.of(Component.translatable("arcanearchives.crafting.memory"),
                    Component.translatable("arcanearchives.crafting.memory_controls")), Optional.empty(), mouseX, mouseY);
                return;
            }
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GuiTextures.select("radiantcraftingtable"), leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}
}
