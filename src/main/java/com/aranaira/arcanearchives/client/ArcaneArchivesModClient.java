package com.aranaira.arcanearchives.client;

//? if forge {
/*import com.aranaira.arcanearchives.ArcaneArchivesMod;
import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ArcaneArchivesMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ArcaneArchivesModClient {
    @SubscribeEvent
    public static void registerColors(net.minecraftforge.client.event.RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> EchoColor.color(stack, layer, event.getItemColors()::getColor), ContentRegistry.ECHO.get());
    }
    @SubscribeEvent
    public static void registerKeys(net.minecraftforge.client.event.RegisterKeyMappingsEvent event) {
        GemSocketKey.register(event);
    }
    @SubscribeEvent
    public static void initialize(FMLClientSetupEvent event) {
        com.aranaira.arcanearchives.config.ClientConfig.initialize(net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get());
        EchoColorCache.initialize(net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get());
        event.enqueueWork(ResonatorLoopSound::initialize);
        event.enqueueWork(AmphoraClient::initialize);
        event.enqueueWork(ArsenalClient::initialize);
        event.enqueueWork(() -> MenuScreens.register(ContentRegistry.GEMCUTTERS_TABLE_MENU.get(), GemCuttersTableScreen::new));
        event.enqueueWork(() -> MenuScreens.register(ContentRegistry.RADIANT_CHEST_MENU.get(), RadiantChestScreen::new));
        event.enqueueWork(() -> MenuScreens.register(ContentRegistry.STORAGE_UPGRADE_MENU.get(), StorageUpgradeScreen::new));
        event.enqueueWork(() -> MenuScreens.register(ContentRegistry.RADIANT_CRAFTING_TABLE_MENU.get(), RadiantCraftingScreen::new));
        event.enqueueWork(() -> MenuScreens.register(ContentRegistry.DEVOURING_CHARM_MENU.get(), DevouringCharmScreen::new));
        event.enqueueWork(() -> MenuScreens.register(ContentRegistry.GEM_SOCKET_MENU.get(), GemSocketScreen::new));
        event.enqueueWork(() -> net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(ContentRegistry.RADIANT_TANK_ENTITY.get(), RadiantTankRenderer::new));
        event.enqueueWork(() -> net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(ContentRegistry.RADIANT_CHEST_ENTITY.get(), RadiantChestRenderer::new));
    }
}
*///?} else if neoforge {
/*import com.aranaira.arcanearchives.ArcaneArchivesMod;
import com.aranaira.arcanearchives.init.ContentRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ArcaneArchivesMod.MOD_ID, value = Dist.CLIENT)
public final class ArcaneArchivesModClient {
    @SubscribeEvent
    public static void registerColors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> EchoColor.color(stack, layer, event.getItemColors()::getColor), ContentRegistry.ECHO.get());
    }
    @SubscribeEvent
    public static void registerKeys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
        GemSocketKey.register(event);
    }
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ContentRegistry.GEMCUTTERS_TABLE_MENU.get(), GemCuttersTableScreen::new);
        event.register(ContentRegistry.RADIANT_CHEST_MENU.get(), RadiantChestScreen::new);
        event.register(ContentRegistry.STORAGE_UPGRADE_MENU.get(), StorageUpgradeScreen::new);
        event.register(ContentRegistry.RADIANT_CRAFTING_TABLE_MENU.get(), RadiantCraftingScreen::new);
        event.register(ContentRegistry.DEVOURING_CHARM_MENU.get(), DevouringCharmScreen::new);
        event.register(ContentRegistry.GEM_SOCKET_MENU.get(), GemSocketScreen::new);
    }
    @SubscribeEvent
    public static void initialize(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        com.aranaira.arcanearchives.config.ClientConfig.initialize(net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get());
        EchoColorCache.initialize(net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get());
        event.enqueueWork(ResonatorLoopSound::initialize);
        event.enqueueWork(AmphoraClient::initialize);
        event.enqueueWork(ArsenalClient::initialize);
    }
    @SubscribeEvent
    public static void registerRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ContentRegistry.RADIANT_TANK_ENTITY.get(), RadiantTankRenderer::new);
        event.registerBlockEntityRenderer(ContentRegistry.RADIANT_CHEST_ENTITY.get(), RadiantChestRenderer::new);
    }
}
*///?}
