"""Recover pinned Munchstone art and migrate its default block-ID list."""
import argparse
import json
import re
import subprocess
from pathlib import Path
from port_parchtear_assets import generate, PIN, ROOT

RENAMES = {
    "log": [wood + suffix for wood in ("oak", "spruce", "birch", "jungle") for suffix in ("_log", "_wood")],
    "log2": [wood + suffix for wood in ("acacia", "dark_oak") for suffix in ("_log", "_wood")],
    "leaves": [wood + "_leaves" for wood in ("oak", "spruce", "birch", "jungle")],
    "leaves2": [wood + "_leaves" for wood in ("acacia", "dark_oak")],
    "sapling": [wood + "_sapling" for wood in ("oak", "spruce", "birch", "jungle", "acacia", "dark_oak")],
    "melon_block": ["melon"], "deadbush": ["dead_bush"],
    "double_plant": ["sunflower", "lilac", "tall_grass", "large_fern", "rose_bush", "peony"],
    "pumpkin": ["pumpkin", "carved_pumpkin"], "lit_pumpkin": ["jack_o_lantern"],
    "pumpkin_stem": ["pumpkin_stem", "attached_pumpkin_stem"],
    "melon_stem": ["melon_stem", "attached_melon_stem"],
    "red_flower": ["poppy", "blue_orchid", "allium", "azure_bluet", "red_tulip", "orange_tulip", "white_tulip", "pink_tulip", "oxeye_daisy"],
    "yellow_flower": ["dandelion"], "reeds": ["sugar_cane"],
    "tallgrass": ["grass", "short_grass", "fern"], "waterlily": ["lily_pad"],
}

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    upstream = parser.parse_args().upstream
    generate(upstream, ("black", "dun"), "oval")
    source = subprocess.check_output(["git", "-C", str(upstream), "show", PIN + ":src/main/java/com/aranaira/arcanearchives/items/gems/oval/MunchstoneItem.java"], text=True)
    entries = re.findall(r'"([^"]+)"', source.split("DEFAULT_ENTRIES = {", 1)[1].split("};", 1)[0])
    result = []
    for entry in entries:
        block, feed = entry.split(",")
        namespace, name = block.split(":")
        names = RENAMES.get(name, [name]) if namespace == "minecraft" else [name]
        result.extend(namespace + ":" + name + ", " + feed.strip() for name in names)
    target = ROOT / "src/main/resources/data/arcanearchives/munchstone_defaults.json"
    target.write_text(json.dumps(result, indent=2) + "\n")
