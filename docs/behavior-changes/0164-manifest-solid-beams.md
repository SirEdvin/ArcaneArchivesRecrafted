# Manifest solid beams

## Original behavior
All rays originate at the player and use distance-dependent pixel-width DEBUG_LINES (0141).

## Approved behavior and reason
Lectern selections retain lectern origin; handheld selections retain player origin. Use solid square-section world-space beams 1/16 block wide, retaining colors and through-wall visibility. Clear stale connection state; preserve target authorization.

User requested this change for 0.0.3 and authorized reasonable security/behavior adaptations during implementation. Purpose: usable presentation and consistent release scope without speculative complexity.

## Targets
Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval
Approved by the current 0.0.3 implementation request; no publication authorization.

## Verification
Implemented. Native origin transport and deterministic beam geometry tests pass; graphical/remote-multiplayer acceptance remains deferred. See docs/migration/0.0.3-IMPLEMENTATION.md.
