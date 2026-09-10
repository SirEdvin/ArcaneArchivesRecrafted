# Tome of Arcana — conversion prerequisites and parity evidence

This slice strengthens the conversion audit and traces the real tome routes. It does **not** register a tome, create a partial Patchouli book, or change gameplay. The approved external backend remains as implemented in [GUIDEBOOK_BACKEND.md](GUIDEBOOK_BACKEND.md). The migration remains unfinished.

## Source and licensing

All upstream references below are relative to `src/main/java/com/aranaira/arcanearchives/` at `80944ce45c6559243d8928cc4b305bf379388652`, unless a resource or archive path is given. The reference checkout is `/tmp/arcane-archives-reference/upstream`.

The main-source MIT notice and credits were reviewed in [LICENSE](../upstream/LICENSE) and [CREDITS.md](../upstream/CREDITS.md). The modified Guidebook's retained [BSD-style notice](../upstream/Guidebook_License.txt) was reviewed before including its standard-template XML in the audit. This is documentation/source analysis, not incorporation of the legacy backend or artwork into production resources. No notice was modified.

The inherited item behavior was read directly from the pinned `embed/Guidebook-1.12.2-2.9.1.s5-sources.jar`, entry `gigaherz/lirelent/guidebook/guidebook/ItemGuidebook.java`. Its SHA-256 is `adf3b889fdf78285474593b3a4c1f70f61318622db846506c659062817cb62e1`. The template audit reads the runtime archive, `embed/Guidebook-1.12.2-2.9.1.s5.jar`, SHA-256 `e6fe9298e9128998d95e0f9572f4528e668ee7ef0890063d3e94a0ea60634ccc`. These are legacy reference artifacts, not the modern Patchouli dependencies.

## Item identity and opening

- `items/TomeOfArcanaItem.java:21-48` extends `ItemGuidebook`, registers `arcanearchives:tome_arcana`, limits stacks to one, supplies a bound creative-tab stack and overrides the tooltip with the gold translated tome tooltip.
- The original binding is the string `Book = arcanearchives:xml/tome.xml`. The book title comes from the bound Guidebook document; this is not the prototype Patchouli book.
- The inherited item invokes `showBook` for both use-on-block and right-click-in-air, using the requested hand. It returns failure on the server, and on the client if the `Book` string tag is absent. A valid client string is passed to `GuidebookMod.proxy.displayBook`; the held stack is returned unchanged. Tome itself does not override these routes.
- `events/EventHandler.java:346-351` registers the XML with Guidebook on the client. `integration/guidebook/GBookInit.java:9-12` registers the custom background factory and the Gem Cutter recipe provider. The background renderer is not ported by installing Patchouli.
- A modern binding/opening implementation must preserve tome identity, held-hand behavior, non-consumption and dedicated-server safety. Old NBT import remains out of scope. A different modern storage representation must still survive normal item copy, drop and new-world save/reload; no such runtime coverage is claimed here.

## Acquisition and persistent receipt

| Route | Exact baseline behavior | Sources |
|---|---|---|
| Crafting recipe | Shapeless vanilla book plus ore-dictionary `nuggetGold`; result is the tome with its `Book` binding, metadata 0. Do not narrow the gold ingredient to a vanilla-only item without analyzing tag equivalents. | `src/main/resources/assets/arcanearchives/recipes/tome_arcana.json:5-16` |
| Bookshelf break | On the server, if `BookFromBookshelf` is enabled and the block is an instance of `BlockBookshelf`, invoke the one-time grant helper. No explicit creative-mode exclusion appears in this handler. | `events/EventHandler.java:526-530` |
| Radiant Resonator crafting | On the server, if `BookFromResonator` is enabled and the crafted item-block is the registered Radiant Resonator, invoke the same helper. | `events/EventHandler.java:533-538` |
| Tome crafting receipt | Inside that same `BookFromResonator` guard, crafting the tome marks the player as having received it. Disabling this option also disables this receipt update; do not silently lift the branch out of the guard. | `events/EventHandler.java:535-545` |
| Creative tab | Produces a bound tome stack. The item method itself does not update receipt state. | `items/TomeOfArcanaItem.java:33-37` |

Both grant options default to `true` in `config/ServerSideConfig.java:19-25`.

`givePlayerBookMaybe` (`events/EventHandler.java:504-524`) returns immediately for a player whose `receivedBook` is already true. Otherwise it marks and saves receipt, creates a bound tome entity at the player's position with zero pickup delay, sends the route-specific gold message, attempts entity spawning, and plays the cloth-fall sound. It does not first insert into the player's inventory.

`data/PlayerSaveData.java:8-50` defaults receipt to false and persists `received_book` in UUID-keyed world saved data. `data/DataHelper.java:45-46,89-97` loads that data from the overworld, not a per-dimension player record. A port needs server-owned, per-player/world persistence with restart, death/reconnect and dimension-change tests, not just an in-memory flag.

**Failure-semantics boundary:** the legacy helper saves receipt before spawning and does not check the spawn result. That ordering creates a possible lost-grant failure path. This is a source observation, not a verified upstream runtime reproduction or approval to redesign acquisition. The separately approved raw-quartz conservation change does not cover it. No implementation of this failure path, and no gameplay correction, was added in this slice; resolve any necessary change through its own decision before implementing acquisition.

## Feature gates and recipe dependencies

- `tome.xml:6-23` defines Arsenal enabled/disabled, loaded/not-loaded pairs for `thaumcraft`, `astralsorcery`, `botania`, `potioncore`, and `quark`, plus true/false predicates. These are not all advancement unlocks. Conditional paragraphs and links need their own gates; entry-level hiding alone cannot represent every branch.
- Arsenal's XML condition checks existence of `arcanearchives:gbook_arsenal_condition`. `init/ItemRegistry.java:119-121` registers that sentinel only when `ConfigHandler.ArsenalConfig.EnableArsenal` is true; the option defaults to false (`config/ConfigHandler.java:74-77`). Many Arsenal items themselves remain registered when disabled. Item existence of a gem therefore is not an equivalent visibility test.
- `init/RecipeLibrary.java:153-185` conditionally adds Arsenal recipes. Disabling dormant features does not authorize dropping the enabled branch of this registered module. Exact optional integration support remains a separate migration dependency, not permission to delete its text.
- `integration/guidebook/GCTRecipeProvider.java:25-31` chooses the first item/meta-equal output in the ordered recipe list; its `recipeIndex` argument is unused. Lookup by key returns no display when the recipe is missing (`:36-40`). The provider copies matching ingredient alternatives and applies each `IngredientStack` count, then copies the output (`:45-63`). Do not replace these counted Gem Cutter ingredients with ordinary crafting-grid assumptions.
- The provider is registered as `arcanearchives:gct_recipe`. The real recipe list includes creator- and player-condition-dependent recipes, for example invitation/resignation/expulsion (`init/RecipeLibrary.java:84-115`). Recipe display is not authorization to craft; the modern server must enforce those conditions separately.
- Shaped quartz acquisition is the Gem Cutter recipe consuming two raw quartz for one shaped quartz (`init/RecipeLibrary.java:78`). The existing compression/decompression resources do not provide this progression step.

The immediate runtime prerequisites remain shared configuration, persistent receipt, progression/registered content and the Gem Cutter recipe system. The tome should not be announced complete by shipping empty stacks or excluding unported sections.

## Stronger reproducible inventory

`scripts/audit_guidebook.py` now retains:

- Parsed XML bodies for every section, including anonymous continuations, ordinary prose, inline formatting and child-tail text.
- Every stack occurrence, not just recipe outputs and stack-link targets: source element path, literal attributes and condition ancestry.
- Complete bodies of the two local templates and four templates in the actual included `gbook_snapshot:xml/standard.xml`, read from the pinned embedded runtime JAR. That standard library contains no further includes. The archive path and hash are recorded in the generated audit.

The pinned tome still contains 7 chapters, 98 sections, 44 recipe elements, 256 link elements and 71 image elements; it additionally exposes all 456 stack occurrences. Existing per-file audit fields were compared recursively against the pre-change snapshot and preserved. Each captured section body and ordered stack-attribute record was independently compared with the pinned tome XML.

This is parsed XML, not byte-for-byte XML reproduction: ElementTree normalizes serialization and omits comments. Source hashes cover the original bytes. This is not a complete renderer, dynamic reachability proof, reference resolver, asset license audit, translated-content conversion, or runtime parity test. No source identifier is normalized or corrected, and the experimental `Arcane Tome Thingy`/`BUZZ` book is not imported.

## Verification

- Red: three newly added regression fixtures failed against the prior audit (missing section bodies, missing all-stack inventory and incorrect archive-qualified source handling). The two existing fixtures remained green.
- Green: `timeout --foreground 30s python3 scripts/test_audit_guidebook.py` — exit 0, six tests. Coverage includes mixed-content/tail preservation, anonymous continuations, conditions, literal malformed identifiers, counted/ore stacks, template bodies and exact pinned Git reads for embedded XML. Synthetic archive data is only a unit-test fixture; the actual audit uses the pinned upstream archive.
- Regenerate/check: `timeout --foreground 2m python3 scripts/audit_guidebook.py /tmp/arcane-archives-reference/upstream` and the same command with `--check` — both exit 0.
- Canonical four-leaf build: `timeout --foreground 10m ./gradlew build --no-daemon` — exit 0, 8s, `build/guidebook-audit-20260907-195202.log`.
- Alternate-state matrix: `./gradlew 'Set active project to 1.20.1-forge' --no-daemon`, `./gradlew build --no-daemon`, both artifact checkers, then `./gradlew 'Reset active project' --no-daemon`. Every Gradle command used `timeout --foreground 10m` and complete file logging. Every exit was 0; aggregate duration 21.19s; `build/guidebook-audit-roundtrip-20260907-195327.log`. Canonical `src/` and `stonecutter.gradle.kts` bytes matched the pre-change snapshot after restoration.
- `scripts/verify_artifacts.py` and `scripts/verify_quartz_resources.py` — all four target pairs pass. JUnit reports still contain 11 MathUtils tests per leaf, no failures/errors/skips; these are existing coverage and may be up-to-date/cached, not new tome tests.
- No production Java, resources or build configuration changed. No new client/server run was warranted by this audit-only slice; the prior runtime matrix remains evidence only for the backend/quartz foundation.

Working-tree backup before edits: `/tmp/arcane-continuation-fb5r24x4`. No commit, push, remote or publication was performed.
