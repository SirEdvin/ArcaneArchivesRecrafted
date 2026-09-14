# 0131 — Native storage-network device discovery

## Status and approval

Implemented the loaded-device discovery portion of the user-requested 0.0.2 plan on Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. This preserves device-owned storage and existing ownership/Hive semantics; it does not introduce remote extraction, new acquisition, or a second saved inventory. User authorization is the request to implement the 0.0.2 plan, with ownerless-device rules already approved in 0116. No new gameplay deviation is proposed here.

Manifest, lectern and Brazier consumers remain unimplemented. Native placement/removal and current-membership checks are verified; real chunk unload/reload, cross-dimension operation and process-restart acceptance remain open.

## Original behavior and source evidence

Inspected release checkout `/tmp/arcane-archives-reference/migration-source`, commit `bb99accf48ed583e29b0efae56e28c963407b8df`. Its MIT license was read; the retained upstream notice remains in `docs/upstream/LICENSE`. Paths below are relative to upstream `src/main/java/com/aranaira/arcanearchives/`.

- `tileentities/ImmanenceTileEntity.java:77–112`: native loading queues a device; saved owner identity selects the personal network. UUID creation happens after ownership is available.
- `events/ServerTickHandler.java:59–91`: server-end-tick queue adds or refreshes devices. The port must preserve approved ownerless/fake-player behavior from 0116, not import the old queue's ownerless-device destruction path.
- `data/ServerNetwork.java:210–234,272–282`: add/update device references, but network save data itself writes only the player ID. Device positions/inventories are reconstructed from loaded entities, not a second persistent inventory database.
- `types/IteRef.java:30–37`: unavailable worlds/chunks return no device instead of loading them.
- `data/ServerNetwork.java:350–375`: current Hive lookup, not a permanent cached membership decision.
- `util/ManifestUtils.java:123–150`: Manifest chooses current personal or Hive devices; aggregation reads live Manifest-capable inventories. Lines 49–120 retain separate in-range, out-of-range and other-dimension groups; this is not permission to hide all nonlocal items.
- `util/ManifestUtils.java:162–225`: monitored-target collection and release double-chest deduplication. Preserve the separate approved 0115 policy when implementing aggregation.

Further routing observations retained for the next consumer implementation:

- `util/InventoryRoutingUtils.java:28–65`: Trove-specific score first; matching-only storage scores 4999 and Gem Cutter scores 5000; unmatched matching-only destinations are excluded. Ordinary empty containers score 0, unmatched occupied containers score occupied slots, and matching ordinary containers use the original occupancy formula.
- `util/InventoryRoutingUtils.java:68–110`: stable descending score order after personal/Hive selection, same dimension and horizontal-radius checks. Apply the approved real-Z correction (0114) and cache revalidation (0103), not new priorities.
- `tileentities/BrazierTileEntity.java:43–50,79–105`: default radius 150, step 10, bounds 0–300; personal-only defaults false. Lines 299–314 persist radius/mode.
- `tileentities/BrazierTileEntity.java:404–448`: insertion-only virtual handler, 999 empty reported slots and slot limit 999, no extraction. This is not a persistent Brazier buffer inventory.
- `tileentities/BrazierTileEntity.java:179–295`: held-item/double-click deposit behavior and conserved returned/rejected remainders need their own runtime port. Nested-container extraction is commented out; do not activate it accidentally.
- `tileentities/BrazierTileEntity.java:451–488`: 1000-ms cached-route lifetime with refresh. Caching is not implemented by this discovery checkpoint.
- `tileentities/RadiantCraftingTableTileEntity.java:24` implements Manifest listing, not `IBrazierRouting`; do not infer ordinary workbench routing from Gem Cutter routing.

This is a partial network source trace, not a completed registration/UI/configuration parity inventory. The master baseline still needs comparison wherever the remaining consumers differ.

## Implementation

`data/StorageNetworks.java` keeps ordered per-world position indexes with weak entity references and weak world keys. Actual inventories, ownership and Hive membership remain in their existing authoritative objects. Unlinked devices can be indexed before ownership assignment but are never visible until a current eligible owner exists.

`mixin/StorageNetworkChunkMixin.java` hooks native `LevelChunk` installation, removal, post-load registration and clearing. Indexing is server-thread-only and uses the actual chunk map, not a detached block entity's `setLevel` call. Asynchronous construction does not touch the index; native server-side registration rebuilds it later.

Each visibility query:

1. Requires the server thread and a nonnull requester identity.
2. Resolves personal/current-Hive owners from `HiveSaveData` without caching permission decisions.
3. Checks chunk availability before world lookup and verifies exact current block-entity identity.
4. Omits removed, replaced, unlinked and ineligible devices.
5. Returns a detached immutable list; consumers must revalidate before later mutation. A list is not a durable authorization token.

Chest, Trove, Tank, Radiant Crafting Table, Resonator and the existing Gem Cutter/Monitoring Crystal ownership family are recognized. Being discoverable does not imply every device is a Manifest inventory or Brazier destination; consumers must apply their own source-backed roles.

The artifact gate includes the two new production classes and source files, requires the lifecycle mixin in the correct loader configuration and checks all four Forge reobfuscated method selectors in the packaged refmap. Test-only lifecycle code remains outside release artifacts.

## Verification

All commands used explicit timeouts, `--no-daemon` for Gradle, and complete redirected output.

- RED: `timeout --foreground 10m ./gradlew :1.21.1-fabric:runGameTest --no-daemon`; exit 1, 25s, `build/network-discovery-red-20260912-055232.log`. With the discovery implementation present but no native hooks, `deviceOwnership` fails specifically at `Device discovery ignored native placement ownership`.
- GREEN: `timeout --foreground 10m ./gradlew build --no-daemon`; exit 0, 82s, `build/network-discovery-green-20260912-055403.log`. All four native servers run and pass their required suites, including the extended device-ownership fixture.
- Final source/build check: same full-build command; exit 0, 76s, `build/network-discovery-final-20260912-055749.log`.
- Final artifacts: `timeout --foreground 2m python3 scripts/verify_artifacts.py`; exit 0, under one second at shell timing resolution, `build/network-discovery-artifacts.log`. All four production/source pairs pass, including new Forge refmap guards.
- `git diff --check` passes. No task-owned game processes remain.

`DeviceOwnershipLifecycle` now verifies actual Gem Cutter/Crystal placement is discoverable only for the ordinary placer, not fake/no-player placement. `StorageNetworkLifecycle` executes inside the already-required ownership GameTest on every loader: ownerless exclusion, unrelated-player isolation, exact Hive sharing, personal-only mode, succession, departure/disband revocation, live owner change, detached-entity isolation, removal, replacement and native entity reinstallation. These assertions use real installed block entities and persisted Hive APIs rather than manually inserting index entries.

Fabric XML reports identify the passing `fabricruntimetests.deviceownership` fixture on each version. Forge-family native GameTest completion is recorded in the full-build logs; their separate JUnit reports are not substituted for the native fixture.

No connected clients, gameplay screenshots, actual chunk unload/restart campaign, alternate-active-state round trip or complete network/Tome release acceptance was performed at this checkpoint. Version remains 0.0.1 until verified 0.0.2 release preparation. No commit, push, tag or publication.
