package com.aranaira.arcanearchives.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class OrderstoneItem extends ArcaneGemItem {
    public OrderstoneItem() { super("orderstone", 100, 400); }

    @Override public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var pos = context.getClickedPos();
        var gem = context.getItemInHand();
        if (!(context.getLevel() instanceof ServerLevel level) || !level.getServer().isSameThread()
                || player == null || !player.isAlive() || player.isSpectator() || charge(gem) == 0
                || !level.hasChunkAt(pos) || !level.mayInteract(player, pos)
                || !player.mayUseItemAt(pos, context.getClickedFace(), gem)
                || level.getBlockEntity(pos) != null) return InteractionResult.PASS;
        BlockState current = level.getBlockState(pos);
        Block replacement = null;
        int cost = 1;
        if (current.is(Blocks.GRAVEL) || current.is(Blocks.MOSSY_COBBLESTONE)) replacement = Blocks.COBBLESTONE;
        else if (current.is(Blocks.COBBLESTONE)) replacement = Blocks.STONE;
        else if (current.is(Blocks.SAND)) replacement = Blocks.COARSE_DIRT;
        else if (current.is(Blocks.COARSE_DIRT)) replacement = Blocks.DIRT;
        else if (current.is(Blocks.DIRT)) replacement = Blocks.MYCELIUM;
        else if (current.is(Blocks.MYCELIUM)) replacement = Blocks.PODZOL;
        else if (current.is(Blocks.PODZOL)) replacement = Blocks.GRASS_BLOCK;
        else if (current.is(Blocks.CRACKED_STONE_BRICKS) || current.is(Blocks.MOSSY_STONE_BRICKS)) replacement = Blocks.STONE_BRICKS;
        else if (current.is(Blocks.STONE_BRICKS)) { replacement = Blocks.CHISELED_STONE_BRICKS; cost = 4; }
        else if (current.is(Blocks.DAMAGED_ANVIL)) { replacement = Blocks.CHIPPED_ANVIL; cost = 25; }
        else if (current.is(Blocks.CHIPPED_ANVIL)) { replacement = Blocks.ANVIL; cost = 25; }
        if (replacement != null) {
            BlockState next = replacement.defaultBlockState();
            if (cost == 25) next = replacement.withPropertiesOf(current);
            // Synchronize clients without adding direct neighbor notifications.
            if (level.setBlock(pos, next, Block.UPDATE_CLIENTS)) {
                GemRecharge.consume(player, gem, cost);
                player.getInventory().setChanged();
            }
        }
        return InteractionResult.SUCCESS;
    }
}
