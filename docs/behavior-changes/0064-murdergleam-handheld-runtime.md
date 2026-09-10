# Murdergleam handheld critical-hit runtime

## Original behavior

Pin bb99accf48ed583e29b0efae56e28c963407b8df: items/gems/asscher/MurdergleamItem.java, events/EventHandler.java:onCriticalHitLanded, init/RecipeLibrary.java and original language/model/texture files. MIT source/asset notices retained.

Capacity 30/150; toggle-capable. At the critical-hit decision, every enabled charged available Murdergleam forces critical status and sets the base damage modifier to 1.5, consumes one charge and toggles off at zero. This applies even to attacks already vanilla-critical and consumes each eligible gem, not merely the first. Otherwise a vanilla critical restores three charge and toggles the gem when full (including an already-full disabled gem). The original event runs before damage success, not after confirming a landed damaging hit. No additional creative exemption or attack-strength requirement is introduced.

Recipe: one shaped quartz, four yellow dyes, a diamond sword and two blaze powder. Original animated yellow/dun asscher and accessibility art; original English text in both language resources.

## Implementation and scope

MurdergleamItem shares authoritative both-hand charge/toggle processing. Forge uses CriticalHitEvent ALLOW and setDamageModifier; NeoForge uses CriticalHitEvent.setCriticalHit and setDamageMultiplier. Fabric modifies the completed native critical flag before its native multiplier/sweep/presentation branches, after sprint exclusion. Mapped bytecode inspection established slot 8, second STORE for 1.20.1; slot 9, first STORE for 1.21.1. Injection requires exactly one match. Existing authoritative gem toggle, creative Arsenal gating, recipe gating and client model properties are connected.

No intentional balance deviation proposed. This is handheld scope; original available-gem socket/wearable integration and shared HUD/upgrades/powders remain open. Custom combat-mod/event ordering, cross-loader hook differences and Fabric local-variable mixin coexistence require deferred runtime acceptance. Slaughtergleam source was inspected but is not implemented by this checkpoint.

## Verification

`./gradlew assemble --no-daemon` with ten-minute timeout exited 0 in 21s. Log `build/murdergleam-integration-20260909-113211.log` reports all four compileJava tasks and BUILD SUCCESSFUL in 20s. Runtime/event classes, recipe, model and animation packaged on all four targets. Both Fabric production mixins contain remapped attack target method_7324. `git diff --check` passed. Artifact verifier class expectations updated; full verifier not run.

No gameplay tests, optimization, clients/servers, commits or publication. Assembly and static mapping/packaging checks do not establish successful mixin application, combat parity or first-playtest readiness. Migration remains incomplete.
