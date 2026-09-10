# Implementation continuation — settings, receipt and inventory handlers

Latest metadata addition: `recipe/CraftingCreator` preserves original creator UUID/display-name fields on copied outputs, with version-correct NBT/CUSTOM_DATA and unrelated-data preservation. [0010](../behavior-changes/0010-creator-output-copy.md) records adaptation and verification: four new cases, 61 passing tests per executed Fabric/NeoForge target, Forge test compilation and all-target assembly/artifact checks. Actual recipe invocation and letter consumers remain pending.

Latest crafting addition: `GemCutterInputHandler` implements owned 18-slot ordinary inputs, validated direct writes/loads and atomic input consumption under [0009](../behavior-changes/0009-gem-cutter-input-boundary.md)/[0005](../behavior-changes/0005-gem-cutter-crafting-conservation.md). Eight regressions pass on both Fabric targets and NeoForge (57 total each); Forge test sources compile, and all four targets assemble/pass artifact checks. Full server-authoritative crafting, output/remainders, dirty/menu notifications and concrete block integration remain pending. Existing Forge runtime-test initialization is not fixed or suppressed.

Latest foundation addition: `types/BlockPosDimension.java` provides immutable dimension-aware identity and strict fresh-world coordinate/dimension NBT under [0008](../behavior-changes/0008-dimension-position-identity.md). Five new tests pass on both Fabric targets and NeoForge (49 total each). Forge packages but its isolated fixture fails at the known loader bootstrap; no all-target test success is claimed. Actual network/link persistence and unavailable-world handling remain pending.

Newest slice: shared optional-upgrade inventory and unchanged upstream upgrade-type names, with [0007](../behavior-changes/0007-optional-upgrade-write-validation.md) guarding direct writes and atomic saved-data loading. Four new regressions pass on both Fabric targets and NeoForge (44 total tests each); Forge compiles tests and all four targets package successfully. The handler's abstract type lookup still needs actual item/device bindings. No textures changed; no storage/menu gameplay milestone is complete.

Latest update: counted ingredient allocation and Minecraft-backed matching are implemented under approved [0005](../behavior-changes/0005-gem-cutter-crafting-conservation.md), which records source traces and focused verification. The user has delegated subsequent properly documented behavior decisions; additional behavior-approval forms are not required. NeoForge's loader-aware JUnit configuration now passes its full test task; the older NeoForge failure below is historical. Forge's Minecraft test harness remains unresolved. Recipe matching is not an implemented crafting transaction, registered Gem Cutter or completed progression. Steps 1–7 remain unfinished.

This continuation starts the requested implementation-first pass. Steps 1–7 are **not complete**. The user requested postponing broad validation until implementation is finished; focused API/build and data-safety checks remain necessary. No full client/server, progression or release-acceptance matrix was run in this slice.

## Implemented

- `config/ServerSideConfig.java`: immutable representation of the pinned upstream server defaults, the original multiplier range, and non-destructive UTF-8 properties-file loading. All loader entrypoints resolve their loader's configuration directory and load `arcanearchives/server.properties` before content registration. Existing invalid files are retained and reported rather than silently replacing storage settings. This is fresh configuration, not an old `.cfg` importer.
- `data/PlayerSaveData.java`: UUID-keyed overworld `SavedData` access on the server thread, original `received_book` boolean, idempotent dirty marking, and version-correct saved-data methods. No player-entity attachment, numeric-dimension conversion or old-world importer was added. Acquisition has not been wired; the class does not grant a book by itself.
- Focused tests for settings defaults/range/round-trip/file preservation and receipt state/dirty marking/NBT round-trip. The artifact contract now explicitly includes these classes and their source files.

## Source consulted

Pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`:

- `config/ServerSideConfig.java`, `config/ConfigHandler.java`, `config/ManifestConfig.java`, `config/NonModTrackingConfig.java`.
- `data/PlayerSaveData.java`, previously traced `data/DataHelper.java`, and `types/BlockPosDimension.java`.
- `inventory/handlers/ExtendedItemStackHandler.java`, `SizeUpgradeItemHandler.java`, `OptionalUpgradesHandler.java`.
- `tileentities/RadiantResonatorTileEntity.java`, `init/ItemRegistry.java`, and the component-item classes while checking downstream dependencies. Inspecting these files does not mark them ported.

Modern loader config-directory APIs and mapped `SavedData`/`DimensionDataStorage` sources were inspected. Upstream MIT and preserved credits were reread. No textures or other assets were replaced or newly imported in this foundation-only slice; subsequent content work must use the source-backed original assets.

## Approval boundary

Storage inspection exposed phantom upgrade extraction, occupied-slot consumption and unchecked negative transfer operations. See [0004](../behavior-changes/0004-inventory-operation-conservation.md). The first form timed out, but the user requested another form and explicitly selected “Approve these corrections and continue implementation.” Approval is now recorded; the handler work below implements that scope. Tome grant ordering and unrelated gameplay changes remain outside this approval.

## Remaining foundation work

Server-to-client settings synchronization, reload/lifecycle behavior, general/client configuration, block entities, dimension-aware persisted network links, item component adapters and gameplay packet validation remain open. The player receipt codec tests do not establish actual world restart, multiplayer or acquisition parity. All storage, progression, networking, tools, tome content and integration milestones remain open unless individually evidenced elsewhere.

## Initial focused verification

`timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.20.1-forge:test :1.21.1-fabric:test :1.21.1-neoforge:test --no-daemon` — exit 0, 15s, full output at `build/foundation-settings-20260907-201650.log`. This compiles all supported leaves and exercises the focused tests; it is not final gameplay acceptance. A final build result, when available, is recorded below rather than inferred from these tests.

No commit, push, remote or publication was performed.

Final build of this partial slice: `timeout --foreground 10m ./gradlew build --no-daemon` — exit 0, 19s, `build/foundation-settings-build-20260907-201858.log`. Artifact contracts pass for all four target pairs. JUnit XML reports 18 tests per leaf, zero failures/errors/skips (the existing MathUtils tests plus seven settings/receipt cases). `git diff --check` passes. No runtime or full gameplay completion is inferred from this build.

## Subsequent approved inventory implementation

- `inventory/handlers/ExtendedItemStackHandler.java` owns shared insert/extract/simulation, component-sensitive stacking, multiplier-based capacities, original single-item extraction for unstackable items, callbacks and comparator output. Forge and NeoForge expose their native modifiable item-handler interfaces; Fabric transfer integration remains pending with actual block entities.
- Fresh-world persistence writes a unit ItemStack plus a separate integer count, avoiding vanilla count-codec limits. Loading validates all entries before replacing live slots; duplicate slots, invalid counts, unresolved items and malformed shapes fail rather than partly emptying live contents. No legacy readers or StackList migration were added.
- `SizeUpgradeItemHandler.java` reuses this storage to retain item data instead of reducing installed upgrades to booleans. Empty-slot extraction returns nothing; occupied-slot insertion returns the complete input; prerequisites and reverse-removal order remain. The two upstream concrete handler mappings and component sizes were consulted. Actual Tank/Trove bindings are not yet registered.
- The original inclusive capacity argument and first-slot bypass were initially retained. Subsequent Tank/Trove source analysis showed they compare against the pre-removal capacity. Under delegated behavior authority, [0006](../behavior-changes/0006-size-upgrade-removal-capacity.md) now checks the remaining potency for every slot. Regressions for rejection, simulation, direct clearing and exact-capacity acceptance pass on both Fabric versions and NeoForge. Concrete Tank/Trove integration is still pending.
- No textures or gameplay blocks were added by this backend slice.

### Current verification limitation — do not report the full suite green

- Four-leaf test attempt: `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.20.1-forge:test :1.21.1-fabric:test :1.21.1-neoforge:test --no-daemon` — exit 1, 16s, `build/inventory-handlers-20260907-203230.log`. Forge fails during Bootstrap/NetworkHooks initialization because its event-bus transformation is absent in plain JUnit (`NetworkEvent.<init>()` cannot be found). This occurs before the inventory test bodies.
- Remaining-version attempt: `timeout --foreground 10m ./gradlew :1.21.1-fabric:test :1.21.1-neoforge:test --continue --no-daemon` — exit 1, 13s, `build/inventory-remaining-20260907-203405.log`. NeoForge likewise fails during plain-JUnit bootstrap (ExceptionInInitializerError / NullPointerException). Neither failure was caught, ignored or disabled to obtain a green result.
- The inventory XML reports eight tests passed, zero failures/errors/skips on **each Fabric version**. They cover partial/simulated transfers, detached outputs, unstackable extraction, invalid inputs, component identity, extended-count round-trip, atomic malformed-load rejection, randomized conservation, and ordered upgrade operations. Forge and NeoForge each report one failed initialization; that is not coverage of eight test bodies.
- Compile/package check only: `timeout --foreground 10m ./gradlew assemble --no-daemon` — exit 0, 12s, `build/inventory-assemble-20260907-203550.log`. `scripts/verify_artifacts.py` passes all four production/source pairs with both handlers explicitly included. `assemble` is not a substitute for the failing tests or a successful full build.
- Follow-up requires loader-aware GameTests or supported loader JUnit bootstrapping, not reflection hacks, fake Minecraft stacks or silently excluding the tests. Broad runtime acceptance remains deferred; the storage phase is not complete.
