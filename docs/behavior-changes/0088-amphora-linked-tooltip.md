# 0088 — Amphora linked-fluid tooltip

Status: implementation and four-target assembly/package verified; client-world acceptance open. Approved scope: source-backed restoration with existing safety boundaries; all four targets.

Original release bb99accf48ed583e29b0efae56e28c963407b8df RadiantAmphoraItem.addInformation and AmphoraUtil.getTile/getFluidType show a blank line and gold linked coordinates, dimension and fluid name. Client lookup only resolves the current client world when its dimension matches; unavailable or empty Tanks yield literal "Unknown fluid". Both original languages use the same English linked-tooltip text.

Restore this through client tooltip events and native loader fluid display names, replacing the port's coordinates-only line. Retain existing controls/mode help. Modern dimension resource IDs replace removed numeric DimensionType names. Adapt original %d placeholders to Component-supported %s. Never query the integrated server from tooltip code, load a chunk, mutate an item, or synchronize remote storage. Cross-dimension fluid queries and dynamic rendering are not implemented by this change.

Verification: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*AmphoraTooltipTest' --offline --no-daemon` exited 0 in 28s, `build/amphora-tooltip-20260909-185253.log`. All four leaves assembled. NeoForge XML reports 2 tests, zero skips/failures/errors: linked coordinates/dimension/unknown-fluid/style without a client world, unlinked/malformed input and read-only item state. These fixtures do not exercise loaded client Tank lookup or native fluid naming.

`timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/amphora-tooltip-artifacts.log`; all four production/source pairs pass. `git diff --check` passes. Connected-client event ordering, visible formatting, loaded/empty/unloaded and other-dimension Tanks, and modded-fluid acceptance remain open. No runtime client/server launched.
