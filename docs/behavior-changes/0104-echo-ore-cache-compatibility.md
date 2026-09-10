# 0104 — Echo ore-selection and cache compatibility

Status: user approved the recommended modern ore-selection/persistent-cache contract. Implemented on all four targets. [Real Fabric 1.21.1 client checks](../migration/ECHO_CLIENT_ACCEPTANCE.md) now verify sampled Echo rendering, resource-reload cache retention, item/cache restart persistence and regeneration; other targets and broader connected acceptance remain open.

## Original behavior and source

Development baseline `80944ce45c6559243d8928cc4b305bf379388652` contains `util/DuplicationUtils.java` and `util/TintUtils.java`; neither file exists at release baseline `bb99accf48ed583e29b0efae56e28c963407b8df`. This is reachable development-baseline Echo presentation, not permission to enable dormant duplication producers. The baseline MIT license was inspected.

- `DuplicationUtils.init/readData/generate` reads `config/arcanearchives/ores.txt`. A missing, unreadable or effectively empty list triggers generation from furnace inputs whose Ore Dictionary name starts with `ore`. It then writes the generated list. Explicit configuration can select non-ore items as well. Legacy entries carry item identifier, metadata and count; wildcard metadata is treated specially during generated ore classification.
- `TintUtils.init` reads `echo_colors.txt`, keyed by packed numeric item/metadata identity. A missing/unreadable/empty cache triggers generation and writing. A nonempty cache is retained rather than regenerated each resource reload. `ClientProxy` invokes initialization during its post-initialization path.
- Cache generation considers the furnace result of each configured input, selects the first south-facing unculled model quad, reads its original PNG resource and samples coordinates 6 through 9 inclusive in both axes. It averages integer RGB channels and uses alpha 255. Raster pixels must have exactly three bands: upstream skips RGBA/indexed/grayscale raster layouts, rather than treating this as an arbitrary all-item or alpha-weighted average.
- The existing port preserves native source tint-provider priority at indices 0, 1, 2, followed by an unfinished fallback of -1. Only Echo layer 1 is colored.

## Compatibility decision

Modern Minecraft has no legacy Ore Dictionary or item metadata identity. Recipe and tag availability is also tied to the connected world's synchronized data rather than one global 1.12 furnace singleton. Translating arbitrary legacy `ore*` names into modern tag membership changes which modded inputs can populate a generated default list. The user must approve that compatibility contract before implementation under AGENTS.md; the authorization for reasonable validation does not select an ore-membership policy.

Recommended contract:

- Use native modern ore item/block tags for default discovery: vanilla ore families plus the appropriate loader/community aggregate ore tags, checked against each pinned dependency before coding. Only inputs with a smelting recipe qualify; do not include raw materials merely because they smelt into ingots.
- Retain an editable explicit input list using namespaced modern item IDs, so users can narrow or extend defaults, including non-ores. Do not invent conversion of old metadata-based configuration or activate duplication gameplay.
- Use namespaced result-item IDs in the persisted color cache instead of unstable numeric registry IDs. Retain the original nonempty-cache priority and deliberate delete-to-regenerate behavior, including user-entered colors; do not silently overwrite edits during resource reload.
- Generate only once the required client recipe/tag/model data is available. Document that a populated persistent cache remains local and shared across worlds, as upstream, rather than silently claiming server-specific automatic refresh.
- Preserve provider priority, exact RGB raster sampling, first south-facing quad selection, unknown-color fallback, and no new Echo acquisition. Missing resources and malformed entries must not mutate items or replace a user's malformed configuration.

Alternative: ship a fixed reviewed vanilla default input list and require explicit configuration for every modded input. This avoids translating community ore-tag membership but loses upstream automatic modded-ore discovery and therefore also needs approval.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. This concerns client Echo presentation/configuration, not storage, crafting payment or dormant item duplication.

## Implemented contract and verification

`EchoColorCache` is initialized only by the existing client entrypoints. The unchanged source-provider ordering in `EchoColor` now falls back to the restored cache. It reads namespaced item IDs from `config/arcanearchives/ores.txt` (one per line, `#` comments) and signed decimal ARGB colors from `echo_colors.txt` (`namespace:item,color`). Existing nonempty configurations are not rewritten. Malformed IDs/numbers and unreadable configuration fail explicitly without replacement; unknown optional-mod IDs remain in the file and are ignored for generation.

If no populated color cache exists, generation waits for the first fallback lookup with an available client world. Default discovery examines smelting ingredients and live native item/block membership in `c:ores`, `c:ores/*`, `forge:ores`, `forge:ores/*`, or vanilla `minecraft:*_ores`. Fabric convention-tag jars were inspected and contain the `c:ores` aggregate. Raw materials are not implicitly classified as ores. Explicit input lists bypass automatic discovery and can include non-ores. The actual result is selected through the native recipe manager's `getRecipeFor(SMELTING, ...)`, using the version-correct input type, rather than approximating custom recipe matching. The result passed to model lookup is copied.

Generation preserves the first south-facing unculled quad, its original PNG, RGB-only fixed sampling window and integer averages. A sampled opaque white value (-1) is retained as a real cache entry, distinct from an unavailable sample. Image streams are closed. Generated session data remains usable if writing fails. Even an empty generation result is attempted only once per client session, never once per frame. A populated cache is intentionally shared between worlds and retained through resource reloads; restart after editing, or delete the cache and restart to regenerate. This is not automatic server/resource-pack-specific refresh.

`EchoTintCacheTest` has nine cases covering persistence, explicit/unknown inputs, nonempty-cache priority, empty regeneration, malformed-file preservation, write failure, RGB sampling/bounds, unsupported formats and opaque white. `EchoOreInputsTest` has three cases covering approved tag families and real native item/block tag rebinding with smelting recipes; original bindings are restored in `finally`. No test-only dependency or code is shipped.

Executed commands (all Gradle executions bounded by `timeout --foreground 10m` and using `--no-daemon`; complete output saved):

- Four-target `build`, exit 0, 49s: `build/echo-cache-final-20260910-090920.log`. Artifact enumeration then correctly rejected the three newly added classes until the exact production-class allowlist was updated; it was not bypassed.
- Alternate Forge-active four-target `build`, exit 0, 51s: `build/echo-cache-roundtrip-20260910-091335.log`. Switch/reset logs: `build/echo-cache-roundtrip-20260910-091329.log` (6s) and `build/echo-cache-roundtrip-20260910-091426.log` (7s), both exit 0. Four unrelated nested-comment normalizations were restored from the pre-switch snapshot. The final native recipe-manager selection refinement was verified afterward in canonical state.
- Final canonical four-target `build`, exit 0, 52s: `build/echo-cache-native-final-20260910-091601.log`. Tests: Fabric 1.20.1 190, Forge loader-run 190, Fabric 1.21.1 190, NeoForge 1.21.1 266; zero failures/errors/skips. Each target executes all 12 new Echo fixtures. Forge's separate 36 standalone fixtures are duplicated coverage, not an additional 36 unique tests.
- `python3 scripts/verify_artifacts.py`: all four binary/source pairs pass, including test-content isolation. `git diff --check`: pass.
- Existing client-startup smoke command: `timeout --foreground 10m xvfb-run -a -s '-screen 0 1280x800x24' python3 scripts/smoke_clients.py <leaf>`. All four return 0 with the mod initialized, atlases/models loaded and normal shutdown. Wrapper logs: `build/echo-cache-client-1.21.1-fabric-20260910-091040.log` (26s), `build/echo-cache-client-1.20.1-fabric-20260910-091134.log` (23s), `build/echo-cache-client-1.20.1-forge-20260910-091157.log` (24s), `build/echo-cache-client-1.21.1-neoforge-20260910-091221.log` (22s). These precede the native recipe-selection refinement, which does not execute at the title screen.

The checks above are fixture, packaging and title-screen startup evidence. Subsequent [Fabric 1.21.1 connected-client verification](../migration/ECHO_CLIENT_ACCEPTANCE.md) provides actual rendering/reload/restart evidence for the specified Iron Ingot Echo scenarios, without extending that result to the other targets. No Echo acquisition/duplication producer was enabled. The overall migration remains incomplete.

## Remaining acceptance

- Configuration defaults, explicit non-ore inputs, invalid/unknown identifiers, nonempty user cache priority, empty-cache regeneration and file preservation.
- Native smelting/tag discovery on every target, including a modded-tag fixture and exclusion of raw materials unless explicitly selected.
- Pixel fixtures for exact integer RGB averaging, bounds, unsupported raster formats and missing resources; no accidental RGBA behavior change.
- Source provider ordering and layer gating, no source/echo mutation, deterministic cache lookup and no per-frame full recipe scan or disk write.
- Four-leaf builds, test-artifact isolation, dedicated-server startup safety, then real client model rendering and lifecycle checks. Unit fixtures and compilation alone do not establish visual acceptance.
