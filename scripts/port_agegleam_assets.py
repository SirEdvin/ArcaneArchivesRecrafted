"""Recover pinned Agegleam asscher models, animation and accessibility resources."""
import argparse
from pathlib import Path
from port_parchtear_assets import generate

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    generate(parser.parse_args().upstream, ("green", "dun"), "asscher")
