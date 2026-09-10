package com.aranaira.arcanearchives.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
//? if >=1.21 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//?}

/** Original LetterTemplate use mechanics, with a shared authoritative completion boundary. */
public abstract class LetterItem extends Item {
    protected LetterItem() { super(new Properties()); }
    public static CompoundTag data(ItemStack stack) {
        //? if >=1.21 {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        //?} else {
        /*return stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
        *///?}
    }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }
    @Override
    //? if >=1.21 {
    public int getUseDuration(ItemStack stack, LivingEntity entity) { return 64; }
    //?} else {
    /*public int getUseDuration(ItemStack stack) { return 64; }
    *///?}
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isAlive() || player.isSpectator()) return InteractionResultHolder.pass(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }
    protected static boolean canFinish(ItemStack stack, Level level, ServerPlayer player) {
        return player.level() == level && player.isAlive() && !player.isSpectator() && player.server.isSameThread()
            && player.server.getPlayerList().getPlayer(player.getUUID()) == player && player.getUseItem() == stack;
    }
}
