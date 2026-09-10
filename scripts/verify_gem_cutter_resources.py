#!/usr/bin/env python3
"""Verify packaged original Gem Cutter geometry/textures and converted model metadata."""
import hashlib
import json
from pathlib import Path
import tomllib
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]


def verify():
    properties = tomllib.loads((ROOT / 'stonecutter.properties.toml').read_text())
    manifest = json.loads((ROOT / 'docs/migration/gem-cutter-upstream.json').read_text())
    prefix = 'assets/arcanearchives/'
    for minecraft, loader in [('1.20.1', 'fabric'), ('1.20.1', 'forge'), ('1.21.1', 'fabric'), ('1.21.1', 'neoforge')]:
        node = minecraft + '-' + loader
        name = f'arcanearchives-{loader}-{properties["mod"]["version"]}+{minecraft}.jar'
        with ZipFile(ROOT / 'versions' / node / 'build/libs' / name) as jar:
            for asset in manifest['assets']:
                path = asset['path'].removeprefix('src/main/resources/')
                if path.endswith(('.png', '.obj')):
                    assert hashlib.sha256(jar.read(path)).hexdigest() == asset['sha256'], (node, path)
                elif path.endswith('.mtl'):
                    original = jar.read(path).replace(b'minecraft:block/diorite', b'minecraft:blocks/stone_diorite')
                    assert hashlib.sha256(original).hexdigest() == asset['sha256'], (node, path)
            for path in ('models/block/gemcutters_table.json', 'models/block/gemcutters_table_accessor.json',
                         'models/item/gemcutters_table.json', 'blockstates/gemcutters_table.json'):
                packaged = json.loads(jar.read(prefix + path))
                canonical = (ROOT / 'src/main/resources' / prefix / path).read_text()
                assert packaged == json.loads(canonical.replace('${obj_loader}', 'neoforge' if loader == 'neoforge' else 'forge')), (node, path)
            model = json.loads(jar.read(prefix + 'models/block/gemcutters_table.json'))
            assert model['flip_v'] is True and model['automatic_culling'] is False
            assert model['render_type'] == 'minecraft:cutout'
            assert len(json.loads(jar.read(prefix + 'blockstates/gemcutters_table.json'))['variants']) == 8
            atlas = json.loads(jar.read('assets/minecraft/atlases/blocks.json'))
            sprites = {entry['resource'] for entry in atlas['sources'] if entry['type'] == 'minecraft:single'}
            for line in jar.read(prefix + 'models/block/gemcutters_table.mtl').decode().splitlines():
                if line.startswith('map_Kd arcanearchives:'):
                    assert line.split()[1] in sprites, (node, line)
            assert 'META-INF/upstream/LICENSE' in jar.namelist()
        print(f'PASS {node}: original OBJ/PNGs, remapped MTL, model metadata, eight states and atlas entries')


if __name__ == '__main__':
    verify()
