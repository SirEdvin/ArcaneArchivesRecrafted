# Sliver of Light

Implemented across 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge: `arcanearchives:quartz_sliver`, original six-facing non-colliding light block, zero hardness, self-drop loot, block/item registration, creative entry and server-side raw-quartz attack acquisition. No substitute Gem Cutter recipe or quartz producer was introduced.

## Pinned behavior and resources

Release `bb99accf48ed583e29b0efae56e28c963407b8df`, `blocks/QuartzSliver.java`, `events/EventHandler.java:onLeftClickBlock`, `init/BlockRegistry.java`, `init/ItemRegistry.java`, `config/ConfigHandler.java:ServerSideConfig`.

- Placement uses the clicked face. Default faces down. The original six half-length outline boxes are preserved; collision is empty and support faces are undefined. No neighbor-support destruction: that upstream method is commented out. The random-tick flag remains, without invented growth behavior.
- A main-hand raw-quartz block attack rolls 0–99: defaults yield 21 cluster outcomes, 40 single outcomes, 39 misses. Clusters consume one raw quartz, including creative, and yield 8–23 slivers. Singles consume no quartz. This is the actual inclusive/exclusive implementation, not the approximate configuration labels.
- Any attacked block can qualify; the original stone-only tooltip wording is retained, not turned into a new material restriction.
- Original OBJ, MTL, generated-item model and item PNG copied byte-identically by `scripts/port_sliver_assets.py`. World rendering reuses the Fabric OBJ adapter or installed Forge-family native loader. The item deliberately remains the upstream flat sprite, not the world mesh. The shared raw-quartz block texture was checked against the pinned Git object, and the item sprite explicitly joins the atlas.
- EN/PT names/tooltips retain the upstream English values. No invented translation, mesh or texture.

Runtime access/spawn/config adaptations and their limits are documented in [0030](../behavior-changes/0030-sliver-smashing-boundaries.md). Existing server.properties files retain their bytes and obtain the new settings' defaults when keys are absent. The four original keys are `ChanceForSliverCluster`, `ChanceForSliverSingle`, `AmountGeneratedOnSliverClusterMinimum`, `AmountGeneratedOnSliverClusterMaximum`.

## Verification

`build/sliver-20260908-121458.log`: scoped matrix exit 0 in 38s. Each Fabric leaf executes 160 tests; NeoForge executes 167, all zero failures/errors/skips. Forge compiles test sources only. Added tests enumerate all default chance outcomes, cluster bounds/equal-bound behavior, settings round trips/invalid ranges, native light/outline/collision/rotation/support properties. These are not connected-player attack tests.

Pinned reproduction check, all four artifact/source pairs and packaged model/texture/hash/loot/translation checks pass. `build/sliver-servers-20260908-123744.log`: four dedicated servers exit successfully; six command-placed states and self-drop loot using a stick pass after reload, fixtures are removed and servers save/stop. Consolidated report: `build/sliver-verification.json`.

No visual runs. Actual attack-event interaction, rejected-spawn refund, third-party protection ordering, connected-player placement, rendering and sliver-specific restart acceptance remain unverified. Natural quartz/resonator production, raw-quartz storage conversion and the wider migration remain unfinished. Raw cluster/resonator source reads in this increment did not implement either feature.
