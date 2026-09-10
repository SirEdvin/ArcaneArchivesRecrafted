# Radiant Resonator runtime and placement accounting

Status: implemented under the user's delegated behavior-decision authority; gameplay acceptance pending. Applies to all four targets.

Original: `RadiantResonatorTileEntity.update` requires the owner network and an online owner, increments growth only with air above, and produces a raw cluster on the tick after growth reaches ResonatorTickTime. Nearby ocelots are flung; completion sound and comparator status are exposed. `ItemBlockTemplate` checks the per-owner placement limit. Master and pinned release differ in config/network package access, not this growth algorithm.

Port: registers block/entity/item, native shaped acquisition recipe, original release OBJ/PNG/sound assets, comparator, persisted owner/growth and server ticking. The malformed trailing brace in the release recipe is repaired without changing its pattern or ingredients. The modern split into Cat/Ocelot covers both descendants of the legacy ocelot type.

Necessary adaptation: a dedicated overworld position/owner index provides placement accounting before full network migration. Unloaded positions remain counted, and stale loaded positions are removed without forcing chunks. Only real connected server players can place an active owned device. This is stronger unloaded-position accounting than the old live-network totals; fresh-world NBT only. Growth resolves the actual online owner directly, not a completed Hive/immanence network.

Presentation adaptations: `%d` becomes supported native translatable `%s`; the duplicated Portuguese tooltip key embedded in its value is removed. Original growth period and comparator rounding remain unchanged.

Outstanding: full network/immanence integration, configurable sound gating and loop playback, tome grant on crafting, relocation and player/restart acceptance. Wonky Resonator's dormant TODO explosion is not enabled.

Compilation: all four `compileJava` tasks and assembly passed in `build/storage-integration-20260908-165131.log` (29 seconds, exit 0). This is not a gameplay test or full migration parity.
