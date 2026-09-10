# Broccolium storage integration assessment

Inspected the user's local Minecraft-Modding-Libs checkout at `47a80ca` (`fix/stonecutter-migration-contracts`), without changing it or adding a dependency to this port.

Broccolium's Stonecutter build covers 1.20.1 and 1.21.1, with Fabric and Forge-family branches (the latter selects NeoForge on 1.21.1). Its APIs include `AgnosticStorage`, `SlottedAgnosticStorage`, item/fluid lookups, and native adapters. It is a relevant cross-platform library, not an unavailable option.

For the current extended-stack chest and conservation-sensitive transfers, use native implementations as explicitly authorized by the user:

- Both versions of `fabric/.../SlottedAgnosticItemStorageSlotWrapper.kt:35` cap capacity at the lesser of storage capacity and the vanilla item's stack limit. Radiant Chest intentionally holds a multiple of that limit.
- The same wrapper's `setStack` calls insertion (`store`), rather than exact replacement. This is not suitable for this port's native transaction snapshot/restore boundary without further adaptation.
- `modules/storage/base/StorageUtils.kt:39-48` tries to return a remainder to its source but only logs if some cannot be returned. That is not a conservation guarantee for arbitrary third-party stores.
- `fabric/.../FabricStorageUtils.kt:78-109` extracts from non-transactional sources before opening the destination transaction and does not verify the returned remainder was restored. Do not use that path as an atomic transfer primitive.

These are source observations, not executed reproductions and not a claim that every Broccolium API is unsuitable. Do not divert the mod migration into repairing or publishing the library. Its lookup code confirmed the shared native `ChestBlock.getContainer(block, state, level, pos, true)` call for double-chest conversion.

Current integration: reuse the existing shared extended handler, Forge item capability, NeoForge block capability registration, and Fabric `SingleStackStorage`/`CombinedStorage` with exact slot replacement, extended capacity and native rollback. Fabric defers dirty/comparator callbacks until final commit. No Broccolium code/assets were copied into the mod.

Later non-slotted item/fluid network interactions may use Broccolium if their required semantics can be met without these fallback paths. Evaluate actual consumers, not a speculative platform-wide rewrite. Fluid-device integration is still unfinished.
