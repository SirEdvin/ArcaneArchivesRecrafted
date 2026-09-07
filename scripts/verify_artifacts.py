"""Check bootstrap production/source JAR contracts using only Python 3.11+ stdlib."""
import json
from pathlib import Path
import struct
import tomllib
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
PROPERTIES = tomllib.loads((ROOT / "stonecutter.properties.toml").read_text())
METADATA = {"fabric.mod.json", "META-INF/mods.toml", "META-INF/neoforge.mods.toml"}
PACKAGE = "com/aranaira/arcanearchives/"
TARGETS = (
    ("1.20.1", "fabric", "Fabric", 61, "fabric.mod.json"),
    ("1.20.1", "forge", "Forge", 61, "META-INF/mods.toml"),
    ("1.21.1", "fabric", "Fabric", 65, "fabric.mod.json"),
    ("1.21.1", "neoforge", "NeoForge", 65, "META-INF/neoforge.mods.toml"),
)


def require(condition, message):
    if not condition:
        raise ValueError(message)


for minecraft, loader, entrypoint, major, metadata in TARGETS:
    node = f"{minecraft}-{loader}"
    version = f"{PROPERTIES['mod']['version']}+{minecraft}"
    basename = f"arcanearchives-{loader}-{version}"
    directory = ROOT / "versions" / node / "build" / "libs"
    with ZipFile(directory / f"{basename}.jar") as jar:
        names = jar.namelist()
        require(len(names) == len(set(names)), f"{node}: duplicate ZIP entries")
        require(METADATA.intersection(names) == {metadata}, f"{node}: wrong loader metadata")
        require("LICENSE" in names and "META-INF/upstream/LICENSE" in names, f"{node}: missing notices")
        raw = jar.read(metadata).decode()
        require("${" not in raw, f"{node}: unexpanded metadata")
        data = json.loads(raw) if loader == "fabric" else tomllib.loads(raw)
        mod = data if loader == "fabric" else data["mods"][0]
        require(mod["version"] == version, f"{node}: version mismatch")
        require(mod.get("id", mod.get("modId")) == "arcanearchives", f"{node}: wrong mod id")
        expected_classes = {PACKAGE + f"ArcaneArchives{suffix}.class" for suffix in ("Mod", entrypoint)}
        classes = {name for name in names if name.endswith(".class")}
        require(classes == expected_classes, f"{node}: unexpected classes {classes}")
        for name in classes:
            require(struct.unpack(">H", jar.read(name)[6:8])[0] == major, f"{node}: incorrect Java target")
        if loader == "fabric":
            require(data["entrypoints"]["main"] == [f"com.aranaira.arcanearchives.ArcaneArchives{entrypoint}"], f"{node}: wrong entrypoint")
            require(data["depends"]["minecraft"] == f"={minecraft}", f"{node}: untested Minecraft range")
        else:
            dependency = next(d for d in data["dependencies"]["arcanearchives"] if d["modId"] == "minecraft")
            require(dependency["versionRange"] == f"[{minecraft}]", f"{node}: untested Minecraft range")
    with ZipFile(directory / f"{basename}-sources.jar") as jar:
        require(PACKAGE + "ArcaneArchivesMod.java" in jar.namelist(), f"{node}: missing common source")
        require(PACKAGE + f"ArcaneArchives{entrypoint}.java" in jar.namelist(), f"{node}: missing loader source")
    print(f"PASS {node}: metadata, entrypoint, Java target, notices, production/source JARs")
print(f"Verified {len(TARGETS)} target pairs. Bootstrap checks do not verify gameplay or runtime startup.")
