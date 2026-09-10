"""Recover pinned Stormway trillion artwork."""
import argparse
from pathlib import Path
from port_parchtear_assets import generate

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    generate(parser.parse_args().upstream, ("yellow", "dun"), "trillion")
