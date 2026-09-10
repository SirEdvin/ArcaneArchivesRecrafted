package com.aranaira.arcanearchives.blocks;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.WonkyResonatorBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/** Registered upstream prototype: a timer, not the unimplemented explosion. */
public final class WonkyResonator extends Block implements EntityBlock {
    public WonkyResonator() {
        this(Properties.of().mapColor(MapColor.METAL).strength(3F).requiresCorrectToolForDrops().noOcclusion()
            .isSuffocating((state, level, pos) -> false));
    }
    public WonkyResonator(Properties properties) { super(properties); }
    //? if >=1.21 {
    public static final com.mojang.serialization.MapCodec<WonkyResonator> CODEC = simpleCodec(WonkyResonator::new);
    @Override public com.mojang.serialization.MapCodec<WonkyResonator> codec() { return CODEC; }
    //?}
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WonkyResonatorBlockEntity(pos, state);
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide || type != ContentRegistry.WONKY_RESONATOR_ENTITY.get() ? null
            : (world, pos, block, entity) -> ((WonkyResonatorBlockEntity) entity).tick();
    }
    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, net.minecraft.world.level.BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.device.wonky_resonator").withStyle(ChatFormatting.GOLD));
    }
}
