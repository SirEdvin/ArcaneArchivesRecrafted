"""Check current migration production/source JAR contracts using Python 3.11+ stdlib."""
import json
import hashlib
from pathlib import Path
import struct
import tomllib
from zipfile import ZipFile
from port_unimplemented_device_assets import NAMES as DEVICE_NAMES, OBJ_NAMES as DEVICE_OBJ_NAMES
DEVICE_TEXTURES = tuple(name for name in DEVICE_OBJ_NAMES if not name.startswith("matrix_")) + ("glass_edge", "placeholder", "block_arcanearchives_master")
from port_lotus_assets import NAME as LOTUS, TEXTURES as LOTUS_TEXTURES
from port_tome_recipe_assets import TEXTURE as TOME_RECIPE_TEXTURE, TEMPLATES as TOME_RECIPE_TEMPLATES, COMPONENT as TOME_RECIPE_COMPONENT

ROOT = Path(__file__).resolve().parents[1]
PROPERTIES = tomllib.loads((ROOT / "stonecutter.properties.toml").read_text())
METADATA = {"fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml"}
PACKAGE = "com/aranaira/arcanearchives/"
TARGETS = (
    ("1.20.1", "fabric", "Fabric", 61, "fabric.mod.json"),
    ("1.20.1", "forge", "Forge", 61, "META-INF/mods.toml"),
    ("1.21.1", "fabric", "Fabric", 65, "fabric.mod.json"),
    ("1.21.1", "neoforge", "NeoForge", 65, "META-INF/neoforge.mods.toml"),
)


def require(condition, message):
    if not condition:
        raise ValueError(message)


def require_no_test_content(names, node):
    for name in names:
        require(not name.startswith((PACKAGE + "gametest/", "org/junit/", "org/opentest4j/", "org/apiguardian/"))
                and not (name.endswith((".java", ".class")) and Path(name).stem.split("$")[0].endswith("Test")),
                f"{node}: test-only content in release artifact: {name}")


for minecraft, loader, entrypoint, major, metadata in TARGETS:
    node = f"{minecraft}-{loader}"
    version = f"{PROPERTIES['mod']['version']}+{minecraft}"
    basename = f"arcanearchives-{loader}-{version}"
    directory = ROOT / "versions" / node / "build" / "libs"
    with ZipFile(directory / f"{basename}.jar") as jar:
        names = jar.namelist()
        tome_inventory = json.loads((ROOT / "docs/migration/tome-conversion.json").read_text())
        for resource in tome_inventory["files"]:
            require(jar.read(resource) == (ROOT / "src/main/resources" / resource).read_bytes(),
                    f"{node}: missing/stale complete Tome resource {resource}")
        for recipe in ("radiant_chest1", "radiant_chest2"):
            path = f"data/arcanearchives/recipe/{recipe}.json"
            packaged = path.replace("/recipe/", "/recipes/") if minecraft == "1.20.1" else path
            expected = (ROOT / "src/main/resources" / path).read_text().replace("${result_key}", "item" if minecraft == "1.20.1" else "id")
            require(json.loads(jar.read(packaged)) == json.loads(expected), f"{node}: wrong Chest recipe {recipe}")
        require_no_test_content(names, node)
        require(len(names) == len(set(names)), f"{node}: duplicate ZIP entries")
        sounds = json.loads(jar.read("assets/arcanearchives/sounds.json"))
        shader_root = "assets/arcanearchives/shaders/core/brazier_fire"
        shader = json.loads(jar.read(shader_root + ".json"))
        require(shader.get("blend") == ({"func": "add", "srcrgb": "srcalpha", "dstrgb": "1-srcalpha"} if minecraft == "1.20.1" else None),
                f"{node}: fire shader blend metadata differs from native entity cutout")
        vertex = jar.read(shader_root + ".vsh").decode()
        require(shader["vertex"] == "arcanearchives:brazier_fire"
                and shader["fragment"] == "minecraft:rendertype_entity_cutout_no_cull",
                f"{node}: wrong unlit fire shader stages")
        require("vertexColor = Color;" in vertex and "lightMapColor = vec4(1.0);" in vertex
                and "minecraft_mix_light" not in vertex and "${" not in vertex,
                f"{node}: fire shader retains lighting or unexpanded placeholders")
        fog = "fog_distance(ModelViewMat, IViewRotMat * Position, FogShape)" if minecraft == "1.20.1" else "fog_distance(Position, FogShape)"
        require(fog in vertex and (any(u["name"] == "IViewRotMat" for u in shader["uniforms"]) == (minecraft == "1.20.1")),
                f"{node}: wrong fire fog interface")
        fire_path = "assets/arcanearchives/models/block/brazier_of_hoarding_fire.json"
        require(json.loads(jar.read("assets/arcanearchives/blockstates/brazier_of_hoarding.json")) ==
                {"variants": {"": {"model": "arcanearchives:block/brazier_of_hoarding"}}},
                f"{node}: Brazier must have a single non-directional model variant")
        for model_kind in ("block", "item"):
            model_path = f"assets/arcanearchives/models/{model_kind}/brazier_of_hoarding.json"
            model_source = (ROOT / "src/main/resources" / model_path).read_text().replace(
                "${obj_loader}", "neoforge" if loader == "neoforge" else "forge")
            require(json.loads(jar.read(model_path)) == json.loads(model_source),
                    f"{node}: Brazier {model_kind} model expansion mismatch")
        fire_source = (ROOT / "src/main/resources" / fire_path).read_text().replace(
            "${obj_loader}", "neoforge" if loader == "neoforge" else "forge")
        require(json.loads(jar.read(fire_path)) == json.loads(fire_source),
                f"{node}: Brazier fire model expansion mismatch")
        fire_mesh = "assets/arcanearchives/models/block/brazier_of_hoarding_fire.obj"
        require(jar.read(fire_mesh) == (ROOT / "src/main/resources" / fire_mesh).read_bytes(),
                f"{node}: changed packaged Brazier fire mesh")
        atlas_sources = json.loads(jar.read("assets/minecraft/atlases/blocks.json"))["sources"]
        atlas_sprites = {source["resource"] for source in atlas_sources if source["type"] == "minecraft:single"}
        material = jar.read("assets/arcanearchives/models/block/brazier_of_hoarding.mtl").decode()
        for line in material.splitlines():
            if not line.startswith("map_Kd "):
                continue
            texture = line.split()[1]
            if texture.startswith("arcanearchives:"):
                require(texture in atlas_sprites, f"{node}: missing Brazier atlas sprite {texture}")
                path = "assets/arcanearchives/textures/" + texture.split(":", 1)[1] + ".png"
                require(jar.read(path) == (ROOT / "src/main/resources" / path).read_bytes(),
                        f"{node}: changed packaged Brazier texture {texture}")
            else:
                require(texture in {"minecraft:block/fire_0", "minecraft:block/fire_1"},
                        f"{node}: unresolved vanilla Brazier material {texture}")
        require(sounds["brazier.absorb"]["sounds"] == [f"arcanearchives:brazier_absorb{i}" for i in (1, 2, 3)],
                f"{node}: Brazier absorption sound variants changed")
        for index in (1, 2, 3):
            sound_path = f"assets/arcanearchives/sounds/brazier_absorb{index}.ogg"
            require(jar.read(sound_path) == (ROOT / "src/main/resources" / sound_path).read_bytes(),
                    f"{node}: packaged Brazier sound differs from recovered source")
        require(hashlib.sha256(jar.read("assets/arcanearchives/textures/gui/jei/radiant_resonator.png")).hexdigest()
                == "f4998ffbe729d418d94b890ef2902d573d4ab4c068f24215c4cb34412677e5de",
                f"{node}: changed pinned Resonator viewer artwork")
        require(METADATA.intersection(names) == {metadata}, f"{node}: wrong loader metadata")
        require("LICENSE" in names and "META-INF/upstream/LICENSE" in names, f"{node}: missing notices")
        require(not any(name.startswith(("vazkii/", "assets/patchouli/", "data/patchouli/")) or name.endswith(".jar") for name in names),
                f"{node}: external dependency must not be bundled")
        require("pack.mcmeta" in names, f"{node}: missing pack metadata")
        require(hashlib.sha256(jar.read(TOME_RECIPE_TEXTURE)).hexdigest() ==
                "8de89eb40b9269eb4ca0dad19b62d60e39553814e16412ee7204735d603fb0c9",
                f"{node}: Tome recipe texture differs from pinned upstream")
        for selector in ("recipe", "output"):
            template = json.loads(jar.read(TOME_RECIPE_TEMPLATES + "gem_cutting_" + selector + ".json"))
            require(template == {"components": [{"type": "patchouli:custom", "class": TOME_RECIPE_COMPONENT,
                    "x": 4, "y": 20, selector: "#" + selector}]}, f"{node}: changed Tome recipe template")
        for resource in names:
            if resource.startswith("arcanearchives") and resource.endswith(".mixins.json"):
                require(json.loads(jar.read(resource)).get("minVersion") == "0.8.5",
                        f"{node}: missing or changed Mixin minimum version in {resource}")
            if resource.startswith("assets/arcanearchives/models/") and resource.endswith(".json"):
                display = json.loads(jar.read(resource)).get("display", {})
                require(not {"firstperson", "thirdperson"}.intersection(display),
                        f"{node}: ignored legacy hand transform in {resource}")
        for device in (*DEVICE_NAMES, LOTUS):
            for relative in (f"assets/arcanearchives/blockstates/{device}.json",
                             f"assets/arcanearchives/models/block/{device}.json",
                             f"assets/arcanearchives/models/item/{device}.json",
                             f"data/arcanearchives/loot_table/blocks/{device}.json"):
                packaged = relative.replace("/loot_table/", "/loot_tables/") if minecraft == "1.20.1" else relative
                source = (ROOT / "src/main/resources" / relative).read_text().replace("${obj_loader}", "neoforge" if loader == "neoforge" else "forge")
                require(json.loads(jar.read(packaged)) == json.loads(source), f"{node}: changed prototype resource {packaged}")
            tag_root = "blocks" if minecraft == "1.20.1" else "block"
            require("arcanearchives:" + device in json.loads(jar.read(f"data/minecraft/tags/{tag_root}/mineable/pickaxe.json"))["values"],
                    f"{node}: missing prototype pickaxe tag {device}")
        for resource in ([f"models/block/{device}.{ext}" for device in DEVICE_OBJ_NAMES for ext in ("obj", "mtl")]
                         + [f"textures/blocks/{texture}.png" for texture in DEVICE_TEXTURES]):
            require(jar.read("assets/arcanearchives/" + resource) == (ROOT / "src/main/resources/assets/arcanearchives" / resource).read_bytes(),
                    f"{node}: changed prototype artwork {resource}")
        for texture in DEVICE_TEXTURES:
            require({"type": "minecraft:single", "resource": "arcanearchives:blocks/" + texture} in
                    json.loads(jar.read("assets/minecraft/atlases/blocks.json"))["sources"], f"{node}: missing prototype atlas entry {texture}")
        wonky = json.loads(jar.read("assets/arcanearchives/models/block/wonky_resonator.json"))
        lotus = json.loads(jar.read(f"assets/arcanearchives/models/block/{LOTUS}.json"))
        require(lotus["render_type"] == "minecraft:solid", f"{node}: changed legacy Lotus render layer")
        for resource in ([f"models/block/{LOTUS}.{ext}" for ext in ("obj", "mtl")]
                         + [f"textures/blocks/{texture}.png" for texture in LOTUS_TEXTURES]):
            require(jar.read("assets/arcanearchives/" + resource) == (ROOT / "src/main/resources/assets/arcanearchives" / resource).read_bytes(),
                    f"{node}: changed Lotus artwork {resource}")
        for texture in LOTUS_TEXTURES:
            require({"type": "minecraft:single", "resource": "arcanearchives:blocks/" + texture} in
                    json.loads(jar.read("assets/minecraft/atlases/blocks.json"))["sources"], f"{node}: missing Lotus atlas entry {texture}")
        language = json.loads(jar.read("assets/arcanearchives/lang/en_us.json"))
        require(language["block.arcanearchives.celestial_lotus_engine"] == "Celestial Lotus Engine"
                and language["arcanearchives.tooltip.device.celestial_lotus_engine"] == "Generates infinite Immanence. Creative only.",
                f"{node}: changed original Lotus translations")
        require(wonky["loader"] == ("neoforge" if loader == "neoforge" else "forge") + ":obj",
                f"{node}: unexpanded/wrong Wonky Resonator OBJ loader")
        require(wonky["model"] == "arcanearchives:models/block/makeshift_resonator.obj", f"{node}: wrong Wonky geometry")
        expected_wonky = json.loads((ROOT / "src/main/resources/assets/arcanearchives/models/block/wonky_resonator.json").read_text())
        require(wonky["display"] == expected_wonky["display"], f"{node}: changed Wonky item transforms")
        for resource in ("models/block/makeshift_resonator.obj", "models/block/makeshift_resonator.mtl", "textures/blocks/radiant_resonator.png"):
            require(jar.read("assets/arcanearchives/" + resource) == (ROOT / "src/main/resources/assets/arcanearchives" / resource).read_bytes(),
                    f"{node}: missing/changed Wonky resource {resource}")
        require(json.loads(jar.read("assets/arcanearchives/blockstates/fake_air.json")) == {
            "variants": {"": {"model": "arcanearchives:block/fake_air"}},
        }, f"{node}: missing Fake Air default-state model")
        require(json.loads(jar.read("assets/arcanearchives/models/block/fake_air.json")) == {
            "parent": "minecraft:block/cube_all", "textures": {"all": "arcanearchives:blocks/placeholder"},
        }, f"{node}: changed Fake Air placeholder model")
        require(jar.read("assets/arcanearchives/textures/blocks/placeholder.png") ==
                (ROOT / "src/main/resources/assets/arcanearchives/textures/blocks/placeholder.png").read_bytes(),
                f"{node}: missing or changed Fake Air placeholder texture")
        require({"type": "minecraft:single", "resource": "arcanearchives:blocks/placeholder"} in
                json.loads(jar.read("assets/minecraft/atlases/blocks.json"))["sources"],
                f"{node}: missing Fake Air atlas entry")
        item_tags = "items" if minecraft == "1.20.1" else "item"
        recipe_root = "recipes" if minecraft == "1.20.1" else "recipe"
        for order, (name, count) in enumerate((
            ("radiant_dust", 2), ("shaped_quartz", 1), ("scintillating_inlay", 1),
            ("material_interface", 1), ("matrix_brace", 1), ("containment_field", 1), ("radiant_lantern", 4),
        )):
            recipe = json.loads(jar.read(f"data/arcanearchives/{recipe_root}/{name}.json"))
            require(recipe["type"] == "arcanearchives:gem_cutting" and recipe["order"] == order
                    and recipe["result"] == {"item": "arcanearchives:" + name, "count": count},
                    f"{node}: incorrect native Gem Cutter recipe {name}")
            require(recipe == json.loads((ROOT / f"src/main/resources/data/arcanearchives/recipe/{name}.json").read_text()),
                    f"{node}: packaged Gem Cutter costs differ from source {name}")
        for ingredient, item, aliases in (
            ("dust_redstone", "redstone", ("c:dusts/redstone", "c:redstone_dusts", "forge:dusts/redstone")),
            ("ingot_gold", "gold_ingot", ("c:ingots/gold", "c:gold_ingots", "forge:ingots/gold")),
            ("nugget_gold", "gold_nugget", ("c:nuggets/gold", "c:gold_nuggets", "forge:nuggets/gold")),
        ):
            tag_path = f"data/arcanearchives/tags/{item_tags}/ingredients/{ingredient}.json"
            require(json.loads(jar.read(tag_path)) == {
                "replace": False,
                "values": [f"minecraft:{item}"] + [{"id": f"#{alias}", "required": False} for alias in aliases],
            }, f"{node}: incorrect Gem Cutter ingredient bridge {ingredient}")
            wrong_root = "item" if item_tags == "items" else "items"
            require(f"data/arcanearchives/tags/{wrong_root}/ingredients/{ingredient}.json" not in names,
                    f"{node}: incorrect item-tag data root")
        pack = json.loads(jar.read("pack.mcmeta"))["pack"]
        resource_format, data_format = (15, 15) if minecraft == "1.20.1" else (34, 48)
        require(pack["pack_format"] == resource_format, f"{node}: wrong resource pack format")
        require(pack["supported_formats"] == {
            "min_inclusive": resource_format, "max_inclusive": data_format,
        }, f"{node}: wrong supported resource/data pack formats")
        raw = jar.read(metadata).decode()
        require("${" not in raw, f"{node}: unexpanded metadata")
        data = json.loads(raw) if loader == "fabric" else tomllib.loads(raw)
        mod = data if loader == "fabric" else data["mods"][0]
        require(mod["version"] == version, f"{node}: version mismatch")
        require(mod.get("id", mod.get("modId")) == "arcanearchives", f"{node}: wrong mod id")
        expected_classes = {PACKAGE + f"ArcaneArchives{suffix}.class" for suffix in ("Mod", entrypoint)}
        if loader == "fabric":
            expected_classes.update(PACKAGE + "client/" + name + ".class" for name in (
                "ArcaneArchivesFabricClient", "GemCutterFabricModel",
            ))
        expected_classes.add(PACKAGE + "util/MathUtils.class")
        expected_classes.update(PACKAGE + name + ".class" for name in ("blocks/MatrixReservoir", "items/MatrixReservoirItem"))
        expected_classes.update(PACKAGE + name + ".class" for name in ("blocks/MatrixDistillate", "items/MatrixDistillateItem",
            "tileentities/MatrixPartBlockEntity"))
        expected_classes.update(PACKAGE + "integration/jei/" + name + ".class" for name in (
            "ArcaneArchivesJei", "ArcaneArchivesJei$Category", "ArcaneArchivesJei$ResonatorCategory"))
        expected_classes.update(PACKAGE + "integration/emi/" + name + ".class" for name in (
            "ArcaneArchivesEmi", "ArcaneArchivesEmi$1", "ArcaneArchivesEmi$Display", "ArcaneArchivesEmi$CraftingTransfer", "ArcaneArchivesEmi$Resonating"))
        expected_classes.add(PACKAGE + "integration/ResonatorDisplay.class")
        expected_classes.add(PACKAGE + "integration/ViewerHiddenItems.class")
        expected_classes.update(PACKAGE + "integration/jade/" + name + ".class" for name in (
            "ArcaneArchivesJade", "ArcaneArchivesJade$Provider"))
        expected_classes.update(PACKAGE + name + ".class" for name in (
            "integration/patchouli/GemCutterBookRecipes", "client/GemCutterBookComponent"))
        expected_classes.add(PACKAGE + "integration/patchouli/TomeConditions.class")
        expected_classes.add(PACKAGE + "items/RadiantTroveItem.class")
        expected_classes.update(PACKAGE + name + ".class" for name in ("items/WritOfExpulsionItem", "mixin/WritAnvilMixin"))
        expected_classes.update(PACKAGE + name + ".class" for name in (
            "data/HiveSaveData", "items/LetterOfInvitationItem", "events/HiveCommands", "items/LetterItem", "items/LetterOfResignationItem"))
        expected_classes.update(PACKAGE + name + ".class" for name in (
            "events/GemSound", "events/GemSound$Effect", "client/GemSoundClient", "client/GemSoundClient$1", "client/ResonatorLoopSound"))
        expected_classes.update(PACKAGE + name + ".class" for name in (
            "blocks/RadiantChest", "blocks/RadiantResonator", "items/RadiantResonatorItem",
            "tileentities/RadiantResonatorBlockEntity", "data/ResonatorSaveData",
            "blocks/WonkyResonator", "tileentities/WonkyResonatorBlockEntity",
            "blocks/UnimplementedDeviceBlock", "blocks/CelestialLotusEngine",
            "tileentities/RadiantChestBlockEntity", "tileentities/RadiantChestBlockEntity$1",
            "inventory/RadiantChestMenu", "inventory/RadiantChestMenu$1", "client/RadiantChestScreen",
            "client/RadiantChestScreen$RoutingButton",
            "client/RadiantChestRenderer", "client/RadiantChestRenderer$1",
            "client/BrazierRenderer",
            "client/BrazierRenderType",
            "blocks/RadiantTrove", "blocks/RadiantTank", "items/StorageUpgradeBlockItem",
            "tileentities/RadiantTroveBlockEntity", "tileentities/RadiantTroveBlockEntity$1", "tileentities/RadiantTroveBlockEntity$2",
            "tileentities/RadiantTankBlockEntity", "tileentities/RadiantTankBlockEntity$1",
            "inventory/RadiantTankStorage", "client/RadiantTankRenderer", "mixin/TankItemRenderMixin",
            "inventory/StorageUpgradeMenu", "inventory/StorageUpgradeMenu$1", "inventory/StorageUpgradeMenu$2",
            "inventory/StorageUpgradeMenu$SizeUpgradeSlot", "inventory/StorageUpgradeMenu$OptionalUpgradeSlot",
            "inventory/handlers/StorageOptionalUpgrades", "inventory/TroveItemAutomation", "inventory/BrazierItemAutomation",
            "items/RadiantKeyItem",
            "items/ArcaneGemItem", "items/GemRecharge", "items/ChromaticPowderItem", "items/ParchtearItem", "blocks/FakeAir", "tileentities/FakeAirBlockEntity",
            "config/ArsenalConfig", "client/ArsenalClient", "config/ClientConfig", "client/GuiTextures",
            "items/GemSocketItem", "items/AvailableGems", "events/AvailableGemEvents",
            "client/GemSocketKey", "events/OpenGemSocket",
            "client/GemHud",
            "items/WornGemSocket", "items/WornGemSocket$Native", "items/WornGemSocket$State", "items/WornGemSocket$Binding",
            "inventory/GemSocketMenu", "inventory/GemSocketMenu$GemSlot", "inventory/GemSocketMenu$SocketProtectedSlot",
            "client/GemSocketScreen", "client/GemSocketScreen$RechargeButton",
            "items/RivertearItem",
            "items/AgegleamItem",
            "items/SalvegleamItem", "events/SalvegleamEvents", "events/GemToggle",
            "items/MindspindleItem", "mixin/MindspindlePickupMixin",
            "items/OrderstoneItem",
            "items/CleansegleamItem",
            "items/MunchstoneItem", "config/MunchstoneConfig",
            "items/SwitchgleamItem",
            "events/SwitchgleamEvents",
            "items/ElixirspindleItem", "events/ElixirspindleEvents",
            "items/PhoenixwayItem",
            "events/PhoenixwayEvents",
            "items/StormwayItem", "events/StormwayEvents",
            "items/MountaintearItem",
            "items/MurdergleamItem", "events/MurdergleamEvents",
            "items/SlaughtergleamItem", "events/SlaughtergleamEvents",
            "client/StorageUpgradeScreen",
            "items/StorageScepterItem",
            "items/StorageScepterItem$Kind",
            "items/RadiantTankItem",
            "items/UnimplementedCharmItem",
            "inventory/TankItemFluidStorage", "inventory/TroveItemStorage",
            "items/EchoItem",
            "items/DebugOrbItem", "events/DebugOrbEvents",
            "blocks/MonitoringCrystal", "blocks/MonitoringCrystal$1", "items/MonitoringCrystalItem", "tileentities/MonitoringCrystalBlockEntity",
            "client/EchoColor", "client/EchoColorCache", "util/EchoTintCache", "util/EchoOreInputs",
            "events/ChestName",
            "items/RadiantAmphoraItem", "items/DispenseAmphora", "inventory/AmphoraFluidStorage",
            "events/AmphoraEvents", "events/AmphoraToggle", "client/AmphoraClient",
            "blocks/RadiantCraftingTable", "tileentities/RadiantCraftingTableBlockEntity",
            "inventory/RadiantCraftingMenu", "inventory/RadiantCraftingMenu$SharedCraftingContainer",
            "inventory/RadiantCraftingMenu$CraftingResultSlot", "inventory/RadiantCraftingMenu$BookmarkSlot", "client/RadiantCraftingScreen",
            "items/DevouringCharmItem", "inventory/DevouringCharmFluids", "inventory/DevouringCharmMenu",
            "inventory/DevouringCharmMenu$CharmProtectedSlot", "inventory/DevouringCharmMenu$FluidDisposalSlot",
            "inventory/DevouringCharmMenu$DisposalSlot", "inventory/DevouringCharmMenu$FilterSlot",
            "client/DevouringCharmScreen", "events/DevouringCharmEvents",
        ))
        if loader == "fabric":
            expected_classes.update(PACKAGE + name + ".class" for name in (
                "inventory/ChestFabricStorage", "inventory/ChestFabricStorage$1",
                "inventory/TroveFabricStorage",
                "inventory/TroveFabricStorage$1",
                "mixin/DevouringCharmPickupMixin",
                "mixin/RivertearRechargeMixin",
                "mixin/StormwayLightningMixin",
                "mixin/MurdergleamCriticalMixin",
                "mixin/SlaughtergleamDeathMixin",
                "mixin/TroveRemovalMixin",
                "mixin/ScepterSneakUseMixin", "mixin/ScepterSneakUseClientMixin",
                "mixin/SwitchgleamTeleportMixin", "mixin/ElixirspindleUseMixin",
            ))
        if loader != "forge":
            expected_classes.update(PACKAGE + name + ".class" for name in (
                "mixin/SlaughtergleamCountMixin", "mixin/SlaughtergleamChanceMixin",
                "mixin/SlaughtergleamEquipmentMixin",
            ))
        if loader != "fabric":
            expected_classes.add(PACKAGE + "client/ArcaneArchivesModClient.class")
        expected_classes.update(PACKAGE + name + ".class" for name in (
            "client/GemCuttersTableScreen", "inventory/GemCuttersTableMenu",
            "inventory/GemCuttersTableMenu$UnavailableSlot", "inventory/GemCuttersTableMenu$OutputSlot", "tileentities/GemCuttersTableBlockEntity$MenuInputs",
        ))
        expected_classes.add(PACKAGE + "recipe/gct/GCTCraftingResult.class")
        expected_classes.update(PACKAGE + "recipe/gct/GemCutterDataRecipe" + suffix + ".class"
                                for suffix in ("", "$Serializer", "$Type", "$Entry"))
        expected_classes.add(PACKAGE + "recipe/gct/GemCutterCraftingState.class")
        expected_classes.add(PACKAGE + "recipe/gct/GemCutterFluidRemainders.class")
        expected_classes.add(PACKAGE + "items/StorageComponentItem.class")
        expected_classes.add(PACKAGE + "items/RawQuartzItem.class")
        expected_classes.add(PACKAGE + "blocks/StorageRawQuartz.class")
        expected_classes.add(PACKAGE + "blocks/RadiantLantern.class")
        expected_classes.add(PACKAGE + "blocks/RawQuartzCluster.class")
        expected_classes.update(PACKAGE + name + ".class" for name in ("blocks/QuartzSliver", "events/SliverSmashing", "events/SliverSmashing$Outcome"))
        expected_classes.update(PACKAGE + name + ".class" for name in (
            "blocks/GemCuttersTable", "tileentities/GemCuttersTableBlockEntity",
            "tileentities/NetworkOwnedBlockEntity",
            "data/StorageNetworks", "mixin/StorageNetworkChunkMixin",
            "data/ManifestContents", "data/ManifestContents$Entry", "data/ManifestContents$Location",
            "data/BrazierRoutes",
            "data/ManifestContents$Range", "data/ManifestContents$Group",
            "data/BrazierRouteCache", "data/BrazierRouteCache$Entry",
            "blocks/Brazier", "tileentities/BrazierBlockEntity",
            "tileentities/BrazierPlayerSelection", "tileentities/BrazierPlayerSelection$Selection",
            "client/AmphoraRenderer",
            "client/BrazierRangeRenderer",
            "client/BrazierRanges$ClientHooks",
            "client/BrazierRanges",
            "client/BrazierRangeState",
            "client/BrazierScreen", "client/BrazierScreen$Control",
            "events/BrazierRadius",
            "inventory/BrazierMenu",
            "events/ManifestSelect", "events/ManifestHover", "events/PlayerPreferences", "mixin/ManifestHoveredSlot", "mixin/ManifestKeyboardMixin",
            "data/ManifestTracking", "data/ManifestTracking$Marker",
            "events/ManifestSnapshotReceiver",
            "events/ClearManifestTracking",
            "blocks/LecternManifest", "items/LecternManifestItem",
            "events/ManifestRequest",
            "events/OpenManifest", "client/ManifestKey",
            "client/ManifestSearch", "client/ManifestSearch$Filter", "client/ManifestScroll", "client/ManifestHighlight",
            "client/ManifestRays", "client/ManifestRays$Beam", "client/ManifestRayRenderer",
            "client/RadiantCraftingTableRenderer", "client/RadiantResonatorRenderer",
            "data/StoragePlacementSaveData", "data/StoragePlacementSaveData$Entry", "items/LimitedStorageBlockItem",
            "client/TroveHud", "client/TroveHudText",
            "items/ManifestItem", "inventory/ManifestMenu", "events/ManifestSnapshot",
            "client/ManifestClient", "client/ManifestScreen",
            "items/EmpoweredQuartzItem",
            "items/ScintillatingInlayItem",
            "recipe/gct/HiveCraftingConditions",
            "items/RadiantDustItem",
            "items/TomeOfArcanaItem", "events/TomeAcquisition",
            "recipe/gct/GCTRecipeList",
            "recipe/gct/GCTRecipe",
            "recipe/CraftingCreator",
            "inventory/handlers/GemCutterInputHandler",
            "init/ContentRegistry", "items/ShapedQuartzItem", "blocks/StorageShapedQuartz",
            "config/ServerSideConfig", "data/PlayerSaveData",
            "inventory/handlers/ExtendedItemStackHandler", "inventory/handlers/SizeUpgradeItemHandler",
            "inventory/handlers/OptionalUpgradesHandler", "types/enums/UpgradeType",
            "types/BlockPosDimension",
            "recipe/IngredientAllocation", "recipe/IngredientStack", "recipe/IngredientsMatcher",
        ))
        if loader == "fabric":
            expected_classes.add("com/aranaira/arcanearchives/tileentities/GemCuttersTableBlockEntity$1.class")
            expected_classes.add("com/aranaira/arcanearchives/inventory/BrazierFabricStorage.class")
            expected_classes.add("com/aranaira/arcanearchives/inventory/BrazierFabricStorage$1.class")
        expected_classes.add(PACKAGE + "inventory/BrazierPullBuffer.class")
        classes = {name for name in names if name.endswith(".class")}
        require(classes == expected_classes,
                f"{node}: extra classes {classes - expected_classes}; missing classes {expected_classes - classes}")
        for name in ("radiantchest", "radiant_upgrades", "radiantcraftingtable", "gemcutterstable", "devouring_charm", "player_inv", "manifest_base", "buttons"):
            for variant in ("", "simple/"):
                resource = f"assets/arcanearchives/textures/gui/{variant}{name}.png"
                require(jar.read(resource) == (ROOT / "src/main/resources" / resource).read_bytes(),
                        f"{node}: missing or changed GUI artwork {resource}")
        for name in classes:
            require(struct.unpack(">H", jar.read(name)[6:8])[0] == major, f"{node}: incorrect Java target")
        invitation_recipe = "recipes" if minecraft == "1.20.1" else "recipe"
        block_tags = "blocks" if minecraft == "1.20.1" else "block"
        require(json.loads(jar.read(f"data/arcanearchives/tags/{block_tags}/tome_bookshelves.json")) ==
                {"replace": False, "values": ["minecraft:bookshelf"]},
                f"{node}: missing or broadened Tome bookshelf tag")
        for resource in ("assets/arcanearchives/models/item/tome_arcana.json",
                         "assets/arcanearchives/textures/item/item_tomeofarcana.png"):
            require(jar.read(resource) == (ROOT / "src/main/resources" / resource).read_bytes(),
                    f"{node}: missing or changed Tome artwork {resource}")
        tome_recipe = (ROOT / "src/main/resources/data/arcanearchives/recipe/tome_arcana.json").read_text()
        require(json.loads(jar.read(f"data/arcanearchives/{invitation_recipe}/tome_arcana.json")) ==
                json.loads(tome_recipe.replace("${result_key}", "item" if minecraft == "1.20.1" else "id")),
                f"{node}: changed Tome acquisition recipe or versioned result")
        for resource in ("assets/arcanearchives/models/item/manifest.json",
                         "assets/arcanearchives/textures/items/item_manifest.png"):
            require(jar.read(resource) == (ROOT / "src/main/resources" / resource).read_bytes(),
                    f"{node}: missing or changed Manifest artwork {resource}")
        require(json.loads(jar.read(f"data/arcanearchives/{invitation_recipe}/manifest.json")) ==
                json.loads((ROOT / "src/main/resources/data/arcanearchives/recipe/manifest.json").read_text()),
                f"{node}: changed Manifest acquisition recipe")
        require({"type": "minecraft:single", "resource": "arcanearchives:items/item_manifest"} in
                json.loads(jar.read("assets/minecraft/atlases/blocks.json"))["sources"], f"{node}: missing Manifest atlas entry")
        for resource in ("assets/arcanearchives/models/item/writ_expulsion.json",
                         "assets/arcanearchives/textures/items/item_writofexpulsion.png"):
            require(jar.read(resource) == (ROOT / "src/main/resources" / resource).read_bytes(),
                    f"{node}: missing or changed writ artwork {resource}")
        require(json.loads(jar.read(f"data/arcanearchives/{invitation_recipe}/writ_expulsion.json")) ==
                json.loads((ROOT / "src/main/resources/data/arcanearchives/recipe/writ_expulsion.json").read_text()),
                f"{node}: changed writ recipe/condition")
        require({"type": "minecraft:single", "resource": "arcanearchives:items/item_writofexpulsion"} in
                json.loads(jar.read("assets/minecraft/atlases/blocks.json"))["sources"], f"{node}: missing writ atlas entry")
        writ_mixins = "arcanearchives.forge.mixins.json" if loader == "forge" else "arcanearchives.mixins.json"
        require("WritAnvilMixin" in json.loads(jar.read(writ_mixins))["mixins"], f"{node}: missing writ anvil hook")
        require("StorageNetworkChunkMixin" in json.loads(jar.read(writ_mixins))["mixins"],
                f"{node}: missing storage network lifecycle hooks")
        if loader == "forge":
            network_mappings = json.loads(jar.read("arcanearchives.refmap.json"))["mappings"].get(
                "com/aranaira/arcanearchives/mixin/StorageNetworkChunkMixin", {})
            require(all(";m_" in network_mappings.get(method, "") for method in (
                "setBlockEntity", "removeBlockEntity", "registerAllBlockEntitiesAfterLevelLoad", "clearAllBlockEntities")),
                f"{node}: missing reobfuscated storage network lifecycle selectors")
        for resource in ("assets/arcanearchives/models/item/letter_resignation.json",
                         "assets/arcanearchives/textures/items/item_letterofresignation.png"):
            require(jar.read(resource) == (ROOT / "src/main/resources" / resource).read_bytes(),
                    f"{node}: missing or changed resignation artwork {resource}")
        require(json.loads(jar.read(f"data/arcanearchives/{invitation_recipe}/letter_resignation.json")) ==
                json.loads((ROOT / "src/main/resources/data/arcanearchives/recipe/letter_resignation.json").read_text()),
                f"{node}: changed resignation recipe/condition")
        require({"type": "minecraft:single", "resource": "arcanearchives:items/item_letterofresignation"} in
                json.loads(jar.read("assets/minecraft/atlases/blocks.json"))["sources"], f"{node}: missing resignation atlas entry")
        for resource in ("assets/arcanearchives/models/item/letter_invitation.json",
                         "assets/arcanearchives/textures/items/item_letterofinvitation.png"):
            require(jar.read(resource) == (ROOT / "src/main/resources" / resource).read_bytes(),
                    f"{node}: missing or changed invitation artwork {resource}")
        require(json.loads(jar.read(f"data/arcanearchives/{invitation_recipe}/letter_invitation.json")) ==
                json.loads((ROOT / "src/main/resources/data/arcanearchives/recipe/letter_invitation.json").read_text()),
                f"{node}: changed invitation recipe/condition")
        require({"type": "minecraft:single", "resource": "arcanearchives:items/item_letterofinvitation"} in
                json.loads(jar.read("assets/minecraft/atlases/blocks.json"))["sources"], f"{node}: missing invitation atlas entry")
        for asset in ("models/item/echo.json", "textures/items/echoes_layer0.png",
                      "textures/items/echoes_layer1.png", "textures/items/echoes_layer1.png.mcmeta",
                      "textures/items/echoes_layer2.png", "textures/items/echoes_layer2.png.mcmeta"):
            resource = "assets/arcanearchives/" + asset
            require(jar.read(resource) == (ROOT / "src/main/resources" / resource).read_bytes(),
                    f"{node}: missing or changed Echo artwork {resource}")
        atlas = json.loads(jar.read("assets/minecraft/atlases/blocks.json"))
        for asset in ("models/block/lectern_manifest.obj", "models/block/lectern_manifest.mtl",
                      "models/block/lectern_manifest_accessor.json", "models/item/lectern_manifest.json",
                      "blockstates/lectern_manifest.json", "textures/blocks/block_lecterngrate.png", "textures/transparent.png"):
            resource = "assets/arcanearchives/" + asset
            require(jar.read(resource) == (ROOT / "src/main/resources" / resource).read_bytes(),
                    f"{node}: missing or changed Manifest Lectern resource {resource}")
        resource = "assets/arcanearchives/models/block/lectern_manifest.json"
        lectern_loader = "neoforge" if "neoforge" in node else "forge"
        require(json.loads(jar.read(resource)) == json.loads((ROOT / "src/main/resources" / resource).read_text().replace("${obj_loader}", lectern_loader)),
                f"{node}: incorrect Manifest Lectern OBJ loader/model")
        for sprite in ("arcanearchives:blocks/block_lecterngrate", "arcanearchives:transparent", "arcanearchives:items/item_manifest"):
            require({"type": "minecraft:single", "resource": sprite} in atlas["sources"], f"{node}: missing lectern atlas sprite {sprite}")
        recipe_folder = "recipes" if minecraft == "1.20.1" else "recipe"
        lectern_recipe = (ROOT / "src/main/resources/data/arcanearchives/recipe/lectern_manifest.json").read_text()
        require(json.loads(jar.read(f"data/arcanearchives/{recipe_folder}/lectern_manifest.json"))
                == json.loads(lectern_recipe.replace("${result_key}", "item" if minecraft == "1.20.1" else "id")),
                f"{node}: incorrect native lectern recipe schema")
        for asset in ("models/block/monitoring_crystal.obj", "models/block/monitoring_crystal.mtl",
                      "models/item/monitoring_crystal.json", "blockstates/monitoring_crystal.json"):
            resource = "assets/arcanearchives/" + asset
            require(jar.read(resource) == (ROOT / "src/main/resources" / resource).read_bytes(),
                    f"{node}: missing or changed Monitoring Crystal resource {resource}")
        resource = "assets/arcanearchives/models/block/monitoring_crystal.json"
        obj_loader = "neoforge" if "neoforge" in node else "forge"
        require(json.loads(jar.read(resource)) == json.loads((ROOT / "src/main/resources" / resource).read_text().replace("${obj_loader}", obj_loader)),
                f"{node}: incorrect Monitoring Crystal OBJ loader/model")
        recipe_path = "data/arcanearchives/" + ("recipes" if minecraft == "1.20.1" else "recipe") + "/monitoring_crystal.json"
        require(json.loads(jar.read(recipe_path)) == json.loads((ROOT / "src/main/resources/data/arcanearchives/recipe/monitoring_crystal.json").read_text()),
                f"{node}: incorrect Monitoring Crystal recipe")
        for asset in ("models/item/debugorb.json", "textures/items/debugorb.png", "textures/items/debugorb.png.mcmeta"):
            resource = "assets/arcanearchives/" + asset
            require(jar.read(resource) == (ROOT / "src/main/resources" / resource).read_bytes(),
                    f"{node}: missing or changed Debug Orb artwork {resource}")
        require({"type": "minecraft:single", "resource": "arcanearchives:items/debugorb"} in atlas["sources"],
                f"{node}: missing Debug Orb atlas entry")
        advancement_folder = "advancements" if minecraft == "1.20.1" else "advancement"
        advancement_names = {
            "manifest", "lectern",
            "monitoring_crystal",
            "amphora", "chest", "containment_field", "devouring_charm", "gemcutters_table",
            "material_interface", "matrix_brace", "raw_quartz", "raw_quartz_cluster", "resonator", "root",
            "scepter_manipulation", "scepter_revelation", "shaped_quartz_block", "slivers", "tank", "trove", "workbench",
        }
        language = json.loads(jar.read("assets/arcanearchives/lang/en_us.json"))
        for advancement_name in advancement_names:
            resource = f"data/arcanearchives/{advancement_folder}/{advancement_name}.json"
            actual = json.loads(jar.read(resource))
            source = (ROOT / f"src/main/resources/data/arcanearchives/advancement/{advancement_name}.json").read_text()
            expected = json.loads(source.replace("${result_key}", "item" if minecraft == "1.20.1" else "id"))
            require(actual == expected, f"{node}: changed advancement {advancement_name}")
            require("parent" not in actual or actual["parent"].removeprefix("arcanearchives:") in advancement_names,
                    f"{node}: missing advancement parent {advancement_name}")
            for field in ("title", "description"):
                require(actual["display"][field]["translate"] in language,
                        f"{node}: untranslated advancement {advancement_name}/{field}")
            if "background" in actual["display"]:
                background = "assets/" + actual["display"]["background"].replace(":", "/", 1)
                require(background in names, f"{node}: missing advancement background")
        for item, texture in (("obstruction_charm", "obstruction_charm"), ("serenity_charm", "item_serenitycharm"),
                              ("scepter_translocation", "item_sceptertranslocation")):
            for asset in (f"models/item/{item}.json", f"textures/items/{texture}.png"):
                resource = "assets/arcanearchives/" + asset
                require(jar.read(resource) == (ROOT / "src/main/resources" / resource).read_bytes(),
                        f"{node}: missing or changed item artwork {asset}")
            require({"type": "minecraft:single", "resource": f"arcanearchives:items/{texture}"}
                    in atlas["sources"], f"{node}: missing item atlas entry {item}")
            recipe_folder = "recipes" if minecraft == "1.20.1" else "recipe"
            recipe = json.loads(jar.read(f"data/arcanearchives/{recipe_folder}/{item}.json"))
            source_recipe = json.loads((ROOT / f"src/main/resources/data/arcanearchives/recipe/{item}.json").read_text())
            require(recipe == source_recipe, f"{node}: missing or changed item recipe {item}")
        for layer in range(3):
            require({"type": "minecraft:single", "resource": f"arcanearchives:items/echoes_layer{layer}"}
                    in atlas["sources"], f"{node}: missing Echo atlas layer {layer}")
        tank_mixins = json.loads(jar.read("arcanearchives.forge.mixins.json" if loader == "forge" else "arcanearchives.mixins.json"))
        require("TankItemRenderMixin" in tank_mixins.get("client", [])
                and "TankItemRenderMixin" not in tank_mixins["mixins"],
                f"{node}: packed Tank rendering must be client-only")
        if loader == "fabric":
            patchouli = PROPERTIES[loader][minecraft]["deps"]["patchouli"]
            mixins = json.loads(jar.read("arcanearchives.fabric.mixins.json"))
            require("ScepterSneakUseMixin" in mixins["mixins"]
                    and "ScepterSneakUseClientMixin" in mixins.get("client", [])
                    and "ScepterSneakUseClientMixin" not in mixins["mixins"],
                    f"{node}: incorrect sided scepter mixin registration")
            require(data["depends"].get("patchouli") == f"={patchouli}", f"{node}: wrong Patchouli dependency")
            require(data["entrypoints"]["main"] == [f"com.aranaira.arcanearchives.ArcaneArchives{entrypoint}"], f"{node}: wrong entrypoint")
            require(data["entrypoints"]["client"] == ["com.aranaira.arcanearchives.client.ArcaneArchivesFabricClient"], f"{node}: wrong client entrypoint")
            require(data["depends"]["minecraft"] == f"={minecraft}", f"{node}: untested Minecraft range")
        else:
            patchouli = PROPERTIES[loader][minecraft]["deps"]["patchouli"]
            book_dependency = next(d for d in data["dependencies"]["arcanearchives"] if d["modId"] == "patchouli")
            require(book_dependency["versionRange"] == f"[{patchouli}]", f"{node}: wrong Patchouli dependency")
            require(book_dependency.get("mandatory") is True if loader == "forge" else book_dependency.get("type") == "required",
                    f"{node}: Patchouli must be required")
            require(book_dependency["side"] == "BOTH" and book_dependency["ordering"] == "AFTER",
                    f"{node}: wrong Patchouli side or load order")
            dependency = next(d for d in data["dependencies"]["arcanearchives"] if d["modId"] == "minecraft")
            require(dependency["versionRange"] == f"[{minecraft}]", f"{node}: untested Minecraft range")
    with ZipFile(directory / f"{basename}-sources.jar") as jar:
        require_no_test_content(jar.namelist(), node)
        require(PACKAGE + "items/UnimplementedCharmItem.java" in jar.namelist(), f"{node}: missing charm source")
        require(PACKAGE + "items/EchoItem.java" in jar.namelist(), f"{node}: missing Echo source")
        for name in ("items/DebugOrbItem", "events/DebugOrbEvents"):
            require(PACKAGE + name + ".java" in jar.namelist(), f"{node}: missing Debug Orb source {name}")
        for name in ("blocks/MonitoringCrystal", "items/MonitoringCrystalItem", "tileentities/MonitoringCrystalBlockEntity"):
            require(PACKAGE + name + ".java" in jar.namelist(), f"{node}: missing Monitoring Crystal source {name}")
        for name in ("events/GemSound", "client/GemSoundClient"):
            require(PACKAGE + name + ".java" in jar.namelist(), f"{node}: missing gem sound source {name}")
        require(PACKAGE + "items/RadiantTroveItem.java" in jar.namelist(), f"{node}: missing packed Trove item source")
        for name in ("items/WritOfExpulsionItem", "mixin/WritAnvilMixin"):
            require(PACKAGE + name + ".java" in jar.namelist(), f"{node}: missing writ source {name}")
        for name in ("data/HiveSaveData", "items/LetterOfInvitationItem", "events/HiveCommands", "items/LetterItem", "items/LetterOfResignationItem"):
            require(PACKAGE + name + ".java" in jar.namelist(), f"{node}: missing Hive source {name}")
        for name in ("blocks/RadiantTrove", "blocks/RadiantTank", "items/StorageUpgradeBlockItem",
                     "tileentities/RadiantTroveBlockEntity", "tileentities/RadiantTankBlockEntity",
                     "inventory/RadiantTankStorage", "client/RadiantTankRenderer", "mixin/TankItemRenderMixin",
                     "items/RadiantAmphoraItem", "items/DispenseAmphora", "inventory/AmphoraFluidStorage",
                     "events/AmphoraEvents", "events/AmphoraToggle", "client/AmphoraClient",
                     "blocks/RadiantCraftingTable", "tileentities/RadiantCraftingTableBlockEntity",
                     "inventory/RadiantCraftingMenu", "client/RadiantCraftingScreen",
                     "items/DevouringCharmItem", "inventory/DevouringCharmFluids", "inventory/DevouringCharmMenu",
                     "client/DevouringCharmScreen", "events/DevouringCharmEvents"):
            require(PACKAGE + name + ".java" in jar.namelist(), f"{node}: missing storage source {name}")
        if loader == "fabric":
            require(PACKAGE + "inventory/TroveFabricStorage.java" in jar.namelist(), f"{node}: missing Trove adapter source")
        if loader == "fabric":
            for name in ("ArcaneArchivesFabricClient", "GemCutterFabricModel"):
                require(PACKAGE + "client/" + name + ".java" in jar.namelist(), f"{node}: missing model adapter source")
        require(PACKAGE + "ArcaneArchivesMod.java" in jar.namelist(), f"{node}: missing common source")
        for name in ("client/GemCuttersTableScreen", "inventory/GemCuttersTableMenu", "config/ClientConfig", "client/GuiTextures"):
            require(PACKAGE + name + ".java" in jar.namelist(), f"{node}: missing menu source")
        if loader != "fabric":
            require(PACKAGE + "client/ArcaneArchivesModClient.java" in jar.namelist(), f"{node}: missing client registration source")
        require(PACKAGE + f"ArcaneArchives{entrypoint}.java" in jar.namelist(), f"{node}: missing loader source")
        require(PACKAGE + "util/MathUtils.java" in jar.namelist(), f"{node}: missing migrated utility source")
        require(PACKAGE + "recipe/gct/GCTCraftingResult.java" in jar.namelist(), f"{node}: missing crafting result source")
        require(PACKAGE + "recipe/gct/GemCutterDataRecipe.java" in jar.namelist(), f"{node}: missing native recipe source")
        require(PACKAGE + "recipe/gct/GemCutterCraftingState.java" in jar.namelist(), f"{node}: missing crafting state source")
        require(PACKAGE + "recipe/gct/GemCutterFluidRemainders.java" in jar.namelist(), f"{node}: missing fluid return source")
        require(PACKAGE + "items/StorageComponentItem.java" in jar.namelist(), f"{node}: missing storage component source")
        require(PACKAGE + "items/RawQuartzItem.java" in jar.namelist(), f"{node}: missing raw quartz source")
        require(PACKAGE + "blocks/StorageRawQuartz.java" in jar.namelist(), f"{node}: missing raw storage source")
        require(PACKAGE + "blocks/RadiantLantern.java" in jar.namelist(), f"{node}: missing lantern source")
        require(PACKAGE + "blocks/RawQuartzCluster.java" in jar.namelist(), f"{node}: missing cluster source")
        for name in ("blocks/QuartzSliver", "events/SliverSmashing"):
            require(PACKAGE + name + ".java" in jar.namelist(), f"{node}: missing sliver source")
        for name in ("init/ContentRegistry", "items/ShapedQuartzItem", "blocks/StorageShapedQuartz", "config/ServerSideConfig", "data/PlayerSaveData",
                     "blocks/GemCuttersTable", "tileentities/GemCuttersTableBlockEntity",
                     "tileentities/NetworkOwnedBlockEntity",
                     "data/StorageNetworks", "mixin/StorageNetworkChunkMixin",
                     "data/ManifestContents",
                     "data/BrazierRoutes",
                     "events/ManifestSelect", "data/ManifestTracking",
                     "data/BrazierRouteCache",
                     "blocks/Brazier", "tileentities/BrazierBlockEntity",
                     "tileentities/BrazierPlayerSelection", "inventory/BrazierItemAutomation",
                     "inventory/BrazierPullBuffer",
                     "client/AmphoraRenderer",
                     "client/BrazierRangeRenderer",
                     "client/BrazierRanges",
                     "client/BrazierRangeState",
                     "client/BrazierScreen",
                     "events/BrazierRadius",
                     "inventory/BrazierMenu",
                     "events/ManifestSnapshotReceiver",
                     "events/ClearManifestTracking",
                     "blocks/LecternManifest", "items/LecternManifestItem",
                     "events/ManifestRequest",
                     "events/OpenManifest", "client/ManifestKey",
                     "client/ManifestSearch", "client/ManifestScroll", "client/ManifestHighlight",
                     "client/ManifestRays", "client/ManifestRayRenderer",
                     "client/RadiantCraftingTableRenderer", "client/RadiantResonatorRenderer",
                     "data/StoragePlacementSaveData", "items/LimitedStorageBlockItem",
                     "client/TroveHud", "client/TroveHudText",
                     "items/ManifestItem", "inventory/ManifestMenu", "events/ManifestSnapshot",
                     "client/ManifestClient", "client/ManifestScreen",
                     "items/EmpoweredQuartzItem",
                     "items/ScintillatingInlayItem",
                     "recipe/gct/HiveCraftingConditions",
                     "items/RadiantDustItem",
                     "recipe/gct/GCTRecipeList",
                     "recipe/gct/GCTRecipe",
                     "recipe/CraftingCreator",
                     "inventory/handlers/GemCutterInputHandler",
                     "inventory/handlers/ExtendedItemStackHandler", "inventory/handlers/SizeUpgradeItemHandler",
                     "items/TomeOfArcanaItem", "events/TomeAcquisition",
                     "inventory/handlers/OptionalUpgradesHandler", "types/enums/UpgradeType",
                     "types/BlockPosDimension",
                     "recipe/IngredientAllocation", "recipe/IngredientStack", "recipe/IngredientsMatcher"):
            require(PACKAGE + name + ".java" in jar.namelist(), f"{node}: missing content source {name}")
    print(f"PASS {node}: metadata, entrypoint, Java target, notices, production/source JARs")
print(f"Verified {len(TARGETS)} target pairs. Artifact checks do not verify gameplay or runtime startup.")
