# Brazier automation contract

Status: Forge/NeoForge insertion-only capabilities and Fabric transactional item-storage registration implemented. Broader native caller, sound and lifecycle acceptance remains open.

The user approved [0146](../behavior-changes/0146-brazier-automation-direct-setter.md): preserve ordinary insertion/remainder semantics without exposing the upstream void-returning direct setter. Forge/NeoForge native capability tests now verify that omission.

Baseline: upstream `bb99accf48ed583e29b0efae56e28c963407b8df`, `src/main/java/com/aranaira/arcanearchives/tileentities/BrazierTileEntity.java`.

## Reachable capability

Lines 120–128 expose the same `FakeHandler` for the item-handler capability regardless of side, including an unsided query. It is not an ordinary input inventory and does not persist items locally.

## Handler operations

Lines 404–448 implement `IItemHandlerModifiable`:

- `getSlots()` returns 999; `getSlotLimit(slot)` returns 999.
- `getStackInSlot(slot)` and `extractItem(...)` always return empty.
- `insertItem(...)` delegates one offered stack to routing with the caller's simulation flag. It returns the first exact nonempty remainder; only complete real acceptance attempts the gated pickup sound. Partial acceptance does not attempt that sound.
- `setStackInSlot(...)` is a deposit command, not assignment: it routes immediately, ejects rejected remainders and attempts sound. It does not install a stack in a local slot.
- The original methods do not use the slot number to distinguish destinations. The reported virtual slots do not create independent routing capacity.

Original routing/cache rules remain those in BRAZIER_ROUTING_CONTRACT.md. In particular, repeated simulation must not count the same preferred destination's capacity twice, and source payment belongs to the automation caller.

## Port boundaries

The existing `BrazierBlockEntity.insert` and routing/cache engine provide the single-stack insertion primitive, but that alone does not expose native automation or establish source payment. Player deposit and dropped-item tests do not establish hopper/capability/Transfer API behavior.

Forge/NeoForge adapters preserve insertion-only reads/extraction, side exposure, exact remainders and simulation semantics. Approved 0146 omits the direct setter entirely rather than introducing a buffer or exceptional ejection path.

Fabric uses transactions rather than a boolean simulation flag. A wrapper that immediately calls the nontransactional real insertion method cannot claim rollback safety merely because the router supports simulation. Trace and reuse the installed Chest/Trove/Gem Cutter transaction mechanisms before implementing the adapter. Aborting a caller transaction must roll back actual destination changes, including partial/multi-destination transfers; no source or destination payment may be deferred into an unchecked later insertion.

Preserve the existing device-live/ownership/audience/dimension/radius/current-eligibility checks. An automation lookup must not turn a removed or detached instance into authority, expose arbitrary monitored containers as destinations, or force-load chunks.

The direct setter is deliberately not exposed under approved 0146. Records 0144 and 0145 do not supply an owner-independent automation recovery buffer by implication.

## Required verification

### Fabric destination audit

The existing destination APIs are not interchangeable transaction adapters:

- `inventory/ChestFabricStorage.java` snapshots individual slots through `SingleStackStorage`, writes via `setTransactionalStack`, and emits `storageChanged` on final commit. Its generic `CombinedStorage` iteration is not the router's occupied-matching-first, empty-slots-second order (`ExtendedItemStackHandler.insertItemStacked`). Preserve that order when using its slots for routing.
- `inventory/TroveFabricStorage.java` snapshots both the stored stack and the separate LOCK reference. Its VOID insertion can report the entire offered amount accepted without increasing stored count. Snapshotting only inventory counts would miss LOCK state; deriving acceptance solely from count deltas would break VOID.
- `GemCuttersTableBlockEntity.acceptRoutingInput` currently delegates to nontransactional crafting-state insertion and calls `setChanged` immediately. It is not an existing Fabric transaction adapter. Preserve matching-occupied-input-only routing and provide rollback for those inputs before registering Brazier Transfer API access.
- `data/BrazierRoutes.accept` currently invokes these nontransactional destination methods directly. `BrazierRouteCache` also owns preferred-route selection and complete-acceptance cache refresh. A separate Fabric loop must not silently discard preference, current authorization checks, source nonmutation, or partial-acceptance cache rules.

Implementation must distinguish reversible destination writes from irreversible notifications/sound. Reuse native per-destination transaction participants where possible; do not simulate now and perform unchecked payment in a commit callback. Tests must mix ordinary native destination access with Brazier access in the same outer/nested transaction, not merely test a private snapshot in isolation. This is a code audit, not implemented Fabric automation or rollback evidence.

- Native sided and unsided lookups, hopper or other actual caller payment, insertion-only reads/extraction and published slot contract where applicable.
- Full/partial/zero acceptance and exact independent remainder; full-success-only insertion sound and muted/simulation behavior.
- Fabric outer/nested transaction commit and abort across mixed destinations, with real source payment and no capacity double-counting.
- Replaced/removed/unavailable devices and destinations, access revocation, no forced loads, existing single-stack/player regressions.
- Verify omission of the modifiable direct setter under approved 0146; do not introduce a buffer or automation ejection path.

## Runtime checkpoint

Partial/refused sound checkpoint: `build/brazier-partial-sound-20260913-123350.log` (four-target build/native suites, exit 0, 53 s). Both Fabric fixtures commit an oversized partially accepted request, followed by refused and zero requests against full destinations, and verify the real sound throttle remains untouched. Combined with the nested full-insertion checkpoint this verifies complete-acceptance-only trigger behavior, not audible playback.

Sound transaction checkpoint: `build/brazier-sound-transactions-20260913-123132.log` (four-target build/native suites, exit 0, 53 s). Both Fabric fixtures exercise every nested/outer commit/abort combination for full insertion. Test-only reflection observes and restores the actual `lastSound` throttle state: no trigger before outer close; only nested plus outer commit advances it. No production observability accessor was added. This verifies callback/gate timing, not audible playback or exhaustive partial/muted insertion sound behavior.

Long-request checkpoint: `build/brazier-long-request-20260913-122840.log` (four-target build/native suites, exit 0, 54 s). Both Fabric fixtures verify zero returns no acceptance, negative input throws, and `Long.MAX_VALUE` yields positive bounded partial acceptance exactly equal to the sum of changed destination counts. Aborting restores every destination slot. This covers overflow-safe finite-capacity requests, not exhaustive large VOID or sound-callback acceptance.

Four-target hopper checkpoint: `build/brazier-hopper-matrix-20260913-122617.log` (build/native suites, exit 0, 56 s). The shared native hopper fixture now runs on both Fabric targets as well as Forge/NeoForge, exercising registered loader automation paths for full/partial/refused acceptance and exact retained source. Removed the former Forge/NeoForge-only invocation rather than duplicate execution. This remains manually advanced native hopper ticks, not automatic world-loop scheduling.

Fabric device registration: `build/brazier-fabric-registration-20260913-122307.log` (four-target build, exit 0, 82 s); `build/brazier-fabric-registration-artifacts.log` (artifact verification, exit 0). `BrazierFabricStorage` exposes insertion-only native storage on every side and unsided, routes immediately through destination transaction participants, and snapshots pending sound for final-commit delivery. One call offers at most `Integer.MAX_VALUE` items to the item-stack router, returning exact partial acceptance for larger native requests rather than narrowing the long request unsafely; full-success sound requires acceptance of the entire native request. Both Fabric native fixtures verify lookup identity, empty iteration/no extraction, abort/commit destination conservation and retained removed-device rejection. Sound callback ordering/nested cancellation, large-request behavior, native Fabric hopper payment and broader lifecycle acceptance still need dedicated verification; audible playback is not claimed.

Gem Cutter mixed-route checkpoint: `build/brazier-gem-transactions-20260913-121922.log` (four-target build/native suites, exit 0, 53 s). Both Fabric fixtures verify Gem Cutter priority with an aborted probe, then route overflow to a Chest. The exact unpaid remainder, both destinations' shared abort/commit, unchanged offer and untouched empty Gem Cutter inputs are asserted. Device-level Fabric Transfer API registration and commit-only sound remain pending.

LOCK checkpoint: `build/brazier-lock-transactions-final-20260913-121650.log` (four-target build, exit 0, 54 s). Both Fabric fixtures first verify an empty unbound Trove refuses routing, seed one item through ordinary native storage inside the transaction, then route matching items and reject another item. Abort restores empty stock and empty LOCK reference together; commit preserves both. Initial `build/brazier-lock-transactions-20260913-121518.log` failed (1, 34 s) because the fixture wrongly expected routing to bind an empty Trove. The fixture was corrected without changing production semantics.

VOID transaction checkpoint: `build/brazier-void-transactions-20260913-121247.log` (four-target build/native suites, exit 0, 53 s). Both Fabric fixtures route overflow into a full Trove carrying the registered Devouring Charm, check complete acceptance without destination count growth, and pay through native source `InventoryStorage` in the same transaction. Abort restores the source; commit consumes the source while the Trove remains full. This verifies VOID accounting, not LOCK capture or registered device-level Fabric automation.

Mixed destination checkpoint: `build/brazier-mixed-transactions-20260913-120909.log` (four-target build/native suites, exit 0, 104 s). Both Fabric fixtures route one offered stack across a Chest and ordinary Trove, verifying exact partial remainder, unchanged offer and rollback/commit of both destinations. This exercises different destination adapters, but does not establish LOCK/VOID behavior, Gem Cutter mixed routing or registered Fabric Brazier automation.

Transactional route checkpoint: `build/brazier-transaction-routing-20260913-120601.log` (four-target build/native suites, exit 0, 81 s). Shared selection now accepts a destination transfer operation; the Fabric cache entry point reuses current authorization/ranking/preference and invokes existing Chest/Trove transaction participants or Gem Cutter transactional top-up. Chest routing retains occupied-matching-first ordering. Aborted probes retain permitted transient cache behavior as boolean simulations do; inventory rollback is separate. Both Fabric fixtures verify exact partial routing across two Chests, outer abort/commit and nested ordinary native destination extraction sharing the same snapshots. Mixed destination-type routing, device-level Transfer API registration and commit-only sound remain pending.

Multi-input rollback prerequisite: `build/gem-routing-multislot-20260913-120225.log` (four-target build/native suites, exit 0, 53 s). Both Fabric fixtures top up two occupied inputs in one transaction, checking exact independent remainder, no empty-input filling, and restoration/commit of both changed slots. This is multi-slot destination coverage, not multi-destination Brazier routing.

Underpayment rollback prerequisite: `build/gem-routing-underpayment-20260913-120001.log` (four-target build/native suites, exit 0, 53 s). Both Fabric fixtures deliberately offer more than the native source can pay, verify partial extraction and destination insertion occurred, then abort and assert exact restoration of both. Cancellation is controlled by the fixture; the destination primitive does not inspect or authorize arbitrary callers' sources. This is rollback evidence, not automatic underpayment detection or registered Fabric Brazier routing.

Native Fabric source-payment prerequisite: `build/gem-routing-source-20260913-115726.log` (four-target build/native suites, exit 0, 52 s). Both Fabric fixtures use native `InventoryStorage` over a seven-item `SimpleContainer` source and the transactional Gem Cutter destination in one transaction. Four accepted items are paid exactly; abort restores source/destination together, while commit retains the three-item source remainder. This tests a native transactional source plus the destination primitive, not a registered Brazier or multi-destination route.

Gem Cutter transactional prerequisite: `build/gem-routing-transactions-20260913-115423.log` (four-target build/native suites, exit 0, 81 s). A Fabric-only per-device snapshot participant now supplies `acceptRoutingInputTransactional`, preserving occupied-input-only acceptance and deferring dirty marking until final commit. Both Fabric native suites verify nested abort restoration, nested commit followed by outer abort, final outer commit, exact partial remainders, unchanged offers/output and untouched empty inputs. This is a destination primitive, not registered Fabric Brazier automation or mixed-destination transaction acceptance. Artifact verification initially identified the new anonymous participant class; its Fabric-only expectation was added explicitly.

Same-owner replacement checkpoint: `build/brazier-capability-replacement-20260913-114950.log` (four-target build/native suites, exit 0, 57 s). Forge/NeoForge native fixtures retain the original handler, remove the device and install an owned replacement at the same position. The old handler remains rejected without changing destination stock; fresh native lookup returns a different handler and deposits exactly the offered stack. This verifies replacement authority, not chunk unload/reload or process restart.

Native hopper caller checkpoint: `build/brazier-hopper-20260913-114732.log` (four-target build/native suites, exit 0, 56 s). Forge/NeoForge fixtures install a downward hopper above the owned Brazier and manually advance native `HopperBlockEntity.pushItemsTick`. A seven-item source pays exactly seven/two/zero items into available destination capacity; all unpaid items remain in the hopper, including repeated attempts against full storage. Fixture cleanup clears its source and restores destination contents. This exercises the actual native hopper payment/capability path, not automatic world-loop scheduling. Fabric native hopper routing remains pending its transactional adapter.

`build/brazier-automation-final-20260913-114403.log`: full four-target build/native suites, exit 0, 78 s. `build/brazier-automation-artifacts.log`: artifact verification exit 0. `BrazierAutomationLifecycle` verifies Forge/NeoForge native capability lookups on every side and unsided, no modifiable interface, 999-slot contract, full/partial/refused transfers, repeated simulation nonmutation, explicit fixture caller payment, independent remainders, empty reads/extraction, and ownerless/detached/removed rejection. Both Fabric suites also exercise shared adapter semantics, not Fabric Transfer API registration or rollback. Actual hopper scheduling, audible sound acceptance, broader lifecycle transitions and Fabric transactions remain open. Full automation parity is not claimed.
