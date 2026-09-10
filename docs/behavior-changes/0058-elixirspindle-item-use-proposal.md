# Elixirspindle handheld runtime — potion-only client scope approved

## Original behavior

Inspected upstream bb99accf48ed583e29b0efae56e28c963407b8df: items/gems/pampel/Elixirspindle.java and events/EventHandler.java onItemUse (lines 555–592).

The event subscriber accepts the LivingEntityUseItemEvent parent. Server handling checks the main-hand stack is an ItemPotion before cancellation. Client handling cancels whenever any available charged Elixirspindle exists, without checking the used item. This can interfere with unrelated item use; no runtime reproduction was performed.

Server potion handling checks the base potion's noninstant effects for any already-active effect. If none are active it applies copies of all stack effects and consumes one gem charge, without consuming the potion. Potion use is canceled even when an effect was already active. The parent-event phase and main-hand lookup require careful preservation during migration, not assumptions based on tooltip text.

The item has 5/20 capacity. Empty-gem recharge consumes up to five nether wart from the first matching main-inventory stack, restoring one charge per item and showing a consumption message, otherwise falling back to shared powder recharge. Purple pampel particle packet has no implemented consumer effect in the previously inspected packet handler.

## Proposed change

Limit client interception to the same potion-use scope as the authoritative server. Leave unrelated food, bow and other item-use actions alone. This fixes the client/server mismatch rather than preserving the original broad client cancellation. Affected targets: Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Keep other potion/charge semantics separate from this proposal.

## Approval and evidence

Explicit user approval: “yes i approve”. The earlier unanswered clarification is superseded. Implemented the shared potion-only scope on client/server, native potion-effect copies with original base-effect reuse guard and one-charge payment, original empty-gem nether-wart recharge/message, and pinned purple pampel art/animation/accessibility and Arsenal-gated recipe. Forge/NeoForge use cancellable Start/Tick/Stop events; Fabric uses corresponding native start/update/release hooks. Finish is not cancellable in modern Forge-family APIs; unusual late gem changes/Finish and broader parent-event compatibility remain an explicit integration gap rather than a fabricated cancellation path. Shared sockets/wearables, powder recharge, HUD/upgrades and runtime acceptance remain open.

`./gradlew assemble --no-daemon`: exit 0, 20s, `build/elixirspindle-approved-20260909-083855.log`; all four compileJava tasks passed, Gradle success in 19s. Class/recipe/model/animation packaging checked across all targets; Fabric mixin contains remapped LivingEntity references and no unmapped startUsingItem target. `git diff --check` passed. Full artifact verifier and gameplay tests were not run. No optimization, client/server launch, commits or publication. Overall migration remains incomplete and not ready for first playtest.
