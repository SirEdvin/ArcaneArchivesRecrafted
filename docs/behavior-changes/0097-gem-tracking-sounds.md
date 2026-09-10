# 0097 — Original gem tracking sounds

Status: implemented and assembly/package-verified on all four targets; connected audio acceptance remains open.

Source: master 80944ce45c6559243d8928cc4b305bf379388652, network/PacketArcaneGems.java:73–93 plays three listener-local sounds: OVAL/BLACK (Munchstone) burp at volume 1 and random pitch [0.75,1.25); PAMPEL/GREEN (Mindspindle) enchantment-table use at 1/1; TRILLION/ORANGE (Phoenixway) fire-charge use at 1/1. Pendeloque and other branches do nothing. Original Munchstone sends on eating and material recharge, Phoenixway on eligible fire placement and material recharge, Mindspindle on book recharge. Networking.java:83–85 delegates to Forge TRACKING_ENTITY; inspected Forge 1.12 FMLOutboundHandler confirms entity tracking recipients, not a sound-distance radius or explicit self-recipient.

Restore a bounded server-to-client sound enum using native entity-tracking distribution. Client playback stays at the receiving local player's position, as upstream. Replace the port's world-position Munchstone/Mindspindle approximations; add missing Phoenixway calls. No invented particles/beam, new recipients, audio assets, recipe changes or item costs. Preserve native server-authoritative operation/charge checks and do not register client-to-server handling.

Approval: source-backed fidelity restoration under standing migration/delegated behavior authority. No new gameplay deviation proposed.

Verification: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*GemSoundTest' --offline --no-daemon`, exit 0, 26s, `build/gem-tracking-sounds-20260909-212814.log`. Four targets assembled; NeoForge codec fixtures: 2 tests, no skips/failures/errors. All three effects round-trip in one byte; null, unknown, negative and truncated effects are rejected. This tests serialization, not audio delivery.

`timeout --foreground 60s python3 scripts/verify_artifacts.py`, exit 0, 0s, `build/gem-tracking-sounds-artifacts.log`; four production/source pairs pass including the packet/client classes. `git diff --check` passes. The inspected Forge 1.12 reference is saved in `build/forge112-outbound-reference.java` (upstream 1.12.x branch, not the mod release pin).

No client/server launched. Connected tracking/self-exclusion, listener-local sound playback, sound sliders and dedicated-server startup remain acceptance work. Native tracking recipients deliberately do not add the invoking player; restoring self-audible effects would be a separate behavior change. Charge-depletion recharge can emit its own sound before an action sound, retaining the original two-call ordering.
