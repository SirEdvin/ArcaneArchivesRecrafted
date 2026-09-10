# 0083 — Registered unfinished Obstruction and Serenity Charms

Status: registered item/recipe/resource restoration implemented; four-target assembly/package and two NeoForge fixtures verified. Upstream unfinished powers remain unfinished by design.

## Source and original behavior

Approved broader baseline `80944ce45c6559243d8928cc4b305bf379388652`: `init/ItemRegistry.java:115,129` registers both items and models. `items/ObstructionCharmItem.java` and `items/SerenityCharmItem.java` supply red/bold and red/italic not-implemented warnings. They have no implemented obstruction/muting action. Serenity declares MUTE, size/slot -1 and Resonator/Brazier targets, but these targets do not expose the inspected IUpgradeableStorage optional-upgrade path. Preserve that declaration here for future integration audits; do not invent muting or install it into Troves/Tanks.

`init/RecipeLibrary.java:145,149` provides reachable Gem Cutter recipes:
- Obstruction Charm x4: hardened clay x1 (modern terracotta), gold nuggets x2, shaped radiant quartz x1.
- Serenity Charm x4: gold nuggets x3, wool x1, radiant dust x1.

## Scope and approval

Faithful restoration under the approved reachable-content scope, not completion of upstream TODO gameplay. Register both items, normal creative visibility, the original warning styling/translations and original model/texture bytes. Reuse the existing cross-loader gold-nugget ingredient tag; use Minecraft wool's item tag. Keep both warnings, including upstream's crash warning, rather than claiming finished powers.

Recipe order 12 is shared with Radiant Key: the existing recipe-ID tiebreaker orders obstruction_charm, radiant_key, serenity_charm, retaining their upstream relative order without renumbering existing recipes.

Affected targets: 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. No new gameplay deviation is proposed. Original MIT notice/credits remain packaged.

## Verification

- `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*UnimplementedCharmItemTest' --offline --no-daemon`: exit 0, 29s, `build/registered-charms-20260909-181853.log`; all four targets assembled.
- NeoForge XML: 2 tests, 0 skips/failures/errors. Registered items preserve both warning keys/styles, ordinary stack limits and read-only tooltips. Actual packaged recipe JSON resolves registered outputs through the native Gem Cutter parser. This does not exercise live tag binding, payment or connected crafting.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, 1s, `build/registered-charms-artifacts.log`; four production/source pairs pass with explicit charm class, source, atlas, model/texture and recipe checks. Four artwork files are byte-identical to the baseline checkout; these two textures have no animation sidecars in that checkout. `git diff --check` passes.

Connected crafting, rendering, save/restart and full migration acceptance remain open. No new obstruction or muting power was implemented or approved.
