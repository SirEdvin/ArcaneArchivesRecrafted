# 0149 — Modern bookshelf eligibility for Tome grants

Approval status: approved by the user ("yes"). Implemented on all four targets; native default-tag break verification passed as scoped below.

## Original behavior

At release pin `bb99accf48ed583e29b0efae56e28c963407b8df`, `events/EventHandler.java:531–535` grants the one-time Tome on a server block-break event when `BookFromBookshelf` is enabled and the block is an instance of legacy `BlockBookshelf`. This includes subclasses, not arbitrary blocks, and has no creative-player exclusion.

The inspected native 1.20.1 Forge sources and 1.21.1 NeoForge sources no longer contain that dedicated bookshelf class. `Blocks.BOOKSHELF` is a generic `Block`. An `instanceof Block` migration would therefore grant books from unrelated blocks; identity-only matching would provide no counterpart for modded bookshelf subclasses.

## Proposed behavior

Replace the removed class predicate with the block tag `arcanearchives:tome_bookshelves`, containing only `minecraft:bookshelf` by default. Other mods and data packs may explicitly add equivalent bookshelf blocks. Do not implicitly include chiseled bookshelves, every enchanting-power provider, or similarly named blocks. Keep the original server/configuration/receipt guards and creative-player eligibility.

This is an explicit compatibility contract, not an assertion that any existing common tag exactly reproduces the old class hierarchy. The new tag is installed with only the approved default.

## Reason, scope and alternatives

Retain the vanilla trigger while providing an explicit modern extension point without broadening grants to unrelated blocks. Applies to 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Does not change the separately retained receipt-before-spawn ordering, crafting grant, sound or message.

Alternative: match only `minecraft:bookshelf` with no extension tag. Neither alternative can discover legacy subclasses on modern Minecraft.

## Verification

Implemented: shared tag/config/receipt checks in `TomeAcquisition`, Fabric BEFORE break callback and Forge/NeoForge break-event listeners, registered during common initialization. Original receipt-before-spawn behavior is retained.

Native `ServerPlayerGameMode.destroyBlock` tests passed for survival and creative players: stone/chiseled shelves do not claim receipt; two ordinary bookshelf breaks produce one immediate-pickup Tome. First run exposed missing 1.20 block-tag pluralization in our namespace (`build/tome-bookshelf-20260913-170901.log`, exit 1, 109 s); both old-version resource transforms now cover it. The next run was interrupted and is not matrix evidence. Recovery: `timeout --foreground 10m ./gradlew build --no-daemon`, exit 0, 81 s, `build/tome-bookshelf-recovery-20260913-172643.log`, all four native suites passed. Artifact checks also require the correct versioned tag path and exact default membership (`build/tome-bookshelf-artifacts.log`, exit 0).

Remaining verification: configured-off behavior, tag-added test block, cancelled-event ordering, actual restart and connected-client feedback. Normal native breaks do not establish those cases. Full Tome page conversion remains unfinished.
