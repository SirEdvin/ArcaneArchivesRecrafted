# Recipe-viewer Arsenal index visibility

## Source and scope

Restores pinned upstream `80944ce45c6559243d8928cc4b305bf379388652` behavior from `integration/jei/JEIPlugin.java:70–85` and `init/ItemRegistry.java:101`, with its EMI counterpart under approved scope 0113.

- Disabled Arsenal hides the original fifteen gems and both powder items from viewer indexes.
- Enabled Arsenal leaves gems visible but still hides both chromatic powders, including colored variants.
- The Gem Socket is not in upstream's exclusion list and remains unaffected, as do ordinary items.
- The upstream guidebook-condition sentinel is not registered in this port; no placeholder or extra item is invented to hide it.
- These are presentation exclusions, not item deregistration, recipe removal, payment changes or access controls. Existing creative inventory and server behavior remain unchanged. Configuration retains the existing restart-required/local behavior.

`ViewerHiddenItems` provides one immutable item-identity set shared by both optional plugins. JEI filters actual indexed variants and removes a nonempty snapshot through its runtime ingredient manager; EMI uses its standard stack-removal predicate. APIs were inspected with `javap` on all eight pinned JEI/EMI artifacts. No dependency or build-tooling change was needed.

## Automated verification

Command: `timeout --foreground 10m ./gradlew build --continue --no-daemon`, full output redirected to the logs below.

- Initial build: exit 1, 70s, `build/viewer-visibility-20260911-141345.log`. A new test attempted to call a protected item helper; corrected the fixture to use native custom-data components without widening production access.
- Corrected tests: exit 0, 55s, `build/viewer-visibility-final-20260911-141534.log`.
- Final guarded-callback build: exit 0, 79s, `build/viewer-visibility-guarded-20260911-141852.log`. All four leaves build and native GameTest suites pass.
- Three NeoForge registered-item tests pass, zero failures/errors/skips: exact upstream exclusions, enabled-state/ordinary-item/socket conservation, and all powder color variants without stack mutation. XML: `versions/1.21.1-neoforge/build/test-results/test/TEST-com.aranaira.arcanearchives.integration.ViewerHiddenItemsTest.xml`. These are shared-rule tests, not four-loader installed-viewer tests.
- `timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, 0s; `build/viewer-visibility-artifacts-final.log`. All four production/source pairs pass; the shared helper is required by the artifact inventory.

## Live-client verification

Private Fabric 1.21.1 client/integrated-server fixture and evidence: `/tmp/arcane-viewer-visibility-1789136231323122217/`. Full per-run logs and observed results are indexed in `runs.json`. Each launch uses the existing private Xvfb driver with `ORG_GRADLE_PROJECT_optionalIntegrationMods=jei` or `emi`, a ten-minute timeout, `:1.21.1-fabric:runClient --no-daemon`, and quick-play into a disposable copy named `Viewer visibility verification`.

The first JEI probe caught an exception from an empty removal collection, despite process exit 0. JEI's creative-derived index already lacked the disabled items. Added the empty-list guard and rebuilt; the subsequent disabled-state JEI and EMI probes have no plugin errors. This is why process exit alone is not viewer acceptance.

Disabled-state screenshots show no Agegleam or chromatic-powder results and retain the Raw Radiant Quartz search control. Native logs confirm normal save/shutdown. Known headless OpenAL errors remain unrelated to index correctness.

Enabled-state JEI and EMI screenshots show Agegleam returning to the index, powders still absent, and the quartz control retained. Four corrected viewer/configuration sessions passed:

| Viewer | Arsenal | Exit | Duration | Full log |
| --- | --- | --- | --- | --- |
| JEI | off | 0 | 82.04s | `build/viewer-visibility-live-1789136432002249643.log` |
| EMI | off | 0 | 81.51s | `build/viewer-visibility-live-1789136526189496483.log` |
| EMI | on | 0 | 88.04s | `build/viewer-visibility-live-1789136631427518572.log` |
| JEI | on | 0 | 103.59s | `build/viewer-visibility-live-1789136730902126475.log` |

Each corrected log contains successful build, complete dimension saves and normal client stop; its only ERROR-level entry is the known headless sound initialization failure. The earlier unguarded JEI probe is retained separately in `runs.json`, not counted as passing acceptance.

Screenshots include full captures for each query and `jei-off-index.png`, `emi-off-index.png`, `emi-on-index.png`, `jei-on-index.png` composites of actual index/search regions. The temporary Arsenal setting was restored from its exact original file, and the owned fixture world was archived outside the repository after all clients stopped. No item grants, crafting or inventory mutations were part of this index test.

## Remaining scope

Other-loader installed-viewer acceptance, combined-viewer visibility, mod-added custom index entries, remote configuration differences and the broader migration remain open. The accepted JEI–EMI tag-recipe collision limitation in 0130 is untouched. This increment does not claim the full integration task complete.
