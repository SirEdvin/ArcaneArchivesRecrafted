"""Recover original release upgrade GUI textures without replacing differing files."""
import argparse
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PIN = "bb99accf48ed583e29b0efae56e28c963407b8df"


def generate(upstream):
    prefix = "src/main/resources/assets/arcanearchives/textures/gui/"
    for name in ("radiant_upgrades.png", "player_inv.png"):
        path = prefix + name
        data = subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":" + path])
        target = ROOT / path
        if target.exists() and target.read_bytes() != data:
            raise ValueError("Refusing to replace differing resource: " + str(target))
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    print("Recovered upgrade GUI resources from " + PIN)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    generate(parser.parse_args().upstream)
