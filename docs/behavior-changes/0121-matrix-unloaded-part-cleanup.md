# 0121 — Matrix cleanup across unloaded chunk boundaries

> Current scope: [0124](0124-matrix-loaded-only-cleanup.md) excludes removal with unloaded footprint chunks. Deferred cleanup and its tests have been removed; 0123 polling was not adopted. Native notifications remain unchanged. The implementation/status/results below are historical, not current obligations or blockers.


## Approval status

Approved by the user's explicit “Yes.” Distillate placement, identity-only part entities, per-dimension saved cleanup data and chunk-load/tick hooks are implemented. The user also approved [0122](0122-matrix-native-neighbor-loads.md): preserve native notifications and allow incidental native chunk loads, while prohibiting explicit Matrix requests/tickets for unloaded chunks. Bounded lifecycle tests pass on every leaf; full acceptance remains open as detailed in 0122. The Matrix repair itself remains approved under 0119.

## Original behavior and current boundary

Pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`, `blocks/MatrixDistillate.java`, declares a directional 3 × 3 × 1 footprint and no block-entity factory. `blocks/templates/BlockTemplate.java:174–212` calculates the footprint from facing and includes all positions between its start/stop bounds, excluding the parent from the accessor list. The invalid accessor-property path documented in 0119 prevents treating this as verified original runtime cleanup behavior.

For example, a parent at (15,64,15) occupies nine positions and two horizontal chunks for every horizontal facing: north/south spans chunks (0,0) and (0,1); east/west spans (0,0) and (1,0). This is a direct coordinate enumeration of the inspected footprint calculation, not a live chunk-unload test.

The port's Reservoir cleanup intentionally skips unloaded positions. Its vertical 1 × 3 × 1 column never crosses a horizontal chunk boundary. Copying that removal loop into Distillate without additional lifecycle handling could leave unloaded pieces behind permanently. Force-loading those pieces would violate the explicit no-forced-load requirement. Refusing boundary placement would narrow the approved footprint-placement behavior.

## Proposed behavior

- Allow valid footprint placement across chunk boundaries only when every required position is already loaded, permitted, replaceable and in bounds, as required by 0119.
- When a placed structure is broken or replaced, clean its matching loaded parts immediately without extra drops.
- Persist narrowly scoped cleanup work for matching parts whose chunks are unloaded; reconcile them when those chunks become available. No explicit Matrix chunk requests/tickets; incidental native loads remain permitted under approved 0122.
- Pending cleanup survives a normal server save/restart. It is internal fresh-world structure bookkeeping, not a legacy-world converter, storage network or new machine function.
- Never delete a foreign replacement or a newly placed complete structure at the old location. Validate structure identity and current state before mutation; coordinates/block type alone are not sufficient authorization for stale queued deletion.
- A cancelled placement/break must not enqueue destructive work. Deferred cleanup must not produce additional item drops or consume items. Repeated reconciliation must be idempotent.
- Process pending work in bounded batches, indexed by relevant chunks rather than scanning every registered device each tick. No general-purpose multiblock framework or external dependency is proposed.

An unloaded orphan may therefore remain in chunk data until that chunk naturally loads. This explicit delay is preferable to forced loading, permanent remnants or silently prohibiting boundary placement.

## Reason

The wider Matrix footprints introduce a lifecycle condition the same-chunk Reservoir implementation cannot encounter. Choose the user-visible cleanup timing and persistence policy before introducing world data or narrowing valid placements.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Initially relevant to Distillate; Crystal Core also has a wider footprint. Reservoir's existing same-chunk behavior is unchanged.

## Verification requirements

- Native placement spanning each horizontal chunk boundary and all horizontal facings.
- Remove parent/child with the other chunk actually unloaded; prove no explicit Matrix requests/tickets and immediate loaded-part cleanup, allowing native notification-triggered loads under 0122.
- Load the other chunk naturally and verify removal without additional drops.
- Save/restart while cleanup is pending, then repeat natural-load reconciliation.
- Foreign block replacement, same-position newly placed structure, repeated cleanup and native event-cancelled placement/removal must remain safe.
- Bounded processing and no full-device per-tick scan; production/source artifacts exclude test fixtures.
- All supported leaves must build and run the relevant native lifecycle fixtures before this is accepted.

The initial production build passed before the lifecycle fixture was added (`build/distillate-initial-20260911-060644.log`, exit 0, 111 seconds). The subsequent native lifecycle run was RED (`build/distillate-lifecycle-fixture-20260911-064603.log`, exit 1, 51 seconds): an adjacent chunk became loaded during native destruction. The user then approved the notification scope in 0122. Its subsequent evidence records green bounded lifecycle, isolated chunk-event wakeup, callback interruption and Forge/NeoForge placement-event cancellation tests. True pending-work process restart and full acceptance remain open; see 0122 for exact commands and verification limits.
