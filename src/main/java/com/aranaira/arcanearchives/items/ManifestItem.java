package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.inventory.ManifestMenu;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Original nonconsuming Manifest; the server owns opening and listing. */
public final class ManifestItem extends Item {
    public ManifestItem() { super(new Properties().stacksTo(1)); }
    /** Upstream hotkey checks the 36 main inventory slots, not armor or the offhand. */
    public static boolean hasManifest(Player player) {
        return player.getInventory().items.stream().anyMatch(stack ->
            stack.is(com.aranaira.arcanearchives.init.ContentRegistry.MANIFEST.get()));
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) ManifestMenu.open(player);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> text, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> text, TooltipFlag flag) {
    *///?}
        text.add(Component.translatable("arcanearchives.tooltip.item.manifest").withStyle(ChatFormatting.GOLD));
    }
}
