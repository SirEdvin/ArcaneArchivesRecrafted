"""Port original Resonator geometry, display transforms, translations and sound assets."""
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
    legacy = json.loads(source("blockstates/radiant_resonator.json"))
    display = {context: {"rotation": rotation_xyz(transform["rotation"]),
                         "translation": [v * 16 for v in transform["translation"]],
                         "scale": [transform["scale"]] * 3}
               for context, transform in legacy["variants"]["inventory"][0]["transform"].items()}
    files = {"models/block/radiant_resonator.obj": source("models/block/radiant_resonator.obj"),
             "models/block/radiant_resonator.mtl": source("models/block/radiant_resonator.mtl").replace(b"minecraft:blocks/", b"minecraft:block/")}
    models = {
        "models/block/radiant_resonator.json": {
            "loader": "${obj_loader}:obj", "model": "arcanearchives:models/block/radiant_resonator.obj",
            "flip_v": True, "automatic_culling": False, "shade_quads": True,
            "render_type": "minecraft:cutout", "textures": {"particle": "arcanearchives:blocks/block_arcanearchives_master"},
            "display": display},
        "models/item/radiant_resonator.json": {"parent": "arcanearchives:block/radiant_resonator"},
        "blockstates/radiant_resonator.json": {"variants": {"": {"model": "arcanearchives:block/radiant_resonator"}}},
    }
    sounds = json.loads(source("sounds.json"))
    models["sounds.json"] = {key: {"sounds": sounds[key]["sounds"]} for key in ("resonator.complete", "resonator.loop")}
    for event in models["sounds.json"].values():
        for sound in event["sounds"]:
            path = "sounds/" + sound.split(":", 1)[1] + ".ogg"
            files[path] = source(path)
    files.update({path: (json.dumps(data, indent=2) + "\n").encode() for path, data in models.items()})
    for path, data in files.items():
        target = ROOT / PREFIX / path
        if target.exists() and target.read_bytes() != data:
            raise ValueError("Refusing to replace differing resource: " + str(target))
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    print("Ported resonator assets from " + PIN)
    for language in ("en_us", "pt_BR"):
        text = source("lang/" + language + ".lang").decode()
        for line in text.splitlines():
            if "=" in line and ("resonator" in line.split("=", 1)[0] or "toomanyplaced" in line.split("=", 1)[0]):
                print(language, line)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    generate(parser.parse_args().upstream)
