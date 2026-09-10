# Fake Air resource parity repair

## Source and change

The Fabric 1.21.1 Echo live test exposed `Exception loading blockstate definition: 'arcanearchives:blockstates/fake_air.json' missing model for variant: 'arcanearchives:fake_air#'`.

Inspected upstream MIT license and development pin `80944ce45c6559243d8928cc4b305bf379388652`, specifically `blocks/FakeAir.java`, `blockstates/fake_air.json` and `textures/blocks/placeholder.png`. Upstream declares invisible rendering but still supplies a cube-all placeholder model through legacy Forge blockstate defaults. The port retained `RenderShape.INVISIBLE` but omitted that resource definition.

Restored a modern empty-key blockstate variant and cube-all model, copying the original placeholder PNG byte-for-byte. SHA256: `bad6a2e10f26870b8eca38e28b3f142255433c1b5fada885e63c803dd03e0753`. Its original plural `blocks/` texture path is preserved with an explicit modern block-atlas source. Existing upstream license packaging remains in place.

No Java, gameplay, acquisition, collision, cooldown, liquid exclusion or rendering-shape behavior changed. This is a missing-resource parity repair, not a proposed gameplay deviation. The placeholder remains invisible in normal block rendering as before.

## Regression protection

`scripts/verify_artifacts.py` now requires the exact modern blockstate/model, packaged texture matching the source and explicit atlas entry in every target JAR. Before the repair it failed with exit 1 because the blockstate was absent.

`scripts/smoke_clients.py` now rejects Arcane Archives `Exception loading blockstate definition` warnings as well as its existing model/texture failures. Applying its actual regex to the old Echo log rejects the previously missed warning.

The first client attempt correctly failed acceptance despite game exit 0: the restored legacy texture path was not yet stitched into the atlas. Log: `build/fake-air-client-1.20.1-fabric-20260910-094422.log`. Adding the explicit atlas entry fixed that cause; no warnings were suppressed.

## Verification

Final command: `timeout --foreground 10m ./gradlew build --no-daemon`, exit 0, 46s, complete output `build/fake-air-final-20260910-094541.log`. All four binary/source artifact pairs pass `python3 scripts/verify_artifacts.py`; `git diff --check` passes.

For each target, executed `timeout --foreground 10m xvfb-run -a -s '-screen 0 1280x800x24' python3 scripts/smoke_clients.py <target>`. Each wrapper saves complete output, and each underlying client uses a bounded Gradle `runClient --no-daemon --console=plain` command. All four report game exit 0, acceptance true, loaded atlases/models and normal window shutdown. No Fake Air warning/error remains in these logs.

| Target | Measured client duration | Complete client log |
| --- | --- | --- |
| 1.20.1 Fabric | 22.69s | `build/smoke-client-1.20.1-fabric-1789033588058959995.log` |
| 1.20.1 Forge | 23.70s | `build/smoke-client-1.20.1-forge-1789033610834341841.log` |
| 1.21.1 Fabric | 21.70s | `build/smoke-client-1.21.1-fabric-1789033634616975710.log` |
| 1.21.1 NeoForge | 22.20s | `build/smoke-client-1.21.1-neoforge-1789033656396795356.log` |

Machine-readable aggregated results, including screenshot locations outside the repository: `build/fake-air-acceptance.json`.

These checks establish native startup resource loading, not an in-world Fake Air lifecycle or manual F3+T reload test. Known headless audio/platform warnings are not claimed fixed. The broader migration and cross-target Echo gameplay acceptance remain incomplete.
