package com.aranaira.arcanearchives.blocks;

import com.aranaira.arcanearchives.data.ResonatorSaveData;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantResonatorBlockEntity;
import com.aranaira.arcanearchives.types.BlockPosDimension;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.material.PushReaction;
//? if >=1.21 {
import com.mojang.serialization.MapCodec;
//?} else {
/*import net.minecraft.world.level.BlockGetter;
*///?}

public final class RadiantResonator extends Block implements EntityBlock {
    //? if >=1.21 {
    public static final MapCodec<RadiantResonator> CODEC = simpleCodec(RadiantResonator::new);
    @Override public MapCodec<RadiantResonator> codec() { return CODEC; }
    //?}

    public RadiantResonator() {
        this(Properties.of().mapColor(MapColor.METAL).strength(3F).noOcclusion().pushReaction(PushReaction.BLOCK));
    }

    public RadiantResonator(Properties properties) { super(properties); }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadiantResonatorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide || type != ContentRegistry.RADIANT_RESONATOR_ENTITY.get() ? null
            : (world, pos, block, entity) -> RadiantResonatorBlockEntity.tick(world, pos, block, (RadiantResonatorBlockEntity) entity);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof ServerPlayer player
                && level.getServer().getPlayerList().getPlayer(player.getUUID()) == player
                && level.getBlockEntity(pos) instanceof RadiantResonatorBlockEntity resonator) {
            resonator.setOwner(player.getUUID());
        }
    }

    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }

    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof RadiantResonatorBlockEntity resonator ? resonator.comparatorSignal() : 0;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            if (!level.isClientSide) ResonatorSaveData.get(level.getServer()).remove(new BlockPosDimension(pos, level.dimension()));
            super.onRemove(state, level, pos, replacement, moving);
        }
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.device.radiant_resonator").withStyle(ChatFormatting.GOLD));
    }
}
