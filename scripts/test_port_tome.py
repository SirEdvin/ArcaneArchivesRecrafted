"""Complete converted-resource contract; no connected rendering claim."""
import json
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET
from port_tome import ROOT, ASSETS, BOOK, item


class TomeConversionTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.resources = ROOT / "src/main/resources"
        cls.base = cls.resources / ASSETS
        cls.entries = {"arcanearchives:" + str(p.relative_to(cls.base / "entries").with_suffix("")):
                       json.loads(p.read_text()) for p in (cls.base / "entries").rglob("*.json")}
        cls.templates = {"arcanearchives:" + str(p.relative_to(cls.base / "templates").with_suffix("")):
                         json.loads(p.read_text()) for p in (cls.base / "templates").rglob("*.json")}

    def test_complete_inventory_and_binding(self):
        report = json.loads((ROOT / "docs/migration/tome-conversion.json").read_text())
        self.assertEqual(98, report["source_sections"])
        self.assertEqual(["Blocks:RadiantFurnace"], report["excluded_sections"])
        self.assertEqual(73, len(self.entries))
        self.assertEqual(report["pages"], sum(len(e["pages"]) for e in self.entries.values()))
        self.assertEqual(44, sum(p["type"] in ("patchouli:crafting", "arcanearchives:gem_cutting_output") for e in self.entries.values() for p in e["pages"]))
        book = json.loads((self.resources / BOOK).read_text())
        self.assertTrue(book["use_resource_pack"] and book["dont_generate_book"])
        self.assertEqual("arcanearchives:tome_arcana", book["custom_book_item"])
        self.assertEqual("RESIZE", book["text_overflow_mode"])
        for file in report["files"]:
            self.assertTrue((self.resources / file).is_file(), file)

    def test_every_link_and_template_resolves(self):
        for path in self.base.rglob("*.json"):
            source = path.read_text()
            self.assertNotIn("PLACEHOLDER", source, str(path))
            self.assertNotIn("radiant_furnace", source, str(path))
            for link in re.findall(r"\$\(l:([^)]*)\)", source):
                self.assertIn(link, self.entries, str(path))
            self.assertEqual(len(re.findall(r"\$\(l:", source)), source.count("$(/l)"), str(path))
        for entry in self.entries.values():
            self.assertTrue(entry["pages"])
            for page in entry["pages"]:
                if not page["type"].startswith("patchouli:"):
                    self.assertIn(page["type"], self.templates)

    def test_all_displayed_mod_items_exist(self):
        registry = (ROOT / "src/main/java/com/aranaira/arcanearchives/init/ContentRegistry.java").read_text()
        names = set(re.findall(r'(?:item|block)\("([^\"]+)"', registry))
        for entry in self.entries.values():
            if entry["icon"].startswith("arcanearchives:"):
                self.assertIn(entry["icon"].split(":")[1], names)
            for stack in entry.get("extra_recipe_mappings", {}):
                self.assertIn(stack.split(":")[1], names)
        for template in self.templates.values():
            for component in template["components"]:
                if component["type"] == "patchouli:item" and component["item"].startswith("arcanearchives:"):
                    self.assertIn(component["item"].split(":")[1], names)
                if component["type"] == "patchouli:image" and component["image"].startswith("arcanearchives:"):
                    self.assertTrue((self.resources / "assets/arcanearchives" / component["image"].split(":")[1]).is_file())

    def test_anonymous_continuations_and_nested_conditions(self):
        self.assertNotIn("flag", self.entries["arcanearchives:gems/recharginggems"])
        flags = {p.get("flag") for p in self.entries["arcanearchives:gems/recharginggems"]["pages"]}
        self.assertIn("arcanearchives:arsenal_disabled", flags)
        self.assertIn("&arcanearchives:arsenal_enabled,arcanearchives:false", flags)
        self.assertTrue(any(p.get("flag") == "&arcanearchives:arsenal_enabled,arcanearchives:ml_potioncore"
                            for p in self.entries["arcanearchives:gems/cleansegleam"]["pages"]))

    def test_flattening_is_explicit_and_unknown_metadata_rejected(self):
        self.assertEqual("minecraft:podzol", item(ET.fromstring('<stack item="minecraft:dirt" meta="2"/>')))
        self.assertEqual("minecraft:damaged_anvil", item(ET.fromstring('<stack item="minecraft:anvil" meta="2"/>')))
        with self.assertRaises(ValueError):
            item(ET.fromstring('<stack item="minecraft:dirt" meta="17"/>'))
        self.assertIsNone(item(ET.fromstring('<stack item="arcanearchives:matrix_storage"/>')))

    def test_audited_paragraph_text_is_preserved_per_topic(self):
        audit = json.loads((ROOT / "docs/migration/guidebook-baseline.json").read_text())
        source = next(f for f in audit["files"] if f["source"].endswith("/tome.xml"))
        chapters = ["home", "tableofcontents", "blocks", "items", "gems", "concepts", "index"]
        topic = None
        previous_chapter = None
        checked = 0
        for record in source["sections"]:
            section = ET.fromstring(record["xml"])
            match = re.search(r"chapter\[(\d+)\]", record["element"])
            assert match is not None
            chapter = chapters[int(match.group(1)) - 1]
            if section.get("id") or chapter != previous_chapter:
                topic = "arcanearchives:" + chapter + "/" + section.get("id", "Introduction").lower()
            previous_chapter = chapter
            if topic == "arcanearchives:blocks/radiantfurnace":
                continue
            assert topic is not None
            entry = self.entries[topic]
            texts = []
            for page in entry["pages"]:
                texts.append(page.get("text", ""))
                for component in self.templates.get(page["type"], {}).get("components", []):
                    texts.append(component.get("text", ""))
            prose = " ".join(re.sub(r"\$\([^)]*\)", "", " ".join(texts)).split())
            for paragraph in section.findall("p"):
                if any(link.get("ref") == "Blocks:RadiantFurnace" for link in paragraph.iter("link")):
                    continue
                for fragment in paragraph.itertext():
                    fragment = " ".join(fragment.split())
                    if len(fragment) >= 8 and fragment != "PLACEHOLDER":
                        self.assertIn(fragment, prose, topic)
                        checked += 1
        self.assertGreater(checked, 600)


if __name__ == "__main__":
    unittest.main()
