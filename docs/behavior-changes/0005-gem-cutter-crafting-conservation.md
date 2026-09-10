# 0005 — Gem Cutter matching and crafting conservation

## Status

Approved; implementation in progress. The user explicitly answered “approved, continue” and delegated subsequent documented behavior decisions. This supersedes the earlier form timeout. Approval is not evidence that the full Gem Cutter transaction is implemented or verified.

## Original behavior

Pinned upstream: `80944ce45c6559243d8928cc4b305bf379388652`.

- `recipe/IngredientsMatcher.java:30-46` visits every ingredient for each inventory stack and accounts the stack's full count independently for every matching ingredient. There is no per-slot remaining-count budget shared between those ingredient checks.
- `recipe/IngredientsMatcher.java:60-78` builds consumption by slot. Multiple matching ingredient deductions use `matchingSlots.put(slot, discount)`, replacing the previous deduction instead of representing a conserved allocation.
- Counts are keyed by Ingredient object in `rebuildCounts` (`:22-27`), so repeated occurrences of the same Ingredient instance overwrite rather than add their requirements.
- `recipe/gct/GCTRecipe.java:82-94` delegates matching and slot calculation to this matcher. The public recipe constructors accept Ingredient and IngredientStack inputs, so overlapping/repeated requirements are within the recipe API's input surface, including custom/scripted recipes.
- `recipe/gct/GCTRecipe.java:149-159` extracts the calculated amounts without checking that all requested ingredients were actually obtained. `api/IGCTRecipe.java:91-113` explicitly documents that output can remain available when only partial consumption occurs.

These are direct control-flow findings, not a claim of a reproduced exploit in a running legacy Minecraft instance or proof that every built-in recipe triggers them.

## Proposed behavior

Across Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge:

- Allocate each physical item unit to at most one ingredient requirement per craft.
- Preserve every declared ingredient occurrence and count, including repeated and overlapping predicates; find a valid allocation when one exists rather than introducing greedy false negatives.
- Preserve recipe declaration order for recipe selection, exact output quantities, player/creator conditions and item-data matching rules.
- Build a complete consumption plan before mutation. Recheck authoritative inputs/conditions when applying it, and deliver no output unless the full plan succeeds.
- Make supported inventory mutation atomic or fully recoverable on failure. Preserve the original behavior for fluid containers, damaged tools and returned remainders; do not substitute generic crafting remainders without tracing those handlers.
- Validate malformed counts and client-selected recipes before mutation. No negative-count or partial-ingredient output path is retained.

## Reason

Avoid duplication, under-consumption and partial-craft loss while preserving the intended declared recipe costs and results. The port's server-authority and conservation requirements prohibit copying the unsafe paths merely for literal source parity.

## Scope limits

This does not authorize new recipes, cost/output rebalance, changed ownership rules, unavailable integration substitutions, dormant content activation, altered tank/trove capacity-removal rules, or changed tome-grant ordering. No legacy save import or cross-loader conversion is introduced.

## Alternatives

- Copy the matching/consumption logic literally: rejected as an implementation option because it retains the demonstrated conservation gaps.
- Disable Gem Cutter crafting: would remove reachable core progression and is not proposed.
- Reject all overlapping ingredients: simpler but narrows the supported recipe API; not proposed because valid allocations should remain craftable.

## Required verification

Regression fixtures for overlapping broad/narrow ingredients, repeated ingredient instances, split stacks, item-data distinctions, missing ingredients, changes between preview and commit, rejected/partial extraction, condition failure, container/tool remainders and output conservation. Run the Minecraft-dependent fixtures with loader-aware test execution; a plain-JUnit bootstrap failure is not a passed test.

## Implemented matching slice

- `recipe/IngredientAllocation.java`: deterministic integral allocation using residual paths. Each ingredient occurrence retains its own demand. Reverse paths allow broad/narrow matches to be reassigned, avoiding both double-counting and greedy false negatives. No item-unit expansion or summed integer demand is used; extended counts can have a total greater than `Integer.MAX_VALUE`. Inputs are not mutated.
- `recipe/IngredientStack.java`: positive counted ingredients, optional exact copied item data, and detached display stacks. Legacy explicit stack NBT maps to exact tags on 1.20.1 and exact item/components on 1.21.1 when the source stack has a component patch. Plain ingredients remain data-agnostic. Immutable counts replace the upstream mutable grow/shrink API; recipe definitions must replace a counted ingredient rather than mutate it during matching. This is an intentional API adaptation, not a claim of binary compatibility with 1.12 integrations.
- `recipe/IngredientsMatcher.java`: reads a copied inventory snapshot and returns either a complete per-slot allocation or no allocation. Matching cannot mutate storage or authorize delivery of a crafted output. Existing allocation results cannot corrupt later results.
- No saved-data or network schema changes occur in this slice. No assets were replaced.

### Verification

- Seven pure allocation tests pass on every target, including 2,000 seeded small cases compared with an independent exhaustive allocation oracle. Four Minecraft-backed matching tests pass on both Fabric targets and NeoForge.
- NeoForge now uses documented ModDevGradle `neoForge.unitTest { enable(); testedMod.set(...) }` support. Its unfiltered test task passed with 37 tests, zero failures/errors/skips: `build/neoforge-loader-tests-20260907-211321.log`, exit 0, 18s. No production workaround or test suppression was used to fix its bootstrap.
- Final command: `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:test --tests '*IngredientAllocationTest' assemble --no-daemon` — exit 0, 27s, `build/crafting-final-20260907-211539.log`. The Forge task is explicitly filtered to the pure allocator; this is not a green full Forge build. All four production/source artifact contracts and `git diff --check` pass.
- The initial broader attempt still reproduced Forge's pre-existing plain-JUnit bootstrap failure: exit 1, 16s, `build/crafting-allocation-20260907-210817.log`. Plugin API inspection confirms `LegacyForgeExtension` does not expose NeoForge's `unitTest` support. Forge Minecraft-dependent fixtures need a loader-aware alternative; they remain in source and are not disabled.

### Still pending

Recipe consumption bridge: `GCTRecipe.consumeIngredients(GemCutterInputHandler)` now uses the definition's matcher directly with the owned atomic handler, instead of accepting an external recipe or preview plan. This implements the approved input-conservation boundary without granting output or invoking effects. Three integration regressions cover stale previews/repeated costs, detached consumed stacks, exact data and unrelated input preservation, zero-ingredient recipes and null inventory rejection. The caller still owns condition checks, persistence and handling of every consumed stack; no full crafting API is exposed.

Verification: `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon` exited 0 in 28s (`build/gct-consumption-20260907-221103.log`). XML totals: 73 tests each on Fabric/Fabric/NeoForge, zero failures/errors/skips. Forge test compilation only; known Minecraft test harness limitation remains. All four artifact pairs and `git diff --check` pass. No new save/network format or assets are introduced.

`GemCutterInputHandler.consume` now implements all-or-nothing owned input consumption, recomputing against copied inputs and rejecting intervening item/data/count changes before mutation; see [0009](0009-gem-cutter-input-boundary.md) for the stricter input boundary and executed regressions. It returns consumed ingredients, not recipe output. No external inventory or effect callback runs between writes.

Server-authoritative full crafting commit, player/recipe condition revalidation, output delivery and remainder handling, Gem Cutter block/entity/menu/screen/network integration, recipe registration and progression remain pending. Input consumption fixtures are not full crafting-transaction/runtime tests. This proposal and steps 1–7 are not complete.
