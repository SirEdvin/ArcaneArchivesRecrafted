package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.inventory.RadiantTankStorage;
import com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/** Original Tank item presentation and fresh-empty-container crafting remainder. */
public final class RadiantTankItem extends LimitedStorageBlockItem {
    public RadiantTankItem(Block block, Properties properties) { super(block, properties); }

    //? if forge {
    /*@Override public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag tag) {
        return new com.aranaira.arcanearchives.inventory.TankItemFluidStorage(stack);
    }
    *///?}

    //? if fabric {
    @Override public ItemStack getRecipeRemainder(ItemStack stack) { return new ItemStack(this); }
    //?} else {
    /*@Override public boolean hasCraftingRemainingItem(ItemStack stack) { return true; }
    @Override public ItemStack getCraftingRemainingItem(ItemStack stack) { return new ItemStack(this); }
    *///?}

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        var data = stack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
        if (data != null && context.registries() != null) appendStoredTooltip(data.copyTag(), context.registries(), tooltip);
    //?} else {
    /*public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level context, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag data = BlockItem.getBlockEntityData(stack);
        if (data != null) appendStoredTooltip(data.copy(), null, tooltip);
    *///?}
        super.appendHoverText(stack, context, tooltip, flag);
    }

    private void appendStoredTooltip(CompoundTag data, HolderLookup.Provider registries, List<Component> tooltip) {
        var tank = new RadiantTankBlockEntity(BlockPos.ZERO, getBlock().defaultBlockState());
        try {
            //? if >=1.21 {
            tank.loadWithComponents(data, registries);
            //?} else {
            /*tank.load(data);
            *///?}
            long amount = tank.inventory().storedAmount();
            long capacity = RadiantTankStorage.capacityFor(tank.upgrades().getUpgradesCount());
            Component fluid = Component.literal("None");
            //? if fabric {
            if (amount > 0) fluid = net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes.getName(tank.inventory().getResource());
            amount = amount * 1000 / net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.BUCKET;
            capacity = capacity * 1000 / net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.BUCKET;
            //?} else {
            /*if (amount > 0) fluid = tank.inventory().getFluid().getDisplayName();
            *///?}
            tooltip.add(Component.translatable("arcanearchives.tooltip.tank.fluid", fluid));
            tooltip.add(Component.translatable("arcanearchives.tooltip.tank.amount", amount, capacity));
            tooltip.add(Component.literal("Capacity is: " + capacity));
        } catch (IllegalArgumentException exception) {
            tooltip.add(Component.translatable("arcanearchives.tooltip.tank.invalid").withStyle(ChatFormatting.RED));
        }
    }
}
