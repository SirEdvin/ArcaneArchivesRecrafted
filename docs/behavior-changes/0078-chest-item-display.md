# 0078 — Radiant Chest item display

Status: implemented; four-target assembly/package and focused NeoForge state fixtures passed. Connected-client acceptance remains open.

## Original behavior

Release bb99accf48ed583e29b0efae56e28c963407b8df, blocks/RadiantChest.java:66–105: with a Manipulation Scepter in either hand and a nonempty other hand, copy that other stack as a display on the clicked face. Sneaking with a scepter and the other hand empty clears the display; a lone scepter without sneaking opens the normal chest. The item is not consumed. Revelation's onItemUseFirst inspection takes precedence when that scepter handles the interaction.

RadiantChestTileEntity.java:25–26,38–55,147–153,185–215 saves displayStack and displayFacing. Clearing retains the facing. RadiantChestTESR.java:16–38,59–86 renders only horizontal faces, at Y=0.435 and X/Z face offsets 0.03/0.97 (other coordinate 0.5), with scale 0.6, FIXED item transforms and rotations NORTH=0, SOUTH=180, EAST=270, WEST=90. UP/DOWN are stored but intentionally not rendered. The hard-disabled tracking overlay is not reachable behavior.

## Port and approval

Restore interaction, copied display state, persistence, native block-entity synchronization and client renderer together on 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Reuse existing native block/item use dispatch, with item-use fallback for sneaking, rather than introduce a client-supplied item-stack packet. Resolve actual held items on the server, preserve the original hand-selection order, and validate live storage, dimension/distance, spectator and mayInteract restrictions before mutation. Returned display stacks are defensive copies, are not inventory slots, and must never be dropped or offered through automation. Only visual state is sent to tracking clients, not the chest inventory.

Approval: original reachable behavior restoration within standing scope, plus required server-authority/conservation boundaries. No new item consumption, horizontal-only placement restriction, automatic display selection, tracking glow, ownership policy or acquisition is introduced. Native modern renderer lighting replaces obsolete global OpenGL state management; geometry/transforms remain source-backed.

## Verification

Command: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*RadiantChestDisplayTest' --no-daemon`.

Exit 0, 28 seconds; `build/chest-display-20260909-172802.log`. All four supported targets assembled. NeoForge XML reports 3 tests, zero failures/errors/skips: defensive input/output copies without inventory insertion; save/load round trips of all six faces and item components alongside real inventory/name/owner state; visual tags omit inventory/owner; clearing a previously populated client state and defaulting missing fields. These are serialization/state fixtures, not a world save/restart or connected-player test.

`timeout --foreground 60s python3 scripts/verify_artifacts.py` returned 0; `build/chest-display-artifacts.log`. All four production/source artifact pairs passed. `git diff --check` passed. Existing upstream MIT notice remains packaged.

Still required: visible item/block/glint rendering on each horizontal face; both-hand and sneak dispatch; Revelation precedence; protection/spectator rejection; initial chunk tracking and live updates with two players; world save/restart; breaking a chest must drop actual inventory but never its display copy. No connected client was launched for this increment. This does not complete Chest, storage, network or full multiplayer/rendering/save-restart acceptance.
