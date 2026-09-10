"""Restore the pinned Celestial Lotus Engine artwork; no invented engine or recipe."""
import argparse
import json
from pathlib import Path
import subprocess
from port_gem_cutter_assets import rotation_xyz

ROOT = Path(__file__).resolve().parents[1]
PIN = "80944ce45c6559243d8928cc4b305bf379388652"
NAME = "celestial_lotus_engine"
PREFIX = "assets/arcanearchives/"
TEXTURES = ("block_arcanearchives_master", "lotuscircle_simple", "lotuscircle", "lotuspetal")


def generate(upstream):
    def source(path):
        return subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":src/main/resources/" + PREFIX + path])
    legacy = json.loads(source("blockstates/" + NAME + ".json"))
    display = {"firstperson_lefthand": {}, "thirdperson_lefthand": {}}
    for context, transform in legacy["variants"]["inventory"][0]["transform"].items():
        display[context + "_righthand" if context in ("firstperson", "thirdperson") else context] = {
            "rotation": rotation_xyz(transform["rotation"]),
            "translation": [v * 16 for v in transform["translation"]], "scale": [transform["scale"]] * 3}
    files = {PREFIX + "models/block/" + NAME + "." + ext: source("models/block/" + NAME + "." + ext)
             for ext in ("obj", "mtl")}
    tree = set(subprocess.check_output(["git", "-C", str(upstream), "ls-tree", "-r", "--name-only", PIN]).decode().splitlines())
    for texture in TEXTURES:
        for ext in (".png", ".png.mcmeta"):
            path = "textures/blocks/" + texture + ext
            if ext == ".png" or "src/main/resources/" + PREFIX + path in tree:
                files[PREFIX + path] = source(path)
    models = {
        PREFIX + "models/block/" + NAME + ".json": {
            "loader": "${obj_loader}:obj", "model": "arcanearchives:models/block/" + NAME + ".obj",
            "flip_v": legacy["defaults"]["custom"]["flip-v"], "automatic_culling": False,
            "shade_quads": True, "render_type": "minecraft:solid",
            "textures": {"particle": "arcanearchives:blocks/" + TEXTURES[0]}, "display": display},
        PREFIX + "models/item/" + NAME + ".json": {"parent": "arcanearchives:block/" + NAME},
        PREFIX + "blockstates/" + NAME + ".json": {"variants": {"": {"model": "arcanearchives:block/" + NAME}}},
        "data/arcanearchives/loot_table/blocks/" + NAME + ".json": {
            "type": "minecraft:block", "pools": [{"rolls": 1,
                "entries": [{"type": "minecraft:item", "name": "arcanearchives:" + NAME}],
                "conditions": [{"condition": "minecraft:survives_explosion"}]}]},
    }
    files.update({path: (json.dumps(value, indent=2) + "\n").encode() for path, value in models.items()})
    for path, data in files.items():
        target = ROOT / "src/main/resources" / path
        if target.exists() and target.read_bytes() != data:
            raise ValueError("Refusing to replace differing resource: " + str(target))
    for path, data in files.items():
        target = ROOT / "src/main/resources" / path
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    print(f"Verified/restored {len(files)} Celestial Lotus Engine resources from {PIN}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    generate(parser.parse_args().upstream)
