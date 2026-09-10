# 0105 — Registered Wonky Resonator runtime

Status: implemented under the approved reachable-content scope. No new explosion, output, recipe, ownership limit or integration is introduced. Four-target builds, timer fixtures, dedicated-server lifecycle/restart and client model-loading checks pass; interactive rendering/multiplayer acceptance remains open.

## Original behavior

Follow-up: [0107 — hand-transform parity](0107-wonky-hand-transform-parity.md) repairs silently ignored legacy perspective names in this slice's initial model conversion. Its native parser tests supersede the initial hand-transform metadata, not the server timer/persistence evidence below.

Development pin `80944ce45c6559243d8928cc4b305bf379388652` registers `WONKY_RESONATOR` in both block/item registries, creative inventory, model registration and block-entity registration. It is not an unregistered dormant class, despite its placement under a model-parts comment. `blocks/WonkyResonator.java` and its texture are absent from release pin `bb99accf48ed583e29b0efae56e28c963407b8df`; the approved scope includes reachable development-baseline content.

The block is iron-material, hardness 3, wooden-pickaxe harvest level, cutout, nonopaque and nonsuffocating. It uses the original `makeshift_resonator.obj` model and a gold `arcanearchives.tooltip.device.wonky_resonator` tooltip. No recipe or localized Wonky name/tooltip was found in the inspected development Java, recipe and language resources. Creative/command acquisition is retained; no survival recipe or invented prose is added.

`WonkyResonatorTileEntity.update` marks dirty on the server each tick. With air above, growth increments while below `ServerSideConfig.ResonatorTickTime`; on the following tick after reaching the limit it resets to zero. Obstruction pauses progress without resetting. It requires no owner or online player and produces neither quartz nor an explosion: the explosion is literally an upstream TODO. The unused client tick counter and unused flags do not produce behavior.

Growth persists as `current_tick`. Reading a tag without that key leaves existing growth unchanged. Native update packet/tag include the state. The progress percentage is floor(growth / configured duration × 100), without new clamping. Breaking produces the ordinary block item, not a saved-growth portable device.

## Port and compatibility

Shared `WonkyResonator` / `WonkyResonatorBlockEntity` restore those contracts using existing registry, native block-entity packet/NBT and Stonecutter patterns. New-world block-entity identity uses the existing modern registry convention; old-save conversion remains out of scope. The server-only ticker guards unbound/client entities. No new gameplay validation policy or approval decision is needed for this parity restoration.

`scripts/port_wonky_resonator_assets.py` recovers the pinned OBJ and original texture byte-for-byte, normalizes only the vanilla water texture path in its MTL, and translates the legacy Forge blockstate/defaults to modern model JSON. The previously reviewed upstream MIT license and retained notices apply. The source texture has no animation sidecar. Fabric uses the existing shared OBJ adapter; Forge/NeoForge use their native OBJ loaders. Explicit atlas inclusion preserves the original plural `blocks/` texture path. Vanilla self-drop loot and pickaxe tags replace the old implicit block-drop/harvest registration.

The existing build scripts explicitly enumerate model expansion rules. Added this model to all three scripts. Client verification then exposed a stale incremental build: changing a `filesMatching` expansion rule alone left `processResources` UP-TO-DATE and the old `${obj_loader}` placeholder in the JAR. `inputs.file(project.buildFile)` now makes each script's resource rules an actual input. This is a build-cache correctness fix, not a loader/toolchain replacement. No clean/rerun-only workaround is required.

## Verification

- `timeout --foreground 10m ./gradlew build --no-daemon`: final exit 0, 48s, complete log `build/wonky-final-20260910-100456.log`.
- Three `WonkyResonatorBlockEntityTest` cases pass on every target: exact limit/following-tick reset, blocked pause, configured limits and existing out-of-range behavior. Actual XML testcase entries were counted; Forge uses its required native runtime report, not a skipped plain-JUnit substitute.
- `python3 scripts/verify_artifacts.py`: four production/source pairs pass. It checks the new production classes, exact expanded loader, OBJ resource path and copied resource bytes, without weakening test isolation.
- `git diff --check`: pass.
- `timeout --foreground 10m python3 scripts/smoke_wonky_resonator.py <leaf>`: two real dedicated-server sessions on each target, all eight exit 0 and save normally. Native commands verify obstruction preserves 5999, unblocking wraps into early progress, no block/output/explosion replaces the device or air, pickaxe tag and item/self-drop resolution, and exact `current_tick:777` after a complete server save/shutdown/restart while obstructed. Three fixture positions are only modified after an all-air guard. The fixture requires the existing default tick duration rather than changing configuration; loopback/EULA preflight is reused. Fixtures, temporary scoreboard and forced chunk are removed after restart acceptance.
- `timeout --foreground 10m xvfb-run -a -s '-screen 0 1280x800x24' python3 scripts/smoke_clients.py <leaf>`: all four final clients load atlases/models and shut down normally. An earlier Forge attempt correctly failed on the unexpanded model; it was repaired, not ignored.

Per-session commands, measured durations, all assertion markers, full log paths and client screenshot locations are aggregated in `build/wonky-acceptance.json` (8 server records, 4 client records and per-target fixture names). Wrapper logs:

| Target | Server pair log | Final client log |
| --- | --- | --- |
| 1.20.1 Fabric | `build/wonky-server-1.20.1-fabric-20260910-095847.log` | `build/wonky-client-1.20.1-fabric-20260910-100544.log` |
| 1.20.1 Forge | `build/wonky-server-1.20.1-forge-20260910-095935.log` | `build/wonky-client-1.20.1-forge-20260910-100607.log` |
| 1.21.1 Fabric | `build/wonky-server-1.21.1-fabric-20260910-095749.log` | `build/wonky-client-1.21.1-fabric-20260910-100630.log` |
| 1.21.1 NeoForge | `build/wonky-server-1.21.1-neoforge-20260910-100029.log` | `build/wonky-client-1.21.1-neoforge-20260910-100652.log` |

The server scenarios preceded the final model-expansion-only build correction; production timer/NBT/loot code did not change afterward. Final clients used the repaired resources. No development game JVM remains running. These are meaningful native runtime checks, not proof of connected-player visual transforms, every mod interaction, or complete migration parity. Missing original translations remain missing; the prototype is not advertised as a functioning alternative quartz generator.
