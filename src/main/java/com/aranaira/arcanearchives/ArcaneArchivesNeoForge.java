package com.aranaira.arcanearchives;

//? if neoforge {
/*import net.neoforged.fml.common.Mod;

@Mod(ArcaneArchivesMod.MOD_ID)
public final class ArcaneArchivesNeoForge {
    public ArcaneArchivesNeoForge(net.neoforged.bus.api.IEventBus bus) {
        com.aranaira.arcanearchives.config.ServerSideConfig.initialize(net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get());
        com.aranaira.arcanearchives.init.ContentRegistry.initialize(bus);
        bus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) ->
            event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                com.aranaira.arcanearchives.init.ContentRegistry.RADIANT_CHEST_ENTITY.get(),
                (chest, side) -> chest.inventory()));
        bus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) ->
            event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                com.aranaira.arcanearchives.init.ContentRegistry.RADIANT_TROVE_ENTITY.get(),
                (trove, side) -> new com.aranaira.arcanearchives.inventory.TroveItemAutomation(trove)));
        bus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) ->
            event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                com.aranaira.arcanearchives.init.ContentRegistry.RADIANT_TANK_ENTITY.get(),
                (tank, side) -> tank.inventory()));
        ArcaneArchivesMod.initialize("NeoForge");
        bus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) ->
            event.registerItem(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.ITEM,
                (stack, context) -> new com.aranaira.arcanearchives.inventory.TroveItemStorage(stack),
                com.aranaira.arcanearchives.init.ContentRegistry.RADIANT_TROVE_ITEM.get()));
        bus.addListener((net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) ->
            event.registerItem(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM,
                (stack, context) -> new com.aranaira.arcanearchives.inventory.TankItemFluidStorage(stack),
                com.aranaira.arcanearchives.init.ContentRegistry.RADIANT_TANK_ITEM.get()));
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) ->
            event.enqueueWork(com.aranaira.arcanearchives.items.WornGemSocket::initialize));
        com.aranaira.arcanearchives.events.SliverSmashing.initialize();
        com.aranaira.arcanearchives.events.AmphoraEvents.initialize();
        com.aranaira.arcanearchives.events.DebugOrbEvents.initialize();
        com.aranaira.arcanearchives.events.DevouringCharmEvents.initialize();
        bus.addListener(com.aranaira.arcanearchives.events.AmphoraToggle::register);
        com.aranaira.arcanearchives.events.SalvegleamEvents.initialize();
        com.aranaira.arcanearchives.events.AvailableGemEvents.initialize();
        com.aranaira.arcanearchives.events.SwitchgleamEvents.initialize();
        com.aranaira.arcanearchives.events.ElixirspindleEvents.initialize();
        com.aranaira.arcanearchives.events.PhoenixwayEvents.initialize();
        com.aranaira.arcanearchives.events.StormwayEvents.initialize();
        com.aranaira.arcanearchives.events.MurdergleamEvents.initialize();
        com.aranaira.arcanearchives.events.SlaughtergleamEvents.initialize();
        bus.addListener(com.aranaira.arcanearchives.events.GemToggle::register);
        bus.addListener(com.aranaira.arcanearchives.events.OpenGemSocket::register);
        bus.addListener(com.aranaira.arcanearchives.events.ChestName::register);
        bus.addListener(com.aranaira.arcanearchives.events.GemSound::register);
    }
}
*///?}
