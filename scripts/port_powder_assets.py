"""Recover pinned chromatic powder art and its animation companions."""
import argparse
import json
from pathlib import Path
import subprocess
from port_parchtear_assets import generate, PIN, PREFIX, ROOT

COLORS = ('red', 'orange', 'yellow', 'green', 'cyan', 'blue', 'purple', 'pink', 'black', 'white')

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('upstream', type=Path)
    upstream = parser.parse_args().upstream
    generate(upstream, COLORS, 'powder')
    for path in ('models/item/full_spectrum_chromatic_powder.json',
                 'textures/items/gems/powder/rainbow.png', 'textures/items/gems/powder/rainbow.png.mcmeta'):
        data = subprocess.check_output(['git', '-C', str(upstream), 'show', PIN + ':' + PREFIX + path])
        target = ROOT / PREFIX / path
        if target.exists() and target.read_bytes() != data:
            raise ValueError('Refusing to replace differing resource: ' + str(target))
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
    model = {'parent': 'arcanearchives:item/gems/powder/white', 'overrides': [
        {'predicate': {'arcanearchives:powder_color': index / 10}, 'model': 'arcanearchives:item/gems/powder/' + color}
        for index, color in enumerate(COLORS, 1)
    ]}
    target = ROOT / PREFIX / 'models/item/chromatic_powder.json'
    data = (json.dumps(model, indent=2) + '\n').encode()
    if target.exists() and target.read_bytes() != data:
        raise ValueError('Refusing to replace differing powder model')
    target.write_bytes(data)
