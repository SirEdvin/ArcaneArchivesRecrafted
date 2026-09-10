# 0085 — Scepter sneak-use dispatch

Status: scepter dispatch implementation and four-target assembly/package verified; connected runtime/protection acceptance remains open.

Original baseline `80944ce45c6559243d8928cc4b305bf379388652`: Revelation, Manipulation and Translocation each return true from doesSneakBypassUse. Restore the shared hook, without inventing new item actions. Forge/NeoForge use the native item extension. Fabric has no equivalent vanilla item hook: narrowly adapt the secondary-use predicate in server useItemOn and client performUseItemOn; both hands must be empty or a migrated scepter, matching native empty-stack handling. An ordinary item in the other hand still suppresses block use while sneaking. Do not change actual player sneak state, replay block interactions or bypass permission/spectator checks.

The mapped 1.20.1 and 1.21.1 bytecode each has one matching predicate call in both methods (`build/scepter-dispatch-bytecode.log`). NeoForge 21.1.234 IItemExtension/IItemStackExtension confirms LevelReader signature and empty-stack bypass. Forge cached 47.1.0 source confirms the same signature; compilation verifies pinned 47.3.39.

Affected targets: all four leaves. Source-backed restoration under standing approval, no new gameplay deviation. Other items with upstream bypass declarations (including several gems) remain a separate unfinished integration surface. Fabric compatibility with other mods replacing this predicate, live mixin application and connected-player/protection acceptance remain open.

Verification:
- `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*TranslocationScepterTest' --offline --no-daemon`: exit 0, 28s, `build/scepter-sneak-20260909-182922.log`. All four targets assembled.
- NeoForge XML: 3 tests, 0 skips/failures/errors. The new fixture checks every pair of empty hand, three registered scepters and ordinary stone against native item-stack bypass hooks; prior recipe/identity fixtures also pass. This is not actual player dispatch.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/scepter-sneak-artifacts.log`; all four production/source pairs pass, including strict client-only mixin registration. `git diff --check` passes.
- Both Fabric production JARs contain remapped server `method_14262` / client `method_41934` injection annotations targeting the corresponding player's `method_21823()Z`. This proves packaging/remapping, not live mixin application.

This supersedes 0084's missing scepter sneak-bypass implementation, not its runtime acceptance gaps. No development client/server was launched for this increment.
