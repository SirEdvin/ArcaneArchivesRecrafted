# 0025 — Gem Cutter menu access validation

Status: implemented under the user's delegated behavior-decision authority; full crafting remains unfinished.

Upstream: `release/0.2.0.25-mixins8`, `bb99accf48ed583e29b0efae56e28c963407b8df`, `inventory/ContainerGemCuttersTable.java`, `canInteractWith`, returns true unconditionally. `blocks/GemCuttersTable.java` redirects accessor interaction to the master and opens its GUI. The container defines output 0, player 1–36, table inputs 37–54 and seven recipe-display slots thereafter.

New behavior: a menu belongs to its opening player and requires the same live master block entity, matching accessor, same level, and distance at most eight blocks from the master. Invalid/stale menu interactions cannot transfer items. Standard Minecraft menu synchronization and clicks are used instead of accepting client-supplied item contents or block positions through custom packets. Recipe/output positions remain noninteractive until actual authoritative crafting is integrated; this is unfinished implementation, not removal of upstream crafting.

Rationale: reject stale/remote device access and writes after paired removal without making the private crafting state mutable. Native Slot.safeInsert writes back detached destination stacks; vanilla moveItemStackTo's in-place merge is unsuitable for the owned input boundary.

Targets: all four supported leaves. Fresh-world NBT is unchanged; a new registered menu uses native window/slot synchronization. No legacy network compatibility is promised. No costs, capacities, textures, recipe rules or ownership system are changed.

Alternatives: reproducing unconditional access; exposing the owned stacks to satisfy vanilla in-place mutation; custom slot-network packets. Rejected as unsafe or unnecessary.

Verification: four menu regressions cover release slot order/coordinates, unavailable display slots, detached-stack shift-click merges, reverse player insertion, partial/full capacity, native nonstackable limits, invalid indices and invalidated input ownership. The complete executable suites pass 141 tests per Fabric/NeoForge leaf with zero failures/errors/skips; Forge compiles test sources only. Matrix exit 0 in 31s, `build/gem-cutter-menu-tests-20260908-094007.log`. All four dedicated-server startup/reload/save/stop checks and packaged artifact/resource checks pass; consolidated report `build/gem-cutter-menu-verification.json`.

Before the user directed deferring visual validation, the 1.21.1 Fabric client exercised actual survival-mode shift-deposit of ten diamonds, right-click splitting, return to the last player hotbar slot, merging back into the owned table input, and accessor reopening. Server NBT inspection confirmed exactly ten diamonds in the master. A scheduled server teleport while the menu was open closed it automatically beyond the distance boundary. The client then saved and stopped normally (`build/model-visual-client-1788860451196721632.log`, markers `AA_GCT_MENU_DEPOSIT` and `AA_GCT_MENU_DISTANCE`). This is not separate-process persistence acceptance for these menu transfers; the copied NeoForge test fixture was not launched.

The server uses a private menu-only `Container` adapter, not `Container` on the block entity itself: vanilla automation must not discover the detached-stack interface and perform incompatible in-place merges. Native click synchronization is shared across loaders; no custom networking or public pending-result access was introduced.

This work does not establish actual crafting, recipe pagination/selection, tool/fluid remainders, output delivery or full multiplayer acceptance. Further visual checks are deferred until non-visual migration implementation is complete. The original 254-high layout also clips at the small development window's automatic GUI scale; small-window accommodation, the optional simple GUI setting, narration and broader presentation acceptance remain unfinished, not silently waived.
