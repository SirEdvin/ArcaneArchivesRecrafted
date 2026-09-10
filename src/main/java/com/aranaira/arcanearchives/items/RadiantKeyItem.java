package com.aranaira.arcanearchives.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class RadiantKeyItem extends Item {
    public RadiantKeyItem() { super(new Properties()); }
    @Override public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null || !(context.getLevel().getBlockEntity(context.getClickedPos())
                instanceof com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity trove)) return InteractionResult.PASS;
        if (!context.getLevel().isClientSide) trove.installUpgrade(context.getPlayer(), context.getHand());
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.item.radiant_key").withStyle(ChatFormatting.GOLD));
    }
}
