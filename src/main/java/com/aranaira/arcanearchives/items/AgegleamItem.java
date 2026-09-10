package com.aranaira.arcanearchives.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AgeableMob;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class AgegleamItem extends ArcaneGemItem {
    private static final int RECHARGE_TICKS = 3600;

    public AgegleamItem() { super("agegleam", 30, 150); }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server) || !server.getServer().isSameThread()
                || !player.isAlive() || player.isSpectator()) return InteractionResultHolder.success(stack);
        AABB bounds = new AABB(player.getX() - 3.5, player.getY() - 3.5, player.getZ() - 3.5,
            player.getX() + 3.5, player.getY() + 3.5, player.getZ() + 3.5);
        int cost = 0;
        // The original advances babies even at zero charge, and does not clamp age at adulthood.
        for (AgeableMob mob : level.getEntitiesOfClass(AgeableMob.class, bounds)) {
            BlockPos pos = mob.blockPosition();
            if (mob.isAlive() && mob.isBaby() && level.mayInteract(player, pos)) {
                mob.setAge(mob.getAge() + 8000);
                cost++;
            }
        }
        if (cost > 0) {
            GemRecharge.consume(player, stack, cost);
            player.getInventory().setChanged();
        }
        return InteractionResultHolder.success(stack);
    }

    public void tickAvailable(ItemStack stack, Player player) {
        Level level = player.level();
        if (!(level instanceof ServerLevel server) || !server.getServer().isSameThread()
                || !player.isAlive() || player.isSpectator()
                || !AvailableGems.contains(player, stack)
                || charge(stack) >= maximumCharge(stack)) return;
        var tag = data(stack);
        int timer = tag.contains("recharge") ? tag.getInt("recharge") - 1 : RECHARGE_TICKS;
        if (timer == 0) {
            setCharge(stack, charge(stack) + 1);
            timer = RECHARGE_TICKS;
        }
        final int remaining = timer;
        //? if >=1.21 {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
            stack, current -> current.putInt("recharge", remaining));
        //?} else {
        /*stack.getOrCreateTag().putInt("recharge", remaining);
        *///?}
        AvailableGems.changed(player);
    }
}
