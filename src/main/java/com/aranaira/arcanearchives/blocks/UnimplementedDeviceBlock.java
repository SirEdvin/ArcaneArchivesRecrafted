package com.aranaira.arcanearchives.blocks;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

/** Shared behavior of five registered upstream prototypes; deliberately no machine or inventory. */
public final class UnimplementedDeviceBlock extends Block {
    public UnimplementedDeviceBlock(boolean cutout) { this(properties(cutout)); }
    public UnimplementedDeviceBlock(Properties properties) { super(properties); }

    private static Properties properties(boolean cutout) {
        Properties properties = Properties.of().mapColor(MapColor.STONE).strength(1.7F)
            .requiresCorrectToolForDrops().lightLevel(state -> 15);
        return cutout ? properties.noOcclusion().isSuffocating((state, level, pos) -> false) : properties;
    }

    //? if >=1.21 {
    public static final com.mojang.serialization.MapCodec<UnimplementedDeviceBlock> CODEC = simpleCodec(UnimplementedDeviceBlock::new);
    @Override public com.mojang.serialization.MapCodec<UnimplementedDeviceBlock> codec() { return CODEC; }
    //?}

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, net.minecraft.world.level.BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.notimplemented1").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        tooltip.add(Component.translatable("arcanearchives.tooltip.notimplemented2").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
    }
}
