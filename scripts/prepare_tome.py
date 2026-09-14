#!/usr/bin/env python3
"""Prepare complete pinned Tome XML for conversion; never enable a partial book."""

import argparse
from collections import Counter
from copy import deepcopy
from pathlib import Path
import subprocess
import xml.etree.ElementTree as ET

BASELINE = "80944ce45c6559243d8928cc4b305bf379388652"
SOURCE = "src/main/resources/assets/arcanearchives/xml/tome.xml"
# Only the literal mappings approved in docs/behavior-changes/0148 and 0150.
REPAIRS = {
    "Concepts:Immanence": "Concepts:Networks",
    "RadiantResonator": "Blocks:RadiantResonator",
    "Items:RadiantCraftingTable": "Blocks:RadiantCraftingTable",
    "Blocks:SliverOfLight": "Items:SliverOfLight",
    "MonitoringCrystal": "Blocks:MonitoringCrystal",
    "Parchtear": "Gems:Parchtear",
    "Blocks:RawQuartz": "Items:RawQuartz",
    "Blocks:ShapedQuartz": "Items:ShapedQuartz",
}
EXPECTED = Counter({key: 2 if key in ("RadiantResonator", "Blocks:SliverOfLight") else 1 for key in REPAIRS})


def prepare(root):
    """Return a detached document; preserve everything except approved targets."""
    result = deepcopy(root)
    chapters = {}
    for chapter in result.findall("chapter"):
        name = chapter.get("id")
        if not name or name in chapters:
            raise ValueError(f"Missing or duplicate chapter ID: {name!r}")
        sections = [s.get("id") for s in chapter.findall("section") if s.get("id")]
        if len(sections) != len(set(sections)):
            raise ValueError(f"Duplicate section ID in {name!r}")
        chapters[name] = set(sections)

    def valid(target):
        parts = target.split(":")
        return parts[0] in chapters and (len(parts) == 1 or len(parts) == 2 and parts[1] in chapters[parts[0]])

    counts = Counter()
    checked = 0

    def resolve(literal):
        nonlocal checked
        checked += 1
        target = REPAIRS.get(literal, literal)
        if not valid(target):
            raise ValueError(f"Unresolved Tome target: {literal!r} -> {target!r}")
        if literal in REPAIRS:
            counts[literal] += 1
        return target

    for link in result.iter("link"):
        if "ref" in link.attrib:
            link.set("ref", resolve(link.get("ref")))
    for stack in result.findall("stack-links/stack"):
        literal = stack.text or ""
        # Do not strip or normalize source tokens to make a lookup succeed.
        stack.text = resolve(literal)
    return result, counts, checked


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    parser.add_argument("output", type=Path, help="Intermediate XML, not production Patchouli resources")
    args = parser.parse_args()
    blob = subprocess.run(["git", "-C", str(args.upstream), "show", f"{BASELINE}:{SOURCE}"],
                          check=True, capture_output=True).stdout
    root = ET.fromstring(blob, parser=ET.XMLParser(target=ET.TreeBuilder(insert_comments=True)))
    result, counts, checked = prepare(root)
    inventory = {tag: len(list(root.iter(tag))) for tag in ("chapter", "section", "recipe", "link", "image")}
    if inventory != {"chapter": 7, "section": 98, "recipe": 44, "link": 256, "image": 71}:
        raise ValueError(f"Incomplete pinned Tome inventory: {inventory}")
    if counts != EXPECTED or checked != 317:
        raise ValueError(f"Pinned reference inventory drift: checked={checked}, repairs={dict(counts)}")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    ET.ElementTree(result).write(args.output, encoding="utf-8", xml_declaration=True)
    print(f"Prepared complete Tome: {checked} targets validated, {sum(counts.values())} approved repairs; {args.output}")


if __name__ == "__main__":
    main()
