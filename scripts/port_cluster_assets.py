"""Reproduce raw cluster geometry and legacy inventory transforms from pinned Git."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
from port_lantern_assets import ROOT, PIN, PREFIX, ROTATIONS
from port_gem_cutter_assets import rotation_xyz


def generated(upstream):
    def source(path):
        return subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":" + PREFIX + path])
    legacy = json.loads(source("blockstates/raw_quartz_cluster.json"))
    assert legacy["defaults"]["model"] == "arcanearchives:raw_quartz.obj"
    display = {context: {
        "rotation": rotation_xyz(transform["rotation"]),
        "translation": [value * 16 for value in transform["translation"]],
        "scale": [transform["scale"]] * 3,
    } for context, transform in legacy["variants"]["inventory"][0]["transform"].items()}
    texture = "textures/blocks/block_storage_rawquartz.png"
    assert (ROOT / (PREFIX + texture)).read_bytes() == source(texture)
    files = {PREFIX + "models/block/raw_quartz." + ext: source("models/block/raw_quartz." + ext) for ext in ("obj", "mtl")}
    models = {
        "models/block/raw_quartz_cluster.json": {
            "loader": "${obj_loader}:obj", "model": "arcanearchives:models/block/raw_quartz.obj",
            "flip_v": True, "automatic_culling": False, "shade_quads": True,
            "render_type": "minecraft:cutout", "textures": {"particle": "arcanearchives:blocks/block_storage_rawquartz"},
            "display": display,
        },
        "models/item/raw_quartz_cluster.json": {"parent": "arcanearchives:block/raw_quartz_cluster"},
        "blockstates/raw_quartz_cluster.json": {"variants": {
            "facing=" + face: {"model": "arcanearchives:block/raw_quartz_cluster", "x": x, "y": y}
            for face, (x, y) in ROTATIONS.items()
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
            assert target.read_bytes() == data, path
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
    print(json.dumps({path: hashlib.sha256(data).hexdigest() for path, data in files.items()}, indent=2))


if __name__ == "__main__":
    main()
