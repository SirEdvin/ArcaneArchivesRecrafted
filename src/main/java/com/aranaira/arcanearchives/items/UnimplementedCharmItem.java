package com.aranaira.arcanearchives.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** The original registered Obstruction/Serenity placeholders, not new charm powers. */
public final class UnimplementedCharmItem extends Item {
    public UnimplementedCharmItem() { super(new Properties()); }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.notimplemented1")
            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("arcanearchives.tooltip.notimplemented2")
            .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
    }
}
