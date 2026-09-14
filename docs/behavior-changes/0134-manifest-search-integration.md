# 0134 — Manifest JEI search integration and search persistence

## Scope and approval

### Enchanted-book search follow-up

#### Native item verification

Moved the same native lookup into `ManifestSearch`, which has no graphical-class linkage, so the screen and native regression call the identical production method. The server-player fixture creates actual Sharpness V books and swords through each version's native `/give` item parser (legacy NBT or modern components). It confirms stored-book matches, rejects enchanted tools, wrong levels and mod queries, rejects an empty enchanted book, and checks the input stack is unchanged. This is native item/name-lookup evidence in the server's default language, not connected rendering or client language reload.

`timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 59s, `build/manifest-native-search-final-20260912-085147.log`, all four native suites pass (initial checkpoint exit 0, 88s, `build/manifest-native-search-20260912-084956.log`). `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/manifest-native-search-artifacts.log`, all four pairs. No production matching semantics or permissions changed.

#### Initial implementation checkpoint

Restored `ManifestList.java:131–143`'s additional ordinary-query match against enchanted books' localized enchantment names including levels. Only enchanted books qualify; enchanted tools and `@` queries do not enter this fallback. The screen reads stored enchantments through `EnchantmentHelper.getEnchantments` on 1.20.1 and `getEnchantmentsForCrafting` on 1.21.1, then formats names through the corresponding native `Enchantment.getFullname` API. No tooltip-wide search or server/listing mutation is introduced. Original behavior restoration applies to all four targets.

Nine search/session tests pass without skips on every leaf. The new predicate test covers case folding, level-specific matching, unrelated names and rejection of mod queries. These are string-predicate assertions, not native enchanted-item or connected-screen runtime verification.

`timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 85s, `build/manifest-enchantment-search-20260912-084601.log`. `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/manifest-enchantment-search-artifacts.log`, all four pairs. Connected stored-enchantment lookup, localization/resource reload and actual query entry remain in consolidated acceptance. This supersedes the earlier enchantment-search implementation gap, not that runtime coverage gap.

### Registry fallback follow-up

Restored `types/lists/ManifestList.java:111–127`'s registry-path fallback for ordinary queries and namespace fallback for `@` queries, in addition to the existing display-name matches. The screen calls the tested predicate directly. This preserves the distinction between item searches and mod searches; it does not introduce full-ID matching for ordinary queries or remove spaces from the user's query. Original mod display-name space removal remains. No server permissions or listing data change.

Eight search/session tests pass without skips on each of the four leaves, including renamed-item lookup, a mod ID different from its display name, case folding, empty queries and negative item/mod boundary cases. Added this Minecraft-independent class to Forge's explicit unit-test allowlist; earlier broad statements about Forge search-session execution were not backed by that allowlist and are superseded by these fresh XML reports.

`timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 91s, `build/manifest-search-fallback-20260912-084143.log`. `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/manifest-search-fallback-artifacts.log`, all four pairs. Connected query entry/JEI rendering remains unverified. Upstream enchanted-book enchantment-name matching (`ManifestList.java:131` onward) is another remaining search path, not completed by these identifier fallbacks.

Implements the original search integration requested by the 0.0.2 plan. No storage permissions, inventory contents, crafting or extraction behavior changes. JEI remains optional and no new dependency is introduced. This record does not complete Manifest tracking, the lectern or the release's connected-client acceptance.

## Original behavior

Source: `AranaiRa/ArcaneArchives` at `bb99accf48ed583e29b0efae56e28c963407b8df`:

- `config/ConfigHandler.java:175–181`: `jeiSynchronise` and `searchTermPersistence` default false.
- `client/gui/controls/ManifestSearchField.java:19–68`: Shift-click copies JEI's query even when synchronization is disabled; edits push the Manifest query to JEI when enabled, including clearing the query.
- `client/gui/GUIManifest.java:134–136,148–164,365–369`: capture JEI's original query for the screen session, optionally retain Manifest's previous search, and immediately push the current Manifest query when synchronization is enabled with the button.
- `client/gui/GUIManifest.java:402–407,518–520`: restore the captured JEI query on close only if synchronization is still enabled and the captured query was nonempty.

The last condition is preserved, not silently repaired: an initially empty JEI query is not restored to empty on close, and disabling synchronization before closing suppresses restoration. Opening alone does not push a new query.

## Implemented behavior

- `client/ManifestSearch.java` owns the search-session state and plain Java read/write callbacks; it has no JEI/Minecraft class dependency. Repeated close is idempotent. Only the query string is remembered, never an inventory snapshot or world/player object.
- `integration/jei/ArcaneArchivesJei.java` binds the actual `IIngredientFilter` callbacks in `onRuntimeAvailable` and unbinds them in `onRuntimeUnavailable`. A session is bound to that runtime identity; an old screen cannot call a retired filter or overwrite a replacement runtime's query. Reopening establishes a fresh session.
- `client/ManifestScreen.java` connects the original-position JEI button, its enabled/disabled feedback, Shift-left-click query copy, normal edits/right-click clear, and screen removal. Resize retains the same active search session and original JEI query; reinitializing a previously removed screen creates a fresh session. Tooltip objects are not replaced every frame/tick while their state is unchanged.
- `config/ClientConfig.java` reads `ManifestSearchTermPersistence` and `ManifestJeiSynchronise` from `config/arcanearchives/client.properties`. Both default false, preserving upstream defaults and old config compatibility. Add either key with `=true` to enable it by default; restart to apply. New files document the keys; existing files, comments and unknown settings are never overwritten. Invalid boolean values fail without replacing the file.
- Artifact verification requires the search implementation, nested callback record and matching source file. No optional JEI classes are referenced by the always-loaded screen/search implementation.

## Verification

Pinned installed JEI APIs inspected with `javap`: 1.20.1 Fabric `jJOr2rUn` and 1.21.1 NeoForge `UJRXzDfp` expose `onRuntimeAvailable`, `onRuntimeUnavailable`, `IIngredientFilter.getFilterText` and `setFilterText`. All four pinned APIs subsequently compile in the full matrix.

Command: `timeout --foreground 10m ./gradlew build --no-daemon`

Exit 0, 85s; complete log `build/manifest-search-20260912-064918.log`. All four native required suites also passed.

Seven `ManifestSearchTest` cases pass on every target, with no errors/failures/skips. They exercise real production search-session code using string callbacks, not a fabricated JEI runtime: no-JEI operation; disabled/enabled edits and clearing; immediate toggle push; Shift-copy semantics; conditional/nonempty restoration and repeated close; opt-in persistence; unloaded/replaced runtime isolation; closed-session rejection. Config tests cover defaults, true overrides, legacy files and invalid-value non-overwrite behavior.

Reports:

- `versions/1.20.1-fabric/build/test-results/test/TEST-com.aranaira.arcanearchives.client.ManifestSearchTest.xml`
- `versions/1.21.1-fabric/build/test-results/test/TEST-com.aranaira.arcanearchives.client.ManifestSearchTest.xml`
- `versions/1.21.1-neoforge/build/test-results/test/TEST-com.aranaira.arcanearchives.client.ManifestSearchTest.xml`
- Forge cases are inside `versions/1.20.1-forge/build/test-results/forge-runtime/TEST-junit-jupiter.xml`, not a standalone per-class report.

Command: `timeout --foreground 2m python3 scripts/verify_artifacts.py`

Exit 0, 0s; complete log `build/manifest-search-artifacts.log`. All four production/source pairs pass.

## Remaining acceptance

Actual JEI widgets, Shift-click delivery, restoring after overlay/recipe-screen transitions, GUI scales, runtime/resource reload and no-optional-mod client startup belong to the consolidated client campaign in the release plan. They are not proven by callback tests or compilation. Remaining Manifest hotkey/item-under-cursor tracking, HUD/lines, other settings and lectern work stays open, followed by Brazier, complete Tome and four-target release acceptance. No commit, version bump or publication is performed here.
