# 0082 — Packed Radiant Tank item fluid adapters

Status: implemented on all four targets; four-target assembly/package and focused NeoForge native-capability fixtures verified. Cross-loader gameplay acceptance remains open.

## Original behavior

Release `bb99accf48ed583e29b0efae56e28c963407b8df`, `inventory/handlers/TankItemFluidHandler.java:19–63` and `items/itemblocks/RadiantTankItem.java`: every Tank item exposes an item fluid handler. Capacity is the packed maximum (base 16,000 mB). Fill requires one container, a positive amount and matching native fluid identity/data, caps acceptance at remaining capacity, copies fluid on initial fill and only mutates on execution. Native FluidHandlerItemStack supplies drain and returns the same Tank container. The handler is item-local, not a remote block proxy. Its fill does not consult the placed Tank's voiding upgrade logic.

## Implementation and approval

Faithful migration under the existing scope approval, retaining the previously approved malformed-state/conservation guards; no new gameplay behavior is proposed.

- `inventory/TankItemFluidStorage.java` exposes Fabric FluidStorage.ITEM / SingleSlotStorage and Forge/NeoForge native item fluid capabilities.
- Decode current packed block-entity state through existing validators, retaining the existing ordered upgrade capacity calculation. Matrix Brace capacity is 48,000 mB.
- Encode replacement fluid data before changing the live item. Preserve owner, size/optional upgrades, unrelated packed fields and outer item components. Emptying does not replace the item with a fresh Tank or destroy upgrades.
- Fabric exchanges exactly one item through the provided ContainerItemContext transaction; outer abort rolls that exchange back. There is no detached mutable fluid cache or on-final-commit-only publication that could miss rollback.
- Forge-family simulation builds a detached result without publishing. Native FluidUtil is free to execute against its own copied item without affecting the caller's original container or another Tank.
- Reject mixed fluid identities/components, malformed packed state and nonunit direct container stacks. No void-overflow shortcut is added to the item handler.
- Amphora remote storage is not exposed through this adapter; its separate copied-item integration boundary remains open.

## Registry boundary and remaining compatibility

Item capability contexts do not provide a Level/registry lookup. This implementation uses built-in registry lookup for packed NBT codecs, as the existing focused item fixtures do. It must not be described as complete support for arbitrary dynamic-registry-backed modded fluid/upgrade components: that integration remains open. Data the existing validators cannot decode is rejected without rewriting the original item. Do not silently discard unknown components or substitute plain fluid to make a transfer succeed. World-aware registry integration and actual modded-fluid acceptance must be resolved before claiming full compatibility.

## Verification

- `timeout --foreground 10m ./gradlew assemble --offline --no-daemon`: exit 0, 20s, `build/tank-item-fluid-20260909-180824.log`.
- `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*TankItemFluidStorageTest' --offline --no-daemon`: exit 0, 16s, `build/tank-item-fluid-tests-20260909-181035.log`.
- NeoForge XML: 4 tests, 0 skips/failures/errors, `versions/1.21.1-neoforge/build/test-results/test/TEST-com.aranaira.arcanearchives.inventory.TankItemFluidStorageTest.xml`.
- Final targeted rerun after strengthening the component-mismatch case to use a partially filled Tank: `timeout --foreground 10m ./gradlew :1.21.1-neoforge:test --tests '*TankItemFluidStorageTest' --offline --no-daemon`, exit 0, 14s, `build/tank-item-fluid-final-20260909-181255.log`; still 4 tests, 0 skips/failures/errors. This tests fluid-component mismatch independently of the full-capacity rejection.
- Fixtures use actual registered native item capabilities and FluidUtil, covering execute/simulate fill/drain, capacity, defensive fluid reads, component mismatch, malformed/stacked inputs, copied-container simulation, owner/upgrades/custom-name conservation and packed block-state decode after draining.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/tank-item-fluid-artifacts.log`; all four production/source pairs pass, including the new adapter class. `git diff --check` passes.

No Fabric transaction runtime test, Forge loader-aware fixture, connected-player transfer, actual world save/restart, modded-fluid integration, dispenser or fluid-item visual acceptance is established by these results. Tank fluid item rendering, Amphora generic adapters and the rest of the storage/network milestones remain unfinished.
