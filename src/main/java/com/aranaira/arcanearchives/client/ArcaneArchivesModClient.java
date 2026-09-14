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
    public static void registerModels(net.minecraftforge.client.event.ModelEvent.RegisterAdditional event) {
        for (String name : new String[]{"brazier_of_hoarding", "brazier_of_hoarding_fire"})
            event.register(new net.minecraft.resources.ResourceLocation("arcanearchives", "block/" + name));
    }
    @SubscribeEvent
    public static void registerKeys(net.minecraftforge.client.event.RegisterKeyMappingsEvent event) {
        GemSocketKey.register(event);
        ManifestKey.register(event);
    }
    @SubscribeEvent
    public static void initialize(FMLClientSetupEvent event) {
        com.aranaira.arcanearchives.config.ClientConfig.initialize(net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get());
        event.enqueueWork(BrazierRanges::initialize);
        event.enqueueWork(ManifestKey::initialize);
        event.enqueueWork(TroveHud::initialize);
        EchoColorCache.initialize(net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get());
        event.enqueueWork(ResonatorLoopSound::initialize);
        event.enqueueWork(AmphoraClient::initialize);
        event.enqueueWork(ArsenalClient::initialize);
        event.enqueueWork(() -> MenuScreens.register(ContentRegistry.GEMCUTTERS_TABLE_MENU.get(), GemCuttersTableScreen::new));
        event.enqueueWork(() -> MenuScreens.register(ContentRegistry.RADIANT_CHEST_MENU.get(), RadiantChestScreen::new));
        event.enqueueWork(() -> MenuScreens.register(ContentRegistry.MANIFEST_MENU.get(), ManifestScreen::new));
        event.enqueueWork(() -> MenuScreens.register(ContentRegistry.BRAZIER_MENU.get(), BrazierScreen::new));
        event.enqueueWork(() -> MenuScreens.register(ContentRegistry.STORAGE_UPGRADE_MENU.get(), StorageUpgradeScreen::new));
        event.enqueueWork(() -> MenuScreens.register(ContentRegistry.RADIANT_CRAFTING_TABLE_MENU.get(), RadiantCraftingScreen::new));
        event.enqueueWork(() -> MenuScreens.register(ContentRegistry.DEVOURING_CHARM_MENU.get(), DevouringCharmScreen::new));
        event.enqueueWork(() -> MenuScreens.register(ContentRegistry.GEM_SOCKET_MENU.get(), GemSocketScreen::new));
        event.enqueueWork(() -> net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(ContentRegistry.RADIANT_TANK_ENTITY.get(), RadiantTankRenderer::new));
        event.enqueueWork(() -> net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(ContentRegistry.RADIANT_CHEST_ENTITY.get(), RadiantChestRenderer::new));
        event.enqueueWork(() -> net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(ContentRegistry.BRAZIER_ENTITY.get(), BrazierRenderer::new));
    }
    @SubscribeEvent
    public static void registerShaders(net.minecraftforge.client.event.RegisterShadersEvent event) throws java.io.IOException {
        event.registerShader(new net.minecraft.client.renderer.ShaderInstance(event.getResourceProvider(), BrazierRenderType.id(),
            com.mojang.blaze3d.vertex.DefaultVertexFormat.NEW_ENTITY), BrazierRenderType::loaded);
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
    public static void registerModels(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event) {
        for (String name : new String[]{"brazier_of_hoarding", "brazier_of_hoarding_fire"})
            event.register(new net.minecraft.client.resources.model.ModelResourceLocation(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("arcanearchives", "block/" + name),
                net.minecraft.client.resources.model.ModelResourceLocation.STANDALONE_VARIANT));
    }
    @SubscribeEvent
    public static void registerKeys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
        GemSocketKey.register(event);
        ManifestKey.register(event);
    }
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ContentRegistry.GEMCUTTERS_TABLE_MENU.get(), GemCuttersTableScreen::new);
        event.register(ContentRegistry.RADIANT_CHEST_MENU.get(), RadiantChestScreen::new);
        event.register(ContentRegistry.MANIFEST_MENU.get(), ManifestScreen::new);
        event.register(ContentRegistry.BRAZIER_MENU.get(), BrazierScreen::new);
        event.register(ContentRegistry.STORAGE_UPGRADE_MENU.get(), StorageUpgradeScreen::new);
        event.register(ContentRegistry.RADIANT_CRAFTING_TABLE_MENU.get(), RadiantCraftingScreen::new);
        event.register(ContentRegistry.DEVOURING_CHARM_MENU.get(), DevouringCharmScreen::new);
        event.register(ContentRegistry.GEM_SOCKET_MENU.get(), GemSocketScreen::new);
    }
    @SubscribeEvent
    public static void initialize(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
        com.aranaira.arcanearchives.config.ClientConfig.initialize(net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get());
        event.enqueueWork(BrazierRanges::initialize);
        event.enqueueWork(ManifestKey::initialize);
        event.enqueueWork(TroveHud::initialize);
        EchoColorCache.initialize(net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get());
        event.enqueueWork(ResonatorLoopSound::initialize);
        event.enqueueWork(AmphoraClient::initialize);
        event.enqueueWork(ArsenalClient::initialize);
    }
    @SubscribeEvent
    public static void registerRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ContentRegistry.RADIANT_TANK_ENTITY.get(), RadiantTankRenderer::new);
        event.registerBlockEntityRenderer(ContentRegistry.RADIANT_CHEST_ENTITY.get(), RadiantChestRenderer::new);
        event.registerBlockEntityRenderer(ContentRegistry.BRAZIER_ENTITY.get(), BrazierRenderer::new);
    }
    @SubscribeEvent
    public static void registerShaders(net.neoforged.neoforge.client.event.RegisterShadersEvent event) throws java.io.IOException {
        event.registerShader(new net.minecraft.client.renderer.ShaderInstance(event.getResourceProvider(), BrazierRenderType.id(),
            com.mojang.blaze3d.vertex.DefaultVertexFormat.NEW_ENTITY), BrazierRenderType::loaded);
    }
}
*///?}
