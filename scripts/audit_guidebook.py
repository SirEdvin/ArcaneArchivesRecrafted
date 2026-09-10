#!/usr/bin/env python3
"""Inventory pinned upstream Guidebook XML; does not convert or enable content."""

import argparse
from collections import Counter
import hashlib
import io
import json
from pathlib import Path
import subprocess
import xml.etree.ElementTree as ET
import zipfile

BASELINE = "80944ce45c6559243d8928cc4b305bf379388652"
RESOURCE_ROOT = "src/main/resources/assets/arcanearchives/xml/"
GUIDEBOOK_JAR = "embed/Guidebook-1.12.2-2.9.1.s5.jar"
STANDARD_TEMPLATE = "assets/gbook_snapshot/xml/standard.xml"
OUTPUT = Path(__file__).resolve().parents[1] / "docs/migration/guidebook-baseline.json"


def inventory(documents):
    result = {
        "baseline": BASELINE,
        "scope": "Static XML inventory, including dormant/conditional content and the included standard library; not rendered pages or runtime parity. Source identifiers and parsed section/template content are preserved without correction. XML serialization is not byte-identical; source hashes cover original bytes.",
        "files": [],
    }
    for filename, blob in sorted(documents.items()):
        root = ET.fromstring(blob)
        counts = Counter(element.tag for element in root.iter())
        record = {
            "source": filename,
            "sha256": hashlib.sha256(blob).hexdigest(),
            "root_attributes": dict(root.attrib),
            "element_counts": dict(sorted(counts.items())),
            "chapters": [], "sections": [], "conditions": [], "condition_uses": [],
            "includes": [], "templates": [], "links": [], "stack_links": [],
            "recipes": [], "images": [], "stacks": [],
        }

        def walk(element, path, conditions):
            current_conditions = conditions + ([element.attrib["condition"]] if "condition" in element.attrib else [])
            entry = {"element": path, "attributes": dict(element.attrib), "condition_ancestry": current_conditions}
            if "condition" in element.attrib:
                record["condition_uses"].append(entry)
            if element.tag == "chapter":
                record["chapters"].append(entry)
            elif element.tag == "section":
                record["sections"].append({
                    **entry,
                    "titles": ["".join(title.itertext()) for title in element.iter("title")],
                    "element_counts": dict(sorted(Counter(child.tag for child in element.iter()).items())),
                    "xml": ET.tostring(element, encoding="unicode").strip(),
                })
            elif element.tag == "conditions":
                record["conditions"].extend({
                    "name": child.get("name"), "xml": ET.tostring(child, encoding="unicode").strip(),
                } for child in element)
            elif element.tag == "include":
                record["includes"].append(entry)
            elif element.tag == "template":
                record["templates"].append({
                    **entry, "xml": ET.tostring(element, encoding="unicode").strip(),
                })
            elif element.tag == "link":
                record["links"].append({**entry, "text": "".join(element.itertext())})
            elif element.tag == "stack-links":
                record["stack_links"].extend({
                    "attributes": dict(child.attrib), "target": child.text,
                } for child in element)
            elif element.tag == "recipe":
                record["recipes"].append({
                    **entry, "xml": ET.tostring(element, encoding="unicode").strip(),
                })
            elif element.tag == "image":
                record["images"].append(entry)
            elif element.tag == "stack":
                record["stacks"].append(entry)
            positions = Counter()
            for child in element:
                positions[child.tag] += 1
                walk(child, f"{path}/{child.tag}[{positions[child.tag]}]", current_conditions)

        walk(root, f"/{root.tag}", [])
        result["files"].append(record)
    return result


def load_inventory(upstream):
    def read_source(path):
        return subprocess.check_output(
            ["git", "-C", str(upstream), "show", f"{BASELINE}:{path}"], timeout=30,
        )

    documents = {
        RESOURCE_ROOT + name: read_source(RESOURCE_ROOT + name)
        for name in ("tome.xml", "arcanearchives_templates.xml")
    }
    library = read_source(GUIDEBOOK_JAR)
    with zipfile.ZipFile(io.BytesIO(library)) as archive:
        documents[f"{GUIDEBOOK_JAR}!/{STANDARD_TEMPLATE}"] = archive.read(STANDARD_TEMPLATE)
    result = inventory(documents)
    result["embedded_library"] = {
        "source": GUIDEBOOK_JAR,
        "sha256": hashlib.sha256(library).hexdigest(),
        "notice": "docs/upstream/Guidebook_License.txt",
        "scope": "Read-only inventory of the included standard XML; no legacy backend or artwork is bundled in production artifacts.",
    }
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path, help="Local upstream git checkout containing the pinned baseline")
    parser.add_argument("--check", action="store_true", help="Require the committed-format audit to match without writing it")
    args = parser.parse_args()
    result = load_inventory(args.upstream)
    rendered = json.dumps(result, indent=2, ensure_ascii=False) + "\n"
    if args.check:
        if not OUTPUT.is_file() or OUTPUT.read_text(encoding="utf-8") != rendered:
            raise SystemExit("Guidebook inventory differs; regenerate and review the source-backed changes")
    else:
        OUTPUT.write_text(rendered, encoding="utf-8")
    for record in result["files"]:
        print(record["source"], json.dumps(record["element_counts"], sort_keys=True))


if __name__ == "__main__":
    main()
