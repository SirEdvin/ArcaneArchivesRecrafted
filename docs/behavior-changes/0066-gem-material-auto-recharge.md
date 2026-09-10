# Gem inventory-material automatic recharge

## Original behavior and source

Pinned revision bb99accf48ed583e29b0efae56e28c963407b8df: GemUtil.consumeCharge(GemStack, int), the five material-recharge overrides, and EventHandler death/XP/potion/fire-protection call sites. MIT LICENSE inspected; existing attribution retained.

GemUtil consumes charge, marks the gem dirty and immediately calls the gem's recharge method when the payment leaves it empty. This is not restricted to right-click recharge. Material overrides scan main inventory in order and use the first matching stack only:

- Slaughtergleam: at most five gold nuggets, twelve charge each.
- Munchstone: at most five bone meal, twelve charge each.
- Phoenixway: at most three gunpowder, twenty-five charge each.
- Elixirspindle: at most five nether wart, one charge each, consumption message.
- Mindspindle: one book, full charge, consumption message.

They activate only at zero charge. Original consumption quantities can exceed the amount needed to fill base capacity; retain the clamp rather than optimizing away payment. Slaughtergleam/Munchstone/Phoenixway overrides return false without powder fallback; Mindspindle/Elixirspindle fall back to powder upstream.

## Implemented behavior and targets

All four targets: shared live-server-thread held-stack validation, material selection/payment and inventory synchronization. Explicit recharge and depletion now use the same rules. Connected payment sites: Slaughtergleam death, Munchstone plant consumption, Mindspindle XP amplification, Elixirspindle potion copying, and Phoenixway ranged placement plus automatic fire protection. Main/offhand ordering remains the existing order; the next gem sees already-consumed materials. No item copy can consume the player's materials through this entry point.

Existing migrated material-recharge messages/sounds are retained. Exact tracking-client-local presentation remains a previously recorded gap, not newly claimed parity.

Important correction to 0065: a Slaughtergleam spent to zero can refill from nuggets during the death callback, before loot is queried. It loses the current kill's bonus only if it remains empty after this automatic recharge. Earlier unconditional final-charge wording omitted the upstream consumeCharge callback.

## Approval and remaining scope

No intentional gameplay deviation; restores a missing upstream callback rather than inventing automatic recharge. No new approval required. This is the inventory-material portion of shared recharge, not full shared-gem completion. Chromatic/full-spectrum powders, their fallback paths, recharge key/networking, gem sockets/wearables, shared HUD/upgrades and runtime compatibility/acceptance remain open. Cleansegleam's approved right-click milk behavior is separate from these five upstream recharge overrides and is unchanged.

## Verification

`timeout --foreground 10m ./gradlew assemble --no-daemon` exited 0 in 20 seconds. Log: `build/gem-material-recharge-20260909-115948.log`; all four compileJava tasks and BUILD SUCCESSFUL in 20s observed. Shared runtime and references from the six payment-owning classes inspected in all four production JARs. `git diff --check` passed. Artifact verifier expectation extended; full verifier not run.

No gameplay tests, optimization, client/server launches, commits or publishing. Build/static integration does not establish gameplay acceptance. Migration remains incomplete and not ready for first playtest.
