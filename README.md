# Arcane Archives Recrafted

A work-in-progress port of [Arcane Archives](https://github.com/AranaiRa/ArcaneArchives) to Minecraft 1.20.1 (Fabric/Forge) and 1.21.1 (Fabric/NeoForge). The bootstrap builds; gameplay migration has not started. These jars are not a playable replacement yet.

## Layout

- `src/main/java/com/aranaira/arcanearchives/`: shared sources and conditional loader entrypoints.
- `src/main/resources/`: metadata and future canonical content resources.
- `versions/<minecraft>-<loader>/`: Stonecutter leaves and their build output. Edit canonical sources, not generated leaf build output.
- `stonecutter.properties.toml`: dependency and identity pins.
- `build.<loader>.gradle.kts`: loader-specific build configuration.
- `docs/MIGRATION_TASKS.md`: migration phases, acceptance gates and open decisions.
- `docs/UPSTREAM_INVENTORY.md`: source-level audit checklist.
- `docs/behavior-changes/`: separately reviewed gameplay/logic deviations.

## Build

Use Java 21 to run Gradle; the build selects Java 17 for Minecraft 1.20.1 and Java 21 for 1.21.1. Run the exact timed/logged build command in `AGENTS.md`. Root `build` selects all four leaves. Canonical editing state is `1.21.1-fabric`.

Verify packaged bootstrap artifacts with `python3 scripts/verify_artifacts.py`. No gameplay tests exist yet; do not confuse compilation with parity.

## Provenance

Upstream baseline: `80944ce45c6559243d8928cc4b305bf379388652`. Main source license: MIT. Upstream notices and credits are preserved under `docs/upstream/`; asset and third-party audits remain required before copying content. Bootstrap build/source patterns derive from the local MIT-licensed TemplateProject; its Edvin copyright notice is retained in `LICENSE`.
