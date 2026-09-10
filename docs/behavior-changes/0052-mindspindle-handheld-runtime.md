# Mindspindle handheld XP amplification and book recharge

## Original behavior and provenance

Inspected upstream pin `bb99accf48ed583e29b0efae56e28c963407b8df`: MindspindleItem, EventHandler.playerPickupXP, GemUtil, ArcaneGemItem.informPlayerOfItemConsumption, PacketArcaneGems.GemParticle, RecipeLibrary, language files and pampel model/texture dependencies. Original MIT notices remain packaged.

Mindspindle has 800 charge, or 3600 with the existing power-upgrade bit. Each available charged gem changes picked-up orb XP using `Math.round(value * 1.5F)` and consumes the incoming value as charge. A partially charged gem still applies the full multiplier, then clamps at zero; multiple held gems apply sequentially. No toggle or creative charge exemption is imposed by the original handler.

Right-clicking an empty gem consumes one ordinary book from main inventory and restores its full charge, including in creative. Nonempty gems do not consume a book. The original action-bar consumption message and enchantment-table sound accompany recharge. Powder fallback, sockets and the shared recharge button are separate unfinished integration paths.

The original opt-in Gem Cutter recipe consumes one shaped quartz, four green dyes, one enchanted book and two emeralds. Original green pampel animated, depleted and accessibility artwork is recovered by the pinned asset script.

## Native implementation and compatibility

`MindspindleItem` supplies authoritative book payment/recharge and shared per-pickup amplification. Native saved charge state and item predicates are reused. Both hands work; stowed gems do not grant the handheld effect.

`MindspindlePickupMixin` applies the boost after native pickup admission (including Forge-family cancellation hooks) and before native take/mending/XP handling. It temporarily exposes the amplified value to native mending, then restores the original value on normal return. Modern orbs hold a count of separate pickups; restoring the unconsumed units prevents accidental repeated multiplication of the remaining orb. Native decrement/discard logic remains untouched. The removed legacy orb pickup-delay field is not emulated by changing the player's independent pickup cooldown.

This adds the first shared gameplay mixin across all four leaves. Fabric remaps annotations through Loom. NeoForge declares the common config in its metadata. Legacy Forge uses the existing ModDevGradle plugin's `mixin` extension, the documented Mixin 0.8.5 annotation processor, an SRG refmap and a production `MixinConfigs` manifest entry. Forge has a separate small config for its refmap; gameplay implementation is shared. The processor is build-only, not a new bundled runtime dependency.

Affected targets: Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Approval scope: requested source-driven migration with authoritative validation and preservation of per-consumed-pickup semantics; no deliberate multiplier, capacity, recipe-cost or book-recharge rebalance. Native event timing is explicitly after admission, rather than the original HIGH-priority event subscription. Compatibility with other mods mutating/canceling pickup during injection remains part of deferred runtime acceptance.

## Remaining scope

Shared socket/wearable discovery, powder fallback/controls, HUD, remaining gem upgrades/unlimited-charge semantics and wider migration remain open. Native spatial sound dispatch replaces the old particle packet's sound side effect; exact observer presentation needs deferred acceptance. This does not establish complete Mindspindle/Arsenal parity or first-playtest readiness.

## Integration evidence

- Initial `./gradlew assemble --no-daemon`: exit 0, 27s, `build/mindspindle-integration-20260909-060439.log`.
- Final assembly after restoring the consumption message: exit 0, 20s, `build/mindspindle-final-20260909-060622.log`.
- All four compileJava tasks and assembly succeeded; `git diff --check` passed.
- Item/mixin classes, recipe, model and animation were inspected in all four production JARs. Fabric annotations resolve to intermediary `method_5694` and the Player take target; Forge refmap maps pickup/take to SRG and its shadow field is reobfuscated. Java compatibility is 17/21 by target; production Forge manifest selects its refmap-bearing config.
- Artifact class expectations are updated. No full artifact verifier, gameplay tests, clients/servers, optimization, commits or publication were run. Static packaging is not runtime mixin acceptance.
