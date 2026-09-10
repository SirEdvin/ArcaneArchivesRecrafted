package com.aranaira.arcanearchives.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class ChromaticPowderItem extends Item {
    private static final String[] COLORS = {"", "red", "orange", "yellow", "green", "cyan", "blue", "purple", "pink", "black", "white"};
    private final boolean rainbow;
    public ChromaticPowderItem(boolean rainbow) { super(new Properties()); this.rainbow = rainbow; }
    public boolean isRainbow() { return rainbow; }
    public static int color(ItemStack stack) {
        int color = ArcaneGemItem.data(stack).getInt("color");
        return color >= 1 && color < COLORS.length ? color : 0;
    }
    private String suffix(ItemStack stack) {
        if (rainbow) return "full_spectrum_chromatic_powder";
        int color = color(stack);
        return "chromatic_powder" + (color == 0 ? "" : "." + COLORS[color]);
    }
    @Override public String getDescriptionId(ItemStack stack) { return "item.arcanearchives." + suffix(stack); }
    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.item." + suffix(stack)).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("arcanearchives.tooltip.creativeonly").withStyle(ChatFormatting.RED));
    }
}
