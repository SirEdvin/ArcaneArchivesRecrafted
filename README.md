# Arcane Archives Recrafted

**Resuming in another session? Start with [docs/HANDOFF.md](docs/HANDOFF.md).** It includes approved decisions, implemented scope, evidence locations and remaining work.

A work-in-progress port of [Arcane Archives](https://github.com/AranaiRa/ArcaneArchives) to Minecraft 1.20.1 (Fabric/Forge) and 1.21.1 (Fabric/NeoForge). Testing prereleases include migrated storage, Gem Cutter crafting, quartz progression, gems, sockets and supporting systems. They are experimental builds, not a complete replacement for the original mod. Use disposable worlds. See [the RC testing notes](docs/releases/0.0.1-rc.1.md) for installation and known limitations.

Support scope: fresh worlds only. No old-world import, legacy data migration or cross-version/cross-loader save conversion is planned. Normal saving and reloading of worlds created by this port remains required.

Patchouli is a required external dependency on both client and server, pinned per target in `stonecutter.properties.toml`; it is not bundled. Its backend starts on every target, but the complete Tome of Arcana content, item and acquisition are not ported yet. See `docs/migration/GUIDEBOOK_BACKEND.md` for provenance. Optional Trinkets/Curios Gem Socket integration is implemented; broader connected-player acceptance remains open.

## Layout

- `src/main/java/com/aranaira/arcanearchives/`: shared sources and conditional loader entrypoints.
- `src/main/resources/`: metadata and canonical content resources.
- `versions/<minecraft>-<loader>/`: Stonecutter leaves and their build output. Edit canonical sources, not generated leaf build output.
- `stonecutter.properties.toml`: dependency and identity pins.
- `build.<loader>.gradle.kts`: loader-specific build configuration.
- `docs/MIGRATION_TASKS.md`: migration phases, acceptance gates and open decisions.
- `docs/HANDOFF.md`: session entry point and documentation index, including all machine-readable audits.
- `docs/UPSTREAM_INVENTORY.md`: source-level audit checklist.
- `docs/migration/UNFINISHED_UPSTREAM_FEATURES.md`: separate notes on unfinished or uncertain upstream content.
- `docs/behavior-changes/`: separately reviewed gameplay/logic deviations.

## Build

Use Java 21 to run Gradle; the build selects Java 17 for Minecraft 1.20.1 and Java 21 for 1.21.1. Run the exact timed/logged build command in `AGENTS.md`. Root `build` selects all four leaves. Canonical editing state is `1.21.1-fabric`.

Verify packaged artifacts with `python3 scripts/verify_artifacts.py`. Root `build` runs shared JUnit tests on all four leaves; XML reports are in `versions/<node>/build/test-results/test/`. These utility tests do not replace gameplay/client/server tests. See `docs/HANDOFF.md` for the current verification map; `docs/migration/FOUNDATION.md` records the original utility slice only.

Development runs: `:<leaf>:runClient` and `:<leaf>:runServer`, with isolated ignored directories per leaf and side. Automated startup checks and EULA/setup requirements are documented in `docs/migration/RUNTIME_BOOTSTRAP.md`. Every client and dedicated-server target has passed bootstrap smoke testing; this does not make the port playable.

Quartz-specific checks: `python3 scripts/verify_quartz_resources.py` and `python3 scripts/smoke_servers.py --quartz`. The latter modifies and cleans a fixture at Y=300 in the isolated ignored development worlds; never point it at a real player world. See `docs/migration/QUARTZ_CONTENT.md` for exact coverage and remaining gameplay tests.

## Provenance

Upstream baseline: `80944ce45c6559243d8928cc4b305bf379388652`. Main source license: MIT. Upstream notices and credits are preserved under `docs/upstream/`; asset and third-party audits remain required before copying content. Bootstrap build/source patterns derive from the local MIT-licensed TemplateProject; its Edvin copyright notice is retained in `LICENSE`.
