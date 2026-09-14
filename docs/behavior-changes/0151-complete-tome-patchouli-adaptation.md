# Complete Tome: native Patchouli presentation

## Original behavior

The audited development Tome at `80944ce45c6559243d8928cc4b305bf379388652` contains 98 sections in seven chapters, conditional paragraphs, stack links, images and output-selected recipes. Anonymous sections continue their preceding topic. Some original sections literally contain `PLACEHOLDER`; several identifiers predate the flattening, refer to unregistered machines, or belong to optional legacy mods.

## Adaptation

Convert the complete prepared source into native Patchouli categories, entries and pages. Preserve paragraph text, emphasis, conditions, navigation, illustration content and live Gem Cutter output lookup. Repaginate for Patchouli rather than cloning the legacy layout engine. Native text, item and image components replace legacy inline layout; image links also have an ordinary clickable text label. Lone repeated leading item icons in named prose/index rows become text links; standalone item illustrations and multi-item diagrams remain. Native book search/category navigation supplements the retained index. Use native Patchouli book artwork rather than stretching an incompatible legacy background. Hover-only navigation artwork uses the native text-link hover affordance instead of a second image.

Remove only the Radiant Furnace entry and its navigation under approved 0110. Retain other unfinished-source topics, replacing literal placeholder prose with an explicit unavailable/unfinished explanation, never fabricated instructions. Unregistered source items are described by name, not displayed as invalid or substitute item stacks. Resolve vanilla flattening aliases explicitly; retain optional compatibility prose with its original visibility condition and describe legacy-only references without requiring unavailable mods. This does not enable unfinished machinery or expand integration gameplay.

## Reason and approval

The user explicitly authorized reasonable behavior adaptation and extra security checks while finishing all implementable 0.0.2 work, with no unnecessary complexity. This record applies that authorization to presentation and reference migration only. It does not change item payment, ownership, grant ordering or storage behavior.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Verification

Deterministic conversion/reference tests, complete packaged resource checks, native registry/recipe checks and Fabric native page/template deserialization pass. Final separate clean/build and alternate-state builds pass on all four targets; all 25 script tests pass. See [checkpoint evidence](../migration/0.0.2-IMPLEMENTATION_CHECKPOINT.md). Real connected screen/input and visual acceptance remain separate; do not infer them from JSON generation or compilation.
