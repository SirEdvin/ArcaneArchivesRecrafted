# 0091 — Amphora unexpected-pickup recovery

Status: implemented; four-target assembly/package and focused native-slot fixtures pass. Approval: existing safety/conservation corrections, not a new fluid-transfer ability.

The port's AmphoraFluidStorage.recoverPickup preserves a modded BucketPickup result that cannot safely be accepted into the linked Tank. Inventory.add can erase a remainder for creative players with full inventories, losing the only recovered representation of removed world fluid. This recovery path is a modern conservation correction, not an original upstream ability.

Use native Slot.safeInsert over only the player's main inventory, merging occupied slots before empty slots. Never touch armor/offhand or consume the rejected item. Synchronize changes and drop any remainder through the existing world-drop path; dispensers with no player continue to drop the recovered container. No changes to fluid identity, acceptance, transfer amounts or saved links.

Verification: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*AmphoraPickupRecoveryTest' --offline --no-daemon`: exit 0, 26s, `build/amphora-recovery-20260909-201856.log`. NeoForge XML reports 2 tests, zero skips/failures/errors: full main-inventory preservation, untouched armor/offhand, merge-before-empty ordering, native bucket limits and component equality. Fixtures use Inventory without a Player; no creative/survival branch exists in the replacement insertion path. They do not exercise a world pickup or drop.

`timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/amphora-recovery-artifacts.log`; all four production/source pairs pass. `git diff --check` passes. Actual modded pickup callbacks, connected-player synchronization and world drops remain runtime acceptance tasks.
