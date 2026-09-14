# Arcane Arsenal default

## Original behavior
Arsenal defaults false in initialization, generated file and missing-key fallback.

## Approved behavior and reason
Default true, preserve explicit false and independent colourblind default.

User requested this change for 0.0.3 and authorized reasonable security/behavior adaptations during implementation. Purpose: usable presentation and consistent release scope without speculative complexity.

## Targets
Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval
Approved by the current 0.0.3 implementation request; no publication authorization.

## Verification
Implemented. Missing-file/key, explicit opt-out and invalid-file-preservation tests pass; see docs/migration/0.0.3-IMPLEMENTATION.md.
