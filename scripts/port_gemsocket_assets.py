"""Recover the original Gem Socket item and both GUI art sets."""
import argparse
from pathlib import Path
import subprocess
from port_parchtear_assets import PIN, PREFIX, ROOT

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('upstream', type=Path)
    upstream = parser.parse_args().upstream
    files = set(subprocess.check_output(['git', '-C', str(upstream), 'ls-tree', '-r', '--name-only', PIN, PREFIX], text=True).splitlines())
    paths = ['models/item/gemsocket.json', 'textures/items/item_gemsocket.png',
             'textures/gui/player_inv.png', 'textures/gui/simple/player_inv.png',
             'textures/gui/fabrial.png', 'textures/gui/simple/single_slot.png']
    for path in paths:
        if path.endswith('.png') and PREFIX + path + '.mcmeta' in files:
            paths.append(path + '.mcmeta')
        data = subprocess.check_output(['git', '-C', str(upstream), 'show', PIN + ':' + PREFIX + path])
        target = ROOT / PREFIX / path
        if target.exists() and target.read_bytes() != data:
            raise ValueError('Refusing to replace differing resource: ' + str(target))
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
        print(path)
