# 0111 — Tome automatic-grant failure safety

Status: not adopted; no longer an approval blocker. The user does not consider a missed automatic grant worth changing because the Tome has a crafting recipe. Preserve upstream grant ordering and restore its shapeless book + `nuggetGold` recipe as part of the Tome migration. No production grant code or partial book has been added. The proposal below is retained as historical analysis, not implementation authorization.

## Original behavior

At development pin `80944ce45c6559243d8928cc4b305bf379388652`, `src/main/java/com/aranaira/arcanearchives/events/EventHandler.java:504-524`, `givePlayerBookMaybe` checks `receivedBook`, sets it true, marks dirty and invokes saved-data persistence before spawning the bound Tome item entity. It sends the gold route message before calling `world.spawnEntity`, ignores the boolean spawn result, and plays cloth-fall sound afterward.

Thus a rejected entity spawn can leave the player permanently recorded as having received the automatic book, despite no book being delivered. This is directly traced control flow, not an upstream runtime reproduction. Crafting a book remains a separate acquisition route.

Bookshelf breaking and Radiant Resonator crafting use this helper under their original independent configuration gates. Tome crafting marks receipt only within the existing `BookFromResonator` branch. There is no explicit creative-mode exclusion in the original bookshelf handler. See `../migration/TOME_PARITY.md` for full provenance.

Current source has server-thread-owned, overworld UUID-keyed `PlayerSaveData` receipt storage and both configuration options, but no registered Tome or automatic grant caller. Existing conservation approvals 0004/0005 explicitly exclude Tome ordering changes. Backend approval 0001 does not resolve this gameplay failure path.

## Proposed behavior

On an eligible server-side automatic-grant attempt:

1. Preserve the existing received check, route eligibility and configuration semantics.
2. Attempt the same bound count-one item-entity drop at the player's position with zero pickup delay; do not replace it with inventory insertion.
3. Only after native entity insertion reports success, mark/persist receipt and issue the original route message and sound.
4. If insertion reports failure, leave receipt unclaimed and emit no successful-grant message/sound. A later eligible bookshelf break or resonator craft may retry; no background retry loop or extra acquisition route.
5. Prevent reentrant grant attempts for the same player during synchronous spawn callbacks, with the temporary guard released on every exit. Do not swallow unexpected exceptions or manufacture success.

This changes failed-spawn behavior and feedback ordering, not ordinary successful-grant quantities, timing triggers, crafting costs, binding, pickup delay or config defaults. Preserve Tome-crafting receipt inside `BookFromResonator`; keep death/dimension/reconnect receipt ownership in overworld saved data.

## Reason and affected targets

Avoid knowingly migrating a lost automatic-grant opportunity when an entity-join hook rejects the drop. Targets: Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

Native item-entity persistence and saved-data persistence are not one crash-atomic transaction. This proposal does not promise exactly-once delivery across abrupt process death, disk failure, or a third-party callback that inserts an entity and then throws. It adds no durable outbox/journal and no legacy-world converter. Such guarantees would require a separate design decision.

## Approval requested

Approve success-only receipt/message/sound with retry eligibility after a rejected spawn, rather than preserving upstream receipt-before-spawn behavior. The general permission for extra validation is not treated as implicit approval for this explicitly excluded ordering change.

## Verification required after approval

- Rejected insertion: no receipt, no success feedback; next eligible event can retry.
- Accepted insertion: one correctly bound entity, zero pickup delay, receipt and original feedback; repeated events do not grant another book.
- Reentrant callback: no duplicate grant; temporary guard clears on failure/exception.
- Both configuration gates, original Tome-crafting branch and creative behavior preserved.
- Receipt survives ordinary save/restart, reconnect/death and dimension change; separate player UUIDs remain independent.
- All four native loader paths exercised; client opening/content/recipe/conditional-page acceptance remains part of the complete Tome milestone.

Current verification is source inspection plus documentation whitespace validation only. No build or gameplay test is claimed for an unimplemented proposal. Radiant Furnace remains excluded; independent Echo support is unchanged.
