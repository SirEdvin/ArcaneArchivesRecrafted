# Ownership of previously placed network devices

## Current inventory contract

[0128](0128-gem-cutter-inventory-output.md) supersedes the historical pending-paid-output/carrier sections below. The table now saves ordinary inputs and completed output, drops both as ordinary contents plus one empty table, and never carries a pending journal. The current native fixture verifies creator-preserving output drops for parent/accessor destruction and stone replacement, followed by empty-table re-placement with correct ownership. Forge-family cancellation fixtures now cover ordinary input/output inventory data. The restart harness checks current input/output inventory and absence of the journal across separate processes on all four leaves. See 0128 for current verification logs and limits; earlier logs remain historical evidence only.

## Original behavior

Upstream placement assigns personal ownership. The current port has no network owner on Gem Cutters and Monitoring Crystals; existing placed instances therefore lack that identity.

## Approved behavior

Existing ownerless Gem Cutters and Monitoring Crystals remain unlinked until broken and placed again. Newly placed devices use the placer identity, subject to the original fake-player exclusion. Do not assign ownership to the nearest player or first user. Preserve existing local device behavior while unlinked.

## Reason

Avoid guessing ownership or exposing existing inventories to an arbitrary player or Hive.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval status

Approved explicitly by the user in response to the three network-migration decisions.

## Verification

Placement identity and native serialization are implemented for both devices through `NetworkOwnedBlockEntity`. This is persisted ownership, not a finished network index, Manifest relay or Brazier routing service. No local-menu owner-only restriction was added.

### Implementation

- Native server-side `setPlacedBy` records the placer after vanilla imports item data. Supplied item ownership therefore cannot claim the device; fake/no-player placement explicitly clears it. Only the Gem Cutter parent has an entity/owner; its accessor does not become another device.
- Each target uses its pinned native FakePlayer type, including Fabric API's `net.fabricmc.fabric.api.entity.FakePlayer`. No nearest-player, first-user or online-player-list ownership inference is introduced. Source baseline: upstream `blocks/templates/BlockTemplate.java:110–127` assigns placer UUID and excludes Forge FakePlayer.
- Optional `network_owner` UUID is saved with the existing block entity data. Missing or malformed ownership loads unlinked without discarding crafting data. Ownership assignment requires the live server thread. Local Gem Cutter use and Monitoring Crystal target access retain their existing rules.
- No ownership migration or existing server-configuration rewrite was introduced; runtime fixtures used development GameTest worlds. No unfinished sound toggle, network routing or machine functionality was added.

### Native verification

`DeviceOwnershipLifecycle` runs as its own required GameTest on every leaf. For each device it places through native BlockItem with an ordinary mock player, the loader's real FakePlayer implementation, and no player. Each offered item contains a foreign owner and two items; assertions require one consumed item, unchanged remaining item data, and the correct player UUID or no owner. Tests reconstruct the actual registered entity through native save/load, preserving ownership and seven diamonds in the Gem Cutter's last input slot. Loading absent/malformed owner data clears any previous identity. Table local access remains valid even when unlinked. Breaking the table accessor clears both parts and returns exactly one device plus all seven diamonds; Crystal destruction returns one device. Owned fixture blocks/items are cleaned in `finally`.

- `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0, 105 seconds, `build/device-ownership-20260911-095747.log`. Native required completions 7/8/7/7 in target order. Both Fabric XML `fabricruntimetests.deviceownership` cases pass; no ERROR/FATAL lines in the build log.
- Artifact verification initially rejected the newly introduced class as an unlisted artifact (`build/device-ownership-artifacts.log`); the exact binary/source inventory was updated, not weakened. `timeout --foreground 60s python3 scripts/verify_artifacts.py` then passed all four production/source pairs, exit 0, reported duration 0 seconds, `build/device-ownership-artifacts-green.log`.
- No project JVMs remained after verification.

### Populated-device replacement verification

The shared fixture now exercises 18 removal cases per leaf: ordinary/fake/no-player placement followed by Gem Cutter parent destruction, accessor destruction, parent replacement with stone, or accessor replacement with stone; and Crystal destruction or stone replacement. Replacements survive, all old device entities/parts disappear, removed tables reject local access, and each table returns exactly one device and seven diamonds. Crystal replacement correctly produces no loot (unlike native drop-producing destruction); ownership does not introduce a new drop rule. Existing production behavior passed without a production patch.

- `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0, 76 seconds, `build/device-replacement-20260911-100725.log`; native suite completions 7/8/7/7. Both Fabric ownership XML cases pass; no ERROR/FATAL lines in the build log.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, reported duration 0 seconds, `build/device-replacement-artifacts.log`; all four production/source pairs pass. No project JVMs remained.

### Pending-paid-output carrier verification

Twelve additional cases per leaf seed a paid staged craft through `GemCutterCraftingState.craft`, then load its native serialized state into the registered table. The fixture deducts two of three diamonds for four creator-stamped paper, retaining one diamond and seven unrelated emeralds. Both parent/accessor destruction and stone replacement emit exactly one table carrying the state, with no loose input refunds or output grants. Native re-placement consumes that carrier; the complete `Crafting` compound (pending output, creator, consumed inputs and remaining inputs) is unchanged while device ownership is reassigned to the ordinary placer or cleared for fake/no-player placement. Foreign stone survives cleanup; removed tables are invalidated. No production patch was needed.

- `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0, 55 seconds, `build/device-pending-20260911-101153.log`; all four native suites pass (7/8/7/7). Both Fabric ownership XML cases pass, with no ERROR/FATAL lines in the build log.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, reported duration 0 seconds, `build/device-pending-artifacts.log`; all four production/source pairs pass. No project JVMs remained.

### Native populated Gem Cutter cancellation

`DevicePlacementCancellation` extends the required Forge/NeoForge native cancellation fixture with eight cases each: four horizontal orientations, with either populated inputs or seeded paid crafting state. A player-scoped real `EntityMultiPlaceEvent` cancels the actual `ForgeHooks.onPlaceItemIntoWorld` / `CommonHooks.onPlaceItemIntoWorld` call. Both positions must be captured; cancellation preserves the complete item/count (including imported former owner and crafting data), restores water, leaves no owned block entity or dropped items, and clears snapshot capture/restoration state. Listeners and fixture state are cleaned in `finally`. No production workaround was needed.

- `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0, 54 seconds, `build/device-cancellation-20260911-101828.log`; all four native suites pass (7/8/7/7), without ERROR/FATAL lines. The new cancellation cases execute on Forge 1.20.1 and NeoForge 1.21.1, not Fabric.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, reported duration 0 seconds, `build/device-cancellation-artifacts.log`; all four production/source pairs pass with fixtures excluded. No project JVMs remained.

### Monitoring Crystal cancellation

The same fixture also exercises all six clicked attachment directions on each Forge-family leaf. The player-scoped listener now handles the native `EntityPlaceEvent` base class, retaining multi-position snapshot counting for Gem Cutters/Matrix and one captured position for Crystals. Each offered Crystal stack contains two items and foreign owner data. Cancellation returns FAIL, preserves the complete stack, restores the replaced water, leaves no owned entity or drops, and clears snapshot state. Existing multi-block cancellation regressions still pass; no production changes were needed.

- `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0, 55 seconds, `build/crystal-cancellation-20260911-102127.log`; all four required native suites pass (7/8/7/7), without ERROR/FATAL lines. Crystal cancellation itself runs on Forge 1.20.1 and NeoForge 1.21.1 only.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, reported duration 0 seconds, `build/crystal-cancellation-artifacts.log`; all four production/source pairs pass. No project JVMs remained.

### Operator-seeded real-process restart

`scripts/smoke_device_ownership.py` seeds one owned and one ownerless instance of each device in guarded empty space in existing approved loopback/EULA worlds. Each table has three diamonds in input 0 and seven emeralds in input 17, with no pending craft. Both sessions check native block state, complete expected crafting input records, exact owned UUID and absence of owner data on unlinked devices. Save/shutdown and a separate server process preserve these identities, inputs and the parent/accessor layout. Cleanup restores empty space, removes owned dropped items/objectives, and releases only the fixture's added chunk hold. This is operator-seeded persistence, not actual player placement across restart or paid-output restart acceptance.

- `timeout --foreground 10m python3 scripts/smoke_device_ownership.py`: exit 0, 197 seconds, `build/device-restart-20260911-103139.log`. Eight sessions (setup and restart per leaf) passed every required marker and save/shutdown check. All four aggregate target results are true. Session logs contain no ERROR/FATAL/Exception or command-parser failures; each restart confirms fixture cleanup, chunk-hold release and objective removal. No project JVMs remained. No Java production change was needed.

### Pending paid-state restart extension

The restart harness now seeds the owned table with one remaining diamond, seven emeralds, a consumed record of two diamonds and four pending paper bearing creator UUID `[I;0,0,0,2]` / name `Restart creator`. The ownerless table remains the ordinary-input control. Before and after full process restart, native NBT predicates check input amounts, pending output/consumed amounts, creator data and ownership; explicit list-length checks reject extra input/output/consumed entries. The idle control has no pending compound. Neither session may contain loose item grants before cleanup. This exercises the native saved-state decoder/persistence, not recipe payment through a connected menu, delivery or crash recovery.

- `timeout --foreground 10m python3 scripts/smoke_device_ownership.py`: exit 0, 215 seconds, `build/device-paid-restart-20260911-103707.log`; all eight sessions and four aggregate target results pass. Every paid-output, consumed-input, no-loose-grant and cleanup marker passes; no ERROR/FATAL/Exception or command-parser failures in session logs. Servers saved/stopped; no project JVMs remained. No Java production fix was required.

This establishes native placement, in-process serialization/reconstruction and bounded destruction/replacement conservation, including the seeded pending-paid-output carrier and Forge-family placement cancellation for both devices, plus operator-seeded ordinary-input/ownership and pending-paid-state process persistence. It does not establish native-menu acquisition or delivery of that seeded pending result. Connected-player multiplayer, player-placed process restart, Fabric protection integrations and the broader network implementation remain open. Existing ownerless worlds are not auto-claimed.
