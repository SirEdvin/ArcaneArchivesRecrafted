# 0015 — Recipe-owned creator output policy

Status: implemented under delegated behavior authority; runtime crafting integration pending.

Pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`, `recipe/gct/GCTRecipeWithCrafter.java:10-23` selects creator stamping by subclass and mutates the output passed to `onCrafted`, writing the acting player's UUID and display name to `creator` and `creator_name`. Ordinary recipes have no creator stamping.

Decision: the shared final recipe definition selects this policy through the named `GCTRecipe.withCreator` factory and a private immutable flag rather than introducing the old inheritance hierarchy. `createOutput(UUID, String)` delegates to the existing copy-safe CraftingCreator helper for creator recipes; ordinary recipes return an unchanged detached template copy. Both require nonnull identity/name before preparation. `getRecipeOutput` remains an unstamped, detached catalog/display template. Explicit null rejection and the typed factory are source API adaptations, not binary compatibility. Existing template data remains preserved by the helper, with creator fields replaced for creator recipes only.

All four targets are affected. No save/network format, costs, assets or actual recipe registration changes. Callers must resolve identity from the authenticated server player, never packet fields. Output preparation neither consumes inputs nor authorizes, delivers, or persists output. Conditions, authoritative membership lookup, full transaction/remainder handling, actual letters and runtime binding remain unfinished.

Alternatives: require every caller to remember stamping (loses the recipe's declared behavior); recreate mutable subclass hooks (unneeded API surface); arbitrary output callbacks (unnecessary side effects before a transaction exists). The small immutable policy reuses 0010's data-copy implementation and 0011's owned definition. Authority: user's explicit delegated behavior decisions, not publication permission.

Two added tests cover per-player output identity, independent counts, unchanged template/preview, ordinary unstamped output, invalid identity arguments and output preparation remaining separate from ingredient matching.

Verification: `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon`, exit 0 in 27s, log `build/recipe-creator-20260907-224136.log`. XML totals: 89 tests each on Fabric 1.20.1, Fabric 1.21.1 and NeoForge 1.21.1, zero failures/errors/skips. Forge test sources compile only; its Minecraft-backed harness remains unresolved. Four artifact pairs and `git diff --check` pass. These are not gameplay or full crafting acceptance results.
