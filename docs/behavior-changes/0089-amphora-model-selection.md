# 0089 — Amphora model selection

Status: implemented, four-target assembly and packaged model-threshold contract verified. All four targets; source-backed restoration under standing approval, no gameplay deviation.

Original release bb99accf48ed583e29b0efae56e28c963407b8df RadiantAmphoraItem.registerModels selects unlinked, fill or empty according to link and mode. The port used 0/1/2 through ClampedItemPropertyFunction, making the threshold-2 empty/drain model unreachable. Cached mapped Minecraft 1.20.1 and 1.21.1 javap confirms call clamps unclampedCall to 0..1.

Use 0/0.5/1 and matching model JSON thresholds. Preserve original models, item link/mode data and transfer behavior. Dynamic fluid-mask rendering remains separate unfinished work.

Verification: `timeout --foreground 10m ./gradlew assemble --offline --no-daemon`, exit 0, 20s, `build/amphora-model-20260909-185740.log`. `timeout --foreground 60s python3 scripts/verify_artifacts.py`, exit 0, `build/amphora-model-artifacts.log`; all four production/source pairs pass. Packaged JSON selection checks for unlinked/fill/empty values pass on every production jar (`build/amphora-model-selection.json`). `git diff --check` passes. No client launched; native live property invocation, model baking and visual acceptance remain open.
