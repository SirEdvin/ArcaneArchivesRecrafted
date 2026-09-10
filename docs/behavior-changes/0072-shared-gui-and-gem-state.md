# 0072 — Shared GUI preference and gem state presentation

Status: implemented and scoped-verified; not full migration or gameplay acceptance.

## Source and original behavior

Pinned release `bb99accf48ed583e29b0efae56e28c963407b8df`, restored locally at `/tmp/arcane-archives-reference/migration-source`:

- `config/ConfigHandler.java` defaults `UsePrettyGUIs` to true. Existing Chest, Crafting Table, Gem Cutter, storage upgrade, Devouring Charm and Gem Socket screens select their original alternate textures. Socket simple geometry omits the strap and uses different offhand/socket frames. The gem HUD does not use this preference.
- `items/gems/GemUtil.java` defines upgrade bits 1/2/4/8; only POWER changes maximum charge. No call to either upgrade setter exists in the pinned source. No new upgrade-acquisition recipe, menu or matter/space/time effect is authorized by this port.
- Unlimited-charge presentation means maximum charge zero, or an existing `infinite` key plus true `infinity`. The two differently spelled keys are an upstream quirk, not a typo to silently repair. This check affects the tooltip and depleted model; consumption and many action guards still check numeric charge directly.
- `ArcaneGemItem.getTooltipData` shows unlimited state and colored upgrade names. Original translations are retained.

## Adaptation and scope

All four supported targets. Restore client-local `UsePrettyGUIs` in `arcanearchives/client.properties`, loaded only from native client initialization; true by default, restart to apply. Invalid files fail without replacement. Existing menu geometry, actions and server authority remain unchanged. No settings packet, gameplay balance change or invented GUI toggle is added.

Preserve the existing non-mutating item reads and validated charge bounds. Restore the source-backed upgrade/unlimited tooltip and model selection without making charge consumption free or introducing dormant upgrade gameplay. Existing approved safety corrections remain in force, as explicitly reconfirmed by the user.

## Verification

Four-target `assemble` plus focused tests passed in `build/shared-gui-state-final-20260909-164113.log`, exit 0, 20s. Exact command: `timeout --foreground 10m ./gradlew assemble :1.20.1-fabric:test --tests '*ClientConfigTest' :1.21.1-fabric:test --tests '*ClientConfigTest' :1.21.1-neoforge:test --tests '*ClientConfigTest' --tests '*ArcaneGemStateTest' :1.20.1-forge:test --tests '*ClientConfigTest' --no-daemon`. Three configuration fixtures pass on each target; three registered-gem state fixtures pass on loader-aware NeoForge, zero failures/errors/skips. XML aggregation: `build/shared-gui-state-verification.json`. This is not Forge Minecraft-dependent coverage or a full test-suite pass.

Initial fixtures incorrectly constructed new unregistered items after vanilla bootstrap; Fabric rejected intrusive-holder creation before assertions (`build/shared-gui-state-20260909-163958.log`, exit 1, 29s). The fixture now uses the actual registered Agegleam in the existing loader-aware NeoForge test setup, not a fake registry, reflection workaround or production fallback. Equivalent Fabric/Forge loader-aware state execution remains unverified.

`python3 scripts/verify_artifacts.py` passes all four production/source pairs (`build/shared-gui-artifacts.log`), including new classes and unchanged GUI artwork. The first verifier run identified the new classes missing from its explicit inventory; expectations were updated, not loosened. Visual/input and full gameplay acceptance remain deferred until implementation is complete. Assets are copied unchanged from the MIT-licensed pinned release with retained notices; `scripts/port_gui_assets.py` rejects differing existing files.
