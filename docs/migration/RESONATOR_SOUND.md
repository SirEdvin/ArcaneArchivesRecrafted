# Resonator ticking-loop implementation

## Source contract

Pinned upstream `RadiantResonatorTileEntity.java:180–197`, `ImmanenceTileEntity.java:213–249`, and `types/MachineSound.java` implement a client-local looping `resonator.loop` sound in the BLOCKS category, at block center, pitch 1. The sound is eligible only while the device is valid, ticking and unobstructed. Defaults in `config/ConfigHandler.java:93–112`: use sounds true, resonator ticking true, ticking volume 0.15.

This restores reachable presentation, not new Resonator mechanics. Server growth, ownership, completion, cat motion and resource assets are unchanged. No upstream implementation was copied.

## Implementation

- `client/ResonatorLoopSound.java` uses native `AbstractTickableSoundInstance`; constructor/field signatures were inspected in mapped 1.20.1 and 1.21.1 artifacts.
- All loader client entrypoints install a sound-ticker factory. Shared block/entity code references only Java function/Runnable types, not Minecraft client classes. Each client block entity retains one ticker and at most one active loop.
- Eligibility uses the existing server-synchronized ticking state, current client world, loaded chunk, live block-entity identity and air above. The sound stops when those conditions cease to hold. Sound instances retain only weak entity references; there is no global world/entity collection.
- A cleared native sound manager can restart a still-eligible device's loop on a subsequent entity tick. This path still needs connected resource-reload acceptance.
- Local `config/arcanearchives/client.properties` now accepts `UseSounds=true`, `ResonatorTicking=true`, and `ResonatorVolume=0.15`. Settings follow the existing restart-to-apply policy. Existing files are not rewritten; missing keys receive original defaults. Boolean syntax and finite, nonnegative volume are validated; zero disables the loop.

Client `UseSounds` gates the migrated loop. Server completion-sound settings are now restored separately as described below. Brazier pickup configuration and remaining global sound wiring are still migration work; neither setting is claimed to control every mod sound yet.

## Completion-sound switches

Upstream `RadiantResonatorTileEntity.java:86–88` gates its server-side completion broadcast on both `resonatorComplete` and `useSounds`. The port now preserves that conjunction through `UseSounds=true` and `ResonatorComplete=true` in `config/arcanearchives/server.properties`. Missing keys retain those original defaults; existing files are not rewritten. Apply through the existing server configuration initialization (restart). Client ticking settings remain local and do not suppress the server broadcast.

Only the sound call is conditional: growth reset, cluster placement, cat motion, synchronization and comparator updates are unchanged. The existing BLOCKS category, position, recipients, volume 1 and pitch 1 remain unchanged. This is restored upstream behavior, not a new gameplay policy.

Verification: `timeout --foreground 10m ./gradlew build --no-daemon`, exit 0, 63s, `build/resonator-completion-20260910-205231.log`. All four leaves report six passing ServerSideConfigTest cases, zero failures/errors/skips; coverage includes both switches independently, round trips, original defaults and invalid-file/snapshot preservation. Artifact verification passed (`build/resonator-completion-artifacts.log`) and `git diff --check` passed. These checks do not establish connected audible completion behavior; that remains pending.

## Verification

- `timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 58s; `build/resonator-loop-20260910-204624.log`. Four supported leaves built. Each leaf's ClientConfigTest XML reports five tests, zero failures/errors/skips, including defaults, overrides, missing keys, invalid sound settings and preserving user files.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0; `build/resonator-loop-artifacts.log`. Exact production class inventories include the new sound class.
- `timeout --foreground 5m python3 scripts/smoke_servers.py`: exit 0, 139s; `build/resonator-loop-servers.log`. All four dedicated development servers initialized, reached readiness, saved and stopped; no project JVMs remained.
  - Fabric 1.20.1: `build/smoke-server-1.20.1-fabric-20260910-204801-557299.log`.
  - Forge 1.20.1: `build/smoke-server-1.20.1-forge-20260910-204839-899895.log`.
  - Fabric 1.21.1: `build/smoke-server-1.21.1-fabric-20260910-204916-738732.log`.
  - NeoForge 1.21.1: `build/smoke-server-1.21.1-neoforge-20260910-204959-114173.log`.

Compilation, configuration tests and server startup do not verify audible playback. Connected-client listening, volume/category controls, obstruction/completion transitions, chunk unload, reconnect and resource reload remain explicitly pending. Full Resonator network behavior and migration parity remain open.
