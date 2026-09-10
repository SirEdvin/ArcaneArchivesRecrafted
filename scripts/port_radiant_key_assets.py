"""Recover the pinned original Radiant Key model and artwork."""
import argparse
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PIN = "bb99accf48ed583e29b0efae56e28c963407b8df"
PREFIX = "src/main/resources/assets/arcanearchives/"


def generate(upstream):
    for path in ("models/item/radiant_key.json", "textures/items/radiant_key.png", "textures/items/radiant_key.png.mcmeta"):
        data = subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":" + PREFIX + path])
        target = ROOT / PREFIX / path
        if target.exists() and target.read_bytes() != data:
            raise ValueError("Refusing to replace differing resource: " + str(target))
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    print("Recovered Radiant Key assets from " + PIN)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    generate(parser.parse_args().upstream)
