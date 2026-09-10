package com.aranaira.arcanearchives.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import com.aranaira.arcanearchives.events.GemSound;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Shared inventory-material recharge, used by explicit recharge and depletion. */
public final class GemRecharge {
    private GemRecharge() {}

    private static boolean liveHeld(Player player, ItemStack gem) {
        return player.level() instanceof ServerLevel level && level.getServer().isSameThread()
            && player.isAlive() && !player.isSpectator()
            && AvailableGems.contains(player, gem);
    }

    public static boolean rechargeMaterial(Player player, ItemStack gem) {
        if (!liveHeld(player, gem) || ArcaneGemItem.charge(gem) != 0) return false;
        Item material;
        int limit, perItem;
        boolean message = false;
        if (gem.getItem() instanceof SlaughtergleamItem) {
            material = Items.GOLD_NUGGET; limit = 5; perItem = 12;
        } else if (gem.getItem() instanceof MunchstoneItem) {
            material = Items.BONE_MEAL; limit = 5; perItem = 12;
        } else if (gem.getItem() instanceof PhoenixwayItem) {
            material = Items.GUNPOWDER; limit = 3; perItem = 25;
        } else if (gem.getItem() instanceof ElixirspindleItem) {
            material = Items.NETHER_WART; limit = 5; perItem = 1; message = true;
        } else if (gem.getItem() instanceof MindspindleItem) {
            material = Items.BOOK; limit = 1; perItem = ArcaneGemItem.maximumCharge(gem); message = true;
        } else return false;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty() || !stack.is(material)) continue;
            int count = Math.min(limit, stack.getCount());
            if (message) player.displayClientMessage(Component.translatable("arcanearchives.message.usedtorecharge",
                stack.getHoverName(), count > 1 ? " x" + count : "", gem.getHoverName())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
            ArcaneGemItem.setCharge(gem, count * perItem);
            stack.shrink(count);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            if (gem.getItem() instanceof MindspindleItem)
                GemSound.send(player, GemSound.Effect.MINDSPINDLE);
            else if (gem.getItem() instanceof MunchstoneItem)
                GemSound.send(player, GemSound.Effect.MUNCHSTONE);
            else if (gem.getItem() instanceof PhoenixwayItem)
                GemSound.send(player, GemSound.Effect.PHOENIXWAY);
            return true;
        }
        return false;
    }

    public static boolean recharge(Player player, ItemStack gem) {
        if (!liveHeld(player, gem) || !(gem.getItem() instanceof ArcaneGemItem)) return false;
        if (rechargeMaterial(player, gem)) return true;
        // These upstream overrides deliberately have no powder fallback.
        if (gem.getItem() instanceof SlaughtergleamItem || gem.getItem() instanceof MunchstoneItem
                || gem.getItem() instanceof PhoenixwayItem) return false;
        int color = gemColor(gem.getItem());
        if (color == 0) return false;
        ItemStack selected = ItemStack.EMPTY;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty() || !(stack.getItem() instanceof ChromaticPowderItem powder)) continue;
            if (powder.isRainbow()) selected = stack;
            else if (ChromaticPowderItem.color(stack) == color) {
                selected = stack;
                break;
            }
        }
        if (selected.isEmpty()) return false;
        player.displayClientMessage(Component.translatable("arcanearchives.message.usedtorecharge",
            selected.getHoverName(), "", gem.getHoverName()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
        selected.shrink(1);
        ArcaneGemItem.setCharge(gem, ArcaneGemItem.maximumCharge(gem));
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        return true;
    }

    public static int gemColor(Item item) {
        if (item instanceof SlaughtergleamItem) return 1;
        if (item instanceof MountaintearItem || item instanceof PhoenixwayItem) return 2;
        if (item instanceof MurdergleamItem || item instanceof StormwayItem) return 3;
        if (item instanceof AgegleamItem || item instanceof MindspindleItem) return 4;
        if (item instanceof CleansegleamItem || item instanceof RivertearItem) return 6;
        if (item instanceof SwitchgleamItem || item instanceof ElixirspindleItem) return 7;
        if (item instanceof SalvegleamItem || item instanceof OrderstoneItem) return 8;
        if (item instanceof MunchstoneItem || item instanceof ParchtearItem) return 9;
        return 0;
    }

    public static void consume(Player player, ItemStack gem, int amount) {
        if (!liveHeld(player, gem) || !(gem.getItem() instanceof ArcaneGemItem) || amount < 0) return;
        ArcaneGemItem.setCharge(gem, ArcaneGemItem.charge(gem) - amount);
        player.getInventory().setChanged();
        if (ArcaneGemItem.charge(gem) == 0) recharge(player, gem);
        AvailableGems.changed(player);
    }
}
