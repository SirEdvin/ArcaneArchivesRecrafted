# 0020 — Fresh-world crafting result persistence

Status: implemented under the user's delegated behavior-change authority; runtime owner and crash-safe delivery remain pending.

## Upstream and decision

Pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`, `tileentities/GemCuttersTableTileEntity.java:29-30,123-141` has 18 inputs and a separate one-slot output handler; the inspected save/load methods store input inventory and recipe index, not a durable consumed-input/result record. This is source evidence, not a reproduced legacy crash-loss report. The port's 0019 already pairs detached output with consumed stacks; these must remain available to future remainder processing.

Add a strict, versioned fresh-world NBT encoding to `GCTCraftingResult`, reusing the existing inventory codecs rather than duplicating NBT/components branches. Root fields are integer `Version=1`, compound `Output` (one-slot extended inventory), and compound `Consumed` (18-slot native-capacity Gem Cutter inventory). Consumed entries occupy contiguous slots in original list order. Zero consumed inputs are valid; output must not be empty. Keep positive output counts exactly, including outputs larger than a native stack, since recipe definitions already permit them. Delivery must split or reject according to the eventual destination policy, not silently truncate them.

Decoding uses temporary owners, rejects unsupported/mistyped/missing fields, empty output, malformed item inventories, gaps, duplicate slots and oversized consumed inputs before returning a result. Constructor validation applies the same result invariants: at most 18 nonempty consumed stacks, each within native capacity. Valid results produced by the owned input handler are unaffected. Caller-owned tags and live inventories are never replaced or mutated by decoding.

## Scope and alternatives

All four targets: Fabric 1.20.1/1.21.1, Forge 1.20.1, NeoForge 1.21.1. New fresh-world format only; no old-save importer or cross-loader/version save compatibility claim. No network packet, recipe cost, texture, dependency or runtime registration change.

Alternatives rejected: retain results solely in memory (cannot recover remainder inputs); serialize only the output (loses container/tool input data); duplicate item codecs (risks version/count divergence); permissive partial recovery (silently loses state).

Serialization is not server authority or a single-use token. The runtime owner must save this alongside deducted inputs, retain it until delivery is resolved, and design output/remainder progress together to prevent replay and crash loss. No block entity currently saves this record; no actual restart, delivery, tool/fluid transformation or full crafting transaction is claimed. The record contains original consumed stacks, not already-processed remainders.

## Verification

Five new tests cover output count 300 with creator metadata, consumed count 64 and damaged-tool data/order, detached round trips with nonmutating reads, zero/18 consumed entries, malformed format and contents, and invalid constructor states. No production workaround or new dependency was required.

Command: `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:compileTestJava assemble --no-daemon` with full silent logging; exit 0 in 26s, `build/crafting-result-persistence-20260908-055839.log`. XML totals: 111 tests per Fabric/NeoForge leaf, zero failures/errors/skips. Forge test sources compile; Minecraft-backed JUnit execution remains unresolved. Four production/source artifact pairs pass `scripts/verify_artifacts.py`; `git diff --check` passes. These are codec/packaging checks, not runtime persistence acceptance.
