#!/usr/bin/env python3
"""Real separate-process Gem Cutter save/restart and two-part removal fixtures.

Uses operator-approved isolated dev worlds and the existing bounded server runner.
Pending data is injected through operator commands: this does NOT test crafting,
item placement, menus, rendering, player delivery or automation.
"""
import argparse
from smoke_servers import NODES, preflight, smoke


def fixtures(node):
    unit = 'count:1' if node.startswith('1.21') else 'Count:1b'
    four = 'count:4' if node.startswith('1.21') else 'Count:4b'
    entity_data = 'Item.components."minecraft:block_entity_data"' if node.startswith('1.21') else 'Item.tag.BlockEntityTag'

    def inventory(size, item, count):
        return '{Size:' + str(size) + ',Items:[{Slot:0,ExtendedCount:' + str(count) + ',Stack:{id:"minecraft:' + item + '",' + unit + '}}]}'

    inputs = inventory(18, 'diamond', 4)
    pending = '{Version:1,Output:' + inventory(1, 'paper', 2) + ',Consumed:' + inventory(18, 'gold_ingot', 1) + '}'
    normal = '{Version:1,Inputs:' + inputs + ',HasPending:0b}'
    staged = '{Version:1,Inputs:' + inputs + ',HasPending:1b,Pending:' + pending + '}'
    region = 'type=minecraft:item,x=7,y=299,z=7,dx=6,dy=4,dz=5'
    table = '@e[' + region + ',nbt={Item:{id:"arcanearchives:gemcutters_table"}},limit=1]'
    setup = [
        'forceload add 0 0',
        'fill 7 299 7 13 299 12 minecraft:stone',
        'gamerule doTileDrops',
        'setblock 8 300 8 air', 'setblock 8 300 9 air',
        'setblock 11 300 8 air', 'setblock 11 300 9 air',
        'kill @e[' + region + ']',
        'setblock 8 300 8 arcanearchives:gemcutters_table[facing=north,accessor=false]',
        'setblock 8 300 9 arcanearchives:gemcutters_table[facing=north,accessor=true]',
        'data merge block 8 300 8 {Crafting:' + normal + '}',
        'setblock 11 300 8 arcanearchives:gemcutters_table[facing=north,accessor=false]',
        'setblock 11 300 9 arcanearchives:gemcutters_table[facing=north,accessor=true]',
        'data merge block 11 300 8 {Crafting:' + staged + '}',
        'execute if data block 8 300 8 Crafting.Inputs.Items[{Slot:0,ExtendedCount:4}] run say AA_GCT_INPUTS_SAVED',
        'execute if data block 11 300 8 Crafting.Pending.Output.Items[{ExtendedCount:2}] run say AA_GCT_PENDING_SAVED',
        'save-all flush', 'say AA_GCT_SETUP_DONE',
    ]
    restore = [
        'execute if block 8 300 9 arcanearchives:gemcutters_table[accessor=true] run say AA_GCT_ACCESSOR_RESTORED',
        'execute if data block 8 300 8 Crafting.Inputs.Items[{Slot:0,ExtendedCount:4}] run say AA_GCT_INPUTS_RESTORED',
        'execute if data block 11 300 8 Crafting.Pending.Output.Items[{ExtendedCount:2,Stack:{id:"minecraft:paper"}}] run say AA_GCT_OUTPUT_RESTORED',
        'execute if data block 11 300 8 Crafting.Pending.Consumed.Items[{ExtendedCount:1,Stack:{id:"minecraft:gold_ingot"}}] run say AA_GCT_CONSUMED_RESTORED',
        'setblock 8 300 9 air',
        'execute if block 8 300 8 air run say AA_GCT_MASTER_REMOVED',
        'execute if entity @e[' + region + ',nbt={Item:{id:"minecraft:diamond",' + four + '}}] run say AA_GCT_INPUTS_DROPPED',
        'execute if entity ' + table + ' run say AA_GCT_TABLE_DROPPED',
        'kill @e[' + region + ']',
        'setblock 11 300 8 air',
        'execute if block 11 300 9 air run say AA_GCT_ACCESSOR_REMOVED',
        'execute if data entity ' + table + ' ' + entity_data + '.Crafting.Pending.Output.Items[{ExtendedCount:2}] run say AA_GCT_PENDING_PACKED',
        'execute if data entity ' + table + ' ' + entity_data + '.Crafting.Inputs.Items[{ExtendedCount:4}] run say AA_GCT_INPUTS_PACKED',
        'execute unless entity @e[' + region + ',nbt={Item:{id:"minecraft:diamond"}}] unless entity @e[' + region + ',nbt={Item:{id:"minecraft:paper"}}] unless entity @e[' + region + ',nbt={Item:{id:"minecraft:gold_ingot"}}] run say AA_GCT_NO_LOOSE_PENDING',
        'kill @e[' + region + ']', 'fill 7 299 7 13 299 12 air', 'forceload remove 0 0', 'say AA_GCT_RESTART_DONE',
    ]
    return setup, restore


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('nodes', nargs='*')
    args = parser.parse_args()
    nodes = args.nodes or NODES
    if len(set(nodes)) != len(nodes) or any(node not in NODES for node in nodes):
        parser.error('Choose unique supported leaf names')
    for node in nodes:
        preflight(node)
    for node in nodes:
        for commands in fixtures(node):
            markers = [command.split('say ', 1)[1] for command in commands if 'say AA_GCT_' in command]
            if not smoke(node, commands=commands, markers=markers):
                return 1
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
