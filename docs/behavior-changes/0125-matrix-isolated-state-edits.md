# 0125 — Isolated Matrix block-state edits

## Approval status

Approved directly by the user: “exclude it”. Isolated external per-part state edits are excluded from migration acceptance. Removed the associated failing fixture; production logic is unchanged. This excludes the demonstrated defect rather than repairing it.

## Original behavior and current evidence

Pinned upstream `80944ce45c6559243d8928cc4b305bf379388652` has invalid inherited Matrix accessor placement, as traced in 0119; it does not establish a working legacy whole-structure reconfiguration contract. The approved port repair preserves native facing/part properties and normal placement/removal. Same-block state reconfiguration was explicitly left open in 0119's verification list.

The port derives the removal footprint from the removed state's current facing and part. `MatrixDistillate.onRemove` only cleans up when the block type changes. Editing just the root's facing through native `Level.setBlock(..., UPDATE_ALL)` keeps the same block type and identity but rotates its inferred footprint without moving its siblings. Destroying that edited root leaves six original loaded parts behind on all four targets. This reproduces the native state transition, not an end-to-end test of a particular command, debug stick, wrench or protection mod.

The fixture starts with native paid item placement and loaded empty space, rotates only the root by a quarter turn, destroys it, and checks the original positions. Its `finally` clears owned fixture blocks and drops even on failure. No missing/unloaded chunks are involved.

## Approved behavior

Exclude arbitrary isolated per-part edits of Matrix `facing`/`part` state after placement. These are unsupported external modifications, not an in-game Matrix rotation feature. Keep ordinary item placement, protected item-state/identity handling, matching loaded-part break/replacement cleanup, and existing native rotation/mirror state functions unchanged. No new scheduler, footprint index, interception layer or whole-structure relocation mechanism.

This does not claim that every complete-structure copy/rotation tool is compatible; such compatibility remains unverified. It does not exclude normal native block-type replacement/removal, which remains supported and tested.

If isolated edits must be supported instead, define a coherent repair policy before implementation: restore the original structure orientation, re-place the entire footprint subject to collision/permission/payment checks, or invalidate/remove the affected structure with explicit drop semantics. These differ observably, and none should be chosen silently. Simply removing the same-block guard is insufficient: it can interact with temporary item-state normalization, placement snapshots and native structure transforms.

## Reason

User-approved scope reduction avoids an unrequested Matrix reconfiguration feature. This resolves the isolated per-part state-edit acceptance item previously left open in 0119; it does not close broader Matrix migration.

## Affected targets

1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. The demonstrated failure is Distillate root-facing mutation; no claim is made that all Matrix blocks or all property mutations were tested.

## Historical reproduction before exclusion

Command:

```sh
timeout --foreground 10m ./gradlew \
  :1.20.1-fabric:runGameTest :1.20.1-forge:runGameTestServer \
  :1.21.1-fabric:runGameTest :1.21.1-neoforge:runGameTestServer \
  --continue --no-daemon
```

Exit 1, 77 seconds. Complete output: `build/distillate-state-change-red-20260911-091835.log`.

Each native task failed with `Isolated root-facing edit orphaned 6 loaded Distillate parts`. Both Fabric XML reports contained the corresponding failing `fabricruntimetests.matrixdistillatelifecycle` case. No project JVMs remained. This is historical evidence for the unsupported case, not a repaired behavior claim.

## Verification after approved exclusion

- Removed only the isolated-state-change fixture and its call. Normal lifecycle, preflight, interruption, item identity and native cancellation coverage remain intact.
- `timeout --foreground 10m ./gradlew build --continue --no-daemon`: exit 0 in 56 seconds, `build/matrix-state-exclusion-20260911-094853.log`. Required native suite completions: 6/7/6/6 across the four targets. Both Fabric Distillate XML cases pass; no ERROR/FATAL lines in the build log.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, `build/matrix-state-exclusion-artifacts.log`; all four production/source pairs pass.
- No project JVMs remained. Broader Matrix/migration acceptance remains open; no production behavior changed.
