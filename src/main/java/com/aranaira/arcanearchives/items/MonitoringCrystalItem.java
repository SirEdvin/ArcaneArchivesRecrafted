package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

public final class MonitoringCrystalItem extends BlockItem {
    public MonitoringCrystalItem(Block block) { super(block, new Item.Properties()); }
    public static InteractionResult checkTarget(UseOnContext context) {
        if (!(context.getItemInHand().getItem() instanceof MonitoringCrystalItem)
                || !MonitoringCrystalBlockEntity.isArcaneDevice(context.getLevel().getBlockEntity(context.getClickedPos())))
            return InteractionResult.PASS;
        if (context.getPlayer() != null) context.getPlayer().displayClientMessage(
            Component.translatable("arcanearchives.message.invalid_crystal_target").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
        return InteractionResult.FAIL;
    }
    //? if !fabric {
    /*@Override public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) { return checkTarget(context); }
    *///?}
    @Override public InteractionResult useOn(UseOnContext context) {
        return checkTarget(context) == InteractionResult.FAIL ? InteractionResult.FAIL : super.useOn(context);
    }
    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<Component> text, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level context, java.util.List<Component> text, TooltipFlag flag) {
    *///?}
        text.add(Component.literal("Placed on inventories to relay their contents to a manifest.").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, context, text, flag);
    }
}
