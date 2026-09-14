# 0133 — Craftable Manifest and read-only browsing route

## Scope and approval

Implements the requested 0.0.2 Manifest item, acquisition, native menu and client inventory-view route. This is an implementation/evidence record, not a proposal to turn the Manifest into a remote extraction terminal. The user-approved plan calls for server authority, bounded requests and current Hive visibility. No storage mutation or new progression recipe is introduced.

The broader Manifest/lectern requirement remains incomplete. Tracking lines/HUD/slot highlighting, hotkey, JEI search synchronization, persistent search/settings, configurable tracking distance and lectern interactions are still open. The current distance classification uses upstream's default 100 blocks, not a completed configuration port. Native widget adaptation is not claimed as final visual parity.

## Original behavior and source

Pinned source: `AranaiRa/ArcaneArchives` at `bb99accf48ed583e29b0efae56e28c963407b8df`.

- `items/ManifestItem.java:31–55`: stack size one, nonconsuming use, sneak clears tracking instead of opening.
- `init/RecipeLibrary.java:76`: Gem Cutter recipe, one paper + one black dye + two Radiant Dust, yielding one Manifest. Reuses already ported modern ingredient tags; no vanilla crafting substitute.
- `network/PacketNetworks.java:65–127`: requesting player's network produces a read-only Manifest response.
- `types/lists/ManifestList.java:79–127,234–258`: localized name/mod-name filtering and name/quantity ordering.
- `inventory/ContainerManifest.java:35–41` and `client/gui/GUIManifest.java:62–185`: nine-by-nine grid, search, sorting, scrolling, refresh and additional controls.
- `config/ConfigHandler.java:171–173`: default tracking distance 100.

The upstream MIT `LICENSE` and existing upstream credits were inspected before reusing assets. Repository licensing/notices remain in place. Original item texture and pretty/simple Manifest base textures are copied from the pinned Git object, not regenerated. SHA-256:

- `textures/items/item_manifest.png`: `a97278ae9cf432cb2c3bbc51c84e4865ad3598d91bd24aded7c986ca1badbc15`
- `textures/gui/manifest_base.png`: `368c3b84a3debfbacd76b3a835a04c1d672b156ae145cd46a4f9e4712936d624`
- `textures/gui/simple/manifest_base.png`: `ef0e8e392a8366655b3875a7f50f0ffaaa5b0b9ff9a6ec040f700f9ec1bc0edf`

## Implemented route

All four targets: Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

- `items/ManifestItem.java`, `ContentRegistry`: registered `arcanearchives:manifest`, original unit stack size/tooltip/model, creative entry, nonconsuming main/offhand use and server-side native menu opening. Sneak does not open; clearing tracking awaits tracking implementation.
- `data/arcanearchives/recipe/manifest.json`: actual Gem Cutter acquisition, including native recipe catalog navigation/payment/output extraction verification.
- `inventory/ManifestMenu.java`: slotless read-only view of `ManifestContents`. Every click type and quick-move path is inert; native refresh button requests must address the viewer's current menu, execute on its server thread and use the supported action. Repeat explicit refreshes are limited to one per ten ticks.
- Opening and Hive audience changes trigger collection; ordinary ticks compare membership without scanning all inventories. Hive change refresh is not delayed by the explicit-button rate limit. Snapshot failure clears old results rather than keeping a revoked Hive visible.
- `events/ManifestSnapshot.java`: registered S2C-only loader transports. Native item serialization uses the world's registry provider, copied unit icons and separate long counts, range groups, dimension-aware sources and descriptions.
- Snapshots are split into bounded 24 KiB fragments. Assembly checks container, offset, total size and sequence, with a 16 MiB aggregate safety limit. Item NBT retains native bounded decoding. Counts, resolved icons, source totals and trailing bytes are validated. A snapshot is published only when fully decoded; encoding failure reports failure instead of truncating the network or disconnecting the player with an oversized payload.
- `client/ManifestClient.java`: client-thread delivery only to the current matching menu; no static world/player inventory cache.
- `client/ManifestScreen.java`: original grid/base artwork and shared pretty/simple preference, search/right-click clear, `@` mod-name filtering, name/long-quantity ordering in both directions, row scrolling, refresh, unit icons, exact totals and Shift source-location tooltips. No visual/client acceptance is claimed by the native server tests.
- `scripts/verify_artifacts.py`: exact production-class whitelist, required source files, pretty/simple textures, model, item texture, atlas registration and version-correct recipe packaging.

## Verification

Initial command:

`timeout --foreground 10m ./gradlew build --no-daemon`

Exit 1, 16s; complete log `build/manifest-interface-first-20260912-063207.log`. Minecraft 1.20.1 compilation found the modern-only `Player.registryAccess()` call. Corrected both production and regression paths to the shared `player.level().registryAccess()` API. This was a compile correction, not a claimed behavioral RED/GREEN.

Intermediate four-target build: exit 0, 88s; `build/manifest-interface-second-20260912-063258.log`.

Final command, after acquisition coverage and packaged checks:

`timeout --foreground 10m ./gradlew build --no-daemon`

Exit 0, 58s; complete log `build/manifest-interface-final-20260912-063730.log`. Fresh required-suite confirmations: 1.20.1 Fabric 7, Forge 8, 1.21.1 Fabric 7, NeoForge 7. New assertions run within each native device-ownership suite:

- Registry-aware snapshot round-trip preserves real fixture item components, source descriptions/positions, range and counts above `Integer.MAX_VALUE`.
- Multi-fragment transport codec and real receiving-menu assembly publish atomically; stale containers, out-of-order/truncated/trailing/oversized data fail safely; a later valid snapshot recovers.
- Slotless click/quick-move paths preserve the carried item; invalid and detached refresh requests fail. Manifest use preserves the item in either hand.
- The shipped Gem Cutter recipe is found through native catalog navigation, rejects insufficient dust without partial payment, consumes the exact full payment, and supplies one extractable Manifest without a second unpaid craft.

Artifact command: `timeout --foreground 2m python3 scripts/verify_artifacts.py`, exit 0, 0s; complete log `build/manifest-interface-artifacts.log`. All four production/source pairs pass. `git diff --check` passes; project game-process check found no surviving task-owned game JVMs.

## Remaining acceptance

### Bounded location tooltip follow-up

Restored upstream `GUIManifest.java:475–490`'s ten-location limit and the original `arcanearchives.tooltip.manifest.andmore` overflow message. The modern translation uses `%s`, supported by native translatable components, instead of the legacy formatter's `%d`. Only presentation is bounded: server listing, aggregate counts and stored source locations are unchanged. This is original behavior restoration on all four targets, not a proposed gameplay deviation.

`timeout --foreground 10m ./gradlew build --no-daemon` passes (exit 0, 87s, `build/manifest-tooltip-20260912-082509.log`), including all four native server suites. `timeout --foreground 2m python3 scripts/verify_artifacts.py` passes (exit 0, 0s, `build/manifest-tooltip-artifacts.log`), all four artifact pairs. These checks do not exercise rendered tooltips; GUI-scale/overflow visual acceptance remains deferred to the consolidated campaign.

### Pixel scrolling and keyboard follow-up

#### Draggable scrollbar follow-up

Restored the twelve-pixel thumb on the original x=178, y=29..191 track, replacing the temporary page buttons. `controls/ScrollBar.java:45–75,91–96,120–128` defines centered thumb dragging and page clicks outside the thumb; `TexturedButton.java:15–21` defines the original twelve-pixel artwork. Dragging snaps to the existing six-pixel steps and clamps outside the track. Release, screen close and list rebuild clear dragging state. Arrow/Page keys remain available without a mouse.

Recovered `textures/gui/buttons.png` and `textures/gui/simple/buttons.png` directly from pinned commit `bb99accf48ed583e29b0efae56e28c963407b8df` after reviewing its MIT license and retained credits. The artifact verifier checks both packaged images against source bytes on every leaf. No storage/server behavior or gameplay deviation is introduced.

`timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 86s, `build/manifest-scrollbar-20260912-083646.log`. All four native suites pass; all four XML reports confirm four scroll geometry tests with no skips/failures/errors, including dragging, endpoint clamping, keyboard-after-drag and empty/non-scrollable lists. `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/manifest-scrollbar-artifacts.log`, all four pairs. Actual mouse capture, rendering, narration and GUI-scale acceptance remain unverified in the consolidated client campaign.

#### Initial keyboard checkpoint

Restored six-pixel arrow/wheel movement and nine-row Page Up/Down movement, traced through `ContainerManifest.SCROLL_STEP`, `registerScrollEventManager`, and `GUIManifest.handleMouseInput/keyTyped`. The screen now uses shared pixel geometry for drawing and tooltip hit testing, clips partial rows, and clamps movement at both ends. Search/filter rebuilds reset the position. This restores original behavior rather than introducing whole-row arrows; no server or storage authority changes.

Three `ManifestScrollTest` regressions cover fractional-row geometry, page/clamp/reset behavior, empty lists, partial last rows and viewport boundaries. XML reports confirm all three pass without skips on each of the four leaves. Forge requires explicit inclusion of this Minecraft-independent fixture in its unit-test allowlist; its first successful build did not run this new fixture, so the allowlist was corrected before reporting matrix coverage.

`timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 62s, `build/manifest-scroll-final-20260912-083204.log` (initial full build exit 0, 83s, `build/manifest-scroll-20260912-082931.log`). `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/manifest-scroll-artifacts.log`, all four pairs. Native suites pass; unchanged tasks may be up-to-date. Actual keyboard delivery, scissor/rendering and GUI-scale acceptance remain in the consolidated client campaign.

The mock-player/native-menu tests do not prove a connected player's opening handshake, loader network delivery, visual controls/tooltip fit, accessibility at all GUI scales, live Hive revocation or resource reload. No new rendering client was launched: the release plan explicitly consolidates that campaign after nonvisual implementation. Finish the remaining Manifest features and lectern, then Brazier/Tome and the full four-target multiplayer/persistence/interface campaign. Do not mark 0.0.2, the Manifest parent task, or Milestone 1 complete from this checkpoint.
