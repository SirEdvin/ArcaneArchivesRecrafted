package com.aranaira.arcanearchives.client;


import com.aranaira.arcanearchives.inventory.RadiantChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

import net.minecraft.world.entity.player.Inventory;

public final class RadiantChestScreen extends AbstractContainerScreen<RadiantChestMenu> {
    private EditBox nameBox;
    private boolean receivingName;
    private RoutingButton routingButton;

    public RadiantChestScreen(RadiantChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 192;
        imageHeight = 253;
    }

    @Override protected void init() {
        super.init();
        nameBox = new EditBox(font, leftPos + 53, topPos + 238, 88, 10,
            Component.translatable("arcanearchives.gui.radiant_chest.name"));
        nameBox.setMaxLength(RadiantChestMenu.MAX_NAME_LENGTH);
        nameBox.setBordered(false);
        nameBox.setValue(menu.chestName());
        nameBox.setResponder(value -> {
            if (!receivingName) com.aranaira.arcanearchives.events.ChestName.send(menu.containerId, value);
        });
        addRenderableWidget(nameBox);
        routingButton = addRenderableWidget(new RoutingButton(leftPos + 161, topPos + 236));
        updateRoutingLabel();
    }

    private void updateRoutingLabel() {
        String mode = menu.noNewStacks() ? "nonewitems" : "any";
        Component label = Component.translatable("arcanearchives.tooltip.radiantchest.routingmode." + mode + "1")
            .append(" ").append(Component.translatable("arcanearchives.tooltip.radiantchest.routingmode." + mode + "2"));
        if (!label.equals(routingButton.getMessage())) {
            routingButton.setMessage(label);
            routingButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(label));
        }
    }

    private final class RoutingButton extends net.minecraft.client.gui.components.Button {
        private RoutingButton(int x, int y) {
            super(x, y, 12, 12, Component.empty(), button -> {
                if (minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
            }, narration -> narration.get());
        }
        @Override public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (isHoveredOrFocused()) graphics.renderOutline(getX(), getY(), width, height, 0xFFFFFFFF);
        }
    }

    @Override protected void containerTick() {
        super.containerTick();
        updateRoutingLabel();
        if (!nameBox.isFocused() && !nameBox.getValue().equals(menu.chestName())) {
            receivingName = true;
            try { nameBox.setValue(menu.chestName()); }
            finally { receivingName = false; }
        }
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (nameBox.isMouseOver(mouseX, mouseY)) {
            if (button == 1) { nameBox.setValue(""); return true; }
            if (button == 0) {
                setFocused(nameBox);
                nameBox.setFocused(true);
                nameBox.mouseClicked(mouseX, mouseY, button);
                return true;
            }
        }
        nameBox.setFocused(false);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (nameBox.isFocused() && key != org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE && key != org.lwjgl.glfw.GLFW.GLFW_KEY_TAB) {
            nameBox.keyPressed(key, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if <1.21 {
        /*renderBackground(graphics);
        *///?}
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GuiTextures.select("radiantchest"), leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.noNewStacks()) {
            graphics.blit(GuiTextures.select("radiantchest"), leftPos + 164, topPos + 239, 234, 0, 6, 6, 256, 256);
            graphics.blit(GuiTextures.select("radiantchest"), leftPos + 176, topPos + 234, 240, 0, 16, 16, 256, 256);
        }
    }

    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        for (int index = 0; index < 54; index++) {
            var slot = menu.slots.get(index);
            if (menu.count(index) > 1 && slot.hasItem()) {
                graphics.renderItemDecorations(font, slot.getItem(), slot.x, slot.y, Integer.toString(menu.count(index)));
            }
        }
    }
}
