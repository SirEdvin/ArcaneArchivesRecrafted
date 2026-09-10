# 0075 — Trove withdrawal parity

Status: implemented and focused-fixture verified; connected-player acceptance remains open.

## Original

Release bb99accf48ed583e29b0efae56e28c963407b8df, RadiantTroveTileEntity.java:176–209: sneak-scepter attacks return before withdrawal/cooldown; withdrawal inserts into the player's main-inventory item handler, stacking into occupied compatible slots before using empty slots and leaving overflow for a world drop. It does not use vanilla creative-mode overflow deletion.

## Port

The port incorrectly called Inventory.add, whose creative instabuild branch clears uninserted items (confirmed in mapped Forge 47.3.39 Inventory.java). Replace that call with shared main-inventory insertion retaining component identity, item stack limits, the native 64-slot limit, slot order and remainder. Main inventory only: no offhand/armor fill. Keep the existing overflow-drop and inventory synchronization path. Sneak-scepter attacks now return before changing the withdrawal cooldown, as upstream does.

Approval: restore source behavior under the standing migration scope; no new gameplay deviation. All four targets affected. The existing player-drop implementation remains unchanged; rejected-drop behavior and multiplayer compatibility are not certified here. The source's network-controlled TrovesDispense reversal is still pending network migration. Sneak-scepter break/spill remains separate unfinished work.

## Verification

Command: timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*TroveWithdrawalTest' --no-daemon

Exit 0, 27 seconds, build/trove-withdrawal-20260909-170113.log. All four targets assembled. NeoForge XML confirms three fixtures, no failures/errors/skips: existing-stack priority, full/partly-full overflow conservation, and component identity/native stack limits. These exercise the insertion routine, not a connected creative player or world-drop hooks.

Artifact checker: timeout --foreground 60s python3 scripts/verify_artifacts.py; exit 0; build/trove-withdrawal-artifacts.log. git diff --check passed. No claim that Fabric/Forge ran the NeoForge-only fixture, or that storage/network migration is complete.
