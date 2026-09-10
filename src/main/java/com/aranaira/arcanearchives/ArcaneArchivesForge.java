package com.aranaira.arcanearchives;

//? if forge {
/*import net.minecraftforge.fml.common.Mod;

@Mod(ArcaneArchivesMod.MOD_ID)
public final class ArcaneArchivesForge {
    public ArcaneArchivesForge() {
        com.aranaira.arcanearchives.config.ServerSideConfig.initialize(net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get());
        com.aranaira.arcanearchives.init.ContentRegistry.initialize(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        ArcaneArchivesMod.initialize("Forge");
        com.aranaira.arcanearchives.events.OpenGemSocket.initialize();
        com.aranaira.arcanearchives.events.ChestName.initialize();
        com.aranaira.arcanearchives.events.GemSound.initialize();
        net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus().addListener(
            (net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) ->
                event.enqueueWork(com.aranaira.arcanearchives.items.WornGemSocket::initialize));
        com.aranaira.arcanearchives.events.SliverSmashing.initialize();
        com.aranaira.arcanearchives.events.AmphoraEvents.initialize();
        com.aranaira.arcanearchives.events.DebugOrbEvents.initialize();
        com.aranaira.arcanearchives.events.DevouringCharmEvents.initialize();
        com.aranaira.arcanearchives.events.SalvegleamEvents.initialize();
        com.aranaira.arcanearchives.events.AvailableGemEvents.initialize();
        com.aranaira.arcanearchives.events.SwitchgleamEvents.initialize();
        com.aranaira.arcanearchives.events.ElixirspindleEvents.initialize();
        com.aranaira.arcanearchives.events.PhoenixwayEvents.initialize();
        com.aranaira.arcanearchives.events.StormwayEvents.initialize();
        com.aranaira.arcanearchives.events.MurdergleamEvents.initialize();
        com.aranaira.arcanearchives.events.SlaughtergleamEvents.initialize();
    }
}
*///?}
