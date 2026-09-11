# 0117 — KubeJS recipe metadata compatibility

## Original behavior

The port's native Gem Cutter JSON parser rejects unknown top-level fields. Upstream CraftTweaker used an imperative recipe API and did not have this JSON metadata boundary.

## Proposed behavior

Accept only KubeJS's verified `_kubejs_changed_marker` metadata at the recipe integration boundary, after inspecting its precise value contract. Strip it before constructing/storing/synchronizing the native recipe. Preserve rejection of every other unknown field and all existing item, count, tag, size and authorization validation. Do not make KubeJS mandatory or change costs/crafting behavior.

## Reason

The pinned NeoForge KubeJS 2101.7.2-build.377 injects this field when processing `event.custom`. The documented example is accepted on both 1.20.1 targets but rejected on NeoForge. Runtime diagnostic lists `[type, inputs, result, order, _kubejs_changed_marker]` at the failing boundary.

## Affected targets

Primary: Minecraft 1.21.1 NeoForge. Regression verification must cover all four native serializers, including absent KubeJS. KubeJS remains excluded on 1.21.1 Fabric.

## Approval status

Approved by the user. Implemented for the verified object shape: string `source`, nonnegative integer `line`, no other marker fields. The existing size bound applies before stripping; only the owned deep copy is modified. The marker is excluded from stored recipe JSON and wire synchronization. No KubeJS API dependency or crafting-authority change is introduced.

## Verification

Post-fix: `timeout --foreground 10m ./gradlew build --no-daemon` passed all four leaves, exit 0, 78s (`build/kubejs-metadata-build.log`). Shared regression coverage checks retained counted payment, input JSON ownership, marker-free wire JSON, malformed markers and continued unknown-field rejection. Artifact verification passed (`build/kubejs-metadata-artifacts.log`).

`timeout --foreground 32m python3 scripts/smoke_kubejs_recipes.py` passed all three supported KubeJS leaves, exit 0, 78s (`build/kubejs-metadata-runtime.log`). Native-catalog assertions passed on Fabric 1.20.1 (30.56s), Forge 1.20.1 (27.30s), and NeoForge 1.21.1 (19.79s), with no ERROR lines in their runtime logs. Fixture scripts were removed. This resolves the metadata blocker; it does not close connected crafting acceptance. Subsequent remove/reload evidence is recorded in the KubeJS usage guide; arbitrary NBT/component scripting is now excluded under [0118](0118-scripted-stack-data.md).

### Historical failing reproduction

`timeout --foreground 32m python3 scripts/smoke_kubejs_recipes.py`, exit 1, 113 seconds; aggregate `build/kubejs-native-recipes-diagnostic.log`.

- Fabric 1.20.1: native catalog assertions passed (`build/smoke-server-1.20.1-fabric-20260910-193215-797293.log`).
- Forge 1.20.1: native catalog assertions passed (`build/smoke-server-1.20.1-forge-20260910-193249-915391.log`).
- NeoForge 1.21.1: native parse rejected injected field (`build/smoke-server-1.21.1-neoforge-20260910-193337-833192.log`).

The probe checks exactly one example recipe, output count four, and two input counts (two diamonds and three planks in the example). It does not yet test crafting, remove/reload or connected viewers. Fixture scripts are removed after runs. Earlier failed probe attempts exposed Rhino Java immutable-list access and block-scoped const/try behavior; those fixture issues were corrected without changing production gameplay.
