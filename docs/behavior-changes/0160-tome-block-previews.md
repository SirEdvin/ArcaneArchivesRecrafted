# Tome block previews

## Original behavior
Converted block illustrations use small item icons.

## Approved behavior and reason
Replace standalone block illustrations with native Patchouli block displays without altering recipes or global item transforms.

User requested this change for 0.0.3 and authorized reasonable security/behavior adaptations during implementation. Purpose: usable presentation and consistent release scope without speculative complexity.

## Targets
Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval
Approved by the current 0.0.3 implementation request; no publication authorization.

## Verification
Implemented. Native/resource/artifact tests pass; a real Fabric client rendered the large Radiant Chest preview. Evidence and remaining model/loader acceptance are recorded in docs/migration/0.0.3-IMPLEMENTATION.md.
