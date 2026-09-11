# Public API review and integration boundaries

## Review result

All 11 Java files beneath the pinned upstream `api/` directory were read at revision `80944ce45c6559243d8928cc4b305bf379388652`. [Machine-readable inventory](upstream-api-audit.json) records each path, SHA-256 and upstream lexical symbol references. Reference matches are not proof of runtime reachability. The upstream checkout was clean during the review.

The current port has no `com.aranaira.arcanearchives.api` package and does not provide source/binary compatibility with the 1.12 Forge API. Existing public Java methods are implementation entry points used by this mod's adapters, not a separately versioned external SDK. This review documents the current boundary; it does not exclude future reachable gameplay or approve removal of a required integration.

No consumer requiring a separate API artifact was identified in the inspected project sources, build configuration, examples or approved integration scope. That is a bounded repository finding, not a claim that no third-party addon exists. Do not add an API publication solely because upstream had an `api/` folder. If an actual addon requires a Java contract, first identify its supported leaves, operations, lifecycle and authorization needs and review the proposed contract with the user.

## Complete upstream inventory

Paths below are relative to upstream `src/main/java/com/aranaira/arcanearchives/api/`.

| File | Original contract and observed use | Current migration boundary |
| --- | --- | --- |
| `GCTRecipeEvent.java` | Noncancelable Forge event exposing the recipe list after built-ins; `init/RecipeLibrary.java:195` constructs it. | No legacy event shim. Native datapack recipes and approved KubeJS recipe events are the implemented customization route. |
| `IGCTRecipeList.java` | Recipe lookup, ordered indices, add/replace/remove, creator/condition factories; implemented by upstream `recipe/gct/GCTRecipeList`. | Native recipe manager supplies definitions; port `GCTRecipeList` is a local generation-guarded catalog, not a global registration API. Do not retain list indices across reload. |
| `IGCTRecipe.java` | Preview/matching, creator callback, conditions, consumption and remainder hooks; used by recipes, menus, networking, guidebook and CraftTweaker. Its consumption documentation explicitly presumes matching already happened. | Port final `GCTRecipe` is a definition/preview; native menu and joint crafting state perform authorized conserved commits. Do not expose the legacy partial-consumption assumption as an external crafting shortcut. CraftTweaker replacement is governed by 0113. |
| `IArcaneArchivesRecipe.java` | Matching/output/ingredient interface. Its own Javadoc explicitly says internal, not intended for overriding or public use. Used by `IGCTRecipe` and fast crafting. | A package name does not make this a promised extension point. No compatibility interface is required merely for this internal abstraction. |
| `RecipeIngredientHandler.java` | Callback for consumed ingredient handling in Gem Cutter/fast crafting. | Remainders remain part of the native paid crafting path. No independent external inventory-mutation callback contract is implemented. |
| `immanence/IImmanenceBus.java` | Generate/consume phases and aggregate totals/multiplier/tick time; used by network types, global immanence and diagnostic command. | No public modern bus contract is implemented. Network/immanence reachability and lifecycle remain in Phase 4/5, not completed or excluded by this review. |
| `immanence/IImmanenceConsumer.java` | Subscriber accepting immanence and reporting requirements/met state; referenced by the bus. | Do not invent a consumer device or expose an unpaid resource mutation API to fill the interface. Reachable consumers require implementation and conservation tests first. |
| `immanence/IImmanenceGenerator.java` | Subscriber supplying a nullable source; used by Matrix Core, Reverberation Chamber and the bus. | Registered-device reachability must be established separately from interface implementation. No external generator hook is currently provided. |
| `immanence/IImmanenceSource.java` | Amount/type/category; source implementation and generators reference it. | Source representation is pending the corresponding runtime machinery, not a stable external data model. |
| `immanence/IImmanenceSubscriber.java` | Network priority; generator/consumer interfaces and network membership reference it. | Ordering and membership must follow actual network lifecycle. No compatibility priority registry is implemented. |
| `immanence/ImmanenceBonusType.java` | ADDITIVE/MULTIPLICATIVE categories used by sources/generators/bus. | Retain as upstream behavioral reference; do not publish a detached enum as evidence that the power system works. |

## Implemented pack-author route

Use the native recipe type `arcanearchives:gem_cutting`, not reflective calls to catalog mutation methods. The native serializer exists on all four supported leaves. Datapack recipe directories follow Minecraft: `data/<namespace>/recipes/` on 1.20.1 and `data/<namespace>/recipe/` on 1.21.1. Choose the matching pack format for the target version.

For scripting, follow the [KubeJS example and runtime evidence](../examples/kubejs/README.md): 1.20.1 Fabric/Forge and 1.21.1 NeoForge only; KubeJS is intentionally omitted on 1.21.1 Fabric. Mods remain optional. Add/remove/replace by exact namespaced recipe ID through `ServerEvents.recipes`; apply edits with an operator-authorized reload or restart. Do not assume generic output filters understand this custom recipe format.

Current native JSON fields, from `recipe/gct/GemCutterDataRecipe.java:54–139`:

- `type`: use `arcanearchives:gem_cutting` for native recipe dispatch.
- `inputs`: at most 54 entries, each with exactly one explicit namespaced `item` or `tag`, plus optional positive integral `count` (default 1).
- `result`: registered non-air `item` and optional positive integral `count` (default 1), capped at the item's native stack limit and 64.
- `order`: optional integer, default 0; native enumeration sorts by order then full recipe ID, not registration order.
- `enabled`: optional boolean, default true.
- `arsenal`: optional boolean, default false; true additionally requires the Arsenal feature gate.
- `record_creator`: optional boolean, default false. Actual paid crafting applies creator identity; preview lookup is not a creator-stamping operation.
- `hive`: optional `invitation`, `resignation` or `expulsion`; evaluated against authoritative player/Hive state during crafting. This is not an arbitrary executable predicate language.

Unknown fields are rejected. Arbitrary NBT/component scripting is excluded under [0118](../behavior-changes/0118-scripted-stack-data.md); native stack identity and conserved payment remain intact. Only validated KubeJS diagnostic metadata is stripped under [0117](../behavior-changes/0117-kubejs-recipe-metadata.md). Native JSON has a 32767-character wire limit, checked before metadata stripping. These document existing parser behavior, not new schema extensions.

## In-mod Java adapters: safe boundaries

- JEI and EMI also expose the [Resonator production-information page](RESONATOR_VIEWERS.md), using the dedicated synthetic ID `arcanearchives:/resonating/raw_quartz`. Its output lookup is informational, not permission to grant quartz. There is no transfer handler and EMI recipe-tree support is disabled. The interval retains upstream's local configured display, not a remote-server settings API.
- JEI and EMI enumerate `GemCutterDataRecipe.entries(...)`, filter enabled definitions, and create display data. Neither registers a Gem Cutter transfer handler. Separately, both provide standard vanilla-crafting grid transfer for the Radiant Crafting Table, excluding output/bookmarks from ingredient sources and fill destinations; EMI exposes the native output separately. See [JEI evidence](RADIANT_CRAFTING_JEI.md) and [EMI evidence](RADIANT_CRAFTING_EMI.md). Visible/enabled recipes do not authorize payment or bypass player-specific conditions.
- Patchouli uses `integration/patchouli/GemCutterBookRecipes` for read-only ID/output lookup. Legacy item/damage output lookup is suitable for book navigation, not exact payment identity.
- `GemCuttersTableMenu` obtains current recipe-manager entries and performs guarded input/output inventory commits through `GemCutterCraftingState`. Decision 0128 replaces staged paid results with a saved output slot; extraction does not craft, and no pre-release journal compatibility API is retained. `GCTRecipeList` snapshots are read-only views and generation checks reject stale catalog attempts; direct catalog mutation is not persistent recipe registration.
- Do not cache definitions, numeric indices or authorization results across reloads or sessions. Re-resolve the recipe ID against the current native manager; leave authority, current conditions, remainders and item conservation with the server crafting path.
- The KubeJS smoke script's `Java.loadClass` calls inspect native state for test assertions. They are not the recommended pack-author API and do not create a stable reflective contract.

## Publication and acceptance

The inspected build defines four loader/version leaves, production JARs and source JARs (`withSourcesJar()` in each loader build). `release.gradle.kts` verifies the four-leaf build/artifact matrix before the explicit GitHub release transport. There is no separately configured API Maven publication. Source JAR availability does not imply compatibility guarantees for every public class. This review changes no build, publication or release behavior.

The API-review checklist can close independently of gameplay acceptance. Native crafting with connected players, viewer transfer/hover/reload, complete network/immanence behavior and any future external-addon contract remain open. No Java compatibility facade, new gameplay, API upload, commit or release is part of this review.
