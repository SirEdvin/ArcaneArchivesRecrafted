# Upstream source inventory

Baseline: `80944ce45c6559243d8928cc4b305bf379388652`. Paths below are relative to upstream `src/main/java/com/aranaira/arcanearchives/`. This is a source index, NOT a claim that every class is registered, complete or shipped. Resolve registry reachability in migration phase 1. For each feature, record IDs, defaults, behavior fixtures and tests before checking its migration task complete.

Handoff: [HANDOFF.md](HANDOFF.md). All 332 Java paths were rechecked against the pinned Git tree with no missing, duplicate or extra entries. Unchecked entries may be partially traced or ported; annotations below distinguish partial progress from full behavioral acceptance. Dormant/unregistered features stay documented and disabled under the approved scope.

## Root classes

- [ ] `AAGuiHandler.java` — trace reachability and assign parity coverage.
- [ ] `ArcaneArchives.java` — trace reachability and assign parity coverage.
- [ ] `CreativeTabAA.java` — shaped-quartz icon/title and the two ported item entries are implemented in modern `init/ContentRegistry.java`; full inventory/order and interactive coverage remain pending. See `migration/QUARTZ_CONTENT.md`.
- [ ] `ExportDocumentation.java` — trace reachability and assign parity coverage.

## api

- [ ] `api/GCTRecipeEvent.java` — trace reachability and assign parity coverage.
- [ ] `api/IArcaneArchivesRecipe.java` — trace reachability and assign parity coverage.
- [ ] `api/IGCTRecipe.java` — trace reachability and assign parity coverage.
- [ ] `api/IGCTRecipeList.java` — trace reachability and assign parity coverage.
- [ ] `api/RecipeIngredientHandler.java` — trace reachability and assign parity coverage.
- [ ] `api/immanence/IImmanenceBus.java` — trace reachability and assign parity coverage.
- [ ] `api/immanence/IImmanenceConsumer.java` — trace reachability and assign parity coverage.
- [ ] `api/immanence/IImmanenceGenerator.java` — trace reachability and assign parity coverage.
- [ ] `api/immanence/IImmanenceSource.java` — trace reachability and assign parity coverage.
- [ ] `api/immanence/IImmanenceSubscriber.java` — trace reachability and assign parity coverage.
- [ ] `api/immanence/ImmanenceBonusType.java` — trace reachability and assign parity coverage.

## blocks

- [ ] `blocks/Brazier.java` — trace reachability and assign parity coverage.
- [x] `blocks/CelestialLotusEngine.java` — registered development-baseline block restored with original OBJ/light/collision/tooltip and inherited hand-harvest permission, stone sound and SOLID rendering. No generator or block entity is invented. Four-target native placement/loot/restart, client resource checks, registered-content tests and native transform parsing pass; connected-player acceptance remains open. See `behavior-changes/0108-celestial-lotus-engine.md`.
- [x] `blocks/EchoingConformanceChamber.java` — registered prototype restored without dormant machinery; original OBJ/warnings/properties, four-target native placement/loot/restart and client resource checks. See `behavior-changes/0106-registered-unfinished-devices.md`; connected visuals remain open.
- [x] `blocks/EchoingReverberationChamber.java` — registered prototype restored without dormant duplication; original OBJ/warnings/properties, four-target native placement/loot/restart and client resource checks. See `behavior-changes/0106-registered-unfinished-devices.md`; connected visuals remain open.
- [ ] `blocks/FakeAir.java` — trace reachability and assign parity coverage.
- [ ] `blocks/GemCuttersTable.java` — trace reachability and assign parity coverage.
- [ ] `blocks/IHasModel.java` — trace reachability and assign parity coverage.
- [x] `blocks/ImmanentIncubator.java` — registered nonfunctional placeholder cube restored with original warnings/light/hardness; four-target native placement/loot/item/restart and client resource checks pass. See `behavior-changes/0106-registered-unfinished-devices.md`; connected input/visual acceptance remains open.
- [ ] `blocks/LecternManifest.java` — trace reachability and assign parity coverage.
- [ ] `blocks/MatrixCrystalCore.java` — trace reachability and assign parity coverage.
- [ ] `blocks/MatrixDistillate.java` — trace reachability and assign parity coverage.
- [ ] `blocks/MatrixRepository.java` — trace reachability and assign parity coverage.
- [ ] `blocks/MatrixReservoir.java` — trace reachability and assign parity coverage.
- [ ] `blocks/MatrixStorage.java` — trace reachability and assign parity coverage.
- [ ] `blocks/MonitoringCrystal.java` — trace reachability and assign parity coverage.
- [ ] `blocks/MultiblockSize.java` — trace reachability and assign parity coverage.
- [ ] `blocks/QuartzSliver.java` — trace reachability and assign parity coverage.
- [ ] `blocks/RadiantChest.java` — implemented; native destruction/drop conservation and hopper/restart acceptance pass on all four leaves (`migration/CHEST_NATIVE_ACCEPTANCE.md`). Connected-player/menu/rendering and broader integration parity remain open.
- [ ] `blocks/RadiantCraftingTable.java` — trace reachability and assign parity coverage.
- [x] `blocks/RadiantFurnace.java` — explicitly excluded by user decision; see `behavior-changes/0110-radiant-furnace-exclusion.md`. Scope closure, not implementation/parity.
- [ ] `blocks/RadiantLantern.java` — trace reachability and assign parity coverage.
- [ ] `blocks/RadiantResonator.java` — trace reachability and assign parity coverage.
- [ ] `blocks/RadiantTank.java` — trace reachability and assign parity coverage.
- [ ] `blocks/RadiantTrove.java` — trace reachability and assign parity coverage.
- [ ] `blocks/RawQuartzCluster.java` — trace reachability and assign parity coverage.
- [x] `blocks/SpellbookLibrary.java` — registered nonfunctional placeholder cube restored with original warnings/light/hardness; four-target native placement/loot/item/restart and client resource checks pass. See `behavior-changes/0106-registered-unfinished-devices.md`; no book inventory or generation invented.
- [ ] `blocks/StorageRawQuartz.java` — trace reachability and assign parity coverage.
- [ ] `blocks/StorageShapedQuartz.java` — ported and registered; all-leaf placement/tag/loot and model checks pass. Player mining, lighting/explosions, interactive visuals and restart persistence remain pending; see `migration/QUARTZ_CONTENT.md`.
- [x] `blocks/VerdantCenser.java` — registered prototype restored with original OBJ/warnings/properties; four-target native placement/loot/item/restart and client resource checks pass. See `behavior-changes/0106-registered-unfinished-devices.md`; no generation invented and connected visuals remain open.
- [x] `blocks/WonkyResonator.java` — registered development content restored with original assets, creative item/tool/self-drop behavior; four-target client resource loading and native server checks pass. See `behavior-changes/0105-wonky-resonator-runtime.md`; interactive visual acceptance remains open.
- [ ] `blocks/modelparts/brazier/BrazierFire.java` — trace reachability and assign parity coverage.
- [ ] `blocks/templates/BlockDirectionalTemplate.java` — trace reachability and assign parity coverage.
- [ ] `blocks/templates/BlockTemplate.java` — quartz-relevant defaults traced and adapted directly to modern Block properties; the general template is not ported. See `migration/QUARTZ_CONTENT.md`.

## client

- [ ] `client/CycleTimer.java` — trace reachability and assign parity coverage.
- [ ] `client/Keybinds.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/AbstractGuiContainerTracking.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/GUIBookContainer.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/GUIBrazier.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/GUIDevouringCharm.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/GUIGemCuttersTable.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/GUIGemSocket.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/GUIManifest.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/GUIRadiantChest.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/GUIRadiantCraftingTable.java` — trace reachability and assign parity coverage.
- [x] `client/gui/GUIRadiantFurnace.java` — explicitly excluded with Radiant Furnace; see `behavior-changes/0110-radiant-furnace-exclusion.md`. Not implementation/parity.
- [ ] `client/gui/GUIUpgrades.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/GUIUtils.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/controls/InvisibleButton.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/controls/ManifestSearchField.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/controls/RightClickTextField.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/controls/ScrollBar.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/controls/TexturedButton.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/framework/CustomCountSlot.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/framework/IScrollabe.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/framework/IScrollableContainer.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/framework/LayeredButton.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/framework/LayeredGuiContainer.java` — trace reachability and assign parity coverage.
- [ ] `client/gui/framework/ScrollEventManager.java` — trace reachability and assign parity coverage.
- [ ] `client/particles/ParticleGenerator.java` — trace reachability and assign parity coverage.
- [ ] `client/particles/particle/ParticlePendeloqueGemLine.java` — trace reachability and assign parity coverage.
- [ ] `client/render/BrazierTESR.java` — trace reachability and assign parity coverage.
- [ ] `client/render/LineHandler.java` — trace reachability and assign parity coverage.
- [ ] `client/render/MultiVertexLine.java` — trace reachability and assign parity coverage.
- [ ] `client/render/RadiantChestTESR.java` — trace reachability and assign parity coverage.
- [ ] `client/render/RadiantTankTEISR.java` — trace reachability and assign parity coverage.
- [ ] `client/render/RadiantTankTESR.java` — trace reachability and assign parity coverage.
- [ ] `client/render/RadiantTroveTESR.java` — trace reachability and assign parity coverage.
- [ ] `client/render/RenderGemcasting.java` — trace reachability and assign parity coverage.
- [ ] `client/render/RenderHUD.java` — trace reachability and assign parity coverage.
- [ ] `client/render/RenderItemExtended.java` — trace reachability and assign parity coverage.
- [ ] `client/render/RenderUtils.java` — trace reachability and assign parity coverage.
- [ ] `client/render/entity/RenderWeight.java` — trace reachability and assign parity coverage.

## commands

- [ ] `commands/CommandBrazier.java` — trace reachability and assign parity coverage.
- [ ] `commands/CommandCopy.java` — trace reachability and assign parity coverage.
- [ ] `commands/CommandHive.java` — trace reachability and assign parity coverage.
- [ ] `commands/CommandImmanence.java` — trace reachability and assign parity coverage.
- [ ] `commands/CommandRebuild.java` — trace reachability and assign parity coverage.
- [ ] `commands/CommandTiles.java` — trace reachability and assign parity coverage.

## config

- [ ] `config/ConfigHandler.java` — trace reachability and assign parity coverage.
- [ ] `config/ManifestConfig.java` — trace reachability and assign parity coverage.
- [ ] `config/NonModTrackingConfig.java` — trace reachability and assign parity coverage.
- [ ] `config/ServerSideConfig.java` — upstream defaults/range and loader-directory properties loading implemented with focused tests. Server/client synchronization, reload and gameplay consumers remain pending; see `migration/IMPLEMENTATION_STATUS.md` and `migration/TOME_PARITY.md`.

## core

- [ ] `core/AALoadingPlugin.java` — trace reachability and assign parity coverage.

## data

- [ ] `data/AccessorSaveData.java` — trace reachability and assign parity coverage.
- [ ] `data/DataHelper.java` — trace reachability and assign parity coverage.
- [ ] `data/HiveSaveData.java` — trace reachability and assign parity coverage.
- [ ] `data/NetworkSaveData.java` — trace reachability and assign parity coverage.
- [ ] `data/PlayerSaveData.java` — UUID-keyed overworld SavedData implementation, receipt dirty marking and NBT fixtures now exist. Acquisition wiring, actual new-world restart and multiplayer verification remain pending; see `migration/IMPLEMENTATION_STATUS.md`.
- [ ] `data/types/ClientNetwork.java` — trace reachability and assign parity coverage.
- [ ] `data/types/HiveMembershipInfo.java` — trace reachability and assign parity coverage.
- [ ] `data/types/HiveNetwork.java` — trace reachability and assign parity coverage.
- [ ] `data/types/IHiveBase.java` — trace reachability and assign parity coverage.
- [ ] `data/types/IServerNetwork.java` — trace reachability and assign parity coverage.
- [ ] `data/types/InvalidNetworkException.java` — trace reachability and assign parity coverage.
- [ ] `data/types/ServerList.java` — trace reachability and assign parity coverage.
- [ ] `data/types/ServerNetwork.java` — trace reachability and assign parity coverage.
- [ ] `data/types/SynchroniseInfo.java` — trace reachability and assign parity coverage.

## entity

- [ ] `entity/EntityItemMountaintear.java` — trace reachability and assign parity coverage.
- [ ] `entity/EntityWeight.java` — trace reachability and assign parity coverage.
- [ ] `entity/ai/AIResonatorSit.java` — trace reachability and assign parity coverage.

## events

- [ ] `events/AnvilHandler.java` — trace reachability and assign parity coverage.
- [ ] `events/ClientTickHandler.java` — trace reachability and assign parity coverage.
- [ ] `events/EventHandler.java` — tome registration, bookshelf/resonator grants and crafted-tome receipt traced in `migration/TOME_PARITY.md`, including save-before-spawn failure risk. These routes and the remaining event behavior are not ported.
- [ ] `events/NetworksHandler.java` — trace reachability and assign parity coverage.
- [ ] `events/ServerTickHandler.java` — trace reachability and assign parity coverage.
- Out of scope: `events/mappings/MappingHandler.java` — legacy saved-ID remaps are not ported under the user-approved fresh-world-only policy; aliases remain historical evidence in `migration/registry-baseline.json`.

## immanence

- [ ] `immanence/ImmanenceBus.java` — trace reachability and assign parity coverage.
- [ ] `immanence/ImmanenceGlobal.java` — trace reachability and assign parity coverage.
- [ ] `immanence/ImmanenceSource.java` — trace reachability and assign parity coverage.

## init

- [ ] `init/BlockRegistry.java` — static registration/ID audit in `migration/registry-baseline.json`; modern registration currently covers shaped-quartz storage only. Full content and reachability remain pending.
- [ ] `init/ItemRegistry.java` — static registration/ID audit in `migration/registry-baseline.json`; modern registration currently covers shaped quartz and its storage block item only. Full content and reachability remain pending.
- [ ] `init/RecipeLibrary.java` — shaped-quartz acquisition, Arsenal recipe gates and player-conditioned Gem Cutter recipe examples traced in `migration/TOME_PARITY.md`; full recipe inventory and implementation remain pending.
- [ ] `init/SoundRegistry.java` — trace reachability and assign parity coverage.

## integration

- [ ] `integration/astralsorcery/Liquefaction.java` — trace reachability and assign parity coverage.
- [ ] `integration/baubles/BaubleBodyCapabilityHandler.java` — trace reachability and assign parity coverage.
- [ ] `integration/baubles/BaubleGemUtil.java` — trace reachability and assign parity coverage.
- [ ] `integration/craftingtweaks/CraftingTweaks.java` — trace reachability and assign parity coverage.
- [ ] `integration/crafttweaker/Action.java` — trace reachability and assign parity coverage.
- [ ] `integration/crafttweaker/GCTTweaker.java` — trace reachability and assign parity coverage.
- [ ] `integration/guidebook/GBookInit.java` — replacement backend approved and externally wired; tome, local and included standard-template bodies inventoried. Original background/provider registration traced in `migration/TOME_PARITY.md`; actual conversion and client interactions remain pending.
- [ ] `integration/guidebook/GCTRecipeProvider.java` — replacement API-only Patchouli component, native ordered output/key lookup, counted alternatives/output copies, original texture and two templates implemented in `behavior-changes/0112-tome-gem-cutter-recipe-display.md`. Shared lookup and native variable/template tests pass; full Tome page integration and connected-player rendering/reload remain pending. Not replaced with a generic crafting grid.
- [ ] `integration/hwyla/WAILAPlugin.java` — trace reachability and assign parity coverage.
- [ ] `integration/hwyla/providers/ProviderRadiantChest.java` — trace reachability and assign parity coverage.
- [ ] `integration/hwyla/providers/ProviderRadiantTrove.java` — trace reachability and assign parity coverage.
- [ ] `integration/hwyla/providers/ProviderResonator.java` — trace reachability and assign parity coverage.
- [ ] `integration/jei/CraftingStationRecipeTransferInfo.java` — trace reachability and assign parity coverage.
- [ ] `integration/jei/JEIPlugin.java` — trace reachability and assign parity coverage.
- [ ] `integration/jei/JEIUnderMouse.java` — trace reachability and assign parity coverage.
- [ ] `integration/jei/gct/GCTCategory.java` — trace reachability and assign parity coverage.
- [ ] `integration/jei/gct/GCTWrapper.java` — trace reachability and assign parity coverage.
- [ ] `integration/jei/quartz/QuartzCategory.java` — trace reachability and assign parity coverage.
- [ ] `integration/jei/quartz/QuartzWrapper.java` — trace reachability and assign parity coverage.
- [ ] `integration/patchouli/TestProcessor.java` — trace reachability and assign parity coverage.
- [ ] `integration/thaumcraft/AspectRegistry.java` — trace reachability and assign parity coverage.
- [ ] `integration/top/TOPPlugin.java` — trace reachability and assign parity coverage.

## inventory

- [ ] `inventory/ContainerBrazier.java` — trace reachability and assign parity coverage.
- [ ] `inventory/ContainerDevouringCharm.java` — trace reachability and assign parity coverage.
- [ ] `inventory/ContainerFakeManifest.java` — trace reachability and assign parity coverage.
- [ ] `inventory/ContainerGemCuttersTable.java` — trace reachability and assign parity coverage.
- [ ] `inventory/ContainerGemSocket.java` — trace reachability and assign parity coverage.
- [ ] `inventory/ContainerManifest.java` — trace reachability and assign parity coverage.
- [ ] `inventory/ContainerRadiantChest.java` — trace reachability and assign parity coverage.
- [ ] `inventory/ContainerRadiantCraftingTable.java` — trace reachability and assign parity coverage.
- [x] `inventory/ContainerRadiantFurnace.java` — explicitly excluded with Radiant Furnace; see `behavior-changes/0110-radiant-furnace-exclusion.md`. Not implementation/parity.
- [ ] `inventory/ContainerUpgrades.java` — trace reachability and assign parity coverage.
- [ ] `inventory/handlers/DevouringCharmHandler.java` — trace reachability and assign parity coverage.
- [ ] `inventory/handlers/ExtendedItemStackHandler.java` — shared implementation with Forge/NeoForge item-handler interfaces, approved input/simulation guards and component-preserving extended-count persistence. Fabric fixtures pass; loader-aware Forge/NeoForge execution, Fabric transfers and chest consumers remain pending.
- [ ] `inventory/handlers/GemSocketHandler.java` — trace reachability and assign parity coverage.
- [ ] `inventory/handlers/ITrackingHandler.java` — trace reachability and assign parity coverage.
- [ ] `inventory/handlers/ITroveItemHandler.java` — trace reachability and assign parity coverage.
- [ ] `inventory/handlers/InventoryCraftingItemHandler.java` — trace reachability and assign parity coverage.
- [ ] `inventory/handlers/InventoryCraftingPersistent.java` — trace reachability and assign parity coverage.
- [ ] `inventory/handlers/ItemStackWrapper.java` — trace reachability and assign parity coverage.
- [ ] `inventory/handlers/ManifestItemHandler.java` — trace reachability and assign parity coverage.
- [ ] `inventory/handlers/OptionalUpgradesHandler.java` — trace reachability and assign parity coverage.
- [ ] `inventory/handlers/SizeUpgradeItemHandler.java` — shared ordered-slot implementation with proposal-0004 empty/occupied-slot guards. Tank/Trove item bindings and capacity consumers remain pending; original inclusive capacity callback argument is retained for separate review before enabling consumers.
- [ ] `inventory/handlers/TankItemFluidHandler.java` — trace reachability and assign parity coverage.
- [ ] `inventory/handlers/TankUpgradeItemHandler.java` — trace reachability and assign parity coverage.
- [ ] `inventory/handlers/TroveItemBlockItemHandler.java` — trace reachability and assign parity coverage.
- [ ] `inventory/handlers/TroveUpgradeItemHandler.java` — trace reachability and assign parity coverage.
- [ ] `inventory/slots/SlotCraftingFastWorkbench.java` — trace reachability and assign parity coverage.
- [ ] `inventory/slots/SlotExtended.java` — trace reachability and assign parity coverage.
- [ ] `inventory/slots/SlotImmutable.java` — trace reachability and assign parity coverage.
- [ ] `inventory/slots/SlotRecipeHandler.java` — trace reachability and assign parity coverage.

## items

- [ ] `items/ContainmentFieldItem.java` — trace reachability and assign parity coverage.
- [ ] `items/DebugOrbItem.java` — trace reachability and assign parity coverage.
- [ ] `items/DevouringCharmItem.java` — trace reachability and assign parity coverage.
- [ ] `items/DispenseAmphora.java` — trace reachability and assign parity coverage.
- [ ] `items/EchoItem.java` — trace reachability and assign parity coverage.
- [ ] `items/EmpoweredQuartzItem.java` — trace reachability and assign parity coverage.
- [ ] `items/FabrialItem.java` — trace reachability and assign parity coverage.
- [ ] `items/GBookArsenalConditionItem.java` — trace reachability and assign parity coverage.
- [ ] `items/GemSocket.java` — trace reachability and assign parity coverage.
- [ ] `items/IUpgradeItem.java` — trace reachability and assign parity coverage.
- [ ] `items/LetterOfInvitationItem.java` — trace reachability and assign parity coverage.
- [ ] `items/LetterOfResignationItem.java` — trace reachability and assign parity coverage.
- [ ] `items/ManifestItem.java` — trace reachability and assign parity coverage.
- [ ] `items/MaterialInterfaceItem.java` — trace reachability and assign parity coverage.
- [ ] `items/MatrixBraceItem.java` — trace reachability and assign parity coverage.
- [ ] `items/ObstructionCharmItem.java` — trace reachability and assign parity coverage.
- [ ] `items/QuartzArrowItem.java` — trace reachability and assign parity coverage.
- [ ] `items/RadiantAmphoraItem.java` — trace reachability and assign parity coverage.
- [ ] `items/RadiantDustItem.java` — trace reachability and assign parity coverage.
- [ ] `items/RadiantKeyItem.java` — trace reachability and assign parity coverage.
- [ ] `items/RawQuartzItem.java` — trace reachability and assign parity coverage.
- [ ] `items/ScepterManipulationItem.java` — trace reachability and assign parity coverage.
- [ ] `items/ScepterRevelationItem.java` — trace reachability and assign parity coverage.
- [ ] `items/ScepterTranslocation.java` — trace reachability and assign parity coverage.
- [ ] `items/ScintillatingInlayItem.java` — trace reachability and assign parity coverage.
- [ ] `items/SerenityCharmItem.java` — trace reachability and assign parity coverage.
- [ ] `items/ShapedQuartzItem.java` — registered with preserved tooltip/assets; server item and client model checks pass. Raw-quartz acquisition/Gem Cutter progression and interactive coverage remain pending; see `migration/QUARTZ_CONTENT.md`.
- [ ] `items/TomeOfArcanaItem.java` — identity, bound creative stack, inherited client-only opening and non-consumption traced from the pinned embedded Guidebook sources in `migration/TOME_PARITY.md`; item/content/acquisition port remains pending.
- [ ] `items/TomeOfArcanaItemBackground.java` — trace reachability and assign parity coverage.
- [ ] `items/WritOfExpulsionItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/ArcaneGemItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/GemRechargePowder.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/GemRechargePowderRainbow.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/GemUtil.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/asscher/AgegleamItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/asscher/CleansegleamItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/asscher/MurdergleamItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/asscher/SalvegleamItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/asscher/Slaughtergleam.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/asscher/SwitchgleamItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/oval/MunchstoneItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/oval/OrderstoneItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/oval/TransferstoneItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/pampel/Elixirspindle.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/pampel/MindspindleItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/pendeloque/MountaintearItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/pendeloque/ParchtearItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/pendeloque/RivertearItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/trillion/PhoenixwayItem.java` — trace reachability and assign parity coverage.
- [ ] `items/gems/trillion/StormwayItem.java` — trace reachability and assign parity coverage.
- [ ] `items/itemblocks/MonitoringCrystalItem.java` — trace reachability and assign parity coverage.
- [ ] `items/itemblocks/RadiantTankItem.java` — trace reachability and assign parity coverage.
- [ ] `items/itemblocks/RadiantTroveItem.java` — trace reachability and assign parity coverage.
- [ ] `items/itemblocks/StorageShapedQuartzItem.java` — adapted using native modern BlockItem plus block tooltip override, not a separate ported class. Full interactive checks remain pending; see `migration/QUARTZ_CONTENT.md`.
- [ ] `items/templates/IItemScepter.java` — trace reachability and assign parity coverage.
- [ ] `items/templates/ItemBlockTemplate.java` — trace reachability and assign parity coverage.
- [ ] `items/templates/ItemMultistateTemplate.java` — trace reachability and assign parity coverage.
- [ ] `items/templates/ItemTemplate.java` — trace reachability and assign parity coverage.
- [ ] `items/templates/LetterTemplate.java` — trace reachability and assign parity coverage.
- [ ] `items/unused/GeomancyPendulumItem.java` — trace reachability and assign parity coverage.
- [ ] `items/unused/GeomanticMapItem.java` — trace reachability and assign parity coverage.
- [ ] `items/unused/ScepterAbductionItem.java` — trace reachability and assign parity coverage.
- [ ] `items/unused/ScepterManipulationItem.java` — trace reachability and assign parity coverage.
- [ ] `items/unused/ScepterTranslocationItem.java` — trace reachability and assign parity coverage.
- [ ] `items/unused/SpiritOrbItem.java` — trace reachability and assign parity coverage.
- [ ] `items/unused/TomeOfRequisitionItem.java` — trace reachability and assign parity coverage.

## mixins

- [ ] `mixins/AAMixinPlugin.java` — trace reachability and assign parity coverage.
- [ ] `mixins/MixinGuiContainer.java` — trace reachability and assign parity coverage.

## network

- [ ] `network/Handlers.java` — trace reachability and assign parity coverage.
- [ ] `network/Messages.java` — trace reachability and assign parity coverage.
- [ ] `network/Networking.java` — trace reachability and assign parity coverage.
- [ ] `network/PacketArcaneGems.java` — trace reachability and assign parity coverage.
- [ ] `network/PacketBrazier.java` — trace reachability and assign parity coverage.
- [ ] `network/PacketClipboard.java` — trace reachability and assign parity coverage.
- [ ] `network/PacketConfig.java` — trace reachability and assign parity coverage.
- [ ] `network/PacketDebug.java` — trace reachability and assign parity coverage.
- [ ] `network/PacketGemCutters.java` — trace reachability and assign parity coverage.
- [ ] `network/PacketNetworks.java` — trace reachability and assign parity coverage.
- [ ] `network/PacketRadiantAmphora.java` — trace reachability and assign parity coverage.
- [ ] `network/PacketRadiantChest.java` — trace reachability and assign parity coverage.
- [ ] `network/PacketRadiantCrafting.java` — trace reachability and assign parity coverage.

## proxy

- [ ] `proxy/ClientProxy.java` — trace reachability and assign parity coverage.
- [ ] `proxy/CommonProxy.java` — trace reachability and assign parity coverage.

## recipe

- [ ] `recipe/IngredientStack.java` — trace reachability and assign parity coverage.
- [ ] `recipe/IngredientsMatcher.java` — trace reachability and assign parity coverage.
- [ ] `recipe/fastcrafting/FastCraftingRecipe.java` — trace reachability and assign parity coverage.
- [ ] `recipe/gct/GCTRecipe.java` — trace reachability and assign parity coverage.
- [ ] `recipe/gct/GCTRecipeList.java` — trace reachability and assign parity coverage.
- [ ] `recipe/gct/GCTRecipeWithConditionsCrafter.java` — trace reachability and assign parity coverage.
- [ ] `recipe/gct/GCTRecipeWithCrafter.java` — trace reachability and assign parity coverage.

## tileentities

- [ ] `tileentities/AATileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/BrazierTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/FakeAirTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/GemCuttersTableTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/ImmanenceTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/MatrixCoreTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/MatrixRepositoryTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/MatrixStorageTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/MonitoringCrystalTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/RadiantChestTileEntity.java` — implemented; native hopper capacity/extraction, extended-count/custom-data and metadata restart pass on all four leaves (`migration/CHEST_NATIVE_ACCEPTANCE.md`). Broader ownership/network/menu/integration parity remains open.
- [ ] `tileentities/RadiantCraftingTableTileEntity.java` — trace reachability and assign parity coverage.
- [x] `tileentities/RadiantFurnaceAccessorTileEntity.java` — explicitly excluded with Radiant Furnace; see `behavior-changes/0110-radiant-furnace-exclusion.md`. Not implementation/parity.
- [x] `tileentities/RadiantFurnaceTileEntity.java` — explicitly excluded with Radiant Furnace; see `behavior-changes/0110-radiant-furnace-exclusion.md`. Not implementation/parity.
- [ ] `tileentities/RadiantResonatorTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/RadiantTankTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/RadiantTroveTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/ReverberationChamberTileEntity.java` — trace reachability and assign parity coverage.
- [x] `tileentities/WonkyResonatorTileEntity.java` — original timer/pause/reset and `current_tick` persistence restored; three fixtures per target and real save/restart checks on all four targets pass. Upstream explosion TODO intentionally remains a no-op. See `behavior-changes/0105-wonky-resonator-runtime.md`.
- [ ] `tileentities/interfaces/IAccessorTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/interfaces/IBrazierRouting.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/interfaces/IDirectionalTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/interfaces/IManifestTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/interfaces/INamedTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/interfaces/ISizedTileEntity.java` — trace reachability and assign parity coverage.
- [ ] `tileentities/interfaces/IUpgradeableStorage.java` — trace reachability and assign parity coverage.

## types

- [ ] `types/BlockPosDimension.java` — trace reachability and assign parity coverage.
- [ ] `types/ISerializeByteBuf.java` — trace reachability and assign parity coverage.
- [ ] `types/IteRef.java` — trace reachability and assign parity coverage.
- [ ] `types/MachineSound.java` — trace reachability and assign parity coverage.
- [ ] `types/enums/UpgradeType.java` — trace reachability and assign parity coverage.
- [ ] `types/iterators/ListConcatIterable.java` — trace reachability and assign parity coverage.
- [ ] `types/iterators/SlotIterable.java` — trace reachability and assign parity coverage.
- [ ] `types/iterators/TileListIterable.java` — trace reachability and assign parity coverage.
- [ ] `types/lists/CombinedTileList.java` — trace reachability and assign parity coverage.
- [ ] `types/lists/ITileList.java` — trace reachability and assign parity coverage.
- [ ] `types/lists/ManifestList.java` — trace reachability and assign parity coverage.
- [ ] `types/lists/ReferenceList.java` — trace reachability and assign parity coverage.
- [ ] `types/lists/TileList.java` — trace reachability and assign parity coverage.

## util

- [ ] `util/ByteUtils.java` — trace reachability and assign parity coverage.
- [ ] `util/ColorUtils.java` — trace reachability and assign parity coverage.
- [ ] `util/DropUtils.java` — trace reachability and assign parity coverage.
- [ ] `util/DuplicationUtils.java` — trace reachability and assign parity coverage.
- [ ] `util/InventoryRoutingUtils.java` — trace reachability and assign parity coverage.
- [ ] `util/ItemUtils.java` — trace reachability and assign parity coverage.
- [ ] `util/KeyboardUtil.java` — trace reachability and assign parity coverage.
- [ ] `util/ManifestTrackingUtils.java` — trace reachability and assign parity coverage.
- [ ] `util/ManifestUtils.java` — trace reachability and assign parity coverage.
- [x] `util/MathUtils.java` — ported with mapped Minecraft classes; caller trace and 11 cross-target JUnit cases documented in `migration/FOUNDATION.md`. Parent Manifest/HUD features remain unported.
- [ ] `util/NBTUtils.java` — trace reachability and assign parity coverage.
- [ ] `util/NetworkUtils.java` — trace reachability and assign parity coverage.
- [ ] `util/PlayerUtil.java` — trace reachability and assign parity coverage.
- [ ] `util/RayTracingUtils.java` — trace reachability and assign parity coverage.
- [ ] `util/StringHelper.java` — trace reachability and assign parity coverage.
- [ ] `util/TileUtils.java` — trace reachability and assign parity coverage.
- [ ] `util/TintUtils.java` — trace reachability and assign parity coverage.
- [ ] `util/UploadUtils.java` — trace reachability and assign parity coverage.
- [ ] `util/WorldUtil.java` — trace reachability and assign parity coverage.
- [ ] `util/zen/ZenDocAppend.java` — trace reachability and assign parity coverage.
- [ ] `util/zen/ZenDocArg.java` — trace reachability and assign parity coverage.
- [ ] `util/zen/ZenDocClass.java` — trace reachability and assign parity coverage.
- [ ] `util/zen/ZenDocExporter.java` — trace reachability and assign parity coverage.
- [ ] `util/zen/ZenDocMethod.java` — trace reachability and assign parity coverage.
- [ ] `util/zen/ZenDocNullable.java` — trace reachability and assign parity coverage.
