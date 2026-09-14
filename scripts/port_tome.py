#!/usr/bin/env python3
"""Convert the complete audited Tome to native Patchouli resources (0151)."""
import argparse
from collections import Counter
import json
from pathlib import Path
import re
import struct
import subprocess
import xml.etree.ElementTree as ET

from prepare_tome import BASELINE, SOURCE, prepare

ROOT = Path(__file__).resolve().parents[1]
ASSETS = "assets/arcanearchives/patchouli_books/tome_arcana/en_us/"
BOOK = "data/arcanearchives/patchouli_books/tome_arcana/book.json"
EXCLUDED = "Blocks:RadiantFurnace"
ITEM_ALIASES = {
    ("minecraft:grass", "0"): "minecraft:grass_block",
    ("minecraft:stonebrick", "0"): "minecraft:stone_bricks",
    ("minecraft:stonebrick", "1"): "minecraft:mossy_stone_bricks",
    ("minecraft:stonebrick", "2"): "minecraft:cracked_stone_bricks",
    ("minecraft:stonebrick", "3"): "minecraft:chiseled_stone_bricks",
    ("minecraft:dirt", "1"): "minecraft:coarse_dirt",
    ("minecraft:dirt", "2"): "minecraft:podzol",
    ("minecraft:anvil", "1"): "minecraft:chipped_anvil",
    ("minecraft:anvil", "2"): "minecraft:damaged_anvil",
    ("arcanearchives:munchgleam", "0"): "arcanearchives:munchstone",
    ("arcanearchives:ordergleam", "0"): "arcanearchives:orderstone",
}
UNREGISTERED = {"arcanearchives:" + name for name in (
    "matrix_crystal_core", "matrix_repository", "matrix_storage", "radiant_furnace")}
EXTERNAL_LABELS = {
    "astralsorcery:blockwell": "Astral Sorcery Lightwell",
    "astralsorcery:itemjournal": "Astral Tome",
    "botania:altar": "Petal Apothecary",
    "botania:lexicon": "Lexica Botania",
    "quark:ancient_tome": "Ancient Tome",
    "thaumcraft:crucible": "Crucible",
    "thaumcraft:infusion_matrix": "Infusion Matrix",
    "thaumcraft:thaumonomicon": "Thaumonomicon",
}
IMAGE_LABELS = {"potioncore:gui/potion/antidote": "Antidote",
                "thaumcraft:aspects/auram": "Auram"}


def dump(value):
    return (json.dumps(value, ensure_ascii=False, indent=2) + "\n").encode()


def plain(value):
    return " ".join((value or "").split())


def slug(value):
    return value.lower()


def item(element):
    literal = element.attrib["item"]
    meta = element.get("meta", "0")
    mapped = ITEM_ALIASES.get((literal, meta), literal)
    if meta != "0" and (literal, meta) not in ITEM_ALIASES:
        raise ValueError(f"Unmapped item metadata: {literal}:{meta}")
    if mapped in UNREGISTERED or mapped in EXTERNAL_LABELS:
        return None
    if not mapped.startswith(("arcanearchives:", "minecraft:")):
        raise ValueError(f"Unreviewed optional item: {mapped}")
    return mapped


def generated(upstream):
    def source(path):
        return subprocess.check_output(["git", "-C", str(upstream), "show", f"{BASELINE}:{path}"], timeout=30)

    root, repairs, checked = prepare(ET.fromstring(source(SOURCE)))
    inventory = Counter(e.tag for e in root.iter())
    if (inventory["chapter"], inventory["section"], inventory["recipe"], checked) != (7, 98, 44, 317):
        raise ValueError("Incomplete audited source")
    files = {}
    topics = {}
    titles = {}
    chapter_first = {}
    for chapter in root.findall("chapter"):
        current = None
        for index, section in enumerate(chapter.findall("section")):
            if section.get("id") or current is None:
                current = chapter.attrib["id"] + ":" + section.get("id", "Introduction")
                topics[current] = []
                titles[current] = section.findtext("page_title_shorter/title") or chapter.get("id")
                chapter_first.setdefault(chapter.get("id"), current)
            topics[current].append(section)
    del topics[EXCLUDED]

    def target(literal):
        literal = chapter_first.get(literal, literal)
        if literal == EXCLUDED:
            return None
        if literal not in topics:
            raise ValueError("Unresolved converted link: " + literal)
        return "arcanearchives:" + slug(literal.replace(":", "/"))

    def formatting(flags):
        return "$()" + ("$(bold)" if flags[0] else "") + ("$(italic)" if flags[1] else "")

    def text(element, inherited=(False, False)):
        flags = tuple(element.get(key, str(value).lower()) == "true"
                      for key, value in zip(("bold", "italics"), inherited))
        parts = [formatting(flags) if flags != inherited else "", element.text or ""]
        for child in element:
            value = text(child, flags)
            if child.tag == "link":
                link = target(child.get("ref"))
                label = value.strip() or titles.get(chapter_first.get(child.get("ref"), child.get("ref")), child.get("ref"))
                value = "" if link is None else f"$(l:{link}){label}$(/l)"
            elif child.tag == "stack":
                if item(child) is None and child.get("item") != "arcanearchives:radiant_furnace":
                    value = str(EXTERNAL_LABELS.get(child.attrib["item"], child.attrib["item"].split(":")[1].replace("_", " ").title())) + " "
            elif child.tag == "image":
                value = IMAGE_LABELS.get(child.get("src"), "")
            parts.extend((value, child.tail or ""))
        if flags != inherited:
            parts.append(formatting(inherited))
        return "".join(parts)

    illustrations = set()

    def image_component(element):
        literal = element.get("src")
        if literal in IMAGE_LABELS:
            return None
        if literal == "arcanearchives:blocks/placeholder":
            # The duplicate Networks navigation used a placeholder, not an illustration.
            return None
        if literal == "minecraft:blocks/furnace_front_on":
            path = "minecraft:textures/block/furnace_front_on.png"
            width = height = 16
        else:
            namespace, name = literal.split(":")
            if namespace != "arcanearchives":
                raise ValueError("Unreviewed image: " + literal)
            resource = f"assets/{namespace}/textures/{name}.png"
            data = source("src/main/resources/" + resource)
            if data[:8] != b"\x89PNG\r\n\x1a\n":
                raise ValueError("Not PNG: " + resource)
            width, height = struct.unpack(">II", data[16:24])
            files[resource] = data
            illustrations.add(resource)
            path = f"{namespace}:textures/{name}.png"
        draw_width = int(element.get("tw", width))
        draw_height = int(element.get("th", height))
        return {"type": "patchouli:image", "image": path, "width": draw_width,
                "height": draw_height, "texture_width": width, "texture_height": height,
                "scale": min(1, 106 / draw_width, 78 / draw_height)}

    recipe_files = {}
    for path in (ROOT / "src/main/resources/data/arcanearchives/recipe").glob("*.json"):
        value = json.loads(path.read_text())
        result = value.get("result", {})
        output = result if isinstance(result, str) else result.get("item", result.get("id", result.get("${result_key}")))
        if output:
            recipe_files.setdefault(output, []).append((value.get("type"), "arcanearchives:" + path.stem))

    mappings = {}
    for stack in root.findall("stack-links/stack"):
        mapped = item(stack)
        link = target(stack.text)
        if mapped and link:
            mappings.setdefault(link, {})[mapped] = 0

    pages_total = 0
    recipes_total = 0
    for order, (topic, sections) in enumerate(topics.items()):
        entry_id = target(topic)
        assert entry_id is not None
        chapter = topic.split(":")[0]
        pages = []
        icons = []
        for section in sections:
            section_flag = section.get("condition")
            for element in section:
                if element.tag in ("page_title_shorter", "horizontal_rule", "space"):
                    continue
                conditions = list(dict.fromkeys(c for c in (section_flag, element.get("condition")) if c))
                flag = ("&" if len(conditions) > 1 else "") + ",".join("arcanearchives:" + c for c in conditions)
                if element.tag == "recipe":
                    output = item(element.find("recipe.result/stack"))
                    if output is None:
                        raise ValueError("Missing recipe output")
                    if element.get("type") == "arcanearchives:gct_recipe":
                        page = {"type": "arcanearchives:gem_cutting_output", "output": output}
                    elif element.get("type") == "shaped":
                        candidates = [name for kind, name in recipe_files.get(output, []) if kind in ("minecraft:crafting_shaped", "minecraft:crafting_shapeless")]
                        if not 1 <= len(candidates) <= 2:
                            raise ValueError(f"Ambiguous/missing crafting recipe: {output}: {candidates}")
                        candidates.sort()
                        page = {"type": "patchouli:crafting", "recipe": candidates[0]}
                        if len(candidates) == 2:
                            page["recipe2"] = candidates[1]
                    else:
                        raise ValueError("Unknown recipe type")
                    recipes_total += 1
                elif element.tag == "p":
                    # Remove the approved furnace-only index/navigation row, not neighbouring prose.
                    if any(link.get("ref") == EXCLUDED for link in element.iter("link")):
                        continue
                    prose = plain(text(element))
                    if prose == "PLACEHOLDER":
                        prose = ("The original Tome left this topic unfinished and supplies no operating instructions. "
                                 "This entry preserves its place in the archive; it does not imply a working or obtainable feature.")
                    components = []
                    for visual in element.iter():
                        if visual.tag == "stack":
                            value = item(visual)
                            if value:
                                icons.append(value)
                                components.append({"type": "patchouli:item", "item": value})
                        elif visual.tag == "image":
                            component = image_component(visual)
                            if component:
                                components.append(component)
                    if not components and not prose:
                        continue
                    # Linked prose already names a lone leading item. Keep standalone item
                    # illustrations and multi-item diagrams; don't turn each index row into a page.
                    if prose and len(components) == 1 and components[0]["type"] == "patchouli:item":
                        components = []
                    if components:
                        x, y, row_height = 0, 0, 0
                        for component in components:
                            width = component.get("width", 16) * component.get("scale", 1)
                            height = component.get("height", 16) * component.get("scale", 1)
                            if x and x + width > 110:
                                x, y, row_height = 0, y + row_height + 4, 0
                            component.update(x=round(x), y=round(y))
                            x += width + 4
                            row_height = max(row_height, height)
                        if prose:
                            components.append({"type": "patchouli:text", "text": prose,
                                               "x": 0, "y": round(y + row_height + 6)})
                        template = "converted/" + slug(topic.replace(":", "/")) + "_" + str(len(pages))
                        files[ASSETS + "templates/" + template + ".json"] = dump({"components": components})
                        page = {"type": "arcanearchives:" + template}
                    else:
                        page = {"type": "patchouli:text", "text": prose}
                        # Native text pages can share short adjacent paragraphs with identical visibility.
                        if pages and pages[-1].get("type") == "patchouli:text" and pages[-1].get("flag", "") == flag and len(pages[-1]["text"]) + len(prose) < 420:
                            pages[-1]["text"] += "$(br2)" + prose
                            continue
                else:
                    raise ValueError("Unconverted element: " + element.tag)
                if flag:
                    page["flag"] = flag
                pages.append(page)
        if not pages:
            raise ValueError("Empty entry: " + topic)
        entry = {"name": titles[topic], "category": "arcanearchives:" + slug(chapter),
                 "icon": icons[0] if icons else "arcanearchives:tome_arcana",
                 "sortnum": order, "pages": pages}
        # Preserve mixed-condition continuation sections, e.g. Arsenal-disabled explanation.
        flags = {section.get("condition") for section in sections}
        if len(flags) == 1 and None not in flags:
            entry["flag"] = "arcanearchives:" + flags.pop()
        if entry_id in mappings:
            entry["extra_recipe_mappings"] = mappings[entry_id]
        files[ASSETS + "entries/" + entry_id.split(":")[1] + ".json"] = dump(entry)
        pages_total += len(pages)

    category_names = {"Home": "Welcome", "TableOfContents": "Table of Contents", "Blocks": "Blocks",
                      "Items": "Items", "Gems": "Arcane Gems", "Concepts": "Concepts", "Index": "Index"}
    for order, (chapter, name) in enumerate(category_names.items()):
        chapter_target = target(chapter)
        assert chapter_target is not None
        files[ASSETS + "categories/" + slug(chapter) + ".json"] = dump({
            "name": name, "description": "$(l:" + chapter_target + ")" + name + "$(/l)",
            "icon": "arcanearchives:tome_arcana", "sortnum": order})
    files[BOOK] = dump({"name": "Tome of Arcana", "landing_text": "The Arcane Archives: quartz, storage, networks and arcana. Begin with the Radiant Resonator in Blocks, or browse the original table of contents.",
        "version": "1", "use_resource_pack": True, "dont_generate_book": True,
        "custom_book_item": "arcanearchives:tome_arcana", "show_progress": False,
        "show_toasts": False, "pause_game": False, "i18n": False,
        "text_overflow_mode": "RESIZE"})
    report = {"baseline": BASELINE, "source_sections": inventory["section"], "excluded_sections": [EXCLUDED],
              "entries": len(topics), "pages": pages_total, "recipes": recipes_total,
              "illustrations": len(illustrations), "validated_source_targets": checked,
              "reference_repairs": dict(repairs), "files": sorted(files)}
    return files, report


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    files, report = generated(args.upstream)
    base = ROOT / "src/main/resources"
    converted = base / ASSETS / "templates/converted"
    for old in converted.rglob("*.json"):
        if str(old.relative_to(base)) not in files:
            if args.check:
                raise ValueError("Obsolete converted template: " + str(old))
            old.unlink()
    for name, data in files.items():
        path = base / name
        if args.check:
            if not path.is_file() or path.read_bytes() != data:
                raise ValueError("Missing or stale converted resource: " + name)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(data)
    report_path = ROOT / "docs/migration/tome-conversion.json"
    if args.check:
        if report_path.read_bytes() != dump(report):
            raise ValueError("Stale conversion inventory")
    else:
        report_path.write_bytes(dump(report))
    print(json.dumps({k: v for k, v in report.items() if k != "files"}, indent=2))


if __name__ == "__main__":
    main()
