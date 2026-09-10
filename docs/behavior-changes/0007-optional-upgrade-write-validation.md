# 0007 — Optional-upgrade write validation

Status: shared handler implemented under delegated behavior-change authority; concrete bindings and gameplay verification pending.

## Upstream evidence

Pinned revision `80944ce45c6559243d8928cc4b305bf379388652`, `inventory/handlers/OptionalUpgradesHandler.java:10-41,44-65`: three slots, limit one, rejects non-upgrades and SIZE, rejects a type already present, exposes type/presence/quantity queries. It inherits direct writes and deserialization from Forge's item handler without overriding either to enforce these rules. This observation concerns the missing validation in this class, not a demonstrated exploit through a particular menu.

`types/enums/UpgradeType.java` defines SIZE, VOID, ORE_DICTIONARY, CHISEL, MUTE, LOCK, ROUTING. Retain those exact names; enum presence does not enable dormant gameplay or decide how ore-dictionary integration should migrate.

Consumers include `inventory/ContainerUpgrades.java`, `tileentities/RadiantTankTileEntity.java`, `tileentities/RadiantTroveTileEntity.java` and `inventory/handlers/TroveItemBlockItemHandler.java`. Item selection originally comes from `items/IUpgradeItem.java`; its device compatibility concerns remain distinct from this inventory's type-uniqueness rules.

## Decision

Preserve normal insertion/extraction behavior and all three single-item slots. Add validation to direct replacement and fresh-world saved-data loading: reject invalid items, SIZE upgrades, counts above one and duplicate optional types. Decode and validate the entire saved inventory before replacing any live slot. Never sanitize by deleting entries. Direct replacement may retain the same type in the replaced slot, but cannot duplicate another slot's type.

This changes behavior for malformed direct writes or saved inventories which the upstream class did not itself guard. It does not change ordinary insertion's duplicate-type rejection, capacities, upgrades, recipes, or slot order. Simulation/remainders use the shared conservation-safe inventory implementation.

Affected targets: 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Uses the existing fresh-world inventory format; no new save or network schema and no old-save conversion. Failing invalid loads preserves the live inventory rather than partially loading it.

Alternatives: rely on callers to validate, or discard invalid saved entries. Rejected because caller bypasses would violate storage invariants, while deletion could lose items.

## Implementation boundary

The shared handler uses an abstract item-to-type binding, matching the existing size-handler separation. Concrete upgrade items, the full IUpgradeItem device contract, and Tank/Trove/menu bindings are not ported by this slice. Tests supply explicit vanilla-item classifications, not registered gameplay upgrades. No textures are changed or replaced.

## Verification

Regression cases cover rejection/partial insertion, type uniqueness, simulation and detached output, direct replacement, invalid indices/amounts, persistence round trip, and atomic rejection of duplicate/invalid/oversized saved upgrades. Actual results are recorded after execution; Forge's known Minecraft-dependent JUnit bootstrap limitation remains explicit.

Executed `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon`: exit 0, 26s, full log `build/optional-upgrades-20260907-213007.log`. XML reports four optional-handler cases and 44 total tests on each of the three executed targets, zero failures/errors/skips. Forge test sources compile but its Minecraft-dependent cases were not executed. All four production/source artifact contracts pass, including the handler and enum. `git diff --check` passes. No gameplay/runtime acceptance is inferred.
