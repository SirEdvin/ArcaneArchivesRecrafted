# 0136 — Manifest hotkey opening

## Original behavior and source

Pinned upstream checkout `bb99accf48ed583e29b0efae56e28c963407b8df`:

- `client/Keybinds.java:43–46`: the Manifest key is registered unbound, independently of the Arsenal feature gate.
- `client/Keybinds.java:75–97`: when the game has focus, the default presence preference requires a Manifest in one of the 36 main inventory slots. Armor/offhand slots do not satisfy this particular shortcut. Disabling the preference permits opening without carrying the item. Missing-item feedback uses the yellow `arcanearchives.gui.missing_manifest` translation.
- `config/ConfigHandler.java:164–165`: `ManifestPresence` defaults true.
- `client/Keybinds.java:90–91,104–133`: sneaking clears tracking; the shortcut over a container/JEI ingredient starts item tracking. These tracking paths are NOT implemented by this increment.
- `src/main/resources/assets/arcanearchives/lang/en_us.lang:328–329`: original activation label and missing-item message.

The upstream MIT license was inspected before reusing translation text. Existing repository upstream notices remain in the verified artifacts.

## Implementation and approval status

Original opening/configuration restoration under the requested 0.0.2 scope; no new extraction, acquisition or network-visibility rule is introduced. Applies to 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

- `client/ManifestKey.java`: loader-native unbound key registration and client tick handling, independent of Arsenal. Coalesces pending clicks, ignores unfocused windows/open screens/dead or spectator players, checks the original main-inventory presence rule, and sends an opening request only when not sneaking.
- `config/ClientConfig.java`: `ManifestPresence=true` in new client properties; absent settings retain the default, explicit false is supported, invalid booleans fail without replacing the file.
- `events/OpenManifest.java`: zero-field C2S request using the existing socket-key transport pattern. Each loader dispatches to the server thread using its authenticated sender, with no client-supplied identity, location, inventory or storage owner.
- `inventory/ManifestMenu.java`: hotkey entry refuses off-thread requests, active containers, spectators and sneaking; existing opening rejects dead players. An already opened Manifest cannot be replaced by repeated opening requests. Listing still waits for validated settings and applies current personal/Hive visibility.
- Presence is deliberately a local preference, NOT an authorization control: a client configured with presence disabled may open its own read-only Manifest without the item, as upstream allows. This does not grant access to someone else's storage.
- All loader/client registrations and production/source artifact expectations include the new classes.

There are no tracking placeholder handlers. Sneaking currently does not open, as in the existing item path; functional clearing, item-under-cursor tracking, HUD/lines and slot highlighting remain open. This record does not mark the whole hotkey or Manifest complete.

## Verification

### Sneaking clear shortcut follow-up

Native dimension-transition follow-up: the fixture selects a real personal chest, closes the Manifest, transfers the registered player to the Nether using native `execute in ... run tp`, and invokes sneaking clear. It verifies the original dimension's selection survives unchanged and no menu opens, then returns the player in `finally` and verifies clearing in the original dimension removes it. No production behavior changed. This proves foreign-dimension retention across native transfers, not simultaneous populated selections in both dimensions or actual client key/network/rendering behavior. Four-leaf `timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 62s (`build/manifest-dimension-clear-20260912-105220.log`), all native suites pass. Artifact verifier: exit 0, 0s (`build/manifest-dimension-clear-artifacts.log`), all four production/source pairs pass.

`client/Keybinds.java:90–91` delegates sneaking to `LineHandler.clearChests`, whose lines 68–73 remove only the current dimension's tracked locations. The port now sends a separate zero-field `ClearManifestTracking` request when the existing presence/focus checks pass and the player is sneaking. The server resolves the sender's current dimension, requires its registered living nonspectator player on the server thread with no open container and sneaking, and removes only that dimension's selections. It does not open a menu or accept client coordinates. All four loader registrations and exact production/source artifact inventories include the payload. This restores original clearing intent within approved server-authoritative tracking under 0140, not a new permission or gameplay rule.

Native regressions verify that non-sneaking and active-menu requests retain selections, while a sneaking request after close clears current-dimension tracking without opening a menu. `timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 85s, `build/manifest-clear-key-20260912-100410.log`, all four native suites. `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/manifest-clear-key-artifacts.log`, all four pairs.

The earlier no-clearing implementation statements below/above are historical and superseded for this server route and client key wiring only. Actual keyboard/network delivery, preservation of simultaneously selected other-dimension markers through a real transition, client marker synchronization and visual clearing remain pending. Under-cursor tracking and HUD/lines are still absent.

Executed on 2026-09-12:

- `timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 88 seconds; complete output `build/manifest-key-20260912-071714.log`.
- All four native suites passed. The extended Manifest regression exercises empty inventory, offhand-only exclusion and a Manifest in every main inventory slot, restoring the original fixture inventory in `finally`.
- `manifestPresenceDefaultsOnAndPreservesExistingFiles` passed on all four targets, including Forge's aggregate native JUnit XML. It verifies new/default/missing settings, explicit false and invalid-file preservation.
- `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, 0 seconds; complete output `build/manifest-key-artifacts.log`. All four production/source pairs pass.
- `git diff --check`: exit 0. Project Java process check found none.

No connected client was launched. Actual Controls binding, keyboard/focus behavior, missing-item feedback, request delivery and server opening guards still require the consolidated connected-client campaign. Native inventory/configuration coverage and compilation do not prove those paths. Tracking, lectern, Brazier, complete Tome and release acceptance remain outstanding. No commit, version bump or publication performed.
