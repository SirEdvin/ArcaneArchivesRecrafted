# Arcane Archives migration implementation plan

Goal: preserve Arcane Archives gameplay on Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

Architecture: one canonical Java/resource tree, four flat Stonecutter leaves, and loader-specific build scripts and conditional entrypoints. Keep mechanics shared and introduce loader adapters only for actual API differences; do not introduce a separately published common module without a need.

Tech stack: Java 17 (1.20.1), Java 21 (1.21.1), Mojang mappings, Gradle 9.6.1, Stonecutter 0.9.7, Loom Back Compat 0.4 / Loom 1.17.12, ModDevGradle 2.0.143. Exact loader and API pins live in `../stonecutter.properties.toml`.

Implementation is direct, not delegated. Checkboxes describe verified work, not intentions. No commits/pushes without authorization. All Gradle commands must use the timeout/file-logging wrapper in `../AGENTS.md`.

## Baseline and decisions

- Upstream: https://github.com/AranaiRa/ArcaneArchives
- Inspected baseline: `master`, commit `80944ce45c6559243d8928cc4b305bf379388652` (Patchouli integration).
- Upstream build: Minecraft 1.12.2, Forge 14.23.5.2838, Java 8, mod version 0.2.1-preview1.
- Preserve `arcanearchives` IDs and `com.aranaira.arcanearchives` package; modern display name is Arcane Archives Recrafted.
- User explicitly accepts Forge for 1.20.1. This does not establish tested compatibility with every early NeoForge distribution.
- Bootstrap only: no upstream content is registered yet. Empty bootstrap jars are not a gameplay release.
- Main source license is MIT; retained notices are under `upstream/`. Audit individual assets and embedded third-party material before importing them.
- The branch includes unfinished/unused code. Presence in source is not proof a feature shipped; registry reachability must determine parity scope.
- Full 1.12 world conversion is unresolved, not implicitly promised by retaining IDs.

## Execution protocol for each feature

1. Trace the baseline registration, callers, configuration, recipes, networking and persistence. Add source paths and exact semantics to that feature's inventory entry.
2. Record any necessary behavior deviation in its own `behavior-changes/NNNN-topic.md`; ask for approval before implementation. Do not treat unavailable integrations or unsafe legacy logic as permission to silently change behavior.
3. Add a failing regression test for the behavior or a repeatable manual baseline scenario where automated coverage is unavailable. Document why the test fails before implementing.
4. Port the smallest vertical slice (data/logic, registrations/adapters, packets, menu, client assets), then run focused tests and the complete build matrix.
5. Exercise client and dedicated server, persistence/restart and multiplayer as relevant. Record command, log and actual result; mark complete only after checking acceptance criteria.

## Phase 0 — bootstrap

- [x] Update AGENTS.md first and copy UnlimitedPeripheralWorks' exact build/logging command.
- [x] Load Stonecutter build-contract guidance and inspect the local TemplateProject DSL before reuse.
- [x] Pin upstream source and retain licensing/credit references without copying obsolete embedded jars.
- [x] Create four flat leaves in `settings.gradle.kts`, canonical 1.21.1 Fabric state in `stonecutter.gradle.kts`, and structured version properties.
- [x] Create `build.fabric.gradle.kts`, `build.forge.gradle.kts`, `build.neoforge.gradle.kts`, wrapper, conditional entrypoints and mutually exclusive loader metadata.
- [x] Build all four leaves; initial build exit 0, 63 seconds, `build/gradle-20260907-160949.log`. Fabric remapping and Forge reobfuscation ran; no test sources yet.
- [x] Verify artifact contents, Java bytecode targets and expanded metadata with `python3 scripts/verify_artifacts.py`: all four production/source JAR pairs passed, including exclusive loader metadata, entrypoints, notices and exact Minecraft ranges.
- [x] Verify all-leaf builds in both active version states and restore canonical source bytes. Alternate state 1.20.1 Forge build: exit 0, 12 seconds, `build/gradle-alternate-20260907-161458.log`; artifact checks passed again. Reset log `build/reset-20260907-161510.log`; `diff` confirmed canonical source tree and controller were byte-identical after restoration.
- [ ] Configure and exercise client/server development runs on each leaf; add automated smoke tests before gameplay implementation.
- [ ] Add CI for all four leaves, artifact checks and tests; ensure clean-cache toolchain resolution. Publishing is not configured.

## Phase 1 — freeze the real parity inventory

Files: `docs/UPSTREAM_INVENTORY.md`; upstream `init/BlockRegistry.java`, `init/ItemRegistry.java`, `init/RecipeLibrary.java`, `proxy/CommonProxy.java`, `proxy/ClientProxy.java`, `events/mappings/MappingHandler.java`, `config/`.

- [ ] Enumerate actual registered blocks, items, entities, menus, sounds and recipes, including registry aliases and configuration-gated content.
- [ ] Separate active content, debug-only objects, commented registrations and unfinished implementations. Ask whether non-shipped features belong in scope rather than enabling them accidentally.
- [ ] Inventory every asset, translation, guide entry, recipe and sound; map each to a reachable feature and its license/credit obligations.
- [ ] Capture default configuration, capacities, timings, ranges, power costs, upgrade limits, filters and crafting outputs as parity fixtures.
- [ ] Confirm baseline release/commit and save migration requirements with user before defining compatibility guarantees.

## Phase 2 — common platform foundation

Create shared code under `src/main/java/com/aranaira/arcanearchives/`; keep loader-specific registration, events and services in conditional files. Runtime resources belong to `src/main/resources/`; introduce datagen roots only when providers exist.

- [ ] Port mod lifecycle and registry ordering from `proxy/` and `init/`; test a representative block, item and block entity on each loader.
- [ ] Port `config/` defaults, server/client separation and server synchronization; test reload and mismatched client configuration.
- [ ] Replace 1.12 metadata blocks/item subtypes and ore dictionary recipes with explicit modern states/items/tags while preserving identity and outputs.
- [ ] Define dimension-aware positions and serialization from `types/BlockPosDimension.java`, `util/NBTUtils.java`, `data/`; test invalid/missing dimensions and missing registry entries.
- [ ] Isolate 1.20.1 item NBT versus 1.21.1 data components; test item copy, drop, save/reload and crafting preservation.
- [ ] Replace coremod/mixin hooks only after tracing `core/AALoadingPlugin.java`, `mixins/` and their callers. Prefer supported events; do not carry old bundled Mixin/runtime jars forward.
- [ ] Port `network/` to Forge channels and Fabric/NeoForge version-matched packet APIs. Test sender permissions, bounds, invalid targets, unloaded chunks, thread ownership and malformed payloads.

## Phase 3 — storage and persistent menus

Sources: `inventory/handlers/`, `inventory/slots/`, `tileentities/Radiant*`, `blocks/Radiant*`, `items/itemblocks/`, `inventory/ContainerRadiant*`, `client/gui/GUIRadiant*`.

- [ ] Port extended-stack handler semantics first; test overflow, simulation, partial insert/extract, NBT/component-sensitive stacking and empty values without duplication or loss.
- [ ] Port Radiant Chest and capacity upgrades; test all slots, quick move, drag/double-click, automation, lock states, break/drop and restart.
- [ ] Port Radiant Trove and item-block storage; test capacity boundaries and contents through placement, breaking and relocation.
- [ ] Port Radiant Tank and Amphora with Fabric transfer transactions / Forge and NeoForge fluid capabilities; test rollback, partial transfer, capacities, fluid identity and item persistence.
- [ ] Port Radiant Crafting Table persistent inventory and recipe matching/remainders; test shift crafting, automation, disconnect and concurrent access.
- [ ] Port Radiant Furnace and accessor links; test fuel, recipe transitions, cancellation and chunk unload.
- [ ] Port upgrade containers and tracked menu synchronization; test invalid upgrades and malicious slot actions.

## Phase 4 — storage networks, manifest and ownership

Sources: `data/`, `events/NetworksHandler.java`, `types/lists/`, `types/iterators/`, `util/Manifest*`, `util/InventoryRoutingUtils.java`, `network/PacketNetworks.java`, Manifest/letters/keys, Brazier and Monitoring Crystal classes.

- [ ] Port player/network/hive saved data, ownership, membership, invitations, resignation and expulsion; test permission revocation and offline players.
- [ ] Port network discovery, dimension/chunk lifecycle, rebuild and stale-link cleanup; test restart and unloaded members without forced chunk loading.
- [ ] Port Manifest/lectern listing, filtering, search, sorting and withdrawal; test component-sensitive identity, large counts and concurrent changes.
- [ ] Port Brazier routing and configuration; test filtering, partial capacity, unavailable destinations and transfer conservation.
- [ ] Port keys, monitoring and tracking/visual lines; verify client visibility matches server access rights.
- [ ] Port relevant administrative commands to Brigadier, with explicit permissions and safe failure paths; do not expose legacy debug commands to ordinary players.

## Phase 5 — progression, machines and immanence

Sources: `recipe/`, `api/`, `immanence/`, resonators/quartz, Gem Cutter, matrix blocks, chamber blocks, engine/incubator/censer classes. Check registry reachability before implementing each.

- [ ] Port quartz/resonator progression and associated world/entity events with baseline timings and yields.
- [ ] Port Gem Cutter recipe type/serializer, conditions, ingredient matching, menu and outputs; test recipe reload, remainders and consumption atomicity.
- [ ] Port immanence sources, bus, subscribers, consumers, bonuses and lifecycle; test tick ordering, unload/reload and conservation with baseline fixtures.
- [ ] Port active matrix core/repository/storage/reservoir/distillate functionality and active chambers/engine/incubator/censer behavior, one tested vertical slice at a time.
- [ ] Port decorative/storage blocks, lanterns and active multiblocks, including placement validation, rotation, break cleanup and loot.

## Phase 6 — tools, charms and gems

Sources: `items/`, `items/gems/`, `entity/`, `events/AnvilHandler.java`, `network/PacketArcaneGems.java`. The inventory index records every discovered class; unregistered `items/unused/` are not automatically scope.

- [ ] Port scepters and relocation/revelation/manipulation behavior; test ownership, occupied destinations, failed moves and inventory preservation.
- [ ] Port Devouring/Obstruction/Serenity charms, gem sockets, recharge powders and shared charge/cooldown rules; test storage, equipment changes, death and reconnect.
- [ ] Port each registered Asscher, Oval, Pampel, Pendeloque and Trillion gem separately, with explicit targeting/cost/effect fixtures and server authority checks.
- [ ] Port active projectile/entity/AI behavior and associated renderers; test spawning, tracking, death and persistence.
- [ ] Decide wearable integration replacement through a separate approved change document before changing Baubles-dependent behavior.

## Phase 7 — client, resources and documentation

Sources: `client/`, upstream `src/main/resources/`, `integration/guidebook/`, `integration/patchouli/`.

- [ ] Port screens, scroll/search controls, custom count rendering, keybindings and HUD; verify scaling, keyboard operation, focus, tooltips and large counts.
- [ ] Replace TESR/TEISR rendering with version-correct block entity/item rendering; preserve tank/chest/trove/brazier appearance, particles and range lines.
- [ ] Convert models, blockstates, item models, recipes, tags, loot, sounds and language files. Handle 1.21 singular data-directory changes per leaf without shipping duplicate incompatible data.
- [ ] Resolve the guidebook implementation through an approved change proposal; preserve instructional content and feature-gated visibility.
- [ ] Run client resource reload and dedicated server startup on each leaf; inspect missing textures, models, translations and codec errors.

## Phase 8 — integrations and public API

- [ ] Evaluate each discovered integration from `UPSTREAM_INVENTORY.md` for both versions/loaders; record supported, unavailable and intentionally excluded cases with user decisions.
- [ ] Port recipe-viewer display/transfer and hover overlays, testing optional-mod absence and exact dependency versions.
- [ ] Decide replacements for Baubles, HWYLA, old guidebook and version-bound magic-mod hooks separately; do not silently add Curios/Trinkets/Jade/REI/EMI dependencies.
- [ ] Port CraftTweaker recipe APIs and scripting examples only against verified compatible releases; test add/remove/reload semantics.
- [ ] Review `api/` for intended external contracts and documentation; publish API artifacts only if a real consumer requires them.

## Phase 9 — release acceptance

- [ ] Build and test all leaves from clean checkout in both canonical active states; compare restored sources and inspect binary/source jars and notices.
- [ ] Run storage/network fuzz and concurrent multiplayer tests for duplication/loss; test save/reload, dimensions, chunk unload, death and crashes where practical.
- [ ] Execute full progression walkthrough on every leaf and compare fixtures to approved baseline behavior.
- [ ] Verify supported optional-mod combinations and no-optional-mod startup, with clean client/server logs.
- [ ] Review performance at large storage/network sizes and verify no inappropriate chunk loads or per-tick full scans.
- [ ] Publish a parity report listing unfinished content, approved deviations and tested save compatibility. Never claim direct 1.12 world compatibility without migration tooling and fixture tests.
- [ ] Confirm repository remote, release naming/versioning and distribution destinations with the user before CI publishing, tagging or uploading.
