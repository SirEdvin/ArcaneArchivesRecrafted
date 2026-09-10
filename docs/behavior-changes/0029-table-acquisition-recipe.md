# 0029 — Native Gem Cutter acquisition recipe

Status: implemented and scoped-tested under the user's delegated behavior-decision authority. Natural quartz production and complete gameplay acceptance remain unfinished.

## Pinned original and adaptation

Release `release/0.2.0.25-mixins8`, commit `bb99accf48ed583e29b0efae56e28c963407b8df`, `src/main/resources/assets/arcanearchives/recipes/gemcutters_table.json` describes `forge:ore_shaped`, pattern `DGP/WCW/QWQ`, output one Gem Cutter. D is stone metadata 3 (diorite), `blockMarble` or `stoneMarble`; G is `paneGlass`; P is `paper`; C is `workbench`; W is `logWood`; Q is Raw Radiant Quartz. The pinned file has an unmatched final brace: strict JSON parsing reports extra data at line 49. Its comment incorrectly names a Resonator. These findings establish the intended source definition, not successful loading in the legacy game.

The port uses a valid native `minecraft:crafting_shaped` recipe with the same pattern, counts, output and raw-quartz identity. Native horizontal mirroring is retained. It uses the existing version-specific result-key/resource-path expansion. The table is also included in the creative group, correcting its missing presentation entry from the earlier bootstrap.

Shared `arcanearchives:ingredients/` tags replace OreDictionary inputs:

- `diorite_or_marble`: diorite; optional `c:stones/marble`, `c:storage_blocks/marble`, `forge:stone/marble`, `forge:storage_blocks/marble`.
- `glass_panes`: ordinary and all sixteen vanilla stained panes; optional `c:glass_panes`, `forge:glass_panes`.
- `paper`: vanilla paper; optional `c:paper`, `forge:paper`.
- `workbenches`: vanilla crafting table; optional `c:workbenches`, `forge:workbenches`.
- `logs`: required native `minecraft:logs`; optional `c:logs`, `forge:logs`. This deliberately follows modern log membership, including modern woods absent from 1.12.

All optional aliases use `required: false`; all bridges use `replace: false`. These are explicit compatibility hooks, not proof every legacy mod has a matching modern tag. Untagged modded substitutes require a data-pack bridge. No dependency was added. Alternatives rejected: reproduce malformed JSON (no usable acquisition), hardcode vanilla-only inputs (lose interchangeability), recreate OreDictionary, or replace raw quartz with Nether quartz (change progression).

## Connected raw-quartz recipe chain

`items/RawQuartzItem.java`, `init/ItemRegistry.java` and `init/RecipeLibrary.java` on the same pin establish a registered `raw_quartz` ingredient and recipes: one raw quartz yields two Radiant Dust; two raw quartz yield one Shaped Radiant Quartz. Both paid definitions are now installed before Inlay/components in the menu catalog. Costs/output are unchanged. The original raw item model and static PNG are byte-identical, hash-checked by `verify_quartz_resources.py`; the legacy item sprite has an explicit atlas entry. Original EN/PT names and the unindented raw-item tooltip translations are retained; a separate tab-prefixed Portuguese cluster-description key is not normalized onto the raw item's key.

The raw item is only the ingredient/presentation part of its port. Its chest/crafting-table conversion requires the missing Radiant devices and the approved conservation correction in 0003. Resonator production, crystal growth/harvest, conversion, advancements and initial survival acquisition remain unfinished, not approved removals. The raw storage block and reversible packing recipes were subsequently implemented; see [raw storage](../migration/RAW_QUARTZ_STORAGE.md). No replacement ore generation or substitute acquisition was invented. Original tooltip descriptions do not certify those missing capabilities.

## Compatibility and verification

All four targets; fresh worlds only. No save/network format change, old-save importer, dependency upgrade or changed item limit. The new native table recipe participates in vanilla resource reload; custom Gem Cutter recipe-definition reload remains separate unfinished work.

`build/raw-quartz-recipes-20260908-111530.log`: standard matrix exits 0 in 30s. Each Fabric target executes 157 tests; NeoForge executes 160, zero failures/errors/skips. Forge compiles test sources only. NeoForge loader-aware tests pay both registered raw-quartz recipes across table/main-player inputs, reject Nether quartz and insufficient quantities, and cannot craft again without payment. Another test decodes the processed table JSON through the native recipe codec, matches normal/mirrored grids, rejects wrong/missing raw quartz and checks detached one-table output. Its tag bindings are controlled fixtures restored afterward, not third-party-mod acceptance.

Four artifact/resource contracts pass. Four dedicated servers load/reload, verify a 64-raw-quartz fixture stack, remove fixtures and save/stop. `build/raw-quartz-servers-20260908-111643.log` exits 0; individual reports and test totals are in `build/raw-quartz-verification.json`. Server runs precede a tooltip-only Portuguese correction; final resources are rebuilt and rechecked separately. No client/vision runs or connected-player crafting/restart acceptance were performed.
