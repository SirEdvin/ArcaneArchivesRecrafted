# Radiant Crafting Table — standard EMI transfer

## Approved scope and implementation

Decision [0113](../behavior-changes/0113-integration-scope.md) includes both recipe viewers and their relevant transfer workflows. This provides the EMI counterpart to the [restored upstream JEI integration](RADIANT_CRAFTING_JEI.md); it does not claim upstream used EMI.

`ArcaneArchivesEmi` registers the Radiant Crafting Table workstation under `VanillaEmiRecipeCategories.CRAFTING` and a `StandardRecipeHandler<RadiantCraftingMenu>` bound to the registered menu type:

- Ingredient sources: grid plus player inventory, menu slots 1–45.
- Fill destinations: nine grid slots, 1–9.
- Native output: slot 0, exposed separately, never an ingredient source or fill destination.
- Bookmark ghosts 46–48: excluded.
- Supported definitions: crafting category with recipe-tree support, matching the pinned native crafting handler's predicate.

The actual pinned EMI APIs and native crafting-handler bytecode were inspected for `YbXDHf8W`, `Axuu9I9R`, `on5GT1qh`, and `5sIPA1To`. There is no new packet, dependency, inventory framework, or custom output-grant path. EMI's standard implementation performs transfer; the ordinary server menu still owns crafting. The Gem Cutter adapter remains display-only, with no Gem Cutter transfer handler.

## Four-target validation

- `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0, 81 seconds, `build/emi-transfer-20260911-130153.log`.
- After the tag-label correction below, the same command passed again: exit 0, 78 seconds, `build/emi-transfer-labels-20260911-131318.log`.
- Both builds ran all four required native suites successfully: 7/8/7/7 tests. Both final Fabric XML reports contain seven cases without failure/error/skipped children. No ERROR/FATAL/FAILED lines in either build log.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, reported duration 0 seconds, `build/emi-transfer-labels-artifacts.log`. All four production/source pairs pass. The new nested handler is explicitly included in the exact production-class inventory; fixture/dependency isolation checks remain intact.

The default native suites do not exercise installed EMI transfer. That runtime evidence is separate below.

## Real installed-client acceptance — Fabric 1.21.1

Used a private Xvfb client with only EMI selected through `ORG_GRADLE_PROJECT_optionalIntegrationMods=emi`, and an isolated copy of the earlier Gem Cutter test world named `EMI transfer verification`. The original archived fixture and unrelated development worlds were not modified.

Command inside the private display:

`timeout --foreground 10m ./gradlew :1.21.1-fabric:runClient --args='--quickPlaySingleplayer "EMI transfer verification"' --no-daemon --console=plain`

1. Granted one Radiant Crafting Table and four Oak Planks, placed the table normally and opened its actual screen. Four pre-existing Lanterns served as an unrelated-inventory control.
2. Searched EMI for Crafting Table. The vanilla recipe displayed the Radiant Crafting Table workstation and an enabled Fill Recipe button.
3. Clicked Fill Recipe. Native block NBT confirmed one Oak Plank at each block-inventory index 0, 1, 3 and 4. The player's inventory contained only the four Lanterns. Transfer provided a normal craftable preview, not a granted output.
4. Took the native output into player inventory. The grid emptied and exactly one vanilla Crafting Table appeared.
5. Reopened the recipe and attempted another transfer. EMI showed **Not enough ingredients to craft**. Native queries confirmed an empty table, four Lanterns and exactly one Crafting Table; no repeated unpaid output or bookmark mutation.
6. Closed normally. A second real client/integrated-server process reopened the same fixture after the language correction below and confirmed the same exact empty-grid/player-inventory state.

First client: exit 0, 258.46 seconds, `build/emi-transfer-live-1789131834975351253.log`. Second client: exit 0, 73.2 seconds, `build/emi-transfer-live-1789132491046913715.log`. Both logs contain normal stopping, all-dimension saves and successful Gradle completion. No project JVMs remain. The owned fixture copy was archived after shutdown.

## Ingredient tag labels

The first run exposed 18 untranslated Arcane ingredient tags in EMI development mode. Added English `tag.item.arcanearchives.ingredients.*` names, checking the actual tag contents and pinned `EmiTagKey`/`EmiUtil.translateId` key construction. No tag memberships or recipes changed, and no warnings were suppressed.

The second client completed EMI plugin/tag/recipe reload without untranslated-tag errors or the prior warning badge. The known unavailable headless SoundSystem was the only ERROR in that final client log; this is not clean-audio acceptance.

Screenshots, temporary driver and owned world archive: `/tmp/arcane-emi-transfer-live-1789131819479703679/`:

- `emi-fill-button.png`
- `emi-filled-grid.png`
- `emi-crafted-once.png`
- `emi-missing-items-refused.png`
- `emi-labels-reloaded.png`
- `EMI transfer verification/`

## Remaining acceptance

Other three leaves' real-client transfer, combined JEI/EMI behavior, occupied/full inventories, maximum transfer, broader hover/reload workflows, remote multiplayer and protection compatibility remain open. Neither four-leaf compilation nor this single-client scenario closes the broader migration/integration parent task.
