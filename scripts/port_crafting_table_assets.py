"""Recover the pinned Radiant Crafting Table geometry and GUI without substitute art."""
import argparse
import json
from pathlib import Path
import subprocess
from port_gem_cutter_assets import rotation_xyz

ROOT = Path(__file__).resolve().parents[1]
PIN = "bb99accf48ed583e29b0efae56e28c963407b8df"
PREFIX = "src/main/resources/assets/arcanearchives/"


def generate(upstream):
    def source(path):
        return subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":" + PREFIX + path])
    legacy = json.loads(source("blockstates/radiant_crafting_table.json"))
    display = {context: {"rotation": rotation_xyz(transform["rotation"]),
                         "translation": [v * 16 for v in transform["translation"]],
                         "scale": [transform["scale"]] * 3}
               for context, transform in legacy["variants"]["inventory"][0]["transform"].items()}
    files = {path: source(path) for path in (
        "models/block/radiant_crafting_table.obj", "models/block/radiant_crafting_table.mtl",
        "textures/blocks/block_arcanearchives_master.png", "textures/blocks/block_radiantchest_quartz.png",
        "textures/blocks/block_radiantcraftingtable.png", "textures/gui/radiantcraftingtable.png",
        "textures/gui/simple/radiantcraftingtable.png")}
    models = {
        "models/block/radiant_crafting_table.json": {
            "loader": "${obj_loader}:obj", "model": "arcanearchives:models/block/radiant_crafting_table.obj",
            "flip_v": True, "automatic_culling": False, "shade_quads": True,
            "render_type": "minecraft:cutout", "textures": {"particle": "arcanearchives:blocks/block_arcanearchives_master"},
            "display": display},
        "models/item/radiant_crafting_table.json": {"parent": "arcanearchives:block/radiant_crafting_table"},
        "blockstates/radiant_crafting_table.json": {"variants": {"": {"model": "arcanearchives:block/radiant_crafting_table"}}}}
    files.update({path: (json.dumps(data, indent=2) + "\n").encode() for path, data in models.items()})
    for path, data in files.items():
        target = ROOT / PREFIX / path
        if target.exists() and target.read_bytes() != data:
            raise ValueError("Refusing to replace differing resource: " + str(target))
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    print("Recovered Radiant Crafting Table assets from " + PIN)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    generate(parser.parse_args().upstream)
