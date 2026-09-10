package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.inventory.GemSocketMenu;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class GemSocketItem extends Item {
    public GemSocketItem() { super(new Properties().stacksTo(1)); }

    public static ItemStack find(Player player) {
        if (player.getMainHandItem().getItem() instanceof GemSocketItem) return player.getMainHandItem();
        ItemStack worn = WornGemSocket.socket(player);
        if (!worn.isEmpty()) return worn;
        // The original non-wearable fallback scans the complete native player inventory.
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof GemSocketItem) return stack;
        }
        return ItemStack.EMPTY;
    }
    public static ItemStack gem(ItemStack socket, Level level) {
        NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
        CompoundTag tag = ArcaneGemItem.data(socket).getCompound("gem");
        //? if >=1.21 {
        ContainerHelper.loadAllItems(tag, items, level.registryAccess());
        //?} else {
        /*ContainerHelper.loadAllItems(tag, items);
        *///?}
        return items.get(0);
    }
    public static void save(ItemStack socket, ItemStack gem, Level level) {
        NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
        items.set(0, gem.copy());
        CompoundTag tag = new CompoundTag();
        //? if >=1.21 {
        ContainerHelper.saveAllItems(tag, items, level.registryAccess());
        //?} else {
        /*ContainerHelper.saveAllItems(tag, items);
        *///?}
        ArcaneGemItem.updateData(socket, data -> data.put("gem", tag));
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        open(player);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }
    public static void open(Player player) {
        if (player == null || player.containerMenu != player.inventoryMenu) return;
        Level level = player.level();
        if (level instanceof net.minecraft.server.level.ServerLevel server && server.getServer().isSameThread()
                && player.isAlive() && !player.isSpectator()) {
            ItemStack socket = find(player);
            if (!socket.isEmpty()) player.openMenu(new SimpleMenuProvider(
                (id, inventory, owner) -> new GemSocketMenu(id, inventory, socket),
                Component.translatable("item.arcanearchives.gemsocket")));
        }
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
        if (context.registries() != null)
            ContainerHelper.loadAllItems(ArcaneGemItem.data(stack).getCompound("gem"), items, context.registries());
        ItemStack gem = items.get(0);
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemStack gem = level == null ? ItemStack.EMPTY : gem(stack, level);
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.bauble.gemsocket").withStyle(ChatFormatting.GOLD));
        if (gem.getItem() instanceof ArcaneGemItem)
            tooltip.add(Component.translatable("arcanearchives.tooltip.bauble.gemsocket.contains").append(" ")
                .append(gem.getHoverName()).append(" [" + ArcaneGemItem.charge(gem) + "/" + ArcaneGemItem.maximumCharge(gem) + "]"));
    }
}
