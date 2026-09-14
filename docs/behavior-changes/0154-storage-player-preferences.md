# Storage player preferences

## Original behavior

At `80944ce45c6559243d8928cc4b305bf379388652`, ConfigHandler and PacketConfig synchronize `trovesDispense` (true) and `defaultRoutingNoNewItems` (false) to the player's server network. RadiantTroveTileEntity's actual formula is: true means ordinary click withdraws one and sneaking withdraws a native stack; false reverses it. Its configuration comment contradicts that formula. New Radiant Chests use the owner's default routing preference.

## Proposed behavior

Restore both client.properties options as `TrovesDispense` and `DefaultRoutingNoNewItems`, with original defaults. Synchronize only these two booleans on connection/player replacement. Scope server preferences to the authenticated player object with weak keys, use original defaults before synchronization, and never deserialize a supplied player identity. Preserve actual upstream withdrawal logic, not its contradictory comment. Apply the routing default only to new placements/conversions; explicit imported `routingType` data and existing world saves retain their routing state. Configuration remains restart-to-apply like adjacent preferences.

## Reason and targets

Restore missing controls with one bounded two-boolean C2S payload, not a general settings framework. All four supported targets.

## Approval

Authorized by the user's standing instruction to finish implementation with reasonable security/behavior adaptations without overcomplication.

## Verification

Config defaults/overrides/invalid-file preservation, two-byte packet encode/decode and authenticated apply, all four native withdrawal combinations, native Chest placement with default/explicit saved routing, and quartz conversion with exact contents/payment pass on every target. Final separate clean/build and alternate-state matrix builds plus artifact checks pass; see [checkpoint evidence](../migration/0.0.2-IMPLEMENTATION_CHECKPOINT.md). Connected input delivery remains separate acceptance evidence.
