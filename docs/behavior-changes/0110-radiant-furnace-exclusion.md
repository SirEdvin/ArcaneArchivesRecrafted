# Radiant Furnace — excluded from Arcane Archives Recrafted

#Project/ArcaneArchivesRecrafted

## Decision

User-approved: exclude the entire Radiant Furnace feature from the migration on all four targets (1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge). Do not restore its block/item, base/accessor entities, GUI, automation, recipes or furnace-driven Echo production. This supersedes proposal [0109](0109-radiant-furnace-runtime-proposal.md); it is not a deferred implementation task. The independently registered Echo item and its tint/state support are not removed by this decision.

## What it was meant to do

A fuel-powered, three-part smelter using ordinary furnace recipes. Four slots hold fuel, input, normal output and an extra Echo output. Eligible ores produce their normal smelting result plus an Echo representing that result, selected through an editable ore list. An Echo is a typed item, not automatically a second ordinary ingot; its item class supplies no right-click redemption action. Accessor parts provide sided automation.

## Why exclude it

Upstream labels it unfinished. Its registered block constructs furnace entities, but their types are omitted from tile registration. Source tracing also identifies a mid-cook fuel-refill stall, unconditional extraction of invalid fuel in the tick path, and unchecked Echo-stack growth. These are source-level findings, not an original-runtime reproduction. Completing the machine would require functional repairs beyond straightforward preservation.

## Evidence and verification scope

Upstream development pin: `80944ce45c6559243d8928cc4b305bf379388652` in AranaiRa/ArcaneArchives. Relevant classes: `RadiantFurnace`, `RadiantFurnaceTileEntity`, `RadiantFurnaceAccessorTileEntity`, `ContainerRadiantFurnace`, `EchoItem`, `DuplicationUtils`, plus `BlockRegistry` and `CommonProxy`.

The current port has no furnace implementation to delete. Its pending migration work is closed as an approved exclusion, not completed gameplay parity. Historical upstream inventory/registry/book snapshots remain evidence, not instructions to reintroduce the feature. Future guidebook conversion must omit its placeholder entry and links.
