# Brazier horizontal radius correction

## Original behavior

The source audit found that ServerNetwork computes the Z delta as `pos2.getZ() - pos2.getZ()`, so destination range ignores Z. This was found in both the release and master baselines.

## Approved behavior

Use the actual X and Z deltas for horizontal radius eligibility, including cached-route revalidation. Preserve existing radius limits, dimension checks, ranking, cache priority and other routing semantics. This explicitly extends 0103, which did not authorize correcting ordinary range calculation.

## Reason

Restore a real horizontal radius rather than an unbounded strip.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval status

Approved explicitly by the user in response to the three network-migration decisions.

## Verification

Decision recorded only; implementation and runtime verification remain pending.

Required tests: X/Z symmetry, radius boundaries, differing heights, cross-dimension rejection and cached destinations becoming ineligible.
