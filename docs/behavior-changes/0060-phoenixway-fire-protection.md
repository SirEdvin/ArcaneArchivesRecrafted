# Phoenixway held-gem fire protection

## Original behavior

Pinned bb99accf48ed583e29b0efae56e28c963407b8df EventHandler.onEntityHurt (LivingAttackEvent) checks available Phoenixway gems. Existing FIRE_RESISTANCE skips the gem. ON_FIRE and IN_FIRE only trigger; lava and other fire-tagged sources are not added. Nonnegative charge includes empty gems. Activation grants 600 ticks at amplifier zero, consumes twelve clamped charge and cancels the attack. The effect normally prevents later gems in the loop from activating. Uses player.playSound for ENCHANTMENT_TABLE_USE, not the separate trillion particle/sound packet.

## Native implementation

PhoenixwayEvents connects native Fabric ALLOW_DAMAGE, Forge LivingAttackEvent and NeoForge LivingIncomingDamageEvent to one authoritative held-gem handler. Checks server thread, live nonspectator player and original damage identities/effect guard. Main-hand then offhand selection; applies the original duration/amplifier and clamped payment. Inventory/menu sync reflects payment. Native server sound excludes the acting player, corresponding to server Player.playSound broadcast semantics.

This is an implementation slice, not full cross-loader event-phase parity. NeoForge's incoming event explicitly runs after invulnerability checks; the legacy attack hook's timing differs. Broader invulnerable/custom damage-hook interactions, effect rejection by another mod and shared socket/wearable selection remain compatibility work. No approval to redesign original charge gating or broaden lava coverage is inferred.

Original tracking-client-local FIRECHARGE_USE on ranged activation/recharge remains unimplemented and separately tracked. Shared HUD/upgrades and runtime acceptance remain open.

## Verification

./gradlew assemble --no-daemon: exit 0, 18s, build/phoenixway-protection-20260909-084702.log. All four compileJava tasks passed; PhoenixwayEvents packaged on each target; git diff --check passed. These are compilation/static integration checks, not damage/gameplay acceptance. No full verifier run, gameplay tests, optimization, client/server launch, commits or publication. Overall migration incomplete, not ready for first playtest.
