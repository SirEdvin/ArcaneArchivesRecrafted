#!/usr/bin/env python3
"""Start each real loader server, require readiness, then stop and save cleanly.

Requires Python 3.11+, GNU timeout, and explicitly accepted EULAs in ignored
versions/<leaf>/runs/server directories. Never accepts legal terms automatically.
This is bootstrap smoke coverage, not GameTests or gameplay parity verification.
"""

import argparse
import datetime
import json
import os
from pathlib import Path
import re
import signal
import subprocess
import time


ROOT = Path(__file__).resolve().parents[1]
NODES = ("1.20.1-fabric", "1.20.1-forge", "1.21.1-fabric", "1.21.1-neoforge")


def preflight(node):
    directory = ROOT / "versions" / node / "runs/server"
    eula = directory / "eula.txt"
    properties = directory / "server.properties"
    if not eula.is_file() or not re.search(r"^eula=true\s*$", eula.read_text(), re.M):
        raise SystemExit(f"{node}: EULA not accepted; ask the operator, never accept automatically")
    if not properties.is_file() or not re.search(
        r"^server-ip=127\.0\.0\.1\s*$", properties.read_text(), re.M
    ):
        raise SystemExit(f"{node}: set server-ip=127.0.0.1 in {properties} before smoke testing")


def smoke(node, quartz=False, commands=None, markers=()):
    if commands is not None and (quartz or not markers):
        raise ValueError("Custom fixtures require completion markers and cannot combine with quartz")
    stamp = datetime.datetime.now(datetime.timezone.utc).strftime("%Y%m%d-%H%M%S-%f")
    log = ROOT / "build" / f"smoke-server-{node}-{stamp}.log"
    log.parent.mkdir(exist_ok=True)
    command = ["timeout", "--foreground", "10m", "./gradlew", f":{node}:runServer", "--no-daemon", "--console=plain"]
    started = time.monotonic()
    requested_stop = False
    requested_reload = False
    sent_fixture = False
    command_index = 0
    timed_out = False
    process = None
    try:
        with log.open("w") as output:
            process = subprocess.Popen(
                command, cwd=ROOT, stdin=subprocess.PIPE, stdout=output,
                stderr=subprocess.STDOUT, text=True, start_new_session=True,
            )
            assert process.stdin is not None
            while process.poll() is None:
                text = log.read_text(errors="replace")
                if not requested_stop and re.search(r'Done \([\d.]+s\)! For help, type "help"', text):
                    if commands is not None:
                        if not sent_fixture:
                            # Give chunk/entity lifecycle updates ticks between fixture commands.
                            process.stdin.write(commands[command_index] + "\n")
                            command_index += 1
                            sent_fixture = command_index == len(commands)
                        elif f"[Server] {markers[-1]}" in text:
                            process.stdin.write("stop\n")
                            requested_stop = True
                    elif not quartz:
                        process.stdin.write("stop\n")
                        requested_stop = True
                    elif not requested_reload:
                        process.stdin.write("reload\n")
                        requested_reload = True
                    elif not sent_fixture and len(re.findall(r"Loaded \d+ recipes", text)) >= 2:
                        # Only the isolated ignored development world is modified.
                        count = "count:1" if node.startswith("1.21") else "Count:1b"
                        full_stack = "count:64" if node.startswith("1.21") else "Count:64b"
                        process.stdin.write("\n".join([
                            "forceload add 0 0",
                            "setblock 0 300 0 arcanearchives:storage_shaped_quartz",
                            "execute if block 0 300 0 arcanearchives:storage_shaped_quartz run say AA_QUARTZ_PLACED",
                            "execute if block 0 300 0 #minecraft:mineable/pickaxe run say AA_QUARTZ_MINING_TAG",
                            "setblock 1 300 0 air",
                            "setblock 1 300 0 minecraft:chest",
                            "loot insert 1 300 0 mine 0 300 0 minecraft:wooden_pickaxe",
                            'execute if data block 1 300 0 Items[{id:"arcanearchives:storage_shaped_quartz",' + count + '}] run say AA_QUARTZ_LOOT',
                            "item replace block 1 300 0 container.1 with arcanearchives:shaped_quartz 1",
                            'execute if data block 1 300 0 Items[{id:"arcanearchives:shaped_quartz",' + count + '}] run say AA_QUARTZ_ITEM',
                            "item replace block 1 300 0 container.2 with patchouli:guide_book 1",
                            'execute if data block 1 300 0 Items[{id:"patchouli:guide_book",' + count + '}] run say AA_PATCHOULI_ITEM',
                            "item replace block 1 300 0 container.3 with arcanearchives:scintillating_inlay 64",
                            'execute if data block 1 300 0 Items[{Slot:3b,id:"arcanearchives:scintillating_inlay",' + full_stack + '}] run say AA_INLAY_STACK',
                            "item replace block 1 300 0 container.4 with arcanearchives:radiant_dust 64",
                            'execute if data block 1 300 0 Items[{Slot:4b,id:"arcanearchives:radiant_dust",' + full_stack + '}] run say AA_DUST_STACK',
                            "item replace block 1 300 0 container.5 with arcanearchives:empowered_quartz 64",
                            'execute if data block 1 300 0 Items[{Slot:5b,id:"arcanearchives:empowered_quartz",' + full_stack + '}] run say AA_EMPOWERED_STACK',
                            "item replace block 1 300 0 container.6 with arcanearchives:material_interface 64",
                            'execute if data block 1 300 0 Items[{Slot:6b,id:"arcanearchives:material_interface",' + full_stack + '}] run say AA_MATERIAL_INTERFACE_STACK',
                            "item replace block 1 300 0 container.7 with arcanearchives:containment_field 64",
                            'execute if data block 1 300 0 Items[{Slot:7b,id:"arcanearchives:containment_field",' + full_stack + '}] run say AA_CONTAINMENT_FIELD_STACK',
                            "setblock 0 300 0 air",
                            "item replace block 1 300 0 container.8 with arcanearchives:matrix_brace 64",
                            'execute if data block 1 300 0 Items[{Slot:8b,id:"arcanearchives:matrix_brace",' + full_stack + '}] run say AA_MATRIX_BRACE_STACK',
                            "setblock 1 300 0 air",
                            "setblock 2 300 0 minecraft:chest",
                            "item replace block 2 300 0 container.0 with arcanearchives:raw_quartz 64",
                            'execute if data block 2 300 0 Items[{Slot:0b,id:"arcanearchives:raw_quartz",' + full_stack + '}] run say AA_RAW_QUARTZ_STACK',
                            "setblock 2 300 0 air",
                            "setblock 3 300 0 arcanearchives:storage_raw_quartz",
                            "execute if block 3 300 0 #minecraft:mineable/pickaxe run say AA_RAW_STORAGE_MINING",
                            "setblock 2 300 0 minecraft:chest",
                            "loot insert 2 300 0 mine 3 300 0 minecraft:wooden_pickaxe",
                            'execute if data block 2 300 0 Items[{id:"arcanearchives:storage_raw_quartz",' + count + '}] run say AA_RAW_STORAGE_LOOT',
                            "setblock 3 300 0 air",
                            "setblock 2 300 0 air",
                            *[f"setblock {x} 300 0 arcanearchives:radiant_lantern[facing={face}]"
                              for x, face in enumerate(("up", "down", "north", "south", "east", "west"), 4)],
                            *[f"execute if block {x} 300 0 arcanearchives:radiant_lantern[facing={face}] run say AA_LANTERN_{face.upper()}"
                              for x, face in enumerate(("up", "down", "north", "south", "east", "west"), 4)],
                            "setblock 2 300 0 minecraft:chest",
                            "loot insert 2 300 0 mine 4 300 0 minecraft:wooden_pickaxe",
                            'execute if data block 2 300 0 Items[{id:"arcanearchives:radiant_lantern",' + count + '}] run say AA_LANTERN_LOOT',
                            *[f"setblock {x} 300 0 arcanearchives:quartz_sliver[facing={face}]"
                              for x, face in enumerate(("up", "down", "north", "south", "east", "west"), 4)],
                            *[f"execute if block {x} 300 0 arcanearchives:quartz_sliver[facing={face}] run say AA_SLIVER_{face.upper()}"
                              for x, face in enumerate(("up", "down", "north", "south", "east", "west"), 4)],
                            "loot insert 2 300 0 mine 4 300 0 minecraft:stick",
                            'execute if data block 2 300 0 Items[{id:"arcanearchives:quartz_sliver",' + count + '}] run say AA_SLIVER_LOOT',
                            *[f"setblock {x} 300 0 arcanearchives:raw_quartz_cluster[facing={face}]"
                              for x, face in enumerate(("up", "down", "north", "south", "east", "west"), 4)],
                            *[f"execute if block {x} 300 0 arcanearchives:raw_quartz_cluster[facing={face}] run say AA_CLUSTER_{face.upper()}"
                              for x, face in enumerate(("up", "down", "north", "south", "east", "west"), 4)],
                            "execute if block 4 300 0 #minecraft:mineable/pickaxe run say AA_CLUSTER_MINING",
                            "setblock 2 300 0 air",
                            "setblock 2 300 0 minecraft:chest",
                            "loot insert 2 300 0 mine 4 300 0 minecraft:wooden_pickaxe",
                            'execute if data block 2 300 0 Items[{Slot:0b,id:"arcanearchives:raw_quartz",' + count + '}] unless data block 2 300 0 Items[{Slot:1b}] run say AA_CLUSTER_RAW',
                            "setblock 2 300 0 air",
                            "setblock 2 300 0 minecraft:chest",
                            'loot insert 2 300 0 mine 4 300 0 minecraft:diamond_pickaxe' + (
                                '[minecraft:enchantments={levels:{"minecraft:fortune":3}}]' if node.startswith("1.21") else '{Enchantments:[{id:"minecraft:fortune",lvl:3s}]}'),
                            'execute if data block 2 300 0 Items[{Slot:0b,id:"arcanearchives:raw_quartz",' + count + '}] unless data block 2 300 0 Items[{Slot:1b}] run say AA_CLUSTER_FORTUNE',
                            "setblock 2 300 0 air",
                            "setblock 2 300 0 minecraft:chest",
                            'loot insert 2 300 0 mine 4 300 0 minecraft:diamond_pickaxe' + (
                                '[minecraft:enchantments={levels:{"minecraft:silk_touch":1}}]' if node.startswith("1.21") else '{Enchantments:[{id:"minecraft:silk_touch",lvl:1s}]}'),
                            'execute if data block 2 300 0 Items[{Slot:0b,id:"arcanearchives:raw_quartz_cluster",' + count + '}] unless data block 2 300 0 Items[{Slot:1b}] run say AA_CLUSTER_SILK',
                            "setblock 2 300 0 air",
                            *[f"setblock {x} 300 0 air" for x in range(4, 10)],
                            "forceload remove 0 0",
                            "say AA_QUARTZ_TESTS_FINISHED",
                        ]) + "\n")
                        sent_fixture = True
                    elif sent_fixture and "[Server] AA_QUARTZ_TESTS_FINISHED" in text:
                        process.stdin.write("stop\n")
                        requested_stop = True
                    process.stdin.flush()
                if time.monotonic() - started > 610:
                    timed_out = True
                    break
                time.sleep(0.25)
    finally:
        if process is not None:
            # Also clean up descendants when Gradle/timeout exits before its JVM.
            try:
                os.killpg(process.pid, signal.SIGTERM)
            except ProcessLookupError:
                pass
            try:
                process.wait(timeout=15)
            except subprocess.TimeoutExpired:
                os.killpg(process.pid, signal.SIGKILL)
                process.wait(timeout=15)
            if process.stdin:
                process.stdin.close()
    text = log.read_text(errors="replace")
    checks = {
        "mod_initialized": "Arcane Archives Recrafted bootstrap initialized on " in text,
        "server_ready": requested_stop,
        "server_stopped": "Stopping server" in text,
        "chunks_saved": "All dimensions are saved" in text,
        "mod_pack_present": not re.search(r"Missing (?:metadata in|data) pack mod:arcanearchives", text),
        "mixin_minimum_declared": not re.search(r'Mixin config arcanearchives[^\s]* does not specify "minVersion"', text),
        "no_timeout": not timed_out and process.returncode != 124,
    }
    checks.update({marker: f"[Server] {marker}" in text for marker in markers})
    if quartz:
        checks.update({
            "patchouli_item_registered": "[Server] AA_PATCHOULI_ITEM" in text,
            "inlay_full_stack_registered": "[Server] AA_INLAY_STACK" in text,
            "dust_full_stack_registered": "[Server] AA_DUST_STACK" in text,
            "empowered_full_stack_registered": "[Server] AA_EMPOWERED_STACK" in text,
            "material_interface_registered": "[Server] AA_MATERIAL_INTERFACE_STACK" in text,
            "containment_field_registered": "[Server] AA_CONTAINMENT_FIELD_STACK" in text,
            "matrix_brace_registered": "[Server] AA_MATRIX_BRACE_STACK" in text,
            **{"lantern_" + check.lower(): f"[Server] AA_LANTERN_{check}" in text
               for check in ("UP", "DOWN", "NORTH", "SOUTH", "EAST", "WEST", "LOOT")},
            **{"sliver_" + check.lower(): f"[Server] AA_SLIVER_{check}" in text
               for check in ("UP", "DOWN", "NORTH", "SOUTH", "EAST", "WEST", "LOOT")},
            **{"cluster_" + check.lower(): f"[Server] AA_CLUSTER_{check}" in text
               for check in ("UP", "DOWN", "NORTH", "SOUTH", "EAST", "WEST", "MINING", "RAW", "FORTUNE", "SILK")},
            "raw_quartz_registered": "[Server] AA_RAW_QUARTZ_STACK" in text,
            "raw_storage_mining_tag": "[Server] AA_RAW_STORAGE_MINING" in text,
            "raw_storage_loot": "[Server] AA_RAW_STORAGE_LOOT" in text,
            "recipes_reloaded": len(re.findall(r"Loaded \d+ recipes", text)) >= 2,
            "no_quartz_resource_errors": not any(
                "arcanearchives" in line and re.search(r"ERROR|Couldn't|Failed to|Parsing error", line)
                for line in text.splitlines()
            ),
            **{marker.lower(): f"[Server] AA_QUARTZ_{marker}" in text
               for marker in ("PLACED", "MINING_TAG", "LOOT", "ITEM", "TESTS_FINISHED")},
        })
    result = {
        "node": node, "command": command, "exit_code": process.returncode,
        "duration_seconds": round(time.monotonic() - started, 2),
        "log": str(log.relative_to(ROOT)), "checks": checks,
        "passed": process.returncode == 0 and all(checks.values()),
    }
    log.with_suffix(".json").write_text(json.dumps(result, indent=2) + "\n")
    print(json.dumps(result), flush=True)
    return result["passed"]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("nodes", nargs="*", help="Leaf names; defaults to all four")
    parser.add_argument("--quartz", action="store_true", help="Reload data and exercise quartz placement/loot in the isolated dev world")
    args = parser.parse_args()
    nodes = args.nodes or NODES
    if len(set(nodes)) != len(nodes) or any(node not in NODES for node in nodes):
        parser.error(f"choose unique leaf names from {', '.join(NODES)}")
    for node in nodes:
        preflight(node)
    passed = True
    for node in nodes:
        passed = smoke(node, quartz=args.quartz) and passed
    return 0 if passed else 1


if __name__ == "__main__":
    raise SystemExit(main())
