# Packed Trove inventory integration

The portable Trove stores items locally. It does not access a placed block or remote network. Only a single Trove item can be opened; stacked or invalid containers reject transfers. Existing owner, upgrades, item components and unknown packed metadata survive updates. LOCK retains its selected item even at zero contents; VOID consumes matching overflow only. Simulations do not select a lock reference or rewrite the item.

## Fabric 1.20.1 and 1.21.1

Use `TroveItemStorage.ITEM` (`com.aranaira.arcanearchives.inventory`). On 1.21.1 this is exactly `ItemStorage.ITEM`, not a separate lookup. On 1.20.1 it is the approved mod-owned `arcanearchives:item_storage` lookup with the same `Storage<ItemVariant>` / `ContainerItemContext` types. Other mods must explicitly query that identifier on 1.20.1.

For a Trove in a vanilla `Container` (including a chest or hopper), wrap the container using native `InventoryStorage.of(container, side)` and select its slot:

```java
var inventory = InventoryStorage.of(container, side);
var context = ContainerItemContext.ofSingleSlot(inventory.getSlot(slot));
var contents = context.find(TroveItemStorage.ITEM);
```

Use `ItemStorage.SIDED.find(...)` for world-facing access so native sided restrictions and vanilla inventory fallbacks remain in effect. Use `ContainerItemContext.ofPlayerHand(...)` for a player's actual hand. The adapter uses transactional context exchange: the updated Trove is written back to its containing slot, not merely to a detached copy. Commit related source/destination transfers in the same outer transaction; close without committing to abort. Never use `withConstant`/`withInitial` as the context for a real inventory transfer: those are detached simulation contexts and may discard overflow. Re-query if a consumer replaces the containing slot.

Vanilla chests/hoppers remain native inventory endpoints. No custom inventory class is required. No mixin modifies vanilla item-container behavior, and no hopper is made to automatically unpack a Trove in another inventory. Supporting an inventory as a container is distinct from adding an automatic portable-container processing mechanic.

## Forge 1.20.1 and NeoForge 1.21.1

Query the Trove stack's native `ITEM_HANDLER` / `Capabilities.ItemHandler.ITEM` capability. The handler exposes the original two-slot shape: slot zero reports actual extended contents, slot one reports empty, and both address the same real storage for transfers. Extraction is capped to the resource's native stack size. Returned stacks are defensive copies.

Native `InvWrapper` supports vanilla `Container` endpoints. Query the capability on the actual Trove stack in the inventory, not a detached copy; preserve native access/sided restrictions and notify the owning inventory/menu after changes. Operating on a copied Trove intentionally changes only that copy.

## Evidence and limits

NeoForge fixtures exercise the registered capability with vanilla `SimpleContainer`, `ChestBlockEntity` and `HopperBlockEntity`, moving pearls into and out of a packed Trove while preserving the Trove and native stack limits. These are detached inventory fixtures, not world-ticking hopper tests. Fabric registration compiles against both pinned API versions, but live Fabric transaction/abort tests remain pending. Item contexts provide no dynamic registry provider; unknown registry-dependent packed components fail closed, as with the Tank item adapter. Multiplayer, protection-mod and save/restart acceptance remain pending.
