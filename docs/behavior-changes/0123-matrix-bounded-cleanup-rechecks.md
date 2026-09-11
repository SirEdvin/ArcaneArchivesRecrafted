# 0123 — Bounded availability rechecks for pending Matrix cleanup

> Current scope: [0124](0124-matrix-loaded-only-cleanup.md) excludes removal with unloaded footprint chunks. Deferred cleanup and its tests have been removed; 0123 polling was not adopted. Native notifications remain unchanged. The implementation/status/results below are historical, not current obligations or blockers.


## Approval status

Proposed; awaiting user approval. No production polling or cleanup-policy change has been implemented. The expanded native regression is RED on both 1.20.1 leaves. This proposal changes the explicit no-polling clause of 0122, not the prohibition on Matrix chunk requests/tickets.

## Original behavior

Under 0121/0122, unavailable parts enter persisted, identity-keyed work. `MatrixCleanupData.remove` does not ready newly queued work; a native chunk-load event or SavedData's initial-ready pass must wake it. `tick` discards a ready marker when `hasChunkAt` is false and waits for a future event. Native neighbor notifications remain enabled.

The original upstream Matrix state/placement defects are documented in 0119. This decision concerns the port's approved deferred-cleanup implementation, not proven legacy runtime semantics.

## Observed failure and source evidence

The shared lifecycle fixture was expanded to an X-boundary root-removal control and upper-child removal in all four horizontal orientations, spanning X and Z boundaries. Each case repeats identity reload, cleanup, stale-work replacement protection and isolated wakeup against the same live SavedData instance. Guarded remote roots are `(16623 + case * 96, 300, 16623)`, case 0 through 4, in ignored GameTest worlds.

The isolated wakeup assertion queues an empty position while `hasChunkAt` is false, checks it remains pending for five ticks, then explicitly makes the chunk available. A test-owned forced chunk keeps it available during the next five-tick assertion window and is released in `finally`; pre-existing forced status is rejected. No production force-load hook was added.

Latest full build: `timeout --foreground 10m ./gradlew build --continue --no-daemon`, exit 1, 102 seconds, `build/distillate-boundary-held-20260911-075607.log`.

- Forge 1.20.1: root `(16623,300,16623)`, pending 1, baseline 0, **loaded=true**, yet newly queued work did not drain.
- Fabric 1.20.1 fresh XML: root `(16719,300,16623)`, pending 7, baseline 6, **loaded=true**, with the same failure. The pre-existing baseline is preserved, not assumed empty.
- Fabric 1.21.1 fresh Distillate XML passed; NeoForge completed all six required tests. These results do not make the four-leaf build green.

Earlier diagnostic runs:

- `build/distillate-boundaries-20260911-075058.log`: exit 1, 72 seconds; Forge isolated wakeup failed.
- `build/distillate-boundary-diagnostic-20260911-075243.log`: targeted Forge exit 1, 51 seconds; occupied remote footprint guard failed. Its source was not established as terrain versus prior residue; moving guarded fixtures to y=300 did not authorize deleting old state.
- `build/distillate-boundary-high-20260911-075410.log`: targeted Forge exit 1, 47 seconds; pending 1 versus baseline 0, but loaded=false by the assertion. The temporary test-owned hold removes that timing ambiguity from the latest RED reproduction.

Pinned Forge 1.20.1-47.3.39 mapped sources:

- `ServerChunkCache.java:243–246`: `hasChunk` tests holder/ticket availability, not completion of disk unload.
- `ChunkMap.java:513–529`: actual unload is asynchronous; it removes pending-unload state, marks the LevelChunk unloaded, posts Unload, saves and clears its block entities.
- `ChunkMap.java:732–740`: Load is posted only inside `entitiesInLevel.add(chunkpos)`; availability transitions need not imply a new Load event.
- `MatrixCleanupEvents.loaded` readies work through server execution; `MatrixCleanupData.tick` can remove readiness while availability is false.

These paths show why `hasChunkAt == false` alone is insufficient evidence of completed chunk unload and why event-only wakeup needs further treatment. The latest fixture proves unavailable-to-available reconciliation failure; it does not isolate whether each failure missed a load notification or consumed readiness before full availability. Earlier documentation's “actual unload” wording is qualified accordingly.

## Proposed behavior

Allow bounded, non-loading availability checks for **pending cleanup only**:

- Maintain fair round-robin retries of persisted pending chunk work, including when no fresh native Load event occurs.
- Bound availability/removal work per dimension per tick (retain the existing budget of at most 64 work steps); do not scan all devices or all pending chunks each tick.
- Use non-loading availability checks. Never explicitly request a chunk, install tickets, or load a chunk to satisfy cleanup.
- Retain unloaded work for future bounded checks, persistence and restart.
- Preserve UUID identity checks, unrelated replacements, newly placed structures, no extra drops, native neighbor notifications and loader cancellation behavior.
- Keep all testing-only forced chunks and fault fixtures out of production and source JARs.

This trades a small, bounded amount of idle checking for eventual reconciliation when native availability changes without a usable load-event wakeup. Dormant work is no longer strictly event-only. No new Matrix machinery, storage, processing, menus or gameplay outputs are proposed.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. The reproduced failure is on the two 1.20.1 leaves; a shared fix must preserve all four.

## Verification required after approval

- Make the current held-available RED regressions pass on all four leaves without dropping identity or baseline checks.
- Verify no explicit production chunk loads/tickets; dormant work remains persisted while chunks stay unavailable.
- Test budget enforcement and fair progress with more queued chunks/positions than one tick's budget.
- Retain existing placement, interruption, native cancellation, replacement and conservation tests.
- Repeat full matrix build and artifact checks; inspect fresh intended-test results.
- True process restart with pending cleanup, removal cancellation, connected visuals and full Matrix acceptance remain separate open tasks.
