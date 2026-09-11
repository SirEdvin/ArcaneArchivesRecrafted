package com.aranaira.arcanearchives.blocks;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/** Registered upstream prototype: a nondirectional three-block footprint, not a fluid tank. */
public final class MatrixReservoir extends Block {
    public static final IntegerProperty PART = IntegerProperty.create("part", 0, 2);

    public MatrixReservoir() {
        this(Properties.of().mapColor(MapColor.NONE).strength(0F).noOcclusion()
            .lightLevel(state -> 15).pushReaction(PushReaction.BLOCK));
    }
    public MatrixReservoir(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, 0));
    }

    //? if >=1.21 {
    public static final com.mojang.serialization.MapCodec<MatrixReservoir> CODEC = simpleCodec(MatrixReservoir::new);
    @Override public com.mojang.serialization.MapCodec<MatrixReservoir> codec() { return CODEC; }
    //?}

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return canPlaceColumn(context) ? defaultBlockState() : null;
    }

    public static boolean canPlaceColumn(BlockPlaceContext context) {
        Level level = context.getLevel();
        var player = context.getPlayer();
        for (int part = 0; part < 3; part++) {
            BlockPos pos = context.getClickedPos().above(part);
            if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)
                    || !level.hasChunkAt(pos)
                    || (player != null && (!level.mayInteract(player, pos)
                        || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())))) return false;
            BlockPlaceContext atPart = BlockPlaceContext.at(context, pos, context.getClickedFace());
            if (!level.getBlockState(pos).canBeReplaced(atPart)) return false;
        }
        return true;
    }

    @Override public RenderShape getRenderShape(BlockState state) {
        return state.getValue(PART) == 0 ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        // Vanilla applies an item's block-state data after placeBlock: it must not turn the root into a child.
        if (state.getValue(PART) != 0 && level.getBlockState(pos).is(this))
            level.setBlock(pos, state.setValue(PART, 0), Block.UPDATE_CLIENTS);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && !level.isClientSide) {
            BlockPos parent = pos.below(state.getValue(PART));
            // The column never crosses a horizontal chunk boundary. Only matching parts are ours.
            for (int part = 0; part < 3; part++) {
                BlockPos other = parent.above(part);
                if (other.equals(pos) || level.isOutsideBuildHeight(other) || !level.hasChunkAt(other)) continue;
                BlockState existing = level.getBlockState(other);
                if (existing.is(this) && existing.getValue(PART) == part) level.removeBlock(other, false);
            }
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

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
