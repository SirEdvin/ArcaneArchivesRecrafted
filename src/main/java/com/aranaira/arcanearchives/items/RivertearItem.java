package com.aranaira.arcanearchives.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;

public final class RivertearItem extends ArcaneGemItem {
    public RivertearItem() { super("rivertear", 25, 100); }
    public static boolean rechargeInWater(ItemEntity entity) {
        if (!(entity.level() instanceof ServerLevel level) || !level.getServer().isSameThread()
                || !entity.isAlive() || !entity.isInWater()) return false;
        ItemStack stack = entity.getItem();
        if (!(stack.getItem() instanceof RivertearItem) || charge(stack) >= maximumCharge(stack)) return false;
        ItemStack restored = stack.copy();
        setCharge(restored, maximumCharge(restored));
        entity.setItem(restored);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1F, 0.5F);
        return true;
    }
    //? if !fabric {
    /*@Override public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) { return rechargeInWater(entity); }
    *///?}
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server) || !server.getServer().isSameThread()
                || !player.isAlive() || player.isSpectator() || charge(stack) == 0) return InteractionResultHolder.success(stack);
        var start = player.getEyePosition();
        var end = start.add(player.getLookAngle().scale(40));
        BlockPos first = BlockPos.containing(start), last = BlockPos.containing(end);
        for (int x = Math.min(first.getX() >> 4, last.getX() >> 4); x <= Math.max(first.getX() >> 4, last.getX() >> 4); x++)
            for (int z = Math.min(first.getZ() >> 4, last.getZ() >> 4); z <= Math.max(first.getZ() >> 4, last.getZ() >> 4); z++)
                if (!level.hasChunkAt(new BlockPos(x << 4, first.getY(), z << 4))) return InteractionResultHolder.fail(stack);
        var hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.success(stack);
        BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
        if (!level.hasChunkAt(pos) || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, hit.getDirection(), stack))
            return InteractionResultHolder.fail(stack);
        var previous = level.getBlockState(pos);
        if ((!previous.isAir() && !previous.canBeReplaced()) || level.getBlockEntity(pos) != null)
            return InteractionResultHolder.fail(stack);
        if (level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3)) {
            if (!player.getAbilities().instabuild) GemRecharge.consume(player, stack, 1);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
        return InteractionResultHolder.success(stack);
    }
}
