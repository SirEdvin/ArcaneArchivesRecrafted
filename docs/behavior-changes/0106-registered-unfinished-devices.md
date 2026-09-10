# 0106 — Registered unfinished device blocks

Status: implemented under the approved reachable-content migration scope. These blocks remain explicitly unfinished prototypes, exactly as upstream; no machine functionality is invented. All four targets pass builds, resource startup and native placement/loot/item/save-restart checks. Connected-player visual/input acceptance remains open.

## Original behavior and provenance

Source: development pin `80944ce45c6559243d8928cc4b305bf379388652`, relative to `src/main/java/com/aranaira/arcanearchives/`:

- `blocks/VerdantCenser.java`
- `blocks/SpellbookLibrary.java`
- `blocks/ImmanentIncubator.java`
- `blocks/EchoingConformanceChamber.java`
- `blocks/EchoingReverberationChamber.java`
- `blocks/templates/BlockTemplate.java`
- `items/templates/ItemBlockTemplate.java`
- `init/BlockRegistry.java` and `init/ItemRegistry.java`

Each block is actually registered, has a matching registered item and is exposed in the creative inventory. All five class paths are absent from release pin `bb99accf48ed583e29b0efae56e28c963407b8df`; this is restoration of reachable development content, not a claim about the release inventory.

All five use rock material, hardness 1.7, maximum light, wooden-pickaxe harvest level and full-unit collision. All show the existing red/bold `arcanearchives.tooltip.notimplemented1` and red/italic `arcanearchives.tooltip.notimplemented2` warnings. Item stacks retain the ordinary limit of 64 and blocks drop their own item.

| Registry name | Original appearance / occlusion |
| --- | --- |
| `verdant_censer` | Cutout, nonopaque/non-full-cube rendering, original OBJ |
| `spellbook_library` | Ordinary opaque cube using the original placeholder texture |
| `immanent_incubator` | Ordinary opaque cube using the original placeholder texture |
| `echoing_conformance_chamber` | Cutout, nonopaque/non-full-cube rendering, original OBJ |
| `echoing_reverberation_chamber` | Cutout, nonopaque/non-full-cube rendering, original OBJ |

The non-full-cube overrides do not replace collision bounds: upstream inherits full-block collision. No constructor assigns a tile/entity class, size/accessors or placement limit. Neither block implementation provides inventory, ticking, GUI, generation or duplication. Cross-reference search in Java and recipe resources found registration/model references, not a reachable recipe or machine implementation for these blocks. The chamber OBJ JSON contains legacy facing/accessor variants, but the actual classes extend the empty-property `BlockTemplate`, not its directional subclass. No direction or multiblock behavior was inferred from that JSON.

## Port and compatibility

One shared `UnimplementedDeviceBlock` preserves the identical behavior without five duplicate Java classes. Existing loader-native registration timing, block items and creative-tab construction are reused. Properties retain maximum light, hardness, tool requirements and the opaque/cutout distinction; full-unit collision is retained for every block. Native modern placement bounds/protections apply. No block entity, inventory, recipe, ticking, ownership restriction or new ability is added. The old empty-accessor bookkeeping has no device state to migrate; old-save conversion remains out of scope.

The main upstream MIT license was read before recovery; existing upstream notices remain in every production/source artifact. `scripts/port_unimplemented_device_assets.py` reads the pinned Git tree, preflights the entire output batch against existing resources, and produces/verifies 31 resource outputs. The three OBJ/MTL pairs and five textures (including the already-restored placeholder) remain byte-for-byte upstream copies. The source tree was checked for texture animation sidecars; none were present for these textures. The script reuses the existing tested legacy rotation conversion and converts translations into modern model units. All six original inventory perspective transforms for each OBJ model are retained.

The two original placeholder blockstates become normal modern `cube_all` models. Forge/NeoForge use the existing native OBJ loader; Fabric routes these three models through the existing shared OBJ adapter. Loader expansion is explicitly included in all three build scripts, under the previously fixed resource input contract. Original plural `blocks/` texture paths have explicit atlas entries, including shared `glass_edge`. The five English names are copied from `en_us.lang` with modern translation keys; no corresponding translations were found in the other upstream locale. Existing warning translations are reused. Vanilla self-drop loot and pickaxe tags replace legacy implicit drop/harvest registration.

Approval: existing authorization to restore reachable original content. No new gameplay deviation requiring a separate decision was identified. Fixture safeguards only protect test worlds and do not alter gameplay.

## Verification

### Regression and packaging

- Red: `timeout --foreground 10m ./gradlew :1.21.1-neoforge:test --tests '*UnimplementedDeviceBlockTest' --no-daemon` failed with both new assertions reaching the missing registry lookup (`AIR`), exit 1, 14s. Complete log: `build/prototype-red-20260910-101835.log`.
- Green: `timeout --foreground 10m ./gradlew build --no-daemon`, exit 0, 58s, `build/prototype-build-20260910-102141.log`. Both new loader-aware tests exercise all five registered blocks/items: light, hardness, full collision, absence of block entities, stack size, warning keys/styles and unchanged tooltip input stacks.
- Alternate-active verification: switch to Forge 1.20.1 (exit 0, 6s, `build/prototype-switch-20260910-103303.log`), build all four leaves (exit 0, 35s, `build/prototype-alternate-20260910-103309.log`), verify all artifacts, then reset (exit 0, 6s, `build/prototype-reset-20260910-103345.log`). Every Gradle invocation used the same 10-minute timeout, `--no-daemon` and complete silent logging.
- Stonecutter normalized pre-existing inactive nested comment delimiters in ContentRegistry, RadiantTankRenderer, AmphoraFluidStorage and RadiantTankStorage. Only those delimiter changes were restored. All 655 source/controller files then matched `/tmp/arcane-prototype-roundtrip-20260910-103303.tar` byte-for-byte; this was not an automatically byte-stable switch.
- Final canonical four-target build: `timeout --foreground 10m ./gradlew build --no-daemon`, exit 0, 26s, `build/prototype-final-20260910-103505.log`.
- `python3 scripts/verify_artifacts.py`: all four binary/source pairs pass, including the new class, exact expanded model/state/item/loot JSON, OBJ/MTL/texture bytes and atlas/tag entries. Existing test/dependency isolation is not weakened.
- Re-running `timeout --foreground 30s python3 scripts/port_unimplemented_device_assets.py /tmp/arcane-archives-reference/migration-source` verifies the same 31 outputs against the original pin. `git diff --check` passes.

Actual XML suite totals, all zero failures/errors/skips: 193 on each Fabric target, 193 in the required native Forge runtime, and 271 on NeoForge. Forge's additional standalone 36 are a duplicated subset, not extra unique gameplay coverage. The two new Java tests run on NeoForge; all four loaders receive the dedicated-server and client checks below.

### Native runtime

`timeout --foreground 10m python3 scripts/smoke_unimplemented_devices.py <leaf>` runs two real dedicated-server sessions per target. All eight sessions exit 0 with normal world saves. For each of the five devices, native commands verify actual registered placement, pickaxe-tag membership, exactly one self-drop through native loot evaluation, a native 64-item stack, and both placed block identity and item stack after full save/shutdown/restart.

The fixture uses existing operator-accepted EULA and loopback preflight. All six positions must be air before any block is replaced. Its scoreboard name is unique, existing force-loaded status is preserved, and it clears the test container before removal to avoid dropping test items into the world. The second session verifies restoration of air and removes the temporary scoreboard/its added forced chunk.

`timeout --foreground 10m xvfb-run -a -s '-screen 0 1280x800x24' python3 scripts/smoke_clients.py <leaf>` passes on all four loaders. Real clients load the resources/models and shut down normally. These title-screen checks are not connected-player visual/rotation acceptance or audio acceptance.

| Target | Server pair wrapper / duration | Client wrapper / duration |
| --- | --- | --- |
| 1.20.1 Fabric | `build/prototype-server-1.20.1-fabric-20260910-102629.log`, 57s | `build/prototype-client-1.20.1-fabric-20260910-102918.log`, 22s |
| 1.20.1 Forge | `build/prototype-server-1.20.1-forge-20260910-102726.log`, 62s | `build/prototype-client-1.20.1-forge-20260910-102940.log`, 24s |
| 1.21.1 Fabric | `build/prototype-server-1.21.1-fabric-20260910-102502.log`, 51s | `build/prototype-client-1.21.1-fabric-20260910-103004.log`, 22s |
| 1.21.1 NeoForge | `build/prototype-server-1.21.1-neoforge-20260910-102828.log`, 50s | `build/prototype-client-1.21.1-neoforge-20260910-103026.log`, 22s |

Every wrapper exit is 0. `build/prototype-acceptance.json` aggregates all eight server results, four client results (including raw log paths, measured per-process durations, assertion markers and screenshots), regression totals and source-branch comparison. Production code/resources were unchanged between runtime acceptance and the final canonical rebuild. These blocks deliberately still say UNIMPLEMENTED; this work does not complete the migration or implement the dormant machine concepts.
