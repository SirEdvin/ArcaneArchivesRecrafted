package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.config.ServerSideConfig;
import com.aranaira.arcanearchives.data.ResonatorSaveData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

public final class RadiantResonatorItem extends BlockItem {
    public RadiantResonatorItem(Block block) { super(block, new Properties()); }

    @Override public void onCraftedBy(net.minecraft.world.item.ItemStack stack, net.minecraft.world.level.Level level,
            net.minecraft.world.entity.player.Player player) {
        super.onCraftedBy(stack, level, player);
        com.aranaira.arcanearchives.events.TomeAcquisition.crafted(player, stack);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        int limit = ServerSideConfig.current().resonatorLimit();
        if (!level.isClientSide) {
            if (player == null || level.getServer().getPlayerList().getPlayer(player.getUUID()) != player)
                return InteractionResult.FAIL;
            if (limit != -1 && ResonatorSaveData.get(level.getServer()).count(level.getServer(), player.getUUID()) >= limit) {
                player.displayClientMessage(Component.translatable("arcanearchives.error.toomanyplaced", limit, getDescription()), true);
                return InteractionResult.FAIL;
            }
        }
        return super.place(context);
    }
}
