# Rivertear handheld runtime

## Original behavior

Pinned upstream `bb99accf48ed583e29b0efae56e28c963407b8df`: `items/gems/pendeloque/RivertearItem.java`, `ArcaneGemItem`, `GemUtil`, `RecipeLibrary`, and original blue pendeloque models/textures.

Rivertear has 25 charge (100 with the power-upgrade bit). Right-click traces 40 blocks without stopping at fluid and places source water adjacent to the hit face; creative players do not pay charge, but an empty gem cannot cast. A dropped Rivertear touching water refills completely and plays the enchantment-table sound at volume 1/pitch 0.5, skipping the rest of that item update after recharge. Acquisition is opt-in with Arsenal: shaped quartz, four sugar, one water bucket and four blue dye ingredients.

## Migrated implementation

`RivertearItem` implements server-owned water placement and dropped-item recharge, reusing native gem charge serialization. Forge/NeoForge use their `onEntityItemUpdate(ItemStack, ItemEntity)` hook. Fabric uses a cancellable ItemEntity tick-head mixin. Recharge publishes a changed entity stack so its restored data can synchronize; client ticks never mutate charge. Loaded-chunk/interaction permission and replaceability/block-entity guards prevent remote chunk loading or destroying stored contents. Failed placement does not consume charge.

Original animated blue, depleted and accessibility models/textures are recovered through the existing dependency-aware asset copier, with atlas entries and native item predicates. Original translations are retained. The original tooltip's Lightwell description does not mean Lightwell integration is implemented. Recipe and creative visibility preserve `EnableArsenal=false` by default; enable it in `config/arcanearchives/arsenal.properties` and restart to expose Arsenal acquisition. No live user config was changed.

Affected targets: 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Approval scope: requested source-driven feature migration with mandatory authoritative access/data-loss safeguards, not authorization to drop the remaining integrations. Native guards intentionally avoid upstream's unchecked overwrite behavior; no-op/failed writes do not spend charge. Those boundaries are explicit rather than hidden in a parity claim.

## Remaining migration

Lightwell, Botania apothecary and Thaumcraft crucible integration, shared powder recharge/socket/HUD behavior and broader upgrade acquisition remain open. Runtime water placement/recharge, multiplayer synchronization, Fabric mixin startup, recipe bucket returns and presentation acceptance are deferred with the user's feature-first testing policy. This is not complete Rivertear/Arsenal parity or first-playtest readiness.

Source correction during checkpoint 0051: the pinned PacketArcaneGems.GemParticle client handler explicitly does nothing for pendeloque gems. Earlier wording about an original beam was an inference from packet naming, not implemented upstream behavior; no beam migration is required at this pin.

## Integration evidence

`./gradlew assemble --no-daemon`: exit 0, 20s, `build/rivertear-integration-20260909-053824.log`. All four compileJava tasks completed and assembly succeeded; `git diff --check` passed. Runtime class, acquisition recipe, item model and animation sidecar were inspected in each target JAR; Fabric mixin declarations and remapped annotations were inspected statically. Artifact class expectations are updated, not evidence of a full verifier run. No gameplay tests, optimization, clients, commits or publication.
