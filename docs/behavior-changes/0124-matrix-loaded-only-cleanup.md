# 0124 — Matrix loaded-only removal scope

## Approval

Approved directly by the user: “Just ignore usecase with unloaded chunks and assume all chunks with matrix is loaded when it broken”. Supersedes deferred cleanup requirements in 0121, the unloaded-cleanup scheduling portion of 0122, and proposed 0123 (not adopted).

## Original and approved behavior

The port previously persisted identity-keyed removal work for unavailable Matrix parts and relied on native load events to resume it. Expanded tests exposed stranded pending work. The user excludes this use case rather than authorizing a retry scheduler.

Support removal with all footprint chunks already loaded. Immediately remove matching loaded parts; preserve structure identity, foreign replacements, ordinary notifications, placement preflight, rollback, cancellation and no-extra-drop behavior. Retain non-loading availability guards as defensive checks. Skip unavailable parts without queuing work, polling, scheduled retries or explicit chunk loads/tickets. Such parts may remain if the loaded-footprint assumption is violated; eventual cleanup is not promised.

Remove the pending SavedData implementation, event hooks and unloaded-boundary/restart/wakeup acceptance tests. Existing ignored development-world pending-data files are not deleted or migrated; the removed service no longer reads them. This does not remove ordinary persistence requirements for placed structures or authorize unfinished Matrix machinery.

## Reason and affected targets

User-directed scope reduction to avoid complexity for the excluded unloaded-chunk removal case. Applies to 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Verification

Implemented: removed MatrixCleanupData, MatrixCleanupEvents and their initialization; immediate identity-checked cleanup now lives in MatrixDistillate with a scoped reentrancy guard. Removed unloaded fixtures; retained loaded placement/destruction/interruption and Forge-family cancellation. Added loaded stone-replacement and foreign-identity preservation assertions.

- `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0, 81 seconds, `build/matrix-loaded-only-20260911-084134.log`. All four native tasks ran; required completions 6/7/6/6 in target order. Both fresh Fabric Distillate lifecycle XML cases passed.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, reported duration 0 seconds, `build/matrix-loaded-only-artifacts.log`. All four production/source pairs passed. Removed cleanup helpers are absent from the built JARs.
- `git diff --check` passed; no project JVMs remained after execution.

The earlier unavailable-to-available RED case is excluded by approval, not fixed. Full Matrix migration and connected appearance remain open. The implementation change left existing world files and unrelated work untouched; the bounded restart fixture below subsequently used guarded development-world space.

### Real Distillate process-restart verification

`timeout --foreground 10m python3 scripts/smoke_matrix_distillate.py` passed with exit 0 in 310 seconds. Complete command/recovery payloads and session reports: `build/distillate-restart-20260911-084900.log`.

- Eight distinct server sessions: setup/save/shutdown and restart/check/cleanup/shutdown on each supported leaf.
- Four operator-seeded Distillates per leaf, one for each horizontal facing, spanning loaded X/Z chunk boundaries. Every part has an explicit shared per-structure UUID. Native block-state and NBT predicates check all part positions, facings and exact identities before shutdown and after restart.
- Restored root/lower-child/upper-child removal clears matching loaded footprints. Four structures yield exactly four items per leaf; fixture cleanup and world save complete.
- Empty-space/entity guards protect existing contents. Temporary fixture chunk holds preserve prior forced state; logs confirm all introduced holds and temporary scoreboard objectives were removed. This is test setup, not a production chunk-loading mechanism.
- Parsed all eight session reports and checked every marker, clean shutdown/save, and exit status. No ERROR/FATAL/Exception/command-parser failures appeared in their logs. No project JVMs remained.

This verifies native serialization and real-process reconstruction of seeded Distillate identities, followed by loaded-footprint cleanup. It does not establish player placement across restart, connected rendering, multiplayer/protection compatibility, crash recovery, or any unloaded-removal behavior. No production code changed in this verification increment.

### Native Distillate placement preflight

`MatrixDistillatePreflight` now runs within each loader's existing Distillate lifecycle GameTest. All four leaves pass 36 pig-collision placements (every part in every horizontal orientation), 12 permission/border/height rejections, and one no-player successful placement. The border fixture keeps the root inside while excluding another part, testing the complete footprint rather than just vanilla root validation. Rejections conserve creator-stamped item count/data and leave no parts, entities or item drops. Temporary player permissions, border settings and collision entities are restored in `finally`. The no-player control verifies all part states/facing/shared identity and subsequent loaded cleanup.

- `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0, 57 seconds, `build/distillate-preflight-20260911-085754.log`. Native suite completions 6/7/6/6; both Fabric Distillate XML cases pass.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, 1 second, `build/distillate-preflight-artifacts.log`; all four production/source pairs pass.
- No project JVMs remained. No production logic change was required. These are bounded native permission/collision tests, not proof of arbitrary protection-mod compatibility or connected rendering.

### Item-supplied identity regression and repair

This corrects the implementation of 0119's approved conserved placement/part identity contract; it adds no machine behavior or unloaded cleanup policy. The identity-only entity has no original upstream user-configurable block-entity fields (Distillate originally had no entity factory).

Native `BlockItem.place` applies item block-entity data after `placeBlock` installs the footprint and before `setPlacedBy` restores root identity or vanilla consumes the item. The original port allowed that import. A malformed `matrix_identity` therefore reached the strict saved-world loader too early. Both 1.20.1 native suites threw `IllegalArgumentException: Missing Matrix part identity`; both 1.21.1 suites caught/logged that error internally and completed. Source ordering establishes the 1.20.1 unpaid-placement window; this fixture does not claim a connected-player exploit reproduction.

`MatrixDistillateItem.updateCustomBlockEntityTag` now declines importing item NBT into identity-only parts, retaining the placement-generated UUID. It does not edit the offered item, weaken saved-world identity validation, alter native component handling or replace the normal payment/cancellation path. Reservoir has no block entity and does not share this import hazard. Core/Repository/Storage are not yet implemented and must preserve the same trust boundary when introduced.

The shared native lifecycle fixture supplies valid foreign UUIDs and malformed UUID values using actual legacy `BlockEntityTag` or modern `BLOCK_ENTITY_DATA`. For each horizontal facing it verifies one consumed item, unchanged remaining item data, correct part states, one fresh shared identity, complete loaded removal and exactly one drop. Test cleanup runs in `finally`.

- RED: explicit four-leaf `runGameTest`/`runGameTestServer` command with `--continue --no-daemon`, under `timeout --foreground 10m`; exit 1 in 79 seconds, `build/distillate-item-identity-red-20260911-091022.log`.
- GREEN: `timeout --foreground 10m ./gradlew build --continue --no-daemon`; exit 0 in 89 seconds, `build/distillate-item-identity-green-20260911-091311.log`. All native suites complete (6/7/6/6), both Fabric Distillate XML cases pass, and no Matrix identity exception or ERROR/FATAL line remains in the build log.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, reported duration 0 seconds, `build/distillate-item-identity-artifacts.log`; all four production/source pairs pass. No project JVMs remain. Connected multiplayer and broader Matrix acceptance remain open.
