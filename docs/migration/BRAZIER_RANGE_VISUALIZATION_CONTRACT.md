# Brazier range visualization contract

Status: pinned-source audit; renderer and visualizer control not yet implemented.

Baseline: `bb99accf48ed583e29b0efae56e28c963407b8df`.

## Source and licensing evidence

- `com/aranaira/arcanearchives/tileentities/BrazierTileEntity.java:349–374` owns transient client visibility, creates the range particle on enabling, and supplies bounds.
- `com/aranaira/enderio/base/render/ranged/RangeParticle.java:18–108` defines lifetime, identity checks, animation and drawing.
- `com/aranaira/enderio/core/client/render/BoundingBox.java:43–44,328–329` constructs a unit block box and expands both minima and maxima symmetrically. Do not substitute modern directional `AABB.expandTowards` for this operation.
- Preserved upstream `docs/upstream/CREDITS.md`, section “Ender Core & Ender IO”, attributes the range-particle code and records permission/public-domain compatibility. Retain attribution; no additional EnderIO dependency is needed merely to port this behavior.

## Required behavior

- Visibility belongs to a client tile instance, not server routing state or player saved data. Enabling creates a visual effect; disabling ends it. Closing the menu does not itself disable visibility.
- Each effect retains its original tile identity. It is alive only while the owner has a world, is not invalid, remains enabled, and is still the tile at that position. A same-position replacement must not inherit the old effect.
- The effect's age limit is upstream `20 * 60 * 10` ticks. Its update clears visibility when the effect is no longer alive or has expired, then advances age. Do not substitute wall-clock expiration.
- Bounds start with the unit block box and expand symmetrically by `(radius, 255, radius)`. The fixed vertical extent is relative to the device, not the world's build-height limits. Preserve this separately from horizontal-only routing; changing it requires a behavior proposal.
- Rendering scales each bound from the device's block center using `min((age + partialTicks) / 20, 1)`, then expands every face by `0.01`.
- The default RGBA is `(0.78, 0.54, 0.19, 0.4)`. The original uses textured bounding-box faces, alpha blending, disabled culling and depth writes, and full-bright lighting. A line-only box is not equivalent. Audit the face-emission helper before implementing modern vertices/render state.
- The effect reads current bounds on each draw, allowing radius updates while visible. Range visibility is not a persistent routing preference.
- GUI control: local toggle at `(6,6)`, size `14×14`; eye artwork at `(7,8)`, size `12×10`, using open UV `(222,0)` and closed UV `(210,0)`. Labels/tooltips distinguish show/hide. It sends no server mutation packet.

## Integration and verification still required

The current client menu has synchronized settings but no device position/reference. Supply a server-bound device identity to the client without trusting a client-provided position for configuration writes. Keep rendering client-only, avoid forced chunk loading, and clean up on world/connection changes.

Verify native client lifecycle, lifetime and animated bounds; replacement/unload behavior; current radius updates; menu-close persistence; resource reload and render-state restoration; actual translucent geometry and eye control at supported GUI scales on all four targets. Compilation or native server menu tests do not establish those rendering outcomes.
