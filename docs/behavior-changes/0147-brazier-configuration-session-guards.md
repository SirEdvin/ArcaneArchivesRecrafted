# 0147 — Brazier configuration session guards

Approval status: approved by the user ("yes"). Device access predicate implemented; menu/session wiring and full verification remain pending.

## Original behavior

Pinned upstream `bb99accf48ed583e29b0efae56e28c963407b8df`: `items/ScepterManipulationItem.java:43–55` invokes the target's manipulation interface before ordinary block use. `tileentities/BrazierTileEntity.java:341–346` opens the Brazier GUI on the server. `inventory/ContainerBrazier.java:24–27` returns true unconditionally from `canInteractWith`. `network/PacketBrazier.java` mutates the addressed tile and sends updates.

This establishes the opening route and lack of container-lifetime checks; it does not establish that every legacy packet path is remotely exploitable. No exploit is claimed or reproduced.

## Proposed behavior

Restore Scepter of Manipulation configuration opening without replacing ordinary deposit activation. Permit nearby non-spectator players allowed to interact with the block; do not add an owner-only or Hive-only restriction to local configuration.

Bind configuration changes to the player's currently open Brazier menu and original installed server block entity. Recheck same dimension, live identity, loaded availability, interaction permission and distance of at most eight blocks for each mutation and menu validity check. Reject stale sessions, replaced/removed devices and unavailable targets without force-loading. Validate/clamp radius to 0–300 and preserve step 10 and network-mode semantics. Use native server-confirmed menu synchronization rather than trusting optimistic client state.

## Reason

Avoid restoring an unbounded stale configuration session. Match the existing port's local storage interaction model while keeping network ownership separate from local configuration access. The new session/distance checks intentionally tighten the legacy container's unconditional validity.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Verification required

Device predicate checkpoint: `BrazierBlockEntity.canConfigure` checks installed live server identity, same dimension, non-spectator, eight-block distance and native interaction permission without restricting ownership. Native fixtures verify nearby non-owner access, exact eight-block acceptance, farther rejection and detached-replica rejection. `build/brazier-config-access-20260913-144128.log`: four-target build/native suites, exit 0, 139 s. This predicate is not yet wired to a configuration menu; spectator/dimension/permission/session acceptance remains open.

Native manipulation opening without item payment; ordinary deposit remains unchanged; nearby non-owner access; rejection of spectator, distant, wrong-dimension, denied-interaction, closed/replaced-session and removed/replaced-device actions; no forced loading; exact radius bounds/step and synchronized network mode. Connected input, screen rendering and multiplayer delivery require separate acceptance.
