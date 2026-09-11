# Manifest monitored-inventory deduplication

## Original behavior

The source audit found that release ManifestUtils deduplicates monitored double-chest partners, while master removed the double-chest handling and the skip, permitting double-counting.

## Approved behavior

Deduplicate monitored inventories, including both halves of a double chest, so the same underlying inventory contributes once. Preserve component-sensitive item aggregation and legitimate separate inventories.

## Reason

Avoid inflated Manifest totals from repeated observation of the same inventory.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval status

Approved explicitly by the user in response to the three network-migration decisions.

## Verification

Decision recorded only; implementation and runtime verification remain pending.

Required tests: repeated target, opposite double-chest halves, distinct inventories, target replacement/unload, and current personal/Hive visibility.
