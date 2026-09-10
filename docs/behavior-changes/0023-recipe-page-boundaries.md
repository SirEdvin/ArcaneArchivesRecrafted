# 0023 — Gem Cutter recipe page boundaries

Status: implemented under the user's delegated behavior-decision authority; menu/runtime integration pending.

## Upstream and preserved behavior

Pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`, `src/main/java/com/aranaira/arcanearchives/tileentities/GemCuttersTableTileEntity.java`, defines `RECIPE_PAGE_LIMIT = 7` and `previousPage`/`nextPage`. Valid nonempty pages wrap in catalog order. The previous-page calculation on an empty catalog produces -1. `setPage` accepts arbitrary integers; a page left beyond the catalog after removal is not normalized consistently by both directions.

## Modern behavior and rationale

Shared `GCTRecipeList` exposes seven-entry immutable page snapshots, page count and next/previous page calculations. Valid nonempty navigation preserves upstream order and wrapping. Empty catalogs have one empty presentation page, index zero. Invalid/stale page lookup returns an empty list; either navigation direction from an invalid index resets to zero. Negative values and integer extremes are checked before index arithmetic. Existing snapshots remain unchanged after catalog publication, including replacement/removal.

This prevents negative or stale presentation state without inventing recipes. A page is not a selected recipe or permission to craft: existing name-resolved server condition/input checks remain necessary. Read operations do not invalidate the catalog generation. Owning-thread confinement is unchanged.

Alternatives: copying negative empty pages was rejected as invalid presentation state; modulo-wrapping arbitrary stale inputs could unexpectedly select a different page; throwing for stale reads would make catalog shrinkage unnecessarily disruptive. Runtime menus must adopt these operations; no screen or packet handler is implemented here.

## Compatibility and authority

All four supported targets: 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. No save/network schema, recipe cost/output, asset or dependency change. No legacy-save conversion. The user authorized independently deciding behavior changes when properly documented; this bounded correction uses that authority. No publication/Git authorization is implied.

## Verification

Commit-boundary follow-up: `browsingPagesDuringCommitDoesNotInvalidateCatalogGeneration` exercises page creation/navigation during both condition gates of a paid craft, including rejected list mutation and detached preview modification. Complete consumption still succeeds exactly once with unchanged recipe output. Existing production code passes unchanged; no new behavior deviation. Scoped matrix below: exit 0, 24s, `build/page-commit-20260908-071702.log`; 136 tests per Fabric/NeoForge leaf, zero failures/errors/skips, Forge compilation only, all artifact pairs pass. This is owning-thread callback coverage, not concurrent browsing or a runtime menu test.

Two tests cover catalog sizes 0 through 22, exact seven-entry boundaries, complete ordered page concatenation, immutable snapshots, both-direction wrapping, integer extremes and stale pages after replacement/removal. Output access remains detached.

Command: `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon`.

Exit 0, 30s; full log `build/recipe-pages-20260908-071344.log`. XML totals: 135 tests on each executable Fabric/NeoForge leaf, zero failures/errors/skips. Forge test compilation only; its Minecraft-backed JUnit harness remains unresolved. `scripts/verify_artifacts.py` passes all four production/source pairs and `git diff --check` passes. No runtime menu, rendering, reload or gameplay acceptance is established by this slice.
