# Brazier fire rendering contract

Status: renderer and unlit shader connected; all four shader-loading startups pass. Placed visual acceptance remains open.

## Original behavior

Pinned upstream `bb99accf48ed583e29b0efae56e28c963407b8df`, `client/render/BrazierTESR.java:16–35`, disables GL lighting, draws the fire mesh with brightness/color 1, then restores the previous lighting state. The mesh is rendered at the block origin. This is not merely a maximum lightmap setting.

## Current port and identified gap

The mismatch described below is now corrected in production: `BrazierRenderType` uses an independently authored vertex shader with unchanged vertex color and constant white lightmap contribution, paired with the native entity-cutout fragment stage. Fog expressions and the older `IViewRotMat` uniform are selected during resource processing. Native Fabric/Forge/NeoForge shader reload hooks replace the owned shader reference. No translucent/emissive preset is substituted. The historical diagnosis below explains why this shader is needed.

`client/BrazierRenderer.java` resolves the standalone baked fire model each frame and renders with `RenderType.entityCutoutNoCull`, maximum packed light, and white vertex color. Loader renderer registration and graphical startup are verified; placed drawing has not been accepted.

Inspection of the actual cached Minecraft 1.21.1 client resource `assets/minecraft/shaders/core/rendertype_entity_cutout_no_cull.vsh` shows:

    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);

The shader independently samples the lightmap. Therefore `LightTexture.FULL_BRIGHT` does not remove directional shading. This is a known unresolved implementation mismatch, not proof of a visually reproduced defect.

Do not blindly switch to `entityTranslucentEmissive`: its 1.21.1 vertex shader also calls `minecraft_mix_light`, and its native render type changes blending and depth-write policy. Likewise, `entityAlpha` is unlit but compares texture alpha against vertex alpha and lacks the same fog processing; it is not a demonstrated drop-in replacement.

## Remaining acceptance

### Version-specific shader interface audit

Both supported native client shader interfaces have now been inspected directly from their cached `minecraft-client.jar` resources. Do not reuse a 1.21.1 vertex shader verbatim on 1.20.1:

- 1.20.1 entity-cutout vertex processing declares `IViewRotMat` and computes `fog_distance(ModelViewMat, IViewRotMat * Position, FogShape)`.
- 1.21.1 computes `fog_distance(Position, FogShape)` and no longer declares that matrix in this shader.
- Both independently apply `minecraft_mix_light` to vertex color, so maximum packed light is insufficient on either line.
- Preserve each version's existing fragment-stage alpha discard, overlay and fog behavior rather than switching to a differently blended/depth-writing native render type solely because its name says emissive.

The bounded implementation direction is an independently authored unlit vertex stage retaining the version-correct interface, paired with the appropriate fragment stage and an otherwise equivalent cutout/no-cull render state. Register it through each loader's native resource-reload shader hooks. These hooks and actual compilation still need verification; no shader files or registration have been added by this audit.

- Select or implement an unlit pipeline preserving the intended texture alpha/depth/culling behavior and appropriate fog behavior; inspect both supported Minecraft lines before changing it.
- Verify real placed fire drawing on all four targets, including orientation-dependent brightness, transparent texture regions, occlusion and resource reload.
- Keep model/startup verification distinct from renderer-output verification. No custom shader, dependency or rendering deviation was introduced by this audit.

Evidence: pinned upstream source, actual 1.21.1 client shader resources and resolved NeoForge `RenderType` source. Existing build/startup checkpoint remains `build/brazier-fire-renderer-20260912-174305.log` / `build/brazier-fire-renderer-clients-20260912-174500.log`; this documentation-only audit did not rerun Gradle or clients.

## Shader implementation verification

Initial build `build/brazier-unlit-20260912-175602.log` failed (exit 1, 112s) because a Fabric resource-copy callback captured the Gradle project through the Stonecutter extension. Moving the fog expression into the already captured properties map fixed configuration-cache serialization without disabling the cache.

Final four-leaf build `build/brazier-unlit-final-20260912-175826.log`: exit 0, 63s, all four native suites pass and configuration cache stored. `build/brazier-unlit-artifacts.log`: artifact verifier exit 0, checking shader stages, no directional-light operation, white lightmap, version-correct fog/interface and renderer class on all targets.

`build/brazier-unlit-clients-20260912-175938.log`: all four graphical startups pass and close, exit 0, 95s combined. No shader-loading errors or missing-uniform warnings in inspected logs; all four still report SoundSystem startup failure. This exercises initial native shader loading, not placed draw output or an explicit second resource reload. Visual/fog/occlusion comparison and audible acceptance remain open.
