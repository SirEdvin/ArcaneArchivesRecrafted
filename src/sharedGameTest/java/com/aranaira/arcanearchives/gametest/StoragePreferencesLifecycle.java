package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.events.PlayerPreferences;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Authenticated preference dispatch, real withdrawals, placement and conversion. */
public final class StoragePreferencesLifecycle {
    public static void run(GameTestHelper helper, ServerPlayer player, BiConsumer<ItemStack, CompoundTag> itemData) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(1, 2, 1));
        require(level.isEmptyBlock(pos), "Storage preference fixture occupied");
        var original = PlayerPreferences.get(player);
        var inventory = new java.util.ArrayList<ItemStack>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) inventory.add(player.getInventory().getItem(slot).copy());
        boolean shift = player.isShiftKeyDown();
        boolean creative = player.getAbilities().instabuild;
        try {
            player.setPos(pos.getX() + 2, pos.getY(), pos.getZ() + 2);
            player.getAbilities().instabuild = false;
            require(original.trovesDispense() && !original.defaultRoutingNoNewItems(), "Wrong unsynchronized defaults");
            for (boolean value : new boolean[]{true, false}) {
                var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                try {
                    var message = new PlayerPreferences(value, value);
                    message.write(buffer);
                    require(buffer.readableBytes() == 2, "Preference payload is not bounded to two booleans");
                    var decoded = PlayerPreferences.read(buffer);
                    require(decoded.equals(message) && buffer.readableBytes() == 0, "Preference codec drift");
                    decoded.apply(player);
                } finally { buffer.release(); }
                require(PlayerPreferences.get(player).equals(new PlayerPreferences(value, value)), "Authenticated preference not applied");
                for (boolean sneaking : new boolean[]{false, true}) {
                    player.getInventory().clearContent();
                    player.setShiftKeyDown(sneaking);
                    level.setBlockAndUpdate(pos, ContentRegistry.RADIANT_TROVE.get().defaultBlockState());
                    var trove = (RadiantTroveBlockEntity) level.getBlockEntity(pos);
                    trove.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 128));
                    int expected = sneaking == value ? 64 : 1;
                    trove.withdraw(player);
                    require(trove.inventory().getStackInSlot(0).getCount() == 128 - expected
                        && player.getInventory().countItem(Items.DIAMOND) == expected, "Upstream Trove click formula or conservation changed");
                    clear(level, pos);
                }
                player.setShiftKeyDown(false);
                for (int saved : new int[]{-1, 0, 1}) {
                    var stack = new ItemStack(ContentRegistry.RADIANT_CHEST_ITEM.get());
                    if (saved >= 0) {
                        var data = new CompoundTag();
                        data.putString("id", "arcanearchives:radiant_chest");
                        data.putInt("routingType", saved);
                        itemData.accept(stack, data);
                    }
                    var context = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, stack,
                        new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
                    require(ContentRegistry.RADIANT_CHEST_ITEM.get().place(context).consumesAction(), "Chest preference placement failed");
                    var chest = (RadiantChestBlockEntity) level.getBlockEntity(pos);
                    require(chest.noNewStacks() == (saved < 0 ? value : saved == 1), "Routing default overwrote explicit saved state or was ignored");
                    clear(level, pos);
                }
                level.setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
                var vanilla = (net.minecraft.world.level.block.entity.ChestBlockEntity) level.getBlockEntity(pos);
                vanilla.setItem(0, new ItemStack(Items.EMERALD, 7));
                player.setShiftKeyDown(true);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ContentRegistry.RAW_QUARTZ.get()));
                var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
                require(ContentRegistry.RAW_QUARTZ.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction(),
                    "Chest conversion rejected preference fixture");
                var converted = (RadiantChestBlockEntity) level.getBlockEntity(pos);
                require(converted.noNewStacks() == value && converted.inventory().getStackInSlot(0).getCount() == 7
                    && player.getMainHandItem().isEmpty(), "Conversion preference lost contents or payment");
                clear(level, pos);
            }
        } finally {
            clear(level, pos);
            player.setShiftKeyDown(shift);
            player.getAbilities().instabuild = creative;
            original.apply(player);
            for (int slot = 0; slot < inventory.size(); slot++) player.getInventory().setItem(slot, inventory.get(slot));
        }
    }
    private static void clear(net.minecraft.server.level.ServerLevel level, BlockPos pos) {
        level.removeBlockEntity(pos);
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
