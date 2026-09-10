# Parchtear runtime — approved temporary-block representation

## Original behavior

Source pin: `bb99accf48ed583e29b0efae56e28c963407b8df`. Inspected ParchtearItem, ArcaneGemItem, GemUtil, FakeAir, FakeAirTileEntity, DevouringCharmHandler and RecipeLibrary in the upstream checkout.

Parchtear starts with 8000 charge (27000 with the power-upgrade bit). Its 40-block source-fluid ray selects a single block when sneaking, otherwise a radius-one cube. Fluid blocks are replaced with FakeAir; the complete cast is charged afterwards with saturation at zero. FakeAir is invisible/passable/replaceable and expires after 200 ticking ticks; its countdown resets on reload. The Charm's bucket slot fully recharges Parchtear and returns it rather than voiding it. Original recipe: shaped quartz, four black dyes, four terracotta, four dead bushes. Arsenal acquisition and creative visibility are opt-in, disabled by default.

## Implemented but not accepted

Native Parchtear item, charge state, server-owned clearing, temporary block/entity, Charm recharge and original acquisition are connected. Original animated/dun/accessibility models and their transitive texture dependencies are recovered by `scripts/port_parchtear_assets.py`. ArsenalConfig reads `config/arcanearchives/arsenal.properties`, preserving `EnableArsenal=false` and `ColourblindMode=false`. The gem-cutting data recipe now supports a validated `arsenal` boolean and uses the server setting for catalog admission. Client model properties select depleted and accessibility art. No user configuration files were changed.

Loaded-chunk and interaction permission checks apply to world writes. Waterlogged host blocks are not treated as liquid blocks. Shared gem powder/socket/HUD integrations, broader upgrade acquisition and runtime acceptance remain unfinished; this is not complete Arsenal migration.

## Approved compatibility decision

Original FakeAir reports itself as air. Modern LevelChunkSection counts only non-air states as occupied, and LevelChunk.getBlockState returns ordinary AIR without consulting an all-air section's palette. Confirmed in the locally mapped 1.20.1 Forge sources. Consequently, copying the air flag can make the temporary fluid barrier disappear from world lookups in otherwise empty sections.

Proposal: remove the native `.air()` property while retaining invisibility, no collision, replacement by placed blocks, explicit liquid refusal through LiquidBlockContainer, and the 200-tick lifetime. Other mods' air checks would then see a block during that interval. Alternative: retain air classification with engine-level adaptations to chunk bookkeeping/lookups.

Affected targets: 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Approval status: APPROVED. After the temporary barrier was explained, the user explicitly accepted the suggested non-air representation: “Yes, I am fine with your suggestion, continue with it”. The native `.air()` property is removed; invisibility, passability, replaceability, liquid refusal and the original lifetime remain. This resolves the compatibility decision, not deferred gameplay acceptance or the entire migration.

## Verification so far

Initial `./gradlew assemble --no-daemon`: exit 0, 19s, `build/parchtear-integration-20260908-205623.log`. After the approved representation change, all four compileJava tasks and assembly passed: exit 0, 20s, `build/parchtear-approved-20260909-053302.log` (Gradle reports 19s). `git diff --check` passed. Parchtear/FakeAir classes, item model and animation metadata were inspected in all four production JARs. Class expectations in the artifact verifier are updated, but the full verifier was not run. These are integration checks, not functional fluid-barrier or full resource/presentation acceptance. No gameplay tests, optimization, clients, commits or publication.
