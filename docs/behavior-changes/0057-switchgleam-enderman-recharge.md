# Switchgleam Enderman suppression and recharge

## Original behavior

Pinned bb99accf48ed583e29b0efae56e28c963407b8df EventHandler.onEndermanTeleport examines players in a cube with half-extent 10.5 around the teleporting entity. Every available Switchgleam gains three charge, clamped to capacity, and the event is canceled. There is no toggle gate and no requirement for missing charge. The loop does not stop after one gem/player. Tooltip wording about toggling does not match the actual handler. Available socket/wearable gems remain a shared integration task.

## Native implementation

Shared SwitchgleamEvents applies this behavior to held gems in both hands of every nearby living nonspectator player on the authoritative server thread. Forge/NeoForge subscribe to native EnderEntity events, restricted to Endermen for this slice. Fabric injects immediately before EnderMan.randomTeleport after the native solid-ground/non-water eligibility checks, matching the inspected Forge hook phase. It returns false on suppression, so the native teleport/success sound path does not run. No charge or toggle gating was added.

No gameplay deviation proposed for this held-Enderman slice. Broader legacy EnderTeleportEvent coverage beyond Endermen (including the modern separate event families), sockets/wearables and runtime acceptance remain unfinished, not implicitly replaced or approved away.

## Integration evidence

./gradlew assemble --no-daemon: exit 0, 18s, build/switchgleam-enderman-20260909-082558.log. Four compileJava tasks/assembly passed; git diff --check passed. Event class packaged on every target; both Fabric mixin classes contain remapped entity references and no unmapped randomTeleport target. This is static integration evidence, not a runtime mixin/behavior test. Artifact class expectations updated; full verifier not run.

No gameplay tests, optimization, client/server launch, commits or publication. Migration remains incomplete and not ready for first playtest.
