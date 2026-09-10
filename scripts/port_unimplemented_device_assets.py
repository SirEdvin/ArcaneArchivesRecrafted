"""Restore five registered, nonfunctional devices from the pinned development baseline."""
import argparse
import json
from pathlib import Path
import subprocess
from port_gem_cutter_assets import rotation_xyz

ROOT = Path(__file__).resolve().parents[1]
PIN = "80944ce45c6559243d8928cc4b305bf379388652"
PREFIX = "assets/arcanearchives/"
OBJ_NAMES = ("verdant_censer", "echoing_conformance_chamber", "echoing_reverberation_chamber")
NAMES = OBJ_NAMES + ("spellbook_library", "immanent_incubator")


def generate(upstream):
    def source(path):
        return subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":src/main/resources/" + PREFIX + path])

    tree = set(subprocess.check_output(["git", "-C", str(upstream), "ls-tree", "-r", "--name-only", PIN,
                                       "--", "src/main/resources/" + PREFIX]).decode().splitlines())
    files = {}
    textures = {"blocks/placeholder"}
    for name in NAMES:
        legacy = json.loads(source("blockstates/" + name + ".json"))
        if name in OBJ_NAMES:
            assert legacy["defaults"]["model"] == "arcanearchives:" + name + ".obj"
            for extension in ("obj", "mtl"):
                path = "models/block/" + name + "." + extension
                files[PREFIX + path] = source(path)
            materials = source("models/block/" + name + ".mtl").decode()
            material_textures = [line.split()[1] for line in materials.splitlines() if line.startswith("map_Kd ")]
            assert material_textures and all(value.startswith("arcanearchives:") for value in material_textures)
            textures.update(value.split(":", 1)[1] for value in material_textures)
            display = {context: {"rotation": rotation_xyz(transform["rotation"]),
                                 "translation": [v * 16 for v in transform["translation"]],
                                 "scale": [transform["scale"]] * 3}
                       for context, transform in legacy["variants"]["inventory"][0]["transform"].items()}
            model = {"loader": "${obj_loader}:obj", "model": "arcanearchives:models/block/" + name + ".obj",
                     "flip_v": legacy["defaults"]["custom"]["flip-v"], "automatic_culling": False,
                     "shade_quads": True, "render_type": "minecraft:cutout",
                     "textures": {"particle": material_textures[0]}, "display": display}
        else:
            assert legacy["variants"]["normal"]["model"] == "cube_all"
            model = {"parent": "minecraft:block/cube_all", "textures": legacy["defaults"]["textures"]}
        models = {
            PREFIX + "models/block/" + name + ".json": model,
            PREFIX + "models/item/" + name + ".json": {"parent": "arcanearchives:block/" + name},
            PREFIX + "blockstates/" + name + ".json": {"variants": {"": {"model": "arcanearchives:block/" + name}}},
            "data/arcanearchives/loot_table/blocks/" + name + ".json": {
                "type": "minecraft:block", "pools": [{"rolls": 1,
                    "entries": [{"type": "minecraft:item", "name": "arcanearchives:" + name}],
                    "conditions": [{"condition": "minecraft:survives_explosion"}]}]},
        }
        files.update({path: (json.dumps(data, indent=2) + "\n").encode() for path, data in models.items()})
    for texture in sorted(textures):
        for suffix in (".png", ".png.mcmeta"):
            path = "textures/" + texture + suffix
            if suffix == ".png" or "src/main/resources/" + PREFIX + path in tree:
                files[PREFIX + path] = source(path)
    # Preflight the whole batch before writing: never overwrite unrelated migrated resources.
    for path, data in files.items():
        target = ROOT / "src/main/resources" / path
        if target.exists() and target.read_bytes() != data:
            raise ValueError("Refusing to replace differing resource: " + str(target))
    for path, data in files.items():
        target = ROOT / "src/main/resources" / path
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    print(f"Verified/restored {len(NAMES)} devices and {len(files)} resources from {PIN}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    generate(parser.parse_args().upstream)
