package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.data.HiveSaveData;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
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
//? if >=1.21 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//?}

public final class WritOfExpulsionItem extends LetterItem {
    /** Server anvil result only. Unresolved/cleared names retain an existing binding, as upstream. */
    public static void bindTarget(ItemStack stack, Function<String, UUID> profiles) {
        if (stack.isEmpty() || !(stack.getItem() instanceof WritOfExpulsionItem)) return;
        //? if >=1.21 {
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName == null) return;
        String name = customName.getString();
        //?} else {
        /*if (!stack.hasCustomHoverName()) return;
        String name = stack.getHoverName().getString();
        *///?}
        UUID target = profiles.apply(name);
        if (target == null) return;
        //? if >=1.21 {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
        //?} else {
        /*CompoundTag tag = stack.getOrCreateTag();
        *///?}
            tag.putUUID("expel", target);
            tag.putString("expel_name", name);
        //? if >=1.21 {
        });
        //?}
    }

    static String expel(ItemStack stack, UUID player, HiveSaveData hives) {
        CompoundTag tag = data(stack);
        if (stack.isEmpty() || !(stack.getItem() instanceof WritOfExpulsionItem)
                || !tag.contains("creator_name", Tag.TAG_STRING) || !tag.hasUUID("creator")) return "invalid";
        if (!tag.contains("expel_name", Tag.TAG_STRING) || !tag.hasUUID("expel")) return "expel_unnamed";
        UUID target = tag.getUUID("expel");
        if (!tag.getUUID("creator").equals(player) || !player.equals(hives.ownerOf(target))) return "expel_no_permission";
        if (!hives.resign(target)) return "expel_failed";
        stack.shrink(1);
        return "expelled";
    }

    @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || !canFinish(stack, level, player)) return stack;
        CompoundTag tag = data(stack);
        HiveSaveData hives = HiveSaveData.get(player.server);
        var previousMembers = hives.members(player.getUUID());
        String outcome = expel(stack, player.getUUID(), hives);
        if (outcome.equals("invalid")) player.sendSystemMessage(Component.literal("Invalid letter! Oops?"));
        else player.displayClientMessage(Component.translatable("arcanearchives.network.hive." + outcome,
            tag.getString("expel_name")), true);
        if (outcome.equals("expelled")) {
            UUID target = tag.getUUID("expel");
            UUID remainingOwner = target.equals(player.getUUID()) ? previousMembers.iterator().next() : player.getUUID();
            var audience = new java.util.LinkedHashSet<>(hives.members(remainingOwner));
            boolean disbanded = audience.isEmpty();
            audience.add(remainingOwner);
            String name = player.server.getProfileCache().get(target).map(com.mojang.authlib.GameProfile::getName).orElse("Unknown");
            for (UUID id : audience) {
                if (id.equals(target)) continue;
                Player listener = level.getPlayerByUUID(id);
                if (listener != null) listener.sendSystemMessage(Component.translatable(
                    "arcanearchives.network.hive." + (disbanded ? "disbanded" : "left_your_network"), name).withStyle(ChatFormatting.GOLD));
            }
            Player expelled = level.getPlayerByUUID(target);
            if (expelled != null) expelled.sendSystemMessage(Component.translatable("arcanearchives.network.hive.were_expelled"));
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            player.server.overworld().getDataStorage().save();
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
        text.add((tag.contains("expel_name", Tag.TAG_STRING)
            ? Component.translatable("arcanearchives.tooltip.item.writ_expulsion.named", tag.getString("expel_name"))
            : Component.translatable("arcanearchives.tooltip.item.writ_expulsion")).withStyle(ChatFormatting.GOLD));
    }
}
