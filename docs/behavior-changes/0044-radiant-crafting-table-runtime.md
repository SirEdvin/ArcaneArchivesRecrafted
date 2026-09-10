# Radiant Crafting Table native runtime

Status: implemented runtime slice in the authorized migration continuation, all four targets. Full presentation/network integration and gameplay acceptance remain open; no parent-feature completion is claimed.

## Original behavior and evidence

Release `bb99accf48ed583e29b0efae56e28c963407b8df`: `blocks/RadiantCraftingTable.java`, `tileentities/RadiantCraftingTableTileEntity.java`, `inventory/ContainerRadiantCraftingTable.java`, `client/gui/GUIRadiantCraftingTable.java`, `recipe/fastcrafting/FastCraftingRecipe.java`, `items/RawQuartzItem.java`, and `config/ConfigHandler.java`. Original resources and translations are from the same MIT-licensed release.

The table holds a persistent nine-slot crafting matrix and three saved recipes. Closing the screen leaves the matrix in the block; breaking the block drops its contents. Click an empty memory slot to save the current recipe, or shift-click it to save the last crafted recipe. Click an occupied memory slot to craft once from the matrix plus player inventory; shift-click to forget it. The original fast-crafting path pays ingredients into a temporary matrix and invokes a native crafting-result slot to produce outputs/remainders.

Acquisition is one raw quartz plus a workbench in a shapeless recipe, or sneak-use raw quartz on a placed workbench. The original workbench conversion consumes quartz even in creative mode (unlike its chest branch). Both conversion paths respect `InWorldChestConversion`, default true. This setting is now exposed through the existing native server.properties configuration without overwriting existing files.

## Native implementation

`RadiantCraftingTableBlockEntity` owns the live matrix, owner UUID and three recipe IDs, persisted through version-specific native NBT/item-component codecs. Open menus share the same native mutable list instead of retaining separate authoritative copies. Recipe output is recomputed before clicks/shift transfers and during menu synchronization. Closing a menu does not clear the matrix; removal clears each stored slot before dropping it.

`RadiantCraftingMenu` uses native crafting recipes and ResultSlot for ordinary crafting. Saved shortcuts resolve IDs against the current RecipeManager, allocate complete ingredient payments from matrix then player storage, reconstruct native shaped/shapeless input from real copied items, validate the recipe/output and current source stacks, then pay once. Only after payment does a native ResultSlot operate on the detached matrix, preserving native recipe awards, player-aware crafting-return hooks and returned containers. Outputs and returns go to player inventory, with unaccepted remainders dropped. No cached preview authorizes payment. Unsupported/empty ingredient lists are not used to invent inputs; normal grid crafting remains available for special recipes.

Saved icons use read-only menu slots; clients send only bounded memory-action indices for their open menu, never recipe definitions, coordinates or ingredient counts. Live block identity, distance, level, player state and native interaction permission gate server actions. These guards replace the original unconditional container-access predicate. Workbench conversion checks permission and successful replacement before consuming quartz, and refuses workbench subclasses with block entities rather than discarding unknown inventories.

`port_crafting_table_assets.py` recovers original OBJ/MTL, textures and both GUI textures, with existing legacy-transform conversion. Native loader OBJ models, Fabric model routing, atlas inclusion, creative registration, acquisition recipe, loot, pickaxe tag, original English/Portuguese item text and localized memory controls are connected. The original pretty GUI and slot coordinates are used; alternate GUI selection and original hover/availability overlays remain unfinished.

## Remaining scope and verification

Manifest/network membership, tracking overlays, optional recipe-viewer integration, broader custom-recipe shortcut compatibility, legacy-world import and final visual/gameplay acceptance remain open. In particular, compiling does not establish concurrent-player, save/restart, full-inventory, modded remainder, protection or rendering acceptance. The original Portuguese description mentions connected inventory; this is retained source text, not a claim that a separate connected-inventory feature was added.

`./gradlew assemble --no-daemon` under the timeout/full-log wrapper passed every target's compileJava and assemble tasks: exit 0, 19s, `build/crafting-table-final-20260908-193943.log`. `git diff --check` passed. Artifact class/source expectations were extended; the full verifier was not run. No unit/gameplay campaign, clients, optimization, commits or publication. Migration remains incomplete and not ready for its first playtest.
