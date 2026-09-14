"""Generator safety tests with explicitly synthetic upstream resources."""
import contextlib
import io
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import port_brazier_assets as assets


def source(command):
    path = command[-1]
    if path.endswith('.obj'):
        return b'mtllib brazier_of_hoarding.mtl\nusemtl Master\n'
    if path.endswith('.mtl'):
        return b'newmtl Master\nmap_Kd arcanearchives:blocks/block_arcanearchives_master\n'
    if path.endswith('.json'):
        return b'{"variants":{"inventory":[{"transform":{}}]}}'
    return b'synthetic test resource'


class BrazierGeneratorTest(unittest.TestCase):
    def test_late_conflict_does_not_create_earlier_files(self):
        with tempfile.TemporaryDirectory() as directory, patch.object(assets, 'ROOT', Path(directory)), \
                patch.object(assets.subprocess, 'check_output', side_effect=source):
            target = Path(directory) / assets.PREFIX / 'models/block/brazier_of_hoarding_fire.json'
            target.parent.mkdir(parents=True)
            target.write_bytes(b'conflicting user resource')
            for check in (False, True):
                with self.assertRaisesRegex(ValueError, 'Refusing to replace|Missing resource'):
                    assets.generate(Path('synthetic-upstream'), check=check)
                self.assertEqual([target], [p for p in Path(directory).rglob('*') if p.is_file()])
                self.assertEqual(b'conflicting user resource', target.read_bytes())

    def test_generation_is_reproducible_and_check_is_read_only(self):
        with tempfile.TemporaryDirectory() as directory, patch.object(assets, 'ROOT', Path(directory)), \
                patch.object(assets.subprocess, 'check_output', side_effect=source), \
                contextlib.redirect_stdout(io.StringIO()):
            assets.generate(Path('synthetic-upstream'))
            before = {p: (p.read_bytes(), p.stat().st_mtime_ns) for p in Path(directory).rglob('*') if p.is_file()}
            assets.generate(Path('synthetic-upstream'), check=True)
            assets.generate(Path('synthetic-upstream'))
            self.assertEqual(before, {p: (p.read_bytes(), p.stat().st_mtime_ns) for p in before})


if __name__ == '__main__':
    unittest.main()
