"""Gradle's GitHub prerelease transport. Requires Python 3.11+, git and authenticated gh.

Never overwrites releases. Gradle owns the build/test/artifact prerequisites.
"""
import hashlib
import json
from pathlib import Path
import subprocess
import tomllib

ROOT = Path(__file__).resolve().parents[1]
REPOSITORY = "SirEdvin/ArcaneArchivesRecrafted"


def run(*args):
    return subprocess.check_output(args, cwd=ROOT, text=True, timeout=180).strip()


def main():
    version = tomllib.loads((ROOT / "stonecutter.properties.toml").read_text())["mod"]["version"]
    if "-rc." not in version:
        raise SystemExit("This task publishes RC prereleases only")
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
    # Paginated listing distinguishes an absent release from an authentication/network error.
    tags = run("gh", "api", "--paginate", f"repos/{REPOSITORY}/releases", "--jq", ".[].tag_name").splitlines()
    if version in tags:
        raise SystemExit("Release already exists; use a new version, never overwrite")
    notes = ROOT / "docs/releases" / f"{version}.md"
    if not notes.is_file():
        raise SystemExit("Missing release notes")
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
    assets.append(checksums)
    print(run("gh", "release", "create", version, "--repo", REPOSITORY, "--verify-tag",
              "--title", f"Arcane Archives Recrafted {version}", "--prerelease", "--latest=false",
              "--notes-file", str(notes), *(str(p) for p in assets)))
    release = json.loads(run("gh", "release", "view", version, "--repo", REPOSITORY,
                             "--json", "url,isPrerelease,isDraft,assets"))
    if not release["isPrerelease"] or release["isDraft"]:
        raise SystemExit("Unexpected remote release state")
    actual = {a["name"]: a["size"] for a in release["assets"]}
    if actual != {p.name: p.stat().st_size for p in assets}:
        raise SystemExit("Remote release assets do not match local artifacts")
    print(f"Verified prerelease and {len(assets)} asset names/sizes: {release['url']}")


if __name__ == "__main__":
    main()
