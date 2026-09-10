# Slaughtergleam handheld loot and recharge runtime

## Original behavior

Pinned source/assets: bb99accf48ed583e29b0efae56e28c963407b8df, items/gems/asscher/Slaughtergleam.java, EventHandler.onLooting/onEntityLivingDeath, RecipeLibrary, language resources and PacketArcaneGems.GemParticle.Handler. MIT license inspected; existing attribution retained.

Capacity 30/150. Each charged available gem adds one looting level for a player damage source. Each player-caused living death consumes one charge from every charged available gem before subsequent looting queries. As restored in [0066](0066-gem-material-auto-recharge.md), that consumption immediately attempts nugget recharge when empty. Only a gem still empty after that attempt loses its bonus. Neither handler checks the toggle, despite the item exposing a toggle. This corrects the earlier incomplete final-charge description.

Right-click an empty gem: consume up to five gold nuggets from the first matching main-inventory stack; restore twelve charge per nugget with the original capacity clamp. Stop after that stack. This can consume more nuggets than needed for the base capacity. No recharge message is sent by the original implementation. Its asscher-red particle packet has no implemented client effect; no invented sound/particles added.

Original recipe: one shaped radiant quartz, four red dyes, one diamond, two gold ingots, two lapis blocks. Original animated red/dun asscher and accessibility art and English tooltip text (also original Portuguese resource text) recovered.

## Implementation and affected targets

Shared server-thread/alive/non-spectator handheld accounting and original empty-gem recharge, registry/Arsenal creative gating, recipe gating, native toggle controls and model properties.

Forge 1.20.1 uses native LootingLevelEvent and LivingDeathEvent at default priority, ignoring canceled events as upstream does. NeoForge 1.21.1 uses LivingDeathEvent; its old LootingLevelEvent is absent. Fabric pays at dropAllDeathLoot entry before loot is read, not AFTER_DEATH (which is after loot). Both Fabric signatures are explicit: 1.20.1 takes DamageSource; 1.21.1 also takes ServerLevel.

Fabric 1.20.1 intercepts the native loot-count, loot-chance and custom-death-loot level queries. Fabric/NeoForge 1.21.1 intercept the native enchanted-count and enchanted-chance queries only when the requested enchantment is LOOTING; unrelated enchantments are untouched. The underlying native queries still run. No persistent enchantment or held weapon mutation is used.

## Approval and remaining scope

No intentional balance deviation proposed; this is partial handheld scope, not parent-family completion. Shared sockets/wearables/powders/HUD/upgrades remain unfinished. Nonstandard/custom loot implementations remain open; do not claim universal LootingLevelEvent equivalence. Fabric death overrides that bypass native dropAllDeathLoot and interaction with modded cancellation/order require compatibility completion. Native registered recipe, table-loot and equipment-drop paths are implemented, not every external consumer.

The 1.21 equipment-drop path now adds 0.01 probability per eligible gem after the native enchantment-effect calculation in Mob.dropCustomDeathLoot. This preserves the older per-looting-level equipment bonus without altering equipment, adding enchantments or charging again. Native zero-base-chance exclusion, prevent-equipment-drop effect, recent-player-kill eligibility, guaranteed-drop classification, item damage and single native drop/slot removal remain in place. The 1.20 native integer-level path is unchanged; its comparison clamps the reduced random roll at zero, also preventing zero-chance equipment drops. The shared equipment mixin intentionally has no injections on 1.20, avoiding duplicate application there. Datapack/modded effect ordering still requires deferred compatibility acceptance.

## Verification

Equipment completion: `./gradlew assemble --no-daemon` under ten-minute timeout exited 0 in 19s; `build/slaughtergleam-equipment-20260909-114955.log` reports all four compileJava tasks and BUILD SUCCESSFUL in 18s. Packaged equipment mixin/config and both modern injection targets inspected; Fabric remaps dropCustomDeathLoot/processEquipmentDropChance to method_6099/method_60113. Native mapped Mob and NeoForge Mob source call sites inspected. `git diff --check` passed. No gameplay tests or optimization.

Asset recovery script executed against the pin. `./gradlew assemble --no-daemon` under ten-minute timeout passed on all four targets. Initial log: `build/slaughtergleam-integration-20260909-114502.log`, exit 0, 21s. Final log: `build/slaughtergleam-final-20260909-114656.log`, exit 0, 18s, Gradle success in 17s and all four compilation tasks observed. `git diff --check` passed.

Production classes, recipe, model and animation inspected across four targets. Fabric loot redirects and final death signatures/targets inspected after remapping; modern native/NeoForge source loot queries traced. Static inspection caught and corrected the 1.21 death-hook signature before handoff. Artifact verifier expectations extended; full verifier not run. No gameplay tests, optimization, clients/servers, commits or publication. Compilation/remapping is not proof of applied mixins or gameplay parity. Migration remains incomplete and not ready for first playtest.
