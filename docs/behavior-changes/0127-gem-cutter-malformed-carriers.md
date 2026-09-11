# Reject malformed Gem Cutter item crafting data before placement

## Status — superseded, not adopted

The user approved [0128 real inventory output](0128-gem-cutter-inventory-output.md) and explicitly dropped pre-release compatibility, including the development-only pending-craft journal/carriers. The journal and its malformed-carrier acceptance fixture were removed. The historical malformed custom item-data placement behavior below is **not claimed fixed**. No pre-placement carrier validator or compatibility reader was introduced; current world inventory decoding remains validated.

The following records the historical proposal and RED evidence, not an active approval blocker.

## Original/current behavior

This concerns the port's fresh-world `Crafting` item data, not importing legacy saves. `GemCuttersTableBlockEntity.loadCrafting` already rejects a non-compound `Crafting` value and delegates valid compounds to the existing strict `GemCutterCraftingState` decoder. Valid paid-state carriers must remain supported.

Native item data import happens after the parent block has been placed. A new native fixture supplies a server-created Gem Cutter item whose entity data contains `Crafting:"invalid compound"`:

- Both 1.20.1 leaves throw `IllegalArgumentException: Invalid Gem Cutter data`, leave the parent placed without its accessor, and retain both offered items.
- Both 1.21.1 leaves finish placement, consume one item and leave both parts despite the rejected crafting payload.

These are reproduced modern native item-placement results, not proof of an upstream legacy bug or a remote client exploit. Earlier valid-carrier preservation and world-data validation are separate contracts.

## Proposed behavior

Validate supplied crafting data on detached state with the existing decoder before writing any block. Reject malformed carriers normally, leaving the whole offered stack/data unchanged and no placed parts/entities/drops. Do not discard malformed data, replace it with empty inventory, or catch errors after placement and call the operation successful.

Preserve absent-data ordinary items, valid ordinary-input and pending-paid-output carriers, native owner reassignment/fake-player exclusion, native placement cancellation and collision checks. Reuse the native item data APIs for each version. Keep strict world-state decoding; do not introduce legacy save conversion or arbitrary scripted NBT support. Only validation failures should become normal rejection, not unrelated programming/runtime errors.

## Reason

The current failure occurs after a world write: one version family leaves a partial unpaid placement, while the other consumes the item despite rejecting its payload. Preflight validation prevents those outcomes without weakening the saved-state contract.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval status

Proposed; awaiting user approval. No production change has been made. The new regression is intentionally RED pending this decision. Approval 0126 covered collision only, not malformed item-data policy.

## Verification

Fixture: `DeviceOwnershipLifecycle.malformedCarrier`, using each wrapper's native item block-entity-data adapter and real registered BlockItem placement. The fixture requires normal rejection, unchanged complete stack, no parent/accessor/entity/item drops, and cleans owned positions/entities in `finally` even when placement throws.

Command: `timeout --foreground 10m ./gradlew :1.20.1-fabric:runGameTest :1.20.1-forge:runGameTestServer :1.21.1-fabric:runGameTest :1.21.1-neoforge:runGameTestServer --continue --no-daemon`.

Result: exit 1 in 54 seconds, `build/gem-cutter-malformed-red-20260911-110450.log`. All four native tasks fail with the version-specific outcomes above. Both Fabric XML reports confirm the exact ownership-test failure. No project JVMs remain. No green build/artifact acceptance is claimed for this new regression.

After approval, verify malformed outer and nested crafting payloads, valid/absent-data controls, paid-state and ordinary-input preservation, cancellation, all four build/native suites and artifact isolation. This does not close connected gameplay or broader migration acceptance.
