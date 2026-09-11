#!/usr/bin/env python3
"""Verify native Matrix Reservoir state persistence and cleanup across real server restarts.

Operator commands seed columns, not player placement. Uses guarded empty space
in existing loopback/EULA-approved ignored worlds. Prints recovery commands and
runs the cleanup session even when setup assertions fail.
"""
import argparse
import json
import uuid
from smoke_servers import NODES, preflight, smoke


def fixtures(node, objective):
    block = 'arcanearchives:matrix_reservoir'
    area = 'x=9,y=298,z=9,dx=4,dy=9,dz=2'
    air = ' '.join(f'if block {x} {y} {z} air'
                   for x in range(9, 14) for y in range(298, 308) for z in range(9, 12))
    guard = f'execute if score ready {objective} matches 1 run '
    positions = [(x, 300 + part, part) for x in range(10, 13) for part in range(3)]
    positions += [(10, 303 + part, part) for part in range(3)]
    identities = ' '.join(f'if block {x} {y} 10 {block}[part={part}]' for x, y, part in positions)
    setup = [
        f'scoreboard objectives add {objective} dummy',
        f'execute store success score forced {objective} run forceload query 0 0',
        f'execute if score forced {objective} matches 0 run forceload add 0 0',
        f'scoreboard players set ready {objective} 0',
        f'execute {air} unless entity @e[{area}] run scoreboard players set ready {objective} 1',
    ]
    setup += [guard + command for command in [
        'say AA_MATRIX_SPACE',
        'fill 9 298 9 13 298 11 stone',
        *[f'setblock {x} {y} 10 {block}[part={part}]' for x, y, part in positions],
        f'execute {identities} run say AA_MATRIX_SEEDED',
        'save-all flush',
    ]] + ['say AA_MATRIX_SETUP_FINISHED']
    restore = [f'execute {identities} run say AA_MATRIX_RESTART_IDENTITIES']
    for part in range(3):
        x = 10 + part
        restore += [
            f'setblock {x} {300 + part} 10 air destroy',
            f'execute if block {x} 300 10 air if block {x} 301 10 air if block {x} 302 10 air run say AA_MATRIX_CLEAN_COLUMN_{part}',
        ]
    neighbor = ' '.join(f'if block 10 {303 + part} 10 {block}[part={part}]' for part in range(3))
    count_key = 'count' if node.startswith('1.21') else 'Count'
    restore += [
        f'execute {neighbor} run say AA_MATRIX_NEIGHBOR_PRESERVED',
        f'tag @e[type=item,{area}] add {objective}',
        f'execute as @e[tag={objective}] run data merge entity @s {{NoGravity:1b,Motion:[0d,0d,0d],PickupDelay:32767s}}',
        f'scoreboard players set total {objective} 0',
        f'execute as @e[tag={objective},nbt={{Item:{{id:"{block}"}}}}] store result score @s {objective} run data get entity @s Item.{count_key}',
        f'execute as @e[tag={objective},nbt={{Item:{{id:"{block}"}}}}] run scoreboard players operation total {objective} += @s {objective}',
        f'execute if score total {objective} matches 3 run say AA_MATRIX_DROPS_CONSERVED',
        f'kill @e[tag={objective}]',
        'fill 9 298 9 13 307 11 air',
        f'execute {air} unless entity @e[type=item,{area}] run say AA_MATRIX_CLEANED',
    ]
    restore = [guard + command for command in restore] + [
        f'execute if score forced {objective} matches 0 run forceload remove 0 0',
        f'scoreboard objectives remove {objective}',
        'say AA_MATRIX_RESTART_FINISHED',
    ]
    return setup, restore


def run(node):
    preflight(node)
    objective = 'aa_m_' + uuid.uuid4().hex[:8]
    passed = True
    for commands in fixtures(node, objective):
        print(json.dumps({'node': node, 'objective': objective, 'commands': commands}), flush=True)
        markers = [command.split('run say ', 1)[-1] if 'run say ' in command else command[4:]
                   for command in commands if 'say AA_MATRIX_' in command]
        passed = smoke(node, commands=commands, markers=markers) and passed
    return passed


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('nodes', nargs='*')
    nodes = parser.parse_args().nodes or NODES
    if len(set(nodes)) != len(nodes) or any(node not in NODES for node in nodes):
        parser.error('Choose unique supported leaf names')
    for node in nodes:
        preflight(node)
    results = {node: run(node) for node in nodes}
    print(json.dumps(results), flush=True)
    raise SystemExit(0 if all(results.values()) else 1)
