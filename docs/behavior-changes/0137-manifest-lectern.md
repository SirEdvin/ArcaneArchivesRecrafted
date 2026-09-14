# 0137 — Manifest Lectern acquisition and world route

## Original behavior

Pinned upstream `bb99accf48ed583e29b0efae56e28c963407b8df` was inspected, including its MIT license and existing credits:

- `blocks/LecternManifest.java:27–112`: wood-material, hardness 1.5, maximum-light, nonopaque two-high directional block, accessor flag, gold tooltip, cutout OBJ rendering and Manifest activation. It has no owned inventory or block entity.
- `blocks/templates/BlockDirectionalTemplate.java:40–43`: placement uses player yaw minus 90 degrees. Its facing property includes all six directions even though ordinary placement is horizontal.
- `blocks/templates/BlockTemplate.java:132–137,160–199`: installs the upper accessor. Lectern `neighborChanged` clears a part only when its companion position becomes air; it does not clear solely because that position contains a different solid block.
- `recipes/lectern_manifest.json`: original shaped Manifest/stick-wood/plank-wood recipe. Modern equivalents reuse the existing wooden-stick ingredient tag and vanilla planks tag.
- `blockstates/lectern_manifest.json`, its OBJ/MTL, and referenced PNGs: original geometry, orientation and hand/GUI transforms. The accessor's transparent cube has no visible geometry.
- `lang/en_us.lang:103,205`: original name and tooltip.

## Implementation and approval status

Original restoration within the requested 0.0.2 scope, with existing placement permission/conservation requirements. No new storage access, owner assignment, extraction, recipe cost or piston-blocking policy is introduced. Applies to 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

- `blocks/LecternManifest.java` and `items/LecternManifestItem.java`: registered two-high block/item, original facing/accessor state, light/hardness, full-block collision with nonopaque presentation, axe mining tag and native ordinary item loot. No block entity or network identity is assigned.
- The item follows the existing column-placement validation/rollback pattern: check both positions for replaceability, player permissions, world limits and entity obstruction before consuming the item. Preserve foreign replacements on interrupted writes. Normalize the root after modern item block-state import so it cannot become a second accessor.
- Either part uses the existing server Manifest opening route for the interacting player, not the placer. Local protection/spectator checks remain; current personal/Hive visibility and read-only behavior remain owned by the existing Manifest implementation. Sneaking is not a new lectern-specific rejection.
- Preserve original air-only neighbor cleanup and native piston reaction rather than silently introducing a new solid-replacement or immovability policy.
- Original recipe, creative entry, translation, tooltip, loot and axe tag are installed. The native recipe uses the existing `${result_key}` expansion, unlike custom Gem Cutter recipe results.
- `scripts/port_lectern_assets.py`: reproducible pinned conversion, with a check mode and refusal to overwrite differing resources. OBJ/MTL/PNG bytes are retained; transforms use the existing conversion function. All six facing states and both accessor values have model variants. Forge/NeoForge use the native OBJ loader; Fabric reuses the existing OBJ adapter. New textures are explicitly stitched.
- Packaged verification checks production/source classes, model-loader expansion, geometry/texture bytes, atlas entries and the version-specific recipe schema.

## Verification

Executed on 2026-09-12:

1. `timeout --foreground 10m ./gradlew build --no-daemon`: exit 1, 21 seconds; `build/manifest-lectern-20260912-073305.log`. Custom shared GameTest roots compile verbatim, so their Stonecutter comments did not select older native APIs. Moved the version-specific calls into existing test adapters.
2. Same command: exit 1, 74 seconds; `build/manifest-lectern-second-20260912-073718.log`. The 1.20 suites passed. The 1.21 Fabric recipe load rejected an `item` result key (`No key id`), and native acquisition failed. Fixed the resource placeholder and included the model in each loader's resource expansion.
3. Same command: exit 0, 88 seconds; `build/manifest-lectern-final-20260912-074048.log`. All four native suites passed, including the lectern regression.
4. `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, 0 seconds; `build/manifest-lectern-artifacts.log`. All four production/source pairs pass.
5. `timeout --foreground 30s python3 scripts/port_lectern_assets.py /tmp/arcane-archives-reference/migration-source --check`: exit 0; all 10 original/converted resources reproduce from the pin.
6. Structural OBJ inspection confirms supported record types and positive position/UV/normal indices, with 89 quads and 16 triangles. This is not a native bake/rendering test.
7. `git diff --check`: exit 0; project Java process check found none.

`ManifestLecternLifecycle` exercises native item placement in every horizontal orientation, both accessor states, absence of owned entities, native activation dispatch for both halves, one-item loot and companion cleanup when either half is destroyed, and refusal/payment conservation when the upper position is occupied. It resolves the shipped recipe with oak and crimson planks and extracts through the native crafting result slot, asserting the complete recipe payment and single output.

The fixture uses mock players: successful activation dispatch is NOT proof that a connected screen opened. No connected client was launched. Actual model baking/rendering, held-item/sneak use, two-player personal/Hive views, world restart, piston behavior and event-cancelled/reentrant placement acceptance remain in the consolidated campaign. The copied rollback pattern is not claimed fully re-tested for every lectern interruption path. Tracking/HUD, Brazier, complete Tome and full 0.0.2 acceptance are still incomplete. No commit, version bump or publication performed.
