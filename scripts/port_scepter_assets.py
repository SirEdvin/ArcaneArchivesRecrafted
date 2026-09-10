"""Recover the two storage scepters' original release art and item transforms."""
import argparse
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PIN = "bb99accf48ed583e29b0efae56e28c963407b8df"


def generate(upstream):
    base = "src/main/resources/assets/arcanearchives/"
    pending = {}
    for name in ("scepter_revelation", "scepter_manipulation"):
        model = json.loads(subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":" + base + "models/item/" + name + ".json"]))
        texture = model["textures"]["layer0"].split(":", 1)[1]
        pending[ROOT / (base + "textures/item/" + name + ".png")] = subprocess.check_output(
            ["git", "-C", str(upstream), "show", PIN + ":" + base + "textures/" + texture + ".png"])
        model["textures"]["layer0"] = "arcanearchives:item/" + name
        pending[ROOT / (base + "models/item/" + name + ".json")] = (json.dumps(model, indent=2) + "\n").encode()
    for target, data in pending.items():
        if target.exists() and target.read_bytes() != data:
            raise ValueError("Refusing to replace differing resource: " + str(target))
    for target, data in pending.items():
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    print("Recovered storage scepter art from " + PIN)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    generate(parser.parse_args().upstream)
