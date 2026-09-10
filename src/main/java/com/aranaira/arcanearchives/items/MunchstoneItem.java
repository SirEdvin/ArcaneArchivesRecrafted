package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.config.MunchstoneConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import com.aranaira.arcanearchives.events.GemSound;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class MunchstoneItem extends ArcaneGemItem {
    public MunchstoneItem() { super("munchstone", 60, 240); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gem = player.getItemInHand(hand);
        GemRecharge.rechargeMaterial(player, gem);
        return InteractionResultHolder.success(gem);
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var pos = context.getClickedPos();
        var gem = context.getItemInHand();
        if (!(context.getLevel() instanceof ServerLevel level) || !level.getServer().isSameThread()
                || player == null || !player.isAlive() || player.isSpectator() || charge(gem) == 0
                || !level.hasChunkAt(pos) || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, context.getClickedFace(), gem)
                || level.getBlockEntity(pos) != null) return InteractionResult.PASS;
        int food = MunchstoneConfig.food(BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString());
        if (food <= 0) return InteractionResult.PASS;
        // Preserve the original pre-added hunger/saturation charge calculation, not just its tooltip.
        long hunger = (long) player.getFoodData().getFoodLevel() + food;
        int hungerCost = hunger + food > 20 ? (int) Math.max(0, Math.min(20, 20 - hunger)) : food;
        float saturation = player.getFoodData().getSaturationLevel() + 1F;
        float saturationCost = saturation + 1F > 20 ? Math.max(0F, Math.min(20F, 20F - saturation)) : 1F;
        int cost = hungerCost + (int) saturationCost;
        if (cost > 0 && level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)) {
            player.getFoodData().eat(food, 1F);
            GemRecharge.consume(player, gem, cost);
            player.getInventory().setChanged();
            GemSound.send(player, GemSound.Effect.MUNCHSTONE);
        }
        return InteractionResult.SUCCESS;
    }

}
