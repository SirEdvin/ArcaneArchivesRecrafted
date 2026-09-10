package com.aranaira.arcanearchives.blocks;

import com.aranaira.arcanearchives.tileentities.GemCuttersTableBlockEntity;
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
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
//? if >=1.21 {
import com.mojang.serialization.MapCodec;
import net.minecraft.world.ItemInteractionResult;
//?} else {
/*import net.minecraft.world.level.BlockGetter;
*///?}

/** Two-part release geometry: the accessor lies opposite the master's facing. */
public final class GemCuttersTable extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACCESSOR = BooleanProperty.create("accessor");
    //? if >=1.21 {
    public static final MapCodec<GemCuttersTable> CODEC = simpleCodec(GemCuttersTable::new);

    @Override
    public MapCodec<GemCuttersTable> codec() {
        return CODEC;
    }
    //?}

    public GemCuttersTable() {
        this(Properties.of().mapColor(MapColor.METAL).strength(3F).lightLevel(state -> 15)
            .noOcclusion().noLootTable().pushReaction(PushReaction.BLOCK));
    }

    public GemCuttersTable(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACCESSOR, false));
    }

    public static BlockPos connectedPos(BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        return pos.relative(state.getValue(ACCESSOR) ? facing : facing.getOpposite());
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.device.gemcutters_table").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Upstream uses player yaw - 90 degrees, rather than the usual opposite-facing placement.
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getCounterClockWise());
        BlockPos other = connectedPos(context.getClickedPos(), state);
        return context.getLevel().getWorldBorder().isWithinBounds(other)
                && context.getLevel().getBlockState(other).canBeReplaced(context) ? state : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && !state.getValue(ACCESSOR)) {
            level.setBlock(connectedPos(pos, state), state.setValue(ACCESSOR, true), UPDATE_ALL);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(ACCESSOR) ? null : new GemCuttersTableBlockEntity(pos, state);
    }

    private void openMenu(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;
        BlockPos master = state.getValue(ACCESSOR) ? connectedPos(pos, state) : pos;
        if (level.getBlockEntity(master) instanceof GemCuttersTableBlockEntity table && table.stillValid(player)) {
            player.openMenu(table);
        }
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) || state.getValue(ACCESSOR) != replacement.getValue(ACCESSOR)) {
            if (!level.isClientSide) {
                if (!state.getValue(ACCESSOR) && level.getBlockEntity(pos) instanceof GemCuttersTableBlockEntity table) {
                    table.dropContents();
                    level.removeBlockEntity(pos);
                }
                BlockPos other = connectedPos(pos, state);
                BlockState otherState = level.getBlockState(other);
                if (otherState.is(this) && otherState.getValue(FACING) == state.getValue(FACING)
                        && otherState.getValue(ACCESSOR) != state.getValue(ACCESSOR)) {
                    level.removeBlock(other, false);
                }
            }
            super.onRemove(state, level, pos, replacement, moving);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACCESSOR);
    }
}
