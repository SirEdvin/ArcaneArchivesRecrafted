# 0032 — Conserved Gem Cutter fluid-container returns

Status: implemented and headlessly verified under the user's delegated behavior-decision authority. Full remainder/gameplay acceptance remains open.

## Pinned original behavior

Release `bb99accf48ed583e29b0efae56e28c963407b8df`, `recipe/gct/GCTRecipe.java`, `handleItemResult`: query the consumed stack's fluid-item capability, drain tank zero's contents and retrieve its container. Return it to table inputs first, then the player's UP/main inventory, then spawn surplus at the player. Flint-and-steel additionally takes damage. `init/RecipeLibrary.java` includes water/lava bucket ingredients in the gated Rivertear/Mountaintear recipes; those abilities are not implemented by this change.

## Decision and implemented scope

Prepare fluid transformations on copies of exactly the allocated consumed units, never on live table/player stacks. Discovery and conversion each receive a single-item copy, including when the paid ingredient stack contains several containers. Support one nonempty tank/view per container, requiring extraction of its complete reported contents. Every allocated unit must prepare successfully before payment. This deliberately replaces the legacy whole-extracted-stack callback with per-container preparation so a multi-count handler cannot silently omit returns. Forge/NeoForge use native fluid-item handlers and `getContainer`; Fabric uses Transfer API with a detached SimpleContainer-backed single slot and a transaction. The Fabric context has no destructive overflow sink. No vanilla bucket allowlist is used in production.

Route the detached returns through the prepared post-payment table inputs and then main-player inventory, ascending slots and matching stacks before empty slots. Preserve native stack limits and item data. Prepare output placement after reserving return capacity. The existing final checks then publish payment, returns and output together through the menu's guarded table/player/cursor path. Pending crafting records remain blocked and untouched.

Intentionally reject the entire craft when returns/output cannot fit instead of introducing a world-drop side effect inside this commit. There is no partial payment, partially delivered return, or unconditional pending-result clear. This is a bounded adaptation from upstream surplus drops, not completed drop parity. Multi-tank/view, empty-fluid, partial extraction and unsupported crafting containers remain refused. Ordinary vanilla flint-and-steel is now supported under 0033; enchanted/custom tools remain gated. Milk containers are handled only if the native loader exposes a supported filled fluid handler; no invented milk-fluid or generic crafting-remainder fallback is added.

This preserves the supported fluid transformation and routing order while extending 0028's prior blanket refusal. Fully general returns, flint damage/enchantment behavior and world surplus routing remain migration work, not approved feature removals.

## Authority, compatibility and alternatives

All four supported targets; implemented using the user's documented delegation to decide and record behavior changes. No dependencies, recipes, textures, serialized block/pending formats or packet formats change. The native recipe data path can now use supported fluid containers; existing seven shipped recipes retain their original costs and outputs.

Rejected draining live capabilities before validation, using Fabric's withInitial/withConstant contexts for return preparation (they can discard overflow), hardcoding water/lava item replacements in production, and granting output while silently discarding unsupported containers. Callback exceptions propagate before owned payment and restore the menu guard. Detached arguments do not sandbox arbitrary third-party callback side effects. Existing separate player/world save limitations in 0028 remain; this is not a cross-file crash journal or multiplayer/restart acceptance.

## Verification and remaining acceptance

Stacked-container extension: `build/stacked-returns-20260908-141555.log`, exit 0 in 33s; 175 tests per Fabric target, 187 NeoForge, zero failures/errors/skips; Forge compilation and all four artifact pairs pass. A shared explicit policy fixture charges three from a stack of five, returns exactly three containers, and verifies second-unit refusal, full return capacity, detached callback arguments and no unpaid repeat craft. The policy fixture uses honey/glass bottles as stackable test data; it does not assert native honey-fluid registration or third-party container compatibility. Per-unit production conversion still uses the native APIs above. No new client/server acceptance is claimed.

`build/fluid-returns-final-20260908-134617.log`: exit 0 in 25s, 171 tests per Fabric target and 182 on NeoForge, zero failures/errors/skips; Forge test-source compilation. All four production/source artifact pairs and whitespace checks pass. Summary: `build/fluid-returns-verification.json`. The full matrix includes both the existing crafting regressions and the new return path; no new client/server startup run is claimed.

Native-menu regressions cover water/lava costs split across table/player inputs, matching-first table returns, player fallback, full-output cancellation, combined output/return capacity rejection, late access rejection, a subsequent clean craft, an unsupported flint cost after fluid preparation, and throwing preparation with guard recovery. NeoForge additionally exercises native capability drain on creator-tagged copies and refuses unsupported/empty/multiple-container preparations.

Fabric plain JUnit still lacks Transfer API mixins: its menu fixtures explicitly inject discovery and water/lava return policy doubles. These are transaction tests, not native Fabric fluid execution. NeoForge runs the production discovery and drain path; Forge currently compiles tests only. No connected-player, arbitrary-mod-container, server restart, world-drop or visual acceptance is claimed.
