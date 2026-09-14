package com.aranaira.arcanearchives.blocks;

import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
//? if >=1.21 {
import com.mojang.serialization.MapCodec;
import net.minecraft.world.ItemInteractionResult;
//?} else {
/*import net.minecraft.world.level.BlockGetter;
*///?}

public final class RadiantChest extends Block implements EntityBlock {
    //? if >=1.21 {
    public static final MapCodec<RadiantChest> CODEC = simpleCodec(RadiantChest::new);
    @Override public MapCodec<RadiantChest> codec() { return CODEC; }
    //?}

    public RadiantChest() {
        this(Properties.of().mapColor(MapColor.QUARTZ).strength(3F, 1200F).lightLevel(state -> 15)
            .noOcclusion().pushReaction(PushReaction.BLOCK));
    }
    public RadiantChest(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new RadiantChestBlockEntity(pos, state); }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player && level.getBlockEntity(pos) instanceof RadiantChestBlockEntity chest) {
            chest.setOwner(player.getUUID());
            //? if >=1.21 {
            var data = stack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
            var saved = data == null ? null : data.copyTag();
            //?} else {
            /*var saved = net.minecraft.world.item.BlockItem.getBlockEntityData(stack);
            *///?}
            if ((saved == null || !saved.contains("routingType")) && !chest.noNewStacks()
                    && com.aranaira.arcanearchives.events.PlayerPreferences.get(player).defaultRoutingNoNewItems())
                chest.toggleRoutingType();
        }
    }

    private void open(Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof com.aranaira.arcanearchives.items.StorageScepterItem scepter
                && scepter.interact(player, hand, level, pos)) return;
        if (level.getBlockEntity(pos) instanceof RadiantChestBlockEntity chest) {
            if (chest.editDisplay(player, hit.getDirection())) return;
            if (!level.isClientSide && chest.stillValid(player)) player.openMenu(chest);
        }
    }

    //? if >=1.21 {
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        open(level, pos, player, InteractionHand.MAIN_HAND, hit);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        open(level, pos, player, hand, hit);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    //?} else {
    /*@Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        open(level, pos, player, hand, hit);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    *///?}

    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            if (level.getBlockEntity(pos) instanceof RadiantChestBlockEntity chest) chest.dropContents();
            super.onRemove(state, level, pos, replacement, moving);
        }
    }
    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof RadiantChestBlockEntity chest ? chest.comparatorSignal() : 0;
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.device.radiant_chest").withStyle(ChatFormatting.GOLD));
    }
}
