# Crafting table rendering

## Original behavior
Nine inputs persist without nearby-client rendering synchronization.

## Approved behavior and reason
Send visual input updates and draw one item per occupied model slot independent of count.

User requested this change for 0.0.3 and authorized reasonable security/behavior adaptations during implementation. Purpose: usable presentation and consistent release scope without speculative complexity.

## Targets
Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval
Approved by the current 0.0.3 implementation request; no publication authorization.

## Verification
Implemented. Four-target build/native/artifact checks pass. Real Fabric client restart verified stored items and corrected model-grid alignment; remaining observer acceptance is recorded in docs/migration/0.0.3-IMPLEMENTATION.md.
