package com.aranaira.arcanearchives.blocks;

import com.aranaira.arcanearchives.inventory.ManifestMenu;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
//? if >=1.21 {
import com.mojang.serialization.MapCodec;
import net.minecraft.world.ItemInteractionResult;
//?} else {
/*import net.minecraft.world.level.BlockGetter;
*///?}

/** Original two-high public station: it opens the interacting player's own Manifest. */
public final class LecternManifest extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ACCESSOR = BooleanProperty.create("accessor");
    //? if >=1.21 {
    public static final MapCodec<LecternManifest> CODEC = simpleCodec(LecternManifest::new);

    @Override
    public MapCodec<LecternManifest> codec() {
        return CODEC;
    }
    //?}

    public LecternManifest() {
        this(Properties.of().mapColor(MapColor.WOOD).strength(1.5F).lightLevel(state -> 15)
            .noOcclusion());
    }

    public LecternManifest(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACCESSOR, false));
    }

    public static BlockPos connectedPos(BlockPos pos, BlockState state) {
        return state.getValue(ACCESSOR) ? pos.below() : pos.above();
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.device.lectern_manifest").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return canPlaceColumn(context) ? defaultBlockState().setValue(FACING,
            context.getHorizontalDirection().getCounterClockWise()) : null;
    }

    public static boolean canPlaceColumn(BlockPlaceContext context) {
        Level level = context.getLevel();
        var player = context.getPlayer();
        for (int part = 0; part < 2; part++) {
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

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        // Keep the root and companion coherent after vanilla imports item block-state data.
        var top = level.getBlockState(pos.above());
        if (state.is(this) && top.is(this)) level.setBlock(pos,
            top.setValue(ACCESSOR, false), UPDATE_CLIENTS);
    }

    private void openMenu(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && level.mayInteract(player, pos) && !player.isSpectator())
            ManifestMenu.open(player, state.getValue(ACCESSOR) ? pos.below() : pos);
    }

    //? if >=1.21 {
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        openMenu(state, level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        openMenu(state, level, pos, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    //?} else {
    /*@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        openMenu(state, level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    *///?}

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from, boolean moving) {
        // Upstream clears only when the companion position becomes air, not on foreign solid replacement.
        if (level.isEmptyBlock(connectedPos(pos, state))) level.removeBlock(pos, false);
        super.neighborChanged(state, level, pos, block, from, moving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACCESSOR);
    }
}
