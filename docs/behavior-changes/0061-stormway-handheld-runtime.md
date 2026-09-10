# Stormway handheld runtime

## Original behavior

Source/asset pin: bb99accf48ed583e29b0efae56e28c963407b8df, items/gems/trillion/StormwayItem.java, events/EventHandler.java, items/gems/GemUtil.java, init/RecipeLibrary.java and original language/model/texture files. MIT notices remain packaged.

- Capacity 30/150. Toggle applies to retaliation, not targeted use, recharge or strike buffs.
- Targeted use casts 30 blocks from full player height, ignoring fluids. Adjacent hit-face position must see the sky. Spawn an ordinary nonvisual-only lightning bolt at integer block coordinates; spend one charge even in creative. No active particle packet is sent by this path.
- Rain instantly restores full charge. Held gems need only world rain (including under cover); dropped gems also require sky visibility. Dropped recharge skips that item update, as the original item hook does. Preserve enchantment-table sound/category/pitch conventions.
- Projectile retaliation targets only a mob true-source (original EntityLiving, not players). Each enabled charged available gem spawns lightning at attacker Y + 0.5, applies a one-tick amplifier-10 instant harm effect (instant health against undead), and spends one charge. It does not cancel the incoming attack.
- Being struck by lightning with positive charge grants Speed amplifier 2, Night Vision 0 and Resistance 0 for 1200 ticks, consumes six clamped charge, and stores a wall-clock cooldown per gem. Eligibility is strictly later than previous timestamp plus 1000 ms. No toggle gate and no lightning cancellation.
- Recipe: shaped quartz, four yellow dyes, sixteen redstone and eight iron bars. Original yellow/dun trillion animated and accessibility art.

## Native implementation and scope

StormwayItem implements targeted use, both-hand rain recharge, dropped rain recharge, retaliation and strike buffs. StormwayEvents uses native damage and lightning hooks on Forge/NeoForge; Fabric uses its damage event, the existing item-tick mixin, and a LightningBolt tick redirect that runs buffs immediately before the original Entity.thunderHit call. Original gameplay rules above are preserved with server-thread/live-player, loaded-chunk and permission guards. Acquisition and creative listing are Arsenal-gated; existing authoritative gem toggle and model-property wiring reused.

No intentional balance redesign proposed or approved. This checkpoint covers handheld/dropped behavior, not complete shared-gem or legacy event compatibility. Modern native lightning retains its version's world interactions; legacy damage-hook timing versus NeoForge incoming-damage timing, other mods' cancellation/effect behavior and Fabric redirect coexistence remain runtime/integration acceptance work. Socket/wearable availability, shared HUD/upgrades/powders, optional integrations and broader migration remain unfinished. Exact legacy undead categorization versus modern inverted-healing behavior for modded mobs is not acceptance-verified.

## Verification

`./gradlew assemble --no-daemon`, timeout 10 minutes, exit 0, wall time 20s; `build/stormway-integration-20260909-085558.log` reports BUILD SUCCESSFUL in 19s and all four compileJava tasks. Runtime/event classes, recipe, model and animation packaged on all four targets. Fabric lightning redirect contains remapped method_5800 and no named thunderHit target in both production JARs. `git diff --check` passed. Artifact class expectations updated; full verifier not run.

No gameplay tests, optimization, client/server launch, commits or publication. Assembly/remapping does not prove startup, damage behavior, multiplayer, weather, rendering or persistence. Migration remains incomplete and not ready for first playtest.
