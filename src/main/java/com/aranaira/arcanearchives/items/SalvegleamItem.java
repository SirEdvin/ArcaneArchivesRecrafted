package com.aranaira.arcanearchives.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SalvegleamItem extends ArcaneGemItem {
    public SalvegleamItem() { super("salvegleam", 30, 150); }
    @Override public boolean hasToggleMode() { return true; }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
    public void tickAvailable(ItemStack stack, Player player) {
        Level level = player.level();
        if (!(level instanceof ServerLevel server) || !server.getServer().isSameThread()
                || !player.isAlive() || player.isSpectator()
                || !AvailableGems.contains(player, stack)
                || charge(stack) == 0 || !isToggledOn(stack) || player.getHealth() >= player.getMaxHealth()) return;
        var tag = data(stack);
        int pulse = tag.contains("pulse") ? tag.getInt("pulse") - 1 : 0;
        updateData(stack, current -> current.putInt("pulse", pulse == 0 ? 20 : pulse));
        if (pulse == 0) {
            player.heal(1F);
            GemRecharge.consume(player, stack, 1);
            if (charge(stack) == 0) setToggle(stack, false);
        }
        AvailableGems.changed(player);
    }
    public static void onAnimalKilled(LivingEntity victim, DamageSource source) {
        if (!(victim instanceof Animal) || !(victim.level() instanceof ServerLevel level)
                || !level.getServer().isSameThread() || !(source.getEntity() instanceof Player player)
                || player.level() != level || !player.isAlive() || player.isSpectator()) return;
        for (ItemStack stack : AvailableGems.get(player)) {
            if (stack.getItem() instanceof SalvegleamItem) {
                setCharge(stack, charge(stack) + (int) victim.getMaxHealth() / 2);
                if (charge(stack) == maximumCharge(stack)) setToggle(stack, true);
                AvailableGems.changed(player);
            }
        }
    }
}
