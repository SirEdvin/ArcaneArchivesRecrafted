# 0084 — Registered Translocation Scepter

Status: item/recipe/assets and shared Trove scepter classification restored. Scepter sneak-bypass implementation is now covered by [0085](0085-scepter-sneak-use.md); runtime acceptance remains open. Statements below describe this increment's original verification boundary.

Approved baseline `80944ce45c6559243d8928cc4b305bf379388652`: `items/ScepterTranslocation.java` registers a one-stack scepter with a gold tooltip translation key and sneak bypass. The tooltip key has no translation in the inspected baseline language files; preserve that unresolved key rather than invent prose. `init/RecipeLibrary.java:121` registers Revelation + Material Interface -> Translocation, despite assigning the returned recipe to the manipulation field. The recipe remains reachable.

Restore item registration, creative visibility, original model/texture and recipe. Preserve shared IItemScepter Trove mining, creative-removal, withdrawal suppression and sneak dismantling behavior through the current shared StorageScepterItem type. Translocation does not inherit Revelation reports or Manipulation editing. Do not enable `items/unused/ScepterTranslocationItem.java` or invent relocation. Recipe order 9 shares the existing ID tiebreaker with Manipulation.

Affected targets: all four supported leaves. Faithful reachable-content restoration under standing approval; no new gameplay deviation. Original MIT notices remain. Generic sneak bypass compatibility (especially Fabric), connected Trove interaction, rendering and save/restart acceptance remain open.

Verification: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*TranslocationScepterTest' --offline --no-daemon` exited 0 in 29s, `build/translocation-20260909-182508.log`. Four targets assembled. NeoForge XML records 2 tests, no skips/failures/errors: registered stack limit/type/tooltip and no inherited reporting dispatch; actual packaged recipe resolves the output, accepts Revelation + Material Interface and rejects missing/wrong scepters. These are fixtures, not connected-player acceptance.

`timeout --foreground 60s python3 scripts/verify_artifacts.py` exited 0 in 0s, `build/translocation-artifacts.log`; all four production/source pairs pass, including explicit recipe/artwork/atlas checks. Two model/texture assets match baseline bytes. `git diff --check` passes. No relocation power or completion of generic sneak bypass is claimed.
