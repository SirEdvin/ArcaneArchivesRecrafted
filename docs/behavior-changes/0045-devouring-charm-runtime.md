# Devouring Charm handheld runtime

Status: implemented handheld migration slice on all four targets. Tank/Trove optional-upgrade use, Parchtear integration and final gameplay acceptance remain unfinished. No approval to remove those requirements is claimed.

## Original behavior and evidence

Pinned release `bb99accf48ed583e29b0efae56e28c963407b8df`: `items/DevouringCharmItem.java`, `inventory/ContainerDevouringCharm.java`, `inventory/handlers/DevouringCharmHandler.java`, `client/gui/GUIDevouringCharm.java`, `events/EventHandler.java`, `util/ItemUtils.java`, and `init/RecipeLibrary.java`.

The original item opens a disposal screen with one fluid slot, six temporary item slots and six persistent ghost pickup filters on its reverse face. Temporary items remain recoverable until closing; only the fluid-slot item is returned on close. Fluids are drained on insertion. Filters copy one item without consuming the cursor stack and compare item identity plus data, ignoring count. Carried charms void matching pickups and play the original eating sound with a 500ms throttle. The Gem Cutter recipe produces four charms from one gold ingot, two obsidian and one flint and steel.

## Native implementation

`DevouringCharmItem`, `DevouringCharmMenu`, `DevouringCharmFluids` and `DevouringCharmScreen` connect registration, original acquisition costs, GUI, fluid disposal and saved filters. Native NBT/CustomData stores filter samples, not temporary disposal inventory. The existing Gem Cutter flint-return path handles the acquisition tool. `port_devouring_charm_assets.py` recovers original item model, animated PNG/metadata and pretty/simple GUI textures from the pinned release; atlas inclusion is explicit. Original English/Portuguese item text is retained, with localized destructive-action warnings and filter controls added.

The server binds the screen to its actual held Charm, prevents moving Charms through its inventory slots/hotbar/offhand swaps, and rejects actions after that binding becomes invalid. Ghost actions carry only bounded button indices; the server copies its own cursor stack. The reverse-face state is synchronized to the server rather than merely hiding slots locally. Filters cannot be extracted as real items or drag-distributed into ordinary inventory. Recursive Charm-as-filter copies are rejected.

Disposal slots delete their contents on close by design, not immediately; the GUI explicitly warns of this. Fluid extraction is immediate, uses native item-fluid APIs and preserves the handler's returned container. Fabric uses a real single-slot ContainerItemContext and transactions; Forge-family branches use native item handlers. Queries do not execute extraction. Radiant Amphoras are excluded from fluid draining so copied remote links cannot drain a world Tank unintentionally. Single containers placed in ordinary disposal slots are also drained, matching the original handler's broader insertion behavior. Unsupported stacked/modded container cases remain part of compatibility acceptance rather than being silently truncated.

The fluid return slot is cleared before returning its contents, and close cleanup runs once. Shift-transfer returns do not merge back into their active source slot: an identical returned container would otherwise feed native AbstractContainerMenu's repeated quickMoveStack loop. Unaccepted close returns are dropped, not silently discarded.

Pickup matching scans Charms in the main player inventory. `DevouringCharmEvents` uses low-priority Forge/NeoForge pickup hooks and respects existing cancellation/denial. Fabric uses a narrow injection immediately before the permitted native Inventory.add call. Shared handling verifies server thread, player/entity state, pickup delay and native saved `Owner` restrictions before discarding a matching entity; item ownership is never reassigned. Native sources/bytecode were inspected for these entrypoints and the public saved ownership representation. The original sound/volume/pitch range and wall-clock throttle are retained.

## Remaining scope and integration evidence

Remaining: Tank/Trove VOID upgrade installation/storage semantics, Parchtear charge handling, alternate-GUI configuration, advancement/tome wiring, broader mod interoperability and runtime acceptance. The retained upstream tooltip mentions optional upgrades; it is not evidence that those are implemented. No runtime pickup/mixin application, fluid-container, save/restart, full-inventory or multiplayer acceptance is claimed.

Four-target `./gradlew assemble --no-daemon` passed under the timeout/full-log wrapper: exit 0, 19s, `build/devouring-charm-final-20260908-200205.log`; preceding Java integration compiled all targets in `build/devouring-charm-integration-20260908-195908.log`, exit 0, 25s. `git diff --check` passed. Both production Fabric JARs were inspected: mixin classes and target descriptors are remapped to intermediary, and compatibility levels are JAVA_17/JAVA_21 respectively. Forge-family resource processing excludes the Fabric mixin config. These are static integration checks, not gameplay tests. Artifact inventory expectations were extended but the full verifier was not run. No clients, test campaigns, optimization, commits or publication.
