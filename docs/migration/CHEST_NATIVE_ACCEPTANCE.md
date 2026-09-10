# Radiant Chest native storage acceptance

## Scope

The native chest implementation is unchanged. This slice adds `scripts/smoke_radiant_chest.py` and three Python fixture-contract tests. It verifies actual vanilla hopper transfers, separate-process world restart and native destruction/drop behavior on all four supported leaves. It does not claim complete chest or migration parity.

Pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`: `RadiantChestTileEntity` owns 54 extended slots, exposes its inventory capability for automation and persists contents/name/display; `RadiantChest.breakBlock` drops its inventory. The modern implementation was traced through `RadiantChest`, `RadiantChestBlockEntity`, `ExtendedItemStackHandler` and `ChestFabricStorage` before exercising those paths.

## Fixtures and safety

- Require existing accepted EULA and loopback binding; never edit EULA or server settings. Require default `RadiantMultiplier=4` instead of changing capacity configuration.
- Require the complete 5×5×5 fixture region (x/z 3–7, y 298–302) to be air and free of entities before mutations. Use a unique scoreboard objective and preserve any existing force-load on chunk 0,0.
- Seed 255 diamonds in slot 0 and 130 papers carrying test custom data in slot 53, plus owner UUID, name and display metadata. This is command-injected state, not player acquisition/menu coverage.
- A real hopper inserts two diamonds: slot 0 reaches 256, slot 1 gets one, and the hopper empties. Bounded command observation intervals allow native hopper ticks; failure to transfer within the interval fails acceptance.
- Save/stop/restart a separate server process and assert both diamond counts, last-slot paper count/custom data, owner UUID, name, display direction and display item data.
- A real lower hopper extracts diamonds. Lock it with redstone, then sum remaining chest slots and extracted inventory to verify conservation of 257 diamonds.
- Destroy the loaded hopper and chest. Sum native item-entity counts, requiring 257 diamonds, 130 papers, one chest and one hopper. Verify native stack limits and preservation of custom data on every paper drop.
- Only after destruction, freeze/tag test drops and disable item merging with the native sentinel pickup delay to stabilize multi-command counting. Delete only tagged drops, restore fixture blocks to air, remove the temporary objective and remove only the fixture-added force-load.
- Run the cleanup session even when the setup session returns failed assertions. Crashes can still leave a fixture; each wrapper log includes its objective and exact commands for recovery.

Initial run `build/chest-server-1.21.1-fabric-20260910-122056.log` failed the diamond-drop assertion (exit 1, 52s). This exposed a fixture defect: vanilla `setblock ... air` replacement clears a vanilla container's contents. Changing the loaded hopper removal to `air destroy` preserved and counted its extracted contents; no production chest fix or altered conservation expectation was needed. Three Python tests now cover guards/force-load ownership, versioned item data and cleanup after failed setup assertions.

## Execution evidence

- Build: `timeout --foreground 10m ./gradlew build --no-daemon`; exit 0, 27s, `build/chest-baseline-20260910-122237.log`.
- Fixture suite: bounded 10-minute Python wrapper ran the three unittest cases and `smoke_radiant_chest.run(node)` for all four leaves; exit 0, 231s, `build/chest-acceptance-20260910-122348.log`. Re-run each leaf with `timeout --foreground 10m python3 scripts/smoke_radiant_chest.py <leaf>` and tests with `python3 -m unittest discover -s scripts -p test_smoke_radiant_chest.py`.
- Eight independent server JSON reports were parsed, with exactly two successful sessions per leaf. Aggregated results: `build/chest-acceptance.json`; each records its command, duration, exit code, complete log and checked markers. All cleanup markers passed.
- Artifact audit: `timeout --foreground 60s python3 scripts/verify_artifacts.py`; exit 0, under 1s, `build/chest-artifacts-20260910-122808.log`; all four production/source pairs pass.
- XML totals (tests/failures/errors/skips): Fabric 1.20.1 `196/0/0/0`, Forge standalone `36/0/0/0`, Forge native runtime `193/0/0/0`, Fabric 1.21.1 `196/0/0/0`, NeoForge `273/0/0/0`. Forge standalone coverage overlaps its runtime suite.
- No Arcane Archives development game JVM remains; an unrelated pre-existing UnlimitedPeripheralWorks data-generation JVM was left untouched. Whitespace checks pass.

## Remaining limits

No connected-player placement, menus/quick-move, display editing/rendering, comparator behavior, permission revocation, networking, multiplayer concurrency, all-side automation or full-inventory rejection claim. No alternate Stonecutter switch was necessary: no shared Java/resources/build code changed.

Forge startup still emits an existing Mixin configuration diagnostic: `arcanearchives.forge.mixins.json does not specify "minVersion" property`. Both sessions continue successfully and all gameplay markers pass, but these are not clean-log acceptance. The other six logs contain no ERROR/command-syntax diagnostics. This warning is separate follow-up work, not silently ignored or repaired in an unrelated storage fixture.

Follow-up: [Mixin metadata repair](MIXIN_METADATA.md) resolves this diagnostic across the four-target server/client startup matrix and adds artifact/runtime regression checks. The original chest logs above remain historical evidence, not new clean-log results.
