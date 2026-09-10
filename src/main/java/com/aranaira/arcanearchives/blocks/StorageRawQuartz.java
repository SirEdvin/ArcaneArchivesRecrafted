package com.aranaira.arcanearchives.blocks;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
//? if >=1.21 {
import com.mojang.serialization.MapCodec;
//?} else {
/*import net.minecraft.world.level.BlockGetter;
*///?}

public final class StorageRawQuartz extends Block {
    //? if >=1.21 {
    public static final MapCodec<StorageRawQuartz> CODEC = simpleCodec(StorageRawQuartz::new);

    @Override
    public MapCodec<StorageRawQuartz> codec() {
        return CODEC;
    }
    //?}

    public StorageRawQuartz() {
        this(Properties.of().mapColor(MapColor.STONE).strength(1.7F).requiresCorrectToolForDrops().lightLevel(state -> 15));
    }

    public StorageRawQuartz(Properties properties) {
        super(properties);
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.item.storage_raw_quartz").withStyle(ChatFormatting.GOLD));
    }
}
