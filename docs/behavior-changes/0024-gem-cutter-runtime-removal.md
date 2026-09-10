# 0024 — Gem Cutter runtime removal and pending-state retention

Status: implemented under the user's delegated behavior-decision authority; runtime lifecycle checkpoint only, not a playable Gem Cutter.

## Source and preserved behavior

Upstream repository: https://github.com/AranaiRa/ArcaneArchives; release/0.2.0.25-mixins8 at `bb99accf48ed583e29b0efae56e28c963407b8df`.

`blocks/GemCuttersTable.java` defines a two-part, hardness-3, fully lit device. Only the non-accessor owns inventory. `getConnectedPos`, `neighborChanged` and `breakBlock` connect the halves and drop table inputs from the master. `blocks/templates/BlockDirectionalTemplate.java` uses placer yaw minus 90 degrees; `BlockTemplate.calculateAccessors` puts the second block opposite this facing. `tileentities/GemCuttersTableTileEntity.java` saves inputs and recipe index; it does not own the modern joint pending-result record introduced by 0021.

The new `GemCuttersTable`/`GemCuttersTableBlockEntity` registration connects the existing joint owner to native block-entity save/load. Input transfer methods enforce the live server thread and mark real changes dirty; reads remain detached. The owner decodes candidate crafting state before replacement. Item placement code reserves the adjacent replaceable position and creates the accessor; either-half removal cleans up the matching opposite half. Piston movement is blocked to avoid separating the pair. Horizontal orientation is represented directly rather than porting unused vertical metadata values.

## Removal policy and rationale

- With no pending craft, master removal drops stored inputs and one ordinary table item.
- With a pending craft, master removal instead drops one table item carrying the complete joint block-entity data. Inputs, paid output and original consumed records stay together; no loose outputs or consumed-input refunds are generated.
- Removal is guarded once per block entity; the accessor does not own or independently drop contents. The block has no loot table, avoiding a second table drop from the normal loot path. This means master removal also drops the table under administrative replacement and creative breaking; it is not an exact copy of survival-only vanilla loot behavior.
- Drops follow the vanilla `doTileDrops` rule and ordinary item-entity destruction/despawn behavior. This is not a durable cross-world transaction or protection against explicit administrative deletion.

Packing a pending record is a conservation adaptation, not a new ability inferred from upstream. Alternatives rejected: discard a paid result, refund consumed items while retaining output, independently spawn output before tool/container remainder handling, or expose unconditional clear/take. Normal runtime crafting and result delivery remain unwired. Native item data is used for the packed record; actual item re-placement and creative/loot edge cases still require acceptance tests before gameplay completion.

All four supported targets. Fresh-world block ID/entity ID `arcanearchives:gemcutters_table`; a `Crafting` compound contains the unchanged version-1 joint owner data. No legacy importer or cross-version save conversion. No dependency, recipe cost, invented art or public network protocol change.

## Verification and limits

Final verification after explicitly restricting removal/drop handling to the old master state and removing its block entity: the same scoped matrix below passed in 27s, exit 0, `build/gem-cutter-runtime-final-20260908-084838.log`. All eight setup/restart server runs were repeated and passed; exact log paths, durations and reread checks are consolidated in ignored `build/gem-cutter-runtime-verification.json`. XML still reports 137 passing tests per Fabric/NeoForge leaf, Forge compilation only; four artifact pairs pass. No project Java processes remain. Earlier log pairs below retain the initial successful checkpoint evidence.

Scoped matrix: `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon`, exit 0, 29s, `build/gem-cutter-runtime-20260908-083730.log`. Existing 137 tests pass on each executable Fabric/NeoForge leaf with zero failures/errors/skips; Forge test compilation only. Four production/source artifact pairs and whitespace checks pass.

`scripts/smoke_gem_cutter.py` reuses the bounded server runner and requires existing operator-accepted EULAs and loopback development servers. It constructs both halves with commands, injects ordinary/pending records, flushes/saves/stops, then starts a separate server process and verifies inputs, pending output and consumed records. Removing an accessor removes its master and drops ordinary inputs; removing a pending master removes its accessor and retains inputs/output in item NBT/components without loose ingredient/output stacks. This is actual block-entity restart coverage, but pending records are injected, not crafted.

Passing setup/restart log pairs (all exit 0; JSON sidecars reread, all checks true):

- 1.20.1 Fabric: `build/smoke-server-1.20.1-fabric-20260908-084324-617333.log` (24.55s), `build/smoke-server-1.20.1-fabric-20260908-084349-162984.log` (25.05s).
- 1.20.1 Forge: `build/smoke-server-1.20.1-forge-20260908-084414-213007.log` (27.81s), `build/smoke-server-1.20.1-forge-20260908-084442-024498.log` (33.06s).
- 1.21.1 Fabric: `build/smoke-server-1.21.1-fabric-20260908-084216-430946.log` (20.8s), `build/smoke-server-1.21.1-fabric-20260908-084237-227477.log` (21.04s).
- 1.21.1 NeoForge: `build/smoke-server-1.21.1-neoforge-20260908-084515-083260.log` (21.29s), `build/smoke-server-1.21.1-neoforge-20260908-084536-374259.log` (21.55s).

The initial Fabric fixture issued all commands in one server tick and failed entity-drop observations. Spacing commands across ticks and providing a floor produced passing checks without changing production code. Do not treat the first failed observation as successful verification. Fixtures/platform blocks are cleaned up and force loading released by the restart phase; all servers stop cleanly.

Unfinished: native player placement/re-placement acceptance, complete drop multiplicity/explosion tests, exact original OBJ rendering and collision presentation, menu/screen, selected recipe persistence, combined table/player crafting, registered recipe catalog/reload, access checks, immanence/network links, tool/fluid remainders, and normal output delivery. The item is intentionally not advertised in the creative tab and has no acquisition recipe yet. Its model is not wired; no substitute texture/model was invented. No gameplay parent checkbox is complete.
