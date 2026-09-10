# Empowered Radiant Quartz — registered item and presentation port

Pinned source: AranaiRa/ArcaneArchives `80944ce45c6559243d8928cc4b305bf379388652`.

## Scope and provenance

`items/EmpoweredQuartzItem.java` defines `empowered_quartz` as an ordinary `ItemTemplate` with two warning tooltip lines: red/bold `notimplemented1`, red/italic `notimplemented2`. `items/templates/ItemTemplate.java` supplies registry identity and creative placement; no special use behavior or stack limit is defined. `init/ItemRegistry.java:44,115,129` establishes registration/model reachability, despite its warning text. The port preserves that reachable placeholder item rather than treating it as unregistered dormant content.

Shared `EmpoweredQuartzItem` and `ContentRegistry` now supply the same identity, native ordinary-item defaults, creative entry and styled translatable warnings on all four leaves. No ability, substitute acquisition recipe, balance change or dependency is introduced. No intentional gameplay deviation requires a new behavior decision. The legacy warning is preserved text, not a claim that a crash was reproduced in the port.

Original `en_us.lang` contains the item name and warnings. `pt_BR.lang` contains both warnings but no empowered-quartz name; only existing translations are copied, preserving English fallback rather than inventing a translation. Both animated texture layers and the item model are copied byte-for-byte from pinned Git objects; explicit modern atlas sources cover both legacy texture paths. Existing upstream MIT notice remains packaged.

`RecipeLibrary.java:187,189,193` references this item in Matrix recipes. Those consumers and acquisition/progression remain unfinished; registering this item does not establish those recipes or machines.

## Asset verification

`scripts/verify_quartz_resources.py` pins SHA-256 for all five copied assets:

- `models/item/empowered_quartz.json`: `59b11fd925ad26d56eec106b84fa4e575794158d4108989cbd9f0661635b31a3`
- `textures/items/item_empquartz.png`: `e60a2b3edd9dde82bd8803833582ba48096ed3940b764534ac8538cfb07b5ca5`
- `textures/items/item_empquartz.png.mcmeta`: `355f5731ba1d5c05146cca1888fcf53d3759fc38c0c66a6c1d7212947becc11a`
- `textures/items/item_empquartz_layer1.png`: `ff39194bb61e54a8873dc2299e5539a357f01d1fafef97019d2b53588fac10ff`
- `textures/items/item_empquartz_layer1.png.mcmeta`: `8b457f2f67a78c79b176a6d58b926590c8f69abdbc9b960c4a90aa8d40f56d64`

Base animation: interpolated, frame time 20, frames 0–7. Overlay: frame time 2, frames 0–3, no interpolation field. Model retains both layers unchanged.

## Scoped verification

`timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon`, full output redirected to `build/empowered-quartz-20260908-062826.log`: exit 0, 28s. No new JUnit fixtures are claimed for this item. Forge Minecraft-backed JUnit execution remains unresolved; its test sources compile only.

`python3 scripts/verify_artifacts.py` and `python3 scripts/verify_quartz_resources.py` each pass all four leaves; `git diff --check` passes. Artifact contracts now require the new production/source class; resource checks cover exact original asset hashes, atlas sources and translated strings.

## Dedicated-server registration follow-up

`python3 scripts/smoke_servers.py --quartz` now inserts 64 empowered quartz into chest slot 5 after data reload and requires the matching item/count/slot NBT marker. All four real loader servers passed, including the existing quartz placement/loot/mining-tag, other item and Patchouli checks. The existing preflight required already-accepted EULAs and loopback binding; no legal terms were accepted by the script. Fixtures were removed, chunks saved and servers shut down. Saved JSON reports were read back with all checks true; subsequent process inspection found no remaining project Java processes.

| Target | Exit | Seconds | Log under `build/` |
| --- | --- | --- | --- |
| 1.20.1 Fabric | 0 | 22.06 | `smoke-server-1.20.1-fabric-20260908-063048-692104.log` |
| 1.20.1 Forge | 0 | 25.30 | `smoke-server-1.20.1-forge-20260908-063110-749243.log` |
| 1.21.1 Fabric | 0 | 18.54 | `smoke-server-1.21.1-fabric-20260908-063136-047051.log` |
| 1.21.1 NeoForge | 0 | 17.79 | `smoke-server-1.21.1-neoforge-20260908-063154-587313.log` |

Each invocation uses `timeout --foreground 10m ./gradlew :<target>:runServer --no-daemon --console=plain`, logging full output silently. This verifies real dedicated-server registration and command insertion/storage of a full stack after reload, not rendered animation/tooltips, creative display, acquisition, over-limit rejection, restart persistence or multiplayer behavior. It does not resolve Forge's separate JUnit harness limitation. Broad gameplay acceptance remains deferred and the overall migration is unfinished.
