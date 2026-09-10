# 0009 — Gem Cutter owned input boundary

Status: owned input handler and atomic consumption implemented under delegated behavior-change authority; full crafting/runtime integration pending.

## Upstream evidence

Pinned source `80944ce45c6559243d8928cc4b305bf379388652`, `tileentities/GemCuttersTableTileEntity.java:29-30,270`: 18 inputs in a TrackingGCTHandler extending ordinary Forge ItemStackHandler, plus a separate one-slot output inventory. These inputs are not Radiant Chest extended storage. `recipe/gct/GCTRecipe.java:149-159` extracts sequentially and invokes item-result callbacks; approved 0005 replaces partial consumption with conservation. The legacy subclass's tracking responsibilities remain unported.

## Decision and boundaries

Introduce a final owned Gem Cutter input handler with the original 18 slots, 64 slot limit and native item stack limits. Do not inherit the RadiantMultiplier capacity or extraction boost from the shared extended handler. Direct writes and complete decoded saves must satisfy these same input limits. Reject an oversized save atomically, leaving live contents unchanged, instead of accepting data that ordinary insertion could not create. Return detached stack reads so callers cannot bypass this boundary by mutating a live alias. These stricter direct-write/load/read API semantics are the deviation documented here; counts, accepted item types and ordinary insertion/extraction remain unchanged.

Under approved 0005, consumption matches a detached snapshot, prepares all deductions and returned stacks, rechecks every live slot for item/data/count changes, then commits all slots without external callbacks between them. An absent match or stale snapshot consumes nothing. A changed live inventory is not rolled back to the old snapshot. Returned consumed stacks are owned by the caller for later traced remainder handling; they are not crafting output. No arbitrary foreign inventory transaction support is claimed.

The handler is server-thread confined by caller contract, like the surrounding inventory API. It is not a synchronization or network authorization boundary. Actual block/entity integration must enforce thread, ownership, recipe and player conditions, persist changes, deliver output/remainders, and notify menus only after a completed operation. None of those effects are installed here. No generic vanilla remainder shortcut, partial foreign-handler rollback, recipes or textures are introduced.

## Compatibility, alternatives and authority

Applies to all four supported targets. Fresh-world inventory encoding reuses the shared handler's format with Size=18; there are no existing modern Gem Cutter saves or network packets to migrate. Valid unavailable-item data still follows the shared decoder's explicit rejection policy. No legacy save importer is added.

Alternatives: reuse extended capacities (changes original device capacity); allow unchecked setters and live aliases (bypasses the owned boundary); attempt sequential extraction from arbitrary handlers with best-effort rollback (cannot guarantee conservation); dispatch callbacks between deductions (exposes partial state). Rejected. Decision authority is the user's explicit delegation recorded in the behavior register. Full crafting remains covered by approved 0005 and unfinished.

## Verification

Regression coverage and actual execution results are recorded after the implementation run. Required cases: all-or-nothing split/overlapping consumption; no stale-preview authorization; reentrant mutation during matching; throwing predicate before mutation; detached read/consumption stacks; native capacities; invalid saved input rejected before commit. Forge's existing Minecraft-dependent test harness limitation must remain explicit.

Executed `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon`: exit 0, 27s, `build/gem-cutter-inputs-20260907-214604.log`. XML confirms 57 tests each on both Fabric targets and NeoForge, with zero failures/errors/skips, including eight new input-handler cases. Forge test sources compile but Minecraft-dependent tests were not executed; its previously observed bootstrap blocker remains unresolved. All four production/source artifact contracts and `git diff --check` pass. No runtime/full-crafting acceptance is inferred.
