# Gem Cutter connected-client acceptance — Fabric 1.21.1

## Scope

Exercised the actual development client and integrated server with native mouse/menu-button interaction, in a private Xvfb display. This is no longer only a mock-player/menu relay test: a player placed the table, selected a shipped recipe, paid through the actual Craft button, saved a completed batch, restarted the whole client/server process and extracted the batch.

This covers Fabric 1.21.1 only, with the existing pretty GUI. It does not establish remote multiplayer, other loaders, simple-GUI mode, arbitrary screen sizes, authenticated-player identity persistence or crash recovery. No production Java/resources/build configuration changed.

## Verified scenario

1. Created a new survival, peaceful, commands-enabled fixture world. The UI truncated the requested name to the actual folder/name `Gem Cutter output verification i`; no older world was opened or modified.
2. Used native commands to create a stone platform at `0 249 0` through `8 249 8`, teleport to `2.5 250 2.5`, and grant exactly one Gem Cutter, two Raw Radiant Quartz and one Gold Nugget. These are fixture ingredients, not free recipe output or injected crafting state.
3. Placed the table through normal right-click interaction; the table item left the player's inventory. Opened its actual screen, navigated the recipe page and selected the shipped Radiant Lantern recipe. Its tooltip and four-item preview rendered while completed output stayed empty and the ingredients remained in inventory.
4. Clicked the preview without extracting it, then clicked **Craft**. Four Lanterns appeared in the real output slot; the player ingredients and preview cleared. A second Craft click did not create another batch.
5. Closed the menu and used native `/data get block 2 250 4 Crafting` and `/data get entity @s Inventory`. The table had one output entry with `ExtendedCount: 4`, `Stack.id: "arcanearchives:radiant_lantern"`, and empty inputs. Player inventory was empty. No `Crafting` state was injected.
6. Closed the client normally. The integrated server saved all dimensions and the process exited successfully. Started a separate client process and reopened the same newly created fixture world. Native queries again showed exactly four saved Lanterns and no table/player inputs, without any additional `/give` command.
7. Opened the real menu after restart. The saved four-item output rendered. Right-click extraction and inventory placement yielded two Lanterns in inventory and two left in the output. Extracted the remainder and consolidated the four items.
8. Picked up all four Lanterns and tried inserting them into the empty output slot. The output stayed empty and all four remained on the cursor. Returned them to inventory.
9. Final native queries showed empty table inputs/output and exactly one player inventory entry: `{count: 4, Slot: 9b, id: "arcanearchives:radiant_lantern"}`. Closed normally and verified complete save/shutdown again.

The development launcher used different generated player names between runs. This result establishes persistence of the player-placed table and its player-crafted output across process restart, not continuity of an authenticated multiplayer identity.

## Commands and execution evidence

Both sessions used the existing development run task through `xvfb-run -a -s '-screen 0 1280x800x24'` and a temporary Python input/capture wrapper. Complete Gradle/client output was redirected to the listed logs; durations are measured subprocess elapsed seconds.

| Session | Command inside private display | Exit | Seconds | Log |
| --- | --- | --- | --- | --- |
| Place/craft/save | `timeout --foreground 10m ./gradlew :1.21.1-fabric:runClient --no-daemon --console=plain` | 0 | 382.63 | `build/gem-cutter-live-1789129293504016928.log` |
| Restart/extract/save | `timeout --foreground 10m ./gradlew :1.21.1-fabric:runClient --args='--quickPlaySingleplayer "Gem Cutter output verification isolated"' --no-daemon --console=plain` | 0 | 233.01 | `build/gem-cutter-live-1789129705004722158.log` |

The second command's quick-play name did not match the UI-truncated folder, so Minecraft showed its missing-world screen. Recovered through **Back to World List**, selecting only `Gem Cutter output verification i`, and completed the scenario in that process. This is a harness argument error, not a gameplay defect; a future quick-play invocation must use the observed folder name exactly.

Both logs contain `Stopping!`, `All dimensions are saved` and `BUILD SUCCESSFUL`. The only ERROR lines were the existing unavailable headless SoundSystem; audio was not accepted. Existing minimum-mipmap/native sound/shader warnings are not a clean release-log claim. No project JVMs remained; both private display authority files were removed by `xvfb-run` shutdown.

## Screenshots and fixture preservation

Evidence and the exact new fixture world are archived outside the repository:

`/tmp/arcane-gem-cutter-live-1789129286634149878`

- `before-craft.png`
- `after-craft.png`
- `after-restart.png`
- `split-extraction.png`
- `output-insertion-refused.png`
- `extraction-complete.png`
- `small-window.png`
- `driver.py`
- `Gem Cutter output verification i/`

Only the newly created world was moved out of the development saves directory after both processes stopped. Existing worlds and resource packs were not changed. `/tmp` evidence is local and may be removed by host cleanup.

## Remaining acceptance

Subsequently, the user selected [0129 retaining upstream sizing](../behavior-changes/0129-gem-cutter-small-window-layout.md). Undersized/high-GUI-scale clipping is excluded from migration acceptance rather than repaired; the small-window observation below is historical evidence, not a pending adaptive-layout task.

At the initial 854×480 window, the existing tall GUI background clipped at its top/bottom. The main scenario used a 1280×800 private window, where the layout fit. A vanilla movement tutorial toast overlapped the upper-right UI; Craft remained clickable on its exposed portion. Neither observation was silently treated as universal visual acceptance or repaired here.

Other loaders, simple-mode appearance, remote packet/latency behavior, multiple connected clients and broader migration remain open. Small-window clipping is excluded under 0129. The earlier four-leaf build/native/artifact checkpoints still apply to unchanged production code; this increment adds real Fabric 1.21.1 client and process-restart evidence rather than substituting it for matrix verification.
