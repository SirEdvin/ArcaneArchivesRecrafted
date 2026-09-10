# 0028 — Immediate paid Gem Cutter menu crafting

Status: implemented and scoped-tested under the user's delegated behavior-decision authority. This is not completion of the playable-machine milestone.

## Pinned original behavior

Release `release/0.2.0.25-mixins8`, commit `bb99accf48ed583e29b0efae56e28c963407b8df`, `inventory/ContainerGemCuttersTable.java`: the combined wrapper includes table and main-player inventories. The output slot's `onTake` consumes ingredients, processes returns, and invokes `onCrafted`. Output shift-transfer uses reverse player-menu order. Output clicks force dragType zero. `recipe/gct/GCTRecipe.java` drains fluid-capable ingredients and damages flint-and-steel before routing returns through table, player, then world. See 0005 for the approved conserved-allocation correction.

## Modern implementation and bounded differences

The real block entity now supplies the menu with its current owned state and live server-thread/device access check. A normal left/right output click pays for one entire output batch and adds it to the cursor; shift-click pays for one batch into main-player inventory, merging matching occupied stacks before empty slots in reverse release menu order. Offhand and armor are excluded. One batch per click is an explicit bounded alternative to native repeated shift-crafting; bulk-repeat parity remains open.

The menu resolves its selected resource name, snapshots table/main-player/cursor state, recomputes conserved allocation, and prepares all deductions and output insertion before changing live state. Insufficient complete output capacity cancels the whole craft, including when partial insertion would fit. Shift crafting may use space freed by its own ingredient payment. Creator-policy outputs use the live server player's UUID/name.

Final gates reject access/device changes, replaced state owners, catalog writes including remove/restore, changed tag membership, and changed player/cursor/table stacks. Both preview evaluation and crafting reject recursive menu craft, transfer, and navigation. Exceptions before publication produce no owned deductions or output; arbitrary external callback side effects are not rolled back. Loader capability probes receive detached stacks.

The state owner prepares and validates replacement table inputs before the final gates, then swaps them without external callbacks. The menu immediately writes prepared native main-player slots and cursor before dirty notifications or synchronization. The compare-and-replace input method alone is not crafting authority. Already-staged pending results block this path and are never cleared, refunded or delivered by it; 0021/0024 packing remains intact.

Initially flint-and-steel, crafting-container and fluid-capable consumed stacks were refused before payment. [0032](0032-gem-cutter-fluid-returns.md) now handles supported single-container fluid drains and reserves table/player return capacity inside this commit; unsupported cases remain refused with an English/Portuguese explanation. Ordinary damageable ingredients are consumed completely, matching upstream rather than treating every tool as a returned catalyst. Production uses the native FlintAndSteelItem type and Fabric Transfer API or Forge-family fluid capabilities, not a fluid-item allowlist. Generalized fluid/flint returns remain migration work. The initial Inlay definition retains all release counts and output.

## Compatibility, alternatives and acceptance limits

All four supported targets. No recipe-cost, asset, dependency, save-format or packet-format change. Native menu slots/cursor synchronization carries paid outputs; ghost slots still cannot be cloned, swapped, thrown, or extracted through native slot APIs. Client menus have no crafting backend; identity and authority are never obtained from submitted slot contents.

New immediate crafts use normal Minecraft player/world persistence, not a cross-file crash journal. Table and player saves are separate: this is not a claim of crash-atomic durability between them. Cursor close/disconnect and actual server restart/multiplayer acceptance still need runtime verification. No new pending-result delivery protocol is implied.

Alternatives rejected: granting a preview before payment; incremental capacity-dependent ingredient extraction; silently dropping tool/fluid returns; erasing old pending records; adding a distributed journal before ordinary native-menu integration. Supporting foundations remain shared, with loader differences limited to remainder-capability discovery.

## Verification

Ordinary-tool parity correction: pinned `GCTRecipe.handleItemResult` treats only fluid containers and `ItemFlintAndSteel` as returns; other tools are consumed, including the diamond sword used by `RecipeLibrary`'s gated `murdergleam` recipe. The port's blanket damageable-item refusal incorrectly blocked these costs. The new native-menu regression reproduced this on NeoForge (`build/tool-input-red-20260908-132240.log`, one test/one failure), then passed after narrowing the guard to native FlintAndSteelItem while preserving all fluid/crafting-container checks. It covers damaged diamond/iron swords, pickaxes and bows paid from table/player inventory, full-output rejection without item/damage mutation, detached offers and no unpaid repeat output. Flint-and-steel and water/milk bucket refusals remain tested. This restores inspected upstream behavior; it neither ports Arsenal abilities nor implements returned tool/fluid processing.

Corrected matrix: `build/tool-input-green-20260908-132353.log`, exit 0 in 33s, 166 tests per Fabric target and 176 on NeoForge, zero failures/errors/skips. Forge test sources compile; four artifact pairs and whitespace checks pass. NeoForge executes native capability lookup, while Fabric retains its explicitly supplied fixture policy. No new client/server smoke or connected-player acceptance is claimed for this one-line runtime correction.

The initial paid-crafting matrix passed in `build/gem-cutter-paid-final-20260908-105035.log` (exit 0, 25s): 157 tests per Fabric leaf and 158 on NeoForge, zero failures/errors/skips; Forge test compilation only. The interrupted server-check job completed successfully: all four `smoke-server-*-20260908-105*.json` reports were read back with every check true and clean save/stop. These were registration/reload smokes, not connected-player crafting tests. The subsequent storage-component extension and consolidated current verification are documented in `../migration/STORAGE_COMPONENTS.md` and `build/storage-components-verification.json`.

`GemCuttersTableCraftingTest` exercises native menu actions with an owned-state-backed input adapter: repeated combined costs, offhand exclusion, whole-batch cursor/shift delivery, capacity freed by payment, partial-capacity rejection, malformed actions, final access failure/exception, late mutations, catalog ABA, reentry, state replacement and pending-result preservation. Additional coverage checks tag changes during matching and the actual registered Inlay definition on loader-aware NeoForge.

Plain Fabric JUnit does not apply the Transfer API `ItemVariantCache` mixin: the initial run failed in the real fluid lookup, not payment assertions. Fabric transaction fixtures therefore supply an explicit remainder-policy test double at a package-private constructor seam; no production fallback or fake mixin was added. NeoForge uses the production capability lookup and registered content. Forge remains test-source compilation only. Fixture tag memberships are restored after each test. These tests are not connected-player, client-packet, world-restart or visual acceptance.
