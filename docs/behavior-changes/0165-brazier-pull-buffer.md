# Brazier filtered pull mode

## Original behavior
The Brazier only routes deposits into permitted loaded network storage. Manipulation opens radius/network configuration. It has no output buffer.

## Approved behavior
User requested a second mode, changed with the Scepter of Manipulation, an item selected by right-clicking, no pulling without a filter, and explicitly selected buffered output.

Use normal Manipulation right-click to toggle Deposit/Pull; sneak-right-click retains the configuration screen. Pull-mode item right-click copies a one-item exact item/components filter without payment. Empty-hand sneak-right-click clears the filter; ordinary empty-hand right-click retrieves buffered items. Messages report mode/filter changes.

One ordinary stack output buffer, bounded by min(64, item maximum). Try one matching source slot once per second; never load chunks. Pull only from owned/permitted Radiant Chests and Troves within the existing horizontal radius and dimension, not crafting inputs, monitored foreign containers or another Brazier. No filter means no extraction. Depositing/absorption/automation insertion is disabled in Pull mode. Output remains extractable after mode/filter changes. Persist mode, filter and buffer; drop paid buffered items once on removal. Preserve exact source components and Trove lock references.

Automation exposes one output slot plus the existing virtual deposit slots on Forge/NeoForge, and a transactional output view on Fabric. No modifiable output setter is exposed through capabilities. Both permissions and current instance validity are checked server-side. Use existing silent transactional source writes to pay the source and update the destination before notifying neighbors.

## Targets and approval
Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge. Approved by the current feature request and buffer confirmation; small interaction/default adaptations use the standing authorization. No publication authorization.

## Verification
Implemented and verified through `timeout --foreground 10m ./gradlew build verifyReleaseArtifacts --no-daemon`: exit 0, 81 seconds, `build/brazier-pull-final-20260914-171051.log`. All four target builds/native suites and production/source artifact checks pass. Tome regeneration and all 25 script tests pass (`build/brazier-pull-tome.log`, `build/brazier-pull-python.log`).

Shared native coverage exercises scepter toggling, no-filter inactivity, nonconsuming filter selection, deposit rejection, unauthorized owner/radius denial, exact source/output conservation, full-buffer inactivity, filter clearing with retained output, mode switching, NBT state round trip, real native hopper extraction on every loader, simulated and actual automation extraction, Chest/Trove sources and exact paid drops on removal. The existing activation test now sneak-uses Manipulation to verify retained configuration access. The first run failed only that old interaction expectation (`build/brazier-pull-20260914-170635.log`); corrected runs pass.

Graphical input/feedback, real process restart of a buffered Brazier, extended protection-mod and multiplayer campaigns remain unperformed. This does not claim arbitrary-inventory remote extraction or full multiplayer acceptance.
