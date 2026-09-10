package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
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

/** Original packed reference/count/voiding presentation, without modifying item data. */
public final class RadiantTroveItem extends BlockItem {
    public RadiantTroveItem(Block block, Properties properties) { super(block, properties); }

    //? if forge {
    /*@Override public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag tag) {
        return new com.aranaira.arcanearchives.inventory.TroveItemStorage(stack);
    }
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
        if (!data.contains("inventory")) return;
        var trove = new RadiantTroveBlockEntity(BlockPos.ZERO, getBlock().defaultBlockState());
        try {
            //? if >=1.21 {
            trove.loadWithComponents(data, registries);
            //?} else {
            /*trove.load(data);
            *///?}
            ItemStack contents = trove.inventory().getStackInSlot(0);
            ItemStack reference = contents.isEmpty() ? trove.lockReference() : contents;
            var name = reference.getHoverName().copy().withStyle(reference.getDisplayName().getStyle());
            //? if >=1.21 {
            if (reference.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) name.withStyle(ChatFormatting.ITALIC);
            //?} else {
            /*if (reference.hasCustomHoverName()) name.withStyle(ChatFormatting.ITALIC);
            *///?}
            tooltip.add(Component.translatable("arcanearchives.tooltip.trove.items", name));
            tooltip.add(Component.translatable("arcanearchives.tooltip.trove.contains", contents.getCount(),
                RadiantTroveBlockEntity.capacity(reference, trove.upgrades().getUpgradesCount())));
            if (trove.optionals().isVoiding()) tooltip.add(Component.translatable("arcanearchives.tooltip.trove.voiding")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        } catch (IllegalArgumentException exception) {
            tooltip.add(Component.translatable("arcanearchives.tooltip.trove.invalid").withStyle(ChatFormatting.RED));
        }
    }
}
