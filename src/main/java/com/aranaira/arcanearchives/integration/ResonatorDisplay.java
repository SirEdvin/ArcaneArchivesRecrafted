package com.aranaira.arcanearchives.integration;

import com.aranaira.arcanearchives.ArcaneArchivesMod;
import com.aranaira.arcanearchives.config.ServerSideConfig;
import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** Client-only production information, not a craftable recipe or permission to grant quartz. */
public enum ResonatorDisplay {
    INSTANCE;

    public static final ResourceLocation ID = ArcaneArchivesMod.id("radiant_resonator");
    public static final ResourceLocation RECIPE_ID = ArcaneArchivesMod.id("/resonating/raw_quartz");
    public static final ResourceLocation TEXTURE = ArcaneArchivesMod.id("textures/gui/jei/radiant_resonator.png");

    public ItemStack output() { return new ItemStack(ContentRegistry.RAW_QUARTZ.get()); }

    // Preserve upstream's local configured interval; this is not remote-server config synchronization.
    public String interval() {
        return I18n.get("jei.gui.resonator", ServerSideConfig.current().resonatorTickTime() / 20D / 60D);
    }
}
