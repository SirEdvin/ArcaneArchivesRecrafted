"""Two real server sessions per leaf: Wonky Resonator timer, loot and restart persistence.

Uses the existing loopback/EULA preflight and isolated ignored development worlds.
No existing block is replaced unless the initial fixture space is entirely air.
"""
import argparse
from smoke_servers import NODES, ROOT, preflight, smoke


def run(node):
    preflight(node)
    config = ROOT / "versions" / node / "runs/server/config/arcanearchives/server.properties"
    if config.exists():
        values = dict(line.split("=", 1) for line in config.read_text().splitlines() if "=" in line and not line.startswith("#"))
        if values.get("ResonatorTickTime", "6000").strip() != "6000":
            raise ValueError("This fixture requires the default ResonatorTickTime=6000; configuration was not changed")
    guarded = [
        "setblock 0 301 0 stone",
        "setblock 0 300 0 arcanearchives:wonky_resonator{current_tick:5999}",
        'execute if data block 0 300 0 {current_tick:5999} run say AA_WONKY_BLOCKED',
        "execute if block 0 300 0 #minecraft:mineable/pickaxe run say AA_WONKY_TOOL",
        "setblock 0 301 0 air",
        "execute store result score timer aa_wonky run data get block 0 300 0 current_tick",
        "execute if score timer aa_wonky matches 1..100 run say AA_WONKY_RESET",
        "execute if block 0 301 0 air if block 0 300 0 arcanearchives:wonky_resonator run say AA_WONKY_NO_EFFECT",
        "setblock 1 300 0 chest",
        "loot insert 1 300 0 mine 0 300 0 minecraft:wooden_pickaxe",
        'execute if data block 1 300 0 Items[{id:"arcanearchives:wonky_resonator"}] run say AA_WONKY_LOOT',
        "item replace block 1 300 0 container.1 with arcanearchives:wonky_resonator 1",
        'execute if data block 1 300 0 Items[{Slot:1b,id:"arcanearchives:wonky_resonator"}] run say AA_WONKY_ITEM',
        "setblock 0 301 0 stone",
        "data merge block 0 300 0 {current_tick:777}",
        'execute if data block 0 300 0 {current_tick:777} run say AA_WONKY_SAVED_STATE',
        "save-all flush",
    ]
    commands = [
        "forceload add 0 0",
        "scoreboard objectives add aa_wonky dummy",
        "scoreboard players set ready aa_wonky 0",
        "execute if block 0 300 0 air if block 0 301 0 air if block 1 300 0 air run scoreboard players set ready aa_wonky 1",
        *["execute if score ready aa_wonky matches 1 run " + command for command in guarded],
        "execute if score ready aa_wonky matches 0 run forceload remove 0 0",
        "say AA_WONKY_FIRST_FINISHED",
    ]
    markers = tuple("AA_WONKY_" + name for name in (
        "BLOCKED", "TOOL", "RESET", "NO_EFFECT", "LOOT", "ITEM", "SAVED_STATE", "FIRST_FINISHED"))
    if not smoke(node, commands=commands, markers=markers):
        return False
    commands = [
        'execute if data block 0 300 0 {current_tick:777} if block 0 301 0 stone run say AA_WONKY_RESTART',
        "setblock 0 300 0 air",
        "setblock 0 301 0 air",
        "setblock 1 300 0 air",
        "forceload remove 0 0",
        "scoreboard objectives remove aa_wonky",
        "say AA_WONKY_CLEANED",
    ]
    return smoke(node, commands=commands, markers=("AA_WONKY_RESTART", "AA_WONKY_CLEANED"))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("node", choices=NODES)
    raise SystemExit(0 if run(parser.parse_args().node) else 1)
