# 0096 — Packed Radiant Tank fluid rendering

Status: implemented and assembly/package-verified on all four targets; live visual acceptance remains open.

Original: release bb99accf48ed583e29b0efae56e28c963407b8df, client/render/RadiantTankTEISR.java:30–54 draws the original shell then delegates item fluid rendering to RadiantTankTESR.java:217–240. The item path uses stored fluid and saved capacity, the same six-face fluid volume as the placed Tank, at the item origin.

Restore fluid drawing after native shell rendering with its already-applied item perspective transform. Reuse RadiantTankRenderer; decode only copied packed state with client registry access. Empty/malformed state draws no fluid and is not rewritten. Never query a remote Tank or mutate item contents. Keep original model/artwork and existing block rendering unchanged.

A client-only ItemRenderer injection immediately before the final pose pop avoids replacing the native OBJ model, shell render passes, perspective handling, or glint. Both Minecraft versions and pinned Forge-family patches were inspected: 1.20.1 has an earlier compass-only pose pop; select ordinal 1 there and ordinal 0 on 1.21.1. This is an additive visual hook, not a new item capability or gameplay change.

Approval: source-backed restoration under the standing migration scope. No proposed gameplay deviation.

Verification: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*RadiantTankItemTest' --tests '*TankItemFluidStorageTest' --offline --no-daemon`, exit 0, 27s, `build/tank-item-render-20260909-212134.log`. All four leaves assembled. NeoForge XML: 7 existing packed-state/remainder/fluid-conservation fixtures, no skips/failures/errors; these do not execute rendering.

`timeout --foreground 60s python3 scripts/verify_artifacts.py`, exit 0, 0s, `build/tank-item-render-artifacts.log`: all four production/source pairs and client-only mixin registration pass. `javap -v -p` confirms both Fabric item-render/pop targets remap to intermediary names, ordinal 1 on 1.20.1 and 0 on 1.21.1 (`build/tank-item-render-bytecode.log`). Forge refmap contains `m_115143_` for render and `m_85849_` for popPose; NeoForge retains named targets. `git diff --check` passes.

No client/server launched. Live GUI/ground/frame/held rendering, modded fluids, resource reload, shader compatibility and dedicated-server startup remain acceptance work. Rendering reuses the existing placed-Tank geometry/tint behavior; complete visual parity is not claimed from bytecode checks. The 1.21 item pass requires client-world registry access and safely omits fluid when no client world is available.
