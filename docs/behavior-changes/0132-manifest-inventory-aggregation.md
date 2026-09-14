# 0132 — Native Manifest inventory aggregation

## Scope and approval

Implements the server-side inventory projection for the user-requested 0.0.2 storage milestone, including the explicitly approved monitored-inventory deduplication in 0115. Applies to 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. The original locator-only role is preserved: this projection has no extraction, insertion or crafting path.

This is an integration of live discovery with inventory reads, not a registered Manifest item/menu or a playable release. Client synchronization, search/sort controls, tracking, lectern, acquisition and complete runtime acceptance remain open.

## Original behavior and source trace

References are relative to upstream `src/main/java/com/aranaira/arcanearchives/`, release `bb99accf48ed583e29b0efae56e28c963407b8df` at `/tmp/arcane-archives-reference/migration-source`.

- `util/ManifestUtils.java:49–120`: component/NBT-sensitive grouping, counts and source descriptors separated into in-range, out-of-range and other-dimension entries. Range is strictly less than the configured squared distance, not inclusive. Nonstackable items retain individual count-one descriptors.
- `util/ManifestUtils.java:123–237`: collect current personal/Hive Manifest-capable devices; descriptors use storage positions or the monitored target, not the Monitoring Crystal position.
- `tileentities/RadiantChestTileEntity.java:157–162`: descriptor `Chest`, or `Chest: <name>`.
- `tileentities/RadiantTroveTileEntity.java:265–282`: stored item/count only, descriptor `Trove`; empty LOCK references must not create items.
- `tileentities/RadiantCraftingTableTileEntity.java:24–44`: list its nine persistent inputs, descriptor `Radiant Crafting Table`.
- `tileentities/GemCuttersTableTileEntity.java:28–58`: list eighteen inputs with descriptor `Gem Cutter's Table`; the separate output inventory is not the Manifest inventory. Preserve this separation with the current ordinary-output model from 0128.
- `tileentities/MonitoringCrystalTileEntity.java:26–27`: descriptor `Monitoring Crystal`.
- Compared `ManifestUtils.java`, `IManifestTileEntity.java` and `GemCuttersTableTileEntity.java` between release and master `80944ce45c6559243d8928cc4b305bf379388652`. The utility diff confirms master removed double-chest handling and the duplicate-target `continue`; 0115 explicitly authorizes correcting that regression.

The upstream MIT source license and retained notice were inspected during 0131. No artwork, embedded library or new dependency is copied here.

## Implementation

`data/ManifestContents.java` reads only `StorageNetworks.visible` devices using the real caller UUID/current Hive membership. It returns copied item icons, long counts and immutable descriptor lists. Fresh queries see current contents and membership; existing snapshots cannot mutate live inventory. Item grouping first partitions by item, then compares native item data within range groups, avoiding a scan across all unrelated item types.

Native sources are Chest, Trove, persistent crafting inputs, Gem Cutter inputs and Monitoring Crystal unsided inventory lookup. Tank/Resonator discovery does not turn those devices into item-inventory entries.

Monitoring deduplicates repeated dimension-aware target positions, validated opposite double-chest halves, and identical native handler objects. Chest partner state must have matching block/facing and the opposite non-single type. A missing partner chunk is not requested through the combined native capability. Distinct replacement inventories remain distinct. This does not claim arbitrary third-party wrappers with undisclosed aliasing can be identified as the same underlying inventory.

Totals are held separately from count-one display stacks using checked long addition, avoiding 32-bit wrapped display totals. This is count-preserving presentation/safety under the planned large-count requirement, not additional storage capacity. Existing original descriptor strings and strict range grouping remain unchanged. Negative caller distance is rejected; configuration/UI transport is not implemented here.

## Native verification

`ManifestInventoryLifecycle` runs inside the already-required `deviceOwnership` GameTest on every target, using real installed native inventories and real Fabric/Forge-family monitoring lookups. No injected inventory provider or prebuilt entry list is substituted.

Covered:

- Own versus unrelated/Hive inventories and fresh revocation.
- Plain versus creator-data item identity, including the last Chest slot.
- Exact source names, positions, dimensions and per-source counts.
- Detached snapshots and copied icons.
- Strict range boundary and zero-radius grouping.
- Seeded totals exceeding the signed-int range, without changing storage capacity or claiming survival acquisition of oversized stacks.
- Original individual nonstackable descriptors.
- Multiple monitors on one target and opposite halves of a real double chest.
- Two distinct replacement barrels and target replacement with stone.
- Fixture inventory/Hive/block cleanup.

Commands used explicit timeouts, complete redirected logs and `--no-daemon`:

- RED: `timeout --foreground 10m ./gradlew :1.21.1-fabric:runGameTest --no-daemon`; exit 1, 20s, `build/manifest-contents-red-20260912-060634.log`. Native aggregation before deduplication failed specifically with `Repeated/double-chest monitoring inflated the Manifest`.
- GREEN: `timeout --foreground 10m ./gradlew build --no-daemon`; exit 0, 82s, `build/manifest-contents-green-20260912-060816.log`.
- Final full build after additional large-count/nonstackable coverage: same build command; exit 0, 56s, `build/manifest-contents-final-20260912-061142.log`. All four native GameTest servers report their required suites passed.
- `timeout --foreground 2m python3 scripts/verify_artifacts.py`; exit 0, under one second at shell timing resolution, `build/manifest-contents-artifacts.log`. All four production/source pairs pass with the new exact class/source allowlist.
- `git diff --check` passes; no task-owned game JVM remains.

Not verified here: connected client/packet behavior, actual chunk unload/dimension-transition/restart scenarios, broad modded-handler aliasing, complete progression, search/sort/localization controls, performance at production network sizes or alternate-active-state restoration. Remaining source-specific Trove/workbench/Gem Cutter listing acceptance is not implied by the Chest/monitor fixture. No Manifest item is prematurely registered. Version remains 0.0.1; no commit, push, tag or publication.
