# Storage placement limits

## Original behavior
Storage has ownership but no placed-device quota.

## Approved behavior and reason
Independent server-wide per-player chest/trove/tank limits default 64; 0 unlimited; creative bypass still counts; survival denies new placements at/above limit. No retroactive removal, no legacy migration. These storage blocks already assign any `Player` (including loader fake players) as owner in `setPlacedBy`, unlike the separate network-device ownership exclusions. Preserve that existing behavior: fake players receive their own UUID quota; no-player callers bypass player quotas and receive no newly invented identity. Existing item-data import/placement ownership ordering is retained.

Account by persisted dimension/position/type/UUID in overworld SavedData. Native chunk install/load/removal and live ownership/NBT changes update the ledger. Chunk unload retains entries. Reconcile available known positions on a quota query without requesting missing chunks. Hive membership does not combine quotas. Negative/malformed settings fail without rewriting files. Apply config changes on restart. Deny conversions before any source escrow/payment; double-chest conversion creates one new Radiant Chest and consumes one quota slot, preserving the existing conversion behavior.

User requested this change for 0.0.3 and authorized reasonable security/behavior adaptations during implementation. Purpose: usable presentation and consistent release scope without speculative complexity.

## Targets
Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval
Approved by the current 0.0.3 implementation request; no publication authorization.

## Verification
Implemented. Four-target native placement, conversion-denial, creative accounting, unloaded-coordinate and NBT tests pass. The complete process-restart/cancellation campaign remains deferred; see docs/migration/0.0.3-IMPLEMENTATION.md.
