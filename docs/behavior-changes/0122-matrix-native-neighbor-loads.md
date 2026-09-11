# 0122 — Matrix cleanup and native neighbor-triggered chunk loads

> Current scope: [0124](0124-matrix-loaded-only-cleanup.md) excludes removal with unloaded footprint chunks. Deferred cleanup and its tests have been removed; 0123 polling was not adopted. Native notifications remain unchanged. The implementation/status/results below are historical, not current obligations or blockers.


## Approval status

Approved by the user's explicit “Yes”: preserve vanilla neighbor updates and scope the no-forced-load guarantee to explicit Matrix chunk requests/tickets. Incidental native loads are permitted. Do not suppress native notifications. Runtime validation of the adjusted contract is in progress; 0121 remains approved.

## Original behavior and observed boundary

The port removes matching loaded Matrix parts through `Level.removeBlock(pos, false)`. Before approval, a fresh native lifecycle run reached actual chunk unload, loaded only the root chunk, then observed the adjacent chunk becoming loaded during `destroyBlock(root, true)`. That historical assertion failed; later verification below exercises the approved contract. It did not isolate every indirect load to Matrix cleanup: the initiating vanilla destruction also sends neighbor updates.

Pinned Forge 1.20.1-47.3.39 mapped source establishes the relevant call paths:

- `Level.java:293–295`: `removeBlock` calls `setBlock` with update flags 3.
- `Level.java:298–315`: `destroyBlock` also calls `setBlock` with flags 3.
- `Level.java:269–280`: these flags enable neighbor notification and shape updates.
- `CollectingNeighborUpdater.java:113–116`: neighbor notification reads adjacent block states without a chunk-availability guard.
- `Level.java:382–387`: `getBlockState` obtains the corresponding full chunk.

This is a modern native-engine compatibility boundary, not evidence about successful legacy Matrix runtime behavior. A `hasChunkAt` guard on the removed position alone does not prevent indirect native chunk loads.

## Approved behavior

Preserve ordinary native neighbor/shape notifications and narrow the guarantee to no explicit Matrix chunk requests, tickets or polling of unloaded chunks. Native updates initiated by removal may nevertheless load an adjacent chunk. Persisted identity checks and deferred cleanup still apply where positions remain unloaded.

The alternative is to retain the strict guarantee and design Matrix-scoped interception/deferred delivery of neighbor and shape notifications, including the initiating removal path. That requires separate implementation and tests for fluids, redstone, neighboring blocks and loader cancellation. Simply disabling notifications or weakening the test without approval is not acceptable.

## Reason

Preserving native update behavior and guaranteeing that no adjacent chunk loads are not equivalent requirements. Silent notification suppression risks changing nearby gameplay; silently allowing indirect loads weakens approved 0121.

## Affected targets

All four supported leaves: 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. The exact source trace above is from pinned Forge 1.20.1; other native paths still require individual tracing before any update-interception implementation.

## Verification

- `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 1, 60 seconds, `build/distillate-lifecycle-20260911-064359.log`. Initial obstruction fixture wrongly clicked the top of a blocked root, permitting a valid placement above it.
- Corrected the obstruction click to the underside without changing production behavior.
- Same build command: exit 1, 51 seconds, `build/distillate-lifecycle-fixture-20260911-064603.log`. Runtime failure: `Production cleanup force-loaded its neighbor`.
- The latter failure occurs inside a scheduled callback and aborts runtime execution; existing Fabric XML reports are stale and must not be treated as results of that run.
- Deferred reconciliation, true restart with pending work, cancellation, and full Distillate acceptance remain unverified. Remote native-test fixtures may remain in their ignored GameTest worlds after the abrupt failure; do not remove run directories or assume those fixtures were cleaned.

### Subsequent bounded GREEN verification

The historical failures above predate approval and fixture corrections. Production native notifications remain unchanged. The fixture now permits incidental native loads, preserves pre-existing pending work, and uses a non-dropping removal for its remote block-loaded/non-entity-ticking chunk. Ordinary drop-producing destruction still checks exactly one item for every part and horizontal facing in the loaded fixture. Scheduled failures use an exception handled by GameTest rather than an `AssertionError` that can abort the server before producing reports.

- Full build: `timeout --foreground 10m ./gradlew build --continue --no-daemon`, exit 0, 65 seconds; `build/distillate-final-20260911-072213.log`.
- Fresh Distillate XML cases passed on both Fabric leaves. Runtime completion: six required tests on each Fabric leaf, seven on Forge, six on NeoForge.
- Artifact verifier: exit 0; `build/distillate-final-artifacts.log`. All four production/source pairs passed, including fixture exclusion. Fixed missing Distillate pickaxe-tag membership and Forge/NeoForge OBJ-loader placeholder expansion revealed by this verifier.
- Consecutive builds also passed at the final fixture location, without moving it between successful runs.
- Verified bounds: all horizontal facing/part destruction combinations; actual native ticket-expiry unload and identity reload across an X boundary; immediate cleanup of originally loaded parts; a nonempty bounded cleanup queue; serialization/reconstruction; eventual reconciliation; no extra drops from non-dropping cleanup; stale work preserving a newly placed structure and stone.
- The serialized SavedData replacement is not a server restart. Its initial-ready pass means this fixture does not independently isolate chunk-event wakeup from startup reconciliation. Both require further focused acceptance, together with other cross-boundary facings/child removals, interruption/cancellation, and connected appearance.
- Older failed fixture locations were not erased. Current guarded fixture root is `(16495,90,16392)` in isolated GameTest worlds and is cleaned on successful completion. No project JVMs remained after the final run.

### Isolated native chunk-event wakeup

The shared fixture now separately lets its remote chunks unload again, adds cleanup for an already-removed position through the existing live SavedData instance, and waits five ticks to confirm the chunk remains unloaded and the queued entry remains pending. It then loads the chunk through the native server API and verifies the same SavedData instance drains that entry. No deserialization, synthetic event invocation or initial-ready pass can wake this new entry. This proves event wiring independently; it does not substitute for an actual server restart or additional live-part deletion cases.

All four leaves passed `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0, 56 seconds, `build/distillate-chunk-event-20260911-072543.log`. Fresh Fabric Distillate XML cases passed; required suite completions remained 6/7/6/6 in target order. Artifact verification passed all four production/source pairs (`build/distillate-chunk-event-artifacts.log`). No project JVMs remained. True pending-work server restart, other cross-boundary/removal combinations, interruption/cancellation and connected rendering remain open.

### Native placement interruption

The shared lifecycle fixture now interrupts placement at each of nine positions in each horizontal orientation (36 combinations per target). The existing test-only replacement callback handles Distillate as well as Reservoir. Assertions verify unchanged item count/creator data, restoration of every replaced water block, preservation of the callback's stone, absence of leftover part entities or item drops, and equality of the serialized pending cleanup tags. No production code changed for this increment.

- Initial fixture run: exit 1, 54 seconds, `build/distillate-interruption-20260911-073740.log`. The fixture used `removeBlock` to clear restored water, which retained its fluid state; the next iteration correctly rejected occupied space. Fixture cleanup now explicitly sets air rather than weakening the occupancy assertion.
- Full build: `timeout --foreground 10m ./gradlew build --continue --no-daemon`, exit 0, 56 seconds, `build/distillate-interruption-cleanup-20260911-073901.log`. Fresh Fabric Distillate cases passed; all required native suite completions passed (6/7/6/6).
- Artifact verification: `timeout --foreground 60s python3 scripts/verify_artifacts.py`, exit 0, reported duration 0 seconds, `build/distillate-interruption-artifacts.log`; all four production/source pairs passed, including exclusion of fault fixtures. `git diff --check` passed and no project JVMs remained.

This closes bounded direct-placement callback rollback coverage, not native event cancellation or arbitrary protection-mod compatibility. True pending-work server restart, remaining cross-chunk/removal cases and connected rendering still require acceptance.

### Native placement-event cancellation

The Forge and NeoForge `matrixReservoirPlacementCancellation` wrappers now also execute shared `MatrixDistillateCancellation` assertions for all four horizontal orientations. Their existing player-scoped `EntityMultiPlaceEvent` listeners cancel actual `ForgeHooks.onPlaceItemIntoWorld` / `CommonHooks.onPlaceItemIntoWorld` calls. Each iteration resets its observed snapshot count and requires exactly nine captured positions, FAIL, unchanged item count/creator data, restored water, no part entities or item drops, unchanged serialized pending work, and cleared native snapshot state. Listeners are removed in `finally`; owned fluid fixtures are explicitly cleared to air. No production change was required.

`timeout --foreground 10m ./gradlew build --continue --no-daemon` passed: exit 0, 56 seconds, `build/distillate-cancellation-20260911-074344.log`. All four native tasks actually ran and completed their required suites (6/7/6/6 in target order); the cancellation extension executes on Forge/NeoForge, not Fabric. Artifact verification passed all production/source pairs, including test-fixture exclusion (`build/distillate-cancellation-artifacts.log`, exit 0, reported duration 0 seconds). Whitespace checks passed and no project JVMs remained.

This verifies loaded placement-event cancellation only. It does not establish cancellation of removal, particular protection-mod compatibility, or Fabric equivalence to Forge-family snapshot hooks. True pending-work process restart, remaining cross-boundary cases and connected rendering remain open.

### Expanded boundary regression — current RED

See proposed 0123. The expanded fixture covers an X-boundary root control plus upper-child removal in all horizontal orientations across X/Z boundaries. Both 1.20.1 leaves can retain pending work after the test makes its chunk available and holds it there. The no-polling clause remains unchanged pending approval. The latest full build fails (`build/distillate-boundary-held-20260911-075607.log`, exit 1, 102 seconds); earlier green results remain historical bounded evidence, not acceptance of this new case.

Source inspection also qualifies earlier “actual unload” wording: `hasChunkAt == false` establishes loss of full availability, not necessarily completed asynchronous unload. A later availability transition need not produce a new native Load event. The new fixture is an availability-transition regression, not proof of complete disk unload/reload or process restart. Test-owned forced chunks are used only to hold the final explicit load through assertions and are released in `finally`; production performs no such requests.
