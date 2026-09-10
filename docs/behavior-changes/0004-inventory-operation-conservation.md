# 0004 — Reject invalid inventory operations and prevent phantom upgrades

## Original behavior

Pinned upstream: `80944ce45c6559243d8928cc4b305bf379388652`.

- `inventory/handlers/SizeUpgradeItemHandler.java:60-85` does not check that the requested upgrade is present before returning `new ItemStack(getUpgradeForSlot(slot))`. With all upgrade flags false, extracting one item from slot zero passes all guards and returns an upgrade. The operation can be repeated without inserting an upgrade. This is a direct source/control-flow finding; a full legacy-game runtime reproduction has not been performed.
- The same class's insertion path (`:35-55`) does not reject an already occupied upgrade slot. It can consume another upgrade while leaving a boolean true.
- Negative slot indices are not consistently rejected by size-upgrade operations.
- `inventory/handlers/ExtendedItemStackHandler.java:48-81` only special-cases amount zero. Negative amounts reach arithmetic intended for positive extraction, potentially increasing the stored count.
- The earlier tome trace separately identifies receipt being committed before entity spawning, without checking spawn success. That is not covered by this proposal.

## Proposed behavior

For every supported target:

- Extract an upgrade only when that slot actually contains it.
- Reject inserting a duplicate into an occupied upgrade slot without consuming input.
- Retain upstream upgrade ordering, capacity constraints, quantities, accepted items and normal valid-operation behavior.
- Validate slot bounds and reject negative transfer amounts before any mutation; zero transfers remain no-ops.
- Keep simulation non-mutating, including returning detached stacks where returning live storage would permit accidental mutation.
- Preserve item identity/components and conservation through rejected/partial operations.

## Reason

Prevent item duplication, accidental consumption and invalid-operation mutation. Copying the old code literally would violate the port's inventory-conservation requirements.

## Scope and approval

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. No legacy save conversion, gameplay capacity changes, new upgrade types, dormant features or tome-grant ordering changes are authorized by this proposal.

Status: approved. After requesting the approval form again, the user selected “Approve these corrections and continue implementation” in the focused proposal-0004 form. This supersedes the earlier timeout. Implementation and verification are tracked below; approval alone is not completion.

## Required verification

Focused regression cases for empty-slot extraction, repeated extraction, occupied-slot insertion, prerequisite/removal ordering, negative/boundary indices and amounts, simulation, partial transfers and component-sensitive inventory totals. These checks cannot be replaced by a final successful compilation.
