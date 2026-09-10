# Dedicated wearable Gem Socket slot

Status: user-selected custom-slot policy implemented; four-target assembly and static packaging verified. Gameplay acceptance remains deferred.

## Original behavior

The pinned Arcane Archives implementation uses Baubles BODY equipment. The previously inspected BaubleGemUtil selects the first matching socket, not every worn gem; open Gem Socket menus take precedence over worn sockets. API-family replacement with optional Curios/Trinkets was approved in 0002, but incompatible equipment-slot semantics require a separate decision.

## Verified compatibility issue

Inspected the exact candidates in docs/migration/integration-candidates.json, validating downloaded bytes against their recorded SHA-512 values:

- Curios 5.14.1+1.20.1 Forge and 9.5.1+1.21.1 NeoForge source JARs: SlotTypePreset explicitly contains BODY("body", 100). Native datapack slot registration is preferred over the deprecated IMC preset API.
- Trinkets 3.7.2 and 3.10.0 distribution JARs: enumerated all bundled data/trinkets slot definitions. The chest group defines back, cape and necklace, but no body slot. Those are the bundled defaults, not a claim that a modpack cannot define extra slots.
- Trinkets bundles version-matched Cardinal Components dependencies; its actual metadata, not the incomplete Modrinth dependency list, must guide Gradle/runtime integration.

Using a necklace/back/cape slot would make the socket compete with a different equipment category from the original BODY slot. Adding chest/body avoids that reassignment, but adds a new slot in standard Trinkets installations and is an explicit compatibility decision.

## Approved behavior

The user requested a mod-specific custom slot on BOTH APIs instead of either body-slot proposal. Curios uses `arcanearchives_gemsocket`; Trinkets uses group/slot `arcanearchives/gemsocket`. Both provide one slot by default, assigned to players, with a custom predicate accepting only Gem Socket items. Unlike the original shared BODY slot, this does not compete with other body equipment: that equipment-cost change is intentional and user-directed.

Preserve first-matching-worn-socket activation, main-hand/offhand ordering and open-menu precedence. Do not activate every socket if another mod or datapack expands slot capacity. Do not repurpose necklace/back/cape slots or intentionally overwrite modpack slot allocations.

## Superseded alternatives

The original Curios body / Trinkets chest/body proposal and necklace fallback were not selected. Do not reopen that approval question or replace the dedicated slots with shared equipment slots.

## Affected targets

All four supported leaves receive their loader's dedicated custom slot.

## Approval

The user asked: "Can't you introduce specific mod custom slot for both cases? So single socket would be always used and it would be mod specific" and then instructed continuation after the dedicated-slot semantics were explained. This selects the custom-slot policy; no further mapping approval is pending. Curios/Trinkets remain optional under 0002.

## Implemented runtime

- Optional compile-only dependencies pinned in Stonecutter properties: Trinkets 3.7.2 / 3.10.0 and Curios 5.14.1+1.20.1 / 9.5.1+1.21.1. Fabric compilation also uses the corresponding CCA base API. No dependency JAR is bundled or made mandatory by this change.
- `WornGemSocket` isolates API classes behind mod-presence checks. It selects the first matching socket in ascending slot-index order; expanded slot capacity cannot multiply worn effects. Curios lookup excludes cosmetic slots and uses native inactive-slot filtering where supported.
- `AvailableGems` keeps main-hand/offhand ordering, followed by the open socket menu or one worn gem, never both. All existing consumers use that selection, including passive tick effects and event-driven recharge/combat behavior.
- Server gem references are backed by the exact equipped socket and a saved stack snapshot. Mutation is published only while that owner remains equipped and unchanged. Replacement, unequip, changed serialized data and menu opening invalidate stale cached references. Weak player keys do not retain disconnected player objects.
- Native equipment serialization remains responsible for socket persistence/death behavior. Trinkets receives inventory dirty notification; inspected Curios event handlers compare stack snapshots and synchronize changed equipment without requiring a custom item capability.
- Client potion-use prediction reads synchronized equipped socket data without using or mutating the server cache. Charge payment stays server-authoritative.
- Existing socket lookup now includes equipped sockets between the main-hand and inventory fallbacks, and socket menus validate worn ownership. Load a gem through the existing item menu before equipping; no new standalone worn-socket hotkey is introduced in this checkpoint.
- Custom predicates are registered during Fabric initialization and queued Forge-family common setup. Loader builds exclude the other API's slot definitions. English/Portuguese slot names reuse the existing Gem Socket name.

## Verification

`timeout --foreground 10m ./gradlew assemble --no-daemon` passed for all four leaves. Final log: `build/worn-socket-final-20260909-141403.log`, exit 0, 20s (Gradle 19s). `git diff --check` passed. Preceding dependency and implementation assemblies also passed.

All four production JARs inspected for adapter classes, one-slot definitions, player assignments, custom validators and localized slot names; foreign-loader slot data and bundled optional API classes are absent. Original resolved Curios/Trinkets artifact hashes recorded in `docs/migration/wearable-resolved-artifacts.json`. Native API signatures were checked against exact candidate JARs/source JARs; slot format was additionally checked against official Curios documentation and Trinkets loader source.

No gameplay test campaign, client/server launch, optimization, commit or publication performed. Equip/unequip, native synchronization timing, death/reconnect, dimension travel, optional-mod absence and arbitrary modpack changes remain in deferred runtime acceptance. Standalone worn-menu access controls, gem HUD/upgrades and other shared/presentation integrations remain in the migration queue. Assembly is not first-playtest acceptance.
