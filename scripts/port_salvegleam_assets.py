"""Recover pinned Salvegleam animated/depleted/accessibility asscher resources."""
import argparse
from pathlib import Path
from port_parchtear_assets import generate

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("upstream", type=Path)
    generate(parser.parse_args().upstream, ("pink", "dun"), "asscher")
