# 0092 — Storage component sneak-use dispatch

Status: implemented; all four targets assemble/package. Source-backed restoration under standing approval.

Master baseline 80944ce45c6559243d8928cc4b305bf379388652 DevouringCharmItem:40–43, ContainmentFieldItem:33–36 and MaterialInterfaceItem:33–36 return true from doesSneakBypassUse. Restore those declarations with native Forge/NeoForge hooks and the existing Fabric secondary-use predicate adapter. Other StorageComponentItem registrations retain false; do not extend this to Matrix Brace by shared class association.

Preserve the native conjunction of both hands: an ordinary non-bypass item in either hand still suppresses block use while sneaking. No player sneak-state mutation, replay, access bypass, new upgrade ability or recipe change. Upstream Fabrial also declares true but is excluded from upstream item/model registration (ItemRegistry.java:101,115,129); it stays dormant under the approved scope, not pending restoration. This corrects the earlier classification in this note.

Verification: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*StorageSneakUseTest' --tests '*GemSneakUseTest' --tests '*TranslocationScepterTest' --offline --no-daemon`: exit 0, 28s, `build/storage-sneak-20260909-202226.log`. NeoForge XML totals 6 tests, zero skips/failures/errors. New fixture checks the three original true declarations, Matrix Brace false, all ordered mixed-hand pairs with ordinary items/scepters/gems and read-only stack behavior against native item hooks. Existing gem/scepter regression fixtures pass.

`timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/storage-sneak-artifacts.log`; all four production/source pairs pass. `git diff --check` passes. Connected block-use, Fabric live predicate application and protection-mod acceptance remain open; no client/server launched.
