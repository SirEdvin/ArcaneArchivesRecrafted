# 0013 — Shared stacked insertion

Status: implemented under delegated behavior authority; crafting integration pending.

## Original behavior and source

Pinned Arcane Archives revision: `80944ce45c6559243d8928cc4b305bf379388652`. `recipe/gct/GCTRecipe.java:114-145` transforms consumed fluid containers/flint-and-steel and calls Forge `ItemHandlerHelper.insertItemStacked` on the crafting tile, then the player's inventory, then drops the remaining stack. Those external inventory operations are incremental, not a transaction spanning ingredient consumption and output delivery.

## Port decision

Provide `ExtendedItemStackHandler.insertItemStacked(stack, simulate)` as a shared owned-handler operation for Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Fill data-equal occupied slots in ascending index order, then empty slots in ascending order. Delegate capacity, item acceptance, change hooks and simulation to the existing per-slot `insertItem`. Copy the caller input and return a detached remainder. Reject null rather than interpreting it as an empty stack. Empty input is a no-op.

The ordering is explicitly defined here rather than claiming complete equivalence with every Forge helper version. In particular, extended handlers may merge identical normally unstackable items when their existing per-slot policy permits it; native Gem Cutter inputs still accept only native capacities. This preserves existing shared-handler capacities instead of imposing a separate utility-level capacity. No recipe costs, outputs, save/network formats or assets change.

Reason: Fabric must share the same remainder-placement policy without importing Forge. Reusing per-slot insertion keeps validation and conservation in one place. Rejected alternatives: loader-specific duplicate loops; scanning empty slots before compatible occupied slots; replacing the existing handler API with a speculative transaction framework.

Authority: user delegated properly documented behavior decisions. No publication is authorized.

## Verification and limits

Three new tests in `InventoryHandlerTest` cover matching-before-empty order, simulation without reserved space or aliases, partial/full rejection, exact item data, native unstackable limits, upgrade prerequisites, empty input and null rejection.

Four-leaf command: `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon`. Exit 0 in 28s; log `build/stacked-insertion-20260907-222046.log`. Forge test compilation is not Minecraft-backed test execution; the existing harness blocker remains.

This is not atomic across slots or arbitrary subclass callbacks. It assumes the existing handler contract, non-reentrant insertion hooks and owning-server-thread use; simulation reserves nothing. Callers must retain/use returned remainders and handle exceptions without blindly retrying the original input after a partial insertion. Fluid-container transformation, tool damage, player-inventory fallback, drops, persistence and full server-authoritative crafting remain unimplemented. No full Gem Cutter or migration completion is claimed.
