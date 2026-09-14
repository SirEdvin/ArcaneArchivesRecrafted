package com.aranaira.arcanearchives;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ArcaneArchivesMod {
    public static final String MOD_ID = "arcanearchives";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private ArcaneArchivesMod() {
    }

    public static void initialize(String loader) {
        com.aranaira.arcanearchives.events.HiveCommands.initialize();
        com.aranaira.arcanearchives.data.ManifestTracking.initialize();
        com.aranaira.arcanearchives.data.PlayerSaveData.initialize();
        com.aranaira.arcanearchives.events.TomeAcquisition.initialize();
        com.aranaira.arcanearchives.integration.patchouli.TomeConditions.initialize();
        LOGGER.info("Arcane Archives Recrafted bootstrap initialized on {}", loader);
    }

    public static ResourceLocation id(String path) {
        //? if <1.21 {
        /*return new ResourceLocation(MOD_ID, path);
         *///?} else
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
