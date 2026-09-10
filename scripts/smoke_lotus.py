"""Two native server sessions: Lotus placement, empty-tool loot, stack persistence and cleanup."""
import argparse
from port_lotus_assets import NAME
from smoke_servers import NODES
from smoke_unimplemented_devices import run

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("node", choices=NODES)
    raise SystemExit(0 if run(parser.parse_args().node, names=(NAME,), loot_tool="minecraft:air") else 1)
