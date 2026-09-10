package com.aranaira.arcanearchives.items;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.inventory.RadiantTankStorage;
import com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;

class RadiantTankItemTest {
    private static final RegistryAccess REGISTRIES = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    private static ItemStack packed(boolean filled) {
        var tank = new RadiantTankBlockEntity(BlockPos.ZERO, ContentRegistry.RADIANT_TANK.get().defaultBlockState());
        assertTrue(tank.upgrades().insertItem(0, new ItemStack(ContentRegistry.MATRIX_BRACE.get()), false).isEmpty());
        if (filled) tank.inventory().setFluid(new FluidStack(Fluids.WATER, 1000));
        ItemStack stack = new ItemStack(ContentRegistry.RADIANT_TANK_ITEM.get());
        tank.saveToItem(stack, REGISTRIES);
        return stack;
    }

    private static List<Component> tooltip(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        stack.getItem().appendHoverText(stack, Item.TooltipContext.of(REGISTRIES), lines, TooltipFlag.NORMAL);
        return lines;
    }

    @Test void packedTooltipReportsFluidAndUpgradedCapacityWithoutMutation() {
        ItemStack stack = packed(true);
        ItemStack before = stack.copy();
        List<Component> lines = tooltip(stack);
        var fluid = (TranslatableContents) lines.get(0).getContents();
        assertEquals("arcanearchives.tooltip.tank.fluid", fluid.getKey());
        assertEquals(new FluidStack(Fluids.WATER, 1000).getDisplayName(), fluid.getArgs()[0]);
        var amount = (TranslatableContents) lines.get(1).getContents();
        assertEquals("arcanearchives.tooltip.tank.amount", amount.getKey());
        // The original Matrix Brace adds two capacity units, not one.
        assertArrayEquals(new Object[]{1000L, RadiantTankStorage.capacityFor(2)}, amount.getArgs());
        assertEquals("Capacity is: " + RadiantTankStorage.capacityFor(2), lines.get(2).getString());
        assertTrue(ItemStack.matches(before, stack));
    }

    @Test void craftingReturnsFreshEmptyTankNotPackedContents() {
        ItemStack stack = packed(true);
        ItemStack before = stack.copy();
        assertTrue(stack.hasCraftingRemainingItem());
        ItemStack remainder = stack.getCraftingRemainingItem();
        assertTrue(ItemStack.matches(new ItemStack(ContentRegistry.RADIANT_TANK_ITEM.get()), remainder));
        assertFalse(remainder.has(DataComponents.BLOCK_ENTITY_DATA));
        assertTrue(ItemStack.matches(before, stack));
    }

    @Test void emptyAndInvalidTooltipsAreDistinctAndDoNotRewriteData() {
        var empty = (TranslatableContents) tooltip(packed(false)).get(0).getContents();
        assertEquals(Component.literal("None"), empty.getArgs()[0]);
        ItemStack invalid = new ItemStack(ContentRegistry.RADIANT_TANK_ITEM.get());
        CompoundTag data = new CompoundTag();
        data.put("fluid_storage", new CompoundTag());
        net.minecraft.world.item.BlockItem.setBlockEntityData(invalid, ContentRegistry.RADIANT_TANK_ENTITY.get(), data);
        ItemStack before = invalid.copy();
        var error = (TranslatableContents) tooltip(invalid).get(0).getContents();
        assertEquals("arcanearchives.tooltip.tank.invalid", error.getKey());
        assertTrue(ItemStack.matches(before, invalid));
    }
}
*///?}
