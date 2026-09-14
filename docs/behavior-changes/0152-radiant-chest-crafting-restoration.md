# Radiant Chest crafting restoration

## Original behavior

At audited development pin `80944ce45c6559243d8928cc4b305bf379388652`, `assets/arcanearchives/recipes/radiant_chest1.json` crafts one Radiant Chest from eight `plankWood` surrounding one raw quartz. `radiant_chest2.json` is shapeless raw quartz plus `chestWood`. The existing port implemented in-world conversion but omitted both crafting resources.

## Restoration and adaptation

Restore both original recipes on every target. Use `minecraft:planks` and a small `arcanearchives:ingredients/chests_wooden` compatibility tag with vanilla chest/trapped chest and optional `c:chests/wooden` / `forge:chests/wooden`. Preserve one result and native payment/remainders. This follows existing ingredient-tag adapters and does not alter in-world conversion. Book pages show both current native recipes.

## Approval

Original recipe restoration is within preservation scope. Ore-dictionary to modern tag adaptation is authorized by the user's instruction to finish implementation with reasonable adaptations and no unnecessary complexity.

## Verification

Native result-slot extraction/payment fixtures cover both vanilla chest variants and oak/birch plank recipes on all four leaves. Results pending the integrated checkpoint. Static artifact verification checks expanded recipe JSON against source on every leaf.
