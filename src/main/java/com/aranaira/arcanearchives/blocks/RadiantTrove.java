package com.aranaira.arcanearchives.blocks;

import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
//? if >=1.21 {
import com.mojang.serialization.MapCodec;
import net.minecraft.world.ItemInteractionResult;
//?}

public final class RadiantTrove extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(3.2, 0, 3.2, 12.8, 16, 12.8);
    //? if >=1.21 {
    public static final MapCodec<RadiantTrove> CODEC = simpleCodec(RadiantTrove::new);
    @Override public MapCodec<RadiantTrove> codec() { return CODEC; }
    //?}
    public RadiantTrove() {
        this(Properties.of().mapColor(MapColor.QUARTZ).strength(3F, 1200F).lightLevel(state -> 15)
            .noOcclusion().pushReaction(PushReaction.BLOCK));
    }
    public RadiantTrove(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new RadiantTroveBlockEntity(pos, state); }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player && level.getBlockEntity(pos) instanceof RadiantTroveBlockEntity trove)
            trove.setOwner(player.getUUID());
    }
    private void useTrove(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).getItem() instanceof com.aranaira.arcanearchives.items.StorageScepterItem scepter
                && scepter.interact(player, hand, level, pos)) return;
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && level.getBlockEntity(pos) instanceof RadiantTroveBlockEntity trove) {
            if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
                com.aranaira.arcanearchives.inventory.StorageUpgradeMenu.open(player, trove.upgrades(), trove.optionals(), trove::canUse);
                return;
            }
            if (player.getItemInHand(hand).getItem() instanceof com.aranaira.arcanearchives.items.DevouringCharmItem
                    || player.getItemInHand(hand).getItem() instanceof com.aranaira.arcanearchives.items.RadiantKeyItem) {
                trove.installUpgrade(player, hand);
                return;
            }
            for (int slot = 0; slot < trove.upgrades().getSlots(); slot++) {
                if (trove.upgrades().isItemValid(slot, player.getItemInHand(hand))) {
                    trove.installUpgrade(player, hand);
                    return;
                }
            }
            trove.deposit(player);
        }
    }
    //? if >=1.21 {
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        useTrove(level, pos, player, InteractionHand.MAIN_HAND); return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        useTrove(level, pos, player, hand); return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    //?} else {
    /*@Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        useTrove(level, pos, player, hand); return InteractionResult.sidedSuccess(level.isClientSide);
    }
    *///?}
    @Override public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;
        HitResult result = player.pick(6, 0, false);
        if (result instanceof BlockHitResult hit && hit.getBlockPos().equals(pos)
                && (hit.getDirection() == Direction.UP || hit.getDirection() == state.getValue(FACING).getClockWise())
                && level.getBlockEntity(pos) instanceof RadiantTroveBlockEntity trove) trove.withdraw(player);
    }
    public static boolean keepsCreativeStorage(Player player) {
        return player != null && player.isCreative() && !player.isSpectator()
            && !(player.getMainHandItem().getItem() instanceof com.aranaira.arcanearchives.items.StorageScepterItem);
    }
    @Override public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (player.getMainHandItem().getItem() instanceof com.aranaira.arcanearchives.items.StorageScepterItem) {
            float hardness = state.getDestroySpeed(level, pos);
            return hardness < 0 ? 0 : 5F / hardness / 30F;
        }
        return super.getDestroyProgress(state, player, level, pos);
    }
    //? if !fabric {
    /*@Override public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
            boolean willHarvest, net.minecraft.world.level.material.FluidState fluid) {
        if (keepsCreativeStorage(player)) {
            attack(state, level, pos, player);
            return false;
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof RadiantTroveBlockEntity trove)
            return trove.removeByPlayer(player, () -> super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid));
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }
    *///?}
    //? if fabric {
    public static void initializeInteractions() {
        var phase = com.aranaira.arcanearchives.init.ContentRegistry.id("trove_creative_withdrawal");
        var before = net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE;
        before.addPhaseOrdering(net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE, phase);
        before.register(phase, (level, player, pos, state, entity) -> {
            if (!(state.getBlock() instanceof RadiantTrove trove) || !keepsCreativeStorage(player)) return true;
            trove.attack(state, level, pos, player);
            return false;
        });
        var attacks = net.fabricmc.fabric.api.event.player.AttackBlockCallback.EVENT;
        attacks.addPhaseOrdering(net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE, phase);
        attacks.register(phase, (player, level, hand, pos, face) ->
            level.isClientSide && hand == InteractionHand.MAIN_HAND && keepsCreativeStorage(player)
                && level.getBlockState(pos).getBlock() instanceof RadiantTrove
                ? InteractionResult.SUCCESS : InteractionResult.PASS);
    }
    //?}
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            if (level.getBlockEntity(pos) instanceof RadiantTroveBlockEntity trove) trove.dropPacked();
            super.onRemove(state, level, pos, replacement, moving);
        }
    }
    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context,
            java.util.List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, BlockGetter context,
            java.util.List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
    *///?}
        tooltip.add(net.minecraft.network.chat.Component.translatable("arcanearchives.tooltip.device.radiant_trove")
            .withStyle(net.minecraft.ChatFormatting.GOLD));
        tooltip.add(net.minecraft.network.chat.Component.translatable("arcanearchives.tooltip.storage_upgrades"));
    }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof RadiantTroveBlockEntity trove ? trove.comparatorSignal() : 0;
    }
}
