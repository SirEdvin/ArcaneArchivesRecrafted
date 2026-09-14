# Session handoff — Arcane Archives Recrafted

## 0.0.2 GitHub testing release preparation

The user explicitly authorized committing/pushing and publishing 0.0.2 on GitHub for testing. Version metadata is now 0.0.2; `verifyReleaseArtifacts --no-daemon` passed all four leaves and production/source checks (exit 0, 88 s, `build/release-002-verify-20260914-092627.log`). Release notes prominently retain unverified graphical/connected/restart acceptance. Earlier no-publication authorization and 0.0.1-metadata statements below are historical. Publication itself must be read back before claiming success; no other distribution platform is authorized.

## Current 0.0.2 checkpoint — implementation ready for initial gameplay testing

See [the consolidated checkpoint](migration/0.0.2-IMPLEMENTATION_CHECKPOINT.md) for current implementation, logs, artifact paths/checksums and deferred acceptance. The full production Tome, both Chest recipes, Manifest hovered-item tracking and both storage player preferences are implemented. Native four-target builds, separate clean/build, normalized Stonecutter round-trip restoration, production/source isolation and 25 Python tests pass. Canonical active state is restored; no project JVMs remain. Native suite totals are 7/8/7/7, not connected-player scenario counts.

Do not resume the historical "pages absent" or "preferences absent" work below. Remaining work is the user-first gameplay test followed by graphical/connected/restart/optional-integration acceptance and release preparation. No new client campaign was run. Metadata is deliberately still 0.0.1 and the entire working tree remains uncommitted; no push/tag/publication was authorized. Original unfinished Tome topics and approved scope exclusions remain explicit rather than fabricated features.

Two verification issues are documented rather than hidden: Loom requires separate clean/build invocations for its generated launch arguments here; the first Stonecutter round trip normalized inactive nested-branch comment markers in five files, while the subsequent full round trip restored the normalized canonical bytes exactly.

## Historical handoff entries

## 0.0.2 in progress — selected-target chunk availability

Extended the native chunk-edge fixture to release the selected source chunk itself, assert actual unavailability, retain tracking without loading it through reconciliation, and resume missing-monitor revocation after explicit availability restoration. Held-ticket cleanup now runs in nested `finally`. No production change. Four-leaf build exit 0, 92s (`build/tracking-target-unavailable-20260912-130832.log`); native suites/artifact pairs pass (`build/tracking-target-unavailable-artifacts.log`). Target/attachment availability transitions are now covered; completed disk unload/reload, restart and connected rendering remain unverified. 0.0.2 is incomplete.

## 0.0.2 in progress — native attachment-chunk availability

New distant chunk-edge fixture selects a monitored barrel, removes its monitor, explicitly removes the neighboring chunk's temporary UNKNOWN ticket and ticks the native cache while holding the target chunk. It verifies unavailable-neighbor retention without reconciliation loading it, then explicit availability followed by revocation. Setup wakeup failures were fixed in the fixture, not production. Final four-leaf build exit 0, 79s (`build/tracking-chunk-boundary-tickets-20260912-130457.log`); all native suites/artifact pairs pass (`build/tracking-chunk-boundary-artifacts.log`). This proves adjacent chunk availability boundaries, not completed disk unload/reload, target-chunk persistence or connected visual invalidation. 0.0.2 remains incomplete.

## 0.0.2 in progress — double-chest split grants

Native regression now removes the opposite half supplying the sole personal monitor grant, verifies the selected half survives as SINGLE, and checks tracking clears without changing its inventory. No production change. Four-leaf build exit 0, 59s (`build/tracking-chest-split-20260912-125533.log`); native suites/artifact pairs pass (`build/tracking-chest-split-artifacts.log`). This is loaded split behavior, not unavailability/reload or connected rendering acceptance. 0.0.2 remains incomplete.

## 0.0.2 in progress — Monitoring Crystal retargeting

Removed the stale cached target from `MonitoringCrystalBlockEntity`; target lookup now derives from current facing for every consumer. Native rotation preserves the installed block entity and verifies new attachment, old-grant removal, no resurrection after facing restoration and explicit reselection. Approved 0143 correction; no new player interaction added. RED→GREEN, final four-leaf build exit 0, 109s (`build/monitor-retarget-20260912-125114.log`); all native suites/artifact pairs pass (`build/monitor-retarget-artifacts.log`). Dedicated unavailability/reload and connected rendered acceptance remain open. 0.0.2 is incomplete.

## 0.0.2 in progress — current Monitoring Crystal grants

External tracking now intersects captured owners with current authorized source grants, reusing `sourceOwners` once per player pass. Missing-monitor decisions defer unless all possible attachment chunks (including a double chest's other half) are available; membership revocation remains immediate. Arcane targets reduce grants to their current authorized owner. Native RED→GREEN verifies independent-grant retention after one monitor removal, clearing after last removal, no resurrection and successful reselection. Existing double-chest/Hive/removal cases pass. Four-leaf build exit 0, 89s (`build/tracking-monitor-20260912-124647.log`); native suites/artifact pairs pass (`build/tracking-monitor-artifacts.log`). Dedicated unavailable-chunk/reload and retargeting tests plus connected visual acceptance remain pending; 0.0.2 is incomplete.

## 0.0.2 in progress — Arcane replacement ownership

Loaded Arcane targets now need a current owner among captured tracking grants and the current audience. Same-owner replacement remains tracked; foreign-owner replacement clears only its marker, and ownership restoration does not resurrect it. Native RED→GREEN proved these cases. Four-leaf build exit 0, 86s (`build/tracking-replacement-20260912-124158.log`); all native suites and artifact pairs pass (`build/tracking-replacement-artifacts.log`). External/non-Arcane monitor-grant reconciliation, dedicated unavailability and connected rendering acceptance remain pending. 0143 and 0.0.2 are not complete.

## 0.0.2 in progress — confirmed tracking-source removal

Under approved 0143, `ManifestTracking.tick` now removes markers whose available loaded target no longer has a block entity. Missing dimensions/unavailable chunks are skipped; no inventory scan. Native RED→GREEN proves empty inventory retains tracking, loaded removal clears and recreation does not resurrect it. Four-leaf build exit 0, 86s (`build/tracking-source-removal-20260912-123801.log`); all native suites/artifact pairs pass. Full replacement/current-monitor-grant reconciliation, dedicated unavailability regression and connected visual invalidation remain pending. 0.0.2 is incomplete.

## 0.0.2 in progress — native Trove HUD synchronization

`TroveHudSync` now verifies actual native update-packet tags and detached replica deserialization for populated, empty LOCKed and refilled Troves, preserving counts/reference/upgrades and live count. A fixture-only missing-level registry-context failure was fixed. No production change. Final four-leaf build exit 0, 59s (`build/trove-hud-sync-final-20260912-121752.log`); native suites/artifact pairs pass (`build/trove-hud-sync-artifacts.log`). No remote client delivery or rendered HUD claim; 0.0.2 remains incomplete.

## 0.0.2 in progress — original Trove HUD

Added `TroveHud` and `TroveHudText`, native client registrations, original translations, format regression and artifact entries. Upstream RenderHUD is specifically a looked-at Trove display, not a Manifest marker panel. Existing synchronized inventory/LOCK/upgrades feed original item/name/count/upgrade geometry; decimal abbreviations are separate from the unchanged binary MathUtils contract. Four-leaf build exit 0, 92s (`build/trove-hud-20260912-120941.log`); native suites, formatter tests and artifacts pass. Four graphical startup smokes pass (exit 0, 95s, `build/trove-hud-client-smoke-20260912-121125.log`), all clients closed normally. Actual in-world HUD layout and synchronized updates remain unverified. See 0142; 0.0.2 remains incomplete.

## 0.0.2 in progress — ray-state failure/revocation regression

Native tracking tests now verify exact retained ray coordinates after revocation/stale replay, fail-closed ray and highlight projection after explicit snapshot failure, rejection of an older successful replay and recovery from a newer valid snapshot. No production change. Four-leaf build exit 0, 60s (`build/manifest-ray-revocation-20260912-120432.log`); all native suites and artifact pairs pass (`build/manifest-ray-revocation-artifacts.log`). These assertions exercise the production receiver/projection/matching code, not OpenGL output or remote delivery. 0.0.2 remains incomplete.

## 0.0.2 in progress — world tracking rays

Implemented `ManifestRays` projection/width policy and client-only `ManifestRayRenderer`, initialized through `ManifestKey`. Approved 0141 one-pixel minimum is active. Rays use confirmed current-dimension deduplicated coordinates, original player-plus-one to block-center endpoints, game-time color phase and through-wall drawing with render-state restoration. Four-leaf build exit 0, 93s (`build/manifest-rays-20260912-115828.log`), all native/width tests and artifact pairs pass. Four graphical startup smokes pass, exit 0, 99s (`build/manifest-rays-client-smoke-20260912-120031.log`); all clients close normally. No in-world ray draw was exercised: actual camera/occlusion/driver widths/render-state/connected behavior remains unverified. HUD, device reconciliation and wider 0.0.2 work remain unfinished. Earlier statements that world rays are absent are historical, not the current implementation state.

## 0.0.2 in progress — native dimension-clear boundary

Added native transfer coverage to `ManifestTrackingLifecycle`: a real selected personal source survives sneaking clear after transfer to the Nether; returning to the original dimension and clearing removes it. Return runs in `finally`. No production change. All four native suites and artifact pairs pass: build exit 0, 62s (`build/manifest-dimension-clear-20260912-105220.log`), verifier exit 0 (`build/manifest-dimension-clear-artifacts.log`). This proves foreign-dimension retention through native transfer, not simultaneous populated selections in both dimensions or connected client/key/rendering behavior. World HUD/lines and device reconciliation remain unfinished; 0.0.2 is incomplete.

## 0.0.2 in progress — selected-item feedback

Manifest grid rendering now highlights server-confirmed selected references using upstream's original eight-color, twelve-tick transitions. `ManifestHighlight` shares count-independent item/data matching with native tests; clearing snapshots removes the highlight predicate. Two color tests ran on each leaf, plus native identity/data/count/clear assertions. Four-leaf build exit 0, 90s (`build/manifest-highlight-20260912-104800.log`); final artifact verifier exit 0 (`build/manifest-highlight-artifacts-final.log`) after adding the new class to the explicit artifact contract. Actual pixel layering/GUI-scale/clipping and connected interaction checks remain pending. World HUD/lines and device reconciliation are still unfinished; 0.0.2 is incomplete. Older claims that selection feedback is absent refer to preceding checkpoints.

## 0.0.2 in progress — tracking synchronization

S2C tracking state is now implemented. `ManifestSnapshotReceiver` shares bounded/revision-aware assembly with menu listings; reserved snapshot context 0 carries reference items/positions independently of the open menu. `ManifestTracking` sends initial/changed/empty states without serializing ownership grants; `ManifestClient` routes and resets connection-scoped state and rejects callbacks from a different source connection. Unreleased Forge/NeoForge snapshot protocol is 3; no mod version bump/publication.

All four native suites and artifact pairs pass: final build exit 0, 88s (`build/manifest-tracking-sync-final-20260912-102329.log`), artifacts exit 0 (`build/manifest-tracking-sync-artifacts.log`). Native tests verify reference/grant-omission encoding, stale revocation replay, context isolation and empty clearing; the server send path executes but remote packet delivery/counts and reconnect races are unverified. HUD/lines and selected-slot feedback are still absent, and device replacement reconciliation remains open. 0.0.2 is incomplete; older no-S2C statements below are historical.

## 0.0.2 in progress — native selection-before-close handler

The native tracking fixture now applies its decoded selection, calls the actual server container-close packet handler, verifies retained markers and rejects a decoded selection replay after close. No production code changed. Full build exit 0, 59s (`build/manifest-native-close-20260912-101341.log`); all four native suites/artifact pairs pass (`build/manifest-native-close-artifacts.log`). This is handler-level evidence, not real socket ordering/client input. S2C tracking state, HUD/lines and other documented release work remain unfinished; 0.0.2 is incomplete.

## 0.0.2 in progress — original Shift-close preference

`ManifestHoldShift=true` is restored in client config and Manifest left-click handling: default left-click sends selection then closes unless Shift is held; false reverses the Shift condition; right-click removal never closes. The upstream click predicate, not its contradictory config comment, defines behavior. Config/file-preservation and all click-policy combinations pass on all four targets, including native Forge JUnit. Full build exit 0, 85s (`build/manifest-shift-close-20260912-100901.log`); all four native suites/artifact pairs pass (`build/manifest-shift-close-artifacts.log`). Real input/packet ordering and visible tracking remain unverified; S2C tracking/HUD still unimplemented. 0.0.2 remains incomplete.

## 0.0.2 in progress — sneaking clear shortcut

`ManifestKey` now sends zero-field `ClearManifestTracking` when sneaking, after existing focus/presence guards. All loaders register it; `ManifestTracking.clearFromKey` validates the native sender and clears only its current dimension without opening a menu. Native guard/current-dimension clearing tests pass on all four targets; build exit 0, 85s (`build/manifest-clear-key-20260912-100410.log`), all artifact pairs pass (`build/manifest-clear-key-artifacts.log`). Real key delivery, retained other-dimension selections across transitions, client synchronization and HUD/lines remain open. No mod version bump/publication; 0.0.2 remains incomplete.

## 0.0.2 in progress — tracking through Hive succession

Added native three-owner active-tracking regression for founder resignation, promotion, continued remaining-member access and final Hive dissolution. With the Manifest closed, only departed owners' sources are pruned; personal tracking remains. No production change. Full four-leaf build exit 0, 59s (`build/manifest-tracking-succession-20260912-100030.log`); all native suites/artifact pairs pass (`build/manifest-tracking-succession-artifacts.log`). Client tracking synchronization/HUD and replacement reconciliation remain open; 0.0.2 is incomplete.

## 0.0.2 in progress — opposite-half tracking grants

Native double-chest tracking regression now verifies two owners monitoring opposite halves, a foreign-half descriptor retaining the viewer's independent grant, nonduplicated counts, retention after closed-menu Hive resignation and clear. No production behavior changed. Full build exit 0, 59s (`build/manifest-tracking-double-chest-20260912-095719.log`); all four native suites/artifact pairs pass (`build/manifest-tracking-double-chest-artifacts.log`). Split/replacement reconciliation, client tracking synchronization and HUD remain open; 0.0.2 is incomplete.

## 0.0.2 in progress — native tracking tick dispatch

Added delayed native event-dispatch verification: close Manifest, resign, drive scheduled native ticks, restore membership before querying, then require foreign markers already removed and personal markers retained. Embedded test connections require an explicit native listener tick to trigger player-tick events; no production hook changed. Final native player removal/disconnect is also followed by an empty-state query. Full build exit 0, 59s (`build/manifest-tracking-ticks-final-20260912-095321.log`); all four native suites/artifact pairs pass (`build/manifest-tracking-ticks-artifacts.log`). This improves native dispatch coverage, not real connection-loop/S2C/rendering acceptance. Client tracking synchronization and HUD remain unimplemented; 0.0.2 is incomplete.

## 0.0.2 in progress — active server tracking

`ManifestSelect` is registered on all loaders; grid left/right clicks and X clear send revision/index-bound commands to `ManifestTracking`. Server session markers survive menu close and prune revoked Hive grants without per-tick inventory/chunk scans. Native tests cover command codec/application, personal/Hive markers, resignation, personal-source retention, no automatic revival on rejoin, remove/clear and independent grants on a duplicate-monitored barrel. Full build exit 0, 84s (`build/manifest-tracking-state-checkpoint-20260912-094610.log`), all four native suites and artifact pairs pass (`build/manifest-tracking-state-artifacts.log`).

No S2C marker state or invalidation is sent yet; there is no visible selection feedback/HUD. Device replacement/ownership reconciliation, shift-close, shortcuts, dimension/disconnect and connected acceptance remain open. Tick pruning assertions call the production tick method directly, not an observed automatic client/server event round-trip. See 0140 for exact scope and resolved fixture failures. 0.0.2 is not complete.

## 0.0.2 in progress — native container-ID reuse

Fixed stale selection acceptance when Minecraft wraps its native menu ID: snapshot revisions are now process-wide rather than restarting per menu. Native RED/GREEN opens menus until the original ID returns, rejects the old revision and accepts the current one. All four native suites pass: full build exit 0, 89s (`build/manifest-id-reuse-20260912-093130.log`); all artifact pairs pass (`build/manifest-id-reuse-artifacts.log`). No tracking packet/UI or marker lifecycle is implemented by this fix; 0.0.2 remains incomplete.

## 0.0.2 in progress — interrupted Manifest refresh safety

Native RED exposed stale published entries retained during a partial newer revision. `ManifestMenu.receive` now clears old entries and prior failure state at new assembly start, remaining not-ready until complete. Replacement/interleaved-fragment regression passes across all four native suites; full build exit 0, 86s (`build/manifest-interleave-20260912-091948.log`), all artifact pairs pass (`build/manifest-interleave-artifacts.log`). Tracking packet/UI, active markers and closed-screen invalidation are still unimplemented; 0140 approval remains valid.

## 0.0.2 in progress — revision-bound Manifest snapshots

`ManifestSnapshot` now carries a per-menu revision; `ManifestMenu` rejects older generations and requires the current revision for tracking selection resolution. Native transport/selection tests pass on all four leaves (exit 0, 87s, `build/manifest-revisions-20260912-091439.log`); all artifacts pass (`build/manifest-revisions-artifacts.log`). Forge/NeoForge Manifest snapshot protocol is `2`; no mod version bump or publication. Tracking packet/UI must bind clicks to a fully received snapshot. Selection wiring, active markers, closed-screen invalidation and HUD remain unimplemented; 0140 approval stands.

## 0.0.2 in progress — server tracking-selection validation

`ManifestMenu.trackingSelection` now resolves only current server-authorized selections from a retained published snapshot and fresh projection. Native tests cover selection bounds/identity, revoked/restored audience and throttling; same-dimension out-of-range selection is intentionally preserved from original `ContainerManifest.slotClick`. Full build exit 0, 86s (`build/tracking-selection-final-20260912-090914.log`); all artifacts pass (`build/tracking-selection-artifacts.log`). The resolver has native-test consumers only: tracking packet/screen wiring, stored markers, closed-screen invalidation and HUD remain open. 0140 approval stands; no further decision is needed on revocation behavior.

## 0.0.2 in progress — shared current permissions for approved tracking

0140 is approved: remove selected markers after access revocation even with the Manifest closed. Discovery and Manifest refresh now share `StorageNetworks.audience`; native permission/succession/disbanding/immutability checks pass. Initial refactor exposed immutable-set null handling for ownerless devices, now explicitly guarded. Full build exit 0, 87s (`build/network-audience-final-20260912-090051.log`); all artifacts pass (`build/network-audience-artifacts.log`). This is a prerequisite, not tracking implementation: selection, closed-screen watcher, invalidation transport and HUD/lines remain. No further approval is needed for the recorded revocation behavior. Brazier/Tome and consolidated acceptance also remain open.

## 0.0.2 in progress — native enchanted-book search verification

[0134 native follow-up](behavior-changes/0134-manifest-search-integration.md#native-item-verification) calls the same production method as the screen with command-created enchanted books/swords. Native tests verify stored-book matching, tool exclusion, wrong-level/mod-query rejection, empty books and unchanged input stacks. All four native suites pass (exit 0, 59s, `build/manifest-native-search-final-20260912-085147.log`); all artifact pairs pass (`build/manifest-native-search-artifacts.log`). Client language/reload/rendering is not proven. Tracking/HUD, Brazier, complete Tome and consolidated acceptance remain open. No commit/publication performed.

## 0.0.2 in progress — enchanted-book search

[0134 follow-up](behavior-changes/0134-manifest-search-integration.md#enchanted-book-search-follow-up) restores ordinary-query matching against stored enchantment names/levels, excluding mod queries and enchanted tools. Version-native APIs compile on all four leaves; nine search/session tests pass per leaf. Full build exit 0, 85s (`build/manifest-enchantment-search-20260912-084601.log`); all artifact pairs pass (`build/manifest-enchantment-search-artifacts.log`). New tests cover string predicates, not native enchanted-item or connected-screen execution. Tracking/HUD, Brazier, complete Tome and consolidated acceptance remain open. No commit/publication performed.

## 0.0.2 in progress — Manifest registry search fallbacks

[0134 follow-up](behavior-changes/0134-manifest-search-integration.md#registry-fallback-follow-up) restores item-path and `@` namespace matching without broadening item/mod query boundaries. Eight search/session tests pass on each leaf; Forge's explicit allowlist now includes this pure fixture, correcting earlier overbroad Forge test claims. Full build exit 0, 91s (`build/manifest-search-fallback-20260912-084143.log`); all artifacts pass (`build/manifest-search-fallback-artifacts.log`). Original enchanted-book enchantment-name search remains unported. Connected acceptance, tracking/HUD, Brazier and complete Tome remain open. No commit/publication performed.

## 0.0.2 in progress — draggable Manifest scrollbar

[0133 follow-up](behavior-changes/0133-manifest-browsing-route.md#draggable-scrollbar-follow-up) restores the original thumb/track, page clicks, six-pixel drag snapping and pretty/simple artwork, replacing temporary page buttons. Keyboard navigation remains. Four geometry regressions pass on each leaf; all native suites and artifact pairs pass (`build/manifest-scrollbar-20260912-083646.log`, exit 0, 86s; `build/manifest-scrollbar-artifacts.log`). Real mouse/rendering/narration acceptance remains open. Tracking/HUD, Brazier and complete Tome remain outstanding. No commit/publication performed.

## 0.0.2 in progress — Manifest pixel scrolling

[0133 follow-up](behavior-changes/0133-manifest-browsing-route.md#pixel-scrolling-and-keyboard-follow-up) restores six-pixel arrow/wheel movement, nine-row paging, clipped partial rows and matching tooltip hit geometry. Three geometry tests pass on all four leaves after adding the pure test to Forge's explicit allowlist. Full build exit 0, 62s (`build/manifest-scroll-final-20260912-083204.log`); all artifact pairs pass (`build/manifest-scroll-artifacts.log`). Actual keyboard/scissor/GUI-scale validation remains deferred. Tracking/HUD, Brazier and Tome remain outstanding. No commit/publication performed.

## 0.0.2 in progress — bounded Manifest location tooltips

[0133 follow-up](behavior-changes/0133-manifest-browsing-route.md#bounded-location-tooltip-follow-up) restores the original ten-location tooltip cap and overflow translation without truncating actual listing data. Four-target build/native suites pass (exit 0, 87s, `build/manifest-tooltip-20260912-082509.log`); all artifacts pass (`build/manifest-tooltip-artifacts.log`). No rendering acceptance claimed. Keyboard source tracing found upstream six-pixel arrow movement versus this port's whole-row scrolling; restore the original contract, not one-row arrows. Tracking/HUD, Brazier and Tome remain outstanding. No commit/publication performed.

## 0.0.2 in progress — native advancement reload

[0138 native reload follow-up](behavior-changes/0138-manifest-advancements.md#native-reload-follow-up) verifies disk reload of earned progress, timestamp preservation and restoration of pending lectern acquisition listeners. Completed progress also survives a second native reload with empty inventory. All four native suites pass (`build/manifest-reload-20260912-082110.log`, exit 0, 59s); all artifact pairs pass (`build/manifest-reload-artifacts.log`). No production change. Full resource reload/reconnect/process restart remain open. Tracking/HUD, Brazier and complete Tome implementation remain outstanding; no commit/publication performed.

## 0.0.2 in progress — native advancement awards and disk saves

The [0138 follow-up](behavior-changes/0138-manifest-advancements.md#native-acquisition-and-disk-save-follow-up) extends the 0139 native player fixture to exercise automatic inventory-change awards and read back the UUID-specific advancement file after native saves. Unrelated items do not trigger the branch; Manifest acquisition earns only its criterion, removal preserves its timestamp, and lectern acquisition completes both. All four native suites pass (exit 0, 59s, `build/manifest-awards-20260912-080944.log`); all artifacts pass (`build/manifest-awards-artifacts.log`). No production behavior changed. Actual survival pickup/crafting, client presentation, reload/reconnect and process restart remain unverified; continue tracking/HUD, Brazier and Tome implementation. No commit/publication performed.

## 0.0.2 in progress — native Manifest server session

[0139](behavior-changes/0139-manifest-server-session.md) adds a registered native `ServerPlayer` regression for actual server container opening, active/detached/closed-menu guards, initial requests, ten-tick refresh recovery and manual-refresh throttling. Forge needs a test EmbeddedChannel; NeoForge uses its supported `NetworkRegistry.configureMockConnection`. No production behavior changed. All four native suites pass (exit 0, 56s, `build/manifest-server-session-final-20260912-080244.log`), all artifact pairs pass (`build/manifest-server-session-artifacts.log`), and no project Java processes remain. These are native test transports, not real client/handshake/rendering evidence. Continue tracking/HUD, Brazier and complete Tome; consolidated connected acceptance remains open. No commit/publication performed.

## 0.0.2 in progress — Manifest advancement branch

[0138](behavior-changes/0138-manifest-advancements.md) restores `gemcutters_table -> manifest -> lectern`, retaining original inventory-acquisition criteria rather than inventing crafting-only triggers. Native NeoForge advancement decoding/parent checks pass; full matrix build/native suites pass (exit 0, 78s, `build/manifest-advancements-20260912-074743.log`) and all artifact pairs pass (`build/manifest-advancements-artifacts.log`). Connected awards/persistence remain unverified. Continue tracking/HUD, Brazier and complete Tome; this is not completion of all network/Tome advancements or 0.0.2. No commit/publication performed.

## 0.0.2 in progress — Manifest Lectern world route

[0137](behavior-changes/0137-manifest-lectern.md) adds the original two-high lectern, recipe/creative item/loot/axe tag, original OBJ/texture conversion and both-part server Manifest activation. It has no owner/entity and opens the interacting player's own listing. Preserve upstream air-only companion cleanup and ordinary piston reaction. Native tests pass for orientations/halves, ordinary single-item drops, blocked-placement payment conservation, activation dispatch and actual recipe/result-slot payment with oak/crimson planks. Full matrix build exits 0 in 88s (`build/manifest-lectern-final-20260912-074048.log`); all artifacts pass (`build/manifest-lectern-artifacts.log`) and 10 assets reproduce with `scripts/port_lectern_assets.py --check`. Actual connected opening/rendering, restart, pistons and interruption/cancellation acceptance remain open. Continue tracking/HUD, Brazier and complete Tome. Earlier lectern-missing statements are historical. No project Java processes remain; no commit/publication performed.

## 0.0.2 in progress — Manifest hotkey opening

[0136](behavior-changes/0136-manifest-hotkey-opening.md) adds the independent, unbound Manifest key, original `ManifestPresence=true` client preference and zero-field server opening request. Default presence checks only the main 36 inventory slots; explicit false intentionally allows the player's own read-only Manifest without the item, not access to other owners. Server opening refuses active containers, off-thread requests, spectators, sneaking and dead players. All four build/native/configuration suites pass (exit 0, 88s, `build/manifest-key-20260912-071714.log`); all artifact pairs pass (`build/manifest-key-artifacts.log`). Native tests cover main-slot presence/offhand exclusion and file preservation, NOT actual keyboard delivery or opening guards. Tracking/clear/under-cursor/HUD, lectern, Brazier, Tome and connected acceptance remain open. No project Java processes remain; no commit/publication performed.

## 0.0.2 in progress — Manifest distance and grid

[0135](behavior-changes/0135-manifest-range-and-grid.md) restores client-file distance/grid preferences and sends distance through a validated C2S request to the current server Manifest menu. Initial collection now waits for settings; subsequent manual/Hive refreshes retain them. Defaults are `ManifestMaxDistance=100` and `DisableManifestGrid=true`. Full matrix build/native/config/codec checks pass (exit 0, 84s, `build/manifest-range-20260912-065926.log`); all artifacts pass (`build/manifest-range-artifacts.log`). Large distance does not bypass Hive revocation. Initial connected opening/throttling, two-player settings and rendered grid acceptance remain unverified. Continue tracking/HUD/hotkey, remaining settings/controls, lectern, Brazier and Tome; older hard-coded-distance statements below are historical.

## 0.0.2 in progress — Manifest search integration

[0134](behavior-changes/0134-manifest-search-integration.md) connects the optional JEI runtime to the Manifest search field/toggle, Shift-click query copy and original conditional query restoration. `ManifestSearchTermPersistence` and `ManifestJeiSynchronise` in client.properties default false and preserve existing files. Seven production search-session regression cases pass on every leaf; Forge's cases are in the aggregate forge-runtime XML. Full build/native suites pass (exit 0, 85s, `build/manifest-search-20260912-064918.log`) and all artifact pairs pass (`build/manifest-search-artifacts.log`). Real JEI UI/reload acceptance is still deferred to the consolidated campaign. Continue tracking/hotkey/other settings, lectern, Brazier and complete Tome; the earlier JEI-search-missing statements below are historical. Preserve upstream's nonempty-query/enabled-on-close restoration conditions unless a change is approved.

## 0.0.2 in progress — craftable Manifest browsing route

[0133](behavior-changes/0133-manifest-browsing-route.md) adds the registered Manifest item, original Gem Cutter recipe/model/artwork, slotless native menu, bounded cross-loader S2C snapshot transport and browsing screen. Implemented controls include name/mod-name search, quantity/name ordering, scrolling, refresh and Shift location tooltips. Native tests prove exact acquisition/payment/extraction, item-data/long-count codec preservation, atomic fragmented assembly and rejected malformed/stale responses; this is not connected-client or visual evidence. Final four-leaf build/native suites pass (exit 0, 58s, `build/manifest-interface-final-20260912-063730.log`), all artifacts pass (`build/manifest-interface-artifacts.log`), and no task-owned game JVMs remain.

Continue Manifest tracking lines/HUD/slot highlighting, hotkey/sneak clearing, JEI search integration, settings and lectern, then Brazier/Tome. Distance currently uses upstream's default 100; configuration and final UI parity remain unfinished. Hive-audience changes refresh the menu without per-tick inventory scans, but actual connected revocation/delivery still needs acceptance. The release plan defers the full visual/multiplayer campaign until nonvisual implementation is complete. The earlier server-projection-only checkpoint below is historical. No commit, version bump or publication performed.

## 0.0.2 in progress — Manifest native inventory projection

[0132](behavior-changes/0132-manifest-inventory-aggregation.md) connects `StorageNetworks` to live Manifest inventory reads: owned/Hive contents, data-sensitive long totals, detached icons, original source descriptions and strict range groups. Approved 0115 deduplication passes actual native repeated-target/double-chest/distinct-barrel scenarios after a genuine RED. Final four-leaf build/native suites pass (exit 0, 56s, `build/manifest-contents-final-20260912-061142.log`); all artifacts pass and no game JVMs remain. This is server projection only, not a registered/playable Manifest. Continue the actual Manifest item/menu/client transport/search/sort/tracking and lectern, then Brazier and Tome; avoid presenting another pure helper as completed gameplay. Remaining per-source listing, chunk/dimension/restart and connected acceptance are explicit in 0132.

## 0.0.2 in progress — native network discovery

Follow [the release implementation plan](plans/0.0.2-implementation-plan.md). [0131](behavior-changes/0131-network-device-discovery.md) implements native loaded-device indexing and current personal/Hive visibility, reusing existing ownership and inventories. Native regressions cover actual placement, ownerless/fake-player exclusion, Hive sharing/personal mode, succession/revocation, detached-copy isolation, removal/replacement and reinstallation. Final four-target build/native suites pass (exit 0, 76s, `build/network-discovery-final-20260912-055749.log`); all artifact pairs and new Forge selector guards pass. No surviving task-owned game processes. This is not a usable Manifest/Brazier or completed 0.0.2.

Continue the integrated network milestone with Manifest/lectern listing and Brazier routing consumers; complete their upstream/master contract trace before coding. Do not expose every indexed device as an inventory or routing destination: the ordinary Radiant Crafting Table is a Manifest inventory upstream, not a Brazier destination. Discovery query results are snapshots, not lasting mutation authorization. Real chunk unload/dimension/process-restart and connected acceptance remain open. The user explicitly deferred the potential Amphora copied-item issue; do not block remaining work on it or re-ask that decision. No commit/push/publication authorization is implied.

## 0.0.1 release and publishing

Current version is `0.0.1`; see [release notes](releases/0.0.1.md) and [publishing instructions](PUBLISHING.md). The user requested reuse of UnlimitedPeripheralWorks/modding-buildenv publishing. Direct buildenv application conflicts with the pinned Loom classpath, so `release.gradle.kts` adapts the same GitHub Release, CurseForgeGradle and Minotaur plugins. The Python upload transport is removed; a read-only preflight/checksum helper retains immutable GitHub release guards. All four build/native suites and artifact pairs pass (`build/release-0.0.1-verify.log`, exit 0, 80s), as do cold configuration, GitHub/Modrinth dry runs and read-only four-leaf publishing metadata assertions. CurseForge debug verification is blocked by HTTP 400 from game-version metadata; CurseForge/Modrinth project IDs and credentials remain operator inputs. No publication on those platforms is claimed. The RC section below is historical.

## RC preparation — 0.0.1-rc.1

The user requested a public GitHub testing prerelease and authorized committing the full migration tree. Destination: `SirEdvin/ArcaneArchivesRecrafted`. Version is `0.0.1-rc.1`; [release notes](releases/0.0.1-rc.1.md) document dependencies and incomplete features. `publishGithub` is a Gradle task gated by all four leaf builds/tests and the artifact verifier, with clean-tree/pushed-tag checks and immutable-release publication. Runtime files/logs remain ignored. RC verification: `build/rc-verify-20260910-161647.log`, exit 0, 59s; Fabric suites 204 each, Forge runtime 199 (standalone 36 overlaps), NeoForge 279; no failures/errors/skips. This is not full migration or multiplayer acceptance.

The Tome adapter checkpoint below predates two repairs: native Patchouli dispatch requires `patchouli:custom` (covered by a genuine regression RED/GREEN), and scaled item hover now uses page coordinates. The post-fix Fabric live probe visibly showed Diamond input and Paper output tooltips on opposite pages. Its timeout does not establish graceful shutdown/save; isolated fixture archival remains pending. Complete Tome registration/content/acquisition is still unfinished.

## Tome grant decision — preserve original behavior

The next selected feature is the complete Tome milestone, not a partial book. Receipt/config prerequisites exist but the Tome and automatic grant callers do not. The user does not consider the missed automatic grant worth changing because crafting provides a fallback; [0111](behavior-changes/0111-tome-grant-failure-safety-proposal.md) is not adopted and no longer blocks implementation. Preserve original grant ordering and restore the original shapeless book + gold-nugget recipe, including modern tag equivalence. No production code changed by this decision; overall migration remains incomplete and Radiant Furnace remains excluded.

## Latest checkpoint — Tome Gem Cutter recipe-display adapter

[0112](behavior-changes/0112-tome-gem-cutter-recipe-display.md) supplies an API-only Patchouli component, output/key templates and original recipe texture. Native ordered lookups preserve counted detached alternatives/output and do not change crafting authority or the catalog's distinct data-sensitive lookup. Six shared cases pass on all targets; two real Patchouli variable/template cases pass per Fabric version. Four server/four client startups and full builds/artifacts in both active states pass; canonical bytes restored. Final build exits 0 in 27s. Complete Tome XML conversion, registered item/recipe/acquisition, live page rendering and connected reload remain pending. No new user-input blocker; do not reopen the rejected grant failsafe or restore Radiant Furnace.

## Previous checkpoint — Mixin metadata repair

[Mixin metadata](migration/MIXIN_METADATA.md) now declares the oldest pinned runtime baseline (0.8.5) in all three production configurations. Packaged-resource checks and both startup harnesses enforce it. Genuine artifact RED precedes the fix; four server/four client startups pass with the diagnostic gone, and the exact guards reject the historical Forge log. Full builds/artifacts pass in canonical and alternate Forge states; four known comment-drift files were restored and all 668 snapshot files compared. Final canonical build exits 0 in 30s. No gameplay semantics changed. The chest checkpoint's Mixin diagnostic below is now resolved; broader migration and player/multiplayer acceptance remain incomplete.

## Previous checkpoint — chest native storage acceptance and furnace exclusion

The user explicitly excluded the whole Radiant Furnace feature; [0110](behavior-changes/0110-radiant-furnace-exclusion.md) supersedes restoration proposal 0109. Do not reintroduce it. Independent Echo item/tint support remains in scope.

[Chest native acceptance](migration/CHEST_NATIVE_ACCEPTANCE.md) now covers actual vanilla hopper insertion/extraction, extended-count/custom-data persistence across separate server processes, metadata restart and exact destruction/drop conservation on all four leaves. Eight server sessions and three fixture-contract tests pass; full build (27s) and all artifact pairs pass. The initial failed drop assertion was a corrected vanilla-container replacement mistake in the test harness, not a production chest defect. No production behavior changed. Fixture cleanup passed and no project game JVM remains. Forge still logs its existing missing Mixin `minVersion` diagnostic; full interaction/rendering/multiplayer acceptance and the overall migration remain incomplete. See the current migration queue rather than treating the older checkpoints below as current blockers.

## Current verification checkpoint — Forge loader-run assertions

The user approved the test-tooling repair. [Forge runtime verification](migration/FORGE_RUNTIME_TESTS.md) now runs the unchanged shared JUnit fixtures inside native Forge GameTest startup, with individual XML results and required-failure propagation into `build`. All 178 active Forge fixtures pass, including 23 Gem Cutter crafting cases; the standalone 36-case subset remains and must not be double-counted. Four-target full builds and artifact isolation checks pass with both Fabric and Forge active. A deliberately failing temporary assertion failed the report/GameTest/Gradle as expected and was removed. Canonical source state was restored; no gameplay logic changed. Missing gameplay and release acceptance remain open, but plain-JUnit Forge initialization is no longer a blocker. Reasonable documented gameplay validation, including 0103 cached Brazier eligibility, is authorized by the renewed completion request.

## Current implementation checkpoint — Trove creative interaction

[0073](behavior-changes/0073-trove-creative-withdrawal.md) restores creative non-scepter attacks as withdrawal without removal, and original scepter mining speed. Native Forge/NeoForge removal overrides retain their break authorization; ordered Fabric callbacks prevent local destruction and route server-side withdrawal after default-phase break protections. Four-target assembly passed (`build/trove-creative-20260909-164945.log`, exit 0, 21s); all artifact pairs and whitespace checks passed. No connected-player runtime acceptance or parent completion claim. Sneak-scepter spill, item-form storage integration, networking and broader migration remain open.

## Current implementation checkpoint — shared GUI preference and gem state

[0072](behavior-changes/0072-shared-gui-and-gem-state.md) restores client-local `UsePrettyGUIs` (default true) on existing screens, original simple artwork/socket geometry, and source-backed upgrade/unlimited tooltip/model semantics. Four-target assembly and artifact verification passed; config tests pass on all targets, gem-state tests on loader-aware NeoForge. Log: `build/shared-gui-state-final-20260909-164113.log`, exit 0, 20s. This is not full-suite or visual/gameplay acceptance. The user explicitly reconfirmed reachable-content scope and approved corrections. The vanished temporary reference was recovered at `/tmp/arcane-archives-reference/migration-source` (release pin); the same clone also contains the master pin. Source audits must check both: master registers additional content absent/commented out in release. No source-scope reduction is authorized.

## Current implementation checkpoint — native gem HUD

[0071](behavior-changes/0071-gem-hud.md) restores original crosshair-adjacent held/socket charge bars, cut silhouettes and toggle indicators using the pinned GUI art and native rendering events. Shared available-gem selection includes worn sockets without duplication. Four-target assembly passed (`build/gem-hud-final-20260909-144424.log`, exit 0, 20s); runtime/client registration and byte-identical art inspected in all production JARs. Upgrades, shared GUI preferences, broader features and deferred visual/gameplay acceptance remain open. No gameplay tests or optimization; not playtest-ready.

## Previous implementation checkpoint — Gem Socket opening key

[0070](behavior-changes/0070-gem-socket-key.md) restores the original Arsenal-gated, unbound-by-default Activate Gem Socket control. It opens the existing menu for held/equipped/inventory sockets via validated native serverbound networking; recharge remains a menu button. Four-target assembly passed (`build/socket-key-20260909-142942.log`, exit 0, 22s); key/packet/resource packaging inspected. Standalone socket-opening access is now implemented, superseding the gap below. HUD/upgrades, shared presentation, broader migration and runtime acceptance remain open. No gameplay tests or optimization; not playtest-ready.

## Previous implementation checkpoint — dedicated worn Gem Socket

[0069](behavior-changes/0069-wearable-body-slot-mapping-proposal.md): user selected dedicated mod-specific slots on BOTH APIs, superseding the body-slot proposal. Implemented custom slot/player definitions and validators, optional native API adapters, single worn-gem activation, live charge persistence, open-menu precedence and client potion prediction. Four-target assembly passed (`build/worn-socket-final-20260909-141403.log`, exit 0, 20s); packaged adapters/resources and dependency hashes inspected. No mapping approval remains pending. Standalone worn-menu access controls, HUD/upgrades, shared presentation, broader features and deferred runtime acceptance remain open. No gameplay tests or optimization; not playtest-ready.

## Previous implementation checkpoint — Gem Socket native runtime

[0068](behavior-changes/0068-gem-socket-runtime.md) connects persistent single-gem storage, original offhand/menu layout and shift priorities, native recharge-button networking, original acquisition/art and active socket effects. Main-hand/offhand/open-socket ordering is shared across migrated event consumers; passive timers/healing/rain use one ordered server tick path. Socket references persist charge/state changes without copied-item payment or duplicate close returns. Four-target assembly passed (`build/gemsocket-complete-20260909-122404.log`, exit 0, 21s); production runtime/caller/resource packaging inspected. Optional worn sockets, shared simple-GUI preference, HUD/upgrades, broader migration and runtime acceptance remain open. No gameplay tests or optimization; not playtest-ready.

## Previous implementation checkpoint — chromatic powder recharge

[0067](behavior-changes/0067-chromatic-powder-recharge.md) implements registered color/full-spectrum powders, original animated art/names, color-first/rainbow-fallback payment, Mindspindle/Elixirspindle explicit fallback and shared depletion recharge across migrated held gems. Original exclusion and full-charge consumption quirks retained. All four targets assembled (`build/chromatic-powder-final-20260909-120801.log`, exit 0, 20s), static packaging inspected. Gem Socket storage/menu/recharge button and wearable availability remain open, alongside HUD/upgrades and broader migration. Source correction: upstream recharge control is the socket GUI button, not a keyboard binding. No gameplay tests or optimization; not playtest-ready.

## Previous implementation checkpoint — automatic gem material recharge

[0066](behavior-changes/0066-gem-material-auto-recharge.md) restores automatic inventory-material recharge on depletion for Slaughtergleam, Munchstone, Mindspindle, Elixirspindle and Phoenixway, including fire protection. Explicit recharge shares the same payment rules. This corrects the earlier unconditional claim that Slaughtergleam's final charge cannot affect the current loot: automatic nugget recharge can restore it before the query. Four-target assembly passed (`build/gem-material-recharge-20260909-115948.log`, exit 0, 20s), packaged runtime/caller references inspected. Powder fallback, recharge key, sockets/wearables, HUD/upgrades and broader migration remain open. No gameplay tests or optimization; not playtest-ready.

## Previous implementation checkpoint — Slaughtergleam handheld loot/recharge

Equipment follow-through: the missing 1.21 native equipment-drop bonus is now connected, retaining native zero-chance/vanishing-style protections, eligibility and guaranteed-drop classification. Four targets assembled (`build/slaughtergleam-equipment-20260909-114955.log`, exit 0, 19s); modern injection targets packaged/remapped. This supersedes the equipment-gap statement in the earlier checkpoint below. Shared gem systems and custom loot/death compatibility remain open; no gameplay testing or optimization, not playtest-ready.

[0065](behavior-changes/0065-slaughtergleam-handheld-runtime.md) implements native table-loot bonuses, pre-loot death payment, empty-gem gold-nugget recharge and original acquisition/animated/accessibility art. Original ignored-toggle and final-charge quirks preserved. Four-target assembly passed (`build/slaughtergleam-final-20260909-114656.log`, exit 0, 18s), packaging and Fabric loot/death-hook remapping inspected. Shared gem systems, 1.21 equipment-drop effects, custom loot/death-hook compatibility remain open. Supersedes earlier research-only Slaughtergleam status. No gameplay tests, optimization, clients/servers, commits or publication. Migration incomplete; not playtest-ready.

## Previous implementation checkpoint — Murdergleam handheld combat

[0064](behavior-changes/0064-murdergleam-handheld-runtime.md) implements forced critical hits, original per-gem charge/toggle/recharge behavior, acquisition and animated/accessibility art. Four-target assembly passed (`build/murdergleam-integration-20260909-113211.log`, exit 0, 21s); packaging and Fabric attack-target remapping inspected. Shared gem systems and combat-mod/event compatibility remain open. Slaughtergleam was researched but not implemented. No gameplay tests, optimization, clients/servers, commits or publication. Migration incomplete; not playtest-ready.

## Previous implementation checkpoint — Mountaintear approved dropped lifecycle

[0063](behavior-changes/0063-mountaintear-dropped-item-proposal.md) explicitly approved and implemented: native fire protection, full lava recharge, same entity/metadata, normal ticking and recharge-only sound. All four targets assembled (`build/mountaintear-lifecycle-20260909-112734.log`, exit 0, 24s), loader hooks packaged. Supersedes earlier warnings that dropped recharge/protection is missing; gameplay acceptance remains deferred. Lightwell/shared systems and broader migration remain incomplete. No approval blocker, no gameplay tests or optimization; not yet playtest-ready.

## Previous implementation checkpoint — Mountaintear approved ranged runtime

[Mountaintear 0062](behavior-changes/0062-mountaintear-placement-safety-proposal.md) destination protection and success-only charge payment explicitly approved and implemented, preserving range and creative exemption; native item, original recipe/art/localization connected. Four-target assembly passed (`build/mountaintear-approved-20260909-091735.log`, exit 0, 21s), packaging inspected. Next: dropped lava recharge/fire-resistant entity; Lightwell and shared gem systems also remain open. Do not throw the current item into lava expecting protection/recharge. No gameplay tests or optimization. Migration incomplete; not playtest-ready.

## Previous implementation checkpoint — Stormway handheld runtime

[Stormway](behavior-changes/0061-stormway-handheld-runtime.md) now has targeted lightning, held/dropped rain recharge, toggled mob-projectile retaliation and lightning-strike buffs/cooldown, plus original recipe and animated/accessibility art. Four-target assembly passed (`build/stormway-integration-20260909-085558.log`, exit 0, 20s); production packaging and Fabric lightning-target remapping checked. Shared gem systems and cross-loader/modded event compatibility remain open. No gameplay tests, optimization, clients/servers, commits or publication. Migration remains incomplete and not ready for first playtest.

## Previous implementation checkpoint — Phoenixway automatic fire protection

[Held-gem protection](behavior-changes/0060-phoenixway-fire-protection.md) now grants the original resistance duration, cancels burning/fire-block damage and preserves empty-gem activation/clamped payment. Native hooks connected across four targets; assembly passed (`build/phoenixway-protection-20260909-084702.log`, exit 0, 18s), class packaging checked. Remaining: tracking-client-local activation/recharge sound, shared gem systems and cross-loader invulnerability/custom-hook compatibility. No gameplay tests/optimization/clients/commits/publication. Migration incomplete; not ready for first playtest.

## Previous implementation checkpoint — Phoenixway ranged use/recharge

[Phoenixway](behavior-changes/0059-phoenixway-ranged-runtime.md) now has native ranged fire placement, original charge/gunpowder recharge rules and acquisition/animated trillion artwork. Four-target assembly passed (`build/phoenixway-ranged-20260909-084406.log`, exit 0, 21s), runtime/resources packaged. Next missing Phoenixway work: original automatic fire resistance and tracking-client-local fire-charge sound; shared gem systems remain open. No gameplay tests/optimization/clients/commits/publication. Migration incomplete and not ready for first playtest.

## Previous implementation checkpoint — Elixirspindle approved handheld runtime

[Elixirspindle](behavior-changes/0058-elixirspindle-item-use-proposal.md): approved potion-only client interception, native reusable potion effects/charge, nether-wart recharge/message, original opt-in acquisition and animated pampel art. Four-target assembly passed (`build/elixirspindle-approved-20260909-083855.log`, exit 0, 20s); packaging/Fabric remapping inspected. Approval blocker resolved. Shared gem integrations, unusual Finish/late-gem-change compatibility and runtime acceptance remain open. No gameplay tests or optimization. Broader migration incomplete; not ready for first playtest.

## Previous implementation checkpoint — Switchgleam Enderman suppression/recharge

[Enderman recharge](behavior-changes/0057-switchgleam-enderman-recharge.md) adds native Forge/NeoForge events and a matching Fabric hook: every held Switchgleam in the original nearby-player cube gains three charge and cancels the attempt, without toggle/charge gates. All four targets assembled (`build/switchgleam-enderman-20260909-082558.log`, exit 0, 18s); packaging/remapped Fabric references inspected. Broader teleport-event coverage, shared gem systems and runtime acceptance remain open. No gameplay tests/optimization/clients/commits/publication. Migration incomplete; not ready for first playtest.

## Previous implementation checkpoint — Switchgleam approved handheld swap

[Switchgleam handheld swap](behavior-changes/0056-switchgleam-safe-swap-proposal.md) implements the explicitly approved two-destination clearance checks, server-authoritative/native player movement, original free use/target order/yaw increment and original acquisition/animated art. Four-target assembly passed (`build/switchgleam-approved-20260909-082109.log`, exit 0, 20s); class/recipe/model/animation packaged. Approval blocker resolved. Enderman suppression/recharge, shared gem systems and runtime acceptance remain open. No gameplay tests/optimization/clients/commits/publication. Broader migration is incomplete; not ready for first playtest.

## Previous implementation checkpoint — Munchstone handheld runtime

[Munchstone runtime](behavior-changes/0055-munchstone-handheld-runtime.md) connects configured plant-block consumption/food restoration, original cost quirks, empty-gem bonemeal recharge, original acquisition and animated oval art. Editable startup configuration preserves optional IDs and expands vanilla metadata families. All four targets assembled (`build/munchstone-integration-20260909-071120.log`, exit 0, 21s); runtime/config/defaults/recipe/model/animation packaging inspected. Shared gem systems, exact sound presentation, optional compatibility and broader migration remain open. No gameplay tests/optimization/clients/commits/publication; not ready for first playtest. Switchgleam was inspected only.

## Previous implementation checkpoint — Cleansegleam; milk recharge approved

[Cleansegleam runtime](behavior-changes/0054-cleansegleam-recharge-proposal.md) implements handheld hunger/nausea/poison cleansing, nearby cleansing while sneaking, original charge costs and artwork/acquisition. The user approved and the implementation applies single-milk full recharge with conserved empty bucket and consumption message. Four-target assembly passed (`build/cleansegleam-approved-20260909-064824.log`, exit 0, 21s); class/recipe/model/animation packaged on each target. Approval blocker resolved. Shared gem systems, optional integrations and broader migration remain unfinished. No gameplay tests/optimization/clients/commits/publication; not ready for first playtest.

## Previous implementation checkpoint — Orderstone; client sync approved

[Orderstone runtime](behavior-changes/0053-orderstone-runtime.md) implements original block conversions/costs, anvil repair/orientation, XP recharge with original hand ordering, recipe and animated oval art. The user explicitly approved client synchronization; UPDATE_CLIENTS is applied without adding direct neighbor notifications or changing costs. Approved-state assembly passed (`build/orderstone-approved-20260909-062923.log`, exit 0, 19s). The earlier clarification blocker is resolved. Shared gem integrations and broader migration remain unfinished; no gameplay tests/optimization/clients/commits/publication. Not ready for playtesting.

## Previous implementation checkpoint — Mindspindle XP and book recharge

[Mindspindle handheld runtime](behavior-changes/0052-mindspindle-handheld-runtime.md) connects per-pickup XP amplification before native mending, either-hand charge accounting, empty-gem book recharge/message/sound, and original opt-in acquisition/animated pampel artwork. A shared mixin restores the remaining units of modern merged XP orbs rather than compounding their value. Forge now has documented annotation-processor/refmap/manifest wiring; Fabric/NeoForge use their native mixin declarations. Four-target final assembly passed (`build/mindspindle-final-20260909-060622.log`, exit 0, 20s); production classes/resources/remapping were inspected. Shared powder/socket/HUD/upgrade integration and broader feature migration remain open. No gameplay tests, clients, optimization, commits or publication. Not ready for playtesting.

## Previous implementation checkpoint — Salvegleam healing/recharge and gem toggle

[Salvegleam handheld runtime](behavior-changes/0051-salvegleam-handheld-runtime.md) connects saved healing pulses in either hand, animal-kill recharge/automatic reactivation, authoritative left-click-air toggle networking and original opt-in acquisition/animated artwork. All four targets assembled (`build/salvegleam-integration-20260909-055451.log`, exit 0, 20s); runtime/event/packet classes, recipe/model/animation packaging inspected. Shared sockets, powder controls, HUD, remaining upgrades/unlimited charge and wider migration remain open. No gameplay tests, optimization, clients, commits or publication. Not ready for playtesting.

Source correction: pinned PacketArcaneGems.GemParticle does nothing for pendeloque gems. Earlier Rivertear/Parchtear beam-gap statements are superseded; there is no implemented beam at the source pin to migrate.

## Previous implementation checkpoint — Agegleam handheld runtime

[Agegleam handheld runtime](behavior-changes/0050-agegleam-handheld-runtime.md) connects nearby baby growth, original charge accounting and held-only timed recharge, plus opt-in acquisition and original asscher animated/depleted/accessibility art. The pinned handler's zero-charge activation and unclamped age addition are preserved; the broader tooltip is not an adult breeding-cooldown implementation. Four-target assembly passed (`build/agegleam-integration-20260909-054820.log`, exit 0, 19s); class/gated recipe/model/animation packaging was inspected. Shared sockets, powder controls, HUD, remaining upgrade/unlimited-charge semantics and other feature families remain open. No gameplay tests, optimization, clients, commits or publication. Migration is not ready for playtesting.

## Previous implementation checkpoint — Rivertear handheld runtime

[Rivertear handheld runtime](behavior-changes/0049-rivertear-handheld-runtime.md) connects long-distance source-water placement, native charge costs, dropped-in-water recharge/sound, opt-in acquisition and original animated/depleted/accessibility art. Forge-family hooks and a Fabric item-tick mixin share authoritative recharge logic. Four-target assembly passed (`build/rivertear-integration-20260909-053824.log`, exit 0, 20s); runtime/recipe/model/animation packaging and Fabric remapped mixin annotations were inspected. Lightwell, optional-mod integrations, beam particles and shared gem systems remain open. No gameplay tests/optimization, clients, commits or publication. Migration is not ready for playtesting.

## Previous implementation checkpoint — Parchtear; representation approved

[Parchtear runtime](behavior-changes/0048-parchtear-runtime.md) includes native clearing/charge, temporary block/entity, Charm recharge, original assets and opt-in acquisition. The user approved the invisible/passable/replaceable non-air representation; `.air()` is removed without changing the original 200-tick lifetime. Four-target assembly passed (`build/parchtear-approved-20260909-053302.log`, exit 0, 20s), and runtime/model/animation packaging was inspected on all four targets. Class expectations are updated; the full verifier and gameplay acceptance were not run. The approval blocker is resolved. Shared powder/socket/HUD integrations, broader gem upgrades, item-form storage and networks remain open. No tests/optimization, clients, commits or publication. Migration is not ready for playtesting.

## Previous implementation checkpoint — Radiant Key / Trove LOCK

[Radiant Key and Trove locking](behavior-changes/0047-radiant-key-trove-lock.md) now includes original acquisition/animated artwork, optional upgrade installation/removal, persistent exact-data reference selection, empty-Trove filtering, LOCK+VOID interaction and Revelation reporting. Fabric snapshots the auxiliary identity alongside native quantity transactions. Four-target final assembly passed: `build/radiant-key-final-20260908-204048.log`, exit 0, 14s; classes and animation metadata are packaged. No tests, clients, optimization, commits or publication. Item-form/network integration, Parchtear and other feature families remain open; the old tooltip's trapped-chest claim has no implementation identified at the pin. Migration is not ready for the first playtest.

## Previous checkpoint — storage VOID upgrades

[Placed-storage VOID upgrades](behavior-changes/0046-storage-void-upgrades.md) now connect Charm installation/removal, the original optional GUI row, saved/packed upgrades and native overflow behavior. Troves void matching overflow across capacity; Tanks void matching input only when already full, as upstream. The original two-slot Trove automation surface is restored for native hopper admission. Four-target compile/assembly passed: `build/storage-void-upgrades-20260908-202717.log`, exit 0, 21s; new classes are packaged in all four JARs. No tests, clients, optimization, commits or publication. Parchtear, keys/LOCK, item-form storage APIs and broader networks remain unfinished. Migration is not ready for the first playtest.

## Previous checkpoint — Devouring Charm handheld workflow

[Devouring Charm runtime](behavior-changes/0045-devouring-charm-runtime.md) connects six temporary disposal slots, fluid draining/container returns, six persistent pickup filters, native pickup handling, original four-item acquisition and animated artwork/GUI. Tank/Trove optional-upgrade use and Parchtear integration remain open. Latest four-target assembly: `build/devouring-charm-final-20260908-200205.log`, exit 0, 19s; `git diff --check` passed. Production Fabric mixin annotations and Java compatibility levels were inspected, not runtime-tested. No tests, clients, optimization, commits or publication. Migration is incomplete and not ready for the first playtest.

## Previous checkpoint — Radiant Crafting Table

[Radiant Crafting Table runtime](behavior-changes/0044-radiant-crafting-table-runtime.md) now includes shared persistent crafting slots, ordinary native crafting/remainders, three saved-recipe actions with paid matrix/player-inventory crafting, original assets/GUI, acquisition and raw-quartz workbench conversion. `InWorldChestConversion` is restored for both native conversion paths. Manifest/network integration, full presentation and custom-recipe shortcut compatibility remain open. Latest four-target compile/assembly: `build/crafting-table-final-20260908-193943.log`, exit 0, 19s; `git diff --check` passed. No tests, clients, optimization, commits or publication. Artifact expectations were extended but the full verifier was not run. Migration remains incomplete and not ready for its first playtest.

## Earlier implementation checkpoints — core storage and progression

The migration remains incomplete and is not ready for the first playtest. The newest user direction is to finish all feature implementation before optimization or test campaigns; do not resume Gem Cutter micro-refinement or screenshot detours. Compilation remains a basic integration check, not gameplay acceptance.

Added [Radiant Resonator runtime](behavior-changes/0034-resonator-runtime.md) and [Radiant Chest runtime](behavior-changes/0035-radiant-chest-runtime.md): native registrations, recipes/conversion, original assets, menus/count synchronization, persistence and item automation. Natural quartz generation is wired; a sneak-used raw quartz now converts a native chest, including double-chest inventory, with only rejected remainders dropped. Chest scepter/display/routing features, full network integration and all other missing gameplay remain open. Neither parent feature has gameplay acceptance.

The user suggested Broccolium for cross-platform storage, with native fallback if unsuitable. Source evaluation at Minecraft-Modding-Libs `47a80ca` found specific capacity/transaction/remainder mismatches; [assessment](migration/BROCCOLIUM.md). Native adapters are used for current chest storage. The library checkout was not modified and no new dependency added. Later non-slotted/fluid callers should be assessed on their actual needs, not automatically excluded.

`./gradlew assemble --no-daemon` under the required timeout/log wrapper passed all four compile/assembly targets: exit 0, 29 seconds, `build/storage-integration-20260908-165131.log`. No unit tests, gameplay smoke tests, client launches, optimization, commits or publication in this continuation. Artifact class expectations were updated but the full checker has not yet been rerun. Preserve all uncommitted files.

The preceding approval timeout was renewed by the user. Native chest lookup was resolved from the local Broccolium source; no external mapping lookup is still required.

Trove/Tank now have runtime implementations: [Trove storage](behavior-changes/0036-trove-native-storage.md), [Tank fluid storage](behavior-changes/0037-tank-native-fluid-storage.md), [portable removal](behavior-changes/0038-portable-storage-removal.md) and [Tank fluid surface](behavior-changes/0039-tank-fluid-surface.md). Both original four-item acquisition recipes, geometry/transforms/translations, capacity formulas, ordered upgrades, native automation, player transfers, comparator and saved contents are connected. Material Interface/Containment Field and shaped-storage installation are wired, including sneak item use; rejected shaped-block installation does not place a block next to the device. Native fluid world rendering is registered on every target. Original source/assets were read from release `bb99accf48ed583e29b0efae56e28c963407b8df`, not assumed from master.

Latest exact code compiles/assembles across all four targets: `build/amphora-final-20260908-191503.log`, exit 0, 16s, `./gradlew assemble --no-daemon`. All four compile/assemble leaves were confirmed in the log; `git diff --check` passed. No unit/gameplay tests, clients, optimization, commits or publication. Artifact verifier class expectations were extended but the verifier has not been rerun. No checkbox or playtest readiness claim is earned by compilation.

[Radiant Amphora runtime](behavior-changes/0042-radiant-amphora-runtime.md) now connects original acquisition costs, persistent Tank linking, native mode packets/attack controls, world pickup/placement, direct Tank-to-Tank interaction, dispenser use and original base-art states. A [real copied-item hazard](behavior-changes/0043-amphora-copied-item-boundary.md) prevents safely registering a direct remote item-fluid proxy: Forge simulation executes writes on copies, and Fabric detached recipe preparation can commit a remote drain. No generic item-fluid binding is registered on any target. It remains required unfinished work, alongside dynamic fluid masks, remote fluid tooltip information and other Amphora acceptance. Do not reintroduce the unsafe proxy or claim the Amphora parent feature complete.

Capacity installation/removal is reachable through the [native capacity menu](behavior-changes/0040-capacity-upgrade-menu.md): empty-handed sneak-use or the newly craftable Manipulation scepter on Troves/Tanks. [Storage scepter integration](behavior-changes/0041-storage-scepter-interactions.md) adds both Revelation/Manipulation acquisition recipes and original artwork. Revelation reports Chest name/free slots, Trove contents/capacity/upgrades, Tank fluid/capacity and Resonator progress/status. Native block dispatch prevents these supported tool interactions from becoming normal deposits/container opens; sneak item use is also connected.

Optional upgrades/combined UI, Chest manipulation, unported device reports and other scepters remain unfinished, alongside native item-form capabilities, Tank container returns/item rendering, Trove creative/spill paths, network settings, crafting storage, Amphora, Manifest/Brazier, tools/charms/gems, tome and integrations. Do not resume helper hardening or validation campaigns; continue actual feature implementation. No user-input blocker has arisen. The migration and core-storage group remain unfinished.

## Earlier checkpoints — historical scope only

Current integrated checkpoint: [Conserved Gem Cutter fluid returns](behavior-changes/0032-gem-cutter-fluid-returns.md) connects native single-container fluid draining to guarded table/player payment and output delivery. Returns fill table inputs before player inventory; insufficient combined capacity cancels rather than dropping surplus. Water/lava native drain and paid routing execute on NeoForge; Fabric unit fixtures inject explicitly limited policies, not native Transfer API execution. Latest matrix: 171 tests per Fabric leaf, 182 NeoForge, Forge compilation only, four artifact pairs passing; `build/fluid-returns-final-20260908-134617.log` (exit 0, 25s), `build/fluid-returns-verification.json`. No new client/server startup or connected-player acceptance. [0031](behavior-changes/0031-native-gem-cutter-recipes.md) already connects seven native data recipes and live reload, now with optional server-supplied creator attribution; ordinary tools are consumed as upstream ingredients. Conditional/data-sensitive recipes, scripting, flint/generalized fluid returns, surplus drops, natural quartz/resonator production, optional stabilization, Radiant storage/conversion/upgrades and multiplayer/restart acceptance remain unfinished. Prioritize missing reachable gameplay, not table-helper hardening. No user-input blocker; migration is not complete.

Latest execution correction: the user challenged the time spent on the cutting table. Do not return to speculative foundation hardening or helper-by-helper milestone reports. Continue source-driven missing gameplay, grouping implementation and verification into usable outcomes. Material Interface/Containment Field upgrade installation and sneak-use bypass remain with the Tank/Trove work, not silently removed.

Latest user correction: complete all non-visual migration implementation before further vision validation. No more screenshot/client walkthrough detours during implementation. The Fabric test client saved/stopped; the copied NeoForge menu fixture was not launched. Retain existing visual evidence as historical only; small-window layout clipping and other presentation acceptance remain open for the later validation phase. Continue headless code/data/testing work, not another presentation checkpoint.

Source correction from the user: inspect upstream release branches, not master alone, before declaring missing assets or defining feature behavior. Gem Cutter source/assets are pinned additionally to `release/0.2.0.25-mixins8`, commit `bb99accf48ed583e29b0efae56e28c963407b8df`; see `migration/gem-cutter-upstream.json`. Local reference: `/tmp/arcane-archives-reference/release-0.2.0.25-mixins8/`. No user-supplied JAR or substitute geometry is needed. `port_gem_cutter_assets.py` reproduces the original-geometry/PNG conversion with modern diorite mapping; the original GUI is now connected to input slots. The acquisition recipe and raw ingredient are now implemented under 0029; natural production still requires the Resonator/growth port. The latest release recipe trace confirms inlay costs six Radiant Dust, twelve redstone dust, one gold ingot and six gold nuggets; do not invent substitute costs. Inlay and both storage components now have paid combined-input recipes; raw-quartz acquisition and the complete progression chain remain unimplemented.

Release code trace: `inventory/ContainerGemCuttersTable.java:52` combines table and player inventories; lines 59/74 use that combined inventory for consumption and matching/conditions. Line 206 forces output click dragType to zero (absent from the prior master container). `GCTRecipe.java` and `GemCuttersTableTileEntity.java` have no diff from the prior pinned master for this release; the block changes use `FACING` directly. Do not infer complete runtime parity from the input-only owner or silently omit player-inventory ingredients. Continue tracing actual registered recipes, callbacks, packet handling and GUI before their implementation. Existing master-based audits/tests remain scoped historical evidence, not automatically release-validated behavior.

Execution priority: the user requested compressed tasks and less fragmentation/overengineering. Follow the consolidated active queue in `MIGRATION_TASKS.md`, starting with a playable Gem Cutter end to end. Historical helper reports below are evidence, not the next-action queue. Do not resume standalone snapshot/pagination/helper polishing; add supporting changes only for the active runtime integration or demonstrated defects. Batch full-matrix verification and documentation at meaningful integrated checkpoints, while preserving safety and properly documenting distinct behavior changes. No gameplay milestone is newly complete by this planning change.

## Historical checkpoints — superseded by the current integration summary above

`GemCutterCraftingState.inputSnapshot()` exposes ordered detached input slots for preview consumers without mutable handler access or pending-output inclusion. See [0021](behavior-changes/0021-joint-crafting-state.md). 137 tests pass per Fabric/NeoForge leaf; Forge compilation only; four artifact pairs pass. Matrix exit 0, 29s, `build/input-snapshot-20260908-072152.log`. Runtime preview/menu wiring, crafting and delivery remain unfinished.

Latest verification: catalog page reads/navigation during both guarded condition checks do not invalidate a paid craft; attempted page-list edits are rejected and preview edits stay detached. Existing production code passes unchanged. See [0023](behavior-changes/0023-recipe-page-boundaries.md). 136 tests pass per Fabric/NeoForge leaf; Forge compilation only; four artifact pairs pass. Matrix exit 0, 24s, `build/page-commit-20260908-071702.log`. Actual menus/runtime crafting and delivery remain unfinished.

Latest catalog presentation slice: seven-recipe immutable pages and next/previous wrapping are implemented in `GCTRecipeList`. Empty catalogs and invalid/stale navigation use page zero under [0023](behavior-changes/0023-recipe-page-boundaries.md). Two tests cover sizes 0–22, integer extremes and snapshot stability through replacement/removal. 135 tests pass per Fabric/NeoForge leaf; Forge compilation only; all four artifact pairs pass. Matrix exit 0, 30s, `build/recipe-pages-20260908-071344.log`. Menu/network/runtime crafting and delivery remain unfinished; page indices never authorize crafting.

Latest routing inspection slice: `inputSlots`/`countEmptyInputs` expose live owned-input information, mirroring upstream's direct slot scan rather than duplicating cached counts. Simulation, mutation, craft consumption and NBT restoration are covered; pending output is not an input slot. See [0021](behavior-changes/0021-joint-crafting-state.md). 133 tests pass per Fabric/NeoForge leaf, Forge compilation only; all artifact pairs pass. Matrix exit 0, 30s, `build/live-input-counts-20260908-070823.log`. Runtime integration and delivery remain unfinished.

Latest production slice: `GemCutterCraftingState.acceptRoutingInput` preserves upstream Brazier matching-occupied-slots-only acceptance, distinct from general stacked insertion. Native limits, detached surplus, simulation and callback guard remain intact; pending results survive routing and NBT reload. See [0021](behavior-changes/0021-joint-crafting-state.md). 132 tests pass per Fabric/NeoForge leaf; Forge compilation only; all four artifact pairs pass. Matrix exit 0, 30s, `build/routing-inputs-20260908-070507.log`. Runtime Brazier/device wiring, access checks, dirty marking and output delivery remain unfinished.

Latest persistence regression: a staged paid craft retains its original creator-output and consumed-input record after whole-catalog replacement, subsequent removal, and disk-NBT reload. Pending still blocks recrafting. Existing production code passes unchanged. See [0021](behavior-changes/0021-joint-crafting-state.md); 130 tests pass per Fabric/NeoForge leaf, Forge compilation only, all four artifact pairs pass. Log `build/pending-catalog-persistence-20260908-065221.log`, exit 0, 25s. This is not server reload/restart or delivery acceptance; migration remains unfinished.

Latest verification follow-up: two stacked-input conservation tests cover tagged versus plain stack separation and damaged nonstackable overflow, including simulation, detached returns and tagged joint persistence. Existing production code passes unchanged. 129 tests pass per Fabric/NeoForge leaf; Forge compilation only; four artifact pairs pass. Log `build/stacked-data-conservation-20260908-063922.log`, exit 0, 24s. See [0021](behavior-changes/0021-joint-crafting-state.md); runtime remainder processing and delivery are still unfinished.

Latest owned-input slice: `GemCutterCraftingState.insertInputStacked` reuses matching-before-empty insertion behind the existing callback guard. Pending results survive insertion and still block recrafting; no delivery/clear API added. See [0021](behavior-changes/0021-joint-crafting-state.md). Two tests added plus expanded reentry coverage: 127 pass per Fabric/NeoForge leaf, Forge compilation only, four artifact pairs pass. Matrix exit 0, 29s, `build/owned-stacked-inputs-20260908-063639.log`. Runtime return processing and output delivery remain unfinished.

Latest registration follow-up: all four real dedicated-server `--quartz` smoke runs pass with a new full-stack Empowered Quartz assertion after reload. Reports were read back, fixtures cleaned and servers saved/stopped; no remaining project Java processes were found. See [Empowered Quartz](migration/EMPOWERED_QUARTZ.md) for exact commands, timings and logs. Rendering, acquisition and restart/multiplayer acceptance remain open; this does not resolve Forge's JUnit harness. The earlier content-only verification below is superseded only for runtime registration coverage.

Latest content slice: [Empowered Quartz](migration/EMPOWERED_QUARTZ.md) ports the upstream registered placeholder item, original styled warnings, model and both animated texture layers. No new ability or acquisition recipe. Scoped matrix exits 0 in 28s (`build/empowered-quartz-20260908-062826.log`), with Forge test compilation only; four artifact/resource pairs and whitespace checks pass. Runtime rendering/registration smoke was not repeated. Overall migration remains unfinished; older baselines below retain their stated scope.

Latest catalog slice: [0022](behavior-changes/0022-atomic-catalog-replacement.md) adds validated whole-catalog replacement. Null/duplicate batches leave live recipes/cache/generation untouched; successful publication invalidates in-flight crafts even with unchanged definitions. Three new tests; 125 pass per Fabric/NeoForge leaf, Forge compilation only, four artifact pairs pass. Log `build/catalog-batch-20260908-062205.log`, exit 0, 29s. Parsing and actual server reload binding are not implemented by this boundary. Overall migration and runtime crafting remain unfinished; older baselines below are historical.

Latest disk-codec verification: three real temporary-file NBT fixtures cover joint state restoration, four truncation boundaries and invalid pending records without partial live-state replacement or rewriting damaged files. No production behavior changed. 122 tests pass per Fabric/NeoForge leaf; Forge test compilation only; four artifact pairs pass. Log `build/crafting-disk-persistence-final-20260908-061841.log`, exit 0, 23s. See [0021](behavior-changes/0021-joint-crafting-state.md). This is not a separate-process/server restart or crash-safe persistence test; runtime owner and delivery remain unfinished. Earlier baselines below are historical.

Latest input-transfer follow-up: `GemCutterCraftingState.insertInput`/`extractInput` delegate to the owned native-capacity handler without exposing it. Simulation and detached returns, invalid inputs, pending-result preservation and callback reentrancy are covered. Three new tests; 119 pass on each Fabric/NeoForge leaf, Forge test compilation only; all artifact pairs pass. Log `build/crafting-input-transfers-20260908-061303.log`, exit 0, 26s. See [0021](behavior-changes/0021-joint-crafting-state.md). Runtime menus/automation adapters, dirty-save integration and remainder/output delivery remain pending. Older baselines below are historical.

Latest state slice: [0021](behavior-changes/0021-joint-crafting-state.md) adds `GemCutterCraftingState`, privately owning inputs and one pending result. Successful craft retains the result, pending blocks repeat crafts, callback reentrancy is rejected, and joint loads decode completely before replacing live state. Five new tests pass: 116 per Fabric/NeoForge leaf, Forge compilation only, four artifact pairs pass. Exit 0 in 26s, `build/joint-crafting-state-20260908-061022.log`. This state is not yet bound to a block entity and intentionally has no result-clear/delivery API; runtime persistence, remainder processing and conserved delivery are still required. Older baselines below are historical.

Latest codec slice: [0020](behavior-changes/0020-crafting-result-persistence.md) adds versioned fresh-world `GCTCraftingResult` NBT, preserving exact output counts/creator data and ordered consumed stacks for later remainder handling. Existing inventory codecs supply version-specific item encoding; malformed records are rejected without touching live state. Five tests added; 111 tests pass per Fabric/NeoForge leaf, Forge test compilation only, all four artifact pairs pass. Matrix exit 0 in 26s, `build/crafting-result-persistence-20260908-055839.log`. No runtime owner saves this record yet; joint input/result persistence, processing/delivery progress and crash/restart acceptance remain pending. Earlier baselines below are historical.

Current baseline: [0019 crafting result boundary](behavior-changes/0019-crafting-result-boundary.md) now has late-failure integration coverage. `consumeForCraft` prepares creator-policy output before guarded input consumption and returns detached output plus consumed stacks only on success. Three additional tests verify final-condition false/exception, live-input data mutation and final-condition catalog remove/restore cannot return output or perform recipe deductions. No production fix was required. Matrix exit 0 in 22s, `build/crafting-late-failure-20260908-055535.log`; 106 tests on each Fabric/NeoForge leaf, zero failures/errors/skips; Forge test sources compile and all four artifact/source pairs pass. Older totals and “latest” sections below are historical. Catalog generation protection is documented in [0018](behavior-changes/0018-catalog-write-generation.md).

Migration remains unfinished. The result is in-memory only, not durable pending output or single-use delivery authority. Persistence, output/remainder handling, runtime registration and full Gem Cutter block/menu/network integration remain open. Forge Minecraft-backed JUnit execution is still unresolved; no gameplay acceptance is claimed by these checks.

Latest content slice: [Scintillating Inlay](migration/SCINTILLATING_INLAY.md) is registered with its original model/animated texture, atlas entry, gold tooltip and English/Portuguese translations. Four-leaf assembly, scoped tests, artifact/resource checks pass (`build/scintillating-inlay-20260907-225447.log`, exit 0, 26s; Forge test compilation only). Acquisition recipe, downstream consumers and live rendering remain pending. No substitute recipe or art was added.

Latest catalog/input integration: [0017](behavior-changes/0017-catalog-input-commit.md) adds name-resolved input consumption guarded against recipe replacement/removal during matching or condition callbacks. Three regressions added; 99 tests pass per Fabric/NeoForge leaf, Forge tests compile, four artifact pairs pass. Command exit 0 in 26s, log `build/catalog-commit-20260907-225052.log`. This returns consumed ingredients only; runtime registration, output/remainders and full crafting remain unfinished. Earlier totals below are historical.

Current verified baseline: 96 tests each on both Fabric targets and NeoForge, zero failures/errors/skips; Forge test sources compile, all four artifact pairs pass. Log `build/hive-commit-integration-20260907-224842.log` (exit 0, 21s). Recipe-level Hive/condition regression fixtures verify changed membership and failed lookups do not authorize deductions and zero-cost definitions still check conditions. They do not implement authoritative Hive storage or runtime letters. See [0016](behavior-changes/0016-input-commit-condition-revalidation.md). The preceding [0015](behavior-changes/0015-recipe-creator-policy.md) binds copy-safe creator output policy to definitions. Full crafting, output/remainders, runtime binding and the Forge Minecraft-backed test harness remain unfinished. Earlier totals below are historical.

Latest catalog continuation: checked `replaceRecipe` rejects absent IDs without mutation, preserves replacement order and stable old snapshots. Two regressions added; 85 tests pass on each Fabric/NeoForge target, Forge test sources compile, all four artifact pairs pass. Evidence: [0012](behavior-changes/0012-recipe-catalog-snapshots.md). Runtime registration/reload/scripting and full crafting remain unfinished.

Latest policy slice: [hive letter crafting conditions](migration/HIVE_CRAFTING_CONDITIONS.md) preserve upstream invitation/resignation/expulsion membership rules. 83 tests pass on each Fabric/NeoForge target; Forge executes the five pure-Java policy tests only. All four artifact pairs pass. Runtime hive lookup, condition binding, letters and crafting authorization remain pending.

Latest matching correction: isolate each ingredient predicate's candidate copy so one predicate cannot fabricate item data or clear counts seen by another. Two regressions fail before and pass after the one-line fix; four-leaf assembly and Fabric/NeoForge tests pass (Forge test compilation only). See [0014](behavior-changes/0014-ingredient-predicate-isolation.md). No full crafting/runtime completion is claimed.

Latest inventory slice: shared `ExtendedItemStackHandler.insertItemStacked` fills data-equal occupied slots before empty slots, respects per-slot policies, and returns detached remainders. Three new regressions pass on both Fabric leaves and NeoForge; Forge test sources compile and all leaves assemble. See [0013](behavior-changes/0013-shared-stacked-insertion.md) for scope/command evidence. This is incremental insertion, not full crafting or player/drop delivery; the migration remains unfinished.

Latest content slice: Radiant Dust item/creative-tab registration, original animated texture/model, atlas source, gold tooltip and original English/Portuguese translations are ported. See [RADIANT_DUST.md](migration/RADIANT_DUST.md) for pinned-source evidence and four-leaf build/artifact/resource checks. Actual raw-quartz Gem Cutter recipe and new runtime rendering/registration acceptance remain pending. No gameplay deviation or substitute acquisition recipe was introduced.

Latest consumption bridge: `GCTRecipe.consumeIngredients` now delegates the recipe's own matcher to the owned input handler, recomputing the full input deduction rather than accepting preview plans. Three integration regressions pass; 73 total tests per Fabric/Fabric/NeoForge leaf, Forge test compilation and all four artifact checks pass. See [0005](behavior-changes/0005-gem-cutter-crafting-conservation.md) for command/log evidence. Conditions, output/remainders, persistence and runtime crafting integration remain pending; this is input consumption only.

Latest catalog slice: `recipe/gct/GCTRecipeList` preserves insertion/replacement order, name/index lookup and data-aware output lookup, with immutable snapshots under [0012](behavior-changes/0012-recipe-catalog-snapshots.md). Five new tests; 70 pass per Fabric/Fabric/NeoForge target. Forge compiles tests; all four targets assemble/pass artifact checks. Runtime ownership, actual recipe population, conditions, reload/menu/network binding and crafting remain pending. Earlier totals are historical.

Latest definition slice: `recipe/gct/GCTRecipe` now connects identity, counted ingredients, copied outputs and conserved preview matching. [0011](behavior-changes/0011-recipe-definition-ownership.md) documents ownership/API adaptations and scoped verification: 65 passing tests each on Fabric/Fabric/NeoForge, Forge test compilation and four-leaf assembly/artifact checks. Registration, conditions, recipe effects, serialization and actual crafting remain unfinished. Earlier totals below are historical slice evidence.

Latest recipe metadata slice: `recipe/CraftingCreator.withCreator` copies output and writes original `creator`/`creator_name` fields using NBT on 1.20.1 and CUSTOM_DATA on 1.21.1. See [0010](behavior-changes/0010-creator-output-copy.md). Four new regressions pass on both Fabric targets and NeoForge (61 total each); Forge compiles tests, all four targets assemble/pass artifact contracts. Recipe invocation, player conditions and hive-letter behavior remain pending; this is not full crafting or authorization.

Latest crafting slice: `inventory/handlers/GemCutterInputHandler` owns 18 ordinary-capacity inputs and atomically consumes a recomputed complete allocation, rejecting stale inputs without partial deductions. See [0009](behavior-changes/0009-gem-cutter-input-boundary.md) and updated [0005](behavior-changes/0005-gem-cutter-crafting-conservation.md). Both Fabric targets and NeoForge pass 57 tests each (eight new cases); Forge compiles test sources and all four targets assemble/pass artifact checks. The Forge Minecraft test harness remains unresolved. Consumed ingredients are returned for future remainder processing; this is not output delivery, server authorization, a block/menu or full Gem Cutter crafting.

Latest foundation slice: `types/BlockPosDimension` now preserves immutable position-plus-dimension identity and strict fresh-world NBT persistence; see [0008](behavior-changes/0008-dimension-position-identity.md). Both Fabric targets and NeoForge pass 49 tests each. Forge's targeted position suite fails during the known NetworkEvent bootstrap, before executing cases; all four targets assemble and pass artifact contracts. Network consumers, world resolution and gameplay integration are not implemented by this slice.

Newest storage slice: shared `OptionalUpgradesHandler` and original `UpgradeType` names are ported under [0007](behavior-changes/0007-optional-upgrade-write-validation.md). Three distinct single-item optional slots, safe simulation and validated direct writes/atomic loads have regression coverage. Both Fabric targets and NeoForge pass 44 tests each; Forge test sources compile and all four targets assemble/pass artifact contracts. Concrete item/device/menu bindings remain pending. No full migration completion or Forge Minecraft-test execution is claimed.

Storage follow-up: [0006](behavior-changes/0006-size-upgrade-removal-capacity.md) corrects the shared size-upgrade removal guard using traced Tank/Trove formulas. Both Fabric targets and NeoForge pass 40 tests each; all four targets assemble and pass artifact checks. This is a handler fix, not completed storage devices or full migration.

Latest continuation: [implementation status](migration/IMPLEMENTATION_STATUS.md). Settings, receipt data, shared inventory/size-upgrade handlers and counted recipe matching exist; steps 1–7 remain unfinished. Proposals 0004 and 0005 are approved. The user explicitly delegated subsequent behavior decisions provided they are properly documented; do not ask another individual behavior-approval form. Publication remains unauthorized. All leaves compile/package. Fabric tests pass; NeoForge's supported ModDevGradle loader-aware JUnit setup now passes its complete test task. Forge 1.20.1 still needs loader-aware execution for Minecraft-dependent tests, not suppression. Recipe matching is not a completed crafting transaction or a functional Gem Cutter. See [0005](behavior-changes/0005-gem-cutter-crafting-conservation.md) for implementation evidence. Historical text below predates these additions and the latest delegation.

Snapshot: 2026-09-07, after Patchouli backend verification, documentation handoff and the subsequent tome parity-audit slice. This is the entry point for another session; read [AGENTS.md](../AGENTS.md) first for repository rules, then this file and the relevant feature report. The migration is **unfinished and not a playable replacement** for upstream Arcane Archives.

## Preserve the working tree

- Repository: `/home/siredvin/projects/ArcaneArchivesRecrafted`.
- Last observed branch: `main`; last commit: `814f2d1` — `chore: bootstrap Stonecutter project and migration plan`. No remote is configured.
- All subsequent migration work is uncommitted, including new Java, resources, tests, scripts and documents. A checkout of the last commit alone does **not** contain this work. Use the same working directory, or preserve tracked modifications **and untracked non-ignored files** when transferring it. Do not reset/clean the tree or assume untracked files are disposable.
- No commit, push, remote, tag, release or upload is authorized by this handoff. The user's original Git authorization covered bootstrap only. Do not use broad staging commands that might include development state.
- Recheck `git status --short --untracked-files=all`, `git branch --show-current`, `git log -1 --oneline` and `git remote` before modifying anything.
- The documentation-only handoff did not change Java, resources, build configuration, scripts or audit JSON. It preserves upstream license/credit files unchanged. An attempted protected AGENTS.md update timed out without approval; that file was left unchanged, and README.md links here instead.
- The subsequent continuation changed only audit scripts/JSON and migration documentation. Pre-edit working-tree snapshot: `/tmp/arcane-continuation-fb5r24x4`. No production source, resource, build configuration, notice or Git publication change was made. See [TOME_PARITY.md](migration/TOME_PARITY.md) for the stronger audit and traced acquisition/opening constraints.

## Settled decisions — do not re-ask them

| Decision | Approved scope | Implementation status |
|---|---|---|
| Targets | 1.20.1 Fabric **and Forge**; 1.21.1 Fabric and NeoForge | Build/bootstrap matrix implemented |
| Identity | Mod ID `arcanearchives`, package `com.aranaira.arcanearchives`, modern display name Arcane Archives Recrafted | Preserved |
| Worlds | Fresh worlds only; no legacy import/remapping or cross-version/loader save conversion | Scope recorded; normal new-world persistence still needs gameplay tests |
| Guidebook | External Patchouli, original content/conditions preserved, licensing reviewed before use; no bundling | Dependency/API classpaths, metadata and startup verified; tome item/content/interactions absent |
| Wearables | Optional Curios on Forge/NeoForge and Trinkets on Fabric; existing non-wearable behavior preserved | Approved only; not installed or implemented; incompatible slot semantics need separate approval |
| Dormant features | Keep dormant/unregistered upstream features documented and disabled | Source notes maintained; do not erase reachable behavior of partly unfinished registered features |
| Raw-quartz conversion | Drop only rejected inventory remainders; preserve successful conversion, ownership and normal consumption; server-authoritative, no content loss on failure | Separately approved; RawQuartzItem/Radiant Chest/conservation correction not implemented |
| Development servers | User accepted Minecraft EULA and authorized local development-server testing | Accepted files are only in ignored local run directories; scripts never accept legal terms automatically |

The user's explicit “approve all three” approved the guidebook, wearable and dormant-feature choices above. Earlier timed-out selection forms did not grant approval; the later explicit message did. The old read-only process/Git command approvals were unrelated and are not pending. No further general backend-selection prompt is needed. See the [decision register](behavior-changes/README.md) for limits and acceptance tests.

## Current implementation and architecture

One canonical `src/main/java` and `src/main/resources` tree feeds four flat Stonecutter leaves. No separately published common module exists. Edit canonical sources, not `versions/*/build` output. Canonical active state is `1.21.1-fabric`; alternate-state verification uses `1.20.1-forge`.

| Leaf | Loader / API | Java | Required external Patchouli |
|---|---|---|---|
| `1.20.1-fabric` | Fabric Loader 0.17.2; Fabric API 0.92.6+1.20.1 | 17 | 1.20.1-85-FABRIC |
| `1.20.1-forge` | Forge 47.3.39 | 17 | 1.20.1-85-FORGE |
| `1.21.1-fabric` | Fabric Loader 0.17.2; Fabric API 0.116.12+1.21.1 | 21 | 1.21.1-93-FABRIC |
| `1.21.1-neoforge` | NeoForge 21.1.234 | 21 | 1.21.1-93-NEOFORGE |

Build stack: Java 21 launches Gradle 9.6.1; Stonecutter 0.9.7, Loom Back Compat 0.4, Fabric Loom 1.17.12, ModDevGradle 2.0.143, Foojay resolver 1.0.0, Mojang mappings. [stonecutter.properties.toml](../stonecutter.properties.toml) owns identity/version/dependency pins; current mod version is `0.1.0`. Minecraft ranges and Patchouli versions are exact tested values, not broad compatibility claims.

Implemented Java under `src/main/java/com/aranaira/arcanearchives/`:

- `ArcaneArchivesMod.java`: shared bootstrap identity/logging.
- `ArcaneArchivesFabric.java`, `ArcaneArchivesForge.java`, `ArcaneArchivesNeoForge.java`: conditional entrypoints, content initialization and loader bus wiring.
- `init/ContentRegistry.java`: shared definitions; eager native Fabric registration versus Forge/NeoForge deferred block/item/tab registration. Creative tab uses the original shaped-quartz icon and currently contains only the two ported item entries.
- `items/ShapedQuartzItem.java`, `blocks/StorageShapedQuartz.java`: shaped quartz and its storage block. Native `BlockItem` is used; there is no modern `CreativeTabAA`, `BlockTemplate` or separate storage-quartz item-block class.
- `util/MathUtils.java`: preserved legacy arithmetic/count formatting/coordinate packing, with mapped Minecraft classes.

Implemented resource behavior:

- Item `arcanearchives:shaped_quartz`; block and block item `arcanearchives:storage_shaped_quartz`.
- Shapeless nine-to-one compression (`storage_shaped_quartz`) and one-to-nine decompression (`destorage_shapedquartz`). No raw-quartz/Gem Cutter acquisition recipe is implemented or replaced with an invented recipe.
- Mining tag, self-drop loot, selected English/Portuguese names and gold tooltips, three original models, two animated PNGs and their sidecars.
- Canonical `recipe`, `loot_table` and `tags/block` data paths become plural for 1.20.1. Recipe result key expands to `item` for 1.20.1 and `id` for 1.21.1. All expansion values are tracked Gradle task inputs.
- `pack.mcmeta` is a Gradle template, not standalone valid JSON before expansion: resource/data formats 15/15 for 1.20.1 and 34/48 for 1.21.1.
- Explicit atlas sources in `assets/minecraft/atlases/blocks.json` stitch preserved plural legacy texture paths. Removing this file reintroduces the runtime-discovered missing-texture regression.
- Patchouli is required on both client/server and supplied externally. Fabric uses `modImplementation`; legacy Forge uses mapped API `modCompileOnly` and implementation `modRuntimeOnly`; NeoForge uses API `compileOnly` and implementation `runtimeOnly`. No backend code/assets/JARs are bundled. No custom tome resources or opening code exist yet.

Not implemented: block entities, extended inventories, storage/network ownership, packets, menus, machines/resonators/Gem Cutter, raw quartz/chest conversion, charms/gems/equipment, complete configuration, tome conversion, recipe-viewer/magic-mod integrations, CI or release automation. See [MIGRATION_TASKS.md](MIGRATION_TASKS.md), not an inferred completion percentage.

## Verification: what passed and what did not

Latest implementation-changing build: `timeout --foreground 10m ./gradlew build --no-daemon`, exit 0, 35s, `build/patchouli-backend-20260907-183831.log`. Subsequent audit-only verification ran the same matrix command: exit 0, 8s, `build/guidebook-audit-20260907-195202.log`; it adds no new gameplay coverage.

Latest alternate-state matrix: switch to 1.20.1 Forge, build all leaves, run both artifact checkers, reset to canonical Fabric. All command exits 0; canonical source and controller bytes restored; 21.19s; `build/guidebook-audit-roundtrip-20260907-195327.log`. Source snapshot: `/tmp/arcane-continuation-fb5r24x4`. Earlier backend round-trip evidence remains in `migration/GUIDEBOOK_BACKEND.md`.

| Coverage | Evidence | Limit |
|---|---|---|
| Production/source archives | All four target pairs pass `verify_artifacts.py` | Metadata, class/Java target, notices, dependency and no-bundling contracts; not runtime |
| Quartz resources | All four archives pass `verify_quartz_resources.py` | Resource schemas/content, not player interactions |
| Shared JUnit | 11 tests per leaf; no failures/errors/skips | MathUtils only; ordinary JUnit, not GameTests |
| Guidebook audit | 6 Python fixtures plus reproducible `--check`; section bodies, all stacks and included standard templates retained | Source inventory, not converted pages |
| Dedicated servers | All four pass readiness, reload, quartz placement/tag/loot/item, Patchouli item registration, cleanup and saved shutdown | Isolated command fixture, not crafting UI, mining, restart persistence or multiplayer |
| Clients | All four pass startup, atlas/model checks, Patchouli preload message and orderly close | No in-world custom-content inspection or actual tome interaction; zero tome JSONs is expected |

Exact eight runtime commands, exit codes, durations and log paths are in [GUIDEBOOK_BACKEND.md](migration/GUIDEBOOK_BACKEND.md). Older [FOUNDATION.md](migration/FOUNDATION.md), [RUNTIME_BOOTSTRAP.md](migration/RUNTIME_BOOTSTRAP.md) and [QUARTZ_CONTENT.md](migration/QUARTZ_CONTENT.md) preserve historical slice evidence rather than superseding this matrix.

During handoff, the 332 source-index entries were checked against the pinned Git tree with no missing/extra/duplicate paths. A scoped process inspection found no Java processes rooted in this project's development-run directories; recheck live state before launching or cleaning processes in a new session. This is not a claim that every system Java/Gradle/Xvfb process is stopped.

Known qualifications:

- NeoForge logs Patchouli's development refmap warning. Fabric 1.21.1 logs removal of an incompatible saved pack selection, but its next resource-manager reload explicitly includes Patchouli. Forge initializes/corrects the new Patchouli config. Headless audio/narrator and platform warnings are not gameplay failures; logs are not warning-free.
- Maven and Modrinth binaries with the same Patchouli version are not interchangeable provenance. Both 1.20.1 variants differ in 14 class entries. Actual tested Maven hashes are recorded separately; no full semantic equivalence or runtime test of the Modrinth distribution JARs is claimed.
- Do not “fix” third-party binaries, pack metadata or user options merely to suppress these warnings without investigating and following the approval policy.
- No full progression, new-world restart persistence, concurrent multiplayer, conservation/fuzz, interactive book, resource reload while reading, missing-Patchouli rejection, clean-cache build or packaged-release runtime acceptance has been completed.

## Reproduce checks safely

Run from the repository root. Preserve complete Gradle output with the required wrapper:

```sh
mkdir -p build
LOG="build/gradle-$(date +%Y%m%d-%H%M%S).log"
START=$SECONDS
timeout --foreground 10m ./gradlew build --no-daemon >"$LOG" 2>&1
RESULT=$?
printf 'exit=%s duration=%ss log=%s\n' "$RESULT" "$((SECONDS-START))" "$LOG"
```

Inspect the exit code before proceeding. Apply the same timeout/log pattern to targeted tasks and Stonecutter switching; never run unlogged Gradle tasks. Use the public task names `Set active project to 1.20.1-forge` and `Reset active project`, not internal generated task names. Snapshot canonical `src/` and `stonecutter.gradle.kts` before a round trip and verify exact restoration even when a build fails.

Fast checks after building:

```sh
python3 scripts/verify_artifacts.py
python3 scripts/verify_quartz_resources.py
python3 scripts/test_audit_guidebook.py
python3 scripts/audit_guidebook.py /tmp/arcane-archives-reference/upstream --check
git diff --check
```

The guidebook audit reads the pinned Git object, not the upstream checkout's working files. Running without `--check` regenerates `docs/migration/guidebook-baseline.json`; review intentional changes rather than hand-editing this audit.

Dedicated-server fixture:

```sh
python3 scripts/smoke_servers.py --quartz
```

Only use the isolated ignored `versions/<leaf>/runs/server` worlds. The fixture modifies coordinates near `(0, 300, 0)`, clears its fixture blocks and removes its forced loading. It is not safe for a real player world. Preflight requires previously accepted `eula=true` and `server-ip=127.0.0.1`; local settings also use port 0, view/simulation distance 2 and online authentication. Missing local EULA/configuration is setup work, not permission to bypass preflight. Never commit EULAs, options, worlds, logs or credentials.

Clients (Linux/X11; Python 3.11+, Xvfb, xauth, xwininfo, libX11 and ffmpeg):

```sh
for NODE in 1.20.1-fabric 1.20.1-forge 1.21.1-fabric 1.21.1-neoforge; do
  xvfb-run -a --server-args='-screen 0 1280x800x24 -nolisten tcp' \
    python3 scripts/smoke_clients.py "$NODE" || exit $?
done
```

Both smoke scripts apply their own ten-minute Gradle deadlines, save complete logs and JSON sidecars, and clean their owned process groups. Do not run concurrent Stonecutter/Gradle mutation workflows in the same checkout. Stop only test-owned processes, never unrelated environment displays/daemons.

Artifacts: `versions/<leaf>/build/libs/arcanearchives-<loader>-0.1.0+<minecraft>.jar` and matching `-sources.jar`. JUnit XML: `versions/<leaf>/build/test-results/test/`; HTML: `versions/<leaf>/build/reports/tests/test/`. Artifact checks inspect existing outputs and are not a substitute for a fresh build after source edits. Adding production classes requires updating the current exact class-set assertion deliberately, without dropping the bytecode, source-JAR or no-bundling checks.

## Documentation-handoff validation

The documentation pass checked 49 local Markdown links with no missing targets, all five audit JSON files for parseability, all 332 unique source-index paths against the pinned upstream Git tree, all seven quartz source/destination asset hashes, and the eight latest runtime rows against their actual JSON sidecars. AGENTS.md and preserved upstream notices match their committed bytes. No new full Gradle build or runtime matrix was needed for these documentation-only edits; the implementation evidence above is retained rather than presented as a new run.

## Recommended continuation, not additional approval

1. Recheck the working tree and active state; retain all uncommitted migration work. Read the latest guidebook report and decision limits. Do not rerun completed approval forms or bootstrap Git.
2. Continue the tome migration using the source-backed opening/acquisition/persistence trace in [TOME_PARITY.md](migration/TOME_PARITY.md) and the real upstream sources. Use the stronger reproducible inventory as a no-loss checklist. Receipt-on-tome-crafting is gated by `BookFromResonator`; automatic receipt is saved before spawning, with no success check. Do not silently change either behavior or treat the raw-quartz approval as a general grant-failure correction. Do not import the prototype titled `Arcane Tome Thingy` with landing text `BUZZ`.
3. Trace feature/configuration gates and custom Gem Cutter recipes before mapping pages. Many referenced items/machines are not ported yet; implement their dependencies as tested vertical slices rather than treating empty stacks, missing pages or an empty book as completion. Preserve literal source identifiers in the audit; source anomalies need explicit analysis, not silent normalization.
4. For storage/progression, trace shared extended-stack handlers before porting Radiant Chest and RawQuartzItem. Start the approved conservation correction with partial-acceptance regression tests, then cover rollback/failure, per-component totals and authoritative server mutation. Creative consumption or supported-chest changes are not automatically approved.
5. For wearables, inspect the pinned candidate APIs/licensing and existing GemUtil/GemSocketHandler routes before choosing exact slots. Keep optional-mod absence working; do not double-activate held/open-menu/equipped gems or silently add a required equipment mod. A slot-semantic incompatibility still requires a focused decision.
6. For each slice, update the source inventory and plan with evidence, build all four leaves, verify the alternate active state and exercise relevant actual runtime behavior. Keep parent checkboxes open until their full acceptance conditions pass. Do not enable dormant features, build save converters or publish anything as a shortcut.

## Documentation and provenance map

| File | Purpose |
|---|---|
| [MIGRATION_TASKS.md](MIGRATION_TASKS.md) | Full phased backlog and evidence-backed checkboxes |
| [UPSTREAM_INVENTORY.md](UPSTREAM_INVENTORY.md) | One entry per pinned upstream Java source; partial port annotations |
| [Decision register](behavior-changes/README.md) | Approvals, deviations and remaining acceptance |
| [Guidebook decision](behavior-changes/0001-guidebook-backend.md) | Approved external backend and content-preservation boundary |
| [Wearable decision](behavior-changes/0002-wearable-integration.md) | Approved optional APIs; slot/effect/persistence constraints |
| [Conservation correction](behavior-changes/0003-raw-quartz-conversion-conservation.md) | Approved rejected-remainder fix and required tests |
| [Foundation](migration/FOUNDATION.md) | Legacy MathUtils semantics and initial registration audit |
| [Runtime bootstrap](migration/RUNTIME_BOOTSTRAP.md) | Local runtime setup, EULA boundary and historical bootstrap evidence |
| [Quartz content](migration/QUARTZ_CONTENT.md) | Ported content, upstream defaults, atlas regression and coverage gaps |
| [Guidebook backend](migration/GUIDEBOOK_BACKEND.md) | Latest matrix, licensing/source pins, Maven/distribution differences and XML inventory |
| [Tome parity prerequisites](migration/TOME_PARITY.md) | Subsequent audit-only matrix, opening/acquisition/receipt trace, feature/recipe dependencies and full section/template inventory |
| [Unfinished upstream features](migration/UNFINISHED_UPSTREAM_FEATURES.md) | Dormant items, matrix/furnace gaps, Arsenal distinction and unresolved reachability |
| [Registry baseline](migration/registry-baseline.json) | Static IDs/registrations/aliases; historical aliases are not converter work |
| [Quartz asset provenance](migration/quartz-assets.json) | Seven unchanged source/destination asset hashes |
| [Guidebook baseline](migration/guidebook-baseline.json) | Generated source inventory: full section bodies, conditions, links, all stacks, recipe XML, images, local and included standard templates |
| [Integration candidates](migration/integration-candidates.json) | Historical Modrinth metadata, not current runtime or approval status |
| [Resolved Patchouli artifacts](migration/patchouli-resolved-artifacts.json) | Actual tested Maven artifact URLs/hashes and comparison with Modrinth |
| [Upstream license](upstream/LICENSE), [credits](upstream/CREDITS.md), [Guidebook license](upstream/Guidebook_License.txt) | Preserved notices; do not rewrite or infer blanket asset permission |

Upstream source: https://github.com/AranaiRa/ArcaneArchives at `80944ce45c6559243d8928cc4b305bf379388652` (`master`, “Patchouli integration.”). Baseline is Minecraft 1.12.2 / Forge 14.23.5.2838 / Java 8 / mod 0.2.1-preview1. Local checkout `/tmp/arcane-archives-reference/upstream` was present and at this commit during handoff. If missing in another environment, obtain a separate reference checkout from that public repository and verify the exact commit; do not add an upstream remote to the working port without authorization.

Local-only scratch references include `/tmp/arcane-archives-reference/patchouli-1.21`, `/tmp/arcane-archives-reference/patchouli-artifacts`, ModDevGradle reference notes under `/tmp/arcane-archives-reference/`, Gradle caches, root `build/` logs and `/tmp/arcane-client-*` screenshots. They are conveniences, not a portable source of truth. Re-download/rebuild when absent, using the source commits and artifact URLs recorded in the documents. The Stonecutter skill reference originally came from the Albina profile; if unavailable, preserve the verified repository build conventions rather than guessing plugin APIs.
