package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.events.BrazierRadius;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.BrazierMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Original radius/network controls and local, instance-bound range visibility. */
public final class BrazierScreen extends AbstractContainerScreen<BrazierMenu> {
    private EditBox radius;
    private Button network;
    private Button eye;
    private com.aranaira.arcanearchives.tileentities.BrazierBlockEntity device;
    private boolean receiving;
    private int confirmedRadius;
    public BrazierScreen(BrazierMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 98;
        imageHeight = 41;
    }
    @Override protected void init() {
        super.init();
        radius = new EditBox(font, leftPos + 45, topPos + 9, 36, 12, label("radius"));
        radius.setMaxLength(3);
        radius.setFilter(value -> value.isEmpty() || value.matches("[0-9]{1,3}"));
        radius.setBordered(false);
        confirmedRadius = menu.radius();
        radius.setValue(Integer.toString(confirmedRadius));
        radius.setResponder(value -> {
            if (receiving || value.isEmpty()) return;
            int number = Integer.parseInt(value);
            // Preserve upstream typed-input limits; decrement can still reach zero.
            if (number > 0 && number <= 300) BrazierRadius.send(menu.containerId, number);
        });
        addRenderableWidget(radius);
        addRenderableWidget(new Control(29, 2, 8, 16, label("decrease"), 0));
        addRenderableWidget(new Control(85, 2, 8, 16, label("increase"), 1));
        network = addRenderableWidget(new Control(67, 27, 12, 12, Component.empty(), 2));
        eye = addRenderableWidget(new Control(6, 6, 14, 14, label("show_range"), 3));
        updateEye();
        updateNetwork();
    }
    private static Component label(String name) { return Component.translatable("arcanearchives.gui.brazier." + name); }
    private boolean showingRange() {
        var state = device == null ? null : BrazierRanges.INSTANCE.state(device);
        return state != null && state.showing();
    }
    private void updateEye() {
        var position = menu.position();
        if (device == null && minecraft.level != null && position != null && minecraft.level.hasChunkAt(position)
                && minecraft.level.getBlockEntity(position) instanceof com.aranaira.arcanearchives.tileentities.BrazierBlockEntity found)
            device = found;
        eye.active = device != null && device.getLevel() == minecraft.level && !device.isRemoved()
            && minecraft.level != null && minecraft.level.hasChunkAt(device.getBlockPos())
            && minecraft.level.getBlockEntity(device.getBlockPos()) == device;
        Component message = label(showingRange() ? "hide_range" : "show_range");
        if (!message.equals(eye.getMessage())) {
            eye.setMessage(message);
            eye.setTooltip(Tooltip.create(message));
        }
    }
    private void updateNetwork() {
        Component value = label(menu.personalOnly() ? "personal" : "network");
        if (!value.equals(network.getMessage())) {
            network.setMessage(value);
            network.setTooltip(Tooltip.create(value));
        }
    }
    private final class Control extends Button {
        private Control(int x, int y, int width, int height, Component text, int action) {
            super(leftPos + x, topPos + y, width, height, text, button -> {
                if (action == 3) {
                    if (device != null) BrazierRanges.INSTANCE.toggle(device, minecraft.level);
                    updateEye();
                } else if (minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action);
            }, narration -> narration.get());
            setTooltip(Tooltip.create(text));
        }
        @Override public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (isHoveredOrFocused()) graphics.renderOutline(getX(), getY(), width, height, 0xFFFFFFFF);
        }
    }
    @Override protected void containerTick() {
        super.containerTick();
        updateEye();
        updateNetwork();
        if (confirmedRadius != menu.radius()) {
            confirmedRadius = menu.radius();
            receiving = true;
            try { radius.setValue(Integer.toString(confirmedRadius)); }
            finally { receiving = false; }
        }
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        if (button == 1 && radius.isMouseOver(x, y)) { radius.setValue(""); return true; }
        return super.mouseClicked(x, y, button);
    }
    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (radius.isFocused() && key != org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE && key != org.lwjgl.glfw.GLFW.GLFW_KEY_TAB) {
            radius.keyPressed(key, scanCode, modifiers);
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
        var texture = ContentRegistry.id("textures/gui/brazier_hoarding.png");
        if (GuiTextures.pretty()) graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        graphics.blit(texture, leftPos + 7, topPos + 8, showingRange() ? 222 : 210, 0, 12, 10, 256, 256);
        if (menu.personalOnly()) {
            graphics.blit(texture, leftPos + 70, topPos + 30, 234, 0, 6, 6, 256, 256);
            graphics.blit(texture, leftPos + 82, topPos + 25, 240, 0, 16, 16, 256, 256);
        }
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}
}
