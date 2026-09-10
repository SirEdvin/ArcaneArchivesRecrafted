# Salvegleam handheld healing and recharge

## Original behavior

Source pin: `bb99accf48ed583e29b0efae56e28c963407b8df`. Inspected SalvegleamItem, EventHandler living-player/death/left-click callbacks, GemUtil toggle/available-gem handling, PacketArcaneGems, RecipeLibrary and original asscher resources.

Salvegleam has 30 charge (150 with the power-upgrade bit), initially toggled off. Left-clicking air with it in the main hand toggles it; right-click returns success without an action. While held in either hand, on, charged and injured, it heals one health point per pulse and spends one charge. The first pulse is immediate, with subsequent pulses after 20 eligible updates. Its saved pulse counter pauses while inactive, stowed or healthy. Depletion turns it off.

A player-credited Animal death restores `(int) maxHealth / 2` charge to each available Salvegleam, capped at maximum. Reaching maximum turns it on automatically, including a previously disabled gem. This is not restricted to direct melee damage. Socket/wearable available-gem paths remain separate migration work.

The opt-in Gem Cutter recipe uses shaped quartz, four pink dyes, one golden apple, one glistering melon slice and one golden carrot. Original animated pink asscher, depleted and accessibility art are recovered.

## Native implementation

Shared `SalvegleamItem` implements held-item pulses and recharge. Fabric uses ServerLivingEntityEvents.AFTER_DEATH; Forge/NeoForge use native LivingDeathEvent listeners at LOWEST priority without receiving already-canceled events. The player/level/server-thread checks prevent client mutation and stale/cross-level player updates. Native heal handling remains in charge of applying health restoration.

`GemToggle` follows existing loader-native networking, dispatching onto the server thread and inspecting the sender's current main-hand gem rather than accepting item state or a target from the client. Only toggle-capable gems can be changed. Native client left-click-air callbacks send it; saved `toggle` and `pulse` data use NBT/custom components. Localized state/control hints expose the existing control without inventing a new keybind. Shared ArcaneGemItem helpers support actual toggle/pulse consumers; no external dependency was added.

Affected targets: 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Approval status: source-driven migration requested by the user, not a balance change. Native authoritative guards and exclusion of already-canceled death events protect event integrity. No original heal amount, pulse interval, recharge amount, default state or charge capacity was deliberately rebalanced.

## Open work and acceptance

Shared sockets/wearables, powder controls, HUD, remaining upgrade/unlimited-charge semantics and wider feature families are still unfinished. Gameplay/persistence/multiplayer/event-ordering/presentation acceptance remains deferred until the implementation pass is complete. This is not complete Arsenal migration or first-playtest readiness.

Related source correction: the pinned PacketArcaneGems.GemParticle client handler explicitly does nothing for pendeloque gems. There is no implemented upstream Rivertear/Parchtear beam effect to migrate at this pin; earlier beam-gap statements were inferred from packet naming and are superseded by this inspection.

## Integration evidence

`./gradlew assemble --no-daemon`: exit 0, 20s, `build/salvegleam-integration-20260909-055451.log`. All four compileJava tasks and assembly succeeded; `git diff --check` passed. Item/event/packet classes, recipe, model and animation sidecar are packaged on each target. Artifact class expectations are updated; the full verifier was not run. No gameplay tests, optimization, clients, commits or publication.
