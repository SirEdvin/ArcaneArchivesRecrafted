# Switchgleam handheld swap — destination clearance approved

## Original behavior

Pinned source: bb99accf48ed583e29b0efae56e28c963407b8df, items/gems/asscher/SwitchgleamItem.java and util/RayTracingUtils.java. The item traces forty blocks, skips block-only results, and swaps with the first returned entity if it is living. Entity results are not sorted by distance and are not clipped to the first block hit. Do not silently substitute nearest-visible targeting.

The item has charge capacities 30/150, but this action neither requires nor consumes charge. It sets both positions directly, retaining pitch and adding Math.PI to degree-valued yaw. No destination-clearance check occurs. Differently sized entities can consequently overlap solid blocks after the swap. This is source-based risk analysis, not a reproduced runtime failure.

## Approved change

Before moving either participant, validate destination clearance for both entities using their respective destination bounding boxes. Cancel the entire swap if either cannot fit; do not move one participant first and only then discover the other cannot move. Retain server authority, live-entity and permission validation, and avoid loading remote chunks as part of validation. Preserve original free-use behavior. Target selection and angle semantics are separate from this proposal.

Rationale: prevent unsafe placements during position exchange. Affected targets: Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval and evidence

Explicit user approval: "yes i approve". The earlier timed-out clarification is superseded. Implemented shared SwitchgleamItem with original free use, forty-block encounter-order ray selection, non-occluded targeting and original Math.PI yaw increment. Both destination block-collision boxes, build height, world border and loaded chunks are checked before either move. The departing participant is not treated as an obstacle. Mounted/passenger cases are rejected rather than moving unvalidated passenger groups. Server-player movement uses the native connection teleport so player clients receive the new position.

Registration, Arsenal-gated recipe/creative entry, original purple/dun asscher animated/accessibility models and translations are connected. Recipe: shaped quartz, four purple dyes, tripwire hook and ender pearl. Pinned assets use the existing MIT-notice-preserving recovery helper. Local mapped native signatures were inspected for movement and block collision APIs.

Verification: ./gradlew assemble --no-daemon, exit 0, 20s, build/switchgleam-approved-20260909-082109.log. All four compileJava tasks and assembly passed; git diff --check passed. Runtime class, recipe, model and animation inspected in every production JAR. Artifact class expectations updated; full verifier not run.

This completes the approved handheld swap slice, not the entire gem family. Enderman teleport suppression/recharge from the original event handler, shared socket/powder/HUD integration and runtime acceptance remain unfinished. Original tooltip references to Endermen are not evidence of migrated behavior. No gameplay tests, optimization, client/server launch, commits or publication. Migration remains incomplete and not ready for first playtest.
