# Chromatic powder and handheld recharge integration

## Original behavior

Source/assets: bb99accf48ed583e29b0efae56e28c963407b8df, ArcaneGemItem.tryRechargingWithPowder, GemRechargePowder, GemRechargePowderRainbow, GemUtil.consumeCharge, individual gem recharge overrides, ItemRegistry, language/model/texture resources. MIT licensing previously inspected and attribution retained.

Registered IDs are chromatic_powder and full_spectrum_chromatic_powder. The ordinary item's `color` integer stores the original ordinal: red 1, orange 2, yellow 3, green 4, cyan 5, blue 6, purple 7, pink 8, black 9, white 10. Missing/invalid ordinals are colorless and render white, not a valid matching white powder. Both items have no creative tab or survival recipe upstream; no acquisition recipe/tab entry invented. Original creative-only tooltip retained.

Recharge scans main inventory in order, uses the first matching color, otherwise the last full-spectrum stack encountered, consumes one, fills the gem and sends the original consumption message. The original UP item capability maps to PlayerMainInvWrapper (Forge 1.12.x EntityPlayer patch inspected), not offhand/armor. No sound is added. Original powder recharge does not guard against already-full gems; explicit Mindspindle/Elixirspindle recharge therefore can consume powder even at full charge. Preserve this quirk.

Book/wart recharge takes precedence while empty. Slaughtergleam, Munchstone and Phoenixway override recharge without calling powder fallback; their powders must not be consumed despite broad powder tooltip wording. Other registered gem colors follow the pinned constructors.

## Current implementation and targets

All four targets: powder registration, per-stack color/name/tooltip state, original ten colored and full-spectrum animated art, atlas integration, native model predicates and shared server-authoritative recharge. Predicate ordinals are scaled into 0..1 because native ClampedItemPropertyFunction clamps its result; saved ordinals are unchanged. Reads do not mutate copies.

Mindspindle/Elixirspindle explicit recharge now includes the original powder fallback. All migrated held-gem charge-spending paths use the shared depletion callback, including casts, XP, healing, combat, lightning and potion interception. Current charge is checked after recharge by existing Murdergleam/Salvegleam toggle logic, matching upstream. Native dropped-item fluid/weather recharging remains unchanged and cannot consume inventory powders through a copy.

## Approval and remaining scope

No intentional gameplay deviation proposed. Registered admin/creative-only items remain outside the ordinary mod tab, with no survival recipe. Shared Gem Socket storage/menu and worn availability, its recharge button, HUD/upgrades, optional integrations and runtime acceptance remain unfinished. No universal parent-family completion claim.

Correction: the pinned RequestRecharge sender is GUIGemSocket.actionPerformed, button ID 0. Whole-source search found its registration/handler and that GUI sender, not a keyboard binding. Earlier queue references to a recharge key were inference, not an implemented upstream feature. Migrate the socket button; do not invent a keyboard control without approval.

## Verification

Asset script ran against the pin. Final `./gradlew assemble --no-daemon` under ten-minute timeout exited 0 in 20s; `build/chromatic-powder-final-20260909-120801.log` shows all four compilation tasks and BUILD SUCCESSFUL in 19s. Initial build: `build/chromatic-powder-20260909-120615.log`, exit 0, 22s. `git diff --check` passed.

Four production JARs inspected for runtime, color predicates/models, eleven PNG animation companions and original English/Portuguese localization; duplicate locale keys rejected in the static check. Artifact verifier class expectation extended; full verifier not run. No gameplay tests, optimization, clients/servers, commit or publishing. Migration incomplete and not first-playtest-ready.
