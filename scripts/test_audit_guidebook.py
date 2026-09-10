"""Small fixtures for the loss-sensitive parts of the Guidebook source inventory."""

import hashlib
import io
import unittest
from unittest.mock import call, patch
import xml.etree.ElementTree as ET
import zipfile

from audit_guidebook import BASELINE, GUIDEBOOK_JAR, RESOURCE_ROOT, STANDARD_TEMPLATE, inventory, load_inventory


class GuidebookInventoryTest(unittest.TestCase):
    def test_loads_xml_and_embedded_library_from_pinned_git_objects(self):
        library = io.BytesIO()
        standard = b'<library><template id="external"><element index="0"/></template></library>'
        with zipfile.ZipFile(library, "w") as archive:
            archive.writestr(STANDARD_TEMPLATE, standard)
        sources = {
            RESOURCE_ROOT + "tome.xml": b'<book><chapter><section><p>Pinned prose</p></section></chapter></book>',
            RESOURCE_ROOT + "arcanearchives_templates.xml": b'<library/>',
            GUIDEBOOK_JAR: library.getvalue(),
        }
        expected_calls = [
            call(["git", "-C", "not-a-working-directory", "show", f"{BASELINE}:{path}"], timeout=30)
            for path in sources
        ]
        with patch("audit_guidebook.subprocess.check_output", side_effect=list(sources.values())) as read:
            result = load_inventory("not-a-working-directory")
        self.assertEqual(read.call_args_list, expected_calls)
        self.assertEqual(result["embedded_library"]["sha256"], hashlib.sha256(library.getvalue()).hexdigest())
        by_source = {record["source"]: record for record in result["files"]}
        external = by_source[f"{GUIDEBOOK_JAR}!/{STANDARD_TEMPLATE}"]
        self.assertEqual(external["sha256"], hashlib.sha256(standard).hexdigest())
        self.assertIn('index="0"', external["templates"][0]["xml"])

    def test_preserves_section_prose_inline_formatting_and_tail_text(self):
        source = b'''<book><chapter id="Items" condition="enabled"><section>
        <p>Before <b>bold <i>nested</i> tail</b> between
        <link ref="Items:Target" condition="optional">linked</link> after &amp; end.</p>
        </section><section><p>Anonymous continuation.</p></section></chapter></book>'''
        record = inventory({"tome.xml": source})["files"][0]
        original = ET.fromstring(source).find("chapter/section")
        captured = ET.fromstring(record["sections"][0]["xml"])
        self.assertEqual(ET.tostring(captured), ET.tostring(original).strip())
        self.assertEqual("".join(captured.itertext()), "".join(original.itertext()))
        self.assertEqual(record["sections"][0]["condition_ancestry"], ["enabled"])
        self.assertIn("Anonymous continuation.", record["sections"][1]["xml"])

    def test_preserves_all_stacks_with_locations_and_conditions(self):
        source = b'''<book><stack-links><stack item="old:linked">Items:Target</stack></stack-links>
        <chapter condition="enabled"><section><p condition="optional">
        <stack item="old:literal Typo" count="3"/> and <stack ore="nuggetGold"/>
        </p><recipe><recipe.result><stack item="old:result" count="2"/></recipe.result>
        </recipe></section></chapter></book>'''
        record = inventory({"tome.xml": source})["files"][0]
        self.assertEqual(len(record["stacks"]), record["element_counts"]["stack"])
        inline = record["stacks"][1]
        self.assertEqual(inline["element"], "/book/chapter[1]/section[1]/p[1]/stack[1]")
        self.assertEqual(inline["attributes"], {"item": "old:literal Typo", "count": "3"})
        self.assertEqual(inline["condition_ancestry"], ["enabled", "optional"])
        self.assertEqual(record["stacks"][2]["attributes"], {"ore": "nuggetGold"})
        self.assertEqual(record["stacks"][3]["condition_ancestry"], ["enabled"])

    def test_preserves_template_bodies_and_exact_source_paths(self):
        path = "embed/library.jar!/assets/example/xml/standard.xml"
        source = b'''<library><template id="title" height="32" mode="flow">
        <p>Original <element index="0" scale="1.5"/> suffix</p>
        <image src="example:book" tx="50"/></template></library>'''
        record = inventory({path: source})["files"][0]
        self.assertEqual(record["source"], path)
        captured = ET.fromstring(record["templates"][0]["xml"])
        self.assertEqual(ET.tostring(captured), ET.tostring(ET.fromstring(source).find("template")))

    def test_preserves_conditions_repeated_sections_and_literal_identifiers(self):
        source = b'''<book title="Test"><conditions><true name="enabled"/></conditions>
        <chapter id="Items" condition="enabled"><section id="First">
        <link ref="Items:TypoPreserved" condition="other">Original text</link>
        </section><section><title>Continuation</title></section></chapter></book>'''
        record = inventory({"tome.xml": source})["files"][0]
        self.assertEqual(record["sha256"], hashlib.sha256(source).hexdigest())
        self.assertEqual(record["element_counts"]["section"], 2)
        self.assertEqual(record["sections"][1]["element"], "/book/chapter[1]/section[2]")
        self.assertEqual(record["sections"][1]["attributes"], {})
        self.assertEqual(record["sections"][1]["titles"], ["Continuation"])
        self.assertEqual(record["links"][0]["attributes"]["ref"], "Items:TypoPreserved")
        self.assertEqual(record["links"][0]["condition_ancestry"], ["enabled", "other"])
        self.assertEqual(record["links"][0]["text"], "Original text")
        self.assertEqual(record["conditions"][0]["name"], "enabled")

    def test_preserves_recipe_stack_links_and_image_attributes(self):
        source = b'''<book><include ref="gbook_snapshot:xml/standard.xml"/>
        <stack-links><stack item="old:id">Items:Target</stack></stack-links>
        <chapter><section><recipe type="arcanearchives:gct_recipe">
        <recipe.result><stack item="old:id" count="2"/></recipe.result></recipe>
        <image src="old:image" hoverSrc="old:hover" tx="4"/></section></chapter></book>'''
        record = inventory({"tome.xml": source})["files"][0]
        self.assertEqual(record["recipes"][0]["attributes"]["type"], "arcanearchives:gct_recipe")
        self.assertIn('count="2"', record["recipes"][0]["xml"])
        self.assertEqual(record["stack_links"], [{"attributes": {"item": "old:id"}, "target": "Items:Target"}])
        self.assertEqual(record["images"][0]["attributes"], {"src": "old:image", "hoverSrc": "old:hover", "tx": "4"})
        self.assertEqual(record["includes"][0]["attributes"]["ref"], "gbook_snapshot:xml/standard.xml")
        self.assertEqual(inventory({"tome.xml": source}), inventory({"tome.xml": source}))


if __name__ == "__main__":
    unittest.main()
