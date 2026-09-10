# Radiant Amphora native runtime

Status: partial feature implementation in the authorized migration continuation; all four targets. Full Amphora parity and gameplay acceptance remain open.

## Original behavior and evidence

Release `bb99accf48ed583e29b0efae56e28c963407b8df`: `items/RadiantAmphoraItem.java`, `items/DispenseAmphora.java`, `network/PacketRadiantAmphora.java`, `events/EventHandler.java`, `util/WorldUtil.java`, and `init/RecipeLibrary.java`. The MIT license was inspected before recovering assets.

The single-stack Amphora links to a Tank through sneak-use. Its default mode fills the linked Tank; drain mode places fluid from it. Left-clicking air sends a toggle request; sneak-left-clicking a block toggles on the server. A dispenser chooses pickup versus placement from its target instead of changing the item's saved mode. Links use position/dimension, and WorldUtil refuses unloaded chunks by default. The original recipe consumes four Radiant Dust, four clay balls and four gold nuggets for one Amphora.

## Current implementation

Native item/creative registration, the original Gem Cutter acquisition costs, saved link/mode, server lifecycle resolution, player pickup/placement and dispenser behavior are connected. Native mode packets carry no player-supplied Tank coordinates or fluid amounts; handlers schedule server work and validate the actual held Amphora. Dedicated-client event registration stays in the client entrypoints. Sneak linking checks live Tank access and native editing permission. Remote player operations check the linked level's native interaction permission; dispensers use the original non-player operation model.

Dimension IDs become native namespaced resource keys rather than obsolete numerical IDs. Newer items use CustomData components, older items use NBT. The link never stores a second copy of the Tank contents or forces a chunk load. This does not implement import of legacy 1.12 worlds or a new network/ownership policy.

Direct interaction with another placed Radiant Tank transfers between live storage handlers on every target, filling the linked Tank first and then attempting the reverse if no fluid moved. World operations use native bucket/block fluid behavior; Fabric reserves the full native bucket quantity transactionally. Forge-family pickup preflights capacity, inspects the actual returned bucket, then inserts its real fluid. An unexpected or no-longer-acceptable pickup is retained as its actual returned item (player inventory, then world drop) instead of throwing it away or inventing a fluid identity. Fabric likewise recovers a pickup that differs from the planned native bucket. Metadata-bearing Fabric variants without a matching ordinary world bucket are not placed; broader modded placement is still unfinished.

The first explicit toggle switches the default fill mode immediately, unlike the upstream missing-mode-tag branch that can initialize fill without switching. This native default-state treatment and the unexpected-pickup recovery are recorded behavioral differences, not claimed byte-for-byte parity.

Original unlinked/upright/tilted PNGs and fluid masks are recovered by `scripts/port_amphora_assets.py`; native item-property overrides expose the three base states. Original fluid-mask overlay rendering and remote fluid-name tooltip presentation remain unfinished, not replaced by fabricated fluid imagery. Original English/Portuguese item text is retained; new localized controls describe the migrated inputs.

## Open scope and verification

Generic item fluid capability/Transfer API registration is intentionally still missing; see [copied-item boundary](0043-amphora-copied-item-boundary.md). This is required unfinished integration, not an approved feature removal. Also pending: dynamic fluid masks, remote fluid tooltip data, advancement/tome integration, full modded-fluid parity, and connected-player/dispenser/protection/multiplayer/save-restart acceptance.

`./gradlew assemble --no-daemon` under the bounded full-log wrapper passed every supported compile/assemble leaf: exit 0, 16s, `build/amphora-final-20260908-191503.log`. `git diff --check` passed. No tests, clients, optimization, commits or publication. Artifact inventory expectations were extended; the full artifact verifier was not run. Compilation does not close the parent feature or establish first-playtest readiness.
