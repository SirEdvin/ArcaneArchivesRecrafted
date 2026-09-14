# 0142 — Original Trove HUD restoration

## Original behavior

Pinned upstream `bb99accf48ed583e29b0efae56e28c963407b8df`, `client/render/RenderHUD.java:34–95`, displays the looked-at Radiant Trove's item, name, compact count and upgrades near the crosshair. Locked empty storage can still display its retained reference and zero count (`RadiantTroveTileEntity.TroveItemHandler.isEmpty`). Filled/reference-bearing storage shows physical storage/optional upgrade quantities; reference-free empty storage shows the weighted storage-upgrade count. Singular/plural translation keys are `arcanearchives.data.gui.radiant_trove.upgrade[s]`.

`util/MathUtils.java:40–49` uses decimal, approximate abbreviations with `k/M/B/T/P/E` suffixes. The port's existing MathUtils formatter instead preserves a separate binary-format contract, so it is not changed.

## Implementation and approval status

Original presentation restoration under the requested 0.0.2 scope, not a proposed gameplay deviation. `TroveHud` registers native HUD callbacks through physical-client initializers on all four targets, independently of Arsenal. It uses the existing synchronized Trove inventory, LOCK reference and upgrade fields; it adds no packet, inventory mutation or permission rule. Minecraft's hidden-GUI setting suppresses the display, as with the existing gem HUD. `TroveHudText` implements the pinned decimal count formatting without changing existing binary-format callers. Original English translations are restored; upstream MIT notices remain packaged.

The inspected upstream RenderHUD is a Trove display, not a standalone Manifest tracking-marker panel. This increment does not invent such a panel or enable the commented-out chest-glow rendering.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Verification

Native synchronization follow-up: `TroveHudSync` installs a real fixture Trove with a storage upgrade and LOCK, then loads the actual `getUpdatePacket()` tags into a detached block entity at counts 32768, zero and seven. It checks packet position/type, exact counts, retained unit diamond reference and both upgrade quantities, while verifying serialization did not mutate the live count. The existing loader/version load adapters supply native deserialization. This is packet-content/deserialization proof, not remote delivery or a ClientLevel fixture. Initial build failed on 1.21 because the detached replica lacked the registry context expected by the existing adapter (exit 1, 60s, `build/trove-hud-sync-20260912-121626.log`); assigning its level without installing it fixed the fixture. No production change. Final `timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 59s (`build/trove-hud-sync-final-20260912-121752.log`), all four native suites pass. Artifact verifier: exit 0, 0s (`build/trove-hud-sync-artifacts.log`), all four pairs pass.

- `timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 92s, `build/trove-hud-20260912-120941.log`; all four native suites pass.
- One decimal-format regression executes on each leaf, including Forge's explicit allowlist: zero, 999/1000 threshold, Trove-sized counts, millions and maximum native integer count. Existing binary-format helpers are unchanged.
- `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/trove-hud-artifacts.log`; all four production/source pairs pass.
- Four `timeout --foreground 11m xvfb-run -a -s '-screen 0 1280x800x24' python3 scripts/smoke_clients.py <leaf>` startup runs pass, aggregate exit 0, 95s, `build/trove-hud-client-smoke-20260912-121125.log`. Native Gradle client runs use `--no-daemon`, all exit 0 and close normally. Per-leaf durations: 23.19s, 24.69s, 23.19s, 23.20s (Fabric 1.20.1, Forge 1.20.1, Fabric 1.21.1, NeoForge 1.21.1). Detailed logs and outside-repository screenshot paths are in the aggregate report.

Actual looked-at HUD rendering, locked/empty/upgrade presentation, GUI scaling, localized layout and connected update delivery remain unverified. Startup and formatter tests do not establish in-world HUD parity or complete 0.0.2 acceptance.
