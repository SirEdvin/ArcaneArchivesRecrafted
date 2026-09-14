# Manifest XMI integration

## Original behavior
Manifest search binds only JEI.

## Approved behavior and reason
Prefer EMI with JEI fallback, retaining query-session semantics and existing configuration keys; label UI XMI integration.

User requested this change for 0.0.3 and authorized reasonable security/behavior adaptations during implementation. Purpose: usable presentation and consistent release scope without speculative complexity.

## Targets
Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval
Approved by the current 0.0.3 implementation request; no publication authorization.

## Verification
Implemented. Four-target build/native/artifact checks pass; callback regressions and deferred installed-viewer acceptance are recorded in docs/migration/0.0.3-IMPLEMENTATION.md.
