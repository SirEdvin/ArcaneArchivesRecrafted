# 0011 — Gem Cutter recipe definition ownership

Status: implemented under the user's delegated behavior-decision authority; runtime recipe integration pending.

## Original behavior

Pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`, `recipe/gct/GCTRecipe.java:41-45` retains the constructor's output reference and copies ingredient list entries. The varargs constructor at 47-68 copies the output but skips unrecognized inputs. `getRecipeOutput` at 97-98 returns a copy; `getIngredients` at 102-103 exposes the mutable internal list. Matching delegates to IngredientsMatcher at 82-94. Empty ingredient lists are permitted. Default craftability is true; subclasses supply conditions and creator effects.

## Decision and scope

The shared typed `GCTRecipe(ResourceLocation, ItemStack, List<IngredientStack>)` owns an output copy and an unmodifiable ingredient-list copy, reusing immutable counted requirements and conserved matching from 0005. Reject null identity/list/entries and empty outputs rather than permitting malformed definitions. Preserve valid output counts/data, identity, repeated requirements and empty-ingredient recipes. Preview outputs and allocation arrays are detached. Editing a definition requires constructing its replacement; do not promise deep immutability of externally supplied Ingredient predicates.

All four leaves are affected. This is a source API adaptation, not binary compatibility with upstream subclasses. No save/network format, assets, recipe costs or registered progression recipes change. No varargs parser is shipped yet; explicit modern tag/script adapters remain pending rather than silently skipping unknown values. Registry/index methods, conditions, creator invocation, remainder effects, serialization and actual crafting are still unfinished. The class deliberately supplies no craft method; preview matching is not authorization to give output.

Alternative: preserve mutable output/list aliases and reconstruct matchers each time. Rejected because external edits could change recipe cost/output behind a preview. Copying the list but exposing a mutable getter would leave the same flaw. No new dependencies are needed. This adaptation is decided under the explicit user delegation recorded in the register; no publication is authorized.

## Verification

Four regression tests exercise output/list ownership, repeated-cost conservation with detached preview plans, creator-stamping isolation, malformed definitions and retained zero-ingredient matching. Executed `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon`: exit 0, 27s, log `build/gct-definition-20260907-220128.log`. XML totals: 65 tests each on both Fabric leaves and NeoForge, zero failures/errors/skips. Forge test-source compilation only; its known Minecraft-dependent harness failure is unresolved. All four artifact pairs pass `scripts/verify_artifacts.py`; `git diff --check` passes. No full-crafting/gameplay acceptance is inferred.
