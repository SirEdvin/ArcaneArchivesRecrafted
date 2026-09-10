# First migration slice: legacy utility contracts and registry baseline

Historical slice report. For the current combined implementation and latest verification, start at [HANDOFF.md](../HANDOFF.md). Shaped quartz and the external Patchouli backend were added after this foundation; statements about what this slice does not implement are not the current whole-project status.

## Scope and provenance

Baseline: https://github.com/AranaiRa/ArcaneArchives/tree/80944ce45c6559243d8928cc4b305bf379388652

This slice ports `util/MathUtils.java` and adds shared JUnit coverage on all four Stonecutter leaves. It does not register content or claim that Manifest/HUD gameplay has been ported. Source license remains MIT, retained at `../upstream/LICENSE` and embedded in production/source JARs.

The implementation keeps original method names, integer arithmetic, default-locale formatting, bit widths, shift order, casts and masks. Only Minecraft class mappings (`MathHelper` → `Mth`, `Vec3d` → `Vec3`) and explanatory comments change. This utility slice introduces no gameplay/logic deviation. The separately approved quartz-conversion correction is not implemented here.

## Traced callers

Paths here are relative to upstream `src/main/java/com/aranaira/arcanearchives/`:

- `inventory/ContainerManifest.java:153` and `client/gui/framework/ScrollEventManager.java:143`: positive-count ceiling division for scroll steps.
- `client/render/RenderHUD.java:60`: extended storage count formatting.
- `util/ManifestTrackingUtils.java:49-79`: stores 1.12 `BlockPos.toLong()` keys and decodes them into vectors for tracking lines.
- `client/render/LineHandler.java:68-73`: re-encodes tracked vectors to remove the matching entries.

## Preserved contracts

- `intDivisionFloor` is historically misnamed: it uses Java integer division (truncation toward zero), not mathematical floor for negative inputs. Zero division still throws. Do not silently replace it with `Math.floorDiv`.
- `intDivisionCeiling` preserves `(a + (b - 1)) / b`, including Java overflow/sign semantics. Current traced callers supply scroll counts; broad arithmetic cleanup needs its own decision and tests.
- HUD counts below 1024 remain unscaled; larger counts use powers of 1024 and lowercase `k/m/g/t/p/e`, one decimal place, and the process FORMAT locale. Values just below the next band may round to `1024.0k`; this is deliberate parity, not a new formatting choice.
- Packed positions use signed 26-bit X, 12-bit Y, 26-bit Z in the original X/Y/Z order. Fractional coordinates truncate toward zero; out-of-range values retain original masking behavior.
- Modern `BlockPos.asLong()` uses a different layout. When porting ManifestTrackingUtils, keep insertion/removal/lookup/iteration on the same encoding; never combine modern keys with this legacy decoder. Fresh worlds need no old-key conversion or compatibility shim; any future internal format choice must still keep its callers consistent.
- The user explicitly selected fresh worlds only and excluded data migration. Existing utility fixtures describe the code already ported; they impose no requirement to read 1.12 saves or convert integer dimension IDs. Normal fresh-world persistence still requires tests.

## Tests and actual execution

Test source: `src/test/java/com/aranaira/arcanearchives/util/MathUtilsTest.java`.

- Five parameterized scroll-count cases plus negative/zero division, count thresholds/suffixes, locale behavior, golden packed bytes in both directions, fractional truncation/signed wrapping and deterministic coordinate round trips.
- RED: `:1.21.1-fabric:test` exited 1 because MathUtils did not exist; `build/migration-red-20260907-162503.log`, 14 seconds.
- First matrix attempt exposed missing Minecraft classes on ModDevGradle test classpaths. The pinned plugin's `addModdingDependenciesTo(sourceSets.test.get())` supplies those classes on Forge and NeoForge. This is plain JUnit, not a running loader/server environment.
- GREEN: root `build --no-daemon` exited 0; `build/migration-green-20260907-162707.log`, 21 seconds. XML reports show 11 tests on each leaf, no failures/errors/skips. Test-only dependencies stay in test configurations.
- Round trip: switched to 1.20.1 Forge, built all leaves, rechecked artifacts, then restored canonical 1.21.1 Fabric. All commands exited 0; source/test tree and controller matched the pre-switch snapshot byte-for-byte. Combined run: 21 seconds, `build/migration-roundtrip-20260907-163241.log`.
- Use the timeout/file-log wrapper from AGENTS.md for all reruns. JUnit XML lives at `versions/<node>/build/test-results/test/`; HTML at `versions/<node>/build/reports/tests/test/`.
- `scripts/verify_artifacts.py` now checks that the migrated utility ships in production/source JARs while test classes and dependencies do not appear as production classes.

No client/server startup or end-to-end Manifest rendering was exercised by these unit tests; those remain separate migration gates.

## Registry audit findings

`registry-baseline.json` records source registration lists, stable IDs, classes, aliases and source paths. It is a static source audit, not a successful upstream runtime trace.

- 29 blocks; 27 matching block items. Fake Air and Brazier Fire have no registered item counterparts.
- 41 unconditional standalone items. Together with block items, the default item registry list has 68 entries.
- One conditional item, `arcanearchives:gbook_arsenal_condition`, makes 69 when Arsenal is enabled.
- `ConfigHandler.java:74-77` defaults Arsenal to false. Gem items are still registered; `ArcaneGemItem.java:47-49` hides them from the creative tab, with crafting/JEI gating handled elsewhere. Do not confuse disabled acquisition with absent registration.
- Three sound events, one weight entity, 21 legacy item aliases and two legacy block aliases are recorded. The external guidebook alias and ignored missing Arsenal-condition item are retained as historical evidence only; implementing old-data remaps is out of scope.
- Unregistered items, matrix/furnace gaps, duplicate Wonky Resonator registration and incomplete menu routes are documented separately in `UNFINISHED_UPSTREAM_FEATURES.md`. Registry presence alone is not a functional-completeness guarantee.

## User decisions after this slice

1. Save unfinished-feature notes separately: recorded in `UNFINISHED_UPSTREAM_FEATURES.md`. This records the findings without silently completing or enabling those features.
2. "Fresh worlds only, ignore any possible data migration tasks": no old-world import, data converters or cross-version/cross-loader save conversion. Continue developing and testing persistence for new worlds normally.
3. Raw-quartz remainder-only dropping is approved: `../behavior-changes/0003-raw-quartz-conversion-conservation.md`. Implementation and runtime/conservation tests remain pending until the chest-conversion mechanic is ported.

The complete asset/recipe/configuration and gameplay port remains unfinished. Later slices add shared registration adapters, quartz resources and all-leaf client/server checks; see [QUARTZ_CONTENT.md](QUARTZ_CONTENT.md) and [GUIDEBOOK_BACKEND.md](GUIDEBOOK_BACKEND.md). These do not complete persistence, multiplayer or the parent migration phases.
