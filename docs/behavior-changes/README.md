# Gameplay and logic change register

- [Tome Gem Cutter recipe-display adapter](0112-tome-gem-cutter-recipe-display.md): approved-backend API component, counted native previews, original artwork and templates; matrix tests/builds/startups pass. Complete Tome integration and connected rendering remain open.

- [Tome automatic-grant failure safety](0111-tome-grant-failure-safety-proposal.md): not adopted; user accepts crafting as fallback. Preserve original grant ordering. No approval blocker; Tome implementation remains pending.

- [Native gem HUD](0071-gem-hud.md): original held/socket silhouettes, charge bars and toggle indicators; four-target assembly and pinned art packaging verified, visual acceptance deferred.

- [Gem Socket opening key](0070-gem-socket-key.md): original unbound, Arsenal-gated key and validated native server menu opening; not a recharge key. Four-target assembly passed; runtime acceptance deferred.

- [Dedicated wearable Gem Socket](0069-wearable-body-slot-mapping-proposal.md): user-selected custom slot on both APIs, optional native adapters, one active worn gem and persisted state; four-target assembly passed. Shared controls/presentation and runtime acceptance remain open; no mapping approval blocker.

- [Gem Socket native runtime](0068-gem-socket-runtime.md): persistent gem inventory, native menu/recharge control, original acquisition/art and open-socket active effects; worn integration, shared presentation and runtime acceptance remain open.

- [Chromatic powder recharge](0067-chromatic-powder-recharge.md): original color/full-spectrum items/art, priority/exclusions and shared held-gem depletion callbacks; socket GUI/recharge button and wearable availability remain open. Corrects the inferred keyboard-control task.

- [Automatic gem inventory-material recharge](0066-gem-material-auto-recharge.md): restores depletion-triggered recharge across five gems; powder/key/socket systems remain open. Corrects Slaughtergleam final-charge behavior when nuggets are available.

- [Slaughtergleam handheld loot/recharge runtime](0065-slaughtergleam-handheld-runtime.md): original table-loot and native equipment-drop bonuses, death payment, gold-nugget recharge and acquisition/art; shared systems and custom loot/death compatibility remain open.

- [Murdergleam handheld critical-hit runtime](0064-murdergleam-handheld-runtime.md): forced critical hits, original charge/toggle/recharge and acquisition/art; shared gem and combat compatibility remain open.

- [Mountaintear dropped lifecycle — approved and implemented](0063-mountaintear-dropped-item-proposal.md): native fire protection/lava recharge, preserving entity metadata/physics and sounding on actual recharge. Supersedes earlier dropped-recharge/fire-protection gaps.

- [Mountaintear ranged runtime — safety approved](0062-mountaintear-placement-safety-proposal.md): destination guards and success-only payment implemented with original acquisition/art; dropped recharge/fire resistance, Lightwell and shared systems remain open.

- [Stormway handheld runtime](0061-stormway-handheld-runtime.md): targeted lightning, rain recharge, retaliation, strike buffs/cooldown and original acquisition/art; shared gem systems and runtime/event compatibility remain open.

- [Phoenixway fire protection](0060-phoenixway-fire-protection.md): original held-gem resistance/payment and damage cancellation through native hooks; shared integration, sound and cross-loader compatibility remain open.

- [Phoenixway ranged runtime](0059-phoenixway-ranged-runtime.md): original fire placement, recharge and acquisition/art; automatic protection, sound and shared gem integrations remain open.

- [Elixirspindle handheld runtime — client scope approved](0058-elixirspindle-item-use-proposal.md): native reusable potion effects, original nether-wart recharge/acquisition/art, approved potion-only client handling; shared integrations and runtime acceptance remain open.

- [Switchgleam Enderman suppression/recharge](0057-switchgleam-enderman-recharge.md): original nearby held-gem recharge/cancellation without a toggle gate; broader event coverage/shared systems and runtime acceptance remain open.

- [Switchgleam handheld swap — clearance approved](0056-switchgleam-safe-swap-proposal.md): both placements validated before movement, original free use/targeting and acquisition/art; Enderman and shared gem integrations remain open.

- [Munchstone handheld runtime](0055-munchstone-handheld-runtime.md): configurable block eating, original charge/bonemeal rules and acquisition/art; shared gem systems, sound presentation and optional compatibility remain open.

- [Cleansegleam handheld runtime — recharge approved](0054-cleansegleam-recharge-proposal.md): native cleansing, original acquisition/art and approved one-milk full recharge with conserved empty bucket; shared/optional integrations and runtime acceptance remain open.

- [Orderstone runtime — client synchronization approved](0053-orderstone-runtime.md): original transformations/XP recharge and artwork, with approved client notification; costs and lack of direct neighbor notification preserved. Shared integrations and runtime acceptance remain open.

- [Mindspindle handheld runtime](0052-mindspindle-handheld-runtime.md): per-pickup XP amplification, native mending, book recharge and original acquisition/art; shared gem integrations and runtime mixin acceptance remain open.

- [Salvegleam handheld runtime](0051-salvegleam-handheld-runtime.md): healing pulses, animal-kill recharge, saved state and authoritative gem toggle controls with original acquisition/art; shared integrations and runtime acceptance remain open.

- [Agegleam handheld runtime](0050-agegleam-handheld-runtime.md): nearby baby growth, original charge quirks and held-only timed recharge, with original opt-in acquisition/artwork; shared gem systems and runtime acceptance remain open.

- [Rivertear handheld runtime](0049-rivertear-handheld-runtime.md): native water placement, dropped-water recharge and original opt-in acquisition/artwork; Lightwell, optional integrations and broader gem systems remain open. The pinned particle handler has no pendeloque beam implementation.

- [Parchtear runtime — representation approved](0048-parchtear-runtime.md): native clearing/recharge and original acquisition/assets; approved non-air temporary barrier preserves its original physical behavior/lifetime. Four-target assembly passed; shared gem integration and runtime acceptance remain open.

- [Radiant Key / Trove LOCK](0047-radiant-key-trove-lock.md) — original recipe/animation, optional upgrade installation/removal, saved empty-storage identity, native insertion filtering and transaction snapshots; item-form/network integration and runtime acceptance remain open.

- [Placed-storage VOID upgrades](0046-storage-void-upgrades.md) — Charm installation/removal, original optional GUI row, saved/packed state and native overflow behavior; restores the Trove automation slot needed for hopper admission. Other optional types, item-form APIs and runtime acceptance remain open.

- [Devouring Charm handheld runtime](0045-devouring-charm-runtime.md) — temporary disposal, native fluid emptying/returns, persistent pickup filters, original acquisition/artwork and server-owned controls; storage-upgrade/Parchtear integration and runtime acceptance remain open.

- [Radiant Crafting Table runtime](0044-radiant-crafting-table-runtime.md) — persistent grid, three saved recipes, native crafting returns, original acquisition/assets/GUI and configurable raw-quartz conversion; network/presentation/compatibility completion and gameplay acceptance remain open.

- [Radiant Amphora runtime](0042-radiant-amphora-runtime.md) — acquisition, saved linking/modes, native fluid world/Tank/dispenser paths and original base art; item-fluid compatibility and dynamic fluid presentation remain unfinished.
- [Amphora copied-item boundary](0043-amphora-copied-item-boundary.md) — source-traced simulation/preparation hazards forbid an unrestricted remote item proxy; required safe integration remains open.

- [Storage scepter interactions](0041-storage-scepter-interactions.md) — original acquisition/artwork; Revelation reports for Chest/Trove/Tank/Resonator and Manipulation capacity-menu access. Other devices/scepters and gameplay acceptance remain unfinished.

- [Native capacity upgrade menu](0040-capacity-upgrade-menu.md) — reachable Trove/Tank capacity insertion/removal and shift transfers, native access checks and original GUI assets; optional/scepter UI and gameplay acceptance remain unfinished.

- [Trove native storage](0036-trove-native-storage.md) — recipe, capacity, ordered size upgrades, transfers and automation; optional upgrades/scepters/item-form automation remain unfinished.
- [Tank native fluid storage](0037-tank-native-fluid-storage.md) — native units/identity, container interaction, capacity upgrades and automation; optional/item-form integrations remain unfinished.
- [Portable storage removal](0038-portable-storage-removal.md) — one packed saved-content item on native removal, not duplicate normal loot; runtime acceptance pending.
- [Tank fluid surface](0039-tank-fluid-surface.md) — native sprites/tint and original volume/UV intent; prevent inverted tiny-fluid geometry; visual acceptance pending.

- [Radiant Chest runtime](0035-radiant-chest-runtime.md) — extended native menu/count synchronization, native automation and approved remainder-only native chest conversion; scepter/network integration and gameplay acceptance remain open.
- [Radiant Resonator runtime](0034-resonator-runtime.md) — natural quartz generation, persisted owner/growth and unloaded placement accounting; full networks, sound configuration/loop and tome grant remain open.

- [Ordinary flint-and-steel returns](0033-gem-cutter-flint-returns.md) — copy-prepared single-use damage and final-use consumption inside paid crafting. Enchanted/custom tools, break effects and surplus drops remain unfinished.

- [Conserved Gem Cutter fluid returns](0032-gem-cutter-fluid-returns.md) — native single-container draining and table/player returns within guarded payment; insufficient capacity cancels rather than dropping surplus. Flint/generalized returns and connected-player acceptance remain open.

- [Native Gem Cutter recipes and live reload](0031-native-gem-cutter-recipes.md) — seven progression recipes moved to native data; server menus follow current manager definitions with conserved payment and removal/disable handling. Plain item/tag schema only; connected-player reload acceptance remains open.

- [Sliver smashing boundaries](0030-sliver-smashing-boundaries.md) — native server attack acquisition, access checks, failed-spawn refund and validated original settings; preserves actual upstream thresholds and free-single behavior. Connected-player/protection acceptance remains open.

- [Native table acquisition recipe](0029-table-acquisition-recipe.md) — repairs malformed pinned recipe JSON and adapts OreDictionary inputs to shared tags; connects original raw-quartz Dust/Shaped recipes. Natural quartz production/conversion remains unfinished.

- [Immediate paid Gem Cutter crafting](0028-immediate-paid-gem-cutter-crafting.md) — complete payment from table/main-player inputs before native cursor or shift delivery; one batch per click, unsupported remainders refused, old pending records preserved. Connected-player/restart acceptance remains open.

- [Server recipe previews](0026-server-recipe-previews.md) — live menu paging/selection and conserved table/main-player matching; ghost output cannot be granted. Selection is menu-local.
- [Gem Cutter ingredient tags](0027-gem-cutter-ingredient-tags.md) — original Inlay costs with live common-tag matching; paid crafting now uses these under 0028. Full recipe-definition reload remains unfinished.

- [Gem Cutter menu access](0025-gem-cutter-menu-access.md) — original slot layout and owned input transfers through a player-bound live-device menu; distance/removal validation replaces unconditional access. Crafting controls remain unfinished.

- [Gem Cutter runtime removal](0024-gem-cutter-runtime-removal.md) — native joint-state save/restart and paired removal; pending records remain packed, not delivered. Gameplay integration remains pending.

- [Recipe page boundaries](0023-recipe-page-boundaries.md) — preserves seven-entry wrapping, normalizes empty/stale navigation safely; menu integration remains pending.

- [Atomic catalog replacement](0022-atomic-catalog-replacement.md) — validate complete ordered batches before publication, preserving live recipes on invalid input; runtime reload binding remains pending.

- [Joint crafting state](0021-joint-crafting-state.md) — owns deducted inputs and one pending result, with joint serialization and atomic load rejection; runtime delivery remains pending.

- [Crafting result persistence](0020-crafting-result-persistence.md) — strict fresh-world output/consumed-input codec; runtime owner, delivery and restart acceptance pending.

- [Crafting result boundary](0019-crafting-result-boundary.md) — guarded input consumption returns detached creator-policy output and consumed inputs; persistence and output/remainder delivery remain pending.

- [Catalog write generation](0018-catalog-write-generation.md) — rejects intervening writes even when callbacks restore the original recipe object; input-only boundary, runtime crafting pending.

- [Catalog-bound input consumption](0017-catalog-input-commit.md) — resolves current recipe costs and rejects replacement/removal during commit; runtime crafting pending.

- [Input commit condition revalidation](0016-input-commit-condition-revalidation.md) — live condition gate before matching and final inventory validation; 93 tests per Fabric/NeoForge target. Runtime authoritative lookup and full crafting remain pending.

- [Recipe-owned creator output policy](0015-recipe-creator-policy.md) — recipe definitions select copy-safe creator stamping; 89 tests pass per Fabric/NeoForge target, Forge compilation only. Runtime crafting remains pending.

- [Ingredient predicate isolation](0014-ingredient-predicate-isolation.md) — implemented with red/green regressions; prevents candidate mutations leaking between requirements.

- [Shared stacked insertion](0013-shared-stacked-insertion.md) — implemented under delegated authority; ordered, data-aware remainder insertion and simulation verified, crafting delivery integration pending.

Current implementation and continuation context: [HANDOFF.md](../HANDOFF.md). The user's “approve all three” applies to external Patchouli subject to licensing review, optional Curios/Trinkets, and keeping dormant features documented/disabled. It is not a commit, push, release or blanket gameplay-change authorization. Raw-quartz conservation has separate prior approval.

The guidebook backend, optional wearable APIs and raw-quartz inventory-conservation correction are approved. The external Patchouli dependency/backend foundation is implemented and startup-tested; actual book content and the other approved implementations remain pending. Missing reachable content is unfinished migration, not a decision to remove it. Dormant/unregistered upstream features remain documented and disabled under the separately approved scope in `../migration/UNFINISHED_UPSTREAM_FEATURES.md`. Mechanical loader/API translation must preserve observable behavior.

Before any deviation, create one numbered Markdown file per change containing: status (proposed/approved/rejected/implemented), exact upstream source and behavior, proposed behavior, reason, affected targets, save/network compatibility impact, alternatives, decision authority and regression tests. Keep unrelated changes in separate files. Security fixes still require a documented decision; never reproduce unsafe behavior merely for parity. The user subsequently approved 0005 and delegated future behavior decisions: “approved, continue. You are free to make your own desicion for behaviour changes, just document them proreply”. Individual behavior-approval forms are no longer required. This does not authorize commits, pushes, remotes or publication.

Approved, partially implemented:
- [Guidebook backend](0001-guidebook-backend.md) — external Patchouli startup verified; content and interaction parity pending.

Approved, implementation pending:
- [Wearable integration](0002-wearable-integration.md) — optional Curios/Trinkets; incompatible slot semantics need separate approval.
- [Raw-quartz conversion inventory conservation](0003-raw-quartz-conversion-conservation.md)

Approved, implementation in progress:
- [Inventory-operation conservation](0004-inventory-operation-conservation.md) — prevent phantom upgrades, occupied-slot consumption and invalid transfer mutation. The user explicitly approved the repeated focused form; the earlier timeout is superseded.

Approved, implementation in progress:
- [Gem Cutter crafting conservation](0005-gem-cutter-crafting-conservation.md) — conserved matching for overlapping/repeated ingredients and no output from partial consumption. Explicit user approval supersedes the earlier form timeout.

Implemented under delegated behavior authority; runtime integration pending:
- [Recipe catalog snapshots](0012-recipe-catalog-snapshots.md) — ordered replacement/removal and data-aware output lookup with stable read-only views; lifecycle/menu binding pending.
- [Recipe definition ownership](0011-recipe-definition-ownership.md) — typed Gem Cutter identity, copied output/list and conserved preview matching; registration/conditions/effects remain pending.
- [Creator output copy](0010-creator-output-copy.md) — preserves original creator fields across NBT/components without mutating recipe templates; recipe/letter integration pending.
- [Gem Cutter owned input boundary](0009-gem-cutter-input-boundary.md) — ordinary 18-slot input capacity, validated direct writes/loads, detached reads and atomic consumption under 0005; full crafting effects and block integration remain pending.
- [Dimension-aware position identity](0008-dimension-position-identity.md) — immutable position keys and strict fresh-world dimension-key persistence; network consumers and world-resolution checks remain pending.
- [Optional-upgrade write validation](0007-optional-upgrade-write-validation.md) — preserves three unique single-item optional upgrades, rejects malformed direct writes and atomically rejects invalid saved inventories. Concrete item/device bindings remain pending.
- [Size-upgrade removal capacity](0006-size-upgrade-removal-capacity.md) — checks the capacity remaining after removal, including the first upgrade. Shared-handler regressions pass on both Fabric versions and NeoForge; actual Tank/Trove consumers and Forge Minecraft test execution remain pending.
