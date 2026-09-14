# Chest count layering

## Original behavior
Custom extended counts are drawn in renderLabels without the native slot depth.

## Approved behavior and reason
Draw full counts at native slot-decoration depth; no count/capacity changes.

User requested this change for 0.0.3 and authorized reasonable security/behavior adaptations during implementation. Purpose: usable presentation and consistent release scope without speculative complexity.

## Targets
Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval
Approved by the current 0.0.3 implementation request; no publication authorization.

## Verification
Implemented. Four-target build/native/artifact checks pass; exact evidence and deferred visual acceptance are recorded in docs/migration/0.0.3-IMPLEMENTATION.md.
