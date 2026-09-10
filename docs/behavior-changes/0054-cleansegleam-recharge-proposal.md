# Cleansegleam handheld runtime — milk recharge approved

## Original behavior

Source: upstream pin `bb99accf48ed583e29b0efae56e28c963407b8df`, `src/main/java/com/aranaira/arcanearchives/items/gems/asscher/CleansegleamItem.java`, onItemRightClick.

When the held gem has zero charge, the server loops over the player's item-handler slots. Each slot containing MILK_BUCKET loses one milk bucket; an empty bucket is inserted back or dropped if rejected. The loop does not stop after a match. No restoreCharge call or other charge restoration appears in this branch. Consequently it consumes milk without replenishing the gem.

The charged branch removes hunger, nausea and poison from the player and, while sneaking, nearby living entities. Native cleansing is now implemented: one charge per affected entity, a 3.5-block half-extent box, and original positive-charge activation/clamped final payment. The upstream caller always passes false for the matter-upgrade effect expansion. Optional PotionCore behavior remains unimplemented and explicitly deferred.

## Approved behavior and rationale

Consume exactly one milk bucket from the player's eligible inventory, conserve its empty bucket via inventory insertion or an ordinary drop, and fully restore the empty held gem. Stop after the successful payment. Nonempty gems retain their cleanse action. Do not consume milk on failed validation or without an actual matching stack.

This deliberately fixes original behavior rather than claiming strict parity: it prevents repeated milk loss and makes the documented recharge usable. Affected targets: Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval and verification

The user explicitly approved: "Yes, apply your suggestion". The earlier timed-out clarification is superseded. Implemented native server-thread/alive/nonspectator/held-item validation, one-milk payment, full recharge, empty-bucket return with drop fallback, consumption message and inventory synchronization. No milk is consumed for a nonempty gem or absent matching inventory stack; creative use does not waive payment. Inventory traversal includes native main, armor and offhand slots.

Registration, Arsenal-gated creative entry and original Gem Cutter acquisition are connected: shaped quartz, four blue dyes, poisonous potato and one milk bucket. Recovered original blue/dun asscher models, animated texture and accessibility variants using the pinned asset recovery helper. Upstream MIT license inspected; existing notices retained. Native recipe processing retains its existing container-remainder path.

Verification: ./gradlew assemble --no-daemon, exit 0, 21s, build/cleansegleam-approved-20260909-064824.log. All four compileJava tasks and assembly passed; git diff --check passed. Runtime class, recipe, model and animation verified in all four production JARs. Artifact class expectations updated; full verifier not run.

No gameplay tests, client/server launch, optimization, commits or publication. Shared sockets/powder/HUD/upgrades, PotionCore and other optional integrations, and runtime acceptance remain open. The original Amphora milk recharge is a TODO in the pinned item despite guidebook wording; it was not silently implemented via unsafe remote item capabilities. Overall migration remains incomplete and not ready for first playtest.
