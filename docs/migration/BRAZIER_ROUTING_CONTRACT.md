# Brazier routing contract — 0.0.2

Status: routing engine, destination adapters and resources implemented in the scopes below; registered Brazier gameplay and release acceptance remain incomplete.

Baseline: upstream `bb99accf48ed583e29b0efae56e28c963407b8df`, inspected at `/tmp/arcane-archives-reference/migration-source`. Source paths below are relative to its `src/main/java/com/aranaira/arcanearchives/`. The upstream MIT license was inspected; retain its notice when porting source/assets.

## Reachable entry points

Acquisition is now restored from pinned `init/RecipeLibrary.java:127`: four Radiant Dust, eight coal/charcoal, two gold ingots and three logs yield one Brazier in the Gem Cutter. `IngredientStack(Item,int)` delegates to legacy `Ingredient.fromItem`, whose wildcard metadata accepts both coal variants; modern recipe uses `minecraft:coals`. Native paid-menu coverage on all four targets verifies each fuel, insufficient-payment nonmutation and exact output extraction (`build/brazier-acquisition-20260912-163337.log`, artifact check `build/brazier-acquisition-artifacts.log`). This supersedes recipe-pending statements below; deposits and connected GUI acceptance remain open.

- `blocks/Brazier.java:73–81`: only MAIN_HAND dispatches server-side player insertion; activation returns true regardless of insertion success. The helper's hand parameter does not establish offhand deposits.
- `blocks/Brazier.java:85–90` and `tileentities/BrazierTileEntity.java:140–177`: item collision routes on the server, ignores dead/already-rejected entities, replaces the consumed entity with rejected remainders, marks those rejected and gives a 20-tick pickup delay.
- `BrazierTileEntity.java:179–295`: main-hand deposits and repeated-click matching-item collection; 300-ms repeat window, reference history clearing after 950 ms. Matching uses item/data identity, not count. Favorite-tagged items are excluded. The helper's held-item capability branch and commented nested-container extraction must not be mistaken for working container-emptying support.
- `BrazierTileEntity.java:404–448`: insertion-only virtual automation surface reports 999 empty slots and limit 999, with no extraction. There is no stored Brazier buffer. Ordinary insertion returns its exact remainder; the modifiable setter routes then ejects rejected items.
- `BrazierTileEntity.java:340–346`: Manipulation interface opens configuration; ordinary activation is deposit, not configuration.

## Current network and ranking

`util/InventoryRoutingUtils.java:68–110` uses the owner's personal network when not in a Hive or when personal-only is selected, otherwise the Hive. Destinations require the routing interface, matching dimension and horizontal radius. Sort descending by weight, discard negative weights. Do not route to every inventory discovered for Manifest.

The inspected interface implementers are Radiant Chest, Radiant Trove and Gem Cutter. Radiant Crafting Table is not an implementer; Monitoring Crystal targets are not automatically Brazier destinations. Enum constants alone are not reachable routing modes.

`InventoryRoutingUtils.java:28–64`:

- Trove-specific score overrides the ordinary score when it is not -1.
- NO_NEW_STACKS and GCT reject a missing packed-item reference; otherwise score 4999 and 5000 respectively.
- Completely empty ordinary inventories score zero.
- An ordinary inventory without the packed item scores occupied-slot count.
- Otherwise score is the ceiling of `(packed item count / potential capacity) * 1000 + 500`; nonstackables use one per slot, others use native max stack size times slot multiplier.
- Packed-reference ranking does not inspect NBT. Actual insertion must still enforce item/data identity and storage rules. Do not replace ranking with component-sensitive matching merely because the port's insertion helper is component-sensitive.

## Cache and transfer order

Simulation nuance: upstream `InventoryRoutingUtils.java:156–161` records complete acceptance in the cache even when `simulate` is true. Preserve this transient preference side effect; inventory simulation must still leave stored/offered items unchanged. Native mixed Chest/Gem Cutter coverage passes across all four targets (`build/brazier-simulation-cache-20260912-155113.log`).

`InventoryRoutingUtils.java:118–165` tries a valid cache before fresh ranking. Routes receive the remaining list in order; the first nonempty remainder advances to the next route. Complete acceptance refreshes the cache. Preserve remainder identity/count, not only success booleans.

`BrazierTileEntity.java:376–393,451–489` keys cache by packed item, expires after 1000 ms and refreshes a still-valid entry rather than replacing its destination. Cache and click state are transient. Approved 0103 requires current live identity, network, mode, dimension, radius and routing admissibility before using cached routes, without force-loading. Approved 0114 corrects the original Z-delta typo for both fresh and cached eligibility. These approvals do not authorize changing normal priority or removing cache preference.

## Settings and persistence

`BrazierTileEntity.java:43–50,79–105,297–314`: default radius 150, controls step by 10, bounds 0–300; personal-only defaults false. Persist `range` and `subnetwork` alongside existing network ownership. No persistent input inventory is implied. `131–137` gates absorption sounds through global/device preferences and a 300-ms throttle. Range visualization is client-only (`349–373`).

## Port mapping and remaining work

The production tree now registers `blocks/Brazier.java`, its native BlockItem and `tileentities/BrazierBlockEntity.java`, using the existing block ownership family and a per-device route cache. Native installed-device tests verify routing and `range`/`subnetwork` serialization. Native item placement/removal now verifies exact placement payment, imported-owner replacement, fake/no-player exclusion, discovery and self-drop conservation (`build/brazier-item-placement-20260912-162918.log`, all four native suites; artifact check `build/brazier-item-placement-artifacts.log`). Recipe acquisition and player/entity/automation deposits remain unimplemented. `recipe/gct/GemCutterCraftingState.java` implements copied-remainder, matching-occupied-slot-only top-up; callers must mark real changes dirty. Do not substitute its general stacked insertion method, which can fill empty slots.

Implemented foundations (port source paths are relative to `src/main/java/com/aranaira/arcanearchives/`):

- `data/BrazierRoutes.java`: current personal/Hive discovery, live identity/dimension/XZ radius checks, one weight evaluation per fresh collection candidate, descending priority, immediate pre-transfer revalidation and exact copied remainders.
- `data/BrazierRouteCache.java`: transient weak references, one-second preference lifetime, current eligibility eviction, complete-acceptance recording/refresh (including simulation), no recording/refresh on partial acceptance.
- `tileentities/GemCuttersTableBlockEntity.java`, `RadiantChestBlockEntity.java`, `RadiantTroveBlockEntity.java`: live occupied-slot Gem Cutter top-up, Chest ANY/NO_NEW_STACKS state and ranking, Trove-specific scores. Chest mode persistence/menu controls are implemented; connected checkbox acceptance remains open.
- `scripts/port_brazier_assets.py`: reproducible solid/fire meshes, shared material, model wrappers/transforms, single-variant blockstate, textures and sounds. All four clients have standalone model loading and startup smoke evidence; this is not in-world visual acceptance.
- `init/ContentRegistry.java` and `config/ServerSideConfig.java`: absorption sound event and default-on `BrazierPickup` setting. Sound playback/throttling is not connected. Client smoke logs report SoundSystem startup failure, so audio acceptance remains open.

Next implementation boundary: connect original recipe acquisition, main-hand/repeated-click deposits, rejected item-entity handling, automation adapters and Manipulation configuration to the registered device. Source payment must occur exactly once; passing current installed-device routing tests does not establish these entry points. Do not add a persistent input buffer or new facing property. Latest installed-device checkpoint: `build/brazier-device-20260912-162501.log` (all four native suites), `build/brazier-device-artifacts.log` (all four production/source pairs); fixture-seeded ownership serialization is not native item placement or process-restart acceptance.

Required executable coverage: actual paid acquisition and installed device activation; main-hand/double-click/drop/automation deposits; full/partial/data-sensitive destinations; cache preference and revocation; X/Z boundaries and height independence; unavailable/replaced devices; exact remainders and nonmutating simulation; save/restart; connected configuration and feedback on all four targets.

Verification: the original contract is backed by pinned source reads. Incremental implementation evidence is recorded in `docs/MIGRATION_TASKS.md`; latest native routing checkpoint is `build/brazier-partial-expiry-20260912-161107.log` (all four suites pass), with artifact check `build/brazier-partial-expiry-artifacts.log`. Mixed priority/overflow, item-data rejection, copied remainder isolation, cache loss/restoration and partial-acceptance expiry are covered in native fixtures. This does not establish registered-device gameplay, real source payment, complete unload/restart, connected configuration, or rendered/audio parity. No new gameplay deviation is proposed here.
