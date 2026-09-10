# Orderstone runtime — client synchronization approved

## Original behavior

Pinned upstream: `bb99accf48ed583e29b0efae56e28c963407b8df`. Inspected OrderstoneItem, EventHandler.playerPickupXP, GemUtil available-gem ordering, RecipeLibrary and original oval assets/localization. Existing packaged MIT notices cover recovered resources.

Runtime charge is 100, or 400 with the power-upgrade bit. Actual onItemUse logic, not the unused DEFAULT_ENTRIES strings/incomplete parser, defines transformations:

- Gravel or mossy cobblestone to cobblestone; cobblestone to stone: cost 1.
- Sand to coarse dirt to dirt to mycelium to podzol to grass: cost 1 per step.
- Cracked or mossy stone bricks to stone bricks: cost 1.
- Stone bricks to chiseled stone bricks: cost 4.
- Damaged anvil to chipped anvil to intact anvil: cost 25 per step, retaining orientation.

Any positive charge permits a transformation even if less than its cost; charge then clamps at zero. Creative does not waive charge. The original returns success for server-side uses with positive charge even when the block has no conversion. Its block-update flags are 0.

XP-orb pickup restores held Orderstones without reducing the player's XP. Original iteration is main hand then offhand. Thus an offhand Orderstone sees the boosted value when a charged Mindspindle is in the main hand; reversing hands restores the unamplified value. The shared existing pickup loop preserves this order before native mending and retains its merged-orb restoration boundary.

Recipe: shaped quartz, four pink dyes, intact anvil and thirty original signs (mapped to oak signs). It remains Arsenal-gated. Original pink oval animated/depleted/accessibility resources and tooltips are recovered.

## Implementation and approved compatibility change

Native Orderstone use checks server thread, living nonspectator player, loaded target, interaction/item-use permission and absence of a block entity. Only successful native writes spend charge. Split modern block IDs replace legacy metadata; anvil properties are retained. Registration, creative gating, item predicates, acquisition and XP recharge are connected across 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

Modern Level.markAndNotifyBlock only sends block updates when flag 2 is present; direct neighbor notifications are independently flag 1. Original flags 0 can therefore leave clients seeing stale transformed blocks. Approved change: use Block.UPDATE_CLIENTS, keeping costs and lack of direct neighbor notifications unchanged.

Approval status: explicitly approved by the user: "For sure, apply it" in response to the client-synchronization request. UPDATE_CLIENTS is now applied. The earlier timed-out clarification and temporary flags-0 state are superseded; no approval blocker remains.

## Integration evidence and open work

Approved-state assembly: ./gradlew assemble --no-daemon, exit 0, 19s, build/orderstone-approved-20260909-062923.log; git diff --check passed. This supersedes the flags-0 checkpoint below.

Initial assembly: exit 0, 21s, build/orderstone-integration-20260909-061057.log (proposed client-sync state, now superseded).

Previous source-faithful flags checkpoint: ./gradlew assemble --no-daemon, exit 0, 19s, build/orderstone-source-flags-20260909-061411.log. All four compileJava tasks/assembly passed; git diff --check passed. Class, recipe, model and animation packaged on all four targets. Artifact class expectations updated; full verifier not run.

Shared gem sockets/powder/HUD/upgrades and wider migration remain open. No gameplay tests, clients/servers, optimization, commits or publication. Compilation does not establish runtime acceptance or first-playtest readiness.
