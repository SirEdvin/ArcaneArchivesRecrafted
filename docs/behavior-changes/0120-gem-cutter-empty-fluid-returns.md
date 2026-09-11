# 0120 — Gem Cutter empty-fluid-container returns

## Approval status

Approved by the user in response to the explicit empty-fluid-container return question. Implemented and verified below: native Reservoir crafting now passes on all four targets and the complete build is green. Connected multiplayer acceptance remains separate.

## Original behavior

Pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`:

- `inventory/ContainerGemCuttersTable.java:55–59` calls `consumeAndHandleInventory` with `recipe::handleItemResult` from the output take path.
- `recipe/gct/GCTRecipe.java:114–145` queries the fluid-item capability, takes tank zero's contents, calls drain, retrieves the container and returns it through table/player inventory. There is no nonempty-fluid condition before deciding to return the container.
- `inventory/handlers/TankItemFluidHandler.java:28–30` reports one tank property even when its contents are empty. `items/itemblocks/RadiantTankItem.java:24–33` also declares itself as its crafting container and installs this capability.
- `init/RecipeLibrary.java:193` requires two Matrix Braces, one Containment Field, six Empowered Quartz and ten Radiant Tanks to make one Matrix Reservoir. The return path means those tanks are returned, not permanently consumed. This is source-backed control-flow evidence, not a legacy runtime reproduction.

## Port behavior before this fix and regression

Decision 0032 deliberately supports only one nonempty fluid tank/view and leaves empty-fluid cases refused. `GemCuttersTableMenu.needsRemainderProcessing` recognizes empty Radiant Tanks as fluid/crafting-container inputs. `GemCutterFluidRemainders.prepare` then returns an empty optional for empty contents, refusing the whole craft before payment. Thus the newly shipped Reservoir recipe is present and previews but cannot actually craft with its ordinary empty tanks.

`src/sharedGameTest/java/com/aranaira/arcanearchives/gametest/MatrixReservoirCrafting.java` runs in every loader's isolated native server. It uses the actual registered two-part Gem Cutter, block entity, menu, server recipe manager and normal recipe-page selection. It tests one-short inputs, split table/player payment, cursor incompatibility, detached/out-of-range menus, exact output, returned tanks and replay prevention. No substitute recipe catalog or remainder policy is injected. The first successful-output assertion initially failed; the approved implementation now passes the complete fixture, including the later conservation assertions.

## Proposed behavior

Extend the existing detached single-container preparation to accept a verified single empty tank/view and return its unchanged detached container. Preserve native item data, require return capacity before payment, and keep all access/catalog/input/cursor checks and atomic commit semantics. Keep multi-tank, inconsistent view data, unsupported containers and partial extraction refused. No generic crafting-container fallback, silent loss of tank data, extra recipe, world-drop side effect or fluid conversion is proposed.

For the Reservoir recipe: require all ten tanks, consume the Braces/Field/Quartz, produce one Reservoir and return ten tanks. Do not silently change this to consuming the tanks. Filled-container behavior and existing failure safeguards remain unchanged.

## Reason

Restore the original reachable empty-container return behavior and unblock the registered Reservoir acquisition path. This extends 0032's intentionally bounded implementation; it does not invent a new Reservoir fluid function. Explicit approval is requested because the returned-tank semantics are materially different from treating every listed ingredient as permanently consumed.

## Affected targets

- Minecraft 1.20.1 Fabric
- Minecraft 1.20.1 Forge
- Minecraft 1.21.1 Fabric
- Minecraft 1.21.1 NeoForge

## Verification

### Approved implementation and GREEN evidence

- `GemCutterFluidRemainders.prepare` now returns an unchanged detached copy for one verified empty native fluid tank/view. Fabric requires exactly one view, a blank resource and zero amount; nonblank/zero and blank/nonzero remain refused. Forge/NeoForge require exactly one native tank and empty contents. No drain is attempted for empty containers, no generic crafting-remainder fallback was added, and filled-container preparation and the conserved menu commit are unchanged.
- The shared native fixture now passes exact recipe payment, output of one Reservoir, return of all ten Tanks, split table/player payment, one-short rejection for each ingredient, cursor/access rejection, preservation of unrelated inventory and no unpaid repeat output on all four targets. Direct tagged-Tank preparation additionally verifies unchanged item data, detached output, caller immutability and refusal of a multi-count preparation argument. The older NeoForge empty-bucket refusal expectation was updated to verify its now-supported detached single-container return; filled water/lava and unsupported-container tests remain.
- `timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 85 seconds, `build/empty-fluid-green-20260911-052348.log`. Native completion: five tests per Fabric target (four Reservoir fixtures plus Patchouli's smoke test), six Forge tests (including shared JUnit assertions), five NeoForge tests. Both Fabric XML reports explicitly identify the passing `matrixreservoircrafting` testcase.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, `build/empty-fluid-green-artifacts.log`. All eight current production/source JARs exclude GameTest classes. `git diff --check` passes; no project JVM remained running.
- The menu's existing return-capacity and final-access safeguards are retained, not bypassed. General third-party container compatibility, surplus world drops and connected multiplayer remain outside this bounded acceptance.

### Historical RED evidence

- Initial full `timeout --foreground 10m ./gradlew build --no-daemon`: exit 1, 46 seconds, `build/matrix-crafting-20260910-225004.log`; native recipe craft failed. No production fix applied.
- Bounded matrix reproduction: `timeout --foreground 10m ./gradlew :1.20.1-fabric:runGameTest :1.20.1-forge:runGameTestServer :1.21.1-fabric:runGameTest :1.21.1-neoforge:runGameTestServer --continue --no-daemon`; exit 1, 53 seconds, `build/matrix-crafting-red-20260910-225302.log`.
- All four targets fail `matrixReservoirCrafting` with `Exact payment did not produce one Reservoir`. Both Fabric native XML reports identify this exact failure; both Forge-family logs identify the same fixture. No project JVM remained running after reproduction.
- The original RED acceptance target is now resolved by the GREEN evidence above. Full-return-space native compatibility beyond the existing conserved-menu regressions and connected multiplayer/rendering acceptance remain separate.
