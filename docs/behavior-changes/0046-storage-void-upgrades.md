# Devouring Charm upgrades for placed storage

## Scope and approval status

Implemented original-feature migration for Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. This restores the placed-storage VOID upgrade path; it does not remove requirements for keys/LOCK, item-form storage APIs, Parchtear, networks or gameplay acceptance. Server-authoritative access, validated saves and simulation/conservation boundaries remain required.

## Original behavior

Release pin `bb99accf48ed583e29b0efae56e28c963407b8df`:

- `items/DevouringCharmItem.java`: VOID upgrade supported by Troves and Tanks.
- `inventory/handlers/OptionalUpgradesHandler.java`: three one-item optional slots, distinct upgrade types, no SIZE upgrades.
- `inventory/handlers/ITroveItemHandler.java`: matching Trove overflow is accepted and discarded, including an insertion that crosses capacity; mismatched items are rejected.
- `tileentities/RadiantTroveTileEntity.java`: two automation slots, the second appearing empty, with shared real storage; optional upgrades persist separately from capacity upgrades.
- `tileentities/RadiantTankTileEntity.java`, `VoidingFluidTank.fillInternal`: voiding applies only when the Tank was already full and the incoming fluid matches. An insertion into a partially filled Tank uses ordinary capacity-limited filling; this is intentionally not changed into Trove-style crossing-capacity voiding.
- `client/gui/GUIUpgrades.java`: original optional row uses the existing upgrade texture at V=32, below the capacity row.

## Implementation

`StorageOptionalUpgrades` binds the existing validated optional handler to Devouring Charms. The upgrade preserves the actual Charm stack data, including its pickup filters; storage voiding follows the stored item/fluid identity, not those pickup filters. Only one VOID upgrade can be installed. Player access is checked by the owning block entity before direct installation; rejected installations do not become ordinary Trove deposits or open the handheld disposal menu.

`StorageUpgradeMenu` now exposes both rows with native cursor/shift transfers and safe detached-slot insertion. Sneak-empty-hand and Manipulation scepter entrypoints pass both handlers. Direct Charm use, including sneak-use through the item's native useOn path, installs an upgrade. Revelation's Trove report includes optional-upgrade counts. Existing original GUI art is reused, and localized text now describes both kinds of upgrade and warns about permanent overflow deletion.

Both block entities serialize `optional_upgrades` into normal saves, update tags and their existing packed block-item drops. Optional data is validated before publishing loaded state. Older migration saves without this field load with no optional upgrades. Removing the Charm disables voiding without deleting stored contents; capacity-removal conservation guards are unchanged.

Trove player/Forge-family insertion and Fabric transactional insertion implement matching overflow acceptance. Forge/NeoForge expose `TroveItemAutomation`, restoring the original empty second automation slot: inspected native `VanillaInventoryCodeHooks.isFull` would otherwise skip a full Trove before calling its voiding insert method. The adapter forwards to one real inventory and rejects stale-device extraction. Fabric retains its native transactional storage.

Tank native fill/insert methods preserve the original already-full rule and fluid-data matching. Simulation reports acceptance without changing storage or mutating the caller's fluid stack. Fabric rollback remains native: actual stored additions use the existing snapshot path; a full void sink changes no destination contents, while the caller's source extraction remains transactional. This does not introduce generic remote Amphora capabilities.

## Evidence and remaining acceptance

`./gradlew assemble --no-daemon`, under the repository timeout/full-log wrapper, exited 0 in 21s; Gradle reported BUILD SUCCESSFUL in 19s. All four compileJava tasks ran. Log: `build/storage-void-upgrades-20260908-202717.log`. `git diff --check` passed. New optional/automation/menu classes were confirmed in all four production JARs; artifact expectations were updated.

No test campaign, client/server launch, optimization, commit or publication. Persistence/replacement, hopper and pipe behavior, transaction rollback, UI rendering and multiplayer still require runtime acceptance after feature migration. Parent storage/Charm migration is not declared complete. Item-form automation and other optional upgrade types remain open.
