# Radiant Lantern

Implemented on all four targets: registered block/item and creative entry, paid Gem Cutter recipe (two Raw Radiant Quartz + one gold nugget → four lanterns), six-face placement, rotation/mirroring, full light, original outline, empty collision shape, self-drop loot, original model/materials and EN/PT presentation text. No new dependency or intentional gameplay deviation.

## Source and semantics

Pinned release `release/0.2.0.25-mixins8`, commit `bb99accf48ed583e29b0efae56e28c963407b8df`:

- `blocks/RadiantLantern.java`: glass material, hardness 0.3, full light, clicked-face placement and six directions; axis-aligned outline bounds 0.35–0.65 on the two thin axes, full extent on the facing axis. No collision, no full/opaque cube, no block entity or support-removal rule.
- `blocks/templates/BlockDirectionalTemplate.java` and `BlockTemplate.java`: shared registration/placement context, no additional lantern tick or inventory behavior.
- `init/ItemRegistry.java` and `init/RecipeLibrary.java`: reachable block/item and four-output recipe with exact raw-quartz/gold-nugget cost. The existing gold-nugget tag bridge preserves interchangeable modern ingredients.
- `assets/arcanearchives/blockstates/radiant_lantern.json`: original six-way and item transforms. `models/block/radiant_lantern.obj` and `.mtl` are copied byte-identically. The referenced shared master PNG already exists and is verified against the same Git object before conversion; no substitute texture or mesh.

The pickaxe mining tag preserves the preferred tool without adding a correct-tool-only drop gate. Legacy GLASS does not call `setRequiresTool`, unlike ROCK; see the inspected 1.12.2 `Material.java` reference at https://github.com/KealJones/mc-1.12.2-source_files/blob/master/src/minecraft/net/minecraft/block/material/Material.java . Native self-drop loot follows the ordinary block behavior rather than vanilla GlassBlock's special no-drop rule. Tests retain the no-tool-gate property.

## Model adaptation

`scripts/port_lantern_assets.py` reproduces the five original/converted model resources from pinned Git objects and checks the shared texture. Existing ordered-rotation conversion is reused for item transforms; translation units change to sixteenths. Native model-state x/y rotations have the opposite sign to the original Forge transforms; a loader-aware test compares all three basis vectors for every facing against the original ordered matrices, rather than testing only the long axis.

Forge and NeoForge use their installed native OBJ loader. Fabric reuses `GemCutterFabricModel` with a model-name parameter; its existing table constructor remains unchanged. Only the two registered model names are selected by the client registration hook. All build scripts expand the lantern's loader placeholder. Existing atlas inclusion supplies the shared master sheet. No new renderer framework.

## Verification and limits

`build/radiant-lantern-20260908-114947.log`: scoped four-target matrix, exit 0 in 37s. Fabric targets each execute 157 tests; NeoForge executes 163, zero failures/errors/skips. Forge compiles test sources only. Native tests cover light, hardness, six outline orientations, empty collision, no block entity/support requirement, rotate/mirror behavior and actual native model-state matrices. The registered paid-recipe fixture rejects missing gold, pays raw quartz across table/player inputs, delivers exactly four lanterns and refuses an unpaid repeat.

Pinned asset reproduction, four production/source artifact pairs and packaged resource/hash/translation checks pass. Four dedicated servers reload resources, place/check all six states, produce a lantern through the wooden-pickaxe loot command, remove fixtures and save/stop. `build/radiant-lantern-servers-20260908-115238.log` exits 0. Consolidated report: `build/radiant-lantern-verification.json`.

No client was launched. Fabric OBJ baking, rendered light/model appearance, connected-player placement/mining/crafting and lantern-specific restart acceptance remain for later validation. Command placement is not a player-placement test; matrix comparisons are not rendered parity. Natural raw-quartz production and the wider migration remain unfinished. No old-save conversion is introduced.
