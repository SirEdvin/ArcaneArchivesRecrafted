# Shaped quartz content slice

Handoff: [HANDOFF.md](../HANDOFF.md). This report preserves the quartz implementation evidence. The later required external Patchouli dependency and newest combined client/server matrix are in [GUIDEBOOK_BACKEND.md](GUIDEBOOK_BACKEND.md).

## Implemented scope

The first registered content is `arcanearchives:shaped_quartz` and `arcanearchives:storage_shaped_quartz` (block and matching block item), on all four leaves. This is not full quartz progression or a playable replacement mod.

Upstream baseline: `80944ce45c6559243d8928cc4b305bf379388652`.

The short Java/recipe/language paths in the comparison below refer to upstream sources. In the modern tree, registration and the creative tab live in `src/main/java/com/aranaira/arcanearchives/init/ContentRegistry.java`; there is no modern `CreativeTabAA` or `BlockTemplate` class. Storage uses native `BlockItem` and the block's tooltip override. Canonical recipes live under `src/main/resources/data/arcanearchives/recipe/`, and selected translations under `src/main/resources/assets/arcanearchives/lang/en_us.json` and `pt_br.json`.

- `items/ShapedQuartzItem.java`: stackable crafting material and gold translated tooltip.
- `blocks/StorageShapedQuartz.java` and `blocks/templates/BlockTemplate.java`: solid rock block, stone map color/sounds, hardness 1.7, pickaxe tier 0, light 15, gold translated tooltip. No block entity or nonzero accessor size is assigned to this block. The modern block keeps correct-tool drops and a vanilla self-drop loot table.
- Legacy vanilla `Block.setLightLevel` scales the upstream value 1.0 by 15. Legacy `setHardness(1.7)` raises internal resistance to hardness times five; `getExplosionResistance` divides by five. Modern `.strength(1.7F)` preserves effective resistance, rather than introducing an 8.5 resistance. Source inspected at `https://github.com/KealJones/mc-1.12.2-source_files/blob/master/src/minecraft/net/minecraft/block/Block.java`; this is a source-level comparison, not an explosion gameplay test.
- `CreativeTabAA.java`: preserves the shaped-quartz icon and translated tab title. Only the currently ported entries are shown; the full upstream creative inventory/order remains incomplete.
- `recipes/storage_shaped_quartz.json`: nine shaped quartz to one block, **shapeless**, not a new shaped recipe.
- `recipes/destorage_shapedquartz.json`: one block to nine shaped quartz. Both recipe IDs are preserved.
- `lang/en_us.lang` and `lang/pt_BR.lang`: selected existing names/tooltips/tab title converted to modern JSON keys; Portuguese source BOM is decoded, not included in its first translation key. Existing text is preserved, including references to not-yet-ported upgrades.

## Resource and loader adaptations

Shared `init/ContentRegistry.java` owns definitions. Fabric registers natively during initialization; Forge and NeoForge use deferred block/item/tab registration on their mod buses. This quartz slice added no loader library or optional gameplay dependency; the later Patchouli backend is a required external dependency.

Canonical resources use 1.21 singular `recipe`, `loot_table` and `tags/block` directories. The 1.20.1 resource tasks map them to plural directories and expand recipe result `item` instead of 1.21 `id`. Expansion values are explicit Gradle task inputs. Production JAR tests reject opposite-version directories.

MIT upstream license and credits were inspected before import. `quartz-assets.json` records exact source/destination paths and SHA-256 values for three unchanged model JSONs, two PNGs and two animation sidecars. No embedded libraries or third-party code from the upstream credits were imported. The existing artifact notice checks remain required.

Legacy Forge-marker blockstates were replaced with an equivalent vanilla blockstate. Real client logs then exposed missing textures on all four leaves: the original `textures/items` and `textures/blocks` paths are not stitched by modern default atlas directories. Explicit `minecraft:single` sources in `assets/minecraft/atlases/blocks.json` preserve the original paths, models, textures and eight-frame interpolation. The client checker now rejects those mod model/texture warnings. Startup alone had previously passed despite them.

## Historical quartz verification

- Resource RED: `python3 scripts/verify_quartz_resources.py` exited 1 before implementation because `data/arcanearchives/recipes/storage_shaped_quartz.json` was missing.
- Initial matrix build: `timeout --foreground 10m ./gradlew build --no-daemon`, exit 0, 24s, `build/quartz-build-20260907-173913.log`.
- Alternate-state matrix: switch to 1.20.1 Forge, build all leaves, run both artifact checkers, reset to canonical 1.21.1 Fabric. Exit/restore/source diff/controller diff all 0, 21s, `build/quartz-roundtrip-20260907-174519.log`; snapshot `/tmp/arcane-quartz-roundtrip-9c2MZ8`. This round trip preceded the additive atlas JSON fix.
- Atlas GREEN matrix build: same timed build command, exit 0, 15s, `build/quartz-atlas-green-20260907-174703.log`. Both artifact checkers passed every target. Existing JUnit XML reports contain 11 passing tests per leaf, with no failures/errors/skips; those are utility tests, not quartz gameplay assertions.
- Final post-atlas alternate-state matrix repeated the switch/build/both-checkers/reset sequence: exit/restore/source diff/controller diff all 0, 21s, `build/quartz-final-roundtrip-20260907-175342.log`; snapshot `/tmp/arcane-quartz-final-iW7Bfp`. Canonical source/controller bytes were restored exactly. `git diff --check` passed; changes remain uncommitted.
- Server command: `python3 scripts/smoke_servers.py --quartz`. Each subprocess runs `timeout --foreground 10m ./gradlew :<leaf>:runServer --no-daemon --console=plain`. It waits for readiness, reloads recipes, places quartz in a test fixture at Y=300, checks block and mining-tag identity, runs wooden-pickaxe loot into a chest and verifies exactly one result, inserts and verifies the shaped-quartz item, removes fixture blocks/forced loading, then requires normal shutdown and saved dimensions. Use only the isolated ignored development worlds, never a real player world.
- Client command per leaf: `xvfb-run -a --server-args='-screen 0 1280x800x24 -nolisten tcp' python3 scripts/smoke_clients.py <leaf>`. Each wraps its Gradle client task in the same ten-minute deadline. The following runs passed the stronger mod-model check after the atlas fix. Screenshots remain outside the repository.

| Leaf | Check | Exit | Duration | Complete log |
|---|---|---|---|---|
| 1.20.1-fabric | server | 0 | 23.3s | `build/smoke-server-1.20.1-fabric-20260907-174052-326061.log` |
| 1.20.1-forge | server | 0 | 26.06s | `build/smoke-server-1.20.1-forge-20260907-174115-627547.log` |
| 1.21.1-fabric | server | 0 | 21.05s | `build/smoke-server-1.21.1-fabric-20260907-174141-691002.log` |
| 1.21.1-neoforge | server | 0 | 18.29s | `build/smoke-server-1.21.1-neoforge-20260907-174202-739336.log` |
| 1.20.1-fabric | client | 0 | 22.7s | `build/smoke-client-1.20.1-fabric-1788803238391764343.log` |
| 1.20.1-forge | client | 0 | 23.7s | `build/smoke-client-1.20.1-forge-1788803261174360466.log` |
| 1.21.1-fabric | client | 0 | 22.7s | `build/smoke-client-1.21.1-fabric-1788803284955202799.log` |
| 1.21.1-neoforge | client | 0 | 22.21s | `build/smoke-client-1.21.1-neoforge-1788803307740193744.log` |

## Still required — do not claim parity

- Actual player crafting, incorrect-tool mining, light propagation and explosion behavior, interactive creative-tab/tooltips and frame-by-frame visual comparison. Command-generated loot is not player mining or crafting.
- Multiplayer, block/item persistence across restart and real inventory transfers. Saving the test world at shutdown does not establish these contracts.
- Acquisition from raw quartz through the Gem Cutter, dependent recipes, advancements, tank/trove upgrades and all other progression. No alternative acquisition recipe was invented.
- Raw-quartz chest conversion and its separately approved remainder-conservation correction are not implemented in this slice.
- Full storage, networks, machines, guidebook and wearable integrations remain open. No gameplay deviation was introduced or approved by this content slice.
