# 0138 — Manifest advancement branch

## Original behavior and restoration

Pinned upstream `bb99accf48ed583e29b0efae56e28c963407b8df`, `src/main/resources/assets/arcanearchives/advancements/manifest.json` and `lectern.json`, defines the branch `gemcutters_table -> manifest -> lectern`. Both use the criterion name `book` with `minecraft:inventory_changed` for the corresponding registered item. Despite the descriptions saying “Craft”, these are acquisition triggers, not crafting-only conditions. Parent links arrange the tree; no new possession or crafting prerequisite is added.

Restore these two resources under the modern advancement directory, using the existing native icon-key expansion and modern item predicates. Preserve identifiers, original titles/descriptions (including upstream spelling), parent links and criterion names. No rewards or additional conditions are introduced. Applicable targets: 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Approval status: original behavior restoration within the requested 0.0.2 plan; no proposed gameplay deviation.

The already-reviewed upstream license/notices apply. Existing registration and vanilla inventory-change handling perform the awards; no custom player-identity packet or extra event handler is needed.

## Verification

- `timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 78 seconds; complete output `build/manifest-advancements-20260912-074743.log`. All four native suites pass.
- Extended `StorageAdvancementsTest` with both entries. The existing NeoForge-native decoder test passes with registered item icons and closed parent references; this specific decoder assertion is NeoForge-only, not claimed on the other targets.
- `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, 1 second; complete output `build/manifest-advancements-artifacts.log`. All four production/source pairs pass, including advancement contents, version-specific icon keys, translation presence and parent closure.
- `git diff --check`: exit 0.

## Native acquisition and disk-save follow-up

The registered server-player fixture from 0139 now changes the player's real inventory and invokes the native inventory-menu broadcast. It does not manually grant advancements or call the criterion trigger. Assertions verify that a fresh player and an unrelated diamond do not earn either advancement; acquiring the Manifest earns only its own criterion; removing it preserves the original earned timestamp; acquiring the lectern then completes both entries.

After each step, the test calls the native advancement save method and reads that fixture UUID's actual advancement file. It checks `done` and the original `book` criterion, so this is evidence of native automatic awards and disk writes, not just decoding or in-memory state. All four server logs also report both advancement awards. No production behavior changed.

- `timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 59 seconds; `build/manifest-awards-20260912-080944.log`. All four native suites pass.
- `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, 0 seconds; `build/manifest-awards-artifacts.log`. All four artifact pairs pass.
- `git diff --check`: exit 0.

## Native reload follow-up

The fixture now calls `PlayerAdvancements.reload` after saving the Manifest criterion with its item removed. This native method clears progress and reloads the player file. The subsequent save must equal the preceding parsed JSON, preserving timestamps and the absence of unearned lectern progress. Acquiring the lectern afterward verifies that the pending inventory criterion listener was restored. A second reload with an empty inventory must preserve the entire completed saved branch. No production behavior changed.

- `timeout --foreground 10m ./gradlew build --no-daemon`: exit 0, 59 seconds; `build/manifest-reload-20260912-082110.log`. All four native suites pass.
- `timeout --foreground 2m python3 scripts/verify_artifacts.py`: exit 0, 0 seconds; `build/manifest-reload-artifacts.log`. All four artifact pairs pass.

The native fixture uses test connections and directly supplies inventory contents. Survival crafting/pickup, actual client toast/tree presentation, full resource reload, reconnect and server-process restart remain in consolidated acceptance; invoking the native advancement reload is not restart verification. This does not complete all network/Tome advancements, tracking/HUD, Brazier, Tome or 0.0.2. No commit, version bump or publication performed.
