# 0090 — Reachable storage advancement branch

Status: implemented; four-target assembly/package and NeoForge native codec verification pass. Source-backed restoration under standing approval, all four targets.

Original release bb99accf48ed583e29b0efae56e28c963407b8df contains 24 advancement definitions. Restore 18 with already registered items and a closed parent graph: root, resonator, raw_quartz, raw_quartz_cluster, slivers, chest, workbench, gemcutters_table, tank, trove, amphora, scepter_revelation, scepter_manipulation, matrix_brace, containment_field, material_interface, shaped_quartz_block, devouring_charm.

Preserve original inventory_changed criteria (acquisition, despite crafting wording), automatic location root, parent IDs, display text, background and default rewards/notifications. Adapt icon keys and item predicates to modern codecs and advancement directory names to each target. No new rewards, recipes or gating. Original MIT notice remains packaged.

Six definitions remain pending their actual runtime items/integration: bonfire, manifest, lectern, monitoring_crystal, network, guidebook. Do not register invalid references or reinterpret them as dormant exclusions.

English titles/descriptions are copied verbatim. Every corresponding Portuguese value present upstream is identical to English; native en_us fallback supplies that same text without duplicating the strings. Upstream Portuguese lacks some keys, also covered by the normal fallback. Cached vanilla advancement resources were inspected for target icon and folder conventions.

Verification: `timeout --foreground 10m ./gradlew assemble :1.21.1-neoforge:test --tests '*StorageAdvancementsTest' --offline --no-daemon`, exit 0, 29s, `build/storage-advancements-20260909-190229.log`. One NeoForge test (zero skips/failures/errors) decodes all 18 definitions through the native Advancement codec with registered item holders, nonempty icons and closed parent references.

`timeout --foreground 60s python3 scripts/verify_artifacts.py`: exit 0, 0s, `build/storage-advancements-artifacts.log`. The permanent verifier checks all 18 processed definitions, target-specific icon keys/folders, parent references, title/description keys and root background in each of four production jars. All production/source contracts pass; `git diff --check` passes. Actual grant/revoke notifications, advancement UI, multiplayer and persistence remain runtime acceptance. No client/server launched; codec acceptance on the other loaders is not established by the NeoForge fixture.
