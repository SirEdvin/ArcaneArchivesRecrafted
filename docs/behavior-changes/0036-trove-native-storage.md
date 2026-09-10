# Trove native item storage

Status: partial runtime implementation under the user's delegated behavior-change authority. All four targets; gameplay acceptance remains pending.

Pinned source: ArcaneArchives release `bb99accf48ed583e29b0efae56e28c963407b8df`, `blocks/RadiantTrove.java`, `tileentities/RadiantTroveTileEntity.java`, `inventory/handlers/ITroveItemHandler.java`, `init/RecipeLibrary.java:119`, and the release Trove blockstate/model resources. MIT notices remain packaged.

Implemented: original four-Trove Gem Cutter output and costs; original geometry/display transforms; one item/data identity; capacity `max(nativeStackSize,64) * 512 * (1 + upgradeStrength)`; three ordered size upgrades with strengths 2/3/4; direct component and shaped-block installation, including sneak item use; native-sized player withdrawals; main-hand deposit and 800ms double-click main-inventory deposit; 150ms withdrawal debounce; live player/device checks; owner and full storage persistence; comparator and loader automation.

Necessary adaptations: expose one real slot rather than the legacy handler's two aliases of the same store. Simulation does not establish a reference or mutate contents. Empty unlocked storage accepts a new item identity. Detached reads and native Fabric transaction replacement avoid mutating live storage through preview/snapshot access. Native Fabric bulk transfer may move more than one ordinary stack per transaction; explicit player and Forge-style extraction remains ordinary-stack bounded. Double-click compares UUID values rather than Java object identity and uses a monotonic clock. These are documented integration/conservation changes, not optimization.

Fresh-world NBT is loader/version-specific, not an old-save or cross-loader converter. Load candidates validate upgrades, identity/count and capacity before publication. Portable removal is separately recorded in 0038.

Alternatives rejected: duplicated alias slots; simulation with side effects; native stack-size capacity clamping; separate unsynchronized persistence of item count and upgrades. Costs/capacity are not reduced to simplify the port.

Still unfinished: optional upgrades (including lock/void), native item-form automation, scepter/revelation and upgrade-removal UI, Hive/Brazier routing and dispense preference, creative attack/scepter dismantling parity, integration overlays and gameplay/restart/protection tests. These are not approved removals.

Compilation: Trove assembly passed all four targets (`build/trove-integration-20260908-181050.log`, exit 0, 25s), then shared Tank/Trove assembly passed (`build/tank-trove-integration-20260908-182043.log`, exit 0, 24s). No gameplay tests were run. Original-asset extraction scripts ran successfully; rendering acceptance remains deferred.
