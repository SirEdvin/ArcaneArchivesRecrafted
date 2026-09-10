"""Native placement, loot, item and restart checks for all five registered prototype devices."""
import argparse
import uuid
from port_unimplemented_device_assets import NAMES
from smoke_servers import NODES, preflight, smoke


def run(node, names=NAMES, loot_tool="minecraft:wooden_pickaxe"):
    preflight(node)
    objective = "aa_p_" + uuid.uuid4().hex[:8]
    guard = f"execute if score ready {objective} matches 1 run "
    count = "count:1" if node.startswith("1.21") else "Count:1b"
    full = "count:64" if node.startswith("1.21") else "Count:64b"
    chest = len(names)
    air = " ".join(f"if block {x} 300 0 air" for x in range(chest + 1))
    commands = [
        f"scoreboard objectives add {objective} dummy",
        f"execute store success score forced {objective} run forceload query 0 0",
        f"execute if score forced {objective} matches 0 run forceload add 0 0",
        f"scoreboard players set ready {objective} 0",
        f"execute {air} run scoreboard players set ready {objective} 1",
        guard + "say AA_PROTO_SPACE",
        guard + f"setblock {chest} 300 0 chest",
    ]
    markers = ["AA_PROTO_SPACE"]
    for x, name in enumerate(names):
        placed, tool, loot, item = (f"AA_PROTO_{x}_{part}" for part in ("PLACED", "TOOL", "LOOT", "ITEM"))
        commands += [guard + command for command in (
            f"setblock {x} 300 0 arcanearchives:{name}",
            f"execute if block {x} 300 0 arcanearchives:{name} run say {placed}",
            f"execute if block {x} 300 0 #minecraft:mineable/pickaxe run say {tool}",
            f"loot insert {chest} 300 0 mine {x} 300 0 {loot_tool}",
            f'execute if data block {chest} 300 0 Items[{{Slot:0b,id:"arcanearchives:{name}",{count}}}] run say {loot}',
            f"item replace block {chest} 300 0 container.0 with air",
            f"item replace block {chest} 300 0 container.{x + 1} with arcanearchives:{name} 64",
            f'execute if data block {chest} 300 0 Items[{{Slot:{x + 1}b,id:"arcanearchives:{name}",{full}}}] run say {item}',
        )]
        markers += [placed, tool, loot, item]
    commands += [guard + "save-all flush",
                 f"execute if score ready {objective} matches 0 if score forced {objective} matches 0 run forceload remove 0 0",
                 "say AA_PROTO_FIRST_FINISHED"]
    markers.append("AA_PROTO_FIRST_FINISHED")
    if not smoke(node, commands=commands, markers=tuple(markers)):
        return False

    commands, markers = [], []
    for x, name in enumerate(names):
        marker = f"AA_PROTO_{x}_RESTART"
        commands.append(guard + f'execute if block {x} 300 0 arcanearchives:{name} if data block {chest} 300 0 Items[{{Slot:{x + 1}b,id:"arcanearchives:{name}",{full}}}] run say {marker}')
        markers.append(marker)
    commands += [guard + f'data merge block {chest} 300 0 {{Items:[]}}']
    commands += [guard + f"setblock {x} 300 0 air" for x in range(chest + 1)]
    commands += [f"execute {air} run say AA_PROTO_AIR_RESTORED",
                 f"execute if score forced {objective} matches 0 run forceload remove 0 0",
                 f"scoreboard objectives remove {objective}", "say AA_PROTO_FINISHED"]
    markers += ["AA_PROTO_AIR_RESTORED", "AA_PROTO_FINISHED"]
    return smoke(node, commands=commands, markers=tuple(markers))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("node", choices=NODES)
    raise SystemExit(0 if run(parser.parse_args().node) else 1)
