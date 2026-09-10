# 0094 — Debug Orb sided Chest inspection

Status: implemented; four-target assembly/package and focused fixtures pass. Live sided interaction acceptance remains pending.

Original: master 80944ce45c6559243d8928cc4b305bf379388652, items/DebugOrbItem.java:269–300 emits a distinct left-click report on each physical side: side label, Chest name and optional display item/facing. It denies block/item use. This is separate from the detailed right-use storage/network report.

Restoration: retain a dedicated read-only left-click Chest report and display the actual local client snapshot as well as the authoritative server snapshot. Do not transmit client state to the server or upload either report. Normal right-use storage reports remain server-only until their complete client/network data dependencies exist. No mutation, access, permission or protection-dispatch changes.

Approval: source-backed restoration under standing migration scope and the explicitly approved local-only diagnostics policy in 0093. No new gameplay deviation proposed.

Verification: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*DebugOrbTest' --offline --no-daemon`, exit 0, 27s, `build/debug-orb-sided-20260909-205707.log`. Four NeoForge fixtures pass with zero skips/failures/errors, including distinct read-only left/right report content. `timeout --foreground 60s python3 scripts/verify_artifacts.py`, exit 0, 0s, `build/debug-orb-sided-artifacts.log`; all four production/source pairs pass. Connected client/server message delivery and protection-mod behavior require later runtime acceptance; no client/server launched.
