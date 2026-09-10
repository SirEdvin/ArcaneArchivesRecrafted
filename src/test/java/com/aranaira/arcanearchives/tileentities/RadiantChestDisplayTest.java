package com.aranaira.arcanearchives.tileentities;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;

import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class RadiantChestDisplayTest {
    private static RadiantChestBlockEntity chest() {
        return new RadiantChestBlockEntity(BlockPos.ZERO, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
    }

    @Test void displayCopiesNeitherConsumeNorBecomeInventory() {
        var chest = chest();
        ItemStack offered = new ItemStack(Items.DIAMOND, 11);
        offered.set(DataComponents.CUSTOM_NAME, Component.literal("Display sample"));
        chest.setDisplay(offered, Direction.WEST);
        assertEquals(11, offered.getCount());
        offered.setCount(2);
        offered.set(DataComponents.CUSTOM_NAME, Component.literal("Changed source"));
        assertEquals(11, chest.displayStack().getCount());
        assertEquals("Display sample", chest.displayStack().getHoverName().getString());
        chest.displayStack().setCount(1);
        assertEquals(11, chest.displayStack().getCount());
        for (int slot = 0; slot < chest.inventory().getSlots(); slot++)
            assertTrue(chest.inventory().getStackInSlot(slot).isEmpty());
    }

    @Test void saveRoundTripKeepsAllFacesAndVisualUpdatesOmitInventory() {
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var source = chest();
        UUID owner = UUID.fromString("a2b6445f-b6a3-40d9-84c0-92b46d95ceaf");
        source.setOwner(owner);
        source.setName("Precious materials");
        source.inventory().setStackInSlot(0, new ItemStack(Items.GOLD_INGOT, 10));
        ItemStack icon = new ItemStack(Items.DIAMOND, 11);
        icon.set(DataComponents.CUSTOM_NAME, Component.literal("Named diamond"));
        for (Direction facing : Direction.values()) {
            source.setDisplay(icon, facing);
            CompoundTag saved = new CompoundTag();
            source.saveAdditional(saved, registries);
            var restored = chest();
            restored.loadAdditional(saved, registries);
            assertEquals(facing, restored.displayFacing());
            assertTrue(ItemStack.matches(icon, restored.displayStack()));
            assertEquals(owner, restored.owner());
            assertEquals("Precious materials", restored.chestName());
            assertEquals(10, restored.inventory().getStackInSlot(0).getCount());
            CompoundTag visual = source.getUpdateTag(registries);
            assertFalse(visual.contains("inventory"));
            assertFalse(visual.contains("owner"));
            var client = chest();
            client.loadAdditional(visual, registries);
            assertTrue(ItemStack.matches(icon, client.displayStack()));
            assertEquals(facing, client.displayFacing());
        }
    }

    @Test void clearUpdatesRemovePreviousIconAndMissingFieldsUseDefaults() {
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var source = chest();
        source.setDisplay(new ItemStack(Items.DIAMOND), Direction.EAST);
        var client = chest();
        client.loadAdditional(source.getUpdateTag(registries), registries);
        assertFalse(client.displayStack().isEmpty());
        source.setDisplay(ItemStack.EMPTY, source.displayFacing());
        client.loadAdditional(source.getUpdateTag(registries), registries);
        assertTrue(client.displayStack().isEmpty());
        assertEquals(Direction.EAST, client.displayFacing());
        client.loadAdditional(new CompoundTag(), registries);
        assertTrue(client.displayStack().isEmpty());
        assertEquals(Direction.NORTH, client.displayFacing());
    }
}
*///?}
