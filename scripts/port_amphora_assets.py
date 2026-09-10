"""Recover pinned Amphora art; native state models retain original base textures."""
import argparse
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PIN = "bb99accf48ed583e29b0efae56e28c963407b8df"


def generate(upstream):
    base = "src/main/resources/assets/arcanearchives/"
    def read(path):
        return subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":" + base + path])
    unlinked = json.loads(read("models/item/radiant_amphora_unlinked.json"))
    pending = {}
    for suffix in ("", "_tilted", "_unlinked", "_fluidmask_empty", "_fluidmask_fill"):
        pending[ROOT / (base + "textures/item/radiant_amphora" + suffix + ".png")] = read("textures/items/item_radiantamphora" + suffix + ".png")
    unlinked["textures"]["layer0"] = "arcanearchives:item/radiant_amphora_unlinked"
    models = {"radiant_amphora_unlinked": unlinked}
    for mode in ("fill", "empty"):
        legacy = json.loads(read("blockstates/radiant_amphora_" + mode + ".json"))["variants"]["inventory"]
        texture = legacy["textures"]["base"].replace("items/item_radiantamphora", "item/radiant_amphora")
        models["radiant_amphora_" + mode] = {"parent": "minecraft:item/generated", "textures": {"layer0": texture}}
    models["radiant_amphora"] = {
        "parent": "arcanearchives:item/radiant_amphora_unlinked",
        "overrides": [
            {"predicate": {"arcanearchives:amphora_state": 1}, "model": "arcanearchives:item/radiant_amphora_fill"},
            {"predicate": {"arcanearchives:amphora_state": 2}, "model": "arcanearchives:item/radiant_amphora_empty"},
        ],
    }
    for name, model in models.items():
        pending[ROOT / (base + "models/item/" + name + ".json")] = (json.dumps(model, indent=2) + "\n").encode()
    for path, data in pending.items():
        if path.exists() and path.read_bytes() != data:
            raise ValueError("Refusing to replace differing resource: " + str(path))
    for path, data in pending.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(data)
    print("Recovered Amphora base art and fluid masks from " + PIN + "; dynamic fluid-mask rendering remains pending.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    generate(parser.parse_args().upstream)
