# 0107 — Wonky Resonator hand-transform parity

Status: corrected under the approved original-behavior migration scope. This repairs a rendering regression in the initial Wonky asset conversion, not a new gameplay feature. Native parser tests pass on both Minecraft versions; all four builds, artifacts and client resource startups pass. Interactive visual acceptance remains open.

## Original behavior and observed defect

The pinned development resource at `80944ce45c6559243d8928cc4b305bf379388652:src/main/resources/assets/arcanearchives/blockstates/wonky_resonator.json` uses legacy Forge inventory `transform.firstperson` and `transform.thirdperson` keys. The initial converter copied those names into modern `display`. Both Minecraft 1.20.1 and 1.21.1 silently ignored them: native parser tests observed identity scale rather than the original 0.375 right-hand scale. Valid JSON, successful compilation and ordinary resource startup had not detected this.

Legacy Forge interpretation was traced rather than inferred from the key names:

- `ForgeBlockStateV1.java:521-555` maps these aliases to `FIRST_PERSON_RIGHT_HAND` / `THIRD_PERSON_RIGHT_HAND`. Explicit suffixed right-hand definitions, if present, override the aliases.
- `SimpleModelState.java:49-59` has no value for an unspecified hand.
- `obj/OBJModel.java:1558-1560` delegates perspective handling to `PerspectiveMapWrapper`.
- `PerspectiveMapWrapper.java:94-101` uses identity when that state has no transform.

These official Forge 1.12.x sources were inspected from the reference snapshot `3effde4f1fc9d14d6ed1dbf6bebc39c2b18780e1`, under `src/main/java/net/minecraftforge/client/model/`, for example:
https://raw.githubusercontent.com/MinecraftForge/MinecraftForge/3effde4f1fc9d14d6ed1dbf6bebc39c2b18780e1/src/main/java/net/minecraftforge/client/model/SimpleModelState.java

This is API/source analysis only; no Forge source was incorporated into the mod. Existing MIT-covered upstream art is unchanged.

## Correction

- Convert the two legacy aliases to `firstperson_righthand` and `thirdperson_righthand` in `scripts/port_wonky_resonator_assets.py` and its generated model.
- Keep the original unspecified left-hand identity using explicit empty `firstperson_lefthand` and `thirdperson_lefthand` definitions. Modern `ItemTransforms.Deserializer` otherwise inherits the right-hand transform for missing left-hand definitions; simply renaming the aliases would introduce a different regression.
- Preserve all numeric transforms, GUI/ground appearances, OBJ/materials/textures and every server-side behavior.
- Audit all 131 current model JSON files. Wonky was the only model retaining these aliases; after correction none retain the aliases or a right-hand definition with an omitted left-hand definition. This observation does not impose a new general policy on vanilla-derived models.
- Artifact verification now rejects these ignored legacy display keys throughout the mod's packaged model namespace and checks exact Wonky display metadata. The converter remains reproducible against the original pin.

No creative/survival acquisition, timer, drops, configuration or network logic changed. Celestial Lotus Engine was inspected while selecting work but was not registered or implemented in this slice.

## Verification

New `src/test/java/com/aranaira/arcanearchives/client/WonkyItemTransformsTest.java` runs through native `BlockModel.fromStream(...).getTransforms()` in the existing Fabric test environment on both Minecraft versions. It does not substitute a Python approximation for the actual parser. Client-only parser classes are not introduced into the Forge dedicated-server harness or release artifacts.

Two cases per version verify:

1. Original first/third-person right-hand scale, translation and rotation.
2. Unspecified legacy left-hand identity rather than modern right-hand inheritance.

Red: `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test --tests '*WonkyItemTransformsTest' --no-daemon`, exit 1, 13s, `build/wonky-transforms-red-20260910-104502.log`. The right-hand assertion fails on both versions; the pre-fix identity-left check passes.

Green: `timeout --foreground 10m ./gradlew build --no-daemon`, exit 0, 47s, `build/wonky-transforms-green-20260910-104635.log`. Actual final XML confirms two passing parser cases per version, no failures/errors/skips.

Alternate-active matrix: switch to Forge 1.20.1, build all four leaves, verify all artifacts and reset. Each Gradle command used a 10-minute timeout and `--no-daemon` with complete silent logging:

| Command | Exit | Duration | Log |
| --- | --- | --- | --- |
| `./gradlew 'Set active project to 1.20.1-forge' --no-daemon` | 0 | 6s | `build/transforms-switch-20260910-105159.log` |
| `./gradlew build --no-daemon` | 0 | 31s | `build/transforms-alternate-20260910-105205.log` |
| `./gradlew 'Reset active project' --no-daemon` | 0 | 6s | `build/transforms-reset-20260910-105236.log` |

The same pre-existing Stonecutter nested-comment normalization occurred in ContentRegistry, RadiantTankRenderer, AmphoraFluidStorage and RadiantTankStorage. Only those delimiter changes were restored. All 656 source/controller files then matched `/tmp/arcane-transform-roundtrip-20260910-105159.tar` byte-for-byte; the switch itself was not automatically byte-stable.

Final canonical full build: `timeout --foreground 10m ./gradlew build --no-daemon`, exit 0, 27s, `build/transforms-final-20260910-105331.log`. All four production/source pairs pass `python3 scripts/verify_artifacts.py`; `git diff --check` passes. Re-running the pinned Wonky asset recovery succeeds with the corrected metadata.

All four real client checks use `timeout --foreground 10m xvfb-run -a -s '-screen 0 1280x800x24' python3 scripts/smoke_clients.py <leaf>`:

| Target | Exit | Duration | Wrapper log |
| --- | --- | --- | --- |
| 1.20.1 Fabric | 0 | 23s | `build/wonky-transforms-client-1.20.1-fabric-20260910-104855.log` |
| 1.20.1 Forge | 0 | 23s | `build/wonky-transforms-client-1.20.1-forge-20260910-104918.log` |
| 1.21.1 Fabric | 0 | 22s | `build/wonky-transforms-client-1.21.1-fabric-20260910-104941.log` |
| 1.21.1 NeoForge | 0 | 23s | `build/wonky-transforms-client-1.21.1-neoforge-20260910-105003.log` |

`build/wonky-transform-acceptance.json` aggregates the four client results, raw log/screenshot paths, parser testcase names and model audit count. No development game JVM remains. Client resource startup is not connected-world hand-rendering acceptance; native parser verification establishes the corrected transform values, not every renderer/mod interaction. Server runtime was not rerun for this resource-only correction; the previous Wonky timer/persistence acceptance remains separate evidence. The overall migration is unfinished.
