# 0031 — Native Gem Cutter recipes and live reload

Status: implemented and headlessly verified under the user's delegated behavior-decision authority; connected-player acceptance remains open.

## Original behavior

Pinned release `bb99accf48ed583e29b0efae56e28c963407b8df`: `init/RecipeLibrary.java` registers ordered Java recipes; `integration/crafttweaker/GCTTweaker.java` queues add/replace/remove operations against the singleton catalog. `inventory/ContainerGemCuttersTable.java` matches and pays from combined table/player inventory. There is no modern native recipe serializer or data-pack reload contract in this 1.12 implementation.

## Decision

Register a native `arcanearchives:gem_cutting` recipe type/serializer on all four targets. Move the seven currently connected plain progression recipes to ordinary version-correct recipe data. Preserve their identifiers, costs, outputs and ordering. Minecraft owns resource priority, reload lifetime and recipe synchronization; no global cross-world catalog or custom reload executor.

Open server menus refresh their existing guarded catalog from the current level's native recipe manager. Retain selection by resource identifier; a removed recipe cannot craft. Refresh before navigation/display and payment, cancel a payment click that first discovers changed definitions, and revalidate the native definitions immediately before owned payment commit. Pending results remain independent of definitions.

The JSON schema supports ordinary item/tag counted inputs and plain item output, explicit order, enabled flag and optional creator attribution. Reject unknown fields, unknown/air items, ambiguous inputs, non-integral/nonpositive counts and oversized output. Empty tags remain live nonmatches. Arbitrary Java predicates, conditional recipes, explicit NBT/components and CraftTweaker APIs are not silently converted or advertised as supported. Existing unsupported remainder refusal remains active. Data packs may disable a definition explicitly; native loader behavior for malformed recipe files is retained (errors logged and invalid recipes omitted, not promised whole-resource-manager rollback).

`record_creator` is an optional strict boolean, default false. This serializes the existing creator policy from 0015, preserving pinned `recipe/gct/GCTRecipeWithCrafter.java`'s `creator` UUID and `creator_name` fields. Definition rebinding during manager reload retains the policy. The live server menu supplies identity through its existing paid-output path; JSON cannot supply a creator UUID/name. Preview, native assemble/result inspection and shared templates remain unstamped. Attribution is not access authorization or a replacement for Hive conditions. Existing seven shipped recipes omit the flag and remain unchanged. This exposes existing behavior through the native schema rather than inventing another stamping implementation; no save-format or dependency change.

## Compatibility and alternatives

Fresh-world block state/save formats do not change. Native serializer wire data is version-specific, bounded and validated; no client crafting request trusts recipe definitions supplied by a player. Mixed old/new mod binaries are not supported. No new dependency.

Rejected a process-global mutable recipe cache and loader-specific custom resource directories, because Minecraft already owns per-world recipe resources and synchronization. Keeping Java-only definitions would leave `/reload` unable to change costs/output. This adds a documented modern customization surface, not a claim that legacy CraftTweaker scripts now work.

## Verification

Creator-policy extension: the focused regression first failed on the unsupported field (`build/creator-data-red-20260908-133056.log`, one test/one failure). `build/creator-data-green-20260908-133218.log` then passed in 33s with 167 tests per Fabric leaf, 177 NeoForge, zero failures/errors/skips; Forge test-source compilation and all four artifact pairs pass. Tests cover omitted/false/true policies, detached source JSON, wire round-trip, definition identity rebinding, two distinct creators through guarded paid crafting, denied payment, unstamped previews and rejection of malformed flags/forged identity fields. NeoForge also parses the creator flag with registered Recipe.CODEC and synchronizes it through RecipeHolder.STREAM_CODEC. This extension did not rerun connected-player or dedicated-server acceptance; the older server evidence below remains scoped to its original checkpoint.

`build/native-recipes-validation-20260908-131606.log` exits 0 in 32s: 165 tests per Fabric target, 175 NeoForge, zero failures/errors/skips. Forge compiles test sources only. All four artifact/resource checks pass; `build/native-recipes-validation-servers-20260908-131638.log` exits 0 with four dedicated servers reloading, exercising existing fixtures and saving/stopping cleanly. Consolidated report: `build/native-recipes-verification.json`.

Shared tests cover owned JSON/wire round-trip, invalid/ambiguous/unknown definitions, bounded wire input, open-menu replacement/disable/removal and final-check payment cancellation. NeoForge additionally exercises the registered native recipe-holder network codec and real RecipeManager replacement feeding the same paid menu. Existing progression-payment tests now decode the shipped JSON through Minecraft's registered Recipe.CODEC instead of rebuilding Java definitions. Fabric reload fixtures supply a non-fluid policy, not a Transfer API mixin claim. Invalid parser failures normalize to JsonParseException so 1.20's native error handler can reject malformed files without unexpected runtime exceptions escaping its catch boundary.

Dedicated servers demonstrate resource parsing/reload without Arcane Archives resource errors, not connected-player reload. Native 1.20 recipe log counters count type buckets (confirmed in pinned RecipeManager bytecode), while 1.21 logs recipe definitions; these counters must not be compared as equal units. Connected-client synchronization/rendering, actual player data-pack editing during an open session and broader gameplay acceptance remain deferred. Native manager replacement tests are not an actual server restart.

## Data-pack usage

Use `data/<namespace>/recipes/<name>.json` for 1.20.1 or `data/<namespace>/recipe/<name>.json` for 1.21.1. The file path supplies identity. To override an existing recipe, retain its namespace/path; shipped recipe IDs remain `arcanearchives:radiant_dust`, `shaped_quartz`, `scintillating_inlay`, `material_interface`, `matrix_brace`, `containment_field` and `radiant_lantern` in that namespace.

Example custom definition (plain inputs/output only):

```json
{
  "type": "arcanearchives:gem_cutting",
  "order": 10,
  "enabled": true,
  "inputs": [
    {"item": "arcanearchives:raw_quartz", "count": 2},
    {"tag": "arcanearchives:ingredients/nugget_gold", "count": 1}
  ],
  "result": {"item": "arcanearchives:radiant_lantern", "count": 4}
}
```

`order` defaults to zero, with resource-name tie-breaking. `enabled` defaults to true; set false on a complete valid definition to disable it. Counts default to one and must be positive integers; at most 54 counted input occurrences, with repeated/overlapping costs conserved by the existing allocator. Output must fit one native stack, capped at 64. Empty input lists are explicit free recipes, as supported by the existing Java definition; they are never inferred from missing/malformed fields. Empty/missing tags match nothing and follow native tag reloads. Save the pack and invoke `/reload`; server menus refresh their ghost slots and retain selection by ID. A click that first discovers changed definitions refreshes rather than pays. Removed/disabled selections cannot grant output. No unconditional pending-result delivery or legacy scripting conversion is introduced.
