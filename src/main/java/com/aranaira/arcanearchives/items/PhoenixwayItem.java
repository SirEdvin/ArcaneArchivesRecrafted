package com.aranaira.arcanearchives.items;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;

public final class PhoenixwayItem extends ArcaneGemItem {
    public PhoenixwayItem() { super("phoenixway", 75, 300); }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gem = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server) || !server.getServer().isSameThread()
                || !player.isAlive() || player.isSpectator()) return InteractionResultHolder.success(gem);
        if (charge(gem) == 0) {
            GemRecharge.rechargeMaterial(player, gem);
            return InteractionResultHolder.success(gem);
        }
        // Upstream starts the ray at full entity height, not eye height.
        var start = player.position().add(0, player.getBbHeight(), 0);
        var end = start.add(player.getLookAngle().scale(40));
        BlockPos first = BlockPos.containing(start), last = BlockPos.containing(end);
        for (int x = Math.min(first.getX() >> 4, last.getX() >> 4); x <= Math.max(first.getX() >> 4, last.getX() >> 4); x++)
            for (int z = Math.min(first.getZ() >> 4, last.getZ() >> 4); z <= Math.max(first.getZ() >> 4, last.getZ() >> 4); z++)
                if (!level.hasChunkAt(new BlockPos(x << 4, first.getY(), z << 4))) return InteractionResultHolder.fail(gem);
        var hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.success(gem);
        BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
        if (!level.hasChunkAt(pos) || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, hit.getDirection(), gem)
                || !level.getWorldBorder().isWithinBounds(pos) || level.isOutsideBuildHeight(pos)) return InteractionResultHolder.fail(gem);
        if (!level.isEmptyBlock(pos) || level.getBlockEntity(pos) != null) return InteractionResultHolder.success(gem);
        level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
        // Original spends a charge after an eligible placement attempt, including creative use.
        GemRecharge.consume(player, gem, 1);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        com.aranaira.arcanearchives.events.GemSound.send(player,
            com.aranaira.arcanearchives.events.GemSound.Effect.PHOENIXWAY);
        return InteractionResultHolder.success(gem);
    }
}
