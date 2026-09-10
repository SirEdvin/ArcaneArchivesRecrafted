# 0014 — Isolate ingredient predicate candidates

Status: implemented under delegated behavior authority; verification below.

## Source and defect

Pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`, `recipe/IngredientsMatcher.java:30-44,60-76`, passes the same inventory stack to successive ingredient tests. The modern matcher already copies live input, but still shares one snapshot stack among predicates. A custom data predicate can rename that candidate so a later exact-data requirement matches a name absent from the real input. Clearing its count can instead hide a valid match. This is a reproduced defect in the port, not evidence of a legacy multiplayer exploit.

## Decision

Give each slot/ingredient evaluation its own stack copy. Keep allocation counts derived from the original detached snapshot. This prevents candidate mutation from contaminating later requirements and preserves the meaning of the owned input commit's final live/snapshot comparison. Ordinary pure predicates, ingredient costs, outputs and ordering are unchanged.

Affected targets: Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. No save/network format, recipe registration or asset changes. Authority: user delegated documented behavior decisions. This does not authorize publication.

Alternatives: documenting predicates as pure leaves accidental cross-predicate mutation possible; rejecting every mutating predicate requires additional comparisons without improving the isolation provided by copying. Removing custom predicates would unnecessarily reduce recipe support.

## Verification

Input-commit integration follow-up: two additional `GemCutterInputHandlerTest` cases verify that fabricated candidate data cannot authorize actual consumption, unrelated inputs survive rejection, successful consumption returns original data and counts, returned data mutations cannot affect remaining inputs, and insufficient remaining input cannot be consumed again. No production change was required. The same four-leaf test/compile/assemble command exits 0 in 22s, log `build/predicate-commit-20260907-223325.log`; XML totals are 87 tests per Fabric/NeoForge target with zero failures/errors/skips. Forge test sources compile only. All four artifact pairs pass. This is input-commit coverage, not complete output delivery or runtime crafting acceptance.

After the fix: `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon` exits 0 in 27s; log `build/predicate-isolation-green-20260907-222405.log`. All executing test targets pass; Forge test compilation only.

Two regression tests reproduce fabricated exact-data matches and hidden valid matches. Before the fix, `:1.21.1-fabric:test --tests '*IngredientsMatcherTest' --no-daemon` fails both new cases (six tests, two failures); log `build/predicate-isolation-red-20260907-222304.log`, exit 1 in 12s. Caller stacks remain unchanged.

This isolates candidate objects, not arbitrary Java side effects or deliberately false predicate results. A predicate capturing the real handler can still mutate it; the existing owned-input revalidation must remain in place. Full crafting authorization, conditions, output/remainders and runtime integration remain pending. Forge Minecraft-backed execution remains blocked by the existing harness issue.
