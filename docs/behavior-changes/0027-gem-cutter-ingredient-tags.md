# 0027 — Gem Cutter OreDictionary ingredient bridges

Status: implemented under the user's delegated behavior-decision authority for the initial Inlay definition.

## Original behavior and adaptation

Pinned release `bb99accf48ed583e29b0efae56e28c963407b8df`, `init/RecipeLibrary.java`, registers `scintillating_inlay`: six Radiant Dust, twelve `dustRedstone`, one `ingotGold`, six `nuggetGold`, producing one Scintillating Inlay. The latter three are Forge 1.12 OreDictionary ingredients, not vanilla-only item matches.

`ContentRegistry.gemCutterRecipes()` now installs that exact counted definition into each newly opened server menu. Modern shared item tags `arcanearchives:ingredients/dust_redstone`, `ingot_gold` and `nugget_gold` bridge vanilla defaults plus optional common-tag conventions:

- Redstone: `minecraft:redstone`, `c:dusts/redstone`, `c:redstone_dusts`, `forge:dusts/redstone`.
- Gold ingot: `minecraft:gold_ingot`, `c:ingots/gold`, `c:gold_ingots`, `forge:ingots/gold`.
- Gold nugget: `minecraft:gold_nugget`, `c:nuggets/gold`, `c:gold_nuggets`, `forge:nuggets/gold`.

Non-vanilla entries are optional nested tags, with `replace: false`; no optional mod is required. These are explicit compatibility aliases, not proof that every legacy mod's OreDictionary registration has a corresponding modern tag. Data packs can extend the shared bridge tags. Untagged substitutes are not silently accepted.

The canonical 1.21 `tags/item` resources map to `tags/items` in both 1.20 leaves. Counts, output, namespace, original item assets and capacities are unchanged. No substitute acquisition recipe was added; other upstream Gem Cutter recipes remain unported, not removed from scope.

## Alternatives and compatibility

All four supported targets. Vanilla-only matching would drop the original interchangeability intent. Loader-exclusive tags would unnecessarily diverge shared behavior. Recreating OreDictionary or adding a compatibility dependency is unnecessary. Existing saves/network formats are unchanged; tag changes follow native data-pack semantics. Tag-backed counted ingredients now retain `TagKey` identity and read native holder membership, so already-open menus follow completed tag rebinding without replacing their catalog. Matching-stack displays read current tag contents directly; synthetic Forge-family empty-tag barrier hints are not actual matches. Non-tag ingredient constructors retain their existing behavior. Native `getIngredient()` representations are fresh when requested, not a promise that retained external Ingredient objects update themselves. Recipe-definition parsing/reload and crafting-time tag-generation revalidation remain separate unfinished work.

## Verification

Live-tag regression: `openMenuAndIngredientDisplayFollowNativeTagRebinding` first failed with cached `Ingredient.of(tag)` matching on 1.21.1 Fabric (`build/menu-tag-reload-red-20260908-102236.log`). After the membership fix, NeoForge exposed its synthetic empty-tag barrier in display expansion; its pinned `Ingredient.java.patch` confirmed the source. The display path now enumerates actual tag holders. The same open menu follows diamond-to-gold replacement and an emptied tag without input/carried mutation, while counted display stacks stay detached. The fixture uses native registry tag rebinding and restores prior memberships in `finally`; it does not execute `/reload` against a connected player. Final matrix: 145 tests pass per runnable Fabric/NeoForge leaf, zero failures/errors/skips; Forge test compilation and all four artifact pairs pass. `build/menu-tag-reload-final-20260908-102527.log`, exit 0, 31s. No new server or client runs were needed for this scoped regression.

The four-target matrix passes (144 executable tests per Fabric/NeoForge leaf; Forge compilation only). Packaged artifact checks enforce every alias, optional flag, fallback, and version-correct tag path. Four real dedicated servers load/reload resources and stop/save without Arcane Archives resource errors. Evidence: `build/gem-cutter-recipes-20260908-101351.log` and `build/gem-cutter-recipe-menu-verification.json`.

The native-menu regressions use vanilla fixture definitions, not frozen-registry custom-item construction. These checks do not demonstrate live Inlay crafting, all third-party substitutions, or actual menu opening; output delivery remains disabled until the crafting transaction is integrated. No visual checks were performed.
