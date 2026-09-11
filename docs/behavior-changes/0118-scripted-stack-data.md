# Scripted Gem Cutter stack data

## Status and decision authority

Rejected and removed from migration scope by explicit user instruction: "Then just remove this from scope and remove this extra logic". The proposed parser extension was never implemented; no corresponding production logic exists to remove. The historical proposal below is retained only as a decision record, not a backlog item.

None of the port's 36 shipped Gem Cutter recipes require arbitrary NBT/component constraints. Preserve item/tag/count scripting and rejection of unsupported fields. Existing creator stamping, stack identity and inventory-conservation checks are separate behavior and remain intact. This decision applies to all four native serializer targets and all three supported KubeJS leaves.

## Original behavior and current gap

Pinned upstream `src/main/java/com/aranaira/arcanearchives/recipe/IngredientStack.java:22–25,46–49,88–94` stores optional NBT on item and ore ingredients. When NBT is supplied, matching requires compound equality, not subset containment; absent NBT imposes no data constraint.

The port's `recipe/IngredientStack.java:51–58` already supports exact stack-data matching for stack templates, but `recipe/gct/GemCutterDataRecipe.java:86–98` accepts only item/tag/count JSON. Consequently KubeJS native JSON cannot currently express data-sensitive inputs or data-bearing outputs. General KubeJS support approval does not specify a new JSON schema or how old NBT maps to modern components.

## Rejected proposal (not to implement)

- Preserve existing item/tag/count recipes unchanged.
- Minecraft 1.20.1: accept optional `nbt` as an SNBT compound string on inputs and outputs. Inputs with supplied NBT require exact compound equality; omission remains data-insensitive. An explicit empty compound is a constraint, not an omitted value.
- Minecraft 1.21.1: accept optional `components` using the native data-component patch JSON representation on inputs and outputs. For inputs, compare the candidate's complete effective component map against the referenced item's defaults with the supplied patch applied. This is exact matching, not a contains-only predicate. For tag alternatives, build the expected map against each candidate item's defaults.
- Reject the wrong version's field instead of converting it silently. Reject malformed data and unknown component IDs through native parsers/codecs. Preserve explicit removal entries where the native patch codec supports them.
- Apply output data before validating its native stack-size limit. Retain all input-count, total wire-size, unknown-field and server crafting-authority checks.
- Ensure ingredient previews carry the required data and counts; do not let viewer display matching authorize payment.
- Keep data decoding registry-aware on 1.21.1, including network synchronization. Verify pinned native APIs before implementation; if their constraints change this proposal, stop and document the issue.

## Reason

Restore upstream data-sensitive recipe expressiveness through the approved native JSON/KubeJS integration without introducing fuzzy matching or an unsafe second crafting path. Native version-specific formats avoid inventing a lossy NBT-to-components conversion.

## Affected targets

Native datapack serializer on all four supported leaves: 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. KubeJS examples/runtime coverage apply only to its three approved leaves; 1.21.1 Fabric remains supported through datapacks, not a new KubeJS dependency.

## Save/network compatibility

No world conversion and no change to default gameplay recipes. Existing recipes retain their behavior. New fields require updated mod binaries on both sides; retain bounded synchronization and reject unsupported data rather than discarding it.

## Alternatives requiring a different decision

Subset component matching is convenient for pack authors but differs from upstream exact-NBT matching. A portable custom-data-only format cannot express native enchantments, names and other modern components. Both are deliberately not assumed.

## Historical proposed verification (out of scope)

Four-leaf build and native round trips; exact/absent/explicit-empty data behavior; tag alternatives and reload; malformed or unknown data rejection; data-dependent output stack limits; detached previews; counted payment and denied mismatched payment. Installed KubeJS add/edit/delete/reload must preserve the data on all three supported leaves. Connected JEI/EMI behavior remains separate acceptance.
