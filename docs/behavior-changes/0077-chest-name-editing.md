# 0077 — Radiant Chest name editing

Status: implemented and four-target assembly/package verified; runtime acceptance pending.

## Original behavior

Release bb99accf48ed583e29b0efae56e28c963407b8df: GUIRadiantChest.java:93–96,118–130,742–754 places a borderless name field at (53,238), size 88×10, and sends changed names immediately. RightClickTextField.java:15–23 clears on right click. PacketRadiantChest.SetName/UnsetName update the stored name and notify clients. The field uses vanilla GuiTextField's default 32-character limit. The port already saved chestName and exposed it through Revelation, but had no editing control.

## Port behavior

Restore that field and clearing action in the existing pretty/simple screen, using native EditBox input/narration. Typing in the field does not invoke inventory hotkeys. Escape and Tab retain native close/focus navigation. English and Portuguese gain a narration label; this is new accessibility text, not claimed as copied original text.

The bounded C2S ChestName request identifies only a container ID and a name. The server resolves its own sender's open RadiantChestMenu and checks matching ID, live server-thread storage, menu identity, distance, dimension, removal/drop state, spectator status and mayInteract before changing the name. Names longer than 32 UTF-16 units or containing characters rejected by native text filtering are not accepted. Decoding is length-bounded too. Clearing uses the same path with an empty string.

The existing chestName persistence and Revelation report are reused. Open menus synchronize names through 32 native 16-bit data slots, one UTF-16 code unit each, with zero padding after a shortened/cleared name. There is no custom S2C transport or client-supplied block position/owner identity. Server-origin field refreshes do not send rename requests; focused local drafts are not overwritten during typing. Other open menus refresh through their normal server broadcast cycle. This is menu synchronization, not the still-unimplemented Manifest/world-display tracking integration.

Approval: source behavior restoration within standing scope, retaining required server authority and trust-boundary validation. No new naming gameplay, acquisition, routing or ownership policy. Brazier routing controls and chest display-stack/facing behavior remain separate unfinished integrations; this increment does not complete the Chest or storage milestone.

## Verification

Command: timeout --foreground 10m ./gradlew assemble --no-daemon

Exit 0, 22 seconds; build/chest-name-20260909-171717.log. All four supported targets assembled. Artifact checker: timeout --foreground 60s python3 scripts/verify_artifacts.py, exit 0; build/chest-name-artifacts.log. git diff --check passed.

Mapped APIs were inspected before use: text filtering is SharedConstants.filterText on 1.20.1 and StringUtil.filterText on 1.21.1. The C2S registration follows the existing loader-specific payload/channel conventions and the new payload is required by the artifact inventory.

No broad test campaign or connected-client run was performed. Final acceptance must cover typing/paste/right-click clearing, keyboard focus/resize, Unicode and length bounds, two viewers, stale-menu/protected/spectator rejection, save/restart/reopen, and Revelation/remaining network consumers. Assembly is not evidence those runtime cases passed.
