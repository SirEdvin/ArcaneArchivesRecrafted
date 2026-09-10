# 0099 — Packed Trove inventory access

Status: approved, implemented and assembly/package-verified on all four targets. Live Fabric and world-interaction acceptance remains open.

Source: release bb99accf48ed583e29b0efae56e28c963407b8df, inventory/handlers/TroveItemBlockItemHandler.java and ITroveItemHandler.java. The registered Trove item exposes two slots: real contents in slot zero and an empty second admission slot. Both insertion paths address the same contents; extraction uses native stack limits. Capacity, item identity, LOCK and VOID derive from packed state.

Restore native Forge/NeoForge item capabilities and Fabric item Transfer API using the already validated packed Trove decoder. Keep item-local state, preserved owner/upgrades/unknown metadata, exact item components, native-sized extraction and approved lock/void/capacity conservation rules. Simulations must not capture a reference or modify saved data (unlike the legacy lazy setter path). Decode and serialize before publishing. Fabric exchanges the item through its transactional container context; abort must restore the complete item including the lock reference. Copied items never touch the original or a remote block. Reject malformed or stacked containers without rewriting them. Item contexts lack dynamic registry access; unresolvable registry-dependent data fails closed as in the existing Tank item adapter.

Approval: source-backed restoration with existing simulation, validation and conservation corrections; no new gameplay deviation proposed.

Verification: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*TroveItemStorageTest' --tests '*RadiantTroveItemTest' --offline --no-daemon`, exit 0, 25s, `build/trove-item-storage-final-20260910-054005.log`. Four targets assembled. NeoForge XML confirms 4 inventory tests and 3 tooltip tests, with no skips/failures/errors. Inventory fixtures exercise native registered capabilities, simulations, copied-container isolation, LOCK/VOID/capacity, components/metadata and rejected malformed/stacked inputs. A round-trip fixture uses actual vanilla SimpleContainer, ChestBlockEntity and HopperBlockEntity endpoints through InvWrapper, preserving the contained Trove and pearl stack limits.

Artifact check: `timeout --foreground 60s python3 scripts/verify_artifacts.py`, exit 0, 0s, `build/trove-item-storage-artifacts.log`; all four production/source pairs pass. No server/client launched. Detached vanilla-container tests do not establish live hopper ticking or Fabric transaction/abort acceptance. Automation/modded components, multiplayer and world restart remain acceptance work.

## Approved compatibility decision

Inspected the exact nested Transfer API versions in the pinned Fabric API jars: 0.92.6+1.20.1 contains 3.3.6+8dd72ea377; 0.116.12+1.21.1 contains 5.4.4+7b3d111d19. The former's ItemStorage exposes only the block SIDED lookup, not an item lookup. The latter provides an ItemApiLookup. Thus a native ItemStorage.ITEM registration cannot be copied across both Fabric targets.

The user approved this approach and required vanilla inventory support. Implemented the native item lookup on Fabric 1.21.1 and the documented mod-owned `arcanearchives:item_storage` ItemApiLookup on Fabric 1.20.1, backed by the same transactional adapter. Forge/NeoForge retain native item capabilities. The 1.20.1 lookup requires consumers to opt into that identifier; it cannot promise automatic interoperability equivalent to the newer standard. No extra library or invented player interaction was added.

Vanilla inventory integration uses native Fabric InventoryStorage/ContainerItemContext and Forge-family InvWrapper. See [integration contract](../migration/PACKED_TROVE_INVENTORY.md). This supports Troves in vanilla inventory slots and transfers through native wrappers; it does not add automatic unpacking of a Trove nested inside another inventory.
