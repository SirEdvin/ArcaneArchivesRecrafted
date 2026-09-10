# Radiant Dust content slice

## Upstream and scope

Pinned source: `80944ce45c6559243d8928cc4b305bf379388652`, `items/RadiantDustItem.java:14-25`, `items/templates/ItemTemplate.java:13-22`, and `init/RecipeLibrary.java:76`. The item is reachable as `ItemRegistry.COMPONENT_RADIANTDUST` and the Gem Cutter's raw-quartz-to-two-dust output. Its item class has only ordinary Item behavior plus a gold crafting-ingredient tooltip.

The shared `RadiantDustItem` and `ContentRegistry.RADIANT_DUST` preserve `arcanearchives:radiant_dust`, ordinary stack capacity and the gold tooltip on all four targets. Added to the existing creative tab. No custom abilities, replacement crafting recipes or raw-quartz placeholders were added. The real Gem Cutter recipe remains unimplemented until raw quartz and crafting runtime are ready.

Original model/PNG/animation references are retained under the original `textures/items` path. Explicit modern atlas registration keeps that legacy path reachable. English and Portuguese strings are preserved; the item-name key is mechanically adapted to modern `item.arcanearchives.radiant_dust`. This is an API/resource translation, not a gameplay deviation. Existing upstream MIT notice packaging remains unchanged.

PNG SHA-256: `9b616bd6c48f592a08aa8c034f1d2b3102c05ef830d0e2f100d504bb57915835`, extracted directly from the pinned Git object, not the mutable reference tree. Model points at `arcanearchives:items/item_component_radiantdust`; animation retains interpolation, frametime 20 and frames 0–7. The resource checker validates these contracts in every production JAR, including both translations and atlas presence.

## Verification and limits

`timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon` exited 0 in 25s; log `build/radiant-dust-20260907-221428.log`. Both Fabric test tasks and NeoForge pass; Forge test-source compilation only (existing Minecraft-backed harness blocker remains).

`python3 scripts/verify_artifacts.py`, `python3 scripts/verify_quartz_resources.py` and `git diff --check` pass. All four production/source artifact pairs contain the new class; all four production JARs pass dust/quartz resource assertions. These are compile/package/resource checks, not a new client-rendering or runtime registry acceptance test. No new dedicated item JUnit fixture was added. Gameplay acquisition, runtime creative-tab display, animation rendering and full progression acceptance remain pending. The migration is unfinished.
