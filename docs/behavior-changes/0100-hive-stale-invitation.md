# 0100 — Revalidate invitation authors before Hive membership changes

Status: explicitly approved by the user; implemented in the server-owned invitation path on all four supported targets. Assembly/package and focused regression verification completed; connected gameplay acceptance remains open.

## Original behavior and evidence

Release pin `bb99accf48ed583e29b0efae56e28c963407b8df`, checked at `/tmp/arcane-archives-reference/migration-source`:

- `init/RecipeLibrary.java:80–89`: invitation crafting is permitted only for nonmembers or the current Hive owner.
- `items/LetterOfInvitationItem.java:39–60`: use reads the saved creator, calls `getHiveByOwner(creator)`, then attempts to add the recipient. It does not recheck the creator's membership.
- `data/HiveSaveData.java:57–64`: getHiveByOwner creates a Hive when none is indexed under that owner.
- `data/HiveSaveData.java:85–103`: membership lookup prefers a nonempty owned Hive over membership in someone else's Hive.
- `data/HiveSaveData.java:108–124`: addMember checks the recipient's existing membership, not the prospective owner's membership elsewhere.
- `data/DataHelper.java:164–185`: network lookup uses this membership result and assembles member networks from the Hive records. Overlapping membership is consequently not just a cosmetic command issue. Actual storage exposure has not been reproduced in Minecraft.

Reachable sequence: Alice crafts an invitation while unaffiliated; Alice joins Bob's Hive; Carol later uses Alice's old invitation. Upstream creates Alice's Hive with Carol while retaining Alice in Bob's member list. Alice's membership lookup switches to her newly created owned Hive.

## Approved behavior

Before creating or modifying a Hive on invitation use, resolve the author's current membership from authoritative server data. Permit the invitation only if its author is unaffiliated or still owns their current Hive. If the author is now a member of another Hive, reject the invitation using the existing failed-join feedback, retain the letter and leave all Hive records unchanged. Continue validating recipient eligibility. Apply validation and mutation together on the server thread.

No expiration timer, revocation registry, new recipe cost or automatic membership migration was added. Eligibility is checked anew on every attempt. Normal invitation behavior remains unchanged. Approval covers only the stale-author correction, not a redesign of ownership succession, resignation, expulsion or network access.

## Reason and approval

Prevent overlapping Hive records and inconsistent network identity. The crafting restriction alone cannot authorize later use of a transferable, persisted letter. The user explicitly approved this proposal with “yes” after the correction and letter-retention behavior were described. This is separate from earlier crafting revalidation approvals.

## Verification

A minimal Python transcription of the relevant lookup/add logic executed successfully. `build/hive-stale-invitation-model.json` records Alice's lookup changing from Bob to Alice while Bob still lists Alice, and Carol joins Alice. This is a source-logic model, not execution of upstream Java or a Minecraft runtime reproduction.

Five NeoForge Hive invitation fixtures cover normal invitations, both author eligibility states, stale-author rejection without consumption/data mutation (including after NBT decode), existing recipient membership, self-invitation, missing creator UUID, wrong/empty items, member order, defensive member views, malformed/overlapping save rejection, and recipe wire/creator stamping. These are detached fixtures, not connected-player or actual disk-restart tests.

Final verification: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*HiveInvitationTest' --tests '*HiveCraftingConditionsTest' --tests '*GemCutterDataRecipeTest' --tests '*GemCuttersTableCraftingTest' --tests '*GemCuttersTableMenuTest' --offline --no-daemon`, exit 0, 27s, `build/hive-invitation-final-20260910-062422.log`. All four targets assembled; XML confirms 53 tests, no skips/failures/errors (5 new invitation cases plus 48 existing policy/recipe/menu/payment regressions). `timeout --foreground 60s python3 scripts/verify_artifacts.py`, exit 0, 0s, `build/hive-invitation-artifacts.log`; all four production/source pairs pass. Both copied artwork files separately compared byte-for-byte with the release pin. No development client/server was launched.

## Restored runtime slice

Follow-up: [0101](0101-hive-resignation-succession.md) restores resignation and ordered succession, superseding those implementation gaps below; their runtime acceptance remains open.

- `data/HiveSaveData.java`: world-owned native SavedData, single-owner membership, ordered members, dirty marking and validated fresh-world persistence. Invitation checks complete before creating a Hive. No remote storage access, static cross-server state or legacy import.
- `items/LetterOfInvitationItem.java`: registered `letter_invitation`, original 64-tick BOW use and ordinary stack limit, read-only gold tooltips, server-held creator identity, existing status messages, same-world member notifications, consumption only on success (including creative as upstream), native saved-data flush.
- Original Gem Cutter acquisition: three paper, one radiant dust and one light-blue dye; creator stamping only in the paid server path. Strict `hive: "invitation"` recipe condition uses authoritative membership at presentation and payment revalidation. Open menus refresh eligibility without reopening.
- `events/HiveCommands.java`: original player-only `/hive` membership report with no operator requirement, owner/name/UUID/member lines, native registration on each loader. The reported UUID is the actual Hive owner, not an invented storage-network ID.
- Original release item model/texture copied byte-for-byte; no animation sidecar exists at the inspected pin. Atlas entry, original English/Portuguese language values, optional common dye tags, production/source packaging checks added. Original MIT notices remain.

This restores invitation membership and diagnostics, not a working shared-storage network. Resignation, succession, expulsion/anvil naming, Manifest/Brazier routing, network ownership lifecycle and synchronization remain separate unfinished migration work. Actual use completion, recipe-tag binding/payment, command delivery, notifications, multiplayer and world restart need live acceptance on all four targets.
