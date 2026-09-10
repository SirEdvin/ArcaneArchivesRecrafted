# Munchstone handheld runtime

## Original behavior and provenance

Inspected pinned upstream bb99accf48ed583e29b0efae56e28c963407b8df: items/gems/oval/MunchstoneItem.java, init/RecipeLibrary.java, PacketArcaneGems.GemParticle consumer and English/Portuguese localization. Original MIT notices retained through the existing asset recovery helper.

Charges: 60 normal, 240 with the power bit. Use an eligible plant block with positive charge to remove it without its direct loot and restore food using its configured hunger and saturation modifier 1.0. Preserve the original unusual cost computation: first add hunger/saturation to temporary current values, then clamp modifiers against those already-increased values. Zero computed cost leaves the block alone. A positive charge permits an operation costing more than remaining charge; final charge clamps to zero. Creative does not waive payment.

Empty-gem right-click consumes up to five bonemeal from the first matching main-inventory stack, restoring twelve charge per consumed item. It does not combine several stacks or fully fill a power-upgraded gem. Nonempty gems cannot use this recharge.

The oval-black particle packet actually plays a burp, not particles. Original recipe: shaped quartz, four black dyes, cake and two speckled melon items (modern glistering melon slices).

## Native implementation and compatibility boundaries

Shared MunchstoneItem connects server-thread/alive/nonspectator checks, use permissions, loaded target checks and block-entity rejection, paid successful removal, native food updates and bonemeal recharge. Registration, Arsenal-gated acquisition/creative entry, original animated/depleted/accessibility oval models, atlas entries and localization are connected.

MunchstoneConfig stores the original ordered block-ID/feed entries in config/arcanearchives/munchstone.json, seeded from a bundled list by the pinned recovery script. Restart to apply. First matching duplicate wins; invalid/nonpositive entries are ignored, malformed JSON fails without replacing the user's file. Missing optional-mod IDs remain inert, not substituted. Vanilla metadata families are expanded into modern IDs; no broad plant tags add new species. Both grass and short_grass identifiers cover the version rename. Dead-bush metadata ambiguity uses the explicit deadbush entry. Original optional IDs are retained without claiming modern optional-mod compatibility.

Implementation status: handheld migration slice, not whole-family parity approval. Shared sockets/powder/HUD/upgrades, optional-ID compatibility and runtime acceptance remain open. Burp uses native positional server sound for now rather than the original tracking-client-local playback; exact recipient/position presentation remains unfinished and is not claimed as approved parity. No new gameplay balance correction is proposed for the original cost/recharge quirks.

## Integration evidence

./gradlew assemble --no-daemon: exit 0, 21s, build/munchstone-integration-20260909-071120.log. All four compileJava tasks and assembly passed; git diff --check passed. Runtime/config classes, bundled defaults, recipe, model and animation inspected in all four production JARs. Artifact class expectations updated; full verifier not run.

No gameplay tests, optimization, client/server launch, commits or publication. Overall migration remains incomplete and not ready for first playtest. Switchgleam source was inspected but no Switchgleam implementation is included.
