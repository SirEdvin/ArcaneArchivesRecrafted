"""Reproduce the pinned monitoring_crystal OBJ/materials and converted model metadata."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
from port_gem_cutter_assets import rotation_xyz

ROOT = Path(__file__).resolve().parents[1]
PIN = "bb99accf48ed583e29b0efae56e28c963407b8df"
PREFIX = "src/main/resources/assets/arcanearchives/"
# Native model-state rotations use negative angles, unlike legacy Forge transforms.
ROTATIONS = {"up": (90, 0), "down": (270, 0), "south": (0, 0),
             "east": (0, 270), "north": (0, 180), "west": (0, 90)}


def generated(upstream):
    def source(path):
        return subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":" + PREFIX + path])
    legacy = json.loads(source("blockstates/monitoring_crystal.json"))
    display = {}
    for context, transform in legacy["variants"]["inventory"][0]["transform"].items():
        display[context] = {
            "rotation": rotation_xyz(transform["rotation"]),
            "translation": [value * 16 for value in transform["translation"]],
            "scale": [transform["scale"]] * 3,
        }
    # The shared atlas sheet already belongs to the Gem Cutter port: never replace it.
    texture = PREFIX + "textures/blocks/block_arcanearchives_master.png"
    if (ROOT / texture).read_bytes() != source("textures/blocks/block_arcanearchives_master.png"):
        raise ValueError("Shared texture differs from pinned monitoring_crystal material")
    files = {PREFIX + "models/block/monitoring_crystal." + ext: source("models/block/monitoring_crystal." + ext)
             for ext in ("obj", "mtl")}
    models = {
        "models/block/monitoring_crystal.json": {
            "loader": "${obj_loader}:obj", "model": "arcanearchives:models/block/monitoring_crystal.obj",
            "flip_v": True, "automatic_culling": False, "shade_quads": True,
            "render_type": "minecraft:cutout", "textures": {"particle": "arcanearchives:blocks/block_arcanearchives_master"},
            "display": display,
        },
        "models/item/monitoring_crystal.json": {"parent": "arcanearchives:block/monitoring_crystal"},
        "blockstates/monitoring_crystal.json": {"variants": {
            "facing=" + facing: {"model": "arcanearchives:block/monitoring_crystal", "x": x, "y": y}
            for facing, (x, y) in ROTATIONS.items()
        }},
    }
    files.update({PREFIX + path: (json.dumps(data, indent=2) + "\n").encode() for path, data in models.items()})
    return files


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    files = generated(args.upstream)
    for path, data in files.items():
        target = ROOT / path
        if args.check:
            if not target.exists() or target.read_bytes() != data:
                raise ValueError("Resource differs from pinned conversion: " + path)
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
    print(json.dumps({path: hashlib.sha256(data).hexdigest() for path, data in files.items()}, indent=2))


if __name__ == "__main__":
    main()
