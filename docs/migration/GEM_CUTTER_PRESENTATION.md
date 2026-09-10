# Gem Cutter original presentation — runtime checkpoint

Status: original model integrated; gameplay milestone remains unfinished.

## Provenance and conversion

Source: https://github.com/AranaiRa/ArcaneArchives, `release/0.2.0.25-mixins8`, commit `bb99accf48ed583e29b0efae56e28c963407b8df`. See `gem-cutter-upstream.json` for the pinned asset hashes. This supplements, rather than silently rebases, the earlier master-based mechanics work.

`port_gem_cutter_assets.py` reproduces 13 resources after verifying the Git blobs against that manifest. The OBJ and seven PNGs (five materials and two GUI backgrounds) remain byte-identical. The only MTL edit maps `minecraft:blocks/stone_diorite` to `minecraft:block/diorite`. The upstream MIT notice remains packaged. Original GUI textures are now present for subsequent menu implementation, not evidence of an implemented screen.

The release OBJ contains 335 faces: 310 quads and 25 triangles, with six material mappings. No substitute geometry was authored. Legacy blockstate/item metadata is converted into modern blockstate variants, an empty accessor model and an item-parent model:

- Same authored two-block geometry; only the master renders it.
- West is the unrotated OBJ; north/east/south use native Y rotations 90/180/270, preserving the legacy facing convention.
- Cutout rendering, authored material UVs with `flip_v`, and explicit atlas entries for the five legacy mod texture paths.
- Legacy ordered Y/X/Z perspective rotations are converted mathematically to native XYZ Euler rotations; translations are converted from block units to JSON sixteenths. Scale is preserved. Two Python regressions check basis-vector equivalence and catch simple rotation relabeling. Upstream Forge 1.12 `ForgeBlockStateV1.TRSRDeserializer.parseRotation` and `ForgeHooksClient.handleCameraTransforms` were consulted for composition and hand mirroring.
- Forge and NeoForge use their already-installed native OBJ loaders. Fabric has one client-only vanilla-quad adapter for the OBJ records actually used by this asset; it is not a generic OBJ/MTL implementation. It reloads via the model-loading API and uses native sprite coordinates for the respective Minecraft version. No new dependency or common renderer framework.

The block name and gold description are copied from release `en_us.lang` and `pt_BR.lang`. The original Portuguese spelling is retained. No crafting ability, acquisition shortcut, creative-tab advertisement, balance change or intentional gameplay deviation was added.

## Execution evidence

Final scoped Java matrix (29s, exit 0):

`timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon`

Log: `build/gem-cutter-model-final-20260908-092040.log`. XML reports 137 tests per executable Fabric/NeoForge leaf, zero failures/errors/skips. Forge shared tests compile, but its Minecraft-backed JUnit harness remains unresolved.

`python3 -m unittest discover -s scripts -p 'test_port_gem_cutter_assets.py'`: two tests pass. Reproduction `--check` passes. `verify_artifacts.py`, `verify_gem_cutter_resources.py`, `verify_quartz_resources.py`, and `git diff --check` pass on the packaged matrix. The existing quartz verifier needed its explicit atlas list updated for the new original textures; no content check was removed.

All four private-Xvfb client startup/resource checks pass, without missing-mod-model/texture warnings. All four dedicated-server startup/reload/save/stop checks pass after client integration, confirming client classes are not loaded on dedicated servers. Exact commands, durations, logs and reread checks are consolidated in ignored `build/gem-cutter-model-verification.json`.

Actual visual checks used a newly created creative flat world, `AA Gem Cutter Model Test`, not a user world:

- 1.21.1 Fabric: placed world model, held item and creative-inventory icon inspected. A real right-click placement created the expected master/accessor pair; the server emitted `AA_GCT_PLAYER_PLACED`. F3+T resource reload retained the model. Log: `build/model-visual-client-1788858522955258925.log`.
- 1.21.1 NeoForge: inspected the same-version copy of that test fixture with the native OBJ loader, including placed/held rendering after F3+T, the inventory icon, English name and gold tooltip. Log: `build/model-visual-client-1788858987721479512.log`.
- Both clients saved their test worlds and stopped normally. Screenshots are outside the repository: `/tmp/arcane-gem-cutter-fabric-hand-model.png`, `/tmp/arcane-gem-cutter-fabric-reloaded.png`, `/tmp/arcane-gem-cutter-neoforge-world-model.png`, `/tmp/arcane-gem-cutter-neoforge-table-tooltip.png`.

This is not exhaustive pixel parity: 1.20.1 clients have startup/resource evidence, not in-world visual walkthroughs. All facings, left-hand/third-person displays, resource-pack overrides, alternative renderers and placement/re-placement edge cases remain to be exercised. The successful placement was one ordinary creative placement, not a packed-pending-table recovery test.

## Remaining active integration

Continue the same playable Gem Cutter milestone with the menu/screen, server-authoritative combined table/player inventory crafting, upstream recipe registration/reload, tool/fluid remainders and conserved output delivery. Pending state must not be granted through an inspection accessor. No parent gameplay checkbox is complete and no user-input blocker is identified.
