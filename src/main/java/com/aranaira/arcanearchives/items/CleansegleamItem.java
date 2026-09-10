package com.aranaira.arcanearchives.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class CleansegleamItem extends ArcaneGemItem {
    public CleansegleamItem() { super("cleansegleam", 30, 150); }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gem = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server) || !server.getServer().isSameThread()
                || !player.isAlive() || player.isSpectator() || gem.getItem() != this)
            return InteractionResultHolder.success(gem);
        if (charge(gem) == 0) {
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                ItemStack milk = player.getInventory().getItem(slot);
                if (!milk.is(Items.MILK_BUCKET)) continue;
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("arcanearchives.message.usedtorecharge",
                    milk.getHoverName(), "", gem.getHoverName()).withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD), true);
                milk.shrink(1);
                setCharge(gem, maximumCharge(gem));
                ItemStack bucket = new ItemStack(Items.BUCKET);
                player.getInventory().add(bucket);
                if (!bucket.isEmpty()) player.drop(bucket, false);
                player.getInventory().setChanged();
                player.containerMenu.broadcastChanges();
                break;
            }
        } else {
            int cost = cleanse(player);
            if (player.isShiftKeyDown()) {
                AABB bounds = new AABB(player.getX() - 3.5, player.getY() - 3.5, player.getZ() - 3.5,
                    player.getX() + 3.5, player.getY() + 3.5, player.getZ() + 3.5);
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bounds)) {
                    if (target.isAlive() && level.mayInteract(player, target.blockPosition())) cost += cleanse(target);
                }
            }
            if (cost > 0) {
                GemRecharge.consume(player, gem, cost);
                player.getInventory().setChanged();
            }
        }
        return InteractionResultHolder.success(gem);
    }

    private static int cleanse(LivingEntity target) {
        boolean affected = target.hasEffect(MobEffects.HUNGER) || target.hasEffect(MobEffects.CONFUSION)
            || target.hasEffect(MobEffects.POISON);
        target.removeEffect(MobEffects.HUNGER);
        target.removeEffect(MobEffects.CONFUSION);
        target.removeEffect(MobEffects.POISON);
        return affected ? 1 : 0;
    }
}
