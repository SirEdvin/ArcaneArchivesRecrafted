"""Restore the registered Wonky Resonator's pinned OBJ artwork; no new recipe or explosion."""
import argparse
import json
from pathlib import Path
import subprocess
from port_gem_cutter_assets import rotation_xyz

ROOT = Path(__file__).resolve().parents[1]
PIN = "80944ce45c6559243d8928cc4b305bf379388652"
PREFIX = "src/main/resources/assets/arcanearchives/"


def generate(upstream):
    def source(path):
        return subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":" + PREFIX + path])
    legacy = json.loads(source("blockstates/wonky_resonator.json"))
    display = {context + "_righthand" if context in ("firstperson", "thirdperson") else context: {"rotation": rotation_xyz(transform["rotation"]),
                         "translation": [v * 16 for v in transform["translation"]],
                         "scale": [transform["scale"]] * 3}
               for context, transform in legacy["variants"]["inventory"][0]["transform"].items()}
    # Legacy Forge aliases address only the right hand; modern absent left hands inherit it.
    display = {"firstperson_lefthand": {}, "thirdperson_lefthand": {}, **display}
    files = {"models/block/makeshift_resonator.obj": source("models/block/makeshift_resonator.obj"),
             "models/block/makeshift_resonator.mtl": source("models/block/makeshift_resonator.mtl").replace(b"minecraft:blocks/", b"minecraft:block/"),
             "textures/blocks/radiant_resonator.png": source("textures/blocks/radiant_resonator.png")}
    models = {
        "models/block/wonky_resonator.json": {
            "loader": "${obj_loader}:obj", "model": "arcanearchives:models/block/makeshift_resonator.obj",
            "flip_v": True, "automatic_culling": False, "shade_quads": True,
            "render_type": "minecraft:cutout", "textures": {"particle": "arcanearchives:blocks/radiant_resonator"},
            "display": display},
        "models/item/wonky_resonator.json": {"parent": "arcanearchives:block/wonky_resonator"},
        "blockstates/wonky_resonator.json": {"variants": {"": {"model": "arcanearchives:block/wonky_resonator"}}},
    }
    files.update({path: (json.dumps(data, indent=2) + "\n").encode() for path, data in models.items()})
    for path, data in files.items():
        target = ROOT / PREFIX / path
        if target.exists() and target.read_bytes() != data:
            raise ValueError("Refusing to replace differing resource: " + str(target))
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    print("Ported Wonky Resonator assets from " + PIN)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    generate(parser.parse_args().upstream)
