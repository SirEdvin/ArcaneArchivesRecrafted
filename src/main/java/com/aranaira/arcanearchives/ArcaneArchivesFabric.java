package com.aranaira.arcanearchives;

//? if fabric {
import net.fabricmc.api.ModInitializer;

public final class ArcaneArchivesFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        com.aranaira.arcanearchives.config.ServerSideConfig.initialize(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir());
        com.aranaira.arcanearchives.init.ContentRegistry.initialize();
        com.aranaira.arcanearchives.blocks.RadiantTrove.initializeInteractions();
        com.aranaira.arcanearchives.items.WornGemSocket.initialize();
        com.aranaira.arcanearchives.events.OpenGemSocket.initialize();
        com.aranaira.arcanearchives.events.ChestName.initialize();
        com.aranaira.arcanearchives.events.BrazierRadius.initialize();
        com.aranaira.arcanearchives.events.GemSound.initialize();
        com.aranaira.arcanearchives.events.ManifestSnapshot.initialize();
        com.aranaira.arcanearchives.events.ManifestRequest.initialize();
        com.aranaira.arcanearchives.events.ManifestSelect.initialize();
        com.aranaira.arcanearchives.events.ManifestHover.initialize();
        com.aranaira.arcanearchives.events.PlayerPreferences.initialize();
        com.aranaira.arcanearchives.events.OpenManifest.initialize();
        com.aranaira.arcanearchives.events.ClearManifestTracking.initialize();
        com.aranaira.arcanearchives.inventory.TroveItemStorage.ITEM.registerForItems(
            (stack, context) -> new com.aranaira.arcanearchives.inventory.TroveItemStorage(context),
            com.aranaira.arcanearchives.init.ContentRegistry.RADIANT_TROVE_ITEM.get());
        net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
            (chest, side) -> chest.fabricStorage, com.aranaira.arcanearchives.init.ContentRegistry.RADIANT_CHEST_ENTITY.get());
        net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
            (trove, side) -> trove.fabricStorage, com.aranaira.arcanearchives.init.ContentRegistry.RADIANT_TROVE_ENTITY.get());
        net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
            (brazier, side) -> brazier.fabricStorage, com.aranaira.arcanearchives.init.ContentRegistry.BRAZIER_ENTITY.get());
        net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.registerForBlockEntity(
            (tank, side) -> tank.inventory(), com.aranaira.arcanearchives.init.ContentRegistry.RADIANT_TANK_ENTITY.get());
        net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.ITEM.registerForItems(
            (stack, context) -> new com.aranaira.arcanearchives.inventory.TankItemFluidStorage(context),
            com.aranaira.arcanearchives.init.ContentRegistry.RADIANT_TANK_ITEM.get());
        ArcaneArchivesMod.initialize("Fabric");
        com.aranaira.arcanearchives.events.SliverSmashing.initialize();
        com.aranaira.arcanearchives.events.AmphoraEvents.initialize();
        com.aranaira.arcanearchives.events.DebugOrbEvents.initialize();
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, level, hand, hit) ->
            com.aranaira.arcanearchives.items.MonitoringCrystalItem.checkTarget(new net.minecraft.world.item.context.UseOnContext(player, hand, hit)));
        com.aranaira.arcanearchives.events.SalvegleamEvents.initialize();
        com.aranaira.arcanearchives.events.AvailableGemEvents.initialize();
        com.aranaira.arcanearchives.events.PhoenixwayEvents.initialize();
        com.aranaira.arcanearchives.events.StormwayEvents.initialize();
    }
}
//?}
