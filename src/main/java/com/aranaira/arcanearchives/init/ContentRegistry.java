package com.aranaira.arcanearchives.init;

import com.aranaira.arcanearchives.ArcaneArchivesMod;
import com.aranaira.arcanearchives.blocks.RadiantResonator;
import com.aranaira.arcanearchives.blocks.UnimplementedDeviceBlock;
import com.aranaira.arcanearchives.blocks.RadiantChest;
import com.aranaira.arcanearchives.blocks.RadiantTrove;
import com.aranaira.arcanearchives.blocks.RadiantTank;
import com.aranaira.arcanearchives.blocks.RadiantCraftingTable;
import com.aranaira.arcanearchives.tileentities.RadiantCraftingTableBlockEntity;
import com.aranaira.arcanearchives.inventory.RadiantCraftingMenu;
import com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import com.aranaira.arcanearchives.inventory.RadiantChestMenu;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import com.aranaira.arcanearchives.items.RadiantResonatorItem;
import com.aranaira.arcanearchives.tileentities.RadiantResonatorBlockEntity;
import net.minecraft.sounds.SoundEvent;
import com.aranaira.arcanearchives.inventory.GemCuttersTableMenu;
import com.aranaira.arcanearchives.recipe.gct.GemCutterDataRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.flag.FeatureFlags;
import com.aranaira.arcanearchives.blocks.StorageShapedQuartz;
import com.aranaira.arcanearchives.blocks.StorageRawQuartz;
import com.aranaira.arcanearchives.blocks.RadiantLantern;
import com.aranaira.arcanearchives.blocks.QuartzSliver;
import com.aranaira.arcanearchives.blocks.RawQuartzCluster;
import com.aranaira.arcanearchives.blocks.GemCuttersTable;
import com.aranaira.arcanearchives.tileentities.GemCuttersTableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.aranaira.arcanearchives.items.ShapedQuartzItem;
import com.aranaira.arcanearchives.items.RawQuartzItem;
import com.aranaira.arcanearchives.items.RadiantDustItem;
import com.aranaira.arcanearchives.items.ScintillatingInlayItem;
import com.aranaira.arcanearchives.items.EmpoweredQuartzItem;
import com.aranaira.arcanearchives.items.StorageComponentItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import java.util.function.Supplier;

//? if fabric {
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
//?} else if forge {
/*import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
*///?} else {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

/** Shared content definitions; only registry timing differs between loaders. */
public final class ContentRegistry {
    //? if !fabric {
    /*private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, ArcaneArchivesMod.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ArcaneArchivesMod.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ArcaneArchivesMod.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ArcaneArchivesMod.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ArcaneArchivesMod.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ArcaneArchivesMod.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ArcaneArchivesMod.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArcaneArchivesMod.MOD_ID);
    *///?}

    public static final Supplier<StorageShapedQuartz> STORAGE_SHAPED_QUARTZ = block("storage_shaped_quartz", StorageShapedQuartz::new);
    public static final Supplier<com.aranaira.arcanearchives.blocks.FakeAir> FAKE_AIR = block("fake_air", com.aranaira.arcanearchives.blocks.FakeAir::new);
    public static final Supplier<BlockEntityType<com.aranaira.arcanearchives.tileentities.FakeAirBlockEntity>> FAKE_AIR_ENTITY = fakeAirEntity();
    public static final Supplier<RadiantResonator> RADIANT_RESONATOR = block("radiant_resonator", RadiantResonator::new);
    public static final Supplier<com.aranaira.arcanearchives.blocks.WonkyResonator> WONKY_RESONATOR = block("wonky_resonator", com.aranaira.arcanearchives.blocks.WonkyResonator::new);
    public static final Supplier<BlockItem> WONKY_RESONATOR_ITEM = item("wonky_resonator", () -> new BlockItem(WONKY_RESONATOR.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<com.aranaira.arcanearchives.tileentities.WonkyResonatorBlockEntity>> WONKY_RESONATOR_ENTITY = wonkyResonatorEntity();
    public static final Supplier<RadiantChest> RADIANT_CHEST = block("radiant_chest", RadiantChest::new);
    public static final Supplier<UnimplementedDeviceBlock> VERDANT_CENSER = block("verdant_censer", () -> new UnimplementedDeviceBlock(true));
    public static final Supplier<com.aranaira.arcanearchives.blocks.CelestialLotusEngine> CELESTIAL_LOTUS_ENGINE = block("celestial_lotus_engine", com.aranaira.arcanearchives.blocks.CelestialLotusEngine::new);
    public static final Supplier<BlockItem> CELESTIAL_LOTUS_ENGINE_ITEM = item("celestial_lotus_engine", () -> new BlockItem(CELESTIAL_LOTUS_ENGINE.get(), new Item.Properties()));
    public static final Supplier<UnimplementedDeviceBlock> SPELLBOOK_LIBRARY = block("spellbook_library", () -> new UnimplementedDeviceBlock(false));
    public static final Supplier<UnimplementedDeviceBlock> IMMANENT_INCUBATOR = block("immanent_incubator", () -> new UnimplementedDeviceBlock(false));
    public static final Supplier<com.aranaira.arcanearchives.blocks.MatrixReservoir> MATRIX_RESERVOIR = block("matrix_reservoir", com.aranaira.arcanearchives.blocks.MatrixReservoir::new);
    public static final Supplier<com.aranaira.arcanearchives.blocks.MatrixDistillate> MATRIX_DISTILLATE = block("matrix_distillate", com.aranaira.arcanearchives.blocks.MatrixDistillate::new);
    public static final Supplier<BlockItem> MATRIX_DISTILLATE_ITEM = item("matrix_distillate", () -> new com.aranaira.arcanearchives.items.MatrixDistillateItem(MATRIX_DISTILLATE.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<com.aranaira.arcanearchives.tileentities.MatrixPartBlockEntity>> MATRIX_PART_ENTITY = matrixPartEntity();
    public static final Supplier<BlockItem> MATRIX_RESERVOIR_ITEM = item("matrix_reservoir", () -> new com.aranaira.arcanearchives.items.MatrixReservoirItem(MATRIX_RESERVOIR.get(), new Item.Properties()));
    public static final Supplier<UnimplementedDeviceBlock> ECHOING_CONFORMANCE_CHAMBER = block("echoing_conformance_chamber", () -> new UnimplementedDeviceBlock(true));
    public static final Supplier<UnimplementedDeviceBlock> ECHOING_REVERBERATION_CHAMBER = block("echoing_reverberation_chamber", () -> new UnimplementedDeviceBlock(true));
    public static final Supplier<BlockItem> VERDANT_CENSER_ITEM = item("verdant_censer", () -> new BlockItem(VERDANT_CENSER.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SPELLBOOK_LIBRARY_ITEM = item("spellbook_library", () -> new BlockItem(SPELLBOOK_LIBRARY.get(), new Item.Properties()));
    public static final Supplier<BlockItem> IMMANENT_INCUBATOR_ITEM = item("immanent_incubator", () -> new BlockItem(IMMANENT_INCUBATOR.get(), new Item.Properties()));
    public static final Supplier<BlockItem> ECHOING_CONFORMANCE_CHAMBER_ITEM = item("echoing_conformance_chamber", () -> new BlockItem(ECHOING_CONFORMANCE_CHAMBER.get(), new Item.Properties()));
    public static final Supplier<BlockItem> ECHOING_REVERBERATION_CHAMBER_ITEM = item("echoing_reverberation_chamber", () -> new BlockItem(ECHOING_REVERBERATION_CHAMBER.get(), new Item.Properties()));
    public static final Supplier<RadiantTrove> RADIANT_TROVE = block("radiant_trove", RadiantTrove::new);
    public static final Supplier<RadiantTank> RADIANT_TANK = block("radiant_tank", RadiantTank::new);
    public static final Supplier<RadiantCraftingTable> RADIANT_CRAFTING_TABLE = block("radiant_crafting_table", RadiantCraftingTable::new);
    public static final Supplier<BlockItem> RADIANT_CRAFTING_TABLE_ITEM = item("radiant_crafting_table", () -> new BlockItem(RADIANT_CRAFTING_TABLE.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<RadiantCraftingTableBlockEntity>> RADIANT_CRAFTING_TABLE_ENTITY = craftingTableEntity();
    public static final Supplier<MenuType<RadiantCraftingMenu>> RADIANT_CRAFTING_TABLE_MENU = craftingTableMenu();
    public static final Supplier<BlockItem> RADIANT_TANK_ITEM = item("radiant_tank", () -> new com.aranaira.arcanearchives.items.RadiantTankItem(RADIANT_TANK.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<RadiantTankBlockEntity>> RADIANT_TANK_ENTITY = tankEntity();
    public static final Supplier<BlockItem> RADIANT_TROVE_ITEM = item("radiant_trove", () -> new com.aranaira.arcanearchives.items.RadiantTroveItem(RADIANT_TROVE.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<RadiantTroveBlockEntity>> RADIANT_TROVE_ENTITY = troveEntity();
    public static final Supplier<BlockItem> RADIANT_CHEST_ITEM = item("radiant_chest", () -> new BlockItem(RADIANT_CHEST.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<RadiantChestBlockEntity>> RADIANT_CHEST_ENTITY = chestEntity();
    public static final Supplier<MenuType<RadiantChestMenu>> RADIANT_CHEST_MENU = chestMenu();
    public static final Supplier<MenuType<com.aranaira.arcanearchives.inventory.StorageUpgradeMenu>> STORAGE_UPGRADE_MENU = upgradeMenu();
    public static final Supplier<RadiantResonatorItem> RADIANT_RESONATOR_ITEM = item("radiant_resonator", () -> new RadiantResonatorItem(RADIANT_RESONATOR.get()));
    public static final Supplier<BlockEntityType<RadiantResonatorBlockEntity>> RADIANT_RESONATOR_ENTITY = resonatorEntity();
    public static final Supplier<SoundEvent> RESONATOR_COMPLETE = sound("resonator.complete");
    public static final Supplier<SoundEvent> RESONATOR_LOOP = sound("resonator.loop");
    public static final Supplier<GemCutterDataRecipe.Type> GEM_CUTTING_TYPE = gemCuttingType();
    public static final Supplier<GemCutterDataRecipe.Serializer> GEM_CUTTING_SERIALIZER = gemCuttingSerializer();
    public static final Supplier<StorageRawQuartz> STORAGE_RAW_QUARTZ = block("storage_raw_quartz", StorageRawQuartz::new);
    public static final Supplier<RadiantLantern> RADIANT_LANTERN = block("radiant_lantern", RadiantLantern::new);
    public static final Supplier<com.aranaira.arcanearchives.blocks.MonitoringCrystal> MONITORING_CRYSTAL = block("monitoring_crystal", com.aranaira.arcanearchives.blocks.MonitoringCrystal::new);
    public static final Supplier<com.aranaira.arcanearchives.items.MonitoringCrystalItem> MONITORING_CRYSTAL_ITEM = item("monitoring_crystal", () -> new com.aranaira.arcanearchives.items.MonitoringCrystalItem(MONITORING_CRYSTAL.get()));
    public static final Supplier<BlockEntityType<com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity>> MONITORING_CRYSTAL_ENTITY = monitoringCrystalEntity();
    public static final Supplier<QuartzSliver> QUARTZ_SLIVER = block("quartz_sliver", QuartzSliver::new);
    public static final Supplier<RawQuartzCluster> RAW_QUARTZ_CLUSTER = block("raw_quartz_cluster", RawQuartzCluster::new);
    public static final Supplier<BlockItem> RAW_QUARTZ_CLUSTER_ITEM = item("raw_quartz_cluster", () -> new BlockItem(RAW_QUARTZ_CLUSTER.get(), new Item.Properties()));
    public static final Supplier<BlockItem> QUARTZ_SLIVER_ITEM = item("quartz_sliver", () -> new BlockItem(QUARTZ_SLIVER.get(), new Item.Properties()));
    public static final Supplier<BlockItem> RADIANT_LANTERN_ITEM = item("radiant_lantern", () -> new BlockItem(RADIANT_LANTERN.get(), new Item.Properties()));
    public static final Supplier<GemCuttersTable> GEMCUTTERS_TABLE = block("gemcutters_table", GemCuttersTable::new);
    public static final Supplier<BlockItem> GEMCUTTERS_TABLE_ITEM = item("gemcutters_table", () -> new BlockItem(GEMCUTTERS_TABLE.get(), new Item.Properties()));
    public static final Supplier<BlockEntityType<GemCuttersTableBlockEntity>> GEMCUTTERS_TABLE_ENTITY = gemCutterEntity();
    public static final Supplier<MenuType<GemCuttersTableMenu>> GEMCUTTERS_TABLE_MENU = gemCutterMenu();
    public static final Supplier<ShapedQuartzItem> SHAPED_QUARTZ = item("shaped_quartz", ShapedQuartzItem::new);
    public static final Supplier<RawQuartzItem> RAW_QUARTZ = item("raw_quartz", RawQuartzItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.EchoItem> ECHO = item("echo", com.aranaira.arcanearchives.items.EchoItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.DebugOrbItem> DEBUG_ORB = item("debugorb", com.aranaira.arcanearchives.items.DebugOrbItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.LetterOfInvitationItem> LETTER_INVITATION = item("letter_invitation", com.aranaira.arcanearchives.items.LetterOfInvitationItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.LetterOfResignationItem> LETTER_RESIGNATION = item("letter_resignation", com.aranaira.arcanearchives.items.LetterOfResignationItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.WritOfExpulsionItem> WRIT_EXPULSION = item("writ_expulsion", com.aranaira.arcanearchives.items.WritOfExpulsionItem::new);
    public static final Supplier<EmpoweredQuartzItem> EMPOWERED_QUARTZ = item("empowered_quartz", EmpoweredQuartzItem::new);
    public static final Supplier<RadiantDustItem> RADIANT_DUST = item("radiant_dust", RadiantDustItem::new);
    public static final Supplier<ScintillatingInlayItem> SCINTILLATING_INLAY = item("scintillating_inlay", ScintillatingInlayItem::new);
    public static final Supplier<StorageComponentItem> MATERIAL_INTERFACE = item("material_interface", () -> new StorageComponentItem("material_interface"));
    public static final Supplier<StorageComponentItem> MATRIX_BRACE = item("matrix_brace", () -> new StorageComponentItem("matrix_brace"));
    public static final Supplier<StorageComponentItem> CONTAINMENT_FIELD = item("containment_field", () -> new StorageComponentItem("containment_field"));
    public static final Supplier<com.aranaira.arcanearchives.items.StorageScepterItem> SCEPTER_REVELATION = item("scepter_revelation", () -> new com.aranaira.arcanearchives.items.StorageScepterItem(false));
    public static final Supplier<com.aranaira.arcanearchives.items.StorageScepterItem> SCEPTER_MANIPULATION = item("scepter_manipulation", () -> new com.aranaira.arcanearchives.items.StorageScepterItem(true));
    public static final Supplier<com.aranaira.arcanearchives.items.StorageScepterItem> SCEPTER_TRANSLOCATION = item("scepter_translocation", () -> new com.aranaira.arcanearchives.items.StorageScepterItem(com.aranaira.arcanearchives.items.StorageScepterItem.Kind.TRANSLOCATION));
    public static final Supplier<com.aranaira.arcanearchives.items.RadiantAmphoraItem> RADIANT_AMPHORA = item("radiant_amphora", com.aranaira.arcanearchives.items.RadiantAmphoraItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.DevouringCharmItem> DEVOURING_CHARM = item("devouring_charm", com.aranaira.arcanearchives.items.DevouringCharmItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.UnimplementedCharmItem> OBSTRUCTION_CHARM = item("obstruction_charm", com.aranaira.arcanearchives.items.UnimplementedCharmItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.UnimplementedCharmItem> SERENITY_CHARM = item("serenity_charm", com.aranaira.arcanearchives.items.UnimplementedCharmItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.RadiantKeyItem> RADIANT_KEY = item("radiant_key", com.aranaira.arcanearchives.items.RadiantKeyItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.ParchtearItem> PARCHTEAR = item("parchtear", com.aranaira.arcanearchives.items.ParchtearItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.RivertearItem> RIVERTEAR = item("rivertear", com.aranaira.arcanearchives.items.RivertearItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.AgegleamItem> AGEGLEAM = item("agegleam", com.aranaira.arcanearchives.items.AgegleamItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.SalvegleamItem> SALVEGLEAM = item("salvegleam", com.aranaira.arcanearchives.items.SalvegleamItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.MindspindleItem> MINDSPINDLE = item("mindspindle", com.aranaira.arcanearchives.items.MindspindleItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.OrderstoneItem> ORDERSTONE = item("orderstone", com.aranaira.arcanearchives.items.OrderstoneItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.CleansegleamItem> CLEANSEGLEAM = item("cleansegleam", com.aranaira.arcanearchives.items.CleansegleamItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.MunchstoneItem> MUNCHSTONE = item("munchstone", com.aranaira.arcanearchives.items.MunchstoneItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.SwitchgleamItem> SWITCHGLEAM = item("switchgleam", com.aranaira.arcanearchives.items.SwitchgleamItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.ElixirspindleItem> ELIXIRSPINDLE = item("elixirspindle", com.aranaira.arcanearchives.items.ElixirspindleItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.PhoenixwayItem> PHOENIXWAY = item("phoenixway", com.aranaira.arcanearchives.items.PhoenixwayItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.StormwayItem> STORMWAY = item("stormway", com.aranaira.arcanearchives.items.StormwayItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.MountaintearItem> MOUNTAINTEAR = item("mountaintear", com.aranaira.arcanearchives.items.MountaintearItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.MurdergleamItem> MURDERGLEAM = item("murdergleam", com.aranaira.arcanearchives.items.MurdergleamItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.SlaughtergleamItem> SLAUGHTERGLEAM = item("slaughtergleam", com.aranaira.arcanearchives.items.SlaughtergleamItem::new);
    public static final Supplier<com.aranaira.arcanearchives.items.ChromaticPowderItem> CHROMATIC_POWDER = item("chromatic_powder", () -> new com.aranaira.arcanearchives.items.ChromaticPowderItem(false));
    public static final Supplier<com.aranaira.arcanearchives.items.ChromaticPowderItem> RAINBOW_CHROMATIC_POWDER = item("full_spectrum_chromatic_powder", () -> new com.aranaira.arcanearchives.items.ChromaticPowderItem(true));
    public static final Supplier<MenuType<com.aranaira.arcanearchives.inventory.DevouringCharmMenu>> DEVOURING_CHARM_MENU = devouringCharmMenu();
    public static final Supplier<com.aranaira.arcanearchives.items.GemSocketItem> GEM_SOCKET = item("gemsocket", com.aranaira.arcanearchives.items.GemSocketItem::new);
    public static final Supplier<MenuType<com.aranaira.arcanearchives.inventory.GemSocketMenu>> GEM_SOCKET_MENU = gemSocketMenu();
    public static final Supplier<BlockItem> STORAGE_SHAPED_QUARTZ_ITEM = item("storage_shaped_quartz", () -> new com.aranaira.arcanearchives.items.StorageUpgradeBlockItem(STORAGE_SHAPED_QUARTZ.get(), new Item.Properties()));
    public static final Supplier<BlockItem> STORAGE_RAW_QUARTZ_ITEM = item("storage_raw_quartz", () -> new BlockItem(STORAGE_RAW_QUARTZ.get(), new Item.Properties()));

    //? if fabric {
    private static final CreativeModeTab TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("arcanearchives"), createTab());

    public static void initialize() {
        // Calling this method initializes the eagerly registered Fabric fields.
    }
    //?} else {
    /*private static final Supplier<CreativeModeTab> TAB = TABS.register("arcanearchives", ContentRegistry::createTab);

    public static void initialize(IEventBus bus) {
        //? if neoforge {
        com.aranaira.arcanearchives.items.EchoItem.registerComponents(bus);
        //?}
        BLOCKS.register(bus);
        SOUNDS.register(bus);
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
        TABS.register(bus);
    }
    *///?}

    private static <T extends Block> Supplier<T> block(String name, Supplier<T> factory) {
        //? if fabric {
        T value = Registry.register(BuiltInRegistries.BLOCK, id(name), factory.get());
        return () -> value;
        //?} else {
        /*return BLOCKS.register(name, factory);
        *///?}
    }

    private static <T extends Item> Supplier<T> item(String name, Supplier<T> factory) {
        //? if fabric {
        T value = Registry.register(BuiltInRegistries.ITEM, id(name), factory.get());
        return () -> value;
        //?} else {
        /*return ITEMS.register(name, factory);
        *///?}
    }

    private static Supplier<BlockEntityType<com.aranaira.arcanearchives.tileentities.MatrixPartBlockEntity>> matrixPartEntity() {
        Supplier<BlockEntityType<com.aranaira.arcanearchives.tileentities.MatrixPartBlockEntity>> factory = () -> BlockEntityType.Builder.of(
            com.aranaira.arcanearchives.tileentities.MatrixPartBlockEntity::new, MATRIX_DISTILLATE.get()).build(null);
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("matrix_part"), factory.get());
        return () -> value;
        //?} else {
        /*return BLOCK_ENTITIES.register("matrix_part", factory);
        *///?}
    }

    private static Supplier<BlockEntityType<GemCuttersTableBlockEntity>> gemCutterEntity() {
        Supplier<BlockEntityType<GemCuttersTableBlockEntity>> factory = () -> BlockEntityType.Builder.of(
            GemCuttersTableBlockEntity::new, GEMCUTTERS_TABLE.get()).build(null);
        //? if fabric {
        BlockEntityType<GemCuttersTableBlockEntity> value = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("gemcutters_table"), factory.get());
        return () -> value;
        //?} else {
        /*return BLOCK_ENTITIES.register("gemcutters_table", factory);
        *///?}
    }

    private static Supplier<BlockEntityType<RadiantResonatorBlockEntity>> resonatorEntity() {
        Supplier<BlockEntityType<RadiantResonatorBlockEntity>> factory = () -> BlockEntityType.Builder.of(
            RadiantResonatorBlockEntity::new, RADIANT_RESONATOR.get()).build(null);
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("radiant_resonator"), factory.get());
        return () -> value;
        //?} else {
        /*return BLOCK_ENTITIES.register("radiant_resonator", factory);
        *///?}
    }

    private static Supplier<BlockEntityType<com.aranaira.arcanearchives.tileentities.WonkyResonatorBlockEntity>> wonkyResonatorEntity() {
        Supplier<BlockEntityType<com.aranaira.arcanearchives.tileentities.WonkyResonatorBlockEntity>> factory = () -> BlockEntityType.Builder.of(
            com.aranaira.arcanearchives.tileentities.WonkyResonatorBlockEntity::new, WONKY_RESONATOR.get()).build(null);
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("wonky_resonator"), factory.get());
        return () -> value;
        //?} else {
        /*return BLOCK_ENTITIES.register("wonky_resonator", factory);
        *///?}
    }

    private static Supplier<BlockEntityType<com.aranaira.arcanearchives.tileentities.FakeAirBlockEntity>> fakeAirEntity() {
        Supplier<BlockEntityType<com.aranaira.arcanearchives.tileentities.FakeAirBlockEntity>> factory = () -> BlockEntityType.Builder.of(
            com.aranaira.arcanearchives.tileentities.FakeAirBlockEntity::new, FAKE_AIR.get()).build(null);
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("fake_air"), factory.get());
        return () -> value;
        //?} else {
        /*return BLOCK_ENTITIES.register("fake_air", factory);
        *///?}
    }

    private static Supplier<BlockEntityType<RadiantChestBlockEntity>> chestEntity() {
        Supplier<BlockEntityType<RadiantChestBlockEntity>> factory = () -> BlockEntityType.Builder.of(
            RadiantChestBlockEntity::new, RADIANT_CHEST.get()).build(null);
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("radiant_chest"), factory.get());
        return () -> value;
        //?} else {
        /*return BLOCK_ENTITIES.register("radiant_chest", factory);
        *///?}
    }

    private static Supplier<BlockEntityType<com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity>> monitoringCrystalEntity() {
        Supplier<BlockEntityType<com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity>> factory = () -> BlockEntityType.Builder.of(
            com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity::new, MONITORING_CRYSTAL.get()).build(null);
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("monitoring_crystal"), factory.get());
        return () -> value;
        //?} else {
        /*return BLOCK_ENTITIES.register("monitoring_crystal", factory);
        *///?}
    }

    private static Supplier<BlockEntityType<RadiantTroveBlockEntity>> troveEntity() {
        Supplier<BlockEntityType<RadiantTroveBlockEntity>> factory = () -> BlockEntityType.Builder.of(
            RadiantTroveBlockEntity::new, RADIANT_TROVE.get()).build(null);
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("radiant_trove"), factory.get());
        return () -> value;
        //?} else {
        /*return BLOCK_ENTITIES.register("radiant_trove", factory);
        *///?}
    }

    private static Supplier<BlockEntityType<RadiantTankBlockEntity>> tankEntity() {
        Supplier<BlockEntityType<RadiantTankBlockEntity>> factory = () -> BlockEntityType.Builder.of(
            RadiantTankBlockEntity::new, RADIANT_TANK.get()).build(null);
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("radiant_tank"), factory.get());
        return () -> value;
        //?} else {
        /*return BLOCK_ENTITIES.register("radiant_tank", factory);
        *///?}
    }

    private static Supplier<BlockEntityType<RadiantCraftingTableBlockEntity>> craftingTableEntity() {
        Supplier<BlockEntityType<RadiantCraftingTableBlockEntity>> factory = () -> BlockEntityType.Builder.of(
            RadiantCraftingTableBlockEntity::new, RADIANT_CRAFTING_TABLE.get()).build(null);
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id("radiant_crafting_table"), factory.get());
        return () -> value;
        //?} else {
        /*return BLOCK_ENTITIES.register("radiant_crafting_table", factory);
        *///?}
    }

    private static Supplier<MenuType<RadiantCraftingMenu>> craftingTableMenu() {
        Supplier<MenuType<RadiantCraftingMenu>> factory = () -> new MenuType<>(RadiantCraftingMenu::new, FeatureFlags.VANILLA_SET);
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.MENU, id("radiant_crafting_table"), factory.get());
        return () -> value;
        //?} else {
        /*return MENUS.register("radiant_crafting_table", factory);
        *///?}
    }

    private static Supplier<MenuType<com.aranaira.arcanearchives.inventory.DevouringCharmMenu>> devouringCharmMenu() {
        Supplier<MenuType<com.aranaira.arcanearchives.inventory.DevouringCharmMenu>> factory = () -> new MenuType<>(
            com.aranaira.arcanearchives.inventory.DevouringCharmMenu::new, FeatureFlags.VANILLA_SET);
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.MENU, id("devouring_charm"), factory.get());
        return () -> value;
        //?} else {
        /*return MENUS.register("devouring_charm", factory);
        *///?}
    }

    private static Supplier<MenuType<com.aranaira.arcanearchives.inventory.GemSocketMenu>> gemSocketMenu() {
        Supplier<MenuType<com.aranaira.arcanearchives.inventory.GemSocketMenu>> factory = () -> new MenuType<>(
            com.aranaira.arcanearchives.inventory.GemSocketMenu::new, FeatureFlags.VANILLA_SET);
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.MENU, id("gemsocket"), factory.get());
        return () -> value;
        //?} else {
        /*return MENUS.register("gemsocket", factory);
        *///?}
    }

    private static Supplier<MenuType<RadiantChestMenu>> chestMenu() {
        Supplier<MenuType<RadiantChestMenu>> factory = () -> new MenuType<>(RadiantChestMenu::new, FeatureFlags.VANILLA_SET);
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.MENU, id("radiant_chest"), factory.get());
        return () -> value;
        //?} else {
        /*return MENUS.register("radiant_chest", factory);
        *///?}
    }

    private static Supplier<MenuType<com.aranaira.arcanearchives.inventory.StorageUpgradeMenu>> upgradeMenu() {
        Supplier<MenuType<com.aranaira.arcanearchives.inventory.StorageUpgradeMenu>> factory = () -> new MenuType<>(
            com.aranaira.arcanearchives.inventory.StorageUpgradeMenu::new, FeatureFlags.VANILLA_SET);
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.MENU, id("storage_upgrades"), factory.get());
        return () -> value;
        //?} else {
        /*return MENUS.register("storage_upgrades", factory);
        *///?}
    }

    private static Supplier<SoundEvent> sound(String name) {
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.SOUND_EVENT, id(name), SoundEvent.createVariableRangeEvent(id(name)));
        return () -> value;
        //?} else {
        /*return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id(name)));
        *///?}
    }

    private static CreativeModeTab createTab() {
        //? if fabric {
        return FabricItemGroup.builder()
        //?} else {
        /*return CreativeModeTab.builder()
        *///?}
            .title(Component.translatable("itemGroup.arcanearchives"))
            .icon(() -> new ItemStack(SHAPED_QUARTZ.get()))
            .displayItems((parameters, output) -> {
                output.accept(RAW_QUARTZ.get());
                output.accept(ECHO.get());
                output.accept(DEBUG_ORB.get());
                output.accept(MONITORING_CRYSTAL_ITEM.get());
                output.accept(RADIANT_RESONATOR_ITEM.get());
                output.accept(WONKY_RESONATOR_ITEM.get());
                output.accept(VERDANT_CENSER_ITEM.get());
                output.accept(CELESTIAL_LOTUS_ENGINE_ITEM.get());
                output.accept(SPELLBOOK_LIBRARY_ITEM.get());
                output.accept(IMMANENT_INCUBATOR_ITEM.get());
                output.accept(MATRIX_RESERVOIR_ITEM.get());
                output.accept(MATRIX_DISTILLATE_ITEM.get());
                output.accept(ECHOING_CONFORMANCE_CHAMBER_ITEM.get());
                output.accept(ECHOING_REVERBERATION_CHAMBER_ITEM.get());
                output.accept(RADIANT_CHEST_ITEM.get());
                output.accept(RADIANT_TROVE_ITEM.get());
                output.accept(RADIANT_TANK_ITEM.get());
                output.accept(RADIANT_CRAFTING_TABLE_ITEM.get());
                output.accept(GEMCUTTERS_TABLE_ITEM.get());
                output.accept(RADIANT_LANTERN_ITEM.get());
                output.accept(QUARTZ_SLIVER_ITEM.get());
                output.accept(RAW_QUARTZ_CLUSTER_ITEM.get());
                output.accept(SHAPED_QUARTZ.get());
                output.accept(EMPOWERED_QUARTZ.get());
                output.accept(RADIANT_DUST.get());
                output.accept(SCINTILLATING_INLAY.get());
                output.accept(MATERIAL_INTERFACE.get());
                output.accept(MATRIX_BRACE.get());
                output.accept(CONTAINMENT_FIELD.get());
                output.accept(SCEPTER_REVELATION.get());
                output.accept(SCEPTER_MANIPULATION.get());
                output.accept(SCEPTER_TRANSLOCATION.get());
                output.accept(RADIANT_AMPHORA.get());
                output.accept(LETTER_INVITATION.get());
                output.accept(LETTER_RESIGNATION.get());
                output.accept(WRIT_EXPULSION.get());
                output.accept(DEVOURING_CHARM.get());
                output.accept(OBSTRUCTION_CHARM.get());
                output.accept(SERENITY_CHARM.get());
                output.accept(RADIANT_KEY.get());
                output.accept(GEM_SOCKET.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(PARCHTEAR.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(RIVERTEAR.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(AGEGLEAM.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(SALVEGLEAM.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(MINDSPINDLE.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(ORDERSTONE.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(CLEANSEGLEAM.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(MUNCHSTONE.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(SWITCHGLEAM.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(ELIXIRSPINDLE.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(PHOENIXWAY.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(STORMWAY.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(MOUNTAINTEAR.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(MURDERGLEAM.get());
                if (com.aranaira.arcanearchives.config.ArsenalConfig.current().enableArsenal()) output.accept(SLAUGHTERGLEAM.get());
                output.accept(STORAGE_SHAPED_QUARTZ_ITEM.get());
                output.accept(STORAGE_RAW_QUARTZ_ITEM.get());
            }).build();
    }

    public static ResourceLocation id(String path) {
        //? if >=1.21 {
        return ResourceLocation.fromNamespaceAndPath(ArcaneArchivesMod.MOD_ID, path);
        //?} else {
        /*return new ResourceLocation(ArcaneArchivesMod.MOD_ID, path);
        *///?}
    }

    private ContentRegistry() {}

    private static Supplier<GemCutterDataRecipe.Type> gemCuttingType() {
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.RECIPE_TYPE, id("gem_cutting"), new GemCutterDataRecipe.Type());
        return () -> value;
        //?} else {
        /*return RECIPE_TYPES.register("gem_cutting", GemCutterDataRecipe.Type::new);
        *///?}
    }

    private static Supplier<GemCutterDataRecipe.Serializer> gemCuttingSerializer() {
        //? if fabric {
        var value = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id("gem_cutting"), new GemCutterDataRecipe.Serializer());
        return () -> value;
        //?} else {
        /*return RECIPE_SERIALIZERS.register("gem_cutting", GemCutterDataRecipe.Serializer::new);
        *///?}
    }

    private static Supplier<MenuType<GemCuttersTableMenu>> gemCutterMenu() {
        Supplier<MenuType<GemCuttersTableMenu>> factory = () -> new MenuType<>(GemCuttersTableMenu::new, FeatureFlags.VANILLA_SET);
        //? if fabric {
        MenuType<GemCuttersTableMenu> value = Registry.register(BuiltInRegistries.MENU, id("gemcutters_table"), factory.get());
        return () -> value;
        //?} else {
        /*return MENUS.register("gemcutters_table", factory);
        *///?}
    }
}
