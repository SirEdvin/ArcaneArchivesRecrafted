# Mountaintear ranged runtime — placement safety approved

## Original behavior

Pinned bb99accf48ed583e29b0efae56e28c963407b8df, src/main/java/com/aranaira/arcanearchives/items/gems/pendeloque/MountaintearItem.java: onItemRightClick traces 40 blocks from eye height, ignoring fluids. For any hit it unconditionally writes default lava at the adjacent hit-face position with flags 11, calls the lava neighbor handler, and spends one charge unless creative. It does not check replacement suitability, a destination block entity, or whether the write succeeded. Capacity is 25/100. Dropped lava recharge and the fire-resistant EntityItemMountaintear were also inspected but are separate migration work.

## Proposed behavior

Before placement, require a loaded destination inside build/world limits, player interaction/use permissions, no destination block entity, and air or a natively replaceable destination state. Reject other destinations without replacing their contents. Spend the original one charge only after a successful native lava write, retaining the creative exemption. Keep the original range, ray origin, lava identity and recharge intent; this approval does not authorize changes to dropped-entity lifetime, damage immunity, sound or shared powder behavior.

## Reason

The unconditional world write can replace a destination without validating its contents; a direct port would retain a data-loss risk. This is source-based analysis, not a reproduced gameplay failure. Avoid silently changing destruction/payment semantics under a parity migration.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval status

User explicitly approved with “yes”. Implemented the approved guards and success-only payment in MountaintearItem, preserving the 40-block eye-height trace, lava state, flags 11 and creative exemption. Native lava placement supplies fluid scheduling. Registered the item and Arsenal-gated recipe (shaped quartz, four orange dyes, four magma blocks, lava bucket), original animated/depleted/accessibility pendeloque art and original localization.

Dropped lava recharge/custom fire-resistant entity, Lightwell, shared powder/socket/HUD/upgrade integration and runtime acceptance remain unfinished. In particular, do not throw this checkpoint's item into lava expecting recharge or protection yet. Original tooltip text is recovered, not proof those remaining features work.

## Verification

Pinned item/custom entity, acquisition and language sources inspected. `./gradlew assemble --no-daemon` under a ten-minute timeout exited 0 in 21s; `build/mountaintear-approved-20260909-091735.log` shows all four compileJava tasks and BUILD SUCCESSFUL in 20s. Runtime class, recipe, model and animation inspected in all four production JARs; `git diff --check` passed. No gameplay tests, optimization, client/server launch, commit or publication. Full artifact verifier not run. Overall migration remains incomplete and not ready for first playtest.
