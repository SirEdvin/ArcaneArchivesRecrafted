package com.aranaira.arcanearchives.client;

//? if fabric {
import com.aranaira.arcanearchives.init.ContentRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

public final class ArcaneArchivesFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        com.aranaira.arcanearchives.config.ClientConfig.initialize(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir());
        EchoColorCache.initialize(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir());
        AmphoraClient.initialize();
        ResonatorLoopSound.initialize();
        GemSoundClient.initialize();
        net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.ITEM.register((stack, layer) ->
            EchoColor.color(stack, layer, (source, index) -> {
                var provider = net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.ITEM.get(source.getItem());
                return provider == null ? -1 : provider.getColor(source, index);
            }), ContentRegistry.ECHO.get());
        ArsenalClient.initialize();
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(ContentRegistry.RADIANT_TANK_ENTITY.get(), RadiantTankRenderer::new);
        net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register(ContentRegistry.RADIANT_CHEST_ENTITY.get(), RadiantChestRenderer::new);
        net.minecraft.client.gui.screens.MenuScreens.register(ContentRegistry.GEMCUTTERS_TABLE_MENU.get(), GemCuttersTableScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(ContentRegistry.RADIANT_CHEST_MENU.get(), RadiantChestScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(ContentRegistry.STORAGE_UPGRADE_MENU.get(), StorageUpgradeScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(ContentRegistry.RADIANT_CRAFTING_TABLE_MENU.get(), RadiantCraftingScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(ContentRegistry.DEVOURING_CHARM_MENU.get(), DevouringCharmScreen::new);
        net.minecraft.client.gui.screens.MenuScreens.register(ContentRegistry.GEM_SOCKET_MENU.get(), GemSocketScreen::new);
        BlockRenderLayerMap.INSTANCE.putBlock(ContentRegistry.RADIANT_CRAFTING_TABLE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ContentRegistry.RADIANT_CHEST.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ContentRegistry.RADIANT_TROVE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ContentRegistry.RADIANT_TANK.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ContentRegistry.GEMCUTTERS_TABLE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ContentRegistry.RADIANT_LANTERN.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ContentRegistry.RADIANT_RESONATOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ContentRegistry.WONKY_RESONATOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ContentRegistry.VERDANT_CENSER.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ContentRegistry.ECHOING_CONFORMANCE_CHAMBER.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ContentRegistry.ECHOING_REVERBERATION_CHAMBER.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ContentRegistry.QUARTZ_SLIVER.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ContentRegistry.RAW_QUARTZ_CLUSTER.get(), RenderType.cutout());
        ModelLoadingPlugin.register(plugin -> plugin.modifyModelAfterBake().register((model, context) -> {
            if (model == null || model instanceof GemCutterFabricModel) return model;
            //? if >=1.21 {
            ResourceLocation resource = context.resourceId();
            ModelResourceLocation topLevel = context.topLevelId();
            ResourceLocation item = topLevel == null ? null : topLevel.id();
            //?} else {
            /*ResourceLocation resource = context.id();
            ModelResourceLocation topLevel = resource instanceof ModelResourceLocation variant ? variant : null;
            ResourceLocation item = topLevel;
            *///?}
            boolean geometry = resource != null && resource.getNamespace().equals("arcanearchives")
                && (resource.getPath().equals("block/gemcutters_table") || resource.getPath().equals("item/gemcutters_table"));
            boolean inventory = topLevel != null && topLevel.getVariant().equals("inventory")
                && item.getNamespace().equals("arcanearchives") && item.getPath().equals("gemcutters_table");
            if (geometry || inventory) return new GemCutterFabricModel(model, context.settings(), context.textureGetter());
            boolean chestGeometry = resource != null && resource.getNamespace().equals("arcanearchives")
                && (resource.getPath().equals("block/radiant_chest") || resource.getPath().equals("item/radiant_chest"));
            boolean chestInventory = topLevel != null && topLevel.getVariant().equals("inventory")
                && item.getNamespace().equals("arcanearchives") && item.getPath().equals("radiant_chest");
            if (chestGeometry || chestInventory) return new GemCutterFabricModel(model, context.settings(), context.textureGetter(), "radiant_chest");
            boolean troveGeometry = resource != null && resource.getNamespace().equals("arcanearchives")
                && (resource.getPath().equals("block/radiant_trove") || resource.getPath().equals("item/radiant_trove"));
            boolean troveInventory = topLevel != null && topLevel.getVariant().equals("inventory")
                && item.getNamespace().equals("arcanearchives") && item.getPath().equals("radiant_trove");
            if (troveGeometry || troveInventory) return new GemCutterFabricModel(model, context.settings(), context.textureGetter(), "radiant_trove");
            boolean tankGeometry = resource != null && resource.getNamespace().equals("arcanearchives")
                && (resource.getPath().equals("block/radiant_tank") || resource.getPath().equals("item/radiant_tank"));
            boolean tankInventory = topLevel != null && topLevel.getVariant().equals("inventory")
                && item.getNamespace().equals("arcanearchives") && item.getPath().equals("radiant_tank");
            if (tankGeometry || tankInventory) return new GemCutterFabricModel(model, context.settings(), context.textureGetter(), "radiant_tank");
            boolean craftingGeometry = resource != null && resource.getNamespace().equals("arcanearchives")
                && (resource.getPath().equals("block/radiant_crafting_table") || resource.getPath().equals("item/radiant_crafting_table"));
            boolean craftingInventory = topLevel != null && topLevel.getVariant().equals("inventory")
                && item.getNamespace().equals("arcanearchives") && item.getPath().equals("radiant_crafting_table");
            if (craftingGeometry || craftingInventory) return new GemCutterFabricModel(model, context.settings(), context.textureGetter(), "radiant_crafting_table");
            boolean resonatorGeometry = resource != null && resource.getNamespace().equals("arcanearchives")
                && (resource.getPath().equals("block/radiant_resonator") || resource.getPath().equals("item/radiant_resonator"));
            boolean resonatorInventory = topLevel != null && topLevel.getVariant().equals("inventory")
                && item.getNamespace().equals("arcanearchives") && item.getPath().equals("radiant_resonator");
            if (resonatorGeometry || resonatorInventory) return new GemCutterFabricModel(model, context.settings(), context.textureGetter(), "radiant_resonator");
            boolean wonkyGeometry = resource != null && resource.getNamespace().equals("arcanearchives")
                && (resource.getPath().equals("block/wonky_resonator") || resource.getPath().equals("item/wonky_resonator"));
            boolean wonkyInventory = topLevel != null && topLevel.getVariant().equals("inventory")
                && item.getNamespace().equals("arcanearchives") && item.getPath().equals("wonky_resonator");
            if (wonkyGeometry || wonkyInventory) return new GemCutterFabricModel(model, context.settings(), context.textureGetter(), "makeshift_resonator");
            for (String name : new String[]{"verdant_censer", "echoing_conformance_chamber", "echoing_reverberation_chamber", "celestial_lotus_engine", "matrix_reservoir", "matrix_distillate"}) {
                boolean deviceGeometry = resource != null && resource.getNamespace().equals("arcanearchives")
                    && (resource.getPath().equals("block/" + name) || resource.getPath().equals("item/" + name));
                boolean deviceInventory = topLevel != null && topLevel.getVariant().equals("inventory")
                    && item.getNamespace().equals("arcanearchives") && item.getPath().equals(name);
                if (deviceGeometry || deviceInventory) return new GemCutterFabricModel(model, context.settings(), context.textureGetter(), name);
            }
            boolean clusterGeometry = resource != null && resource.getNamespace().equals("arcanearchives")
                && (resource.getPath().equals("block/raw_quartz_cluster") || resource.getPath().equals("item/raw_quartz_cluster"));
            boolean clusterInventory = topLevel != null && topLevel.getVariant().equals("inventory")
                && item.getNamespace().equals("arcanearchives") && item.getPath().equals("raw_quartz_cluster");
            if (clusterGeometry || clusterInventory) return new GemCutterFabricModel(model, context.settings(), context.textureGetter(), "raw_quartz");
            if (resource != null && resource.getNamespace().equals("arcanearchives") && resource.getPath().equals("block/quartz_sliver"))
                return new GemCutterFabricModel(model, context.settings(), context.textureGetter(), "quartz_sliver");
            boolean crystalGeometry = resource != null && resource.getNamespace().equals("arcanearchives")
                && (resource.getPath().equals("block/monitoring_crystal") || resource.getPath().equals("item/monitoring_crystal"));
            boolean crystalInventory = topLevel != null && topLevel.getVariant().equals("inventory")
                && item.getNamespace().equals("arcanearchives") && item.getPath().equals("monitoring_crystal");
            if (crystalGeometry || crystalInventory) return new GemCutterFabricModel(model, context.settings(), context.textureGetter(), "monitoring_crystal");
            boolean lanternGeometry = resource != null && resource.getNamespace().equals("arcanearchives")
                && (resource.getPath().equals("block/radiant_lantern") || resource.getPath().equals("item/radiant_lantern"));
            boolean lanternInventory = topLevel != null && topLevel.getVariant().equals("inventory")
                && item.getNamespace().equals("arcanearchives") && item.getPath().equals("radiant_lantern");
            return lanternGeometry || lanternInventory
                ? new GemCutterFabricModel(model, context.settings(), context.textureGetter(), "radiant_lantern") : model;
        }));
    }
}
//?}
