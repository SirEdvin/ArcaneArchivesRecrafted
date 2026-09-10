# 0010 — Copy Gem Cutter output before creator stamping

Status: implemented under delegated behavior authority; recipe integration pending.

Pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`, `recipe/gct/GCTRecipeWithCrafter.java:16-22` writes `creator` UUID and `creator_name` display name onto its output argument. `GCTRecipe.java:97-98` supplies a copied recipe output. `items/LetterOfInvitationItem.java:33-48` reads these fields to locate the creator's hive; `LetterOfResignationItem.java:33-40` compares the creator UUID to the interacting player. These consumers are not ported by this slice.

Decision: retain both field names, UUID identity and literal display-name value. On 1.20.1 use item NBT; on 1.21.1 use CUSTOM_DATA, retaining other components and unrelated custom fields. The small `CraftingCreator.withCreator` helper owns a fresh output copy instead of mutating an arbitrary caller's stack. Reject empty outputs and null arguments before mutation. This is a source API adaptation with defensive-copy semantics, not binary compatibility with the old recipe subclass. No player-visible output/cost/ownership rules change. Existing creator fields on the result are replaced, as upstream did; the template is never rewritten.

Affected targets: all four supported leaves. Fresh-world item metadata only; the modern UUID representation is not an old-save converter. No packet/schema for networks or hive authorization is introduced. The helper accepts server-supplied identity; metadata alone is not proof of authorization. Recipe invocation, display-name retrieval from the actual player, condition checks and letter behavior remain pending.

Alternatives: mutate the template directly (could leak one crafter's identity into another output); replace the whole custom-data compound (loses unrelated fields); introduce a custom synced component solely for two existing fields (unnecessary for this port). Rejected. Authority: the user's explicit delegation recorded in the behavior register. Original assets and notices are unchanged.

Verification: targeted tests cover copied templates, overwritten creator fields, preservation of unrelated data/name/count, persistence round trips and invalid arguments. Actual execution evidence is appended after the run.

Executed `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon`: exit 0, 26s, `build/crafting-creator-20260907-215009.log`. Both Fabric targets and NeoForge report 61 tests each, zero failures/errors/skips, including four creator cases. Forge test sources compile; its Minecraft-dependent fixtures were not executed and the known harness blocker remains. All four artifact contracts and `git diff --check` pass. Actual crafting, letters and multiplayer authorization remain unverified.
