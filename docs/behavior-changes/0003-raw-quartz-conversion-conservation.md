# 0003 — Raw-quartz chest conversion must conserve inventory

Status: approved; implementation pending. Neither conversion nor this correction is implemented in the modern port.

Handoff: [HANDOFF.md](../HANDOFF.md). This correction was approved independently of the later guidebook/wearable/dormant-feature choices. It depends on porting RawQuartzItem and Radiant Chest; the existing shaped-quartz slice and its smoke tests do not exercise chest conversion.

## Observed upstream logic

Baseline `80944ce45c6559243d8928cc4b305bf379388652`, `src/main/java/com/aranaira/arcanearchives/items/RawQuartzItem.java:59-104`:

1. Sneak-use on a chest extracts stacks into `stacks` and replaces the block with a Radiant Chest.
2. Lines 81-85 insert each extracted stack into the new inventory and collect rejected remainders in `leftover`.
3. When `leftover` is nonempty, lines 94-98 spawn every original stack from `stacks`, rather than just the remainders. Successfully inserted amounts would then also be dropped.

This is a source-level inventory-conservation defect under partial-rejection conditions. The trigger has not been reproduced in a running upstream game; ordinary vanilla chests may not reach it at default capacity. Do not describe this as a verified universal exploit.

## Proposed change

When destination insertion partially rejects contents, drop only rejected remainders, never stacks already inserted. Preserve successful chest conversion, owner assignment and normal quartz consumption. Validate prerequisites and perform world/inventory mutations on the authoritative server without losing contents on failure. Any additional player-visible changes to creative consumption, supported chest types or failed-conversion semantics need a separate proposal.

## Reason and affected targets

Inventory conservation and multiplayer integrity. Applies to Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Compatibility

Existing storage content must not be lost or duplicated. No save schema change is proposed. Players can no longer receive both inserted contents and duplicate dropped stacks in the partial-rejection path.

## Alternatives

- Reproduce the old branch exactly: unacceptable for a new implementation because it intentionally preserves an inventory duplication defect.
- Refuse any conversion that cannot retain all contents: a larger behavior change, not included in this proposal.

## Approval

The user answered "3. Yes" to the explicit request to correct raw-quartz conversion so it drops only rejected inventory remainders. This approves the correction above, not unrelated gameplay changes. Implement it as part of the chest-conversion port and retain the verification gates below.

## Verification required

- Add a failing conservation test with a destination that accepts part of a stack and rejects the rest.
- Assert original total equals new inventory total plus dropped remainders, item by item and including NBT/data components.
- Cover no remainders, full rejection, mixed item types, destination creation failure and survival/creative consumption under separately confirmed semantics.
- Exercise server authority and multiplayer interaction on all four leaves before marking the conversion task complete.
