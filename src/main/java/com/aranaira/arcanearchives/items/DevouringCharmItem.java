package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.inventory.DevouringCharmMenu;
import com.aranaira.arcanearchives.inventory.handlers.ExtendedItemStackHandler;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
//? if >=1.21 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//?}

public final class DevouringCharmItem extends Item {
    private static final Map<Player, Long> LAST_SOUND = new WeakHashMap<>();
    public DevouringCharmItem() { super(new Properties()); }

    //? if !fabric {
    /*@Override public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader level,
            net.minecraft.core.BlockPos pos, Player player) { return true; }
    *///?}

    @Override public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return net.minecraft.world.InteractionResult.PASS;
        var device = context.getLevel().getBlockEntity(context.getClickedPos());
        if (device instanceof com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity trove) {
            if (!context.getLevel().isClientSide) trove.installUpgrade(player, context.getHand());
        } else if (device instanceof com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity tank) {
            if (!context.getLevel().isClientSide) tank.installUpgrade(player, context.getHand());
        } else return net.minecraft.world.InteractionResult.PASS;
        return net.minecraft.world.InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player.isAlive() && !player.isSpectator())
            player.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new DevouringCharmMenu(id, inventory, hand),
                Component.translatable("item.arcanearchives.devouring_charm")));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static NonNullList<ItemStack> filters(ItemStack stack, Level level) {
        NonNullList<ItemStack> filters = NonNullList.withSize(6, ItemStack.EMPTY);
        CompoundTag data = data(stack).getCompound("autovoid_handler");
        //? if >=1.21 {
        ContainerHelper.loadAllItems(data, filters, level.registryAccess());
        //?} else {
        /*ContainerHelper.loadAllItems(data, filters);
        *///?}
        return filters;
    }
    public static void saveFilters(ItemStack stack, List<ItemStack> values, Level level) {
        NonNullList<ItemStack> filters = NonNullList.withSize(6, ItemStack.EMPTY);
        for (int index = 0; index < 6; index++) {
            ItemStack filter = values.get(index).copy();
            if (!filter.isEmpty()) filter.setCount(1);
            filters.set(index, filter);
        }
        CompoundTag saved = new CompoundTag();
        //? if >=1.21 {
        ContainerHelper.saveAllItems(saved, filters, level.registryAccess());
        //?} else {
        /*ContainerHelper.saveAllItems(saved, filters);
        *///?}
        CompoundTag data = data(stack);
        data.put("autovoid_handler", saved);
        //? if >=1.21 {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        //?} else {
        /*stack.setTag(data);
        *///?}
    }
    private static CompoundTag data(ItemStack stack) {
        //? if >=1.21 {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        //?} else {
        /*return stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
        *///?}
    }

    public static boolean voidPickup(Player player, ItemEntity entity) {
        if (!(player.level() instanceof ServerLevel level) || !level.getServer().isSameThread()
                || entity.level() != level || !entity.isAlive() || entity.hasPickUpDelay()
                || !player.isAlive() || player.isSpectator()) return false;
        ItemStack incoming = entity.getItem();
        if (incoming.isEmpty()) return false;
        boolean matches = false;
        for (ItemStack stack : player.getInventory().items) {
            if (!(stack.getItem() instanceof DevouringCharmItem)) continue;
            for (ItemStack filter : filters(stack, level)) {
                if (!filter.isEmpty() && ExtendedItemStackHandler.sameItemAndData(incoming, filter)) {
                    matches = true;
                    break;
                }
            }
            if (matches) break;
        }
        if (!matches) return false;
        // Native pickup ownership is private; inspect its public saved representation without changing the entity.
        CompoundTag state = new CompoundTag();
        entity.addAdditionalSaveData(state);
        if (state.hasUUID("Owner") && !state.getUUID("Owner").equals(player.getUUID())) return false;
        incoming.setCount(0);
        entity.discard();
        long now = System.currentTimeMillis();
        if (now - LAST_SOUND.getOrDefault(player, 0L) >= 500) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.GENERIC_EAT,
                SoundSource.PLAYERS, .4F, .7F + level.random.nextFloat() * .6F);
            LAST_SOUND.put(player, now);
        }
        return true;
    }
    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(Component.translatable("arcanearchives.tooltip.item.devouring_charm").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("arcanearchives.devouring.warning").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("arcanearchives.devouring.storage_void").withStyle(ChatFormatting.RED));
    }
}
