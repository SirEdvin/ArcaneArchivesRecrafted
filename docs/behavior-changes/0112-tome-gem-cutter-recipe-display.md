# 0112 — Tome Gem Cutter recipe display adapter

Status: implemented adapter and packaged templates under approved Patchouli backend decision 0001. Full Tome integration and connected-player rendering remain pending; this is not a completed book or gameplay-parity claim. No new acquisition, consumption, grant-failure or authorization behavior is introduced.

## Original behavior and licensing

Development pin `80944ce45c6559243d8928cc4b305bf379388652`, `src/main/java/com/aranaira/arcanearchives/integration/guidebook/GCTRecipeProvider.java`:

- Output lookup scans the ordered recipe list and selects the first `ItemStack.areItemsEqual` match (item/metadata, ignoring arbitrary NBT and count). The recipe-index argument is unused.
- Key lookup returns no display when absent.
- Every ingredient's alternatives are copied with its counted cost, without clamping the display to native stack size; output is copied without invoking creator stamping or crafting conditions.
- Inputs use four columns at 22-pixel spacing, starting at (3,3); output is at (107,14), over the original 127x44 region in the 256x256 recipe texture.

This is deliberately different from `GCTRecipeList.getRecipeByOutput`, whose upstream helper compares stack data too. The existing catalog/crafting lookup is unchanged.

Reviewed main upstream MIT notice before copying the original texture. `scripts/port_tome_recipe_assets.py` reads pinned Git bytes and refuses to overwrite differing resources; `--check` verifies without writing. Texture SHA-256: `8de89eb40b9269eb4ca0dad19b62d60e39553814e16412ee7204735d603fb0c9`. Existing packaged upstream notices remain. No Patchouli implementation or artwork is copied or bundled.

## Modern implementation

- `integration/patchouli/GemCutterBookRecipes.java`: read-only lookup of existing ordered native recipe entries by output or holder/key identity. Filters recipes disabled by explicit/Arsenal configuration, matching the effective original registered catalog. Does not evaluate player-specific Hive crafting permission or forge creator metadata for a preview.
- `client/GemCutterBookComponent.java`: public-API `ICustomComponent`, referenced by templates rather than server initialization. Resolves synchronized recipes and current tag alternatives on render, so it does not retain stale costs/results across native recipe/tag updates. Missing recipes render nothing, not invented fallback recipes. Alternatives cycle every 20 book ticks.
- Original layout/artwork uniformly scaled to 0.85 to fit Patchouli's narrower page, with matching mouse-coordinate conversion. Backend/UI adaptation is covered by 0001; exact connected-player visual/tooltip acceptance is still open. All ingredients are traversed; the original layout's overflow for oversized custom ingredient lists is not silently truncated or redesigned.
- Separate `gem_cutting_output` and `gem_cutting_recipe` templates live under `assets/arcanearchives/patchouli_books/tome_arcana/en_us/templates/`. Supply `output` (native Patchouli item-stack value) or `recipe` (namespaced native recipe ID), respectively. A page uses the template ID directly as its `type`: `arcanearchives:gem_cutting_output` or `arcanearchives:gem_cutting_recipe`. The custom component's own type must be `patchouli:custom`; unqualified `custom` resolves to the Minecraft namespace and is silently discarded by Patchouli's native parser.
- Malformed/ambiguous selectors fail explicitly during component construction; recipe identifiers are not normalized. These are additional content-validation checks permitted by the standing goal.
- Version branches adapt the installed 1.20/1.21 Patchouli variable API: 1.21 requires registry-aware variable values. API signatures and behavior were inspected in the resolved 85/93 Fabric source JARs; Forge/NeoForge compile against the already approved external API artifacts.

No `book.json`, empty book, Tome item, grant hook or recipe has been registered by this adapter slice. The complete XML conversion still needs prose/condition/link/image conversion and use of these templates. The automatic-grant failsafe remains explicitly rejected under 0111. Radiant Furnace remains excluded.

## Verification

- Test-first RED: `timeout --foreground 10m ./gradlew :1.21.1-fabric:test --tests '*GemCutterBookRecipesTest' --no-daemon`; exit 1, 8s, `build/tome-recipes-red-20260910-143035.log`. Missing new adapter symbol caused compilation failure; not a runtime assertion RED.
- First full build exposed a real test-fixture error in the 1.21 Patchouli variable callback: unqualified `IVariable.wrap` returns a value backed by empty registries. Fixed the fixture to pass the native registry provider, matching the production callback contract. `build/tome-recipes-build-20260910-143438.log`, exit 1, 47s. No production fallback was added to hide it.
- Green full build: `timeout --foreground 10m ./gradlew build --no-daemon`; exit 0, 40s, `build/tome-recipes-green-20260910-143658.log`.
- Six shared assertions run on each target (Forge through native GameTest JUnit bridge): first enabled output match ignoring creator/count, holder-ID rebinding, missing/disabled/empty lookup, detached 256-count ingredient/output previews, replacement/removal snapshots, and Hive preview without authorization/stamping.
- Two additional native Patchouli variable/template cases pass on each Fabric Minecraft version: actual packaged template deserialization/selection and explicit malformed/ambiguous selector rejection. These exercise installed variable serializers, not a synthetic API replacement. They do not exercise screen rendering or a live connected recipe reload.
- Four dedicated-server/four client startup reports independently parsed as passing: `build/tome-recipes-startup-20260910-143752.log`, exit 0, aggregate 173s; per-process commands, durations and logs in `build/tome-recipes-startup.json`. Commands: `timeout --foreground 10m python3 scripts/smoke_servers.py`; then each leaf with `timeout --foreground 10m xvfb-run -a -s '-screen 0 1280x800x24' python3 scripts/smoke_clients.py <leaf>`. Title-screen startup is not book-page rendering acceptance.
- Alternate state: bounded `./gradlew 'Set active project to 1.20.1-forge' --no-daemon`, full `build`, then `'Reset active project'`. Exit 0 throughout, durations 6s/37s/7s; logs `build/tome-recipes-switch-20260910-144105.log`, `build/tome-recipes-alternate-20260910-144111.log`, `build/tome-recipes-reset-20260910-144148.log`. All four artifact pairs passed in the alternate state.
- Snapshot `/tmp/arcane-tome-recipes-roundtrip-20260910-144105.tar`: restored four known Stonecutter comment-drift files and independently compared all 675 source/controller bytes. Automatic round-trip is not byte-stable.
- Final canonical full build: same bounded build command, exit 0, 27s, `build/tome-recipes-final-20260910-144254.log`. All four artifact pairs pass, including exact new classes, original texture hash, templates and unchanged dependency/test isolation checks.
- Final test XML: Fabric 1.20.1 204, Forge standalone 36 plus native runtime 199, Fabric 1.21.1 204, NeoForge 279; zero failures/errors/skips. Forge standalone coverage overlaps native coverage and is not additive unique gameplay coverage.
- No project game JVM remains. Whitespace checks pass. No commit/push or unrelated process termination.

Remaining: connected-player page layout, tooltips/alternative cycling, live recipe/tag reload while open, complete Tome content/conditions/navigation, original crafting/automatic acquisition and new-world receipt acceptance. Overall migration remains incomplete.
