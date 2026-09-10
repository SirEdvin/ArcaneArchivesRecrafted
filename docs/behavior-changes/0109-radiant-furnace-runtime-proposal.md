# 0109 — Radiant Furnace runtime decision

Status: **superseded and rejected for implementation** by the user's [approved full-feature exclusion](0110-radiant-furnace-exclusion.md). The proposal below is historical source analysis only; its requests and acceptance plan are no longer active. No furnace block, entity, menu, duplication producer or tick-logic change was implemented for this proposal.

## Original behavior and reachability

All mod source references below use development pin `80944ce45c6559243d8928cc4b305bf379388652`; paths are relative to `src/main/java/com/aranaira/arcanearchives/`.

- `init/BlockRegistry.java:53,110,131` constructs and registers the Radiant Furnace block and assigns a normal ItemBlock. `ItemRegistry` includes that block item.
- `blocks/RadiantFurnace.java:195-207` explicitly declares tile entities and directly constructs the base or accessor entity according to the block's BASE/BOTTOM/TOP state. They are not merely unused singleton declarations.
- Nevertheless, `init/BlockRegistry.java:87-88,148-152` instantiates both furnace tile types but omits them from its tile-registration list. Searching the pinned Java source for `registerTileEntity` found the registry loop as the sole call site. This is a registration gap; original-client/runtime consequences have not been reproduced here.
- The block has three parts, horizontal facing, a half-height upper accessor, native interaction/menu routing and inventory-drop behavior. It must not be replaced with a decorative no-op block merely because its tooltip warns that it is unfinished.
- `tileentities/RadiantFurnaceTileEntity.java` has real server-side cooking logic and four inventory slots: fuel, input, ordinary output and Echo output. Its timing constant is `BURN_TIME = 200`, with `getMaxCookTime()` returning `BURN_TIME + 1`. Side wrappers expose input/fuel or output according to the original block/accessor rules.
- `RadiantFurnaceTileEntity.update()` calls `DuplicationUtils.shouldDuplicate` and `EchoItem.echoFromItem` on its production path. `proxy/CommonProxy.java:68-72` calls `DuplicationUtils.init()` during load completion. The ore predicate is not inherently an uninitialized, dead helper.

### Correction to the earlier scope shorthand

`0080-echo-item-state.md` correctly recorded that the furnace tile lacks registration and left furnace activation outside that Echo-item slice. However, "unregistered" alone is insufficient to establish that the entire furnace behavior is unreachable: the registered block directly constructs those classes. This audit supersedes a blanket dormant classification, but does not claim an original runtime test, complete furnace functionality, or approval to silently repair its logic.

The prior decision to leave genuinely dormant/unregistered features disabled still applies. This proposal requests an explicit decision on restoring this registered-but-broken machine, not broad permission to activate unrelated unfinished content.

## Confirmed source-level fuel rollover defect

In `RadiantFurnaceTileEntity.java:87-160`:

1. Existing burn time is decremented before the cooking eligibility guard.
2. The continuing-cook branch requires both `isCooking()` and `isBurning()` at line 104.
3. Inside that branch, `shouldConsumeFuel` is set when `!isBurning()` at lines 119-120, but nothing in that branch changes burn time. That refill condition cannot become true while the enclosing burning condition remains satisfied.
4. After fuel expires mid-cook, the continuing-cook branch is skipped. The new-recipe branch at line 130 is also skipped because cook time remains positive.
5. `shouldConsumeFuel` remains false, so the fuel-consumption gate at line 151 never requests another available fuel item. The same unchanged-input state repeats on subsequent updates.

This is a control-flow deduction from the actual pinned method, not a claimed original-Minecraft runtime reproduction. A native regression case must reproduce the boundary before implementing the correction.

Separately, the same method extracts a fuel item at line 160 even when its burn value was not positive, and its Echo-output growth path has no maximum-stack check. Those findings require conservation checks under the user's existing permission for reasonable gameplay validation; they are not permission to rebalance outputs or invent acquisition.

## Proposed behavior

Restore the existing furnace as one usable, verified feature rather than shipping an inventory/timer stub:

- Register modern base/accessor entity types, retain the original block/item/state structure, art, menus, sided inventory exposure, persistence and existing cooking/Echo behavior.
- Correct the fuel-rollover gate so a pending valid cook can request valid replacement fuel after its previous fuel expires, without discarding accumulated cook progress. This changes the original tick control flow and therefore requires approval beyond merely adding validation.
- Apply the already-approved modern ore-input compatibility contract from `0104-echo-ore-cache-compatibility.md` on the authoritative server for the original ore-to-Echo production path. Client tint caches must not authorize gameplay. Do not invent a second, unrelated ore-selection rule.
- Add conservative validation before world/inventory mutations: do not overwrite occupied accessor locations, delete invalid fuel, overflow outputs or consume inputs for outputs that cannot be retained. Preserve contents through part removal, chunk unloading and restart. Document concrete checks during implementation.
- Preserve timing/defaults and existing reachable acquisition unless a further source/API incompatibility requires a separate decision. Do not implement unrelated matrix, gemstone or Immanence features merely because the furnace references shared APIs.

## Reason and affected targets

A byte-for-byte control-flow port would carry the identified mid-cook fuel stall. A modern functional port also needs explicit entity registration and a server-side replacement for the removed Ore Dictionary. These are material runtime/scope decisions, not formatting or asset conversion.

Affected targets: Minecraft 1.20.1 Fabric/Forge and 1.21.1 Fabric/NeoForge.

## Approval requested

Approve restoring the registered Radiant Furnace's existing cooking and Echo-producing behavior with the entity-registration repair, server-authoritative ore compatibility, and minimal fuel-rollover correction described above. Existing authorization for reasonable conservation/validation checks remains in force. No unrelated dormant feature activation is requested.

Until approved, do not register a partial/decorative substitute or silently select the broken-versus-repaired cooking semantics.

## Verification and acceptance still required

Source review completed: block, base/accessor tiles, full block-registration list, CommonProxy initialization and DuplicationUtils were read from the pinned Git objects; previous scope records were checked for conflicting assumptions. No production code was changed, so no new build or runtime success is claimed.

Implementation acceptance after approval must include a genuine regression at the fuel-expiry boundary, ordinary and ore smelting, both output-capacity boundaries, invalid fuel/conservation, accessor placement/removal and side routing, menu access, persistence/restart, resource/item transforms, all four loader leaves and both Stonecutter active states. Connected-player and multiplayer acceptance must remain separate from compilation and command-fixture results.
