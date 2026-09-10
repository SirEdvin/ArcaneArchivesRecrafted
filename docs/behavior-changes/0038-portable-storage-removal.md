# Preserve portable storage on native removal

Status: implemented under delegated authority; runtime acceptance pending. All four targets.

Pinned upstream release `bb99accf48ed583e29b0efae56e28c963407b8df`: RadiantTrove/RadiantTank custom harvest methods save inventory/upgrades onto a dropped block item. Creative harvest suppresses that drop; Trove creative breaking is specially rejected without a scepter. Non-harvest removal does not run those same portable-item harvest methods. Trove sneak-scepter harvest instead spills individual contents and upgrades.

Port policy: the live block entity's removal path creates one native block item containing its saved state, and has a one-time removal guard. An empty normal loot table avoids a duplicate plain block drop. This protects data on replacement as well as normal breaking, including creative native removal. The item restores its state through native BlockItem block-entity-data handling; placement assigns the new placer as owner.

This deliberately broadens portable preservation instead of reproducing separate loss-prone non-harvest paths. It does not claim atomic crash-safe world/drop persistence. Rejected alternatives: emptying a large Trove into unbounded loose entities by default; having both loot and removal emit a block; silently discarding creative/replaced storage. Creative attack interception and the explicit scepter spill operation remain unfinished gameplay, not silently reproduced by this policy.

Save format is fresh-world native block-entity data (NBT on 1.20.1, components on 1.21.1). Native item-form automation and client item contents rendering remain separate work. No legacy importer or network packet is added.

Evidence: both devices compile and assemble across all leaves. Player break/replacement/explosion, item placement, restart and denied-drop behavior have not been exercised yet; no parity claim is made.
