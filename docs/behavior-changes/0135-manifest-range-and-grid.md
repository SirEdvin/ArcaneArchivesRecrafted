# 0135 — Manifest distance preference and grid

## Scope and original behavior

Implements the requested 0.0.2 Manifest configuration path without changing storage eligibility or adding extraction. All four supported leaves are covered.

Pinned upstream: `bb99accf48ed583e29b0efae56e28c963407b8df`.

- `config/ConfigHandler.java:167–173`: grid disabled by default; maximum tracking distance defaults to 100.
- `network/PacketConfig.java:22–35`: the client supplies its distance preference and the server applies it to the player's network.
- `data/ServerNetwork.java:77–82` and `util/ManifestUtils.java:79–97`: the server groups the requesting player's visible storage by range and dimension. Range is not an ownership permission.
- `client/gui/GUIManifest.java:281–295`: draw the original 18-pixel grid cell from base-texture coordinates (224, 0) for populated entries when enabled.

No dynamic client-side reclassification was added: snapshots retain their query-time range grouping. The existing safe distance arithmetic and nonnegative input contract remain in place.

## Implementation

- `ClientConfig` adds `ManifestMaxDistance=100` and `DisableManifestGrid=true` in `config/arcanearchives/client.properties`. Set the latter false to show the grid. Restart to apply file preferences. Existing files/comments/unknown settings are retained; invalid distance or boolean values fail without rewriting them.
- `events/ManifestRequest.java` uses the existing cross-loader request pattern: only container ID and a nonnegative integer distance travel C2S. No player identity, inventory, item or source location is accepted from the client.
- `ManifestScreen` sends that preference once when initializing the view; resize does not flood requests. It draws populated grid cells using the already packaged original pretty/simple artwork.
- `ManifestMenu` waits for the initial request before collecting, avoiding an initial incorrectly grouped default-distance snapshot and a race with the refresh limiter. The server validates its own thread, actual viewer and current menu, then applies the same ten-tick request limit before accepting the distance. Native refresh buttons reuse the accepted distance; Hive membership refreshes retain it and continue to bypass only the manual-refresh limiter.
- The unchanged `StorageNetworks.visible`/`ManifestContents.collect` path determines eligible owned/Hive devices and avoids unavailable chunks. Increasing distance does not include foreign or revoked storage; it only changes range grouping within that visible set.
- Artifact checks require the new production request class and source on every target.

## Verification

Command: `timeout --foreground 10m ./gradlew build --no-daemon`

Exit 0, 84s. Complete log: `build/manifest-range-20260912-065926.log`. All four native required suites passed. Native regressions exercise request-codec round trips for zero, ordinary and maximum integer distances; negative request fields and nonserver/detached requests are rejected. Actual owned/Hive inventory reads verify strict/zero range behavior, safe large-distance arithmetic, unchanged totals and revocation even at the largest accepted distance. These are not connected network-delivery tests.

The range/grid configuration regression passed on every leaf, including the Forge runtime aggregate JUnit report: original defaults, overrides, old-file fallback, maximum integer input and invalid-value file preservation.

Command: `timeout --foreground 2m python3 scripts/verify_artifacts.py`

Exit 0, 0s. Complete log: `build/manifest-range-artifacts.log`. All four production/source pairs passed. `git diff --check` passed and the project process check found no surviving task-owned game JVMs.

## Remaining acceptance

The initial client/server opening request, valid live request throttling, simultaneous players with different preferences, real grid appearance and GUI scales still require the plan's consolidated connected-client campaign. No rendered or connected acceptance is inferred from compilation/native serialization tests. Tracking lines/HUD, hotkey, remaining controls/settings, lectern, Brazier, complete Tome and release acceptance remain open. No commit, version bump or publication performed.
