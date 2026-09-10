"""Recover all GUI preference textures from the pinned release, without replacing differing art."""
import argparse
from pathlib import Path
import subprocess
from port_parchtear_assets import PIN, PREFIX, ROOT

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('upstream', type=Path)
    upstream = parser.parse_args().upstream
    for name in ('radiantchest', 'radiant_upgrades', 'radiantcraftingtable', 'gemcutterstable',
                 'devouring_charm', 'player_inv'):
        for variant in ('', 'simple/'):
            path = 'textures/gui/' + variant + name + '.png'
            data = subprocess.check_output(['git', '-C', str(upstream), 'show', PIN + ':' + PREFIX + path])
            target = ROOT / PREFIX / path
            if target.exists() and target.read_bytes() != data:
                raise ValueError('Refusing to replace differing resource: ' + str(target))
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
            print(path)
