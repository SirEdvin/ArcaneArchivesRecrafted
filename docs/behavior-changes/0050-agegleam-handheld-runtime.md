# Agegleam handheld runtime

## Original behavior and source

Pin: `bb99accf48ed583e29b0efae56e28c963407b8df`. Inspected `items/gems/asscher/AgegleamItem.java`, `events/EventHandler.java` (living-player update), `GemUtil` held/available-gem selection, `RecipeLibrary`, and original asscher models/animation/accessibility resources.

Agegleam starts with 30 charge, or 150 with the power-upgrade bit. Right-click searches an axis-aligned box extending 3.5 blocks each side of the player's position. Every baby EntityAgeable has 8000 added to its age; one charge is consumed per affected entity, with remaining charge saturating at zero. Importantly, this handler does NOT gate activation on positive charge, does NOT affect already-adult breeding cooldowns, and does NOT clamp the resulting age at zero. Those original behaviors are retained rather than silently rebalanced to match the broader tooltip.

While available in either hand and below maximum charge, a saved `recharge` counter initializes to 3600 without decrementing on that first update, then decrements each player update. At exactly zero it restores one charge and resets to 3600. Full charge and stowing the gem pause the counter. Original available-gem iteration also includes socket/wearable paths, which remain a separate integration gap.

Original acquisition: shaped quartz, four green dye ingredients, nine wheat, nine carrots and nine wheat seeds. Arsenal acquisition/creative visibility remain opt-in and disabled by default.

## Implementation and scope

`AgegleamItem` connects the area action to native AgeableMob and the held-only timer to native item inventory ticks on all targets. Server-thread, live-player and spectator guards protect mutations; world interaction permission is checked at each affected mob. Native NBT/custom data stores the original timer alongside charge, without overwriting unrelated data. No new packet, mixin or dependency is required.

Original green asscher animated/depleted/accessibility resources are recovered through the existing asset copier, extended with a cut parameter while retaining existing pendeloque defaults. Item model predicates, atlas entries, recipe/tag bridges and the original English/Portuguese-source strings are registered.

Affected targets: 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Approval status: original-feature migration requested by the user; no balance correction proposed or applied. Authoritative interaction safeguards remain mandatory. The original tooltip is not proof of adult breeding-cooldown functionality.

## Remaining migration and acceptance

Socket/wearable activation/recharge, shared powder controls, HUD and remaining shared gem upgrade/unlimited-charge behavior are unfinished. Held-timer persistence, multiplayer, entity growth behavior, acquisition and presentation acceptance are deferred until the implementation pass is complete. This checkpoint does not establish complete Arsenal parity or playtest readiness.

## Integration evidence

`./gradlew assemble --no-daemon`: exit 0, 19s; `build/agegleam-integration-20260909-054820.log`. All four compileJava tasks and assembly succeeded; `git diff --check` passed. Agegleam class, gated recipe, model and animation sidecar were inspected in every production JAR. Artifact class expectations were updated; the full artifact verifier was not run. No gameplay tests, optimization, clients, commits or publication.
