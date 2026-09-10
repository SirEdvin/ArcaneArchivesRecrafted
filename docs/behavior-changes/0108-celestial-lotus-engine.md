# 0108 — Registered Celestial Lotus Engine

Status: restored under the approved reachable-development-content migration scope. No new engine logic, Immanence generation, recipe, facing state or block entity is introduced. Four-target builds, artifact checks, dedicated-server placement/loot/restart checks and client resource startups pass. Connected-player visual/mining/input acceptance remains open.

## Original behavior

Development pin `80944ce45c6559243d8928cc4b305bf379388652`:

- `blocks/CelestialLotusEngine.java` extends `BlockTemplate`, uses `Material.GLASS`, hardness 0.3, full light, pickaxe harvest level 0, non-full/non-opaque rendering and an OBJ model.
- `init/BlockRegistry.java` constructs/registers the block, assigns a normal `ItemBlockTemplate`, and registers its model. `init/ItemRegistry.java` registers the block item and inventory model.
- No caller assigns this block an entity class, multiblock size or placement limit. Its actual state container has no properties. The six `facing` variants in legacy JSON therefore do not establish directional behavior.
- `ItemBlockTemplate` retains vanilla stack size and ordinary placement when no accessor size/limit is assigned; modern vanilla placement retains world-height validation.
- Collision remains a full unit box despite non-full/opaque rendering. There is no inventory, ticking engine or generator.
- The gold tooltip is literally `Generates infinite Immanence. Creative only.` This describes unfinished intent, not an implemented generator. The text is preserved rather than silently rewritten or used as permission to invent machinery.
- The English name is `Celestial Lotus Engine`. No other upstream language supplies these two keys. Creative-tab availability is restored; no survival acquisition recipe is added, and survival placement is not artificially prohibited.
- OBJ, MTL and four textures are copied from the same pin under the retained MIT notice. The asset converter verifies shared existing bytes before writing; `block_arcanearchives_master.png` already matches, so unrelated artwork is not replaced.

### Inherited behavior must not be inferred from the material name

The Minecraft 1.12.2 decompiled reference snapshot `12ae74ca50912e7f3fe279eeabd3ec02526856e6` in `KealJones/mc-1.12.2-source_files` was inspected for inherited behavior, not copied into the mod:

- `src/minecraft/net/minecraft/block/material/Material.java:23,70,128-131`: GLASS does not call `setRequiresTool`; the default allows harvesting without a tool.
- Official Forge 1.12.x reference `3effde4f1fc9d14d6ed1dbf6bebc39c2b18780e1`, `src/main/java/net/minecraftforge/common/ForgeHooks.java:216-238`: `canHarvestBlock` accepts a material that does not require a tool before checking the configured harvest tool/level.
- Thus the modern block deliberately does **not** call `requiresCorrectToolForDrops()`. The pickaxe mining tag still expresses tool effectiveness. A pickaxe declaration alone would have been an incorrect reason to prohibit hand harvesting.
- Native `Block.java:330,787-790,928-930` defaults to stone sounds, self-item drops and SOLID rendering. The original mod overrides none of those for this block. Do not clone all properties of modern vanilla glass, add glass sounds/no-drops, or guess a translucent/cutout layer.
- Non-suffocation follows the original `Block.causesSuffocation` full-cube check.

References:
https://raw.githubusercontent.com/KealJones/mc-1.12.2-source_files/12ae74ca50912e7f3fe279eeabd3ec02526856e6/src/minecraft/net/minecraft/block/material/Material.java
https://raw.githubusercontent.com/MinecraftForge/MinecraftForge/3effde4f1fc9d14d6ed1dbf6bebc39c2b18780e1/src/main/java/net/minecraftforge/common/ForgeHooks.java

## Proposed modern equivalent and reason

Implement the registered block's reachable behavior, not its unfinished tooltip promise:

- Shared `blocks/CelestialLotusEngine.java`, normal registered block item and creative-tab entry.
- Modern explicit light, hardness, collision/occlusion and harvesting properties replace the removed material API. Map color remains NONE; default stone sound is preserved.
- Existing Fabric OBJ adapter and native Forge/NeoForge OBJ loaders render the same geometry/materials; resource expansion is tracked as a Gradle input and verified in each JAR.
- `scripts/port_lotus_assets.py` reproducibly restores ten resources. It translates legacy right-hand aliases and explicitly retains unspecified left-hand identity, using the already-verified ordered rotation converter. GUI/ground scale remains 0.25. Original SOLID rendering is preserved. The unused `defaults.elements.shade` object is not a Forge V1 variant field; it is not treated as an instruction to disable OBJ shading.
- Original English text, native self-drop loot and version-correct pickaxe tags/atlas entries complete the ordinary item/block lifecycle.

Affected targets: Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

Approval: covered by the user's approved restoration of reachable upstream features and preservation of original behavior. No gameplay deviation is proposed. Native world-height/placement checks remain; test-only all-air guards prevent replacing occupied fixture space, preserve pre-existing chunk forceload state and remove their own scoreboard objective.

## Verification

`CelestialLotusEngineTest` uses actual NeoForge-registered block/item objects. Two cases cover light, hardness, full collision, empty state properties, absence of block entity, non-occlusion, no required harvesting tool, original stone sounds, stack size and gold tooltip key/style. RED before registration: both tests failed at the absent block assertion.

`LotusItemTransformsTest` exercises Minecraft's native model parser on both Fabric Minecraft versions: right-hand rotation/scale/translation, GUI translation and GUI/ground scale, and original left-hand identity. It does not infer success from JSON validity alone.

All Gradle commands below used `timeout --foreground 10m`, `--no-daemon` and complete output redirection:

| Command/task | Exit | Duration | Log |
| --- | --- | --- | --- |
| `./gradlew :1.21.1-neoforge:test --tests '*CelestialLotusEngineTest' --no-daemon` (RED) | 1 | 14s | `build/lotus-red-20260910-110428.log` |
| `./gradlew build --no-daemon` | 0 | 61s | `build/lotus-build-20260910-110757.log` |
| `./gradlew 'Set active project to 1.20.1-forge' --no-daemon` | 0 | 7s | `build/lotus-switch-20260910-111813.log` |
| `./gradlew build --no-daemon` (alternate active) | 0 | 42s | `build/lotus-alternate-20260910-111820.log` |
| `./gradlew 'Reset active project' --no-daemon` | 0 | 7s | `build/lotus-reset-20260910-111902.log` |
| `./gradlew build --no-daemon` (final canonical) | 0 | 27s | `build/lotus-final-20260910-112006.log` |

All four artifact production/source pairs pass in both active states. After resetting, the known nested-inactive-comment normalization in ContentRegistry, RadiantTankRenderer, AmphoraFluidStorage and RadiantTankStorage was restored; all 668 snapshotted source/controller files match `/tmp/arcane-lotus-roundtrip-20260910-111813.tar` byte-for-byte. The switch itself is not claimed automatically byte-stable.

Actual final XML totals, all with zero failures/errors/skips:

| Test execution | Tests |
| --- | ---: |
| 1.20.1 Fabric | 196 |
| 1.20.1 Forge standalone subset | 36 |
| 1.20.1 Forge native runtime shared suite | 193 |
| 1.21.1 Fabric | 196 |
| 1.21.1 NeoForge | 273 |

Forge's standalone subset duplicates coverage and is not an additional unique gameplay suite.

### Native servers and clients

`timeout --foreground 10m python3 scripts/smoke_lotus.py <leaf>` runs two real server sessions per target: placement, pickaxe-tag membership, self-drop with an explicitly empty loot tool, full item-stack persistence, world restart and fixture cleanup. It reuses the established prototype fixture with configurable names/tool and preserves the default five-device command path. Existing operator-accepted EULA and loopback preflight are unchanged. An empty-tool loot command tests loot evaluation, not an interactive player's full break pipeline; harvesting permission is separately covered by the registered-state assertion.

| Target | Exit | Combined duration | Wrapper log |
| --- | --- | --- | --- |
| 1.20.1 Fabric | 0 | 47s | `build/lotus-server-1.20.1-fabric-20260910-111106.log` |
| 1.20.1 Forge | 0 | 52s | `build/lotus-server-1.20.1-forge-20260910-111153.log` |
| 1.21.1 Fabric | 0 | 40s | `build/lotus-server-1.21.1-fabric-20260910-111245.log` |
| 1.21.1 NeoForge | 0 | 40s | `build/lotus-server-1.21.1-neoforge-20260910-111325.log` |

Default shared-fixture regression: `timeout --foreground 10m python3 scripts/smoke_unimplemented_devices.py 1.20.1-forge`, exit 0, 63s, `build/lotus-fixture-regression-20260910-112051.log`; both sessions pass for the existing five devices with wooden-pickaxe loot.

Each client uses `timeout --foreground 10m xvfb-run -a -s '-screen 0 1280x800x24' python3 scripts/smoke_clients.py <leaf>`:

| Target | Exit | Duration | Wrapper log |
| --- | --- | --- | --- |
| 1.20.1 Fabric | 0 | 22s | `build/lotus-client-1.20.1-fabric-20260910-111421.log` |
| 1.20.1 Forge | 0 | 24s | `build/lotus-client-1.20.1-forge-20260910-111443.log` |
| 1.21.1 Fabric | 0 | 22s | `build/lotus-client-1.21.1-fabric-20260910-111507.log` |
| 1.21.1 NeoForge | 0 | 23s | `build/lotus-client-1.21.1-neoforge-20260910-111529.log` |

`build/lotus-acceptance.json` records the eight Lotus server sessions, four clients, exact raw log/screenshot paths and XML totals. Restart markers, restored-air markers and removal of the fixture scoreboard objectives were read back from actual server output. No development game JVM remains. `git diff --check` passes.

These are resource-startup and command-driven dedicated-server checks, not connected-player visual/input or remote-multiplayer acceptance. No claim is made that the whole migration, Immanence subsystem or creative engine functionality is complete.
