# Tome item and crafting acquisition

## Implemented scope

- Registered `arcanearchives:tome_arcana`, stack limit one, creative entry, original gold tooltip and original item artwork/model.
- Modern binding is intrinsic to item identity: `TomeOfArcanaItem.BOOK` is `arcanearchives:tome_arcana`. No legacy NBT import or second mutable authoritative binding is introduced. Ordinary copies and crafted/granted stacks all refer to that same book ID.
- Right-click in air (either hand) and use-on-block invoke the public Patchouli client API without consuming the stack. Server use returns failure as upstream did; no Minecraft client implementation class is referenced by the item.
- Shapeless book + existing `arcanearchives:ingredients/nugget_gold` ingredient tag yields one Tome. That tag includes vanilla gold nuggets and optional existing common/Forge gold-nugget tags. Native recipe result keys follow the repository's version expansion.
- Native item crafting callbacks for the Resonator and Tome feed shared `TomeAcquisition`. `BookFromResonator` guards both routes. Resonator crafting grants once; Tome crafting marks receipt. Existing UUID-owned overworld `PlayerSaveData` is reused.
- Preserve 0111: mark and flush receipt before spawning; send the original gold message, attempt a count-one item entity at the player with zero pickup delay, then play wool-fall sound. Failed spawn is not retried and does not clear receipt. No inventory insertion or creative-player exclusion added.

Bookshelf eligibility is awaiting the separate decision in [0149](../behavior-changes/0149-tome-bookshelf-eligibility.md); no bookshelf hook is installed. The complete Patchouli book JSON is **not yet generated**. The opening route is implemented, but this is not a usable/readable book or a release-ready deliverable until page conversion is complete.

## Source and artwork

Read release-pinned `items/TomeOfArcanaItem.java`, `events/EventHandler.java:509–551`, `recipes/tome_arcana.json`, `models/item/tome_arcana.json` and `lang/en_us.lang` at `bb99accf48ed583e29b0efae56e28c963407b8df`. The main-source MIT notice and credits in `docs/upstream` were reviewed before recovery. `textures/items/item_tomeofarcana.png` was recovered directly with `git show` from that pin (3348 bytes) into modern `textures/item/`; model texture reference adapted accordingly. No Guidebook/Patchouli code or backend artwork was copied.

## Verification

`timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 97 s, `build/tome-acquisition-20260913-165408.log`. All four native suites passed. Shared `TomeAcquisitionLifecycle` is called from the already wired native lectern/crafting fixture and executes on every leaf.

Native assertions cover two paid Resonator crafts, preserved bucket remainder and exact ordinary ingredients, only one Tome drop/receipt, immediate pickup eligibility, shapeless Tome crafting in two different arrangements with exact payment, max stack size, item copy and nonconsuming server use from both hands. These are in-process native result-slot operations, not connected client clicks. Configured-off behavior, failed-spawn injection/actual loader cancellation, fresh Tome-crafting receipt separate from the Resonator route, book UI, use-on-block interaction delivery, audio, restart and reconnect remain unverified.

`timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, `build/tome-acquisition-artifacts.log`; checks compiled/source classes, exact packaged Tome model/texture and versioned recipe. No client campaign or harness extension was run. Full migration remains incomplete.
