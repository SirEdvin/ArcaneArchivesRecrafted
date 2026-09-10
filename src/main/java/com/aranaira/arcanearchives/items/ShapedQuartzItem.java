package com.aranaira.arcanearchives.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if <1.21 {
/*import net.minecraft.world.level.Level;
*///?}

public final class ShapedQuartzItem extends Item {
    public ShapedQuartzItem() {
        super(new Properties());
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.item.shaped_quartz").withStyle(ChatFormatting.GOLD));
    }
}
