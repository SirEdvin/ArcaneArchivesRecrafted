# Gem Cutter small-window layout

## Original behavior and current evidence

At pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`, `client/gui/GUIGemCuttersTable.java:59–65` sets a fixed `206 × 254` container layout. Its superclass `client/gui/AbstractGuiContainerTracking.java` extends vanilla `GuiContainer` and only adds slot highlighting, not adaptive sizing. This is static source evidence, not a legacy-client reproduction.

The current `client/GemCuttersTableScreen.java` retains those dimensions. The real Fabric 1.21.1 check documented in [GEM_CUTTER_CLIENT_ACCEPTANCE.md](../migration/GEM_CUTTER_CLIENT_ACCEPTANCE.md) showed background clipping in an 854×480 window at the native automatic GUI scale. The same screen fit at 1280×800. The new Craft button, recipe preview and saved output worked at the tested larger size; no duplication or failed crafting was found.

## Proposed behavior — not adopted

Fit the Gem Cutter screen down only when its normal layout would extend outside the available screen. Preserve the existing layout at sizes where it fits. Keep the original artwork proportions, logical slot layout, recipe selection, explicit crafting and server-owned inventory behavior.

Apply any visual scale consistently to pointer hit-testing, drag/split interactions, tooltips and buttons. Retain keyboard controls and narration. Do not change or save the user's global GUI-scale preference, change other screens automatically, suppress native tutorial overlays, or add a UI framework/dependency.

Implementation details still require tracing the pinned native screen/input APIs; no scaling mechanism is implemented or claimed verified by this proposal.

## Reason

Avoid clipping at a normal small development-client window size without redesigning the inventory or changing crafting semantics.

## Selected behavior

Retain upstream's fixed-size layout and explicitly exclude undersized/high-GUI-scale presentation from migration acceptance. Players can enlarge their window or lower Minecraft's GUI scale. This is an acceptance decision, not a repair of the observed clipping.

## Affected targets

Gem Cutter screen on Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge, including pretty and simple artwork modes. Other tall mod screens are separate scope.

## Approval status

The user explicitly selected “retain upstream sizing.” Keep the existing fixed-size layout; adaptive fit-down is not adopted. Undersized/high-GUI-scale clipping is excluded from migration acceptance, not repaired. No production change is needed and this decision is no longer a blocker. Broader migration and other client acceptance remain incomplete.

## Historical verification plan for the unadopted proposal

The following resizing-specific checks are not pending implementation requirements. Existing inventory/crafting and ordinary client acceptance remain applicable independently.

- All four leaf builds and artifact contracts.
- Real-client checks at the previously clipped and previously fitting window sizes.
- Correct output/preview/button geometry, hover, pickup, shift-click, drag/split and keyboard behavior after resize; no inventory mutation caused by resizing.
- Both artwork modes, unchanged global GUI preference, and normal close/reopen behavior.
- Keep existing payment, output, two-viewer and native synchronization regressions green.
