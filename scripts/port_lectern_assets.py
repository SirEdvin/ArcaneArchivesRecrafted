"""Restore the pinned Manifest Lectern OBJ and native model metadata."""
import argparse
import json
from pathlib import Path
import subprocess
from port_gem_cutter_assets import rotation_xyz

ROOT = Path(__file__).resolve().parents[1]
PIN = "bb99accf48ed583e29b0efae56e28c963407b8df"
PREFIX = "src/main/resources/assets/arcanearchives/"


def generate(upstream, check=False):
    def source(path):
        return subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":" + PREFIX + path])
    legacy = json.loads(source("blockstates/lectern_manifest.json"))
    display = {context: {"rotation": rotation_xyz(transform["rotation"]),
                         "translation": [v * 16 for v in transform["translation"]],
                         "scale": [transform["scale"]] * 3}
               for context, transform in legacy["variants"]["inventory"][0]["transform"].items()}
    files = {path: source(path) for path in (
        "models/block/lectern_manifest.obj", "models/block/lectern_manifest.mtl",
        "textures/blocks/block_arcanearchives_master.png", "textures/items/item_manifest.png",
        "textures/blocks/block_lecterngrate.png", "textures/transparent.png")}
    models = {
        "models/block/lectern_manifest.json": {
            "loader": "${obj_loader}:obj", "model": "arcanearchives:models/block/lectern_manifest.obj",
            "flip_v": True, "automatic_culling": False, "shade_quads": True,
            "render_type": "minecraft:cutout", "textures": {"particle": "arcanearchives:blocks/block_arcanearchives_master"},
            "display": display},
        "models/block/lectern_manifest_accessor.json": {
            "textures": {"particle": "arcanearchives:transparent"}, "elements": []},
        "models/item/lectern_manifest.json": {"parent": "arcanearchives:block/lectern_manifest"},
        "blockstates/lectern_manifest.json": {"variants": {
            f"accessor={accessor},facing={facing}": {
                "model": "arcanearchives:block/lectern_manifest" + ("_accessor" if accessor == "true" else ""), "y": rotation}
            for accessor in ("false", "true")
            for facing, rotation in [("west", 0), ("north", 90), ("east", 180), ("south", 270), ("up", 0), ("down", 0)]}}}
    files.update({path: (json.dumps(data, indent=2) + "\n").encode() for path, data in models.items()})
    for path, data in files.items():
        target = ROOT / PREFIX / path
        if target.exists() and target.read_bytes() != data:
            raise ValueError("Refusing to replace differing resource: " + str(target))
        if check:
            if not target.exists():
                raise ValueError("Missing resource: " + str(target))
        elif not target.exists():
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
    print(f"{'Verified' if check else 'Recovered'} {len(files)} Manifest Lectern resources from {PIN}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    generate(args.upstream, args.check)
