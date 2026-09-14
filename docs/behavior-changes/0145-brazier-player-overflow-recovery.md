# 0145 — Brazier player overflow spawn-failure recovery

Approval status: approved by the user ("yes"). Persistence, codec, delivery, login hooks and installed-device paid-deposit recovery implemented; block activation and connected acceptance remain pending.

## Original behavior

Pinned upstream `bb99accf48ed583e29b0efae56e28c963407b8df`, `tileentities/BrazierTileEntity.java:267–294`, returns the first unpaid stack to the selected slot when the reference was held, stacks subsequent remainders into the main inventory, then ejects overflow. `rejectItemStack` at lines 166–176 ignores the result of `world.spawnEntity(item)`. Failed overflow spawning therefore has no explicit recovery for already extracted player items. This is a source-audited failure path, not a reproduced live loss and not a concurrency claim. Normal bounded matching stacks usually fit into the space their extraction vacated; overflow is a separate exceptional path, including oversized source stacks.

Approved 0144 retains a dropped-item source entity with only its unpaid remainder. It does not define player recovery: this caller has no original source entity to retain.

## Proposed behavior

Preserve ordinary extraction, destination-major batch insertion, selected-slot return, main-inventory stacking and successful overflow ejection.

If an overflow spawn is refused:

1. Retry returning only that unpaid remainder to the player's main inventory, without creative-mode voiding or overwriting unrelated items.
2. If it still cannot fit, retain the exact remaining item/data/count in persistent player-owned pending returns, not a Brazier input buffer.
3. Attempt inventory delivery on login and subsequent Brazier interaction; never route pending returns automatically or repeatedly attempt world spawning.
4. Refuse new Brazier player deposits while pending returns remain, so repeated failures cannot grow an unbounded backlog.
5. Never restore any portion already accepted by a destination or successfully spawned. Clear only the portion actually delivered.

## Reason

Prevent exceptional failed ejection from deleting unpaid player items without undoing successful destination transfers, bypassing a spawn cancellation, overwriting inventory or introducing ordinary device buffering. This requires an explicit behavior decision rather than treating 0144's entity-source fallback as authorization for a new persistent player recovery path.

## Affected targets

Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Verification required

Native full/partial/refused deposits; selected-slot return and stacking; successful overflow; injected spawn refusal with exact conservation; no restoration of accepted/spawned items; full and creative inventories; repeated interaction while recovery is pending; retained item-data/count; player persistence and reconnect/restart delivery without duplication. Injected refusal is not evidence of an actual loader event cancellation. Existing player save infrastructure must be inspected before choosing its storage representation.

## Persistence prerequisite

Post-recovery follow-up verifies that a separate explicit interaction can pay recovered items normally, and a subsequent repeat against the now-full destination neither duplicates payment nor resurrects pending returns. All four native suites pass (`build/brazier-post-recovery-20260913-093215.log`, exit 0, 58s); artifacts pass (`build/brazier-post-recovery-artifacts.log`). Test-only continuation of the installed-device recovery sequence; connected input and persistence/restart acceptance remain separate.

Recovery interactions now stop after pending delivery, even when it completely succeeds: returning items into an empty selected slot must not automatically route those same items on the same interaction. This enforces the approved no-automatic-routing requirement; a subsequent explicit interaction can deposit normally. Native regression first failed on Fabric 1.21.1 (`build/brazier-recovery-reroute-red-20260913-092832.log`, exit 1, 19s, `Recovery interaction automatically routed the just-returned items`). The minimal early-return fix passes all four native suites (`build/brazier-recovery-reroute-20260913-092924.log`, exit 0, 86s), with artifact verification passing (`build/brazier-recovery-reroute-artifacts.log`). No connected-input or crash/restart claim is added.

`BrazierBlockEntity.deposit` now retries pending inventory returns before source selection, blocks payment while returns remain, uses device-local click history and actual native source extraction, routes the batch, returns the first remainder to the selected slot and later remainders to the main inventory, and reuses rejected-entity creation for overflow. A refused spawn retries inventory then queues only unpaid/unspawned items. Native full/partial/zero acceptance and oversized-source successful/refused overflow tests verify source/destination/ejected/pending counts, subsequent payment blocking and interaction-time recovery on all four targets (`build/brazier-paid-player-20260913-091513.log`, exit 0, 85s; `build/brazier-paid-player-artifacts.log`, exit 0). Failed spawning is injected; tests invoke the installed device directly. Block activation, actual loader cancellation, connected delivery and restart remain unverified.

Native login wiring now registers Fabric `ServerPlayConnectionEvents.JOIN` and Forge/NeoForge `PlayerLoggedInEvent` handlers from common initialization, resolving saved returns using the joining player's UUID. Four-target build/native regression suites and artifacts pass (`build/brazier-login-hook-20260913-091039.log`, exit 0, 86s; `build/brazier-login-hook-artifacts.log`, exit 0). Existing native tests exercise delivery directly; this checkpoint does not verify a login with seeded pending returns, connected reconnect delivery or restart. Those acceptance gaps remain open.

`PlayerSaveData.deliverBrazierReturns` now prepares copied main-inventory slots and exact residual pending entries before publishing either, reusing the Trove's non-voiding occupied-first insertion. It never spawns, routes, writes offhand, or treats an undecodable batch as delivered. Native survival/creative fixtures verify partial delivery, full-inventory no-op retries, exact subsequent completion, item-data preservation, no duplicate completion and undecodable batch retention. All four native suites pass (`build/brazier-pending-delivery-20260913-090659.log`, exit 0, 89s); artifacts pass (`build/brazier-pending-delivery-artifacts.log`). Delivery is called directly by tests: login/interaction hooks and production failed-ejection payment are still pending. This does not establish crash-atomic persistence across separate player/world saves or real client packet delivery.

The native item codec now stores one-item identity/data separately from a positive integer count, preserving oversized pending stacks without legacy byte-count truncation. It prepares the entire bounded batch before saving, refuses overwriting outstanding returns, and decodes all entries before exposing any for delivery. Unknown items, invalid counts or unrecognized payloads remain stored and block decoding rather than disappearing. Native tests cover ordered item/data/count round trips (including `Integer.MAX_VALUE`), independent decoded stacks, invalid later entries, missing items, overwrite refusal and failed-encoding atomicity. All four native suites pass (`build/brazier-pending-guards-20260913-090343.log`, exit 0, 61s); artifact verification passes (`build/brazier-pending-guards-artifacts.log`). The codec's initial build also passed (`build/brazier-pending-codec-20260912-211556.log`, exit 0, 86s). These are storage/codec checks, not delivery or activation verification.

`PlayerSaveData` now retains an optional copied `brazier_pending_returns` NBT payload alongside the original receipt flag. Presence is separate from decoding: unknown payloads survive a save/load round trip rather than being discarded as delivered. Setter/getter/save/load boundaries copy tags, unchanged assignments do not dirty the save, and explicit clearing removes stale data even when reusing an output compound. No item decoding, inventory delivery, deposit blocking or activation is implemented by this storage change.

`timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 89s, `build/brazier-pending-save-20260912-211139.log`. Fresh `PlayerSaveDataTest` XML reports on all four targets contain five tests each with zero failures/errors/skips. Tests verify receipt compatibility, opaque payload and count preservation, copy isolation, dirty tracking, explicit clearing and unknown-payload retention. Artifact verifier exit 0 (`build/brazier-pending-save-artifacts.log`). This is NBT round-trip coverage, not process-restart or delivered-item conservation evidence. No live loss has been reproduced.
