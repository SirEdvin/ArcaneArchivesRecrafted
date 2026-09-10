"""Recover the original Devouring Charm animation and both GUI faces."""
import argparse
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PIN = "bb99accf48ed583e29b0efae56e28c963407b8df"
PREFIX = "src/main/resources/assets/arcanearchives/"


def generate(upstream):
    paths = ("models/item/devouring_charm.json", "textures/items/item_devouringcharm.png",
             "textures/items/item_devouringcharm.png.mcmeta", "textures/gui/devouring_charm.png",
             "textures/gui/simple/devouring_charm.png", "textures/gui/player_inv.png",
             "textures/gui/simple/player_inv.png")
    for path in paths:
        data = subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":" + PREFIX + path])
        target = ROOT / PREFIX / path
        if target.exists() and target.read_bytes() != data:
            raise ValueError("Refusing to replace differing resource: " + str(target))
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    print("Recovered Devouring Charm assets from " + PIN)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    generate(parser.parse_args().upstream)
