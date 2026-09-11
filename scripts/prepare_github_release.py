"""Validate immutable GitHub release prerequisites and generate SHA256SUMS.

Uploads are owned by the Gradle GitHub Release plugin, not this script.
--offline prepares assets for the plugin's dry run without remote checks.
"""
import argparse
import hashlib
from pathlib import Path
import re
import subprocess
import tomllib

ROOT = Path(__file__).resolve().parents[1]
REPOSITORY = "SirEdvin/ArcaneArchivesRecrafted"


def run(*args):
    return subprocess.check_output(args, cwd=ROOT, text=True, timeout=180).strip()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--offline", action="store_true")
    args = parser.parse_args()
    version = tomllib.loads((ROOT / "stonecutter.properties.toml").read_text())["mod"]["version"]
    if not re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+(?:-[0-9A-Za-z.-]+)?", version):
        raise SystemExit("Expected a release version or prerelease version")
    if not (ROOT / "docs/releases" / f"{version}.md").is_file():
        raise SystemExit("Missing release notes")
    if not args.offline:
        if run("git", "status", "--porcelain", "--untracked-files=all"):
            raise SystemExit("Commit all project changes before publishing")
        head = run("git", "rev-parse", "HEAD")
        if run("git", "rev-parse", f"refs/tags/{version}^{{commit}}") != head:
            raise SystemExit("Release tag must identify HEAD")
        remote = run("git", "ls-remote", f"https://github.com/{REPOSITORY}.git",
                     f"refs/tags/{version}", f"refs/tags/{version}^{{}}")
        refs = dict(line.split()[::-1] for line in remote.splitlines())
        if refs.get(f"refs/tags/{version}^{{}}", refs.get(f"refs/tags/{version}")) != head:
            raise SystemExit("Push the exact release tag before publishing")
        tags = run("gh", "api", "--paginate", f"repos/{REPOSITORY}/releases", "--jq", ".[].tag_name").splitlines()
        if version in tags:
            raise SystemExit("Release already exists; use a new version, never overwrite")
    assets = []
    for minecraft, loader in (("1.20.1", "fabric"), ("1.20.1", "forge"),
                              ("1.21.1", "fabric"), ("1.21.1", "neoforge")):
        for suffix in ("", "-sources"):
            asset = ROOT / f"versions/{minecraft}-{loader}/build/libs/arcanearchives-{loader}-{version}+{minecraft}{suffix}.jar"
            if not asset.is_file():
                raise SystemExit(f"Missing artifact: {asset}")
            assets.append(asset)
    checksums = ROOT / "build/SHA256SUMS"
    checksums.write_text("".join(f"{hashlib.sha256(p.read_bytes()).hexdigest()}  {p.name}\n" for p in assets))
    print(f"Prepared {len(assets)} artifacts and {checksums.name}; no uploads performed")


if __name__ == "__main__":
    main()
