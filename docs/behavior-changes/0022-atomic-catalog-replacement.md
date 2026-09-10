# 0022 — Atomic whole-catalog replacement

Status: implemented under the user's delegated behavior-change authority. Runtime reload binding remains pending.

## Upstream and decision

Pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`, `recipe/gct/GCTRecipeList.java`: the catalog exposes its mutable insertion-ordered map and supports individual add/replace/remove operations with cache invalidation. It has no validated whole-catalog replacement transaction. Decisions 0012 and 0018 already establish detached snapshots and generation-guarded consumption.

Add `GCTRecipeList.replaceAll(List<GCTRecipe>)`: copy the supplied list and prepare a new insertion-ordered map before publishing it. Null batches/entries and duplicate names (including the same object repeated) fail without changing the catalog, cached snapshot or generation. Empty batches intentionally clear the catalog. Successful publication invalidates snapshots and advances the existing generation even when definitions are unchanged; an in-flight craft is cancelled. Previously returned snapshots remain stable.

This is a server-thread-confined internal publication boundary, not concurrent-map support or a resource reload listener. Caller collection/predicate side effects are not sandboxed. Single-entry `addRecipe` retains its overwrite behavior; duplicate rejection applies only to complete batches.

## Rationale, compatibility and alternatives

Validate before replacing rather than clear-and-rebuild, which risks leaving a partial catalog after a malformed definition. Reject duplicate batch names rather than silently allowing order-dependent last-wins results. Reuse the existing generation guard rather than adding a second transaction mechanism.

All four targets are affected. No recipe costs, item outputs, assets, dependencies, save format or network format change. No upstream runtime reload parity or legacy integration API compatibility is claimed. Parsing, server reload lifecycle binding, player conditions and runtime Gem Cutter integration remain open.

## Verification

Three tests cover ordered replacement and removal of absent names, caller-list ownership and old snapshot stability, empty publication, null/duplicate rejection without cache changes, same-definition publication cancelling a craft without input deductions, and rejected batches leaving the generation valid for crafting.

Command: `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon`, complete output redirected to `build/catalog-batch-20260908-062205.log`. Exit 0 in 29s. XML: 125 tests per Fabric/NeoForge leaf, zero failures/errors/skips. Forge test compilation only; Minecraft-backed execution remains unresolved. All four artifact pairs and `git diff --check` pass. No runtime resource reload or crafting acceptance was performed.
