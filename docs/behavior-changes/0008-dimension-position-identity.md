# 0008 — Stable dimension-aware position identity

Status: shared value/codec implemented under delegated behavior-change authority; network integration and full runtime verification pending.

## Source and scope

Pinned upstream `80944ce45c6559243d8928cc4b305bf379388652`, `types/BlockPosDimension.java:13-49`: public mutable BlockPos and numeric dimension fields; equality and hashing include both. The constructor retains its caller's BlockPos reference. `util/ManifestUtils.java:128-158,186` uses these values in a HashSet to deduplicate inventory locations, including monitoring-crystal targets. Mutating a coordinate after insertion can invalidate hash lookup. This is a source-based finding, not a reproduced legacy-game incident.

The upstream class has no NBT serializer. `util/NBTUtils.java` contains UUID/recipe/default-int helpers, not a position codec. Do not claim a new serializer preserves an upstream position wire format.

## Decision

Preserve exact position-plus-dimension identity and coordinate getters. Make fields final and take an immutable BlockPos copy at construction, so a caller's mutable cursor cannot change a stored key. Modern dimensions use ResourceKey<Level>; only the dimension registry is accepted. This is a source API adaptation, not binary compatibility with the original public mutable fields.

Fresh-world persistence stores explicit integer x/y/z and a fully namespaced dimension string. Avoid packed-long coordinate truncation and numeric-dimension conversion. Missing/wrong-typed fields, invalid identifiers and implicit-namespace identifiers fail explicitly; never substitute zero coordinates or the overworld. A valid but currently unavailable dimension key is preserved unchanged: decoding does not resolve a world, load chunks, or erase a link. Runtime consumers must separately check dimension availability, world bounds, permissions and chunk availability before using a stored position.

All four targets are affected. No existing ported network or position save schema is changed: those consumers are not implemented yet. No old saves/importers, numeric-ID lookup or cross-loader conversion are introduced. No textures or assets change.

Alternatives: preserve mutable hash keys (unsafe for the traced HashSet use); use packed positions (would restrict lossless integer round trips); silently fall back to overworld for unknown keys (could target the wrong inventory). Rejected. A small source-compatible-name value class retains upstream coordinate access; it does not introduce a separate dimension registry or world resolver.

## Verification boundary

Tests cover dimension-sensitive equality/hash collection behavior, mutation of constructor input, coordinate round trips including extreme integers, preservation of unknown namespaced dimensions, and rejection of malformed/legacy numeric data. Block-entity construction helpers, actual network save integration, restart/multiplayer and unavailable-dimension gameplay behavior remain pending. Results will be appended after execution.

Executed `timeout --foreground 10m ./gradlew :1.20.1-fabric:test :1.21.1-fabric:test :1.21.1-neoforge:test :1.20.1-forge:test --tests '*BlockPosDimensionTest' assemble --continue --no-daemon`: exit 1, 26s, log `build/dimension-positions-final-20260907-213548.log`. Both Fabric targets and NeoForge report 49 tests each, zero failures/errors/skips, including the five new position cases. Forge reports one initialization failure before the cases execute: the existing `NetworkEvent.<init>()` loader transformation problem. All four assemble tasks complete and production/source artifact checks pass, including the new class; `git diff --check` passes. This is not a successful unrestricted matrix build.

The first attempt, `build/dimension-positions-20260907-213443.log` (exit 1, 23s), exposed a fixture initialization omission on isolated Forge execution (`Not bootstrapped`). The fixture now explicitly initializes Minecraft, rather than depending on another test class running first; this exposes the known Forge loader-harness failure instead of concealing it.
