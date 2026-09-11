# 0119 — Matrix footprint placement repair

## Approval status

Approved by the user in response to the explicit request to repair Matrix multi-block placement and cleanup while preserving declared footprints and leaving unfinished machine mechanics inactive. Reservoir and Distillate now have native implementations with bounded verification. Distillate's current loaded-only lifecycle scope and evidence are recorded in [0124](0124-matrix-loaded-only-cleanup.md). Core, Repository, Storage and full Matrix acceptance remain pending.

## Original behavior and source evidence

Baseline: `80944ce45c6559243d8928cc4b305bf379388652`. Paths below are relative to upstream `src/main/java/com/aranaira/arcanearchives/`.

The Matrix family is registered in `init/BlockRegistry.java:131` and its items in `init/ItemRegistry.java:117`. All five blocks show the original not-implemented tooltip, but registration and inherited placement are reachable; the tooltip alone does not authorize removing them.

| Block | Declared footprint (width × height × length) | State template | Entity |
| --- | --- | --- | --- |
| Matrix Crystal Core | 3 × 4 × 3 | Directional | MatrixCoreTileEntity |
| Matrix Repository | 1 × 3 × 1 | Nondirectional | MatrixRepositoryTileEntity |
| Matrix Storage | 1 × 3 × 1 | Nondirectional | MatrixStorageTileEntity |
| Matrix Reservoir | 1 × 3 × 1 | Nondirectional | None |
| Matrix Distillate | 3 × 3 × 1 | Directional | None |

Evidence is in `blocks/Matrix*.java` constructors and entity factories. Core has a placement limit of one. `items/templates/ItemBlockTemplate.java:36–102` checks network limits, height and footprint replaceability before normal item use. `blocks/MultiblockSize.java:27–29` enables accessors for these dimensions.

Concrete static control-flow defect:

- `blocks/templates/BlockTemplate.java:110–137` invokes `handleAccessors` after placement.
- `handleAccessors`, lines 140–150, uses `getDefaultState().withProperty(ACCESSOR, true)` on the parent block for each footprint position and casts the parent to `BlockDirectionalTemplate` to obtain facing.
- `BlockTemplate.createBlockState`, lines 231–238, declares no properties. `BlockDirectionalTemplate.createBlockState`, lines 51–54, declares only FACING. None of the five Matrix classes override those state containers to declare ACCESSOR.
- Repository, Storage and Reservoir do not extend the directional template, so fixing only the missing property would still leave an invalid cast.

This is source-backed defect analysis, not a reproduced legacy-client crash. The exception happens after the parent placement path has begun; the port must not reproduce a partially placed structure or silent item loss.

Other boundaries discovered during this trace:

- Core/Repository/Storage directly construct entities despite omitted upstream entity registrations; that omission alone is not proof the entities are dormant.
- Core's `generateImmanence` returns a 1000 additive matrix_core source. `events/ServerTickHandler.java:107–110` calls the global bus tick. Its complete network/source lifecycle must be traced before classifying power behavior; this proposal does not enable or exclude it.
- Repository's inventory insertion implementation is commented out; Storage has no implemented storage inventory; their activation methods return false. Reservoir/Distillate have no entity factories. Do not invent menus, inventories, fluid capacities or resource processing.

## Proposed behavior

On all four targets, restore the declared footprint and directional placement where applicable through valid native state/part handling rather than copying the broken property write/cast.

- Preserve the declared dimensions, original horizontal orientation convention, light and presentation, and existing placement-limit semantics once backed by native ownership/network state.
- Validate the complete footprint against permissions, world bounds and loaded/replaceable positions before modifying the world; do not force-load chunks.
- Track parent/part identity so breaking or replacing a part cannot leave stale parts, duplicate item drops or delete unrelated blocks. Preserve structure identity through fresh-world save/reload.
- Use one parent/device identity rather than creating a full network device at every footprint position.
- Preserve the original unimplemented tooltip and inactive menu/storage/fluid behavior. Do not add unfinished gameplay simply to give these models a function.

### Native implementation direction from UnlimitedPeripheralWorks

The user requested consulting that project's block properties and pedestals. Inspected `projects/core/src/main/kotlin/site/siredvin/peripheralworks/common/block/BasePedestal.kt:25–40,58` and `FlexibleStatue.kt:33–55,90–103` in `/home/siredvin/projects/UnlimitedPeripheralWorks`.

Use their ordinary native pattern: explicitly register each property in the state definition, initialize defaults, and implement placement/rotation/mirroring through the declared direction property. Core and Distillate retain their upstream facing state and horizontal placement convention; do not copy the pedestal's clicked-face placement rule. Repository, Storage and Reservoir remain nondirectional, without a directional superclass cast. Part identity is separate from facing. No new library dependency or general-purpose multi-block framework is proposed, and no UPW implementation has been copied.

The reference establishes state/orientation handling, not Matrix footprint lifecycle: the inspected pedestal does not place multiple occupied world positions. Matrix part placement/removal must still be verified against each loader's callbacks. Approval covers the bounded repair, not arbitrary new structure mechanics or a new Immanence system. The UPW reference supplied design guidance; the subsequent explicit user approval authorizes this repair.

## Reason

A literal port of the reachable placement path uses invalid state properties and type assumptions. Treating all five as single-block decorative cubes would instead discard the declared space/placement contract without approval. Repairing valid multi-block placement is the proposed minimal behavior adaptation.

## Affected targets

- Minecraft 1.20.1 Fabric
- Minecraft 1.20.1 Forge
- Minecraft 1.21.1 Fabric
- Minecraft 1.21.1 NeoForge

## Verification

Native acquisition is now verified separately under approved [0120](0120-gem-cutter-empty-fluid-returns.md): the empty-Tank refusal was reproduced and repaired, and the real Gem Cutter recipe/menu produces one Reservoir while returning all ten Tanks on every target. Full build/artifacts are green. Historical placement results below are separate evidence; connected crafting/rendering remains open.

### Matrix Reservoir implementation increment

- Registered `matrix_reservoir` and its block item/creative entry, with explicit integer `part=0..2` state and no facing or block entity. The parent renders the original full OBJ; the two occupied children are invisible but retain full collision. Native chunk block states carry the column identity; no save converter or separate network device was introduced.
- `MatrixReservoirItem` validates all three positions for build/world bounds, loaded chunks, replaceability, player permissions and entity collision before writing. Successful placement uses the native item consumption path. Failed writes restore captured owned positions without replacing unrelated blocks; Forge callback fault-injection evidence and its resulting repair are recorded below.
- Removal clears only the matching column parts without additional drops. Piston movement is blocked as a structural-integrity safeguard of this repair; it cannot move only one part of the column. Item-supplied block-state data cannot turn the placed parent into a child.
- Preserved upstream zero default hardness, glass-material hand harvesting, stone sound, light 15, nondirectionality and both not-implemented tooltip lines. No fluid capacity, processing, inventory or menu was invented.
- Restored the recipe from `init/RecipeLibrary.java:193`: two Matrix Braces, one Containment Field, six Empowered Quartz and ten Radiant Tanks produce one Matrix Reservoir. The native catalog order places it after the existing migrated gem recipes.
- Extended `scripts/port_unimplemented_device_assets.py` to import the pinned MIT OBJ/MTL/master texture and convert the original item transforms. Preserved the default solid render layer. Existing packaged upstream license remains included. Artifact verification checks the new resources on all four targets.
- Full matrix command: `timeout --foreground 10m ./gradlew build --no-daemon`, exit 0, 54 seconds, `build/matrix-reservoir-final-20260910-214557.log`. This includes the Forge GameTest server. `timeout --foreground 60s python3 scripts/verify_artifacts.py` passed; `build/matrix-reservoir-final-artifacts.log`.
- Forge runtime fixture `ForgeRuntimeTests.matrixReservoirPlacement` exercises actual block-item placement: obstructed top rejects without consumption/partial placement; successful placement consumes one item and writes the three correct parts despite item `BlockStateTag` attempting `part=2`; replacement of each position preserves the replacing stone and clears the others; destruction of each position drops exactly one reservoir and clears the column. The complete GameTest batch passed with both required tests successful.
- Additional Forge runtime acceptance: adventure-style `mayBuild=false` and a footprint overflowing the top build limit both reject without item consumption or partial placement. Two vertically touching independently placed columns retain distinct identities: removing the lower column leaves every upper part intact. These extend the existing native placement fixture rather than introducing a simulated world. Full four-leaf `timeout --foreground 10m ./gradlew build --no-daemon` passed, exit 0, 29 seconds; `build/matrix-reservoir-boundaries-20260910-214917.log`.
- Four-leaf fresh-world restart acceptance: `timeout --foreground 10m python3 scripts/smoke_matrix_reservoir.py`, exit 0, 209 seconds; complete output `build/matrix-reservoir-restart-20260910-220007.log`. Eight real server sessions (setup/save and restart/cleanup per target) passed every assertion and clean shutdown/save check. Native `part` states survived restart; destroying the parent, middle and top of three separate restored columns cleared each column and produced exactly three Reservoir items total. A directly touching upper column remained intact. Fixture cleanup passed on every target; the harness preserves pre-existing force-loads and refuses occupied fixture space. No ERROR/FATAL/Exception lines were found in these eight server logs, and no project JVM remained running afterward.
- Forge cancellation acceptance: `ForgeRuntimeTests.matrixReservoirPlacementCancellation` calls the pinned Forge `ForgeHooks.onPlaceItemIntoWorld` path, not direct `BlockItem.place`. A temporary player-scoped `EntityMultiPlaceEvent` listener cancels the captured three-position transaction. The fixture verifies all three replaced water states are restored, stack count and custom NBT are unchanged, no Reservoir item drops appear, and Forge's capture/restoration flags and snapshot list are cleared. Listener removal and fixture cleanup are scoped in `finally`. No production workaround was needed. Full four-leaf `timeout --foreground 10m ./gradlew build --no-daemon` passed, exit 0, 28 seconds; `build/matrix-cancellation-20260910-220651.log` records all three required GameTests passing. Artifact verification passed in `build/matrix-cancellation-artifacts.log`.
- Callback interruption regression: a test-only registered `PlacementInterruptionBlock` replaces itself with stone when placement reaches it. `ForgeRuntimeTests.matrixReservoirInterruptedPlacement` initially failed at the middle part (`build/matrix-interruption-20260910-221224.log`, exit 1, 27 seconds): recursive removal had already cleared an earlier owned part before rollback captured ownership, losing its original water state. The shared item now records the successfully written prefix and restores cleared positions in that prefix, while retaining the existing non-air replacement guard. No new gameplay or production test hook was introduced.
- The corrected fixture interrupts each of the three writes, verifying prior water states are restored, the callback's stone is retained, the item count/NBT are unchanged and no Reservoir item drops appear. Full four-leaf `timeout --foreground 10m ./gradlew build --no-daemon` passed, exit 0, 51 seconds; `build/matrix-rollback-fix-20260910-221329.log` records all four required Forge GameTests passing. Artifact verification passed in `build/matrix-rollback-fix-artifacts.log`; the fault block is confined to `src/forgeGameTest` and absent from production/source artifacts.
- Forge preflight acceptance: `ForgeRuntimeTests.matrixReservoirPlacementPreflight` rejects an actual pig entity occupying each of the three footprint positions without consumption or partial writes. A temporarily moved/shrunk world border similarly rejects placement; all original border settings are restored in `finally`. After removing obstacles, a null-player native placement consumes exactly one item and establishes the correct three part identities without a collision-context exception. This is not evidence of a particular automation mod. Full four-leaf build passed, exit 0, 27 seconds; `build/matrix-preflight-20260910-221738.log` records all five required Forge GameTests passing. Artifact verification passed in `build/matrix-preflight-artifacts.log`.
- The restart fixture seeds block states with operator commands; it does not exercise block-item placement, cross-loader placement cancellation or rendering. Native placement/fault-injection counterparts now pass on both Fabric leaves and NeoForge as recorded below. Still open: live chunk unload/reload separately from server restart, unloaded-chunk and installed protection-mod fixtures, same-block state reconfiguration, connected appearance and crafting. Core/Repository/Storage/Distillate remain unimplemented under this repair.

### NeoForge native Reservoir acceptance

- Added a test-only `src/neoforgeGameTest` source set and `:1.21.1-neoforge:runGameTestServer`, required by `check`. Normal clients, servers and JUnit explicitly exclude this test mod; production and source JAR checks confirm no runtime-test/fault-fixture classes are shipped. Existing runtime dependencies, including mandatory Patchouli, are inherited rather than duplicated or bundled.
- Four native fixtures cover the corresponding Forge placement, cancellation, interruption and preflight cases: blocked/denied/height-border/entity placement; component-supplied invalid parent-part normalization; consumption; destruction/replacement at each part; touching-column independence; null-player placement; callback interruptions at each write; and the real `CommonHooks.onPlaceItemIntoWorld`/`EntityMultiPlaceEvent` cancellation path. Cancelled/interrupted placement retains custom item components and count, restores replaced water, preserves callback stone and emits no duplicate Reservoir items. No production workaround was needed for NeoForge.
- The initial short fixture intersected NeoForge's enclosing barrier, confirmed by an explicit precondition failure reporting `minecraft:barrier` (`build/neoforge-matrix-runtime-20260910-222718.log`). The NeoForge empty template now contains the full stacked-column height. This was a test-boundary defect, not a production placement failure. The initial-empty precondition remains.
- A missing-Patchouli startup returned process exit zero without tests. The Gradle task now requires a freshly written log with completion of all four required fixtures. A temporary external init script removed only the test runtime inheritance: `build/neoforge-matrix-negative-20260910-223156.log` records missing Patchouli and the new completion gate correctly failing the build (exit 1, 17 seconds). The project dependency configuration was not weakened.
- Final `timeout --foreground 10m ./gradlew build --no-daemon` passed all four leaves, exit 0, 33 seconds, `build/neoforge-matrix-final-20260910-223229.log`: four required NeoForge GameTests and five required Forge GameTests passed. NeoForge JUnit reports contain 284 tests with zero failures/errors/skips. Artifact verification passed in `build/neoforge-matrix-artifacts.log`; no project JVM remained running. This closes these bounded NeoForge world/hook fixtures, not Fabric runtime placement, connected gameplay/rendering or arbitrary protection-mod compatibility.

### Fabric native Reservoir acceptance

- Enabled the pinned Loom `fabricApi.configureTests` server-only setup, with a separate test mod/source set, no client-test run and no automatic EULA acceptance or run-directory deletion. `src/fabricGameTest/java` contains shared assertions; small `1.20.1`/`1.21.1` adapters only bridge mock-player construction and native NBT/components. Production dependencies and entrypoints are unchanged.
- Each Fabric target passes three native fixtures: actual item placement and per-part destruction/replacement, permissions/height/obstruction checks, touching independent columns, malicious root-part item state, callback-interrupted rollback with item-data conservation, world-border/entity collision rejection and null-player placement. The initial-empty precondition remains; Fabric's provided eight-block empty template encloses the fixture. Forge-style post-placement snapshot cancellation is not claimed on Fabric; particular protection-mod behavior remains unverified.
- `check` invokes `runGameTest`. Its gate requires a fresh native XML report, each of the three exact Reservoir test names once, and no failure/error/skipped elements. The fourth reported test on each target is Patchouli's `patchoulismoketest.doesitrun`, not another Reservoir case.
- Full four-leaf `timeout --foreground 10m ./gradlew build --no-daemon` passed, exit 0, 61 seconds, `build/fabric-matrix-build-20260910-223929.log`. Both Fabric report files at `versions/<leaf>/build/gametest-run/results.xml` contain all three required Reservoir cases and the passing Patchouli smoke test. Artifact verification passed in `build/fabric-matrix-artifacts.log`; test classes/adapters are absent from the production/source JARs.
- Ordinary Fabric dedicated-server smoke also passed both leaves, exit 0, 38 seconds, `build/fabric-matrix-normal-servers-20260910-224059.log`. Both servers initialized, became ready, saved and stopped; neither log lists `arcanearchives_test`. No project JVM remained running. These checks establish test isolation and bounded placement behavior, not connected rendering, crafting or complete Matrix parity.

### Distillate source trace for the next slice

Pinned `BlockDirectionalTemplate.getStateForPlacement` uses `EnumFacing.fromAngle(placer.rotationYaw - 90)`, not the commonly used player-facing opposite convention. `BlockTemplate.calculateAccessors` (lines 174–212) uses the opposite-facing axis for a nonsquare footprint: Distillate's three-wide, one-deep footprint therefore extends one position each way along that horizontal axis, with three vertical layers and the parent at bottom center. Preserve this established geometry rather than guessing width from conventional front/back terminology. The original `matrix_distillate.json` blockstate rotates its west-oriented model by -90 for north, +90 for south and 180 for east; up/down variants have no rotation. OBJ/MTL resources exist in the pinned Git tree. This is static source evidence, not implementation or rendering acceptance.

Before approval: inspected all five block classes, three Matrix entity classes, both state templates, ItemBlockTemplate, MultiblockSize and the server tick call. No legacy runtime reproduction is claimed.

Required after implementation: all-leaf builds/artifacts; placement in each supported orientation; blocked/denied/boundary placement without consumption or partial writes; destruction from parent and parts with exactly one appropriate drop; external replacement without deleting unrelated blocks; chunk unload/reload and server restart; ownership/limit accounting; connected appearance and continued absence of invented menus/storage/fluid mechanics. Keep the migration parent task open until actual acceptance is verified.
