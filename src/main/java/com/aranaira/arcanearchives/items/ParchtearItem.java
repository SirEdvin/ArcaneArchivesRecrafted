package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.HitResult;

public final class ParchtearItem extends ArcaneGemItem {
    public ParchtearItem() { super("parchtear", 8000, 27000); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server) || !server.getServer().isSameThread()
                || !player.isAlive() || player.isSpectator() || charge(stack) == 0) return InteractionResultHolder.success(stack);
        var start = player.getEyePosition();
        var end = start.add(player.getLookAngle().scale(40));
        BlockPos first = BlockPos.containing(start), last = BlockPos.containing(end);
        // Native ray tracing must not load chunks for a long-distance gem action.
        for (int x = Math.min(first.getX() >> 4, last.getX() >> 4); x <= Math.max(first.getX() >> 4, last.getX() >> 4); x++)
            for (int z = Math.min(first.getZ() >> 4, last.getZ() >> 4); z <= Math.max(first.getZ() >> 4, last.getZ() >> 4); z++)
                if (!level.hasChunkAt(new BlockPos(x << 4, first.getY(), z << 4))) return InteractionResultHolder.fail(stack);
        var hit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.success(stack);
        int radius = player.isShiftKeyDown() ? 0 : 1;
        int removed = 0;
        for (int x = -radius; x <= radius; x++) for (int y = -radius; y <= radius; y++) for (int z = -radius; z <= radius; z++) {
            BlockPos pos = hit.getBlockPos().offset(x, y, z);
            if (!level.hasChunkAt(pos) || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, hit.getDirection(), stack)) continue;
            // Waterlogged blocks are not fluid blocks; never destroy their host/container.
            if (level.getBlockState(pos).getBlock() instanceof LiquidBlock && level.getBlockEntity(pos) == null
                    && level.setBlock(pos, ContentRegistry.FAKE_AIR.get().defaultBlockState(), 3)) removed++;
        }
        if (removed > 0) {
            // Upstream pays after the whole cast, even when the remaining charge is smaller than the area.
            GemRecharge.consume(player, stack, removed);
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
        return InteractionResultHolder.success(stack);
    }
}
