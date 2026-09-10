# Native capacity upgrade menu

Status: partial upgrade-interface implementation under the previously recorded delegated behavior-decision authority. Applies to Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Compilation is not gameplay acceptance.

Original: pinned release `bb99accf48ed583e29b0efae56e28c963407b8df`, `inventory/ContainerUpgrades.java` and `client/gui/GUIUpgrades.java`, provides three ordered size slots, three optional slots, native player inventory and shift transfers. Menu access is unconditional upstream; the original interaction includes scepters. Original pretty GUI texture regions and size-slot/player-slot coordinates are retained by the implemented screen.

Implemented: a native shared server-owned menu connects the existing ordered size handler to actual insertion/removal, ordinary clicks and shift transfers. Existing handler extraction rules forbid removing a prerequisite or reducing capacity below stored contents. Native client menus hold only synchronized preview slots. Live-device access is checked when opening, clicking and transferring. Detached destination stacks are changed using `Slot.safeInsert`, not vanilla in-place destination merging. Shift-extracted one-item upgrades are removed only after a successful player-inventory move; a full inventory leaves the upgrade installed.

Interaction deviation: sneak-use with both hands empty opens this capacity menu on Troves/Tanks. This makes installed upgrades removable before scepter migration; it is an additive migration interaction, not a claim that upstream scepter behavior is implemented. New English/Portuguese title and discovery tooltip describe this interaction. Slot ordering is native menu-internal, not upstream packet-compatible.

The optional row is intentionally not drawn or backed by fake inventory. Optional upgrades, scepters, simple-GUI configuration, and their eventual combined interface remain unfinished. Original source textures are recovered by `scripts/port_upgrade_assets.py` from the release pin; existing differing resources are not overwritten. No new library dependency or substitute artwork.

Reason: expose real capacity upgrade removal and preserve the existing conservation guards while progressing reachable storage gameplay. Alternative rejected: leave upgrades permanently installed until all networks/tools are migrated; show unimplemented optional slots as functioning storage.

Verification: four-target `assemble --no-daemon` passed after menu registration and asset recovery (`build/upgrade-menu-20260908-183451.log`, exit 0, 18s). Final compilation after tooltip additions is recorded in HANDOFF. No unit/gameplay/visual test campaign; full-slot transfer, capacity rejection, every click mode, persistence, simultaneous automation and multiplayer acceptance remain deferred to the requested post-implementation phase.
