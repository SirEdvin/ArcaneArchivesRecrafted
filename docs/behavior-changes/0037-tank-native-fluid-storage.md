# Tank native fluid storage

Status: partial runtime implementation under the user's delegated behavior-change authority. All four targets; no gameplay acceptance yet.

Pinned source: release `bb99accf48ed583e29b0efae56e28c963407b8df`, `blocks/RadiantTank.java`, `tileentities/RadiantTankTileEntity.java`, `init/RecipeLibrary.java:121`, and native release model/blockstate resources.

Implemented: original Gem Cutter costs and four-Tank output, original block/item geometry and display transforms, 16-bucket base capacity, ordered Matrix Brace / Containment Field / shaped-storage upgrades (2/3/4), native fluid-container right click and fluid automation, owner and complete fluid/upgrade persistence, comparator, and packed removal under 0038. Material Interface is not a Tank upgrade; Containment Field is not a Trove upgrade.

Use Fabric SingleVariantStorage and native transactional container interaction, and Forge/NeoForge FluidTank/FluidUtil. Loader-native fluid variants, tags/components and quantities remain native: Fabric bucket units are not silently treated as millibuckets. Candidate decoding checks the native identity and exact declared amount against the capacity derived from validated upgrades. Invalid candidates do not partially publish. Mutations require a live server-owned device; client state is synchronized through native block-entity packets.

Alternative rejected: inventing a generic cross-loader fluid serialization/conversion format or adopting an unverified simulate/move/remainder fallback. This follows the user's permitted native fallback; no Broccolium dependency or library repair was introduced.

Fresh worlds only. Portable item storage uses current native block-entity data, not 1.12 root item NBT. Native world/item fluid persistence is not a promise of cross-version conversion.

Still unfinished: optional void upgrade, item-form fluid capabilities/container returns, upgrade-removal/scepter UI, connected-player restart/container/automation acceptance and item fluid rendering. World fluid renderer implementation follows separately from the block mesh and remains unvalidated visually. Missing features are not removals.

Four-target compilation/assembly passed in `build/tank-trove-integration-20260908-182043.log` (exit 0, 24s), before the subsequently added world fluid renderer. Tests and clients are deferred until feature implementation is complete, per user direction.
