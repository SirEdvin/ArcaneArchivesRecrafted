package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.data.HiveSaveData;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Creator-bound resignation, including the original ordered ownership succession. */
public final class LetterOfResignationItem extends LetterItem {
    static String resign(ItemStack stack, UUID player, HiveSaveData hives) {
        CompoundTag tag = data(stack);
        if (stack.isEmpty() || !(stack.getItem() instanceof LetterOfResignationItem)
                || !tag.contains("creator_name", Tag.TAG_STRING) || !tag.hasUUID("creator")) return "invalid";
        if (!tag.getUUID("creator").equals(player)) return "leaving_failed";
        if (!hives.resign(player)) return "left_failed";
        stack.shrink(1);
        return "left";
    }

    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || !canFinish(stack, level, player)) return stack;
        HiveSaveData hives = HiveSaveData.get(player.server);
        UUID previousOwner = hives.ownerOf(player.getUUID());
        var previousMembers = hives.members(previousOwner);
        String outcome = resign(stack, player.getUUID(), hives);
        if (outcome.equals("invalid")) {
            player.sendSystemMessage(Component.literal("Invalid letter! Oops?"));
        } else {
            player.displayClientMessage(Component.translatable("arcanearchives.network.hive." + outcome), true);
        }
        if (outcome.equals("left")) {
            UUID remainingOwner = previousOwner.equals(player.getUUID())
                ? previousMembers.iterator().next() : previousOwner;
            var audience = new java.util.LinkedHashSet<>(hives.members(remainingOwner));
            boolean disbanded = audience.isEmpty();
            audience.add(remainingOwner);
            String name = player.server.getProfileCache().get(player.getUUID())
                .map(com.mojang.authlib.GameProfile::getName).orElse("Unknown");
            for (UUID id : audience) {
                if (id.equals(player.getUUID())) continue;
                Player listener = level.getPlayerByUUID(id);
                if (listener != null) listener.sendSystemMessage(Component.translatable(
                    "arcanearchives.network.hive." + (disbanded ? "disbanded" : "left_your_network"), name)
                    .withStyle(ChatFormatting.GOLD));
            }
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            // Unlike invitations, upstream resignation only marks saved data dirty; native saves persist it.
        }
        return stack;
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> text, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> text, TooltipFlag flag) {
    *///?}
        text.add(Component.translatable("arcanearchives.tooltip.item.letter_resignation").withStyle(ChatFormatting.GOLD));
    }
}
