# 0139 — Native Manifest server-session verification

## Scope and approval status

Verification of the existing server-owned Manifest opening and refresh rules from 0133–0136. No production gameplay, networking registration, permissions or configuration was changed. Applies to all four supported targets.

`ManifestServerSession` uses a native registered `ServerPlayer`, rather than the lightweight players used by earlier inventory/codec fixtures. Its assertions exercise:

- Sneaking does not open through the hotkey entry.
- The normal server hotkey entry creates an actual Manifest container.
- Repeating it does not replace an active container.
- Negative distance is rejected and the first valid request succeeds.
- A second request in the same tick is rejected.
- A detached menu is rejected even with the same container ID.
- A request succeeds after the existing ten-tick interval, while an immediate manual refresh is throttled.
- Closing invalidates the old menu; reopening creates a new one without restoring the old menu's authority.

The existing integrated native regression now waits for the delayed session assertions before succeeding. Task-owned players are removed/disconnected in cleanup on successful and failed assertions.

## Loader fixture corrections

The first run failed on Forge because the vanilla 1.20 mock player's connection has no Netty channel, while Forge login hooks inspect the pipeline. The Forge-only adapter now constructs a native server player with a `Connection(PacketFlow.SERVERBOUND)` backed by an `EmbeddedChannel`, then registers it through `PlayerList.placeNewPlayer`. This follows the inspected newer native helper pattern and uses already available test-runtime classes.

The second run passed Forge but failed on NeoForge because the vanilla mock has not negotiated the required Manifest payload channel. The pinned NeoForge source exposes `NetworkRegistry.configureMockConnection` specifically for tests; its adapter now invokes that helper. Production channel registration and validation were not weakened. Fabric retains its version's native helper.

These are test transports, not rendering clients. A successful server send call is not proof of client receipt/decoding or real channel negotiation. Incoming hotkey/request packet dispatch, spectator/dead-player cases, actual widgets, two-player views and persistence remain separate acceptance work.

## Verification

Executed on 2026-09-12, each build using `timeout --foreground 10m ./gradlew build --no-daemon` with complete silent file logging:

1. Exit 1, 49 seconds; `build/manifest-server-session-20260912-075646.log`: Forge fixture null-channel failure during native login.
2. Exit 1, 57 seconds; `build/manifest-server-session-second-20260912-080034.log`: NeoForge fixture rejected `arcanearchives:manifest_snapshot` as unnegotiated.
3. Exit 0, 56 seconds; `build/manifest-server-session-final-20260912-080244.log`: all four native suites pass, including the delayed server session.
4. `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, 0 seconds; `build/manifest-server-session-artifacts.log`. All four production/source pairs pass.
5. `git diff --check`: exit 0. Project Java process check found none.

No full connected-client campaign was launched. The fixture checks are stronger evidence for server opening/refresh than the earlier lightweight mocks, but do not complete Manifest, tracking/HUD, Brazier, Tome or 0.0.2. No commit, version bump or publication performed.
