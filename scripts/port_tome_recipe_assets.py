"""Restore the pinned Tome Gem Cutter recipe artwork and API-only Patchouli templates."""
import argparse
import json
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]
PIN = "80944ce45c6559243d8928cc4b305bf379388652"
TEXTURE = "assets/arcanearchives/textures/gui/guidebook/recipe_gct.png"
TEMPLATES = "assets/arcanearchives/patchouli_books/tome_arcana/en_us/templates/"
COMPONENT = "com.aranaira.arcanearchives.client.GemCutterBookComponent"


def generated(upstream):
    files = {TEXTURE: subprocess.check_output([
        "git", "-C", str(upstream), "show", PIN + ":src/main/resources/" + TEXTURE], timeout=30)}
    for selector in ("output", "recipe"):
        template = {"components": [{"type": "patchouli:custom", "class": COMPONENT,
                    "x": 4, "y": 20, selector: "#" + selector}]}
        files[TEMPLATES + "gem_cutting_" + selector + ".json"] = (json.dumps(template, indent=2) + "\n").encode()
    return files


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    files = generated(args.upstream)
    for path, data in files.items():
        target = ROOT / "src/main/resources" / path
        if target.exists() and target.read_bytes() != data:
            raise ValueError("Refusing to replace differing resource: " + str(target))
        if args.check and not target.is_file():
            raise ValueError("Missing resource: " + str(target))
    if not args.check:
        for path, data in files.items():
            target = ROOT / "src/main/resources" / path
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
    print(f"Verified/restored {len(files)} Tome recipe resources from {PIN}; no book registered")


if __name__ == "__main__":
    main()
