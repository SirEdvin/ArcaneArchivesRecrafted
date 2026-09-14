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

Server-side implementation and bounded native verification now pass under [0132](0132-manifest-inventory-aggregation.md). A real Fabric native regression failed before deduplication; the corrected aggregate passes repeated targets, opposite double-chest halves, distinct replacement barrels, target removal and current personal/Hive reads on all four loaders. Final full build exits 0 in 56s (`build/manifest-contents-final-20260912-061142.log`); all artifact pairs pass. The Manifest item/UI/packets, actual target unload and connected acceptance remain pending; this is not full feature completion.

Required tests: repeated target, opposite double-chest halves, distinct inventories, target replacement/unload, and current personal/Hive visibility.
