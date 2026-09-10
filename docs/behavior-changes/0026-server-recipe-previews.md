# 0026 — Server-owned Gem Cutter recipe previews

Status: implemented under the user's delegated behavior-decision authority. Paid crafting remains unfinished.

## Upstream and change

Pinned release `bb99accf48ed583e29b0efae56e28c963407b8df`, `release/0.2.0.25-mixins8`: `inventory/ContainerGemCuttersTable.java` combines table inputs with the main player inventory exposed on side UP, draws seven recipe slots in reverse container-index order, and updates the output preview as inputs change. `updateRecipe` also contains writes to player slot zero; `updateLastRecipeFromServer` writes a recipe output into table slot zero. These source findings are not a claim of a universally reproduced legacy exploit. `tileentities/GemCuttersTableTileEntity.java` stores shared current recipe by catalog index and keeps shared page state.

The modern menu now computes detached previews on the server using the existing conserved matcher over 18 table slots followed by 36 main-player slots, excluding armor/offhand. It never writes previews to input, player or carried inventories. Native menu slot synchronization transports ghost stacks; native validated menu-button actions wrap pages and slot clicks select a server-resolved recipe name. The client has no authoritative catalog. Output, recipe clones, swaps, throws and extraction are rejected before vanilla click handling. The output is an ingredient preview, not authorization or a deliverable item.

Selection/page state is deliberately per open menu, not shared mutable block state or a saved numeric catalog index. New menus select their first available definition. This avoids cross-player UI interference and persisted index reassignment; current user selection does not survive closing/reopening. Catalog replacement is resolved by name, with removed selections producing no output and stale pages reset. Existing joint crafting NBT and pending records are unchanged and never accessed for delivery here.

Native focusable/narrated previous/next buttons are overlaid on the original GUI; original image bytes remain unchanged. New English/Portuguese control labels are modern additions, not upstream quotations. Visual/layout acceptance remains deferred by user direction.

## Scope, rationale and alternatives

All four supported targets. No new dependencies, costs, item capacity, custom packet protocol or legacy-save conversion. Alternatives were copying upstream slot-overwrite behavior, trusting client display slots, persisting indices, or adding a per-player persistent selection framework. These were rejected as unsafe or unnecessary. Keeping crafting disabled is unfinished integration, not an approved removal of crafting.

## Verification and limits

Three new native-menu tests exercise combined/repeated costs, offhand exclusion, ghost click modes/buttons, detached preview restoration, reverse recipe-slot ordering, wrapping/replacement, unchanged player/table inventories, passive client handling and invalid access. The full runnable suites pass 144 tests per Fabric/NeoForge leaf, zero failures/errors/skips; Forge compiles tests only. Matrix `build/gem-cutter-recipes-20260908-101351.log`, exit 0, 36s. Artifact/resource checks and four dedicated-server reload/save/stop smokes pass; report `build/gem-cutter-recipe-menu-verification.json`.

No client was launched. Server smokes do not open menus or execute paid crafting. Runtime catalog reload binding, authoritative conditional recipes, consumption, remainders, delivery, complete progression and broader multiplayer acceptance remain unfinished. No preview may be used to bypass those boundaries.
