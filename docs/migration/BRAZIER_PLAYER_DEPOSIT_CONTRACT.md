# Brazier player deposit contract

Status: installed paid player-deposit path, approved failure recovery and native block callbacks implemented and tested; connected input/acceptance remains unverified.

Baseline: upstream `bb99accf48ed583e29b0efae56e28c963407b8df`. Paths below are relative to `src/main/java/com/aranaira/arcanearchives/` in that checkout.

## Reachable activation and history

Paid controlled-clock repeat coverage now verifies first-click selected-only extraction, empty-hand remembered-reference collection at exactly 300 ms, offhand preservation, and refusal without payment after a 301-ms repeat gap. All four native suites pass (`build/brazier-paid-repeat-20260913-093455.log`, exit 0, 58s); artifacts pass (`build/brazier-paid-repeat-artifacts.log`). These tests call the installed device with a controlled clock; real callback/packet timing remains unverified.

The same activation fixture now also runs through native `ServerPlayerGameMode.useItemOn` with the existing registered server-player/test-connection fixture. Empty-hand no-op, offhand exclusion and exact main-hand payment pass on every target (`build/brazier-gamemode-20260913-092558.log`, exit 0, 60s; artifacts `build/brazier-gamemode-artifacts.log`). This adds native game-mode dispatch coverage; incoming packets, real mouse input, timed empty-hand repeat and connected presentation remain unverified.

Native callback fixtures now exercise handled empty-hand no-op, handled offhand exclusion and exact main-hand payment into an owned Chest through the existing loader/version adapters. Minecraft 1.20.1 uses native block-state `use`; 1.21.1 uses native empty-hand block-state dispatch and the registered block's held-item callback. All four native suites pass (`build/brazier-native-activation-20260913-092258.log`, exit 0, 58s); artifacts pass (`build/brazier-native-activation-artifacts.log`). This is callback-level activation, not incoming packet/game-mode sequencing, connected mouse input or a timed empty-hand repeat.

The port now dispatches server MAIN_HAND activation to the installed paid-deposit path. Minecraft 1.20.1 uses `use`; 1.21.1 handles both `useItemOn` and `useWithoutItem`, returning handled even for no-op/excluded deposits and not falling through to duplicate empty-hand activation after item handling. All four build/native regression suites pass (`build/brazier-activation-20260913-091858.log`, exit 0, 86s); artifacts pass (`build/brazier-activation-artifacts.log`). Existing fixtures call the paid device method directly: actual native callback input sequencing, real clicks and client delivery remain acceptance gaps.

`blocks/Brazier.java:73–81` dispatches only server-side MAIN_HAND activation to `BrazierTileEntity.beginInsert(..., false)`, while returning handled regardless of success. Offhand deposits are not established by the helper's hand argument.

`tileentities/BrazierTileEntity.java:179–207` maintains device-local transient click history: same native player's UUID object and at most 300 ms identifies a repeat; a gap greater than 950 ms clears stored item references. An empty hand on repeat can reuse the stored reference. An empty reference, a stack bearing `Quark:FavoriteItem`, Manipulation scepter or Debug Orb stops insertion. The duplicated Manipulation check is not evidence that every scepter is excluded.

## Source collection

The non-paying `BrazierPlayerSelection` implementation now selects ordered main-inventory slot indices and a copied reference using transient history and the existing item/data comparator. Initial held-container exclusion uses native Forge/NeoForge capabilities and the existing `TroveItemStorage.ITEM` lookup (native on Fabric 1.21.1; approved 0099 mod-owned API on Fabric 1.20.1). Repeat matching still includes the container item itself, as upstream does; it never extracts nested contents. Native selection/history/capability tests pass on all four targets (`build/brazier-player-selection-20260912-183239.log`, exit 0, 87s; artifacts `build/brazier-player-selection-artifacts-final.log`). It remains disconnected from production activation and source payment. Favorite metadata and broader history/data combinations still need dedicated native regressions.

`BrazierTileEntity.java:209–265` requires a server network before extracting source items. It obtains the player's upward-facing inventory handler, explicitly excluding offhand. A held item exposing its own item-handler capability is not extracted by the initial held-stack branch; commented nested-container extraction is not working functionality and must remain dormant.

For ordinary held items, the selected slot is extracted first. A first click records the reference; a repeat then collects matching item/data stacks from the exposed player inventory. Count does not participate in matching. Preserve source ordering and component-sensitive matching when mapping this to native modern inventory APIs.

## Batch routing is not repeated single-stack routing

`util/InventoryRoutingUtils.java:118–165` first offers the input list to an eligible preferred destination, then constructs fresh ranking for the remaining list. Its inner loop removes one input, offers it to the current route, and:

- on complete acceptance, records/refreshes the route cache and proceeds to the next input at that same destination;
- on a nonempty remainder, replaces that input with its remainder and immediately advances to the next destination, leaving later inputs in order.

Consequently a source collector cannot simply call the port's single-stack cache API once per inventory slot. That would reconstruct fresh rankings and reconsider preferred routing between stacks rather than preserving the original destination-major batch traversal. Complete acceptance of an individual stack may update the cache even when the overall batch remains incomplete.

`BrazierRoutes.insertBatch`, `BrazierRouteCache.insertBatch` and the installed block entity now provide real destination-major batch insertion. Existing single-stack deposits/simulation remain separate; both paths share current cache eligibility and recording logic. Native fixtures verify full/partial acceptance, remaining input order, offered-stack preservation and independent results across all four targets (`build/brazier-batch-routing-20260912-181417.log`, exit 0, 91s; artifact check `build/brazier-batch-routing-artifacts.log`). This does not yet connect native player collection, payment or activation, and broader multi-destination batch coverage remains pending.

## Source payment and remainders

`BrazierBlockEntity.deposit` now connects history/selection, source validation and native extraction to batch routing, selected-slot/main-inventory returns and rejected overflow. Approved 0145 recovery retries inventory, stores only unpaid/unspawned items, and blocks subsequent payment until pending returns are delivered. Full/partial/zero acceptance and oversized-source successful/refused ejection pass on all four native targets (`build/brazier-paid-player-20260913-091513.log`, exit 0, 85s; artifacts `build/brazier-paid-player-artifacts.log`). Tests directly invoke the installed entity; block activation, connected inventory updates and actual loader cancellation remain pending.

`BrazierTileEntity.java:267–294` attempts sound when there are extracted inputs, routes the collected batch, returns the first remainder to the selected slot when the reference was held, then stacks later remainders into the player inventory and ejects any overflow. It explicitly synchronizes the native server player's inventory afterward.

The reachable block caller passes `simulate=false`. Do not expose or claim safe player-deposit simulation from the legacy helper alone: its initial selected-slot extraction passes false independently of the helper's simulation parameter. This audit does not authorize changing that dormant parameter behavior or adding a simulation entry point.

Approved 0144 establishes rejected item-source recovery for failed ejection spawning. Any player-source implementation must separately demonstrate conservation of its extracted batch and returned/ejected remainders; item-entity fallback tests do not establish player-source payment.

The user approved the recovery policy in [0145](../behavior-changes/0145-brazier-player-overflow-recovery.md). The exceptional pending-return path remains unimplemented; the approval blocker is cleared. Source inspection establishes the unchecked spawn result, not a reproduced live player loss.

## Required connected verification

Native item-data selection fixtures verify plain versus data-bearing references in both directions using the existing version-specific block-entity NBT/component adapter, count-independent matching, and unchanged source counts/data (`build/brazier-player-data-20260912-183922.log`, exit 0, 61s; all four suites and `build/brazier-player-data-artifacts.log` pass). This is selection coverage, not paid player routing or favorite-marker coverage.

Native selection-only follow-up verifies distinct-player repeat interruption, private-reference retention at a 950-ms gap versus clearing at 951 ms, and fresh history-instance isolation (`build/brazier-player-history-20260912-183558.log`, exit 0, 60s; all four suites and `build/brazier-player-history-artifacts.log` pass). This uses existing native player/fake-player fixtures and is not connected multiplayer or installed-device history lifecycle evidence.

- Native main-hand activation, offhand exclusion and handled/no-op behavior.
- First click, timed repeat with held/empty reference, different player and expired history.
- Favorite and tool exclusions; held item-handler compatibility on each loader without activating nested-container extraction.
- Destination-major batch order, partial acceptance, cache refresh and exact source/destination/remainder conservation.
- Selected-slot return, inventory stacking, overflow ejection and inventory synchronization.
- Preserve existing single-stack routing regressions and dedicated-server safety across all four targets.

Mixed Gem Cutter/Chest batch fixtures additionally verify fresh priority and exact overflow, unchanged offers/empty-input exclusion, retention of the first completed stack's cache preference across fallback, preferred-first partial remainder order, and cache refresh from an individually completed stack despite incomplete overall acceptance. Four native suites pass (`build/brazier-multi-batch-20260912-182551.log`, exit 0, 61s); artifact verifier passes (`build/brazier-multi-batch-artifacts.log`). These native routing results do not establish player activation, source collection/payment, or connected gameplay.
