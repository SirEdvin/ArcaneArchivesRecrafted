package com.aranaira.arcanearchives;

//? if fabric {
import net.fabricmc.api.ModInitializer;

public final class ArcaneArchivesFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ArcaneArchivesMod.initialize("Fabric");
    }
}
//?}
