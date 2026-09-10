"""Reproduce Sliver of Light resources from the pinned release Git tree."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
from port_lantern_assets import ROOT, PIN, PREFIX, ROTATIONS


def generated(upstream):
    def source(path):
        return subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":" + PREFIX + path])
    original = json.loads(source("blockstates/quartz_sliver.json"))
    assert original["defaults"]["model"] == "arcanearchives:quartz_sliver.obj"
    assert set(original["variants"]["facing"]) == set(ROTATIONS)
    texture = "textures/blocks/block_storage_rawquartz.png"
    assert (ROOT / (PREFIX + texture)).read_bytes() == source(texture)
    files = {PREFIX + path: source(path) for path in (
        "models/block/quartz_sliver.obj", "models/block/quartz_sliver.mtl",
        "models/item/quartz_sliver.json", "textures/items/item_quartzsliver.png",
    )}
    models = {
        "models/block/quartz_sliver.json": {
            "loader": "${obj_loader}:obj", "model": "arcanearchives:models/block/quartz_sliver.obj",
            "flip_v": True, "automatic_culling": False, "shade_quads": True,
            "render_type": "minecraft:cutout", "textures": {"particle": "arcanearchives:blocks/block_storage_rawquartz"},
        },
        "blockstates/quartz_sliver.json": {"variants": {
            "facing=" + face: {"model": "arcanearchives:block/quartz_sliver", "x": x, "y": y}
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
