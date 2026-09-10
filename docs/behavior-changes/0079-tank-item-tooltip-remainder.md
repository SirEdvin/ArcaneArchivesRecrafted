# 0079 — Tank item tooltip and crafting remainder

Status: implemented; four-target assembly/package and three focused NeoForge item fixtures passed. Gameplay acceptance remains open.

## Original behavior

Release bb99accf48ed583e29b0efae56e28c963407b8df, items/itemblocks/RadiantTankItem.java:24–65 sets itself as its container item and adds stored fluid name, amount/maximum in millibuckets and a literal `Capacity is: ` line before the inherited tooltip. Empty tagged Tanks report literal `None`. Both original language files use `Fluid: %s` and `Amount: %s/%smb` for these lines. The default Item container-item behavior returns a fresh empty item, not a copy of stored contents/upgrades.

## Port and approval

Replace the registered plain BlockItem with a Tank-specific BlockItem. Decode the existing packed block-entity save through the Tank's validated load path for tooltips; do not create a second save format or assume that item fluid capabilities already exist. Use native loader fluid names, preserve original literal text and unit presentation, and retain the existing device/upgrade tooltip. Invalid packed data is reported explicitly rather than silently described as empty or rewritten. Registry-less modern tooltip contexts omit detailed decoding safely.

Restore one fresh empty Tank remainder via Fabric's stack-aware recipe-remainder hook and Forge/NeoForge's stack-aware crafting hooks. Do not duplicate packed fluid, upgrades, owner or custom data into a crafting remainder. Update the Gem Cutter's Fabric remainder detection to consult the native stack-aware hook instead of the item-only static property, so the newly restored behavior is not skipped there.

Approval: reachable source behavior restoration within standing scope; malformed-data presentation and nonmutating decoding preserve existing safety requirements. All four supported version/loader targets are affected. Native item fluid capability/Transfer API support, dynamic item-fluid rendering and broader modded-fluid/container acceptance remain unfinished; this slice does not claim those are implemented.

## Verification

Command: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*RadiantTankItemTest' --offline --no-daemon`.

Exit 0, 15 seconds; `build/tank-item-offline-20260909-174027.log`. All four supported targets assembled. NeoForge XML reports 3 tests with zero failures/errors/skips: packed fluid/upgraded-capacity tooltip without input mutation, a fresh empty crafting remainder without packed contents, and distinct empty/invalid tooltip handling without rewriting data. The fixture uses the source-backed Matrix Brace weight of two capacity units.

Prior runs are retained: `build/tank-item-20260909-173545.log` (exit 1, 29s) compiled all targets but caught a fixture incorrectly treating Matrix Brace as one capacity unit; corrected the test rather than changing production capacity. `build/tank-item-final-20260909-173653.log` (exit 1, 167s) failed resolving Forge userdev metadata because the network was unreachable. The successful offline retry used existing pinned cached dependencies without changing build metadata or versions.

`timeout --foreground 60s python3 scripts/verify_artifacts.py` returned 0, log `build/tank-item-artifacts.log`; all four production/source artifact pairs pass. `git diff --check` passed. Cached Fabric item API sources confirmed stack-aware `getRecipeRemainder` on both target generations; mapped native API inspection confirmed modern TooltipContext registry access and the old BlockItem NBT accessor.

No connected-client tooltip inspection or real crafting-grid/Gem Cutter interaction was run. Native item fluid capability/Transfer API, item fluid rendering, runtime recipe/container combinations and save/restart acceptance remain open. This does not complete Tank or storage migration.
