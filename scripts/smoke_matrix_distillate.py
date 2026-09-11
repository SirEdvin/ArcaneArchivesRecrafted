#!/usr/bin/env python3
"""Check Distillate part identity persistence across full server-process restarts.

Operator-seeded states, not player placement. Every footprint chunk is held loaded
through removal (0124). Reuses approved loopback/EULA worlds without deleting them.
Printed commands include recovery cleanup; cleanup runs even after failed setup.
"""
import argparse
import json
import uuid
from smoke_servers import NODES, preflight, smoke


BLOCK = 'arcanearchives:matrix_distillate'
AREA = 'x=29,y=298,z=29,dx=4,dy=17,dz=4'
CHUNKS = ((16, 16), (16, 32), (32, 16), (32, 32))


def footprints():
    # Native MatrixDistillate.offset: root replaces cell 1, part 1 is cell 0;
    # horizontal cell offsets follow facing.getOpposite().
    result = []
    for index, (facing, dx, dz) in enumerate((('east', -1, 0), ('west', 1, 0),
                                              ('north', 0, 1), ('south', 0, -1))):
        positions = []
        for part in range(9):
            cell = 1 if part == 0 else 0 if part == 1 else part
            positions.append((31 + dx * (cell % 3 - 1), 300 + index * 4 + cell // 3,
                              31 + dz * (cell % 3 - 1), part))
        result.append((facing, positions))
    return result


def fixtures(node, objective):
    air = ' '.join(f'if block {x} {y} {z} air'
                   for x in range(29, 34) for y in range(298, 316) for z in range(29, 34))
    guard = f'execute if score ready {objective} matches 1 run '
    setup = [f'scoreboard objectives add {objective} dummy']
    for index, (x, z) in enumerate(CHUNKS):
        setup += [f'execute store success score forced{index} {objective} run forceload query {x} {z}',
                  f'execute if score forced{index} {objective} matches 0 run forceload add {x} {z}']
    setup += [f'scoreboard players set ready {objective} 0',
              f'execute {air} unless entity @e[{AREA}] run scoreboard players set ready {objective} 1']
    seed = ['say AA_DISTILLATE_SPACE', 'fill 29 298 29 33 298 33 stone']
    restore = []
    shapes = footprints()
    for index, (facing, positions) in enumerate(shapes):
        identity = uuid.uuid4().int
        words = [(identity >> shift) & 0xffffffff for shift in (96, 64, 32, 0)]
        nbt = '{matrix_identity:[I;' + ','.join(str(w if w < 2**31 else w - 2**32) for w in words) + ']}'
        checks = []
        for x, y, z, part in positions:
            state = f'{BLOCK}[facing={facing},part={part}]'
            seed.append(f'setblock {x} {y} {z} {state}{nbt}')
            checks.append(f'if block {x} {y} {z} {state}{nbt}')
        predicate = ' '.join(checks)
        seed.append(f'execute {predicate} run say AA_DISTILLATE_SEEDED_{index}')
        restore.append(f'execute {predicate} run say AA_DISTILLATE_RESTART_IDENTITY_{index}')
    seed.append('save-all flush')
    setup += [guard + command for command in seed] + ['say AA_DISTILLATE_SETUP_FINISHED']
    # Both sides of each X/Z boundary remain loaded. Remove a root, lower child,
    # upper child and another root in distinct restored orientations.
    for index, (_, positions) in enumerate(shapes):
        x, y, z, _ = positions[(0, 1, 8, 0)[index]]
        empty = ' '.join(f'if block {px} {py} {pz} air' for px, py, pz, _ in positions)
        restore += [f'setblock {x} {y} {z} air destroy',
                    f'execute {empty} run say AA_DISTILLATE_REMOVED_{index}',
                    f'tag @e[type=item,{AREA}] add {objective}',
                    f'execute as @e[tag={objective}] run data merge entity @s {{NoGravity:1b,Motion:[0d,0d,0d],PickupDelay:32767s}}']
    count = 'count' if node.startswith('1.21') else 'Count'
    restore += [f'scoreboard players set total {objective} 0',
                f'execute as @e[tag={objective},nbt={{Item:{{id:"{BLOCK}"}}}}] store result score @s {objective} run data get entity @s Item.{count}',
                f'execute as @e[tag={objective},nbt={{Item:{{id:"{BLOCK}"}}}}] run scoreboard players operation total {objective} += @s {objective}',
                f'execute if score total {objective} matches 4 run say AA_DISTILLATE_DROPS_CONSERVED',
                f'kill @e[tag={objective}]',
                'fill 29 298 29 33 315 33 air',
                f'execute {air} unless entity @e[type=item,{AREA}] run say AA_DISTILLATE_CLEANED']
    restore = [guard + command for command in restore]
    for index, (x, z) in enumerate(CHUNKS):
        restore.append(f'execute if score forced{index} {objective} matches 0 run forceload remove {x} {z}')
    restore += [f'scoreboard objectives remove {objective}', 'save-all flush', 'say AA_DISTILLATE_RESTART_FINISHED']
    return setup, restore


def run(node):
    preflight(node)
    objective = 'aa_d_' + uuid.uuid4().hex[:8]
    sessions = fixtures(node, objective)
    print(json.dumps({'node': node, 'objective': objective, 'sessions_and_recovery': sessions}), flush=True)
    passed = True
    for commands in sessions:
        markers = [command.split('run say ', 1)[-1] if 'run say ' in command else command[4:]
                   for command in commands if 'say AA_DISTILLATE_' in command]
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
