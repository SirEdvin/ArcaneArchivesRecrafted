# JEI/EMI combined tag-recipe identity collisions

## Original and currently approved behavior

Pinned upstream uses JEI. Decision 0113 adds EMI alongside JEI and retains supported combinations; it does not authorize silently disabling either viewer, suppressing diagnostics or removing tag-information categories. Independent Fabric 1.21.1 transfer acceptance is recorded in `../migration/RADIANT_CRAFTING_JEI.md` and `../migration/RADIANT_CRAFTING_EMI.md`.

## Observed compatibility issue

A real Fabric 1.21.1 client with the unchanged pinned pair JEI `EO4u1E2G` / `19.51.0.418` and EMI `on5GT1qh` / `1.1.24+1.21.1+fabric` successfully starts and performs paid Radiant Crafting Table transfer. However, EMI reports 348 duplicate-ID diagnostics covering 174 distinct `jei:/...` tag-recipe IDs. No colliding ID in this run contains the `arcanearchives` namespace. The count is parsed from the complete log, not inferred from the GUI warning total.

Example: `2 recipes loaded with the same id: jei:/minecraft/logs`.

Pinned bytecode inspection locates an identity mismatch:

- JEI `JeiInternalPlugin` creates tag-information categories scoped to registries.
- JEI `TagInfoRecipeMaker` enumerates tags within a registry, retaining the registry in its source `TagKey`.
- JEI `TagInfoRecipeCategory.getRegistryName` returns only the tag's identifier, not its registry/category.
- EMI `JemiRecipe` constructs its global `jei:/...` ID from that returned identifier without adding the category/registry.

Therefore different registry/category tag entries with the same tag identifier can collide after bridging. This is source-level explanation supported by live duplicate diagnostics, not a separate minimal reproduction with Arcane Archives removed. No item duplication or lost craft payment was observed. General tag-browser correctness is not established by the successful crafting scenario.

## Proposed next step — not adopted

Evaluate a compatible official JEI/EMI release pair and propose or apply verified pins with user approval, keeping Minecraft and loader versions unchanged. Preserve optional-mod status, both viewer integrations and tag information. Verify metadata/dependencies and actual combined runtime before accepting a candidate; do not assume a different release fixes the issue.

## Selected behavior and approval

The user explicitly chose: "Just ignore it, it is not important, I believe". Accept the observed bridged tag-recipe ID collisions as a known limitation and exclude their repair from migration acceptance. Do not investigate or change dependency pins solely to resolve this issue. Keep both viewers, tag categories and diagnostics unchanged; ignoring the issue is not suppressing its logs or claiming it fixed.

Status: closed by user-approved exclusion; no compatibility approval blocker remains. No production/dependency changes or workaround were implemented. The bounded combined-client transfer/payment evidence remains valid; broader viewer, other-loader and multiplayer acceptance remains separate.

## Affected targets

Reproduced on Fabric 1.21.1 only. Other target combinations remain unverified for this issue. Any changed pin must retain the complete supported target matrix and the separately approved KubeJS constraints.

## Verification performed

Private Xvfb with `ORG_GRADLE_PROJECT_optionalIntegrationMods=jei,emi`, running:

`timeout --foreground 10m ./gradlew :1.21.1-fabric:runClient --args='--quickPlaySingleplayer "Combined viewer verification"' --no-daemon --console=plain`

Exit 0, 171.08 seconds. Full log: `build/viewers-combined-live-1789132848115929031.log`. The diagnostics are now an accepted limitation, not a repair blocker. This successful process exit alone still does not establish full coexistence acceptance.

Used a separate copy of the prior isolated EMI world. Initial native queries confirmed an empty grid, four Lanterns and one pre-existing Crafting Table. Granted one Stick and four Oak Planks, put the Stick in the grid, then used the visible EMI Fill Recipe action with both viewers installed:

1. The unrelated Stick returned to player inventory; four Planks occupied grid indices 0, 1, 3, 4. Transfer itself granted no output.
2. Normal output pickup consumed the grid and increased Crafting Tables from one to two.
3. Another fill attempt displayed **Not enough ingredients to craft**.
4. Final native queries confirmed an empty grid and exactly one Stick, four Lanterns and two Crafting Tables. Bookmark ghosts remained empty.

This verifies bounded occupied-grid transfer/payment through the combined setup's visible EMI UI; it does not independently identify which bridge/native handler EMI selected internally or prove both UIs separately.

Normal save, all-dimension shutdown and Gradle success were inspected; no project JVMs remain. The only other ERROR was the known unavailable headless SoundSystem. Owned fixture and screenshots archived at `/tmp/arcane-viewers-combined-1789132840814382561/` (`combined-occupied-grid-filled.png`, `combined-paid-once.png`, `combined-missing-items-refused.png`, `Combined viewer verification/`). No production code changed in this increment; prior four-leaf build evidence is not recast as a fresh build.
