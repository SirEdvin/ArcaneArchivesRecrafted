package com.aranaira.arcanearchives.blocks;

import com.aranaira.arcanearchives.tileentities.RadiantCraftingTableBlockEntity;
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

public final class RadiantCraftingTable extends Block implements EntityBlock {
    //? if >=1.21 {
    public static final MapCodec<RadiantCraftingTable> CODEC = simpleCodec(RadiantCraftingTable::new);
    @Override public MapCodec<RadiantCraftingTable> codec() { return CODEC; }
    //?}
    public RadiantCraftingTable() {
        this(Properties.of().mapColor(MapColor.QUARTZ).strength(3F).lightLevel(state -> 15)
            .noOcclusion().pushReaction(PushReaction.BLOCK));
    }
    public RadiantCraftingTable(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RadiantCraftingTableBlockEntity(pos, state);
    }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player
                && level.getBlockEntity(pos) instanceof RadiantCraftingTableBlockEntity table) table.setOwner(player.getUUID());
    }
    private void open(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof RadiantCraftingTableBlockEntity table && table.canUse(player))
            player.openMenu(table);
    }
    private boolean inspect(Level level, BlockPos pos, Player player, InteractionHand hand) {
        return player.getItemInHand(hand).getItem() instanceof com.aranaira.arcanearchives.items.StorageScepterItem scepter
            && scepter.interact(player, hand, level, pos);
    }
    //? if >=1.21 {
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        open(level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (inspect(level, pos, player, hand)) return ItemInteractionResult.sidedSuccess(level.isClientSide);
        open(level, pos, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    //?} else {
    /*@Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (inspect(level, pos, player, hand)) return InteractionResult.sidedSuccess(level.isClientSide);
        open(level, pos, player);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    *///?}
    @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())) {
            if (level.getBlockEntity(pos) instanceof RadiantCraftingTableBlockEntity table) table.dropContents();
            super.onRemove(state, level, pos, replacement, moving);
        }
    }
    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.device.radiant_crafting_table").withStyle(ChatFormatting.GOLD));
    }
}
