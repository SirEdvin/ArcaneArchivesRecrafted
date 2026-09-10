# Radiant Key and placed Trove locking

## Original behavior and scope

Pinned release `bb99accf48ed583e29b0efae56e28c963407b8df`: `items/RadiantKeyItem.java`, `inventory/handlers/ITroveItemHandler.java`, `tileentities/RadiantTroveTileEntity.java`, `init/RecipeLibrary.java` and original Key resources.

The Key is a LOCK optional upgrade. The Trove keeps an item reference separately from its count, uses locking in empty/reference handling and routing, and persists that reference. The original Gem Cutter recipe yields four Keys from a gold ingot, three gold nuggets and shaped radiant quartz. Original model, PNG and animation metadata are recovered by `port_radiant_key_assets.py` with an explicit atlas source.

Affected targets: 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Approval status: implementation within the requested migration; no authorization to remove remaining requirements is claimed. The implemented scope is the placed Trove's functional LOCK upgrade. Generic acceptance by other devices, item-form APIs and broader network routing remain outside this checkpoint.

The old tooltip mentions safely opening trapped chests. No corresponding interaction was found in the pinned Key implementation or Key/LOCK call sites. The original text is retained; it is not evidence of a migrated trapped-chest feature. This source/tooltip discrepancy remains explicit rather than being filled with invented behavior.

## Native implementation and rationale

`RadiantKeyItem` is registered with creative-tab access and the original acquisition/artwork. Direct Trove use and the existing optional menu install/remove the Key. Duplicate LOCK upgrades are rejected; LOCK and VOID can coexist. Trove block interaction consumes rejected upgrade attempts instead of depositing the Key as ordinary storage contents. Tank fluid-lock behavior is not invented.

A separate one-count native item reference preserves exact item/data identity when the real inventory becomes empty. Installing a Key on occupied storage captures the current item; an empty unassigned lock learns its first accepted item. Removing the Key clears the lock without deleting inventory. Normal player deposits, Forge-family automation and Fabric insertion share the identity gate, including when VOID is also installed, so nonmatching inputs are rejected rather than discarded.

The reference persists with optional upgrades in block saves, synchronization and packed drops. Loaded references are validated before publishing state; old migration saves lacking a reference remain readable. The reference is not real inventory and cannot be withdrawn. Revelation reports the selected item or an unassigned lock.

Fabric must snapshot both quantity and the auxiliary reference: a first insertion can select the identity even if that transaction is later aborted. A native SnapshotParticipant captures the reference before the existing SingleStackStorage mutation/snapshot path. This is required state consistency, not an optimization or replacement storage framework. Ordinary server writes capture the reference after changing contents; transactional writes defer world updates to final commit.

## Integration evidence and remaining acceptance

`./gradlew assemble --no-daemon` compiled all four targets successfully in the initial integration (`build/radiant-key-lock-20260908-203834.log`, exit 0, 19s). Final assembly including the recovered animation sidecar passed (`build/radiant-key-final-20260908-204048.log`, exit 0, 14s). `git diff --check` passed. Key classes/models/textures and final animation metadata were confirmed in all four production JARs; Fabric's reference snapshot class was also present. Artifact expectations are extended, not evidence of a full verifier run.

No gameplay tests, client/server launch, optimization, commit or publication. Restart/packed replacement, item-data matching, LOCK+VOID interactions, rollback, automation, visual presentation and multiplayer acceptance remain deferred until the feature migration pass is complete. Parchtear, networks and other missing feature families remain unfinished. The full migration is not ready for the first playtest.
