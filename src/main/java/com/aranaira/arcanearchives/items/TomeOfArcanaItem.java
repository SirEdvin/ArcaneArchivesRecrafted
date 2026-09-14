package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** The registered item identity binds every acquired stack to the same modern book. */
public final class TomeOfArcanaItem extends Item {
    public static final ResourceLocation BOOK = ContentRegistry.id("tome_arcana");
    public TomeOfArcanaItem() { super(new Properties().stacksTo(1)); }

    private static InteractionResult open(Level level) {
        if (!level.isClientSide) return InteractionResult.FAIL;
        vazkii.patchouli.api.PatchouliAPI.get().openBookGUI(BOOK);
        return InteractionResult.SUCCESS;
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return new InteractionResultHolder<>(open(level), player.getItemInHand(hand));
    }

    @Override public InteractionResult useOn(UseOnContext context) { return open(context.getLevel()); }

    @Override public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        com.aranaira.arcanearchives.events.TomeAcquisition.crafted(player, stack);
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> text, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> text, TooltipFlag flag) {
    *///?}
        text.add(Component.translatable("arcanearchives.tooltip.item.tome_arcana").withStyle(ChatFormatting.GOLD));
    }
}
