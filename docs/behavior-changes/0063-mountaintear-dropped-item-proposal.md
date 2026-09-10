# Mountaintear dropped-item lifecycle — approved and implemented

## Original behavior

At bb99accf48ed583e29b0efae56e28c963407b8df, EventHandler.onEntityJoinedWorld replaces every ordinary Mountaintear EntityItem with EntityItemMountaintear on the server. It copies position, stack and motion, calls setDefaultPickupDelay, and kills the original. It does not copy owner, thrower, age, health or other entity metadata. EntityItemMountaintear rejects fire damage both in attackEntityFrom and isEntityInvulnerable.

MountaintearItem.onEntityItemUpdate restores missing charge when in lava, plays the enchantment-table sound, and returns true even when already full. The true return skips normal item updating; this is not merely a one-time recharge. The item also includes a fallback replacement path for noncustom entities in lava.

## Proposed behavior

Use native dropped-item fire protection without replacing the entity, preserving ownership, pickup delay, age and other native metadata. Restore full charge on server-side lava contact and play the recharge sound only when charge is actually restored. Continue normal item movement, pickup-delay countdown and despawn aging in lava rather than indefinitely skipping item ticks.

Retain the original full-charge refill and immunity to fire/lava damage. This does not add immunity to explosions or other damage. It does not change the already approved handheld lava placement, costs or creative exemption.

## Reason

Avoid resetting ownership/lifetime on entity replacement and keep a dropped gem recoverable through ordinary item physics and pickup behavior. A direct preservation of the unconditional skip can freeze normal item updates while in lava. These are source-derived concerns, not reproduced gameplay failures. The lifecycle and repeated-sound adjustments are separate from the previous placement-safety approval.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval status

User explicitly approved with “yes”. Implemented native Item.Properties.fireResistant through a properties overload of ArcaneGemItem (other gems retain their existing properties). MountaintearItem.rechargeInLava publishes a charged stack copy into the same live server ItemEntity, sounding only on an actual refill. Forge-family onEntityItemUpdate returns false; Fabric reuses the existing item-tick injection without cancelling Mountaintear ticks. No entity replacement or entity-metadata setters are used. Lightwell and shared gem systems remain unfinished.

## Verification

Inspected pinned EventHandler.onEntityJoinedWorld, MountaintearItem.onEntityItemUpdate and EntityItemMountaintear, plus native ItemEntity fire protection/ticking. `./gradlew assemble --no-daemon` under a ten-minute timeout exited 0 in 24s; `build/mountaintear-lifecycle-20260909-112734.log` reports BUILD SUCCESSFUL in 23s and compilation for all four targets. Inspected production recharge method and loader-hook packaging on all four targets. `git diff --check` passed. No gameplay tests, optimization, clients/servers, commits or publication. Full artifact verifier not run. Migration remains incomplete and not ready for first playtest; gameplay/persistence/multiplayer acceptance is deferred.
