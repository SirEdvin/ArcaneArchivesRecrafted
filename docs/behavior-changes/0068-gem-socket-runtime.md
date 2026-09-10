# Gem Socket storage, recharge control and active-gem selection

## Original behavior and provenance

Pinned source/assets: bb99accf48ed583e29b0efae56e28c963407b8df. Inspected GemSocket, GemSocketHandler, ContainerGemSocket, GUIGemSocket, GemUtil.AvailableGemsHandler, GemUtil.rechargeGems, EventHandler, ItemRegistry and RecipeLibrary. Original MIT license inspected; existing attribution retained. Native implementation uses SimpleContainer/Slot/ContainerHelper rather than copying the legacy handler's Roots-derived implementation.

The socket is a single-stack item with one Arcane Gem inventory entry. Its non-wearable opening lookup prioritizes main hand, then native player inventory (including offhand). The upstream worn-socket lookup belongs between those steps; that optional integration remains pending. Opening from offhand can select an earlier inventory socket: this original lookup quirk is preserved, not silently changed to the used hand.

Menu ordering: main inventory 0–26, hotbar 27–35, offhand 36, gem slot 37. Shift transfers accept gems only: inventory prioritizes socket then offhand; offhand prioritizes socket then hotbar/main inventory; socket extraction prioritizes offhand then hotbar/main inventory. Socket stacks themselves cannot be moved through this menu.

Active-gem ordering is main hand, offhand, then the open socket's gem. The worn socket is consulted only when no socket menu is open. Merely carrying a closed socket in normal inventory does not grant passive effects. The original tooltip's broad wording is not proof that it does.

The recharge button invokes each available gem's original recharge path; no keyboard binding exists in this path. Acquisition is Arsenal-gated: one scintillating inlay, four native gold nuggets, one bowl, one leather. Gem Socket itself has the normal upstream creative-tab visibility, unlike the individually gated gem items.

## Implemented scope — all four targets

- Registered `arcanearchives:gemsocket`, persistent native item inventory, charge/name tooltip, original item/GUI artwork and gated Gem Cutter acquisition.
- Native server-authoritative menu with offhand and one-gem slots, original shift-transfer priorities and slot coordinates, recharge icon, narration, keyboard focus and tooltip.
- Native container-button packet validates the current menu, button ID, player/thread and continued live socket identity. No client-supplied item payload, unrestricted remote operation or new hotkey.
- Native slot sync and socket persistence on menu changes and passive/event charge changes. The menu is a live view of the already-packed gem, not a second item returned on close. Replacement/missing sockets invalidate operations. Native swap/drag/pick-all paths cannot extract the bound socket through its menu.
- Shared active-gem selection connected to XP amplification/recharge, potion reuse, looting/death payment, critical hits, healing/animal recharge, lightning/rain behavior, fire protection and Enderman suppression/recharge.
- Salvegleam, Stormway and Agegleam ticking now uses one shared available-gem iteration per player tick, instead of separate native item ticks that could reorder held/socket effects or execute twice. Order is main hand, offhand, open socket. Fabric uses end-server-tick, Forge end-player-tick and NeoForge player-post-tick; all mutations retain logical-server/main-thread guards. Loader/mod event-phase compatibility remains deferred acceptance, not a parity claim.
- Item copies cannot qualify as available live gems for inventory-material/powder payment. Original material overrides and powder exclusions remain intact.

## Approval / remaining scope

This is the original non-wearable gameplay route, with native safety/synchronization adaptation; no balance or new activation policy proposed. Optional Curios/Trinkets API families remain approved by 0002, but have not been installed or implemented here. Exact body-slot mapping still needs source/API confirmation before any new slot decision.

Both original GUI art sets are recovered; the screen currently uses the pretty set. The shared pretty/simple preference, wearable opening/active references and equipment synchronization, HUD, upgrades, broader integrations and runtime acceptance remain open. Do not mark the whole socket/gem family complete. Recipe-list order/presentation and other-mod hook timing have not been runtime-accepted.

## Verification

`python3 scripts/port_gemsocket_assets.py /tmp/arcane-archives-reference/upstream` recovered the pinned item/GUI resources and checked PNG companions. No companion animation existed for this socket item in the enumerated pin.

`timeout --foreground 10m ./gradlew assemble --no-daemon` exited 0 in 21s. Log: `build/gemsocket-complete-20260909-122404.log`; all four compileJava leaves present, BUILD SUCCESSFUL in 20s. `git diff --check` passed. Two preceding integration failures (protected Button narration constant; modern tooltip context has registries, not level) were resolved against native bytecode before this successful build.

All four production JARs inspected for socket/menu/screen/active-selection/tick classes, eight active-effect caller references, original model/GUI and gated recipe. Artifact verifier expectations extended; full verifier not run. No gameplay test campaign, client/server launch, optimization, commit or publication. Migration remains incomplete and not first-playtest-ready.
