# Quartz growth rendering

## Original behavior
Server growth and update packets exist without a growing crystal renderer.

## Approved behavior and reason
Render the raw-quartz block model scaled by server-normalized progress; preserve timing and production.

User requested this change for 0.0.3 and authorized reasonable security/behavior adaptations during implementation. Purpose: usable presentation and consistent release scope without speculative complexity.

## Targets
Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval
Approved by the current 0.0.3 implementation request; no publication authorization.

## Verification
Implemented. Four-target build/native/artifact checks pass; exact evidence and deferred visual acceptance are recorded in docs/migration/0.0.3-IMPLEMENTATION.md.
