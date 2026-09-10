package com.aranaira.arcanearchives.inventory;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.junit.jupiter.api.Test;

class TroveItemStorageTest {
    private static final RegistryAccess REGISTRIES = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    private static ItemStack empty() { return new ItemStack(ContentRegistry.RADIANT_TROVE_ITEM.get()); }
    private static IItemHandler handler(ItemStack item) {
        return java.util.Objects.requireNonNull(item.getCapability(Capabilities.ItemHandler.ITEM));
    }
    private static RadiantTroveBlockEntity device() {
        return new RadiantTroveBlockEntity(BlockPos.ZERO, ContentRegistry.RADIANT_TROVE.get().defaultBlockState());
    }
    @Test void registeredCapabilitySimulationAndCopiesAreIsolated() {
        ItemStack item = empty();
        var storage = handler(item);
        ItemStack before = item.copy();
        assertEquals(2, storage.getSlots());
        assertTrue(storage.insertItem(1, new ItemStack(Items.DIAMOND, 80), true).isEmpty());
        assertTrue(ItemStack.matches(before, item));
        assertTrue(storage.insertItem(1, new ItemStack(Items.DIAMOND, 80), false).isEmpty());
        assertTrue(storage.getStackInSlot(1).isEmpty());
        assertEquals(80, storage.getStackInSlot(0).getCount());
        storage.getStackInSlot(0).setCount(1);
        before = item.copy();
        assertEquals(64, storage.extractItem(1, 1000, true).getCount());
        assertTrue(ItemStack.matches(before, item));
        var copy = handler(item.copy());
        assertEquals(64, copy.extractItem(0, 1000, false).getCount());
        assertEquals(80, storage.getStackInSlot(0).getCount());
        assertEquals(64, storage.extractItem(0, 1000, false).getCount());
        assertEquals(16, storage.getStackInSlot(0).getCount());
    }
    @Test void vanillaContainerRoundTripPreservesNativeLimitsAndPackedItem() {
        for (net.minecraft.world.Container chest : new net.minecraft.world.Container[]{
                new SimpleContainer(27),
                new net.minecraft.world.level.block.entity.ChestBlockEntity(BlockPos.ZERO,
                    net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState()),
                new net.minecraft.world.level.block.entity.HopperBlockEntity(BlockPos.ZERO,
                    net.minecraft.world.level.block.Blocks.HOPPER.defaultBlockState())}) {
        chest.setItem(0, empty());
        chest.setItem(1, new ItemStack(Items.ENDER_PEARL, 16));
        var vanilla = new InvWrapper(chest);
        var packed = handler(chest.getItem(0));
        ItemStack offered = vanilla.extractItem(1, 16, true);
        assertTrue(ItemHandlerHelper.insertItemStacked(packed, offered, true).isEmpty());
        assertEquals(16, chest.getItem(1).getCount());
        assertTrue(ItemHandlerHelper.insertItemStacked(packed, vanilla.extractItem(1, 16, false), false).isEmpty());
        assertTrue(chest.getItem(1).isEmpty());
        assertEquals(16, handler(chest.getItem(0)).getStackInSlot(0).getCount());
        assertTrue(ItemHandlerHelper.insertItemStacked(vanilla, packed.extractItem(0, 64, false), false).isEmpty());
        assertEquals(16, chest.getItem(1).getCount());
        assertTrue(packed.getStackInSlot(0).isEmpty());
        assertTrue(chest.getItem(0).is(ContentRegistry.RADIANT_TROVE_ITEM.get()));
        }
    }
    @Test void lockVoidCapacityComponentsAndMetadataSurviveTransfers() {
        var trove = device();
        UUID owner = UUID.randomUUID();
        trove.setOwner(owner);
        trove.upgrades().insertItem(0, new ItemStack(ContentRegistry.MATRIX_BRACE.get()), false);
        for (var upgrade : new net.minecraft.world.item.Item[]{ContentRegistry.RADIANT_KEY.get(), ContentRegistry.DEVOURING_CHARM.get()}) {
            boolean installed = false;
            for (int slot = 0; slot < trove.optionals().getSlots(); slot++) {
                if (trove.optionals().isItemValid(slot, new ItemStack(upgrade))) {
                    installed = trove.optionals().insertItem(slot, new ItemStack(upgrade), false).isEmpty();
                    if (installed) break;
                }
            }
            assertTrue(installed);
        }
        ItemStack item = empty();
        trove.saveToItem(item, REGISTRIES);
        item.set(DataComponents.CUSTOM_NAME, Component.literal("Portable hoard"));
        CompoundTag original = item.get(DataComponents.BLOCK_ENTITY_DATA).copyTag();
        original.putString("external_metadata", "preserve");
        BlockItem.setBlockEntityData(item, ContentRegistry.RADIANT_TROVE_ENTITY.get(), original);
        var storage = handler(item);
        ItemStack named = new ItemStack(Items.DIAMOND);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("Precious"));
        ItemStack before = item.copy();
        assertTrue(storage.insertItem(0, named, true).isEmpty());
        assertTrue(ItemStack.matches(before, item));
        assertTrue(storage.insertItem(0, named, false).isEmpty());
        assertEquals(1, storage.extractItem(0, 64, false).getCount());
        assertEquals(1, storage.insertItem(0, new ItemStack(Items.DIAMOND), false).getCount());
        int capacity = RadiantTroveBlockEntity.capacity(named, 2);
        assertTrue(storage.insertItem(1, named.copyWithCount(capacity + 100), false).isEmpty());
        assertEquals(capacity, storage.getStackInSlot(0).getCount());
        assertTrue(storage.insertItem(0, named.copyWithCount(64), false).isEmpty());
        CompoundTag after = item.get(DataComponents.BLOCK_ENTITY_DATA).copyTag();
        for (String key : new String[]{"inventory", "lock_reference"}) { original.remove(key); after.remove(key); }
        assertEquals(original, after);
        assertEquals(Component.literal("Portable hoard"), item.get(DataComponents.CUSTOM_NAME));
        trove.loadWithComponents(item.get(DataComponents.BLOCK_ENTITY_DATA).copyTag(), REGISTRIES);
        assertEquals(owner, trove.owner());
        assertEquals(2, trove.upgrades().getUpgradesCount());
        assertEquals(named.get(DataComponents.CUSTOM_NAME), trove.lockReference().get(DataComponents.CUSTOM_NAME));
    }
    @Test void malformedAndStackedContainersFailWithoutRewriting() {
        ItemStack invalid = empty();
        CompoundTag tag = new CompoundTag();
        tag.put("inventory", new CompoundTag());
        BlockItem.setBlockEntityData(invalid, ContentRegistry.RADIANT_TROVE_ENTITY.get(), tag);
        for (ItemStack item : new ItemStack[]{invalid, empty().copyWithCount(2)}) {
            ItemStack before = item.copy();
            var storage = handler(item);
            assertEquals(5, storage.insertItem(0, new ItemStack(Items.DIAMOND, 5), false).getCount());
            assertTrue(storage.extractItem(0, 64, false).isEmpty());
            assertTrue(ItemStack.matches(before, item));
        }
    }
}
*///?}
