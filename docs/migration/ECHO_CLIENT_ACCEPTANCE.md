# Echo connected-client acceptance — Fabric 1.21.1

## Scope

Executed the actual development client with its integrated server in a private Xvfb display, using a newly created creative world named `Echo verification isolated`. This verifies the restored Echo cache beyond title-screen startup and unit fixtures. It is not a remote multiplayer test and does not close connected-client acceptance on the other three targets. No production Java, recipe or asset was changed during this verification.

The implementation/approved compatibility contract is [0104](../behavior-changes/0104-echo-ore-cache-compatibility.md).

## Executed scenarios

1. Created the isolated world and gave a bound Echo through the native command:
   `/give @s arcanearchives:echo[arcanearchives:echo_source=[{slot:0,item:{id:"minecraft:iron_ingot",count:1}}]]`
   The server confirmed `Echo of Iron Ingot`. The actual item rendered without missing-texture geometry.
2. The first fallback lookup generated 19 unique smeltable ore IDs, including vanilla/deepslate/nether variants and ancient debris. No raw-material input was present. The color file was empty with the installed default textures; this is consistent with the retained upstream RGB-only raster rule, not a claim that every texture format should produce a color.
3. Installed a temporary resource pack replacing only `assets/minecraft/textures/item/iron_ingot.png` with a 16×16, three-channel RGB PNG containing `(18,52,86)`. Reopened the same world. Actual recipe/model/resource lookup generated `minecraft:iron_ingot,-15584170` (ARGB `0xff123456`). The Echo's middle layer visibly became blue. No cache value was manually supplied for this positive generation test.
4. Changed that temporary PNG to RGB `(240,16,32)` and invoked native `F3+T`. The log confirmed resource reload, and a separately granted iron ingot visibly used the red replacement texture. The Echo remained blue and the persisted color file remained byte-identical. This verifies the approved persistent-cache behavior, not automatic recoloring on reload.
5. Closed the client normally, waited for process exit/world save, and reopened the world without granting more Echoes. The cached blue color remained unchanged despite the red source texture. Native `/data get entity @s SelectedItem` confirmed the saved Echo stack still had count 2 and source component `{slot:0,item:{id:"minecraft:iron_ingot",count:1}}`. The count was 2 because the first and second sessions had each issued one explicit `/give`; the restart did not create another item.
6. Closed normally, moved only the test-generated populated color cache out of the client configuration, and restarted again. The existing Echo now generated `minecraft:iron_ingot,-1044448` (ARGB `0xfff01020`) and visibly became red. This proves delete-to-regenerate works through real client startup, native smelting selection and model sampling.

## Commands and logs

First session:
`timeout --foreground 10m ./gradlew :1.21.1-fabric:runClient --no-daemon --console=plain`

Subsequent sessions reopened the existing test world:
`timeout --foreground 10m ./gradlew :1.21.1-fabric:runClient --args='--quickPlaySingleplayer "Echo verification isolated"' --no-daemon --console=plain`

Each ran inside `xvfb-run -a -s '-screen 0 1280x800x24'` via a temporary Python wrapper, with complete Gradle/client output redirected to the log. Exit codes were read from the tracked processes, not inferred from screenshots. Durations below are measured log-write spans (log creation timestamp to last modification), not precise process-manager wall-clock timings.

| Session | Exit | Log span | Complete log |
| --- | --- | --- | --- |
| Default textures and initial grant | 0 | 255.93s | `build/echo-live-1789032033703747776.log` |
| Positive RGB sampling and resource reload | 0 | 183.58s | `build/echo-live-1789032371552075419.log` |
| Saved item and populated-cache restart | 0 | 137.29s | `build/echo-live-1789032579656979311.log` |
| Explicit removal and regeneration | 0 | 90.43s | `build/echo-live-1789032750632850168.log` |

All four logs contain normal client stopping and successful Gradle completion. Client/server save and shutdown occurred between sessions, rather than terminating a live test world forcibly.

## Evidence and cleanup

Screenshots, temporary driver, final test resource pack, generated input/color files, run metadata and the test world are archived outside the repository at:

`/tmp/arcane-echo-acceptance-1789032750632850168`

Useful screenshots there:
- `arcane-echo-uncolored.png`
- `arcane-echo-rgb-before-reload.png`
- `arcane-echo-rgb-after-reload.png`
- `arcane-echo-red-source-after-reload.png`
- `arcane-echo-after-restart.png`
- `arcane-echo-after-regeneration.png`

Restored the client's original `resourcePacks:["fabric"]` setting. Archived the test pack and generated cache rather than leaving the diagnostic colors in the development client. The uniquely created test world was moved to the evidence directory; other existing worlds were not changed. The temporary Xvfb displays and development game processes were shut down. `/tmp` evidence is local and may be removed by host cleanup; the scenario description and results remain in this document.

## Limits and remaining work

- This closes the sampled Iron Ingot Echo's real rendering, resource-reload cache retention, item/source save-restart and explicit cache regeneration scenarios on **Fabric 1.21.1 only**.
- Connected-client equivalents on Fabric 1.20.1, Forge 1.20.1 and NeoForge 1.21.1 remain open. Their previously passing matrix builds/fixtures/title-screen checks are not substituted for these scenarios.
- Remote multiplayer synchronization, other source tint providers/components, optional/modded recipes, and broader visual acceptance remain open.
- The headless client logged an unavailable OpenAL device; no audio acceptance is claimed. It also logged a missing `fake_air` blockstate-model variant. The migrated block explicitly returns `RenderShape.INVISIBLE`, but that separate resource warning has not been fixed or accepted by this Echo check. This is not a clean-log release claim.
- The broader migration remains incomplete; no dormant Echo producer or new gameplay was enabled.
