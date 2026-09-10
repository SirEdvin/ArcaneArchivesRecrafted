# Unfinished and uncertain upstream features

Current project state and resumption instructions: [HANDOFF.md](../HANDOFF.md). This remains a source audit, not a list of newly implemented or permanently deleted mechanics.

## Purpose and scope

The user requested a separate record of unfinished upstream features. These notes track evidence from baseline `80944ce45c6559243d8928cc4b305bf379388652`; they are not instructions to silently enable, complete or delete those features. Continue the ordinary gameplay port without treating every source class as a shipped feature. Completing unfinished gameplay requires a separate proposal when it introduces new behavior.

Approved scope: the user explicitly replied “approve all three” to the recommendation to port reachable behavior and leave dormant/unregistered features documented and disabled, alongside the guidebook and wearable backend choices. Do not complete or enable dormant features as part of migration. Registered but partly unfinished features still require reachability analysis; this decision does not authorize removing their reachable behavior or treating default-disabled Arsenal content as unregistered.

Paths below are relative to upstream `src/main/java/com/aranaira/arcanearchives/`. This is a static source audit, not a runtime reproduction. Full registration evidence is in `registry-baseline.json`.

## Instantiated items excluded from registration

### Fabrial

- `init/ItemRegistry.java:75` creates `FABRIAL` (`items/FabrialItem.java`).
- `init/ItemRegistry.java:101,115,129` comments it out of the Arsenal list, item registration and model registration.
- A constructor and source file do not establish an obtainable, supported item. Keep this finding separate from normal registered-item parity; do not enable it merely because the class ports successfully.

### Transferstone

- `init/ItemRegistry.java:85` creates `TRANSFERSTONE` (`items/gems/oval/TransferstoneItem.java`).
- `init/ItemRegistry.java:101,115,129` comments it out of Arsenal, item and model registration lists.
- Intended behavior, recipes and reasons for exclusion still require investigation before any decision to expose it.

## Matrix and furnace registration gaps

- Matrix blocks are registered by `init/BlockRegistry.java:131`, but `MATRIX_CORE_TILE_ENTITY`, `MATRIX_REPOSITORY_TILE_ENTITY` and `MATRIX_STORAGE_TILE_ENTITY` are instantiated and omitted/commented out of the tile-registration list at line 149.
- `RADIANT_FURNACE_TILE_ENTITY` and `RADIANT_FURNACE_ACCESSOR_TILE_ENTITY` are instantiated at lines 87-88 but absent from that tile-registration list. The furnace block itself is registered.
- Follow-up [0109](../behavior-changes/0109-radiant-furnace-runtime-proposal.md): the registered block's `createTileEntity` directly constructs these classes, and CommonProxy initializes the ore predicate used by their cooking/Echo path. Missing tile registration does not by itself establish dead gameplay. The user subsequently [excluded the entire furnace feature](../behavior-changes/0110-radiant-furnace-exclusion.md), closing the restoration proposal. Retain this historical evidence; do not reintroduce furnace content or its guidebook placeholder.
- `AAGuiHandler.java:20-22` declares matrix storage/repository/reservoir IDs, with repository and reservoir both assigned 6. Matrix storage/repository menu branches are commented out at lines 64-67 and 113-116.
- These are concrete completeness warnings, not proof that every related mechanic is absent. Audit block/entity/menu/recipe reachability as a whole before porting each feature. Do not resolve the gaps by inventing gameplay.

Relevant classes: `MatrixCoreTileEntity`, `MatrixRepositoryTileEntity`, `MatrixStorageTileEntity`, `RadiantFurnaceTileEntity`, `RadiantFurnaceAccessorTileEntity` and their registered block counterparts.

## Wonky Resonator duplicate registration

- `init/BlockRegistry.java:149` lists `WONKY_RESONATOR_TILE_ENTITY` twice.
- Preserve the observation in the source audit; do not infer two distinct block-entity types or deliberately duplicate modern registration.
- Runtime consequences in the old build have not been reproduced. Determine intended content behavior separately from the duplicate registration call.

## Unused items and incomplete menu routes

- `items/unused/` contains older/unregistered concepts including Geomancy Pendulum, Geomantic Map, Scepter of Abduction, Spirit Orb and Tome of Requisition; commented declarations appear in `init/ItemRegistry.java:56-60,108-109`.
- `AAGuiHandler.java:37-39` returns null for Tome of Requisition on the server, while lines 87-88 retain a client book-container path.
- Modern menu registration must follow reachable behavior, not mechanically convert every declared numeric ID.
- The full source index remains in `../UPSTREAM_INVENTORY.md`; listing a class there does not place an unfinished feature in the release scope.

## Disabled is not the same as unfinished

- `config/ConfigHandler.java:74-77` defaults `EnableArsenal` to false and describes crafting/JEI gating.
- Registered gem classes still exist when Arsenal is disabled. `items/gems/ArcaneGemItem.java:47-49` removes their creative-tab visibility.
- Preserve the default and audit recipe/visibility gates. Do not group all Arsenal content with unregistered Fabrial and Transferstone or turn Arsenal on as a porting convenience.

## Related decisions, kept separate

- Fresh worlds only: old-world import, data conversion and legacy missing-mapping handlers are out of scope. Historical aliases in the registry snapshot are reference data, not implementation tasks.
- Raw-quartz conversion's remainder-drop correction is approved but not implemented; see `../behavior-changes/0003-raw-quartz-conversion-conservation.md`.
- Guidebook and wearable choices are approved in separate decision records. External Patchouli startup is verified, but tome content and Curios/Trinkets integration remain unimplemented. Backend approval does not establish that original gameplay should be removed.

## Follow-up discipline

- Add newly discovered unfinished features here with source paths and observed limitations.
- Distinguish confirmed registration gaps, suspected defects and reproduced runtime failures.
- Keep new gameplay decisions in separate behavior-change files and obtain approval before implementing them.
- Do not mark this entire audit complete until the remaining recipes, configuration gates and runtime paths have been examined.
