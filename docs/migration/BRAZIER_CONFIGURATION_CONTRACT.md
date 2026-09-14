# Brazier configuration contract

Status: source audit; configuration menu/transport restoration remains incomplete.

Baseline: upstream `bb99accf48ed583e29b0efae56e28c963407b8df`.

## Confirmed controls and persistence

- `tileentities/BrazierTileEntity.java:43,49–52,79–104`: radius defaults to 150, network restriction defaults to false, radius buttons step by 10, and explicit radius setters clamp to 0–300. Range visibility is separate transient state.
- `BrazierTileEntity.java:298–314`: radius and network mode are serialized; absent keys retain constructor defaults. The port already persists and bounds these device settings.
- `network/PacketBrazier.java`: explicit radius, increment, decrement and network-mode messages mutate the server tile, mark it dirty and publish a tile update. Do not copy legacy packet authorization assumptions into the modern port.
- `client/gui/GUIBrazier.java:37–84`: radius field initializes from the tile, permits only up to three decimal digits or an empty editing value, and has separate decrement/increment, network-mode and range-visualization controls. The first three send server messages; visualization toggles the client tile locally.
- `blocks/Brazier.java:73–81`: ordinary main-hand server activation invokes deposit, not this configuration screen. Preserve ordinary deposit rather than replacing it with unconditional menu opening.

## Implementation boundaries

Opening is now traced: upstream `ScepterManipulationItem.onItemUseFirst` calls `BrazierTileEntity.handleManipulationInterface`, which opens the configuration GUI. `ContainerBrazier.canInteractWith` always returns true. The user approved the modern session/distance/permission guards in [0147](../behavior-changes/0147-brazier-configuration-session-guards.md); implementation and verification remain pending. They preserve nearby non-owner local access rather than adding owner-only configuration. Keep visual range display separate from persistent routing radius and network scope.

The inspected sources establish device defaults, not per-player placement preferences. Searches of the pinned Java tree for Brazier default/radius preference names and setter call sites did not identify a per-player default mechanism; do not implement a new preference system based only on historical handoff wording. Complete the inherited placement/opening call-path audit before declaring that broader question settled.

This document records source evidence only. No configuration GUI, input, packet, persistence-restart or rendering acceptance is claimed.
