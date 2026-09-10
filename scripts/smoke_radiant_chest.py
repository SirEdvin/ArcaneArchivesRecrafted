#!/usr/bin/env python3
"""Real hopper, extended-inventory restart and break conservation checks.

Operator commands seed storage/visual metadata, not player interactions. Uses
only guarded air in ignored, loopback, EULA-approved worlds. Both sessions run
so assertion failure still gets normal fixture cleanup. A process crash may
leave fixtures: the objective and complete commands are printed for recovery.
"""
import argparse
import json
import uuid
from smoke_servers import NODES, ROOT, preflight, smoke


def fixtures(node, objective):
    modern = node.startswith("1.21")
    count_key = "count" if modern else "Count"
    unit = "count:1" if modern else "Count:1b"
    custom = 'components:{"minecraft:custom_data":{aa_chest_probe:1b}}' if modern else 'tag:{aa_chest_probe:1b}'
    stack = '{id:"minecraft:paper",' + unit + ',' + custom + '}'
    paper = '{Slot:53,ExtendedCount:130,Stack:' + stack + '}'
    diamond = '{Slot:0,ExtendedCount:255,Stack:{id:"minecraft:diamond",' + unit + '}}'
    state = '{inventory:{Size:54,Items:[' + diamond + ',' + paper + ']},owner:[I;1,2,3,4],chestName:"AA chest fixture",displayFacing:5,displayStack:' + stack + '}'
    area = 'x=3,y=298,z=3,dx=4,dy=4,dz=4'
    air = ' '.join(f'if block {x} {y} {z} air' for x in range(3, 8) for y in range(298, 303) for z in range(3, 8))
    guard = f'execute if score ready {objective} matches 1 run '
    setup = [
        f'scoreboard objectives add {objective} dummy',
        f'execute store success score forced {objective} run forceload query 0 0',
        f'execute if score forced {objective} matches 0 run forceload add 0 0',
        f'scoreboard players set ready {objective} 0',
        f'execute {air} unless entity @e[{area}] run scoreboard players set ready {objective} 1',
    ]
    seeded = [
        'say AA_CHEST_SPACE',
        'fill 3 298 3 7 298 7 stone',
        'setblock 5 300 5 arcanearchives:radiant_chest' + state,
        'setblock 5 301 5 hopper[facing=down]',
        'item replace block 5 301 5 container.0 with diamond 2',
        # Bounded observation interval; vanilla hopper ticks perform the transfers.
        *['data get block 5 300 5 inventory' for _ in range(12)],
        'execute unless data block 5 301 5 Items[0] if data block 5 300 5 inventory.Items[{Slot:0,ExtendedCount:256,Stack:{id:"minecraft:diamond"}}] if data block 5 300 5 inventory.Items[{Slot:1,ExtendedCount:1,Stack:{id:"minecraft:diamond"}}] run say AA_CHEST_HOPPER_CAPACITY',
        'execute if data block 5 300 5 inventory.Items[' + paper + '] run say AA_CHEST_LAST_SLOT',
        'save-all flush',
    ]
    setup += [guard + command for command in seeded] + ['say AA_CHEST_SETUP_FINISHED']
    restored = [
        'execute if data block 5 300 5 inventory.Items[{Slot:0,ExtendedCount:256,Stack:{id:"minecraft:diamond"}}] if data block 5 300 5 inventory.Items[{Slot:1,ExtendedCount:1,Stack:{id:"minecraft:diamond"}}] run say AA_CHEST_RESTART_COUNTS',
        'execute if data block 5 300 5 inventory.Items[' + paper + '] run say AA_CHEST_RESTART_COMPONENTS',
        'execute if data block 5 300 5 {owner:[I;1,2,3,4],chestName:"AA chest fixture",displayFacing:5,displayStack:' + stack + '} run say AA_CHEST_RESTART_METADATA',
        'setblock 5 299 5 hopper[facing=north]',
        *['data get block 5 299 5 Items' for _ in range(8)],
        'setblock 4 299 5 redstone_block',
        'execute if block 5 299 5 hopper[enabled=false] if data block 5 299 5 Items[{id:"minecraft:diamond"}] run say AA_CHEST_HOPPER_EXTRACT',
        f'execute store result score remaining {objective} run data get block 5 300 5 inventory.Items[{{Slot:0}}].ExtendedCount',
        f'execute store result score next {objective} run data get block 5 300 5 inventory.Items[{{Slot:1}}].ExtendedCount',
        f'execute store result score extracted {objective} run data get block 5 299 5 Items[0].{count_key}',
        f'scoreboard players operation remaining {objective} += next {objective}',
        f'scoreboard players operation remaining {objective} += extracted {objective}',
        f'execute if score remaining {objective} matches 257 run say AA_CHEST_EXTRACTION_CONSERVED',
        'setblock 5 301 5 air',
        # Vanilla setblock replace clears container inventories; destroy exercises drops.
        'setblock 5 299 5 air destroy',
        'setblock 4 299 5 air',
        'setblock 5 300 5 air destroy',
        f'tag @e[type=item,{area}] add {objective}',
        f'execute as @e[tag={objective}] run data merge entity @s {{NoGravity:1b,Motion:[0d,0d,0d],PickupDelay:32767s}}',
        f'execute as @e[tag={objective}] store result score @s {objective} run data get entity @s Item.{count_key}',
    ]
    for item, expected in (("diamond", 257), ("paper", 130), ("radiant_chest", 1), ("hopper", 1)):
        namespace = 'arcanearchives' if item == 'radiant_chest' else 'minecraft'
        restored += [
            f'scoreboard players set {item} {objective} 0',
            f'execute as @e[tag={objective},nbt={{Item:{{id:"{namespace}:{item}"}}}}] run scoreboard players operation {item} {objective} += @s {objective}',
            f'execute if score {item} {objective} matches {expected} run say AA_CHEST_DROP_{item.upper()}',
        ]
    restored += [
        f'execute unless entity @e[tag={objective},scores={{{objective}=65..}}] run say AA_CHEST_NATIVE_STACK_LIMITS',
        f'execute unless entity @e[tag={objective},nbt={{Item:{{id:"minecraft:paper"}}}},nbt=!{{Item:{{{custom}}}}}] run say AA_CHEST_DROP_COMPONENTS',
        f'kill @e[tag={objective}]',
        'fill 3 298 3 7 298 7 air',
        f'execute {air} unless entity @e[type=item,{area}] run say AA_CHEST_CLEANED',
    ]
    restore = [guard + command for command in restored] + [
        f'execute if score forced {objective} matches 0 run forceload remove 0 0',
        f'scoreboard objectives remove {objective}',
        'say AA_CHEST_RESTART_FINISHED',
    ]
    return setup, restore


def run(node):
    preflight(node)
    config = ROOT / 'versions' / node / 'runs/server/config/arcanearchives/server.properties'
    if config.exists():
        values = dict(line.split('=', 1) for line in config.read_text().splitlines() if '=' in line and not line.startswith('#'))
        if values.get('RadiantMultiplier', '4').strip() != '4':
            raise ValueError('Fixture requires RadiantMultiplier=4; configuration was not changed')
    objective = 'aa_c_' + uuid.uuid4().hex[:8]
    passed = True
    for commands in fixtures(node, objective):
        print(json.dumps({'node': node, 'objective': objective, 'commands': commands}), flush=True)
        markers = [command.split('run say ', 1)[-1] if 'run say ' in command else command[4:]
                   for command in commands if 'say AA_CHEST_' in command]
        passed = smoke(node, commands=commands, markers=markers) and passed
    return passed


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('node', choices=NODES)
    raise SystemExit(0 if run(parser.parse_args().node) else 1)
