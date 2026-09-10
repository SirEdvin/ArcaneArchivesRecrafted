"""Recover Parchtear's pinned gem models, textures and animation/accessibility companions."""
import argparse
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PIN = "bb99accf48ed583e29b0efae56e28c963407b8df"
PREFIX = "src/main/resources/assets/arcanearchives/"


def generate(upstream, colours=("black", "dun"), cut="pendeloque"):
    tree = set(subprocess.check_output(["git", "-C", str(upstream), "ls-tree", "-r", "--name-only", PIN], text=True).splitlines())
    copied = set()

    def recover(path):
        if path in copied:
            return
        data = subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":" + PREFIX + path])
        target = ROOT / PREFIX / path
        if target.exists() and target.read_bytes() != data:
            raise ValueError("Refusing to replace differing resource: " + str(target))
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
        copied.add(path)
        if path.startswith("models/"):
            model = json.loads(data)
            parent = model.get("parent", "")
            if parent.startswith("arcanearchives:"):
                recover("models/" + parent.split(":", 1)[1] + ".json")
            for value in model.get("textures", {}).values():
                if value.startswith("arcanearchives:"):
                    texture = "textures/" + value.split(":", 1)[1] + ".png"
                    recover(texture)
                    if PREFIX + texture + ".mcmeta" in tree:
                        recover(texture + ".mcmeta")
            for override in model.get("overrides", []):
                target = override["model"]
                if target.startswith("arcanearchives:"):
                    recover("models/" + target.split(":", 1)[1] + ".json")

    for colour in colours:
        recover("models/item/gems/" + cut + "/" + colour + ".json")
    print(json.dumps(sorted(copied), indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    generate(parser.parse_args().upstream)
