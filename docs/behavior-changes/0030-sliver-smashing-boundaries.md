# 0030 — Sliver smashing runtime boundaries

Status: implemented with scoped headless verification under the user's delegated behavior-decision authority; connected-player acceptance remains open.

## Original behavior

Pinned release `bb99accf48ed583e29b0efae56e28c963407b8df`, `events/EventHandler.java:onLeftClickBlock` and `config/ConfigHandler.java:ServerSideConfig`: a server-side main-hand raw-quartz attack on any block rolls 0–99. A roll <= cluster chance consumes one raw quartz and produces a cluster; otherwise <= cluster + single chance produces one sliver without consuming quartz. Other rolls do nothing. Defaults are cluster 20, single 40, minimum 8, maximum 24. Cluster maximum is exclusive unless equal to minimum. Creative also loses quartz on the cluster branch. The tooltip's stone-only wording does not match the unrestricted handler and is retained as original text.

Legacy code consumes cluster payment before spawning, does not check spawn success, and has no local configuration validation. Item drops use the old event's hit vector and randomized velocity.

## Modern boundary and rationale

All four targets use their native attack event, server side and main hand only. Forge-family START is handled once, ignoring STOP/ABORT/client-hold and canceled/denied events. Fabric runs in an ordered phase after default callbacks and returns PASS, without canceling ordinary mining. Only living, non-spectator players with build permission, within six blocks of a loaded, non-air target inside the world border, and native world interaction permission can generate items. These explicit checks prevent early event hooks from bypassing vanilla access validation. Extended-reach integrations and later callback cancellation require separate acceptance; no custom client-authoritative item/count packet is added.

The drop uses the attacked face center (modern attack events do not carry the old precise hit vector), preserving the original velocity ranges. Cluster payment is deducted before insertion and refunded if insertion is rejected, preventing an entity-join callback from reusing unpaid quartz. Unchanged Java callback state is assumed; arbitrary third-party mutations are not transactional or crash-safe.

Configuration accepts each chance 0–100, and 1 <= minimum <= maximum <= 64. Invalid files remain untouched and fail loading through the existing settings loader. This rejects malformed/oversized cluster definitions rather than crashing during play or spawning illegal oversized stacks. Inclusive chance comparisons, free single-sliver branch, exclusive cluster upper bound and creative cluster cost are intentionally preserved, not silently rebalanced. Existing settings files use defaults for missing new keys.

## Compatibility and alternatives

Fresh-world block/item ID `arcanearchives:quartz_sliver`; six named facing states. No old-save importer, new dependency or network protocol. Rejected changing to right-click/crafting acquisition, stone-only targeting, consuming every single-sliver success, correcting probabilities to their labels, or silently clamping invalid settings. No resonator production bypass is introduced.

## Verification

Scoped matrix `build/sliver-20260908-121458.log` passes: 160 tests per Fabric leaf, 167 NeoForge, Forge test compilation only. Deterministic tests enumerate all default probability branches, exclusive/equal cluster bounds, settings round trips and invalid ranges; native block tests cover six outlines/light/collision/rotation/support. All artifact/resource checks and four dedicated-server state/loot fixtures pass; report `build/sliver-verification.json`. No connected player exercised the attack callback, access denials or rejected-spawn refund. Those cases and later callback/protection integrations remain explicit acceptance gaps, not covered by probability fixtures. Rendering remains deferred under the non-visual-first instruction.
