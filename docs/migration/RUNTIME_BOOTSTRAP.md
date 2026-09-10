# Runtime bootstrap verification

## Scope

All four real loader clients and dedicated servers have been exercised. The run table below preserves the original pre-content bootstrap evidence, not the latest whole-project state. Current source includes quartz and required external Patchouli. See [HANDOFF.md](../HANDOFF.md), [QUARTZ_CONTENT.md](QUARTZ_CONTENT.md) and the latest matrix in [GUIDEBOOK_BACKEND.md](GUIDEBOOK_BACKEND.md). Startup is not GameTests, multiplayer, interactive book rendering or storage parity.

- Loader scripts expose `:<leaf>:runClient` and `:<leaf>:runServer` with separate ignored `versions/<leaf>/runs/client` and `runs/server` directories. No test world is shared between versions or loaders.
- Servers require explicit EULA acceptance. The user explicitly accepted Minecraft's EULA and authorized development-server testing; only ignored local EULA files were updated. Scripts never accept terms automatically.
- Server smoke tests require `server-ip=127.0.0.1`. Local test settings also use `server-port=0`, `view-distance=2` and `simulation-distance=2`; online authentication stays enabled.
- `python3 scripts/smoke_servers.py` runs the matrix sequentially, waits for the real `Done (...)!` message, sends `stop`, and requires mod initialization, saved dimensions, orderly shutdown, no timeout and no missing mod-pack warning. JSON result sidecars preserve exact commands and outcomes alongside complete logs.
- Linux client checks use `xvfb-run -a --server-args='-screen 0 1280x800x24 -nolisten tcp' python3 scripts/smoke_clients.py <leaf>`. Installed native X11/ffmpeg tools capture the isolated display; WM_DELETE_WINDOW requests an orderly game exit. Screenshots live outside the repository in `/tmp/arcane-client-*`.
- Each Java run is wrapped in `timeout --foreground 10m ./gradlew :<leaf>:runClient|runServer --no-daemon --console=plain`; full output is saved silently. The harness reports exit code, duration and log path and cleans up its process group.
- Current `--quartz` server coverage additionally reloads data, tests quartz placement/tag/loot/item results and verifies the external `patchouli:guide_book` item in an isolated chest fixture. Current client coverage rejects Arcane Archives model/texture errors and requires Patchouli's resource-loader message. The original run table predates those assertions; use the latest guidebook report for their results.
- Headless client prerequisites: Linux/X11, Python 3.11+, Xvfb, xauth, xwininfo, libX11 and ffmpeg. The Python harness uses the standard library, not PIL, python-xlib or mss. Logs and JSON sidecars are ignored under root `build/`; existing accepted EULAs and worlds are local state, not portable repository contents.
- Screenshots from every target were inspected: the Minecraft panorama, text and first-run accessibility screen rendered. No crash UI or missing-texture pattern appeared. No custom content was rendered and no client world was entered.

## Regression found and repaired

The initial 1.21.1 Fabric server run exited 0 at the EULA gate, without starting a server. Exit code alone must not count as readiness.

A real Forge launch then warned `Missing metadata in pack mod:arcanearchives` and skipped the data pack. Added canonical `src/main/resources/pack.mcmeta`, with expanded, tracked Gradle inputs on every loader. Exact formats were checked against the cached Minecraft `version.json` files: 1.20.1 resource/data 15/15; 1.21.1 resource/data 34/48. The 1.21.1 metadata declares supported formats spanning both values.

Artifact regression RED: `python3 scripts/verify_artifacts.py` exited 1 with `1.20.1-fabric: missing pack metadata`. GREEN: `timeout --foreground 10m ./gradlew build --no-daemon` exited 0 in 21s, `build/pack-metadata-green-20260907-165405.log`; all four artifact checks passed. A repeated server matrix passed with no missing mod-pack warnings.

## Historical bootstrap runs

| Leaf | Run | Exit | Duration | Complete log |
|---|---|---|---|---|
| 1.20.1-fabric | server | 0 | 24.8s | `build/smoke-server-1.20.1-fabric-20260907-165426-093732.log` |
| 1.20.1-forge | server | 0 | 46.58s | `build/smoke-server-1.20.1-forge-20260907-165450-898593.log` |
| 1.21.1-fabric | server | 0 | 17.29s | `build/smoke-server-1.21.1-fabric-20260907-165537-480448.log` |
| 1.21.1-neoforge | server | 0 | 17.78s | `build/smoke-server-1.21.1-neoforge-20260907-165554-772163.log` |
| 1.20.1-fabric | client | 0 | 22.69s | `build/smoke-client-1.20.1-fabric-1788800310777464336.log` |
| 1.20.1-forge | client | 0 | 23.71s | `build/smoke-client-1.20.1-forge-1788800333554915307.log` |
| 1.21.1-fabric | client | 0 | 36.24s | `build/smoke-client-1.21.1-fabric-1788800263888626999.log` |
| 1.21.1-neoforge | client | 0 | 22.7s | `build/smoke-client-1.21.1-neoforge-1788800357346487670.log` |

## Remaining coverage and diagnostics

- No claim of warning-free platform logs: first-run Forge configuration corrections, loader-library metadata warnings, headless audio/narrator limitations and unauthenticated development-client service requests require classification separately from mod failures. Initial 1.20.1 terrain generation also logged vanilla `large_dripstone` far-chunk placement diagnostics; no mod world generation exists yet.
- Quartz data reload and basic content checks now exist; GameTests, interactive resource reload, multiplayer and save/restart conservation still need implementation. Successful startup is not completion of any gameplay phase.
- EULA files, logs, configurations, worlds and screenshots must never be committed. Existing unrelated system displays/processes were not stopped; only test-owned process groups are cleaned up.
