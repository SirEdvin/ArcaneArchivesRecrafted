# Phoenixway ranged use and recharge

## Original behavior

Source pin bb99accf48ed583e29b0efae56e28c963407b8df: items/gems/trillion/PhoenixwayItem.java, init/RecipeLibrary.java, events/EventHandler.java and network/PacketArcaneGems.java. Original MIT notices remain packaged.

Charge capacity 75/300. Empty-gem right-click consumes up to three gunpowder from the first matching main-inventory slot and restores 25 charge per item, then returns without firing. No recharge-consumption message and no powder fallback in this item. Charged use casts 40 blocks from full entity height, ignores fluids, and places default ordinary fire in air beside the hit face. One charge is spent after an eligible placement attempt; creative has no exemption. Preserve these quirks rather than substituting flint-and-steel behavior or soul-fire selection.

## Native implementation

Shared PhoenixwayItem preserves that action/recharge structure with server-thread/live-player, loaded-chunk, permission, world-border and build-height guards. Uses native collision ray tracing and default FIRE state with ordinary update flags. Original orange/dun trillion models, animation, accessibility artwork and both original-language descriptions are recovered through the pinned asset script. Original recipe: shaped quartz, four blaze powder, four coal and flint-and-steel; Arsenal-gated acquisition/creative listing. No gameplay deviation proposed for this slice.

## Remaining implementation

This is not complete Phoenixway parity. The pinned attack handler grants 600 ticks of fire resistance against ON_FIRE/IN_FIRE only (not lava), consumes twelve clamped charge and cancels damage; its nonnegative-charge gate permits empty gems and existing fire resistance skips it. That native attack-hook integration remains next work, alongside shared available socket/wearable gems. The trillion-orange packet consumer actually plays FIRECHARGE_USE locally on each tracking client for both recharge and firing; that presentation is still missing, not a no-op and not silently replaced by a positional sound. Shared HUD/upgrades and runtime acceptance also remain open.

## Integration evidence

./gradlew assemble --no-daemon: exit 0, 21s, build/phoenixway-ranged-20260909-084406.log. All four compileJava tasks and assembly passed; git diff --check passed. Runtime class, recipe, model and animation packaged on every target. Artifact class expectations updated, including the two previously omitted Fabric-only mixins from preceding checkpoints. Full verifier not run. No gameplay tests, optimization, client/server launch, commits or publication. Migration remains incomplete and not ready for first playtest.
