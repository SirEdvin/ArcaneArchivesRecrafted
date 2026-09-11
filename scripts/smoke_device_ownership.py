#!/usr/bin/env python3
"""Restart operator-seeded owned/unlinked devices in approved smoke worlds.

Not player placement or network acceptance. Preserves prior chunk holds and
cleans only guarded fixture space; prints recovery commands before starting.
"""
import argparse
import json
import uuid
from smoke_servers import NODES, preflight, smoke

AREA = 'x=48,y=298,z=48,dx=6,dy=6,dz=6'


def fixtures(node, objective):
    air = ' '.join(f'if block {x} {y} {z} air'
                   for x in range(48, 55) for y in range(298, 305) for z in range(48, 55))
    guard = f'execute if score ready {objective} matches 1 run '
    setup = [f'scoreboard objectives add {objective} dummy',
             f'execute store success score forced {objective} run forceload query 48 48',
             f'execute if score forced {objective} matches 0 run forceload add 48 48',
             f'scoreboard players set ready {objective} 0',
             f'execute {air} unless entity @e[{AREA}] run scoreboard players set ready {objective} 1']
    seed = ['say AA_DEVICE_SPACE']
    checks = []
    count = 'count:1' if node.startswith('1.21') else 'Count:1b'
    for index, owned in enumerate((True, False)):
        z = 50 + index * 2
        # Seed ordinary input/output inventory, not native-menu acquisition.
        diamonds = 1 if owned else 3
        crafting = ('{Inputs:{Size:18,Items:['
                    '{Slot:0,ExtendedCount:' + str(diamonds) + ',Stack:{id:"minecraft:diamond",' + count + '}},'
                    '{Slot:17,ExtendedCount:7,Stack:{id:"minecraft:emerald",' + count + '}}]}')
        if owned:
            creator = '{creator:[I;0,0,0,2],creator_name:"Restart creator"}'
            stamp = (',components:{"minecraft:custom_data":' + creator + '}'
                     if node.startswith('1.21') else ',tag:' + creator)
            crafting += (',Output:{Size:1,Items:['
                         '{Slot:0,ExtendedCount:4,Stack:{id:"minecraft:paper",' + count + stamp + '}}]}')
        else:
            crafting += ',Output:{Size:1,Items:[]}'
        crafting += '}'
        owner = 'network_owner:[I;0,0,0,1],' if owned else ''
        table = 'arcanearchives:gemcutters_table[facing=north,accessor=false]'
        accessor = 'arcanearchives:gemcutters_table[facing=north,accessor=true]'
        crystal = 'arcanearchives:monitoring_crystal[facing=up]'
        seed += [f'setblock 50 300 {z} {table}{{{owner}Crafting:{crafting}}}',
                 f'setblock 50 300 {z + 1} {accessor}',
                 f'setblock 52 300 {z} {crystal}{{{owner.rstrip(",")}}}']
        checks += [f'execute if block 50 300 {z} {table}{{Crafting:{crafting}}} '
                   f'if block 50 300 {z + 1} {accessor} run say AA_DEVICE_INPUTS_{index}']
        for x in (50, 52):
            predicate = (f'if data block {x} 300 {z} {{network_owner:[I;0,0,0,1]}}' if owned
                         else f'unless data block {x} 300 {z} network_owner')
            checks.append(f'execute {predicate} run say AA_DEVICE_OWNER_{index}_{x}')
        checks.append(f'execute if block 52 300 {z} {crystal} run say AA_DEVICE_CRYSTAL_{index}')
        checks += [f'execute store result score inputs {objective} run data get block 50 300 {z} Crafting.Inputs.Items',
                   f'execute if score inputs {objective} matches 2 run say AA_DEVICE_INPUT_SHAPE_{index}']
        checks += [f'execute store result score entries {objective} run data get block 50 300 {z} Crafting.Output.Items',
                   f'execute if score entries {objective} matches {1 if owned else 0} run say AA_DEVICE_OUTPUT_SHAPE_{index}',
                   f'execute unless data block 50 300 {z} Crafting.Pending unless data block 50 300 {z} Crafting.HasPending run say AA_DEVICE_NO_JOURNAL_{index}']
    checks.append(f'execute unless data block 50 300 {z} Crafting.Pending run say AA_DEVICE_IDLE_NO_PENDING')
    checks.append(f'execute unless entity @e[type=item,{AREA}] run say AA_DEVICE_NO_LOOSE_GRANTS')
    setup += [guard + command for command in seed + checks + ['save-all flush']]
    setup += ['say AA_DEVICE_SETUP_FINISHED']
    cleanup = checks + ['fill 48 298 48 54 304 54 air', f'kill @e[type=item,{AREA}]',
                        f'execute {air} unless entity @e[type=item,{AREA}] run say AA_DEVICE_CLEANED']
    restore = [guard + command for command in cleanup]
    restore += [f'execute if score forced {objective} matches 0 run forceload remove 48 48',
                f'scoreboard objectives remove {objective}', 'save-all flush', 'say AA_DEVICE_RESTART_FINISHED']
    return setup, restore


def run(node):
    preflight(node)
    objective = 'aa_o_' + uuid.uuid4().hex[:8]
    sessions = fixtures(node, objective)
    print(json.dumps({'node': node, 'objective': objective, 'sessions_and_recovery': sessions}), flush=True)
    passed = True
    for commands in sessions:
        markers = [c.split('run say ', 1)[-1] if 'run say ' in c else c[4:]
                   for c in commands if 'say AA_DEVICE_' in c]
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
