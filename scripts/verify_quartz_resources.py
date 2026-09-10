"""Verify the shaped-quartz resource slice in every production JAR (not gameplay)."""
import json
import hashlib
from pathlib import Path
import tomllib
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
VERSION = tomllib.loads((ROOT / "stonecutter.properties.toml").read_text())["mod"]["version"]
NODES = ("1.20.1-fabric", "1.20.1-forge", "1.21.1-fabric", "1.21.1-neoforge")

for node in NODES:
    minecraft, loader = node.split("-", 1)
    modern = minecraft == "1.21.1"
    recipe_dir, loot_dir, block_tag = ("recipe", "loot_table", "block") if modern else ("recipes", "loot_tables", "blocks")
    result_key = "id" if modern else "item"
    path = ROOT / "versions" / node / "build/libs" / f"arcanearchives-{loader}-{VERSION}+{minecraft}.jar"
    with ZipFile(path) as jar:
        def load(name):
            return json.loads(jar.read(name))

        lantern_model = jar.read("assets/arcanearchives/models/block/radiant_lantern.json").decode()
        expected_loader = "neoforge" if loader == "neoforge" else "forge"
        cluster_model = jar.read("assets/arcanearchives/models/block/raw_quartz_cluster.json").decode()
        assert load("assets/arcanearchives/models/block/raw_quartz_cluster.json")["loader"] == expected_loader + ":obj", node
        cluster_normalized = cluster_model.replace('"loader": "' + expected_loader + ':obj"', '"loader": "${obj_loader}:obj"')
        assert hashlib.sha256(cluster_normalized.encode()).hexdigest() == "67b6d74150aafb216671349ebbf6faa704a8cb6f535f6f7b28c994ed33c5894a", node
        predicate = {"predicates": {"minecraft:enchantments": [{"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}} if modern else {"enchantments": [{"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]}
        assert load(f"data/arcanearchives/{loot_dir}/blocks/raw_quartz_cluster.json") == {
            "type": "minecraft:block", "pools": [{"rolls": 1,
                "entries": [{"type": "minecraft:alternatives", "children": [
                    {"type": "minecraft:item", "name": "arcanearchives:raw_quartz_cluster", "conditions": [{"condition": "minecraft:match_tool", "predicate": predicate}]},
                    {"type": "minecraft:item", "name": "arcanearchives:raw_quartz"}]}],
                "conditions": [{"condition": "minecraft:survives_explosion"}]}],
        }, node
        assert "arcanearchives:raw_quartz_cluster" in load(f"data/minecraft/tags/{block_tag}/mineable/pickaxe.json")["values"], node
        sliver_model = jar.read("assets/arcanearchives/models/block/quartz_sliver.json").decode()
        assert load("assets/arcanearchives/models/block/quartz_sliver.json")["loader"] == expected_loader + ":obj", node
        sliver_normalized = sliver_model.replace('"loader": "' + expected_loader + ':obj"', '"loader": "${obj_loader}:obj"')
        assert hashlib.sha256(sliver_normalized.encode()).hexdigest() == "56dc120be31ed8b22aae586f43265d762a1c1426e9f2ba8a39e093eafb949bc6", node
        assert load(f"data/arcanearchives/{loot_dir}/blocks/quartz_sliver.json") == {
            "type": "minecraft:block", "pools": [{"rolls": 1,
                "entries": [{"type": "minecraft:item", "name": "arcanearchives:quartz_sliver"}],
                "conditions": [{"condition": "minecraft:survives_explosion"}]}],
        }, node
        assert load("assets/arcanearchives/models/block/radiant_lantern.json")["loader"] == expected_loader + ":obj", node
        normalized = lantern_model.replace('"loader": "' + expected_loader + ':obj"', '"loader": "${obj_loader}:obj"')
        assert hashlib.sha256(normalized.encode()).hexdigest() == "ebce712ddfc9190d140b2aea17e4d5453c079bc503da50beb4bd9716fdf4e2c7", node
        assert load(f"data/arcanearchives/{loot_dir}/blocks/radiant_lantern.json")["pools"][0]["entries"][0]["name"] == "arcanearchives:radiant_lantern", node
        assert "arcanearchives:radiant_lantern" in load(f"data/minecraft/tags/{block_tag}/mineable/pickaxe.json")["values"], node

        table = load(f"data/arcanearchives/{recipe_dir}/gemcutters_table.json")
        assert table["type"] == "minecraft:crafting_shaped", node
        assert table["pattern"] == ["DGP", "WCW", "QWQ"], node
        assert table["result"] == {result_key: "arcanearchives:gemcutters_table", "count": 1}, node
        expected_keys = {key: {"tag": f"arcanearchives:ingredients/{tag}"} for key, tag in {
            "D": "diorite_or_marble", "G": "glass_panes", "P": "paper", "C": "workbenches", "W": "logs",
        }.items()}
        expected_keys["Q"] = {"item": "arcanearchives:raw_quartz"}
        assert table["key"] == expected_keys, node
        panes = ["minecraft:glass_pane"] + [f"minecraft:{color}_stained_glass_pane" for color in (
            "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black",
        )]
        item_tags = "item" if modern else "items"
        for tag, defaults, aliases in (
            ("diorite_or_marble", ["minecraft:diorite"], ["c:stones/marble", "c:storage_blocks/marble", "forge:stone/marble", "forge:storage_blocks/marble"]),
            ("glass_panes", panes, ["c:glass_panes", "forge:glass_panes"]),
            ("paper", ["minecraft:paper"], ["c:paper", "forge:paper"]),
            ("workbenches", ["minecraft:crafting_table"], ["c:workbenches", "forge:workbenches"]),
            ("logs", ["#minecraft:logs"], ["c:logs", "forge:logs"]),
        ):
            assert load(f"data/arcanearchives/tags/{item_tags}/ingredients/{tag}.json") == {
                "replace": False, "values": defaults + [{"id": "#" + alias, "required": False} for alias in aliases],
            }, (node, tag)
            wrong_tag_root = "items" if modern else "item"
            assert f"data/arcanearchives/tags/{wrong_tag_root}/ingredients/{tag}.json" not in jar.namelist(), node

        pack = load(f"data/arcanearchives/{recipe_dir}/storage_shaped_quartz.json")
        raw_pack = load(f"data/arcanearchives/{recipe_dir}/storage_raw_quartz.json")
        raw_unpack = load(f"data/arcanearchives/{recipe_dir}/destorage_rawquartz.json")
        assert raw_pack["type"] == raw_unpack["type"] == "minecraft:crafting_shapeless", node
        assert raw_pack["ingredients"] == [{"item": "arcanearchives:raw_quartz"}] * 9, node
        assert raw_pack["result"] == {result_key: "arcanearchives:storage_raw_quartz", "count": 1}, node
        assert raw_unpack["ingredients"] == [{"item": "arcanearchives:storage_raw_quartz"}], node
        assert raw_unpack["result"] == {result_key: "arcanearchives:raw_quartz", "count": 9}, node
        assert load("assets/arcanearchives/blockstates/storage_raw_quartz.json") == {
            "variants": {"": {"model": "arcanearchives:block/storage_raw_quartz"}},
        }, node
        assert load(f"data/arcanearchives/{loot_dir}/blocks/storage_raw_quartz.json")["pools"][0]["entries"][0]["name"] == "arcanearchives:storage_raw_quartz", node
        assert "arcanearchives:storage_raw_quartz" in load(f"data/minecraft/tags/{block_tag}/mineable/pickaxe.json")["values"], node
        unpack = load(f"data/arcanearchives/{recipe_dir}/destorage_shapedquartz.json")
        assert pack["type"] == unpack["type"] == "minecraft:crafting_shapeless", node
        assert pack["ingredients"] == [{"item": "arcanearchives:shaped_quartz"}] * 9, node
        assert pack["result"] == {result_key: "arcanearchives:storage_shaped_quartz", "count": 1}, node
        assert unpack["ingredients"] == [{"item": "arcanearchives:storage_shaped_quartz"}], node
        assert unpack["result"] == {result_key: "arcanearchives:shaped_quartz", "count": 9}, node
        loot = load(f"data/arcanearchives/{loot_dir}/blocks/storage_shaped_quartz.json")
        assert loot["pools"][0]["entries"][0]["name"] == "arcanearchives:storage_shaped_quartz", node
        assert "arcanearchives:storage_shaped_quartz" in load(f"data/minecraft/tags/{block_tag}/mineable/pickaxe.json")["values"], node
        state = load("assets/arcanearchives/blockstates/storage_shaped_quartz.json")
        assert state == {"variants": {"": {"model": "arcanearchives:block/storage_cut_quartz"}}}, node
        for model in ("item/shaped_quartz", "item/storage_shaped_quartz", "block/storage_cut_quartz"):
            load(f"assets/arcanearchives/models/{model}.json")
        for texture in ("items/item_cutquartz", "blocks/block_storage_cutquartz"):
            assert jar.read(f"assets/arcanearchives/textures/{texture}.png").startswith(b"\x89PNG\r\n\x1a\n"), node
            assert "animation" in load(f"assets/arcanearchives/textures/{texture}.png.mcmeta"), node
        assert load("assets/minecraft/atlases/blocks.json")["sources"] == [
            {"type": "minecraft:single", "resource": "arcanearchives:items/item_quartzsliver"},
            {"type": "minecraft:single", "resource": "arcanearchives:items/item_component_matrixbrace"},
            {"type": "minecraft:single", "resource": "arcanearchives:blocks/block_storage_rawquartz"},
            {"type": "minecraft:single", "resource": "arcanearchives:items/item_rawquartz"},
            {"type": "minecraft:single", "resource": "arcanearchives:items/item_component_materialinterface"},
            {"type": "minecraft:single", "resource": "arcanearchives:items/item_component_containmentfield"},
            {"type": "minecraft:single", "resource": "arcanearchives:blocks/block_arcanearchives_master"},
            {"type": "minecraft:single", "resource": "arcanearchives:blocks/block_gemcutterstable"},
            {"type": "minecraft:single", "resource": "arcanearchives:blocks/block_lensglass"},
            {"type": "minecraft:single", "resource": "arcanearchives:blocks/block_gemcutterstable_tools"},
            {"type": "minecraft:single", "resource": "arcanearchives:blocks/block_gemcutterstable_scroll"},
            {"type": "minecraft:single", "resource": "arcanearchives:items/item_empquartz"},
            {"type": "minecraft:single", "resource": "arcanearchives:items/item_empquartz_layer1"},
            {"type": "minecraft:single", "resource": "arcanearchives:items/item_component_scintillantinginlay"},
            {"type": "minecraft:single", "resource": "arcanearchives:items/item_component_radiantdust"},
            {"type": "minecraft:single", "resource": "arcanearchives:items/item_cutquartz"},
            {"type": "minecraft:single", "resource": "arcanearchives:blocks/block_storage_cutquartz"},
        ], node
        for language in ("en_us", "pt_br"):
            translations = load(f"assets/arcanearchives/lang/{language}.json")
            assert translations["block.arcanearchives.raw_quartz_cluster"] == ("Raw Quartz Cluster" if language == "en_us" else "Quartzo Radiante Bruto"), node
            assert translations["block.arcanearchives.quartz_sliver"] == "Sliver of Light", node
            assert translations["arcanearchives.tooltip.item.quartz_sliver"] == "An alternative to torches. Acquire by smashing Radiant Quartz against stone.", node
            assert translations["block.arcanearchives.radiant_lantern"] == ("Radiant Lantern" if language == "en_us" else "Lâmpada Radiante"), node
            assert translations["arcanearchives.tooltip.device.radiant_lantern"] == ("Decoration. A light source." if language == "en_us" else "Decoração. Uma fonte de luz."), node
            assert translations["item.arcanearchives.matrix_brace"] == ("Matrix Brace" if language == "en_us" else "Suporte da Matriz"), node
            assert translations["arcanearchives.tooltip.item.matrix_brace"] == "A crafting ingredient. Can also be used as a first tier upgrade for Radiant Tanks and Radiant Troves.", node
            assert translations["item.arcanearchives.raw_quartz"] == ("Raw Radiant Quartz" if language == "en_us" else "Quartzo Radiante Bruto"), node
            assert translations["arcanearchives.tooltip.item.raw_quartz"] == (
                "A crafting ingredient. Can also be used to upgrade chests and crafting tables into their Radiant versions." if language == "en_us" else
                "Um ingrediente. Também pode ser usado para transformar baús e mesas de trabalho para seus equivalentes radiantes."
            ), node
            assert translations["arcanearchives.tooltip.notimplemented1"] == ("UNIMPLEMENTED" if language == "en_us" else "NÃO IMPLEMENTADO"), node
            assert translations["arcanearchives.tooltip.notimplemented2"] == ("Using this item may crash your game!" if language == "en_us" else "Aviso: usar este item pode crashar o jogo!"), node
            if language == "en_us":
                assert translations["item.arcanearchives.empowered_quartz"] == "Empowered Radiant Quartz", node
            assert translations["item.arcanearchives.scintillating_inlay"] == ("Scintillating Inlay" if language == "en_us" else "Detalhe Cintilante"), node
            assert translations["arcanearchives.tooltip.item.scintillating_inlay"] == ("A crafting ingredient." if language == "en_us" else "Um ingrediente."), node
            assert translations["item.arcanearchives.radiant_dust"] == ("Radiant Dust" if language == "en_us" else "Pó Radiante"), node
            assert translations["arcanearchives.tooltip.item.radiant_dust"] == ("A crafting ingredient." if language == "en_us" else "Um ingrediente."), node
            for key in ("item.arcanearchives.shaped_quartz", "block.arcanearchives.storage_shaped_quartz", "itemGroup.arcanearchives", "arcanearchives.tooltip.item.shaped_quartz", "arcanearchives.tooltip.item.storage_shaped_quartz"):
                assert translations[key], (node, key)
        wrong = ("recipes", "loot_tables", "blocks") if modern else ("recipe", "loot_table", "block")
        assert load("assets/arcanearchives/models/item/radiant_dust.json") == {
            "parent": "item/generated", "textures": {"layer0": "arcanearchives:items/item_component_radiantdust"},
        }, node
        dust = "assets/arcanearchives/textures/items/item_component_radiantdust.png"
        for asset, digest in {
            "models/block/raw_quartz.obj": "a5c8010c65617bc77e2fce6b58db75d0a1867441941516cf414d6079fc0e7d70",
            "models/block/raw_quartz.mtl": "c300b8bdf438df7660738586c591890410c86ba3e6996b68cae6fcfced738280",
            "models/item/raw_quartz_cluster.json": "0e158b0c8f6391ef59425f1d63fb84e58b152802cccbac620b1a0e63315cb34f",
            "blockstates/raw_quartz_cluster.json": "4892c5a52478518f3f637caedcb8995b346b19bdffb2905d8083f265f3a53685",
            "models/block/quartz_sliver.obj": "0bdfda4c4bd8f30daf19af28bb25d4e9d69c02c286704da8a395c40b4d81e935",
            "models/block/quartz_sliver.mtl": "c9236b7f0e1785122f82ef7590623649a1802a912c47e94ad1f2adad1ad10483",
            "models/item/quartz_sliver.json": "c07071543055975c36a3ce88cd90fa20d694bf9347fa069e704c54667fa6447c",
            "textures/items/item_quartzsliver.png": "48e53c4e03357d22236a4209754bc424331ec85b2ec121ea1c0f59b3e9f9de6a",
            "blockstates/quartz_sliver.json": "0d4b97198aabd0608a62783a763a68eaec6052c7d17da74a5fa8020557262b3e",
            "models/block/radiant_lantern.obj": "6b15216a989aae0de7bbb353ae30d877d923db7c15209f26770635188062b6e2",
            "models/block/radiant_lantern.mtl": "c18241f6a056dba898b600c6b99f4fab17df56bbe5afc14af7aa02b7fba05c2c",
            "models/item/radiant_lantern.json": "37cd967f5358223407a8da2df8e93b120d61c5d5cf6df496167c0610d5e67d81",
            "blockstates/radiant_lantern.json": "7b7fa367779a1ef7cd52dbb6828f39df8cee4e3def17fb80c25af1377a374314",
            "models/item/matrix_brace.json": "07263a1a83864af891cadd447d77fec14013a2e885929754485237c2c98601d3",
            "textures/items/item_component_matrixbrace.png": "dfc9766cd3fa53a7c64f63d08225c5a317458b4c916ee61ccf5cad0be71973e0",
            "models/item/storage_raw_quartz.json": "1c8446366ec9a9d94da993ed31ada8c4d16f028bf004ec745f99228288b84ac3",
            "models/block/storage_raw_quartz.json": "fce0a21fc7e33baaf632c3aa92f79b68ba064d1dcfa5e78abf0363c60b24907b",
            "textures/blocks/block_storage_rawquartz.png": "d7ed65e10ef7ef7917cac1a4e0faf990f2503424a6f35867e636a1c0ad442e97",
            "models/item/raw_quartz.json": "5664354129fb12bb3175ec148afb1a7b5ca17cf5504532076d3452c84f0a256f",
            "textures/items/item_rawquartz.png": "a0a32833a61bc58345524a09d808d39ddf85a4d8a64b5078a32c0a17c2c0e832",
            "models/item/material_interface.json": "976f7406570c45ad2f54c11cd1d35aa77c81c6f2b441fbfe04de1f0799c9e1b7",
            "models/item/containment_field.json": "8b57570efb0bf0448bfb84bb5fc88c65a1652bef84bbda0069b5b8c010b17d3f",
            "textures/items/item_component_materialinterface.png": "05073751c4df24ddc91b0c3840427a0283a1a28c14e368b918532ee9a9dd9ff4",
            "textures/items/item_component_containmentfield.png": "3e82a5cda21ece3b5944058fe76b7d2671d43f925be571229a376bddfe25ced8",
            "textures/items/item_component_containmentfield.png.mcmeta": "c822d44d5983d3a221e71f1adbcbfa66a03cb12e777ae41a7f7317fc0ab87992",
            "models/item/empowered_quartz.json": "59b11fd925ad26d56eec106b84fa4e575794158d4108989cbd9f0661635b31a3",
            "textures/items/item_empquartz.png": "e60a2b3edd9dde82bd8803833582ba48096ed3940b764534ac8538cfb07b5ca5",
            "textures/items/item_empquartz.png.mcmeta": "355f5731ba1d5c05146cca1888fcf53d3759fc38c0c66a6c1d7212947becc11a",
            "textures/items/item_empquartz_layer1.png": "ff39194bb61e54a8873dc2299e5539a357f01d1fafef97019d2b53588fac10ff",
            "textures/items/item_empquartz_layer1.png.mcmeta": "8b457f2f67a78c79b176a6d58b926590c8f69abdbc9b960c4a90aa8d40f56d64",
            "models/item/scintillating_inlay.json": "a9f92d950688054d83c7b0faf53636b06553b47dffc5474cbd75639745da5a4c",
            "textures/items/item_component_scintillantinginlay.png": "0c7c7889b9104bf98d6ebcff025abfbe050aa313026731ffb3fa60c06dda76f7",
            "textures/items/item_component_scintillantinginlay.png.mcmeta": "c822d44d5983d3a221e71f1adbcbfa66a03cb12e777ae41a7f7317fc0ab87992",
        }.items():
            assert hashlib.sha256(jar.read("assets/arcanearchives/" + asset)).hexdigest() == digest, (node, asset)
        assert hashlib.sha256(jar.read(dust)).hexdigest() == "9b616bd6c48f592a08aa8c034f1d2b3102c05ef830d0e2f100d504bb57915835", node
        assert load(dust + ".mcmeta") == {"animation": {"interpolate": True, "frametime": 20, "frames": list(range(8))}}, node
        assert not any(name.startswith((f"data/arcanearchives/{wrong[0]}/", f"data/arcanearchives/{wrong[1]}/", f"data/minecraft/tags/{wrong[2]}/")) for name in jar.namelist()), node
    print(f"PASS {node}: quartz/table recipes, ingredient tags, original item assets, animations, atlas entries and translations")
