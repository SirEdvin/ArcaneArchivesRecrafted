# Native gem HUD

## Original behavior / source

Pinned bb99accf48ed583e29b0efae56e28c963407b8df EventHandler.renderGemHUD and client/render/RenderGemcasting.java were inspected. The handler draws every available gem after the HUD: main hand on the right, offhand on the left, socket below/right of the crosshair. It uses textures/gui/fabrial.png, five cut silhouettes, colored charge bars and toggle checkboxes. Main hand remains right regardless of the player's preferred arm. The simple texture constant exists but is not used by the pinned drawing method.

## Implemented behavior

GemHud uses native GuiGraphics with the original sprite rectangles, positioning, mirrored offhand silhouette/bar and socket offset. Twenty-pixel charge fills retain the minimum one-pixel indicator for positive charge. Toggle indicators use the existing synchronized toggle value and original color strips. Fifteen migrated gems use their corresponding cut silhouettes; color selection reuses the recharge color mapping rather than maintaining another color table.

AvailableGems supplies held/open-menu/worn selection, preserving one socket display and no ordinary closed-inventory socket activation. Client reads are non-mutating. Registration is client-only through ArsenalClient: Fabric HudRenderCallback and Forge/NeoForge RenderGuiEvent.Post. The native hidden-HUD option suppresses drawing. Blend handling uses native RenderSystem with cleanup; no extra shader/dependency or substituted artwork.

## Approval / scope

Original implemented presentation restored with native APIs; no layout redesign proposed. The unused upstream simple-HUD constant does not justify inventing a simple-style behavior. Shared GUI style settings, upgrade mechanics/unlimited semantics and broader migration remain separate. Other-mod overlay ordering, scaled displays and visual acceptance are deferred, not claimed complete.

## Verification

`timeout --foreground 10m ./gradlew assemble --no-daemon`: exit 0, 20s; `build/gem-hud-final-20260909-144424.log`, all four compileJava leaves, Gradle success in 19s. `git diff --check` passed. All four production JARs contain GemHud and its client initializer reference. Packaged fabrial.png compared byte-for-byte with the pinned upstream asset. Artifact expectations extended; full verifier not run. No client launch, screenshot, gameplay test campaign, optimization, commit or publication. Migration is not first-playtest-ready.
