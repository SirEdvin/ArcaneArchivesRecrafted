package com.aranaira.arcanearchives.items;

import java.util.List;
import java.util.ArrayList;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if <1.21 {
/*import net.minecraft.world.level.Level;
*///?}

/** Raw-quartz ingredient and server-owned Radiant conversion. */
public final class RawQuartzItem extends Item {
    public RawQuartzItem() {
        super(new Properties());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!com.aranaira.arcanearchives.config.ServerSideConfig.current().inWorldChestConversion()) return InteractionResult.PASS;
        var player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) return InteractionResult.PASS;
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        if (state.getBlock() instanceof net.minecraft.world.level.block.CraftingTableBlock) {
            if (level.getBlockEntity(pos) != null || !level.mayInteract(player, pos)
                    || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) return InteractionResult.FAIL;
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (!level.setBlockAndUpdate(pos, ContentRegistry.RADIANT_CRAFTING_TABLE.get().defaultBlockState())) return InteractionResult.FAIL;
            if (level.getBlockEntity(pos) instanceof com.aranaira.arcanearchives.tileentities.RadiantCraftingTableBlockEntity table)
                table.setOwner(player.getUUID());
            // The original workbench conversion consumes quartz even in creative mode.
            context.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
        }
        if (!(state.getBlock() instanceof ChestBlock chestBlock)) return InteractionResult.PASS;
        if (!player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand()) || !level.mayInteract(player, pos))
            return InteractionResult.FAIL;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        var original = ChestBlock.getContainer(chestBlock, state, level, pos, true);
        if (original == null) return InteractionResult.FAIL;
        List<ItemStack> contents = new ArrayList<>();
        for (int slot = 0; slot < original.getContainerSize(); slot++) contents.add(original.getItem(slot).copy());
        // Removing a chest normally drops its inventory; escrow first, then publish or restore.
        for (int slot = 0; slot < original.getContainerSize(); slot++) original.setItem(slot, ItemStack.EMPTY);
        if (!level.setBlockAndUpdate(pos, ContentRegistry.RADIANT_CHEST.get().defaultBlockState())) {
            for (int slot = 0; slot < contents.size(); slot++) original.setItem(slot, contents.get(slot));
            original.setChanged();
            return InteractionResult.FAIL;
        }
        if (!(level.getBlockEntity(pos) instanceof RadiantChestBlockEntity radiant)) {
            for (ItemStack stack : contents) Block.popResource(level, pos.above(), stack);
            return InteractionResult.SUCCESS;
        }
        radiant.setOwner(player.getUUID());
        if (com.aranaira.arcanearchives.events.PlayerPreferences.get(player).defaultRoutingNoNewItems())
            radiant.toggleRoutingType();
        for (ItemStack stack : contents) {
            ItemStack remainder = radiant.inventory().insertItemStacked(stack, false);
            while (!remainder.isEmpty()) Block.popResource(level, pos.above(), remainder.split(remainder.getMaxStackSize()));
        }
        if (!player.getAbilities().instabuild) context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.item.raw_quartz").withStyle(ChatFormatting.GOLD));
    }
}
