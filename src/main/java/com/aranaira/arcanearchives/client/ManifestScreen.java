package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.data.ManifestContents;
import com.aranaira.arcanearchives.inventory.ManifestMenu;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Original nine-by-nine locator grid. Icons are presentation, never native inventory slots. */
public final class ManifestScreen extends AbstractContainerScreen<ManifestMenu> {
    private EditBox search;
    private ManifestSearch searchSession;
    private Button jeiButton;
    private List<ManifestContents.Entry> snapshot = List.of();
    private List<ManifestContents.Entry> filtered = List.of();
    private boolean quantity;
    private boolean descending;
    private final ManifestScroll scroll = new ManifestScroll();
    private boolean requested;
    private boolean draggingScroll;

    public ManifestScreen(ManifestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 200;
        imageHeight = 224;
    }
    @Override protected void init() {
        if (searchSession == null || searchSession.closed()) {
            var config = com.aranaira.arcanearchives.config.ClientConfig.current();
            searchSession = new ManifestSearch(config.manifestSearchTermPersistence(), config.manifestJeiSynchronise());
        }
        String query = searchSession.query();
        super.init();
        search = new EditBox(font, leftPos + 13, topPos + 13, 89, 11, label("search"));
        search.setBordered(false);
        search.setMaxLength(256);
        search.setValue(query);
        search.setResponder(value -> {
            searchSession.edit(value);
            rebuild();
        });
        addRenderableWidget(search);
        addButton("A", "sort", 112, 10, button -> {
            quantity = !quantity;
            button.setMessage(Component.literal(quantity ? "#" : "A"));
            rebuild();
        }).setMessage(Component.literal(quantity ? "#" : "A"));
        addButton("+", "direction", 130, 10, button -> {
            descending = !descending;
            button.setMessage(Component.literal(descending ? "-" : "+"));
            rebuild();
        }).setMessage(Component.literal(descending ? "-" : "+"));
        jeiButton = addButton("J", "jei_sync", 148, 10, button -> {
            searchSession.toggle();
            updateJeiButton();
        });
        updateJeiButton();
        addButton("R", "refresh", 178, 200, button -> {
            if (minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
        });
        addButton("X", "clear_tracking", 160, 200, button -> {
            if (menu.ready() && !menu.failed()) com.aranaira.arcanearchives.events.ManifestSelect.send(
                menu.containerId, menu.snapshotRevision(), -1, 2);
        });

        rebuild();
        if (!requested) {
            requested = true;
            com.aranaira.arcanearchives.events.ManifestRequest.send(menu.containerId,
                com.aranaira.arcanearchives.config.ClientConfig.current().manifestMaxDistance());
        }
    }
    private Button addButton(String text, String help, int x, int y, Button.OnPress action) {
        var button = Button.builder(Component.literal(text), action).bounds(leftPos + x, topPos + y, 14, 14).build();
        button.setTooltip(Tooltip.create(label(help)));
        return addRenderableWidget(button);
    }
    private static Component label(String name) { return Component.translatable("arcanearchives.gui.manifest." + name); }
    private void updateJeiButton() {
        jeiButton.active = searchSession.available();
        jeiButton.setMessage(Component.literal(searchSession.synchronizing() ? "J+" : "J"));
        jeiButton.setTooltip(Tooltip.create(label(!searchSession.available() ? "jei_unavailable"
            : searchSession.synchronizing() ? "jei_sync_on" : "jei_sync_off")));
    }

    private void rebuild() {
        snapshot = menu.entries();
        String query = search.getValue().toLowerCase(Locale.ROOT);
        Comparator<ManifestContents.Entry> order = quantity ? Comparator.comparingLong(ManifestContents.Entry::count)
            : Comparator.comparing(entry -> entry.stack().getHoverName().getString());
        if (descending) order = order.reversed();
        filtered = snapshot.stream().filter(entry -> {
            var id = BuiltInRegistries.ITEM.getKey(entry.stack().getItem());
            return ManifestSearch.matches(query, entry.stack().getHoverName().getString(), id.getPath(),
                query.startsWith("@") ? modName(entry) : "", id.getNamespace())
                || ManifestSearch.matchesEnchantment(entry.stack(), query);
        }).sorted(order).toList();
        scroll.reset(filtered.size());
        draggingScroll = false;
    }
    private static String modName(ManifestContents.Entry entry) {
        String id = BuiltInRegistries.ITEM.getKey(entry.stack().getItem()).getNamespace();
        //? if fabric {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(id)
            .map(mod -> mod.getMetadata().getName()).orElse(id);
        //?} else if forge {
        /*return net.minecraftforge.fml.ModList.get().getModContainerById(id)
            .map(mod -> mod.getModInfo().getDisplayName()).orElse(id);
        *///?} else {
        /*return net.neoforged.fml.ModList.get().getModContainerById(id)
            .map(mod -> mod.getModInfo().getDisplayName()).orElse(id);
        *///?}
    }

    @Override protected void containerTick() {
        super.containerTick();
        if (jeiButton.active != searchSession.available()) updateJeiButton();
        if (snapshot != menu.entries()) rebuild();
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        // Do not reinterpret a click on the previously drawn grid as an entry in a newer listing.
        if (snapshot != menu.entries()) { rebuild(); return true; }
        if ((button == 0 || button == 1) && menu.ready() && !menu.failed()
                && x >= leftPos + 12 && x < leftPos + 174 && y >= topPos + 30 && y < topPos + 192) {
            int cell = scroll.index((int) (x - leftPos - 12), (int) (y - topPos - 30));
            if (cell >= 0) {
                var entry = filtered.get(cell);
                if (entry.range() != ManifestContents.Range.OTHER_DIMENSION) {
                    com.aranaira.arcanearchives.events.ManifestSelect.send(menu.containerId,
                        menu.snapshotRevision(), snapshot.indexOf(entry), button);
                    if (com.aranaira.arcanearchives.config.ClientConfig.current().closeManifestAfterSelection(button, hasShiftDown()))
                        onClose();
                }
                return true;
            }
        }
        if (button == 0 && x >= leftPos + 178 && x < leftPos + 190
                && y >= topPos + 29 && y < topPos + 191) {
            search.setFocused(false);
            double trackY = y - topPos - 29;
            if (trackY >= scroll.thumb() && trackY < scroll.thumb() + 12) {
                draggingScroll = true;
                scroll.drag(trackY);
            } else scroll.move(trackY < scroll.thumb() ? -162 : 162);
            return true;
        }
        if (search.isMouseOver(x, y)) {
            if (button == 1) { search.setValue(""); return true; }
            if (button == 0) {
                if (hasShiftDown()) searchSession.copyFromJei().ifPresent(search::setValue);
                setFocused(search);
                search.setFocused(true);
                search.mouseClicked(x, y, button);
                return true;
            }
        }
        search.setFocused(false);
        return super.mouseClicked(x, y, button);
    }
    @Override public void removed() {
        draggingScroll = false;
        try { if (searchSession != null) searchSession.close(); }
        finally { super.removed(); }
    }

    @Override public boolean keyPressed(int key, int scanCode, int modifiers) {
        int movement = switch (key) {
            case org.lwjgl.glfw.GLFW.GLFW_KEY_UP -> -6;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN -> 6;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP -> -162;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN -> 162;
            default -> 0;
        };
        if (movement != 0) { scroll.move(movement); return true; }
        if (search.isFocused() && key != org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE && key != org.lwjgl.glfw.GLFW.GLFW_KEY_TAB) {
            search.keyPressed(key, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }
    @Override public boolean mouseDragged(double x, double y, int button, double deltaX, double deltaY) {
        if (button == 0 && draggingScroll) {
            scroll.drag(y - topPos - 29);
            return true;
        }
        return super.mouseDragged(x, y, button, deltaX, deltaY);
    }
    @Override public boolean mouseReleased(double x, double y, int button) {
        if (button == 0 && draggingScroll) { draggingScroll = false; return true; }
        return super.mouseReleased(x, y, button);
    }
    @Override
    //? if >=1.21 {
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
    //?} else {
    /*public boolean mouseScrolled(double x, double y, double vertical) {
    *///?}
        scroll.move(-6 * (int) Math.signum(vertical));
        return true;
    }
    @Override protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(GuiTextures.select("manifest_base"), leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        graphics.blit(GuiTextures.select("buttons"), leftPos + 178, topPos + 29 + scroll.thumb(),
            0, 0, 12, 12, 16, 16);
        if (!com.aranaira.arcanearchives.config.ClientConfig.current().disableManifestGrid()) {
            graphics.enableScissor(leftPos + 11, topPos + 29, leftPos + 173, topPos + 191);
            for (int cell = scroll.first(); cell < filtered.size() && scroll.y(cell) < 162; cell++)
                graphics.blit(GuiTextures.select("manifest_base"), leftPos + 11 + cell % 9 * 18,
                    topPos + 29 + scroll.y(cell), 224, 0, 18, 18, 256, 256);
            graphics.disableScissor();
        }
    }
    @Override protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.ready() || menu.failed() || filtered.isEmpty()) {
            graphics.drawWordWrap(font, label(menu.failed() ? "failed" : !menu.ready() ? "loading" : "empty"), 14, 35, 155, 0x404040);
            return;
        }
        graphics.enableScissor(leftPos + 12, topPos + 30, leftPos + 174, topPos + 192);
        var tracking = ManifestClient.tracking();
        int highlight = ManifestHighlight.color(minecraft.level.getDayTime());
        for (int cell = scroll.first(); cell < filtered.size() && scroll.y(cell) < 162; cell++) {
            var entry = filtered.get(cell);
            int x = 12 + cell % 9 * 18;
            int y = 30 + scroll.y(cell);
            if (ManifestHighlight.matches(entry.stack(), tracking)) graphics.fill(x, y, x + 16, y + 16, highlight);
            graphics.renderItem(entry.stack(), x, y);
            if (entry.range() != ManifestContents.Range.IN_RANGE) graphics.fill(x, y, x + 16, y + 16, 0x77000000);
            if (entry.count() > 1) {
                String count = Long.toString(entry.count());
                float scale = Math.min(1F, 18F / font.width(count));
                graphics.pose().pushPose();
                graphics.pose().translate(x + 17, y + 16 - 8 * scale, 200);
                graphics.pose().scale(scale, scale, 1F);
                graphics.drawString(font, count, -font.width(count), 0, 0xFFFFFF);
                graphics.pose().popPose();
            }
        }
        graphics.disableScissor();
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if <1.21 {
        /*renderBackground(graphics);
        *///?}
        super.render(graphics, mouseX, mouseY, partialTick);
        int x = mouseX - leftPos - 12;
        int y = mouseY - topPos - 30;
        if (!menu.ready() || menu.failed() || x < 0 || x >= 162 || y < 0 || y >= 162) return;
        int index = scroll.index(x, y);
        if (index < 0) return;
        var entry = filtered.get(index);
        var tooltip = new ArrayList<>(getTooltipFromItem(minecraft, entry.stack()));
        tooltip.add(Component.translatable("arcanearchives.gui.manifest.count", Long.toString(entry.count())));
        tooltip.add(label(entry.range().name().toLowerCase(Locale.ROOT)));
        if (hasShiftDown()) {
            int shown = Math.min(10, entry.locations().size());
            for (var source : entry.locations().subList(0, shown)) tooltip.add(Component.literal(source.description() + ": "
                + source.position().pos.toShortString() + " (" + source.position().dimension.location() + ") × " + source.count()));
            if (entry.locations().size() > shown) tooltip.add(Component.translatable(
                "arcanearchives.tooltip.manifest.andmore", entry.locations().size() - shown));
        } else tooltip.add(label("locations"));
        graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
    }
}
