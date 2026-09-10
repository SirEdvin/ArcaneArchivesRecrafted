# 0101 — Hive resignation and ordered succession

Status: implemented and assembly/package-verified on all four supported targets. Source-backed restoration within the standing migration scope; no new gameplay deviation proposed. Connected gameplay acceptance remains open.

## Original behavior

Release `bb99accf48ed583e29b0efae56e28c963407b8df`:

- `items/LetterOfResignationItem.java:31–62`: a valid creator-stamped resignation is usable only by its creator. A member leaves their current Hive; successful use consumes one letter, including creative mode. Invalid/wrong-owner/nonmember use retains it. Original messages and gold tooltip are preserved.
- `items/templates/LetterTemplate.java:17–39`: ordinary stack limit, BOW use animation and 64-tick held use, with mutation only on the server.
- `data/HiveSaveData.java:127–163,198–216`: owner departure promotes the first member of the LinkedHashSet (oldest join order), removes that member from the nonowner member set and rekeys the Hive. A Hive with no nonowner members disbands. Thus either departure from a two-player Hive leaves both players unaffiliated.
- `data/HiveSaveData.java:165–194`: gold same-world notifications to remaining members/owner; disband notification to the lone remaining owner. No cross-dimension broadcast.
- `init/RecipeLibrary.java:91–100`: three paper, one radiant dust and one pink dye; creator attribution; only current Hive members can craft.

## Implementation

`HiveSaveData.resign` preserves member order, succession and disbanding while maintaining the existing validated persistence invariant. `LetterOfResignationItem` consumes only after successful mutation, checks stored creator UUID, uses original feedback and notifies the remaining Hive or lone player. Resignation marks SavedData dirty rather than adding an immediate forced save absent from the original action.

`LetterItem` shares the existing invitation data reads, use mechanics and live server/player completion boundary with resignation. Invitation behavior and the approved stale-author rule are unchanged. The native recipe serializer adds the strict `hive: "resignation"` condition, reusing authoritative membership checks in open-menu refresh and guarded payment. Original item model/texture, atlas entry, language values and creative entry are restored. MIT notices remain; both resource files match the release pin byte-for-byte and no animation sidecar exists there.

This completes the implemented membership join/leave lifecycle, not the storage-network milestone. Expulsion/anvil naming, Manifest/Brazier routing and full ownership/network lifecycle integration remain unfinished.

## Verification

`timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*HiveResignationTest' --tests '*HiveInvitationTest' --tests '*HiveCraftingConditionsTest' --tests '*GemCutterDataRecipeTest' --tests '*GemCuttersTableCraftingTest' --tests '*GemCuttersTableMenuTest' --offline --no-daemon`

Exit 0, 29s; `build/hive-resignation-20260910-063036.log`. Four targets assembled. NeoForge XML confirms 58 tests, no skips/failures/errors, including five new resignation cases: ordered succession and NBT round trips, member departure/two-player disbanding, unauthorized/malformed/nonmember/repeated attempts, old invitation eligibility after resignation, and native recipe wire/creator preservation plus use properties. Existing invitation and crafting regressions remain green.

`timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, 0s; `build/hive-resignation-artifacts.log`. All four production/source pairs pass, including new classes, original artwork, recipe and atlas entry.

No client/server launched. Fixtures do not execute native held-use completion, live recipe payment/tag binding, notification delivery, connected `/hive` reports, real disk save/restart, or multiplayer across dimensions. Those remain acceptance requirements; compilation is not gameplay parity.
