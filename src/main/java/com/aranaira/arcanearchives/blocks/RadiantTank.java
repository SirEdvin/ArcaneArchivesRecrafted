package com.aranaira.arcanearchives.blocks;

import com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
//? if >=1.21 {
import com.mojang.serialization.MapCodec;
import net.minecraft.world.ItemInteractionResult;
//?}

public final class RadiantTank extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.box(.1, 0, .1, .9, .98, .9);
    //? if >=1.21 {
    public static final MapCodec<RadiantTank> CODEC = simpleCodec(RadiantTank::new);
    @Override public MapCodec<RadiantTank> codec() { return CODEC; }
    //?}
    public RadiantTank() {
        this(Properties.of().mapColor(MapColor.QUARTZ).strength(3F).lightLevel(state -> 15)
            .noOcclusion().pushReaction(PushReaction.BLOCK));
    }
    public RadiantTank(Properties properties) { super(properties); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new RadiantTankBlockEntity(pos, state); }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player && level.getBlockEntity(pos) instanceof RadiantTankBlockEntity tank)
            tank.setOwner(player.getUUID());
    }
    private boolean interact(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (player.getItemInHand(hand).getItem() instanceof com.aranaira.arcanearchives.items.StorageScepterItem scepter
                && scepter.interact(player, hand, level, pos)) return true;
        if (player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()
                && level.getBlockEntity(pos) instanceof RadiantTankBlockEntity tank) {
            com.aranaira.arcanearchives.inventory.StorageUpgradeMenu.open(player, tank.upgrades(), tank.optionals(), tank::canUse);
            return true;
        }
        if (player.getItemInHand(hand).getItem() instanceof com.aranaira.arcanearchives.items.DevouringCharmItem
                && level.getBlockEntity(pos) instanceof RadiantTankBlockEntity tank) {
            tank.installUpgrade(player, hand);
            return true;
        }
        return level.getBlockEntity(pos) instanceof RadiantTankBlockEntity tank
            && (tank.installUpgrade(player, hand) || tank.inventory().interact(player, hand));
    }
    //? if >=1.21 {
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return level.isClientSide || interact(level, pos, player, hand)
            ? ItemInteractionResult.sidedSuccess(level.isClientSide) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return player.isShiftKeyDown() && (level.isClientSide || interact(level, pos, player, InteractionHand.MAIN_HAND))
            ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }
    //?} else {
    /*@Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return level.isClientSide || interact(level, pos, player, hand) ? InteractionResult.sidedSuccess(level.isClientSide) : InteractionResult.PASS;
    }
    *///?}
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            if (level.getBlockEntity(pos) instanceof RadiantTankBlockEntity tank) tank.dropPacked();
            super.onRemove(state, level, pos, replacement, moving);
        }
    }
    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof RadiantTankBlockEntity tank ? tank.comparatorSignal() : 0;
    }
    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context,
            java.util.List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, BlockGetter context,
            java.util.List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
    *///?}
        tooltip.add(net.minecraft.network.chat.Component.translatable("arcanearchives.tooltip.device.radiant_tank")
            .withStyle(net.minecraft.ChatFormatting.GOLD));
        tooltip.add(net.minecraft.network.chat.Component.translatable("arcanearchives.tooltip.storage_upgrades"));
    }
}
