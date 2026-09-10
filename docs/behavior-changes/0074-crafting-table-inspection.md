# 0074 — Radiant Crafting Table revelation inspection

Status: implemented; assembly/package verification passed; connected-player acceptance pending.

Original: release bb99accf48ed583e29b0efae56e28c963407b8df, ScepterRevelationItem.java:87–116 reports an empty table or a comma-separated list of item names from occupied ingredient slots. Order and repeated names are retained; counts and saved recipes are not listed. Original English and Portuguese messages are copied from the release language resources.

Port: StorageScepterItem now recognizes the crafting table for revelation only. The table's version-specific block interaction handlers route recognized scepter actions before opening its menu. Server-only reporting uses table.canUse, retaining live-block, permission, distance, player and thread checks. Offhand revelation remains consumed without a duplicate report. Manipulation and ordinary crafting interaction are unchanged. Inspection does not mutate ingredients or stored recipes. Item.getName(stack) supplies the item's localized name without the stack's anvil-name override, matching the original item-name lookup.

Targets: 1.20.1 Fabric/Forge; 1.21.1 Fabric/NeoForge.
Approval: restoration of reachable original behavior under the standing migration scope; no gameplay deviation proposed.

Verification: timeout --foreground 10m ./gradlew assemble --no-daemon returned exit 0 in 26s, build/crafting-inspection-20260909-165653.log. timeout --foreground 60s python3 scripts/verify_artifacts.py returned exit 0, build/crafting-inspection-artifacts.log. git diff --check passed. These verify all four artifact targets, not connected-player interaction or persistence acceptance. Full storage/tool migration remains open.
