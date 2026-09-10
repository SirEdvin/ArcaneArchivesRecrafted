# 0076 — Sneak-scepter Trove dismantling

Status: implemented; four-target assembly/package verification passed. Runtime acceptance remains open.

## Original behavior

Release bb99accf48ed583e29b0efae56e28c963407b8df, blocks/RadiantTrove.java:98–139: survival harvesting while sneaking with an IItemScepter spills the inventory in batches of at most 64 (also capped by the item's native stack size), then optional upgrades, size upgrades and an empty Trove. Other survival harvesting packs the contents. The original native harvest path runs after break authorization and removal. ITroveItemHandler.java:115–141 establishes the additional native-stack-size cap.

The original TroveItemHandler.isEmpty (RadiantTroveTileEntity.java:495–497) can remain false for an exhausted LOCK reference, so blindly copying the original while-loop would never terminate for a locked Trove.

## Implementation and reason

Restore the missing dismantling action for both currently migrated scepters. All four targets share the existing onRemove drop dispatch and one drop latch. The new removal scope selects loose drops only for a sneaking, noncreative, nonspectator scepter user. Other removal paths retain their existing packed behavior; this does not change creative drop policy.

Forge/NeoForge scope their native onDestroyedByPlayer call. Fabric scopes only ServerPlayerGameMode.destroyBlock's native ServerLevel.removeBlock invocation, after authorization callbacks. Other blocks use the original call unchanged. The scope restores its prior value in finally, so rejected/throwing removal cannot leave a later removal in spill mode. Nothing is dropped merely by selecting the mode: onRemove must actually run. No inventory transfer occurs on the client.

Spill copies the actual stored count into native-sized output stacks, rather than testing the lock reference. This preserves contents/components and terminates for empty locked storage. The drop latch is set before spawning outputs to prevent a second packed drop or live automation during removal. Optional and size upgrades retain their components; the returned Trove has no saved contents/upgrades/lock reference.

Approval: source-parity restoration under the standing scope and existing approved conservation corrections; no new acquisition, ability, or creative-policy change. Avoiding the original locked-empty infinite loop follows those safety constraints.

## Verification

Command: timeout --foreground 10m ./gradlew assemble --no-daemon

Exit 0, 21 seconds; build/trove-spill-20260909-170928.log. All four leaves assembled. Command timeout --foreground 60s python3 scripts/verify_artifacts.py returned 0; build/trove-spill-artifacts.log. git diff --check passed.

Mapped native bytecode was inspected on both Fabric versions; destroyBlock invokes ServerLevel.removeBlock once, after native authorization. Forge 47.3.39 source confirms its break event and restrictions precede onDestroyedByPlayer. Both production Fabric jars contain the registered new mixin with remapped method/target annotations; evidence: build/trove-spill-mixin-verification.json. An initial ad-hoc annotation assertion used an incorrect expected intermediary method name and was corrected to inspect the actual emitted annotations; it was not a build failure.

These are compilation/packaging checks, not runtime proof. Final acceptance must exercise actual player breaking on all loaders, empty/loaded/locked/voiding/full-upgrade Troves, native-sized and component-bearing outputs, cancelled and failed removal, normal packed breaking and creative behavior. Fabric mods redirecting the same invocation require compatibility testing. Entity-spawn vetoes, gamerules and interrupted saves remain part of the storage acceptance campaign, not certified here.
