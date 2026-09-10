# 0006 — Size-upgrade removal must fit the remaining capacity

## Decision and authority

Implement under the user's delegated authority for properly documented behavior changes. Preserve the upgrade items, their order and potency, and the Tank/Trove capacity formulas. This is separate from 0004, which deliberately retained the original capacity-callback argument pending consumer analysis.

## Source trace

Pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`:

- `inventory/handlers/SizeUpgradeItemHandler.java:60-84`: first-slot removal bypasses `canReduceMultiplierTo`; other slots pass the inclusive `getUpgradesCount(slot)`, which still includes the upgrade being removed.
- `tileentities/RadiantTankTileEntity.java:29-43,55-56`: the removal callback compares stored fluid to `BASE_CAPACITY * (size + 1)`. After changing the upgrades, its contents callback replaces tank capacity with the recomputed capacity.
- `tileentities/RadiantTroveTileEntity.java:65-71,400-423`: the removal callback compares stored items to `BASE_COUNT * (size + 1)`, and the contents callback updates the inventory upgrade potency.
- The concrete Tank/Trove handlers only select upgrade items; neither compensates for the shared handler's inclusive argument or first-slot bypass.

Thus the check can permit a removal whose resulting capacity is below stored contents. This is a source-backed over-capacity finding, not proof of immediate fluid/item deletion in a running legacy world.

## Change and compatibility

All four modern targets check capacity against the potency remaining after removal, including zero remaining potency for the first upgrade. Reverse-removal order remains mandatory. Both simulated and actual extraction are checked. Direct clearing through `setStackInSlot` uses the same guard rather than bypassing it.

The observable difference is rejection of formerly accepted unsafe removals. Users must first drain/extract enough contents to fit the lower capacity. No contents are truncated, discarded or converted. No upgrade type, recipe, base capacity or potency changes. No persistence or packet schema changes. Runtime Tank/Trove integration is still unimplemented; this closes the shared handler defect before those consumers are ported.

Alternative: retain the callback bug and permit over-capacity storage. Rejected because it defeats the original consumers' explicit removal-capacity condition. Automatically ejecting excess contents is not chosen because it adds unrelated loss/drop semantics.

## Verification

A parameterized regression covers every upgrade slot with a normalized version of the source capacity formula. It checks rejection above the resulting capacity, acceptance exactly at capacity, no callback/mutation on rejection or simulation, detached simulated output, and direct slot clearing. Existing reverse-order and conservation cases remain.

Red verification: `timeout --foreground 10m ./gradlew :1.21.1-neoforge:test --tests '*InventoryHandlerTest.removalChecksPostRemovalCapacityIncludingFirstUpgrade' --no-daemon` failed all three cases at the unsafe simulated removal assertion, before the production fix. Exit 1, 14s, `build/upgrade-capacity-red-20260907-211916.log`.

Green verification: `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test assemble --no-daemon` — exit 0, 25s, `build/upgrade-capacity-green-20260907-212026.log`. Each of these three test targets reports 40 tests, zero failures/errors/skips. All four production/source artifact contracts and `git diff --check` pass. The handler fix is implemented and regression-tested on Fabric and NeoForge. Forge is compiled/packaged but its Minecraft-dependent loader-aware harness remains unresolved; compilation is not runtime acceptance.
