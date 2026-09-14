"""Recover pinned Brazier meshes, materials, artwork and absorption sounds; not model registration."""
import argparse
import json
from pathlib import Path
import subprocess
from port_gem_cutter_assets import rotation_xyz

ROOT = Path(__file__).resolve().parents[1]
PIN = "bb99accf48ed583e29b0efae56e28c963407b8df"
PREFIX = "src/main/resources/assets/arcanearchives/"


def generate(upstream, check=False):
    paths = [
        "models/block/brazier_of_hoarding.obj", "models/block/brazier_of_hoarding_fire.obj",
        "models/block/brazier_of_hoarding.mtl", "textures/blocks/brazier_charcoal.png",
        "textures/blocks/block_arcanearchives_master.png", "textures/gui/brazier_hoarding.png",
        *[f"sounds/brazier_absorb{i}.ogg" for i in (1, 2, 3)],
    ]
    files = {path: subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":" + PREFIX + path])
             for path in paths}
    material = "models/block/brazier_of_hoarding.mtl"
    legacy = json.loads(subprocess.check_output([
        "git", "-C", str(upstream), "show", PIN + ":" + PREFIX + "blockstates/brazier_of_hoarding.json"]))
    display = {context: {"rotation": rotation_xyz(transform["rotation"]),
                         "translation": [value * 16 for value in transform["translation"]],
                         "scale": [transform["scale"]] * 3}
               for context, transform in legacy["variants"]["inventory"][0]["transform"].items()}
    models = {
        "blockstates/brazier_of_hoarding.json": {
            "variants": {"": {"model": "arcanearchives:block/brazier_of_hoarding"}}},
        "models/block/brazier_of_hoarding.json": {
            "loader": "${obj_loader}:obj", "model": "arcanearchives:models/block/brazier_of_hoarding.obj",
            "flip_v": True, "automatic_culling": False, "shade_quads": True,
            "render_type": "minecraft:cutout",
            "textures": {"particle": "arcanearchives:blocks/block_arcanearchives_master"},
            "display": display},
        "models/item/brazier_of_hoarding.json": {"parent": "arcanearchives:block/brazier_of_hoarding"},
        "models/block/brazier_of_hoarding_fire.json": {
            "loader": "${obj_loader}:obj", "model": "arcanearchives:models/block/brazier_of_hoarding_fire.obj",
            "flip_v": True, "automatic_culling": False, "ambientocclusion": False,
            "render_type": "minecraft:cutout", "textures": {"particle": "minecraft:block/fire_0"}},
    }
    files.update({path: (json.dumps(data, indent=2) + "\n").encode() for path, data in models.items()})
    files[material] = files[material].replace(b"minecraft:blocks/fire_layer_0", b"minecraft:block/fire_0") \
        .replace(b"minecraft:blocks/fire_layer_1", b"minecraft:block/fire_1")
    materials = {line.split()[1] for line in files[material].decode().splitlines() if line.startswith("newmtl ")}
    for path, data in files.items():
        if path.endswith(".obj"):
            lines = data.decode().splitlines()
            used = {line.split()[1] for line in lines if line.startswith("usemtl ")}
            if not used <= materials or "mtllib brazier_of_hoarding.mtl" not in lines:
                raise ValueError("Unresolved mesh material: " + path)
        target = ROOT / PREFIX / path
        if target.exists() and target.read_bytes() != data:
            raise ValueError("Refusing to replace differing resource: " + str(target))
        if check:
            if not target.exists():
                raise ValueError("Missing resource: " + str(target))
    # Validate the entire batch before creating any resources.
    if not check:
        for path, data in files.items():
            target = ROOT / PREFIX / path
            if not target.exists():
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(data)
    print(f"{'Verified' if check else 'Recovered'} {len(files)} Brazier resources from {PIN}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    generate(args.upstream, args.check)
