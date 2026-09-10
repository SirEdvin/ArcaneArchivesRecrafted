# 0087 — Amphora dispenser behavior

Status: registered dispatch implementation and four-target assembly/package verified; world-transfer runtime acceptance remains open.

Original release `bb99accf48ed583e29b0efae56e28c963407b8df`: RadiantAmphoraItem constructor registers DispenseAmphora. That behavior chooses placement for replaceable non-fluid blocks, pickup otherwise, independent of saved mode. It restores saved mode, retains the Amphora on failure rather than ejecting it, emits dispenser click/particles on successful placement, and emits them for pickup attempts even when pickup fails.

Restore registered dispenser dispatch using existing explicit live AmphoraFluidStorage.worldTransfer operations. No temporary item-mode mutation is needed. Native fluid-state detection covers modern waterlogged blocks; fluid-bearing blocks select pickup, not replacement. Use the face opposite dispenser facing for target access, no player, and MAIN_HAND for the native placement API context. Preserve existing loaded-chunk, world-border, build-height and live-storage conservation guards. This does not expose a copied-item remote fluid capability or add chunk loading. Existing native transfer correction boundaries remain in force.

Affected targets: all four leaves. Approval: faithful registered behavior restoration under standing approval and existing safety/conservation corrections. No new gameplay powers or recipe changes. Original notices retained.

Verification:
- Follow-up lifecycle correction: the existing `AmphoraEvents.started` callback overwrote the constructor registration with an older lambda. Removed that duplicate implementation; startup now only sets the live server reference. The original registration-only test did not catch this runtime overwrite, so the earlier completion claim was incomplete.
- `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*AmphoraLifecycleTest' --tests '*DispenseAmphoraTest' --offline --no-daemon`: exit 0, 25s, `build/amphora-lifecycle-20260909-184921.log`. All four leaves assemble; NeoForge has 1 lifecycle callback test and 2 existing dispenser tests, no skips/failures/errors. The lifecycle fixture directly calls the production startup callback with a null server and verifies registration identity through repeated callbacks; it is not an actual server restart test. Artifact verification passes in `build/amphora-lifecycle-artifacts.log`; whitespace check passes.
- `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*DispenseAmphoraTest' --offline --no-daemon`: exit 0, 28s, `build/amphora-dispenser-20260909-183943.log`; all four targets assembled.
- NeoForge XML: 2 tests, 0 skips/failures/errors. Tests verify actual registered behavior replaces default ejection and pickup/placement selection for air, replaceable vegetation, water, lava, solid blocks, containers and waterlogged slabs. They do not run the world-transfer path or establish item retention under actual pulses.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/amphora-dispenser-artifacts.log`; all four production/source pairs pass with explicit DispenseAmphora class/source checks. `git diff --check` passes.
- Cached NeoForge 21.1.234 FluidUtil source confirms nullable-player placement builds an empty-item context rather than dereferencing the absent player. Both target Minecraft BlockSource APIs were inspected before adapting accessors. Modern dispenser particle event uses facing's 3D data value, rather than the old 1.12 horizontal event encoding.

Real redstone pulses, world placement/pickup, cross-dimension loaded/unloaded Tanks, protection integrations and persistence remain runtime acceptance tasks. No client/server was launched for this increment.
