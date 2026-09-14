#!/usr/bin/env python3
import unittest
import xml.etree.ElementTree as ET

from prepare_tome import REPAIRS, prepare


class TomePreparationTest(unittest.TestCase):
    def document(self):
        root = ET.Element("book")
        chapters = {}
        for target in REPAIRS.values():
            chapter, section = target.split(":")
            if chapter not in chapters:
                chapters[chapter] = ET.SubElement(root, "chapter", id=chapter)
            if chapters[chapter].find(f"section[@id='{section}']") is None:
                ET.SubElement(chapters[chapter], "section", id=section)
        return root

    def test_explicit_repairs_preserve_content_conditions_and_source(self):
        root = self.document()
        links = ET.SubElement(root, "stack-links")
        for literal in REPAIRS:
            link = ET.SubElement(root, "link", ref=literal, condition="arsenal_enabled")
            link.text = "Before "
            ET.SubElement(link, "b").text = "formatted label"
            link[0].tail = " after"
            link.tail = " outside"
            stack = ET.SubElement(links, "stack", item="arcanearchives:quartz_sliver")
            stack.text = literal
        before = ET.tostring(root)
        result, counts, checked = prepare(root)
        self.assertEqual(before, ET.tostring(root))
        self.assertEqual(checked, len(REPAIRS) * 2)
        self.assertEqual(dict(counts), dict.fromkeys(REPAIRS, 2))
        # Undo precisely the authorized changes; entire parsed tree must match.
        for old, new in zip(root.iter("link"), result.iter("link")):
            self.assertEqual(new.get("ref"), REPAIRS[old.attrib["ref"]])
            new.set("ref", old.attrib["ref"])
        for old, new in zip(root.findall("stack-links/stack"), result.findall("stack-links/stack")):
            assert old.text is not None
            self.assertEqual(new.text, REPAIRS[old.text])
            new.text = old.text
        self.assertEqual(before, ET.tostring(result))

    def test_unknown_targets_and_whitespace_are_not_repaired(self):
        for literal in ("Missing", "Items:Missing", " RadiantResonator", "Blocks:RadiantResonator "):
            with self.subTest(literal=literal):
                root = self.document()
                ET.SubElement(root, "link", ref=literal)
                with self.assertRaisesRegex(ValueError, "Unresolved Tome target"):
                    prepare(root)

    def test_missing_approved_destination_is_rejected(self):
        root = ET.fromstring('<book><chapter id="Blocks"/><link ref="RadiantResonator"/></book>')
        with self.assertRaisesRegex(ValueError, "Unresolved Tome target"):
            prepare(root)

    def test_existing_links_and_external_urls_are_unchanged(self):
        root = self.document()
        ET.SubElement(root, "link", ref="Blocks")
        ET.SubElement(root, "link", ref="Blocks:RadiantResonator")
        ET.SubElement(root, "link", href="https://example.org")
        result, counts, checked = prepare(root)
        self.assertEqual(ET.tostring(result), ET.tostring(root))
        self.assertFalse(counts)
        self.assertEqual(checked, 2)

    def test_duplicate_ids_are_rejected(self):
        for xml in ('<book><chapter id="A"/><chapter id="A"/></book>',
                    '<book><chapter id="A"><section id="B"/><section id="B"/></chapter></book>'):
            with self.subTest(xml=xml), self.assertRaises(ValueError):
                prepare(ET.fromstring(xml))


if __name__ == "__main__":
    unittest.main()
