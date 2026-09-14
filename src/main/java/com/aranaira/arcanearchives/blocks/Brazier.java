package com.aranaira.arcanearchives.blocks;

import com.aranaira.arcanearchives.tileentities.BrazierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Installed device foundation; acquisition and deposit entry points are wired separately. */
public final class Brazier extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.box(.15, 0, .15, .85, .75, .85);
    //? if >=1.21 {
    public static final com.mojang.serialization.MapCodec<Brazier> CODEC = simpleCodec(Brazier::new);
    @Override public com.mojang.serialization.MapCodec<Brazier> codec() { return CODEC; }
    //?}
    public Brazier() { this(Properties.of().strength(3).requiresCorrectToolForDrops().lightLevel(state -> 15).noOcclusion()); }
    public Brazier(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new BrazierBlockEntity(pos, state); }
    private void deposit(net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand) {
        if (player.getItemInHand(hand).getItem() instanceof com.aranaira.arcanearchives.items.StorageScepterItem scepter
                && scepter.interact(player, hand, level, pos)) return;
        if (!level.isClientSide && hand == net.minecraft.world.InteractionHand.MAIN_HAND
                && level.getBlockEntity(pos) instanceof BrazierBlockEntity brazier) brazier.deposit(player);
    }

    //? if >=1.21 {
    @Override public net.minecraft.world.InteractionResult useWithoutItem(BlockState state, net.minecraft.world.level.Level level,
            BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hit) {
        deposit(level, pos, player, net.minecraft.world.InteractionHand.MAIN_HAND);
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public net.minecraft.world.ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state,
            net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        deposit(level, pos, player, hand);
        return net.minecraft.world.ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    //?} else {
    /*@Override public net.minecraft.world.InteractionResult use(BlockState state, net.minecraft.world.level.Level level,
            BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand,
            net.minecraft.world.phys.BlockHitResult hit) {
        deposit(level, pos, player, hand);
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }
    *///?}
    @Override public void entityInside(BlockState state, net.minecraft.world.level.Level level, BlockPos pos,
            net.minecraft.world.entity.Entity entity) {
        if (!level.isClientSide && entity instanceof net.minecraft.world.entity.item.ItemEntity item
                && level.getBlockEntity(pos) instanceof BrazierBlockEntity brazier) brazier.absorb(item);
    }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override
    //? if >=1.21 {
    public void appendHoverText(net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.Item.TooltipContext context,
            java.util.List<net.minecraft.network.chat.Component> text, net.minecraft.world.item.TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(net.minecraft.world.item.ItemStack stack, BlockGetter context,
            java.util.List<net.minecraft.network.chat.Component> text, net.minecraft.world.item.TooltipFlag flag) {
    *///?}
        text.add(net.minecraft.network.chat.Component.translatable("arcanearchives.tooltip.device.brazier")
            .withStyle(net.minecraft.ChatFormatting.GOLD));
    }
    @Override public void setPlacedBy(net.minecraft.world.level.Level level, BlockPos pos, BlockState state,
            net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BrazierBlockEntity brazier) brazier.recordPlacer(placer);
    }
}
