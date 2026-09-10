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


/** Original held-use invitation; creator eligibility is revalidated when it is consumed (0100). */
public final class LetterOfInvitationItem extends LetterItem {

    /** Called only after the live server/player boundary below; fixtures exercise this exact mutation path. */
    static String accept(ItemStack stack, UUID recipient, HiveSaveData hives) {
        CompoundTag tag = data(stack);
        if (stack.isEmpty() || !(stack.getItem() instanceof LetterOfInvitationItem)
                || !tag.contains("creator_name", Tag.TAG_STRING) || !tag.hasUUID("creator")) return "invalid";
        UUID author = tag.getUUID("creator");
        if (author.equals(recipient)) return "yours";
        if (!hives.acceptInvitation(author, recipient)) return "failed";
        stack.shrink(1); // Original also consumes a successful letter in creative mode.
        return "joined";
    }

    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || !canFinish(stack, level, player)) return stack;
        CompoundTag tag = data(stack);
        HiveSaveData hives = HiveSaveData.get(player.server);
        String outcome = accept(stack, player.getUUID(), hives);
        if (outcome.equals("invalid")) {
            player.sendSystemMessage(Component.literal("Invalid letter! Oops?"));
        } else {
            player.displayClientMessage(Component.translatable("arcanearchives.network.hive." + outcome,
                tag.getString("creator_name")), true);
        }
        if (outcome.equals("joined")) {
            UUID author = tag.getUUID("creator");
            var audience = new java.util.LinkedHashSet<>(hives.members(author));
            audience.add(author);
            // Upstream notifications target online players in the triggering world, not all dimensions.
            String name = player.server.getProfileCache().get(player.getUUID())
                .map(com.mojang.authlib.GameProfile::getName).orElse("Unknown");
            for (UUID id : audience) {
                if (id.equals(player.getUUID())) continue;
                Player listener = level.getPlayerByUUID(id);
                if (listener != null) listener.sendSystemMessage(Component.translatable(
                    "arcanearchives.network.hive.joined_your_network", name).withStyle(ChatFormatting.GOLD));
            }
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            player.server.overworld().getDataStorage().save(); // Original invitation flushes world saved data.
        }
        return stack;
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> text, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> text, TooltipFlag flag) {
    *///?}
        CompoundTag tag = data(stack);
        text.add((tag.contains("creator_name", Tag.TAG_STRING)
            ? Component.translatable("arcanearchives.tooltip.item.letter_invitation.named", tag.getString("creator_name"))
            : Component.translatable("arcanearchives.tooltip.item.letter_invitation")).withStyle(ChatFormatting.GOLD));
    }
}
