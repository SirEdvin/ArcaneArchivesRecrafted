# 0141 — Positive widths for Manifest tracking rays

## Original behavior

Pinned upstream `bb99accf48ed583e29b0efae56e28c963407b8df`, `client/render/RenderUtils.java:29–64,99–103`, draws through-wall lines from interpolated player position plus one Y block to selected block centers. The color cycles with total world time. Line width is `(1 - (clamp((distance - 10) / 60, 0, 1) * 0.7 + 0.3)) * 10`, measured to the block corner before adding the center offset.

At distances of 70 blocks or more the requested width is zero. OpenGL rejects widths less than or equal to zero with `GL_INVALID_VALUE`; this is not a supported way to hide a line and cannot be assumed to produce an invisible far-away marker. Source: https://registry.khronos.org/OpenGL-Refpages/gl4/html/glLineWidth.xhtml (Errors section, fetched and inspected).

## Proposed behavior

Preserve upstream ray endpoints, current-dimension filtering, location deduplication, through-wall visibility and cycling color. Clamp the original requested width to a minimum of one pixel before drawing. Restore modified render state afterward.

## Reason

Avoid invalid OpenGL calls and give distant selected locations a defined, visible locator instead of relying on an invalid width request. This intentionally changes the original width calculation's result near and beyond its far-distance limit; it is not compilation-only migration work.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge, client tracking-line renderer only. No storage permission, inventory, selection or networking changes.

## Approval status

Approved by the user's explicit “yes” to the one-pixel minimum. Implemented: preserve distant rays by clamping the original requested width to at least one pixel; do not omit them at the far-distance limit. Rendered-world acceptance remains pending.

## Verification

Ray-state follow-up: native transport fixtures now assert the exact retained coordinate set after revocation and old-snapshot replay, not merely its size. They restore nonempty references, apply the explicit failure snapshot, replay the older success and verify both ray positions and highlight matching remain empty; a newer valid authorized snapshot then recovers normally. This tests the production receiver/projection/matching path, not OpenGL output or connected delivery. No production change. Four-leaf `timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 60s (`build/manifest-ray-revocation-20260912-120432.log`), all native suites pass. Artifact verifier: exit 0, 0s (`build/manifest-ray-revocation-artifacts.log`), all four pairs pass.

`ManifestRayRenderer` registers at Fabric LAST and Forge/NeoForge AFTER_LEVEL through the existing client-only initialization route. It draws confirmed current-dimension locations, deduplicated by `ManifestRays`, from interpolated player position plus one Y block to block centers. Width measures distance to the block corner as upstream; color uses total game time, unlike the Manifest slot's day-time phase. Coordinates are camera-relative, with the loader's render matrix. Depth testing/writes are disabled for the draw; prior depth enable/mask, width, shader and shader color are restored in `finally`. No client inventory/chunk discovery or new server authorization is introduced.

The width regression executed on all four leaves (including the Forge explicit allowlist): near/mid/far values, monotonically nonincreasing widths and one-pixel minimum. Native tracking fixtures exercise the exact projection used by rendering: duplicate references collapse, other dimensions are excluded and cleared snapshots yield no rays.

`timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 93s, `build/manifest-rays-20260912-115828.log`; all four native suites pass. `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, 1s, `build/manifest-rays-artifacts.log`; all four production/source pairs pass.

Four graphical startup smoke runs used `timeout --foreground 11m xvfb-run -a -s '-screen 0 1280x800x24' python3 scripts/smoke_clients.py <leaf>`. Every native `:<leaf>:runClient --no-daemon` exited 0, initialized the mod and atlases, loaded Patchouli resources and closed normally. Aggregate exit 0, 99s, `build/manifest-rays-client-smoke-20260912-120031.log`; per-leaf durations 26.21s/24.71s/24.70s/23.20s in Fabric 1.20.1, Forge 1.20.1, Fabric 1.21.1, NeoForge 1.21.1 order. Detailed paths and screenshots outside the repository are recorded there.

These are startup tests, not an in-world ray render test. Actual ray placement, occlusion, line widths across graphics drivers, camera modes, render-state restoration during a world frame, connected revocation/clear presentation and graphics-mode compatibility remain unverified. The implementation does not establish complete Manifest or release acceptance.
