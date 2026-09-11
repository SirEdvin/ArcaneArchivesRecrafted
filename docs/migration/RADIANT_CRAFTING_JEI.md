# Radiant Crafting Table — native JEI transfer

## Source and scope

Restores the existing upstream JEI behavior under approved integration scope 0113. At `80944ce45c6559243d8928cc4b305bf379388652`:

- `integration/jei/JEIPlugin.java:57–58` registers the Radiant Crafting Table transfer handler and vanilla crafting catalyst.
- `integration/jei/CraftingStationRecipeTransferInfo.java:36–53` exposes crafting slots 1–9 and player inventory after the grid, excluding the final three bookmark slots.

The migrated `RadiantCraftingMenu` has output slot 0, grid slots 1–9, player slots 10–45 and bookmark ghosts 46–48. `ArcaneArchivesJei.registerRecipeTransferHandlers` now uses JEI's native standard handler with recipe start/count `1, 9` and inventory start/count `10, 36`, bound to the registered menu type and `RecipeTypes.CRAFTING`. The table is also registered as a vanilla-crafting catalyst.

No bespoke transfer packets, item mutation service, recipe framework, dependency or publication were added. The adapter fills the ordinary crafting grid; it does not extract or grant crafted output. The native menu remains responsible for crafting. Gem Cutter recipe display is unchanged and has no transfer handler. The subsequently implemented EMI counterpart has [separate evidence](RADIANT_CRAFTING_EMI.md).

## Pinned API inspection

Inspected the actual resolved JEI API bytecode for all current pins: `jJOr2rUn`, `9jqubC9n`, `EO4u1E2G`, `UJRXzDfp`. Every pin exposes the menu-class/menu-type/recipe-type/four-integer overload of `IRecipeTransferRegistration.addRecipeTransferHandler`. The native `BasicRecipeTransferInfo` constructor/slot enumeration confirmed these integers mean starts and counts, not end indices. `RecipeTypes.CRAFTING` supplies the version-correct recipe versus recipe-holder type without a custom cross-version wrapper.

## Build and artifacts

- `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0, 233 seconds, `build/jei-transfer-20260911-124933.log`. All four required native suites pass (7/8/7/7), both Fabric XML crafting cases pass, no ERROR/FATAL/FAILED lines in this build log.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, reported duration 0 seconds, `build/jei-transfer-artifacts.log`. All four production/source pairs pass. No test/dependency packaging relaxation.

These default native suites do not exercise JEI transfer itself. The real installed-client scenario below provides separate runtime evidence.

## Real installed-client scenario — Fabric 1.21.1

Ran a private Xvfb development client with JEI only via `ORG_GRADLE_PROJECT_optionalIntegrationMods=jei`. The log confirms JEI `19.51.0.418`. Used a separate copy of the previously isolated Gem Cutter test world, folder `JEI transfer verification`; did not modify the archived original or other development worlds.

1. Granted one Radiant Crafting Table and four Oak Planks through native commands, placed the table by normal right-click, and opened its actual screen. The existing four Lanterns were an unrelated-inventory conservation control.
2. Searched JEI for Crafting Table, opened the vanilla recipe, and observed the Radiant Crafting Table catalyst and enabled Move Items button.
3. Clicked Move Items. Four single Planks appeared in the crafting grid. Native `/data get block 5 250 4 Items` confirmed block inventory indices 0, 1, 3, 4 each contained exactly one Oak Plank. Player inventory then contained only the four unrelated Lanterns. Output remained a normal craftable preview; transfer itself did not grant the Crafting Table.
4. Took the native output and placed it in player inventory. The grid and output cleared. An initial click before the reopened screen finished synchronizing did nothing; after fresh visual inspection, ordinary pickup succeeded.
5. Reopened the JEI recipe and attempted another transfer without ingredients. JEI displayed **Missing Items**, and no second craft was supplied.
6. Final native queries confirmed an empty table inventory and exactly four Lanterns plus one vanilla Crafting Table in player inventory. Bookmark ghosts were excluded from transfer and remained empty.

Command inside the private display:

`timeout --foreground 10m ./gradlew :1.21.1-fabric:runClient --args='--quickPlaySingleplayer "JEI transfer verification"' --no-daemon --console=plain`

Process exit 0, measured duration 243.28 seconds. Full log: `build/jei-transfer-live-1789131245043438857.log`. Normal client stopping, all-dimension save and successful Gradle exit were verified. The only ERROR line was the known unavailable headless SoundSystem; no audio or universally clean startup claim. No project JVMs remain; the private authority file was removed by display shutdown.

Screenshots, temporary driver and the exact fixture copy were archived outside the repo at `/tmp/arcane-jei-transfer-live-1789131230273351367/`:

- `jei-catalyst-and-transfer.png`
- `jei-filled-grid.png`
- `jei-crafted-once.png`
- `jei-missing-items-refused.png`
- `JEI transfer verification/`

## Remaining verification

Other three loaders' connected transfer behavior, combined viewers, full inventory/occupied-grid transfers, maximum transfer, remote multiplayer/protection compatibility and broader migration remain open. EMI's separate Fabric 1.21.1 scenario does not establish viewer coexistence. The implementation is four-leaf compiled, but the live transfer/crafting scenario is verified on Fabric 1.21.1 only.
