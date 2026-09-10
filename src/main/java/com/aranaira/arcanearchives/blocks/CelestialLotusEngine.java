package com.aranaira.arcanearchives.blocks;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

/** Registered upstream artwork/light source; no engine block entity or generation was implemented. */
public final class CelestialLotusEngine extends Block {
    public CelestialLotusEngine() {
        // Legacy GLASS permits hand harvesting despite its configured pickaxe harvest level.
        this(Properties.of().mapColor(MapColor.NONE).strength(0.3F).lightLevel(state -> 15)
            .noOcclusion().isSuffocating((state, level, pos) -> false));
    }
    public CelestialLotusEngine(Properties properties) { super(properties); }
    //? if >=1.21 {
    public static final com.mojang.serialization.MapCodec<CelestialLotusEngine> CODEC = simpleCodec(CelestialLotusEngine::new);
    @Override public com.mojang.serialization.MapCodec<CelestialLotusEngine> codec() { return CODEC; }
    //?}

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, net.minecraft.world.level.BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.device.celestial_lotus_engine").withStyle(ChatFormatting.GOLD));
    }
}
