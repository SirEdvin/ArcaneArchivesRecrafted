# Gem Cutter real output inventory

## Approval

Approved by the user: use input/output slots, prohibit insertion into outputs, and keep the implementation simple. This supersedes the unused staged-paid-result design; 0127's malformed-carrier proposal is not the selected architecture.

## Original behavior

The current menu crafts immediately into the cursor/player inventory. A separate unused staging API persists a pending result and consumed-input journal; normal menu crafting never creates that state.

## Approved behavior

Keep explicit player-triggered crafting and recipe selection. Store completed output in a real saved output slot. Check payment, container returns and output capacity before committing; failed crafting changes nothing. Output extraction never crafts. Manual insertion and shift-click routing cannot insert into output. Preserve creator data, native access checks and container returns. Drop real input/output contents on removal, not a special pending-craft carrier.

All four version/loader targets are affected. The user explicitly approved dropping pre-release backward compatibility, including development-only pending-craft saves and item carriers. No conversion layer or old journal reader is retained. Worlds created with this inventory format must persist their current input/output contents correctly.

## Verification

Subsequent [real Fabric 1.21.1 client acceptance](../migration/GEM_CUTTER_CLIENT_ACCEPTANCE.md) verifies player placement, paid GUI crafting, player-crafted output persistence across full process restart, native extraction/insertion refusal and exact conservation. This supersedes the absence of connected-client evidence below only for those scenarios on that leaf; other loaders, remote multiplayer and broader visual coverage remain open.

Implemented in the existing inventory state, table entity, menu and screen, without a timed-machine framework or new dependency. Recipe selection remains a non-extractable preview; the Craft button uses the native menu-button packet. The output slot supports ordinary extraction but rejects insertion, including shift routing and native hotbar swapping into the slot. Crafting never depends on cursor/player output space; only input/return space and the real output slot are reserved. Existing table-plus-player ingredient sourcing is retained.

- `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0, 84 seconds, `build/gem-cutter-output-native-20260911-115205.log`. All native suites pass (7/8/7/7 in target order); both Fabric XML ownership/crafting cases are fresh and passing. Forge's transformed shared assertions report 206 successful tests. No ERROR/FATAL lines in the build log.
- Native Reservoir crafting preserves exact payment and all ten returned Radiant Tanks. Native Lantern crafting fills the output to capacity, refuses the next paid craft unchanged, and checks right-click splitting, refused insertion/hotbar swap, hotbar extraction, partial shift extraction, and no crafting during extraction. Reopening a menu retains completed output. Shared state/menu regressions cover stale authorization/catalog/tag snapshots, failed returns, output limits, detached data and transactional serialization.
- Native removal/replacement fixtures drop remaining inputs and creator-stamped completed output exactly once with one empty table. Re-placement does not recreate dropped contents. Forge/NeoForge cancellation preserves supplied current-format inventories and native snapshot state. These cancellation hooks are not Fabric protection-mod acceptance.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, reported duration 0 seconds, `build/gem-cutter-output-artifacts.log`; all four production/source pairs pass, including test-fixture exclusion.
- `timeout --foreground 10m python3 scripts/smoke_device_ownership.py`: exit 0, 221 seconds, `build/gem-cutter-output-restart-20260911-115357.log`. Eight separate server sessions verify owned/unlinked devices, ordinary inputs, creator-stamped output, exact entry counts and absence of journal fields before/after restart. All checks and target aggregates pass. Session logs have no ERROR/FATAL/Exception or command-parser failures; fixtures, temporary objectives and owned chunk holds are cleaned and servers save/stop. No project JVMs remain. This is operator-seeded persistence, not player-placed or connected-player restart acceptance.

The old malformed custom item-data placement proposal (0127) was not implemented; removing its obsolete carrier fixture is not a fix for arbitrary malformed item data. Connected-client visual/input inspection, multiplayer and broader migration acceptance remain separate from these native fixtures.


## Two-viewer native server acceptance

The shared native crafting fixture now opens two actual menus for distinct mock-player UUIDs on one table. It checks that both menus read the same output, only the successful crafter's player ingredients are charged, another viewer cannot submit actions through the wrong menu, a delayed extraction cannot duplicate an already-taken batch, and closed/out-of-range menus cannot extract or craft. Alternating viewers receive only their existing paid batches. Removing the table with both menus open drops its unclaimed output once and invalidates both viewers. No production fix was needed.

- `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0, 68 seconds, `build/gem-cutter-viewers-20260911-120723.log`; all four native suites pass (7/8/7/7). Both Fabric crafting XML cases pass; no ERROR/FATAL/FAILED lines. No project JVMs remain.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, reported duration 0 seconds, `build/gem-cutter-viewers-artifacts.log`; all four production/source pairs pass.

These are two-viewer native server-thread interaction tests, not two connected clients, packet synchronization/rendering, latency stress or general protection-mod compatibility. Those broader acceptance boundaries remain open.

## Native menu synchronization acceptance

`GemCutterMenuSynchronization` attaches a native `ContainerSynchronizer` to the real server menu and forwards its full snapshots, slot deltas and cursor updates into a separately constructed public receiving menu using native `initializeContents`, `setItem` and `setCarried`. The relay copies stacks, as a network boundary would; it does not run a network codec/socket or actual client world.

All slots and the cursor are compared after initial synchronization, paid crafting, a fresh receiving-menu snapshot, partial extraction and final extraction. The fixture verifies the preview is separate from completed output, both table/player payment updates arrive, output insertion remains disabled, receiving menus cannot authorize crafts or extract recipe ghosts, and extraction never replenishes payment. Existing production behavior passed without a fix.

- `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0, 50 seconds, `build/gem-cutter-sync-20260911-121621.log`; all native suites pass (7/8/7/7), both Fabric crafting XML cases pass, no ERROR/FATAL/FAILED lines and no surviving project JVMs.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, reported duration 0 seconds, `build/gem-cutter-sync-artifacts.log`; all four production/source pairs pass.

This closes bounded native menu synchronization coverage only. Connected packet delivery, client prediction, rendering, input geometry and remote multiplayer acceptance remain open.
