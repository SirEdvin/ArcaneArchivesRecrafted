#!/usr/bin/env python3
"""Reproduce the Gem Cutter's release assets and native model metadata.

No replacement geometry: keep the upstream OBJ/PNGs byte-identical. The MTL's
obsolete vanilla diorite ID and Forge 1.12 model metadata need explicit mapping.
"""
import argparse
import hashlib
import json
import math
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def multiply(a, b):
    return [[sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)] for i in range(3)]


def rotation_xyz(rotations):
    # Legacy rotations are an ordered product, not vanilla's XYZ Euler tuple.
    matrix = [[1, 0, 0], [0, 1, 0], [0, 0, 1]]
    for rotation in rotations:
        axis, degrees = next(iter(rotation.items()))
        c, s = math.cos(math.radians(degrees)), math.sin(math.radians(degrees))
        r = {
            'x': [[1, 0, 0], [0, c, -s], [0, s, c]],
            'y': [[c, 0, s], [0, 1, 0], [-s, 0, c]],
            'z': [[c, -s, 0], [s, c, 0], [0, 0, 1]],
        }[axis]
        matrix = multiply(matrix, r)
    y = math.asin(max(-1, min(1, matrix[0][2])))
    if abs(math.cos(y)) < 1e-8:
        # At +/-90 degrees yaw only the combined roll/pitch is identifiable.
        return [math.degrees(math.atan2(matrix[2][1], matrix[1][1])), math.degrees(y), 0]
    x = math.atan2(-matrix[1][2], matrix[2][2])
    z = math.atan2(-matrix[0][1], matrix[0][0])
    return [math.degrees(value) for value in (x, y, z)]


def generated(upstream):
    manifest = json.loads((ROOT / 'docs/migration/gem-cutter-upstream.json').read_text())
    files = {}
    for asset in manifest['assets']:
        data = subprocess.check_output(['git', '-C', str(upstream), 'show', manifest['commit'] + ':' + asset['path']])
        if hashlib.sha256(data).hexdigest() != asset['sha256']:
            raise ValueError('Upstream hash mismatch: ' + asset['path'])
        files[asset['path']] = data
    prefix = 'src/main/resources/assets/arcanearchives/'
    legacy = json.loads(files[prefix + 'blockstates/gemcutters_table.json'])
    result = {p: data for p, data in files.items() if p.endswith(('.png', '.obj', '.mtl'))}
    material = prefix + 'models/block/gemcutters_table.mtl'
    result[material] = result[material].replace(b'minecraft:blocks/stone_diorite', b'minecraft:block/diorite')
    display = {}
    for context, transform in legacy['variants']['inventory'][0]['transform'].items():
        display[context] = {
            'rotation': rotation_xyz(transform['rotation']),
            'translation': [value * 16 for value in transform['translation']],
            'scale': [transform['scale']] * 3,
        }
    models = {
        'models/block/gemcutters_table.json': {
            'loader': '${obj_loader}:obj', 'model': 'arcanearchives:models/block/gemcutters_table.obj',
            'flip_v': True, 'automatic_culling': False, 'shade_quads': True,
            'render_type': 'minecraft:cutout', 'textures': {'particle': 'arcanearchives:blocks/block_gemcutterstable'},
            'display': display,
        },
        'models/block/gemcutters_table_accessor.json': {
            'textures': {'particle': 'arcanearchives:blocks/block_gemcutterstable'}, 'elements': [],
        },
        'models/item/gemcutters_table.json': {'parent': 'arcanearchives:block/gemcutters_table'},
        'blockstates/gemcutters_table.json': {'variants': {
            f'accessor={accessor},facing={facing}': {
                'model': 'arcanearchives:block/gemcutters_table' + ('_accessor' if accessor == 'true' else ''), 'y': rotation,
            }
            for accessor in ('false', 'true')
            for facing, rotation in [('west', 0), ('north', 90), ('east', 180), ('south', 270)]
        }},
    }
    result.update({prefix + path: (json.dumps(data, indent=2) + '\n').encode() for path, data in models.items()})
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('upstream', type=Path)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    files = generated(args.upstream)
    for path, data in files.items():
        target = ROOT / path
        if args.check:
            if not target.exists() or target.read_bytes() != data:
                raise ValueError('Resource differs from pinned conversion: ' + path)
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
    print(f'{"Verified" if args.check else "Wrote"} {len(files)} original/converted Gem Cutter resources')


if __name__ == '__main__':
    main()
