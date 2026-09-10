# Scintillating Inlay content slice

## Real-loader registration verification

The existing `scripts/smoke_servers.py --quartz` fixture now places 64 Inlay and 64 Radiant Dust in distinct chest slots, checks exact item IDs/slots/counts, and retains the quartz placement/loot, Patchouli registration, reload and cleanup checks. This uses the already approved isolated development worlds and existing loader lifecycle, not a new JUnit harness or registry workaround. All four servers passed and saved/stopped; a subsequent process check found no remaining Java command lines containing this project path. Exact JSON sidecars alongside these logs were read back and all checks verified:

| Target | Exit | Seconds | Log |
|---|---|---|---|
| 1.20.1-fabric | 0 | 22.30 | `build/smoke-server-1.20.1-fabric-20260908-053654-629231.log` |
| 1.20.1-forge | 0 | 25.04 | `build/smoke-server-1.20.1-forge-20260908-053716-929991.log` |
| 1.21.1-fabric | 0 | 19.29 | `build/smoke-server-1.21.1-fabric-20260908-053741-973356.log` |
| 1.21.1-neoforge | 0 | 17.79 | `build/smoke-server-1.21.1-neoforge-20260908-053801-260102.log` |

Each invokes `timeout --foreground 10m ./gradlew :<target>:runServer --no-daemon --console=plain`. This establishes dedicated-server registration and command insertion/storage of a native full stack after reload, including Forge. It does not verify tooltip rendering, creative-tab display, stack limits above 64, acquisition, restart persistence or multiplayer gameplay. It does not resolve Forge's Minecraft-dependent JUnit harness. Historical coverage limitations below are superseded only for this narrow registration check.

## Item behavior fixture limitation

A follow-up attempted ordinary JUnit tests constructing ScintillatingInlayItem after Minecraft bootstrap to inspect stack defaults and tooltip behavior. Both Fabric targets failed before assertions: `IllegalStateException: This registry can't create intrusive holders`, from `MappedRegistry.createIntrusiveHolder` through the Item constructor. Log `build/inlay-behavior-20260908-053447.log`, exit 1 in 16s. This is invalid fixture lifecycle, not evidence of an item behavior defect. The newly authored fixture was removed, with no production changes or registry bypass. The existing scoped matrix command passed again, exit 0 in 7s, `build/inlay-fixture-rollback-20260908-053547.log`. No new item behavior coverage is claimed. Future checks must use the item registered in the real loader lifecycle rather than construct registry entries after bootstrap; dedicated loader-aware testing setup remains separate work.

Implemented: `arcanearchives:scintillating_inlay`, shared item registration and creative-tab inclusion, ordinary Item defaults, gold translatable tooltip, original English/Portuguese names and tooltip text. No intentional gameplay deviation; modern namespaced translation keys and explicit atlas stitching follow the existing item port convention. The original texture spelling `item_component_scintillantinginlay` is preserved exactly.

Upstream baseline: `80944ce45c6559243d8928cc4b305bf379388652`. Sources: `items/ScintillatingInlayItem.java:14-25`, `items/templates/ItemTemplate.java:10-23`, `init/ItemRegistry.java:67,115,129`, English language lines 26/241 and Portuguese lines 20/227. This item is registered upstream, not dormant. `init/RecipeLibrary.java:131` declares one inlay from six Radiant Dust, twelve redstone dust, one gold ingot and six gold nuggets. That Gem Cutter recipe and its consumers remain pending; no substitute crafting-table recipe is introduced.

The model, PNG and animation metadata were copied from pinned Git objects without modification. SHA-256 values, also enforced against every production JAR by `scripts/verify_quartz_resources.py`:

- `models/item/scintillating_inlay.json`: `a9f92d950688054d83c7b0faf53636b06553b47dffc5474cbd75639745da5a4c`
- `textures/items/item_component_scintillantinginlay.png`: `0c7c7889b9104bf98d6ebcff025abfbe050aa313026731ffb3fa60c06dda76f7`
- `textures/items/item_component_scintillantinginlay.png.mcmeta`: `c822d44d5983d3a221e71f1adbcbfa66a03cb12e777ae41a7f7317fc0ab87992`

Paths above are under `assets/arcanearchives/`. Animation preserves interpolation, frametime 5 and frames 0–7. Existing upstream license/credits remain intact and packaged; no substitute art or dependency was added.

Verification: `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon`, exit 0 in 26s, `build/scintillating-inlay-20260907-225447.log`. Artifact and quartz/dust/inlay resource checks pass for all four targets; `git diff --check` passes. Forge Minecraft-dependent JUnit execution remains unresolved. This verifies compilation, packaging and resource preservation, not live registration/rendering, acquisition, or complete progression. Broad gameplay acceptance is still deferred.
