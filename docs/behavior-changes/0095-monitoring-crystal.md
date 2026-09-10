# 0095 — Monitoring Crystal registered block and inventory target

Status: registered block/item, recipe, native inventory-target lookup and Revelation inspection implemented on all four targets. Network relay integration and runtime acceptance remain incomplete.

Original: release bb99accf48ed583e29b0efae56e28c963407b8df registers Monitoring Crystal in BlockRegistry; RecipeLibrary:123 crafts one from shaped radiant quartz (1), gold nuggets (2), wooden sticks (4). MonitoringCrystal.java preserves clicked-face placement, six directional overhanging selection boxes, no collision, hardness .8 and full light. MonitoringCrystalItem rejects Immanence tile targets and adds its original literal tooltip. The tile queries the block opposite FACING, preferring unsided item capability then ordinary inventory, excluding Immanence devices. Revelation reports that opposite direction. Original OBJ/materials and MIT notices are retained.

Scope: restore the craftable/placeable device, target lookup, original artwork and Revelation direction inspection. Use native unsided Fabric Transfer/Forge-family item storage lookups. Do not expose a new automation capability on the Crystal itself, load chunks or manufacture network IDs. Network membership, Manifest display/withdrawal, tracking and owner-network lifecycle remain pending with the network milestone; this increment is not a working network relay claim.

Approval: source-backed restoration under standing migration scope; no new gameplay ability or recipe change proposed. Native APIs replace legacy capability classes. Any further behavior deviation requires its own approval.

Implementation also restores original English/Portuguese text, loot and pickaxe tag, six blockstate rotations, item perspective transforms, original OBJ/MTL, and the existing shared atlas sheet. Fabric reuses the existing OBJ adapter; Forge-family uses native OBJ loading. The target cache retains original lazy selection; loaded-chunk guards prevent queries from loading remote chunks. The temporary explicit migrated-Immanence type list must grow with the network migration; FakeAir is intentionally not excluded (originally an AATileEntity, not ImmanenceTileEntity).

Asset conversion encountered previously unsupported singular Euler rotation at the Crystal's third-person perspective. The existing converter now chooses an equivalent zero-roll representation; ordered-matrix basis fixtures cover both signs of singular yaw and all six block rotations. This changes representation, not the intended original transform.

Verification: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*MonitoringCrystalTest' --offline --no-daemon`, exit 0, 34s, `build/monitoring-crystal-20260909-210526.log`. NeoForge XML: 2 tests, no skips/failures/errors; six facing targets, no-world lookup, noncollision/selection/light and migrated-device exclusion. These do not exercise live inventory capabilities.

`timeout --foreground 60s python3 scripts/verify_artifacts.py`, final exit 0, 0s, `build/monitoring-crystal-artifacts.log`; all four production/source pairs, model/OBJ/material/recipe packaging pass. A verifier-local variable shadowing error was fixed before the final successful run. `python3 -m unittest discover -s scripts -p test_port_gem_cutter_assets.py` (30s timeout): 3 tests pass (`build/monitoring-crystal-rotations.log`). `port_monitoring_crystal_assets.py <pinned checkout> --check` (30s timeout) passes (`build/monitoring-crystal-assets-check.log`). Original OBJ/MTL/shared texture bytes are checked against the pinned Git source.

The original Big Brother advancement is also restored: its parent is the already migrated Gem Cutter, not a network advancement. The native advancement codec and packaging checks include it. English fallback retains the identical original Portuguese wording.

Final advancement-inclusive verification: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*MonitoringCrystalTest' --tests '*StorageAdvancementsTest' --offline --no-daemon`, exit 0, 23s, `build/monitoring-crystal-final-20260909-210855.log`; 2 Crystal fixtures plus 1 native advancement-codec fixture pass with no skips/failures/errors. All four artifact checks rerun successfully in `build/monitoring-crystal-artifacts.log` (exit 0, 0s). `git diff --check` passes.

No client/server launched. Placement, live Gem Cutter crafting, advancement grants, modded inventory targets, rendering, native protections, multiplayer and save/restart remain runtime acceptance.
