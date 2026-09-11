# Optional integration startup acceptance

## KubeJS combined development profile

The opt-in `optionalIntegrationMods` property now accepts `kubejs`. Selecting it adds pinned Rhino and, on 1.20.1, Architectury companions. Gradle configures all leaves, so the approved 1.21.1 Fabric exception filters KubeJS out on that leaf. Default runtime remains unchanged; none of these dependencies are bundled or mandatory player dependencies. Exact companion metadata and verified downloaded/Gradle-resolved hashes are in `kubejs-companion-artifacts.json`.

Executed with `ORG_GRADLE_PROJECT_optionalIntegrationMods=jade,jei,emi,kubejs`: `timeout --foreground 32m python3 scripts/smoke_servers.py 1.20.1-fabric 1.20.1-forge 1.21.1-neoforge`. Initial aggregate exited 1 after 76s: Fabric compilation lacked Rhino interfaces injected into vanilla classes by KubeJS. Fixed the opt-in Fabric Rhino scope to `modImplementation` (not bundling); its isolated server rerun exited 0 after 36s. Forge/NeoForge passed the initial run. Clients ran serially using `timeout --foreground 11m xvfb-run -a -s '-screen 0 1280x800x24' python3 scripts/smoke_clients.py <leaf>`, aggregate exit 0 after 77s.

| Leaf/side | Duration | Passing runtime log |
| --- | ---: | --- |
| 1.20.1 Fabric server | 36.06s | `build/smoke-server-1.20.1-fabric-20260910-191451-101723.log` |
| 1.20.1 Forge server | 30.81s | `build/smoke-server-1.20.1-forge-20260910-191328-458052.log` |
| 1.21.1 NeoForge server | 19.78s | `build/smoke-server-1.21.1-neoforge-20260910-191359-268724.log` |
| 1.20.1 Fabric client | 23.70s | `build/smoke-client-1.20.1-fabric-1789067735897738186.log` |
| 1.20.1 Forge client | 28.20s | `build/smoke-client-1.20.1-forge-1789067759678297793.log` |
| 1.21.1 NeoForge client | 24.20s | `build/smoke-client-1.21.1-neoforge-1789067787961823703.log` |

All six final records have exit 0 and all smoke assertions true, including shutdown. KubeJS's generated example scripts loaded with zero script errors/warnings. Server error/exception scans were empty; clients retained headless OpenAL failures, and 1.20.1 clients warned about absent optional ModNameTooltip/REI mixin targets. This is not clean-log acceptance. No custom recipe scripting, add/remove/reload or connected-player behavior was tested here.

The default four-target `timeout --foreground 10m ./gradlew build --no-daemon` passed, exit 0 after 28s (`build/kubejs-profile-build.log`); `timeout --foreground 60s python3 scripts/verify_artifacts.py` passed (`build/kubejs-profile-artifacts.log`).

## Installed dedicated-server profile

The NeoForge row below is historical: JEI subsequently changed to `UJRXzDfp` / `19.25.1.334` to satisfy KubeJS's declared optional-JEI minimum. A replacement Jade+JEI+EMI server smoke passed in 17.54s, exit 0, log `build/smoke-server-1.21.1-neoforge-20260910-190728-795249.log`; its client counterpart passed in 26.20s, log `build/smoke-client-1.21.1-neoforge-1789067222558912178.log`. KubeJS was not installed; see [0113](../behavior-changes/0113-integration-scope.md).

Executed `ORG_GRADLE_PROJECT_optionalIntegrationMods=jade,jei,emi timeout --foreground 42m python3 scripts/smoke_servers.py`: exit 0, 81 seconds, aggregate log `build/optional-installed-servers.log`. The existing harness invokes each leaf's `runServer` with a ten-minute timeout and `--no-daemon --console=plain`. Existing EULA/loopback preflights passed without modifying legal acceptance or network binding.

| Leaf | Exit | Duration | Full log |
| --- | ---: | ---: | --- |
| 1.20.1 Fabric | 0 | 21.80s | `build/smoke-server-1.20.1-fabric-20260910-190304-105990.log` |
| 1.20.1 Forge | 0 | 24.54s | `build/smoke-server-1.20.1-forge-20260910-190325-905980.log` |
| 1.21.1 Fabric | 0 | 17.28s | `build/smoke-server-1.21.1-fabric-20260910-190350-448984.log` |
| 1.21.1 NeoForge | 0 | 17.54s | `build/smoke-server-1.21.1-neoforge-20260910-190407-728071.log` |

Programmatic aggregation required four distinct passing leaf results. All servers initialized the mod, reached readiness, saved all dimensions and stopped. Jade logs explicitly identify this mod's server plugin on every leaf. Full-log scanning found no `ERROR`, `FATAL` or `Exception` lines. Warnings remain, including Forge initializing/correcting `jei-server.toml`; this is not a warning-free log claim. A subsequent `/proc` check found no remaining project Java processes.

This closes only dedicated-server bootstrap with the combined Jade+JEI+EMI development profile. It does not verify client/server connections, overlay payloads, viewer workflows, every optional-mod subset, KubeJS, or packaged-distribution startup. Installed client startup evidence and the older-JEI compatibility decisions are in [0113](../behavior-changes/0113-integration-scope.md).

## Historical optional-absence matrix

Verified after adding the Jade, EMI and JEI providers, with no mod JARs in any `versions/*/runs/*/mods` directory. The optional integration dependencies remain compile-only; Patchouli and the normal loader/API dependencies remain present. This is development-runtime startup coverage, not installed-integration or connected gameplay acceptance.

## Execution

Servers: `timeout --foreground 42m python3 scripts/smoke_servers.py`, aggregate exit 0, full summary `build/optional-absence-servers.log`.

Clients: for each leaf below, `timeout --foreground 11m xvfb-run -a -s '-screen 0 1280x800x24' python3 scripts/smoke_clients.py <leaf>`, each exit 0, summaries `build/optional-absence-client-<leaf>.log`. Client matrix elapsed time: 92 seconds.

Both harnesses invoke `timeout --foreground 10m ./gradlew :<leaf>:runServer` or `runClient`, with `--no-daemon --console=plain`, and silently preserve full Gradle/runtime output. Existing server EULA and loopback binding prerequisites passed; no legal terms were accepted by the agent.

| Leaf | Server duration | Client duration | Server log | Client log |
| --- | ---: | ---: | --- | --- |
| 1.20.1 Fabric | 21.80s | 22.69s | `build/smoke-server-1.20.1-fabric-20260910-182331-046854.log` | `build/smoke-client-1.20.1-fabric-1789064702411709700.log` |
| 1.20.1 Forge | 24.80s | 24.20s | `build/smoke-server-1.20.1-forge-20260910-182352-848481.log` | `build/smoke-client-1.20.1-forge-1789064725183107604.log` |
| 1.21.1 Fabric | 17.54s | 22.19s | `build/smoke-server-1.21.1-fabric-20260910-182417-648436.log` | `build/smoke-client-1.21.1-fabric-1789064749464658036.log` |
| 1.21.1 NeoForge | 17.54s | 22.69s | `build/smoke-server-1.21.1-neoforge-20260910-182435-184666.log` | `build/smoke-client-1.21.1-neoforge-1789064771737803719.log` |

The eight JSON results were aggregated and asserted to pass in `build/optional-absence-results.json`. All servers reached readiness, stopped and saved all dimensions. All clients initialized the mod, loaded atlases and Patchouli resources, and closed through the window-close request with exit 0. Runtime diagnostic scanning found no optional-API classloading exception. No project Java processes remained after the matrix.

## Limits and diagnostics

Client logs contain ALSA/OpenAL initialization errors (`Failed to open OpenAL device`); Minecraft disabled sound and continued. These runs do **not** meet clean-audio-log or audible-sound acceptance, and no production audio behavior was changed to suppress the diagnostics.

Screenshots were retained outside the repository in the paths listed in each client JSON result. Visual inspection showed normal startup UI: the Forge client was at the first-run accessibility welcome screen, and the 1.21.1 clients were at the main menu. These are not connected recipe-viewer or overlay screenshots.

Still pending: installed-Jade overlays, installed JEI/EMI discovery and rendering, viewer coexistence, reload behavior, transfer authorization/conservation, KubeJS add/remove/reload semantics, optional wearable combinations, packaged-distribution runtime coverage and broader migration acceptance. The integration and release-acceptance parent checkboxes remain open.
