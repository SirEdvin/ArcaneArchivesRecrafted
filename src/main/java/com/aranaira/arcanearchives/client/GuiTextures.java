package com.aranaira.arcanearchives.client;

import com.aranaira.arcanearchives.config.ClientConfig;
import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.resources.ResourceLocation;

/** The original shared GUI preference, not a server or HUD setting. */
public final class GuiTextures {
    private GuiTextures() {}
    public static boolean pretty() { return ClientConfig.current().usePrettyGUIs(); }
    public static ResourceLocation select(String name) {
        return ContentRegistry.id("textures/gui/" + (pretty() ? "" : "simple/") + name + ".png");
    }
}
