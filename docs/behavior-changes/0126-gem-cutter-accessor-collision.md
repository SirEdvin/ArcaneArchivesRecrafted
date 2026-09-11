# Gem Cutter accessor entity collision

## Original and current behavior

Pinned upstream revision: `80944ce45c6559243d8928cc4b305bf379388652`.
`items/templates/ItemBlockTemplate.java:76–95` checks accessor air/replaceability, not entity collision. This is static source evidence, not a legacy runtime reproduction.

The migrated `blocks/GemCuttersTable.java:74–87` checks the second position's world border and replaceability, then writes the accessor in `setPlacedBy`. Native BlockItem placement checks collision at the parent, but that later accessor write bypasses entity collision validation.

A native pig collision fixture confirms the distinction on all four targets: all four parent-position controls reject placement; all four accessor-position cases return CONSUME, consume one of two items, and install the table into the occupied position.

## Proposed behavior

Before any placement write or item consumption, also require the accessor's actual collision shape to be unobstructed using the native collision API and placement context. Refuse the entire placement if an entity obstructs either part, without consuming/changing the offered stack or leaving blocks, block entities or drops.

Preserve the original two-part geometry, yaw convention, valid placement, item-data import, ownership assignment, inventory handling and native cancellation hooks. This proposal does not authorize a general structure framework or additional unreviewed placement restrictions.

## Reason

Avoid placing the second solid footprint block into an entity while the same obstruction correctly prevents placement at the parent. This deliberately tightens the source's accessor checks, so approval is required rather than silently calling it exact upstream parity.

## Affected targets

- Minecraft 1.20.1 Fabric
- Minecraft 1.20.1 Forge
- Minecraft 1.21.1 Fabric
- Minecraft 1.21.1 NeoForge

## Approval status

Approved by the user ("yes") and implemented. `GemCuttersTable.getStateForPlacement` now checks the accessor state with native `isUnobstructed` and the player's collision context (empty context for no-player placement), before any placement write. Existing parent validation and placement/ownership/cancellation paths are unchanged.

## Verification

Shared fixture: `src/sharedGameTest/java/com/aranaira/arcanearchives/gametest/GemCutterPlacementCollision.java`, invoked from `DeviceOwnershipLifecycle`. Runs four horizontal orientations with parent/accessor collision cases, checks item conservation and absence of residual state/drops, and cleans owned blocks/entities in `finally`.

Command: `timeout --foreground 10m ./gradlew :1.20.1-fabric:runGameTest :1.20.1-forge:runGameTestServer :1.21.1-fabric:runGameTest :1.21.1-neoforge:runGameTestServer --continue --no-daemon`.

Result: exit 1, 54 seconds, `build/gem-cutter-collision-red-20260911-104544.log`. Every leaf reports exactly the four accessor failures, each `result=CONSUME remaining=1`. Both Fabric XML reports contain the corresponding failing `fabricruntimetests.deviceownership` case. No project JVMs remained; whitespace check passed. No green build or post-change artifact acceptance is claimed.

GREEN verification: `timeout --foreground 10m ./gradlew build --continue --no-daemon`, exit 0 in 83 seconds, `build/gem-cutter-collision-green-20260911-110033.log`. All four native suites pass (7/8/7/7 required tests), including existing populated-device conservation and Forge-family cancellation. The shared collision fixture now also checks successful unobstructed placement, exact parent/accessor states and single-item consumption in every horizontal orientation. Both Fabric ownership XML cases pass; no ERROR/FATAL lines or surviving project JVMs were found.

`timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, reported duration 0 seconds, `build/gem-cutter-collision-artifacts.log`; all four production/source pairs pass. This is bounded native placement verification, not connected multiplayer or arbitrary protection-mod acceptance.
