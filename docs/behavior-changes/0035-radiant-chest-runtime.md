# Radiant Chest native menu and automation

Status: implemented in part under the user's delegated behavior-decision authority; gameplay acceptance pending. Applies to all four targets.

Original: `RadiantChestTileEntity` owns 54 extended slots and ordinary physical access, supports item automation and comparator fullness, and drops stored contents on breaking. `ContainerRadiantChest` handles extended counts with custom packets and native-sized player/cursor stacks. Raw quartz sneak conversion drains the native chest (including a double chest), replaces the clicked block, assigns the player network and consumes one quartz outside creative.

Port: shared extended inventory, block/entity/menu registration, original release model/GUI/texture, native-sized unit display stacks plus split integer data slots, server-owned click/drag/shift/hotbar/throw handling, comparator output, ordinary-stack drops and persisted contents/owner/name. The menu validates a live nearby owning level instead of upstream's unconditional `canInteractWith`. Physical chest access remains public, not owner-locked.

Native storage adapters: Forge item capability, NeoForge block capability, Fabric slotted `SingleStackStorage` plus `CombinedStorage`. Exact replacement restores Fabric snapshots; dirty/comparator notifications run at final commit. Broccolium assessment is in `../migration/BROCCOLIUM.md`; its unsuitable fallback paths are not adopted or copied.

Quartz conversion implements the previously approved remainder-only drop correction (0003). Snapshot and clear the native inventory before block replacement to prevent vanilla duplicate drops; restore if replacement returns false. Insert into the new chest and drop only rejected remainders. Native containers supply ordinary and double-chest contents. This does not yet implement crafting-table conversion or prove compatibility with every modded chest/protection hook.

Outstanding: chest name editing/display-item scepter integration, network routing settings/defaults, complete connected-player click/drag/creative acceptance, arbitrary callback/replacement-failure conservation and restart/unload validation. The original GUI is present but not visually validated. No claim of full storage migration.

Compilation: all four `compileJava` tasks and assembly passed in `build/storage-integration-20260908-165131.log` (29 seconds, exit 0). Unit/gameplay/runtime tests deliberately deferred until implementation is complete, as the user requested.
