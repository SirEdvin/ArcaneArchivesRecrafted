# Radiant Resonator production information — JEI and EMI

## Source-backed restoration

Under approved integration scope 0113, restored the upstream Resonator production page in JEI and supplied its EMI counterpart. Source pin: `80944ce45c6559243d8928cc4b305bf379388652`, inspected in `/tmp/arcane-archives-reference/upstream`:

- `integration/jei/JEIPlugin.java:64–66` registers the informational quartz recipe and Resonator catalyst.
- `integration/jei/quartz/QuartzWrapper.java:22–32` supplies Raw Radiant Quartz as output lookup and formats the configured tick interval as minutes.
- `integration/jei/quartz/QuartzCategory.java:16–17` supplies the original 22×62 diagram.
- `config/ServerSideConfig.java:12–13` declares the local configuration field, default 6000 ticks. The inspected direct references contain no network writer for that field.

This is production information, not a machine-item crafting recipe, inventory grant, or new timed machine implementation. It retains upstream's Raw Radiant Quartz lookup rather than inventing an input cost or changing cluster growth/harvesting.

## Implementation

- Shared client-only `integration/ResonatorDisplay` provides detached output, artwork identity and localized configured interval.
- Category ID: `arcanearchives:radiant_resonator`.
- Synthetic informational recipe ID: `arcanearchives:/resonating/raw_quartz`.
- JEI registers the category, one informational recipe, invisible output lookup and Resonator catalyst. Original diagram and caption are centered in a panel large enough to contain both.
- EMI registers the same information/workstation, with empty ingredient inputs and recipe-tree support explicitly disabled. No transfer handler or output-grant path is added for this page.
- Existing Gem Cutter displays and Radiant Crafting Table transfer remain unchanged. The Gem Cutter's user-selected fixed screen sizing is not altered.

The caption uses the local `ServerSideConfig.current().resonatorTickTime()` divided by 20 and 60, matching the original wrapper's calculation, and the original `jei.gui.resonator=Every %.1f minutes.` text. It is **not remote-server configuration synchronization**. Remote settings differing from local settings, nondefault configured timing, and broader configuration synchronization remain separate acceptance; this increment does not silently invent a network/settings layer.

## Artwork provenance and packaging

Copied `assets/arcanearchives/textures/gui/jei/radiant_resonator.png` byte-for-byte from the pinned MIT upstream. SHA-256: `f4998ffbe729d418d94b890ef2902d573d4ab4c068f24215c4cb34412677e5de`. Existing upstream notices remain packaged. The artifact verifier explicitly checks this hash and the added classes; a negative control that altered only the ZIP-read result failed with `changed pinned Resonator viewer artwork`. No shipped JAR was modified for that control.

## Validation

- Initial `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0, 106s, `build/resonator-viewers-20260911-134651.log`.
- Final identical four-target command: exit 0, 75s, `build/resonator-viewers-synthetic-20260911-135809.log`. Native required suites pass 7/8/7/7; both Fabric XML reports contain seven passing cases without failure/error/skipped children. No ERROR/FATAL/FAILED lines in the final build log.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, reported duration 0s, `build/resonator-viewers-artifacts-final.log`; all four production/source pairs pass. These default native suites do not test installed viewer rendering.

Real Fabric 1.21.1 clients used private Xvfb, a separate copy of the isolated prior EMI world, and each optional viewer separately. Command inside the private display:

`timeout --foreground 10m ./gradlew :1.21.1-fabric:runClient --args='--quickPlaySingleplayer "Resonator viewer verification"' --no-daemon --console=plain`

Final sessions:

| Profile | Exit | Seconds | Log |
| --- | --- | --- | --- |
| `ORG_GRADLE_PROJECT_optionalIntegrationMods=emi` | 0 | 149.64 | `build/resonator-viewers-live-1789135188602894264.log` |
| `ORG_GRADLE_PROJECT_optionalIntegrationMods=jei` | 0 | 87.51 | `build/resonator-viewers-live-1789135351792968921.log` |

In both actual UIs, searching Raw Radiant Quartz exposes the Resonator category, localized title, original diagram, Resonator workstation and **Every 5.0 minutes.** caption. The page has no usable transfer/craft action. Final native inventory queries still show only the fixture's four Lanterns and one Crafting Table, with no granted quartz. Both clients saved all dimensions and stopped normally; only the known headless SoundSystem error remained in the final logs. No project JVMs remain.

### Diagnostics caught during development

The first EMI run found that using `arcanearchives:radiant_resonator` for the informational recipe collides with the real recipe that crafts the machine. The next run required EMI's leading-slash convention for recipes absent from the native recipe manager. The final dedicated synthetic ID fixes both diagnostics; final EMI reload has neither. Historical logs: `build/resonator-viewers-live-1789134713759210669.log` and `build/resonator-viewers-live-1789134933742933224.log`. These owned-page defects are separate from the user-accepted external bridged-tag limitation in 0130, which was not changed or suppressed.

Screenshots and owned fixture archive: `/tmp/arcane-resonator-viewers-1789134557732260285/` (`jei-resonator-final.png`, `emi-resonator.png`, `Resonator viewer verification/`).

## Remaining scope

Other-loader real rendering, remote configuration correctness, combined-viewer behavior for this new page, wider reload/localization, and actual machine growth/harvest/multiplayer acceptance remain open. This restores the informational integration, not complete Resonator/network or migration parity.
