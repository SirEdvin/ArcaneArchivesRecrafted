# 0144 — Preserve Brazier remainders when ejection spawning fails

Approval status: approved by the user ("yes"). Implemented in the dropped-item collision path.

## Original behavior

Pinned upstream `bb99accf48ed583e29b0efae56e28c963407b8df`, `tileentities/BrazierTileEntity.java:140–176`: item collision routes the offered stack, calls `rejectItemStacks` for remainders, then marks the original entity dead. `rejectItemStack` calls `world.spawnEntity(item)` without checking its boolean result. Thus a failed rejected-entity spawn has no source-side recovery in this path. This is a sequential failure-path concern, not concurrency; no live loss has been reproduced here.

## Proposed behavior

Preserve the ordinary route/eject/discard behavior. If spawning a rejected remainder fails, keep the original source entity alive with exactly the unpaid remainder, mark it rejected, and apply the original 20-tick pickup delay. Never restore the portion already accepted by storage and never automatically route the rejected remainder again.

## Reason

Prevent a failed/cancelled ejection from deleting unpaid items, without duplicating accepted items or introducing a persistent input buffer. Existing 0103 covers destination eligibility and remainder preservation; this record explicitly proposes the source-entity recovery policy for spawn failure.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Verification

`BrazierBlockEntity.absorb` uses the exact routing remainder, spawns rejected items with a 20-tick pickup delay, and keeps only the unpaid remainder in the original source when the spawn boundary returns false. A native persisted entity tag prevents immediate repeat routing. `Brazier.entityInside` dispatches server-side item collisions.

Native `BrazierCollisionLifecycle` coverage passes on all four targets: full/partial/zero acceptance, successful native ejection, injected spawn failure, dead-source rejection, rejected-source repeat collision, and native serialization of the rejection marker. The package-private spawn predicate injects only the spawn result; this is not a reproduced loader event cancellation. Full acceptance and repeat rejection exercise the registered block callback directly, not physical item travel through the world's tick loop.

`timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 94s, `build/brazier-collision-20260912-171139.log`. All four native suites pass. Artifact verification passes in `build/brazier-collision-artifacts.log`. Subsequent collision sound wiring and controlled-clock/mute-gate tests pass in `build/brazier-collision-sound-20260912-171516.log` (exit 0, 88s; all four native suites), with artifact check `build/brazier-collision-sound-artifacts.log`. Audible playback, rendered ejection, connected item travel and process restart remain unverified.
