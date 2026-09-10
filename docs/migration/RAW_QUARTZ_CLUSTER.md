# Raw Quartz Cluster

Implemented on all four targets: `arcanearchives:raw_quartz_cluster` block/item, creative entry, six-facing placement/rotation/mirroring, full light, hardness 1.4, pickaxe/tool requirement, ordinary and Silk Touch loot, original OBJ/materials and inventory transforms. This is the harvest/decorative block, not resonator production.

## Pinned source and parity

Release `bb99accf48ed583e29b0efae56e28c963407b8df`:

- `blocks/RawQuartzCluster.java`, `blocks/templates/BlockDirectionalTemplate.java`, `BlockTemplate.java`, `init/BlockRegistry.java` and `ItemRegistry.java` establish actual registration, default UP facing and clicked-face placement.
- `getDrops` returns one Raw Radiant Quartz regardless of Fortune; `canSilkHarvest` permits collecting the cluster itself. Modern native alternatives loot preserves that distinction, without adding Fortune bonuses or substituting Nether Quartz.
- The original outline stays vertical (0.2–0.8 in X/Z, full Y) for every facing. The port intentionally retains that unusual behavior and solid collision rather than rotating its hitbox to fit the mesh. No support-removal rule, random growth or block entity was invented.
- The actual upstream tooltip uses `arcanearchives.tooltip.item.raw_quartz`, not the unused cluster-specific descriptive key. That existing gold tooltip is preserved. Names retain original EN/PT values.
- The optional Thaumcraft infusion stabilizer hook (`0.15`, enabled) remains unfinished integration scope, not an approved removal or a claim of supported modern Thaumcraft. No dependency was added.
- `RadiantResonatorTileEntity` creates this block only through its owner-network/online-player growth path. That producer remains unported; no ore generation, substitute recipe or ungated producer was added.

## Resources and native version adaptation

`scripts/port_cluster_assets.py` copies original `models/block/raw_quartz.obj` and `.mtl` byte-identically, verifies the shared raw-quartz texture against the same pinned Git object, and converts the legacy blockstate inventory transforms. The original release supplies inventory presentation through blockstate metadata, not a separate `models/item/raw_quartz_cluster.json`; the modern item inherits the converted block model.

Existing Fabric OBJ machinery is reused with the actual `raw_quartz` asset name. Forge-family uses its installed native loader. All build scripts expand loader metadata. No new atlas sheet or replacement artwork.

The source loot JSON contains the valid JSON string placeholder `@SILK_PREDICATE@`; resource processing replaces the complete quoted token with a version-specific predicate object. 1.20.1 uses the native enchantment predicate, while 1.21.1 uses the native enchantment sub-predicate. Both shapes were checked against the pinned vanilla diamond-ore loot resources. The predicate is declared as a resource-processing input, and packaged verification requires the exact expanded structure.

## Verified checkpoint and limits

Scoped matrix `build/raw-cluster-20260908-124819.log`: exit 0 in 40s. Each Fabric target executes 160 tests; NeoForge executes 168, zero failures/errors/skips. Forge compiles test sources only. The new native test covers all facing states, fixed outline and solid collision, hardness, full light, tool requirement, default orientation and rotation/mirror behavior.

All four production/source artifact pairs, original asset reproduction, metadata/hash/translation/loot checks pass. Four real dedicated servers reload resources, command-place/check six cluster states, confirm the mining tag and generate exactly one ordinary raw-quartz drop, one Fortune III raw-quartz drop and one Silk Touch cluster drop into empty chests. Checks require the expected first slot and no second slot. Fixtures are removed, servers save/stop. `build/raw-cluster-servers-20260908-124930.log` exits 0; report `build/raw-cluster-verification.json`.

Command loot is not a survival-player mining/tool-access test. Rendering, actual player placement/harvesting, restart acceptance, natural production and optional-mod integration remain unverified or unfinished. No vision runs. Fresh worlds only; no old-save conversion or publication.
