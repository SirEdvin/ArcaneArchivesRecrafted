package com.aranaira.arcanearchives.inventory;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.junit.jupiter.api.Test;

class TankItemFluidStorageTest {
    private static final RegistryAccess REGISTRIES = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    private static ItemStack empty() { return new ItemStack(ContentRegistry.RADIANT_TANK_ITEM.get()); }

    @Test void registeredCapabilityFillsAndDrainsWithoutChangingSimulations() {
        ItemStack item = empty();
        var handler = FluidUtil.getFluidHandler(item).orElseThrow();
        assertEquals(16000, handler.getTankCapacity(0));
        ItemStack before = item.copy();
        assertEquals(1000, handler.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.SIMULATE));
        assertTrue(ItemStack.matches(before, item));
        assertEquals(1000, handler.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE));
        before = item.copy();
        assertEquals(1000, handler.drain(2000, FluidAction.SIMULATE).getAmount());
        assertTrue(ItemStack.matches(before, item));
        assertEquals(1000, handler.drain(2000, FluidAction.EXECUTE).getAmount());
        assertTrue(handler.getFluidInTank(0).isEmpty());
        assertSame(item, handler.getContainer());
        assertTrue(item.is(ContentRegistry.RADIANT_TANK_ITEM.get()));
    }

    @Test void preservesPackedMetadataUpgradesAndFluidComponents() {
        var tank = new RadiantTankBlockEntity(BlockPos.ZERO, ContentRegistry.RADIANT_TANK.get().defaultBlockState());
        UUID owner = UUID.randomUUID();
        tank.setOwner(owner);
        tank.upgrades().insertItem(0, new ItemStack(ContentRegistry.MATRIX_BRACE.get()), false);
        ItemStack item = empty();
        tank.saveToItem(item, REGISTRIES);
        item.set(DataComponents.CUSTOM_NAME, Component.literal("Portable reservoir"));
        CompoundTag original = item.get(DataComponents.BLOCK_ENTITY_DATA).copyTag();
        var handler = FluidUtil.getFluidHandler(item).orElseThrow();
        assertEquals(48000, handler.getTankCapacity(0));
        FluidStack fluid = new FluidStack(Fluids.WATER, 60000);
        fluid.set(DataComponents.CUSTOM_NAME, Component.literal("Named fluid"));
        FluidStack first = fluid.copy();
        first.setAmount(1000);
        assertEquals(1000, handler.fill(first, FluidAction.EXECUTE));
        // Component mismatch must reject even while physical space remains.
        assertEquals(0, handler.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE));
        assertEquals(0, handler.fill(new FluidStack(Fluids.LAVA, 1000), FluidAction.EXECUTE));
        assertEquals(47000, handler.fill(fluid, FluidAction.EXECUTE));
        assertEquals(0, handler.fill(fluid, FluidAction.EXECUTE));
        FluidStack exposed = handler.getFluidInTank(0);
        exposed.setAmount(1);
        assertEquals(48000, handler.getFluidInTank(0).getAmount());
        FluidStack drained = handler.drain(48000, FluidAction.EXECUTE);
        assertEquals(fluid.get(DataComponents.CUSTOM_NAME), drained.get(DataComponents.CUSTOM_NAME));
        CompoundTag after = item.get(DataComponents.BLOCK_ENTITY_DATA).copyTag();
        original.remove("fluid_storage");
        after.remove("fluid_storage");
        assertEquals(original, after);
        assertEquals(Component.literal("Portable reservoir"), item.get(DataComponents.CUSTOM_NAME));
        tank.loadWithComponents(item.get(DataComponents.BLOCK_ENTITY_DATA).copyTag(), REGISTRIES);
        assertEquals(owner, tank.owner());
        assertEquals(2, tank.upgrades().getUpgradesCount());
        assertEquals(0, tank.inventory().storedAmount());
    }

    @Test void malformedAndStackedInputsAreNotRewritten() {
        ItemStack invalid = empty();
        CompoundTag data = new CompoundTag();
        data.put("fluid_storage", new CompoundTag());
        BlockItem.setBlockEntityData(invalid, ContentRegistry.RADIANT_TANK_ENTITY.get(), data);
        for (ItemStack item : new ItemStack[]{invalid, empty().copyWithCount(2)}) {
            ItemStack before = item.copy();
            var handler = FluidUtil.getFluidHandler(item).orElseThrow();
            assertEquals(0, handler.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE));
            assertTrue(handler.drain(1000, FluidAction.EXECUTE).isEmpty());
            assertTrue(ItemStack.matches(before, item));
        }
    }

    @Test void nativeCopiedContainerSimulationCannotChangeOriginalOrSource() {
        ItemStack item = empty();
        ItemStack before = item.copy();
        FluidTank source = new FluidTank(4000);
        source.setFluid(new FluidStack(Fluids.WATER, 4000));
        var prepared = FluidUtil.tryFillContainer(item, source, 1000, null, false);
        assertTrue(prepared.isSuccess());
        assertTrue(ItemStack.matches(before, item));
        assertEquals(4000, source.getFluidAmount());
        assertEquals(1000, FluidUtil.getFluidContained(prepared.getResult()).orElseThrow().getAmount());
        var real = FluidUtil.tryFillContainer(item, source, 1000, null, true);
        assertTrue(real.isSuccess());
        assertEquals(3000, source.getFluidAmount());
        assertTrue(ItemStack.matches(before, item));
        assertEquals(1000, FluidUtil.getFluidContained(real.getResult()).orElseThrow().getAmount());
    }
}
*///?}
