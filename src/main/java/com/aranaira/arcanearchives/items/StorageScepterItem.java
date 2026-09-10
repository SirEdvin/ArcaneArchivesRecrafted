package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.inventory.StorageUpgradeMenu;
import com.aranaira.arcanearchives.inventory.RadiantTankStorage;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantCraftingTableBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Release scepter interactions for the currently migrated storage devices. */
public final class StorageScepterItem extends Item {
    public enum Kind { REVELATION, MANIPULATION, TRANSLOCATION }
    private final Kind kind;
    private final boolean manipulation;
    /** Fabric equivalent of the native two-hand gate for migrated bypass items. */
    public static boolean handsBypassSneakUse(ItemStack main, ItemStack off) {
        return stackBypassesSneakUse(main) && stackBypassesSneakUse(off);
    }
    private static boolean stackBypassesSneakUse(ItemStack stack) {
        return stack.isEmpty() || stack.getItem() instanceof StorageScepterItem
            || stack.getItem() instanceof DevouringCharmItem
            || stack.getItem() instanceof StorageComponentItem component && component.bypassesSneakUse()
            || stack.getItem() instanceof ArcaneGemItem gem && gem.bypassesSneakUse();
    }
    //? if !fabric {
    /*@Override public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader level,
            BlockPos pos, Player player) { return true; }
    *///?}
    public StorageScepterItem(boolean manipulation) {
        this(manipulation ? Kind.MANIPULATION : Kind.REVELATION);
    }
    public StorageScepterItem(Kind kind) {
        super(new Properties().stacksTo(1));
        this.kind = java.util.Objects.requireNonNull(kind);
        this.manipulation = kind == Kind.MANIPULATION;
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        if (manipulation && context.getPlayer() != null
                && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof RadiantChestBlockEntity chest
                && chest.editDisplay(context.getPlayer(), context.getClickedFace()))
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        return interact(context.getPlayer(), context.getHand(), context.getLevel(), context.getClickedPos())
            ? InteractionResult.sidedSuccess(context.getLevel().isClientSide) : InteractionResult.PASS;
    }
    public boolean interact(Player player, InteractionHand hand, Level level, BlockPos pos) {
        if (kind == Kind.TRANSLOCATION) return false;
        if (player == null || player.isSpectator() || !level.mayInteract(player, pos)) return false;
        var device = level.getBlockEntity(pos);
        if (!(device instanceof RadiantTroveBlockEntity || device instanceof RadiantTankBlockEntity
                || !manipulation && (device instanceof RadiantChestBlockEntity
                    || device instanceof RadiantCraftingTableBlockEntity
                    || device instanceof com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity
                    || device instanceof com.aranaira.arcanearchives.tileentities.RadiantResonatorBlockEntity))) return false;
        if (level.isClientSide || !manipulation && hand != InteractionHand.MAIN_HAND) return true;
        if (device.isRemoved() || player.level() != level || player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) > 64) return true;
        if (device instanceof RadiantTroveBlockEntity trove && trove.canUse(player)) {
            if (manipulation) StorageUpgradeMenu.open(player, trove.upgrades(), trove.optionals(), trove::canUse);
            else {
                ItemStack stored = trove.inventory().getStackInSlot(0);
                report(player, "radiant_trove." + (stored.isEmpty() ? "empty" : "item"), stored.getHoverName());
                report(player, "radiant_trove.count", stored.getCount(), trove.inventory().getStackLimit(0, stored));
                if (trove.optionals().isLocked()) report(player, trove.lockReference().isEmpty()
                    ? "radiant_trove.lock_waiting" : "radiant_trove.locked", trove.lockReference().getHoverName());
                if (trove.upgrades().getTotalUpgradesQuantity() > 0 || trove.optionals().getTotalUpgradesQuantity() > 0)
                    report(player, "radiant_trove.upgrades", trove.upgrades().getTotalUpgradesQuantity(), trove.optionals().getTotalUpgradesQuantity());
            }
        } else if (device instanceof RadiantTankBlockEntity tank && tank.canUse(player)) {
            if (manipulation) StorageUpgradeMenu.open(player, tank.upgrades(), tank.optionals(), tank::canUse);
            else {
                var storage = tank.inventory();
                if (storage.storedAmount() == 0) report(player, "radiant_tank.empty");
                else {
                    //? if fabric {
                    report(player, "radiant_tank.item", net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes.getName(storage.getResource()));
                    //?} else {
                    /*report(player, "radiant_tank.item", storage.getFluid().getDisplayName());
                    *///?}
                }
                long amount = storage.storedAmount();
                long capacity = RadiantTankStorage.capacityFor(tank.upgrades().getUpgradesCount());
                //? if fabric {
                amount = amount * 1000 / net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.BUCKET;
                capacity = capacity * 1000 / net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.BUCKET;
                //?}
                report(player, "radiant_tank.count", amount, capacity);
            }
        } else if (device instanceof RadiantCraftingTableBlockEntity table && table.canUse(player)) {
            var names = Component.empty();
            boolean empty = true;
            for (ItemStack stack : table.items()) {
                if (stack.isEmpty()) continue;
                if (!empty) names.append(", ");
                names.append(stack.getItem().getName(stack));
                empty = false;
            }
            report(player, "radiant_crafting." + (empty ? "empty" : "contains"), names);
        } else if (device instanceof RadiantChestBlockEntity chest && chest.stillValid(player)) {
            report(player, "radiant_chest." + (chest.chestName().isEmpty() ? "unnamed" : "name"), chest.chestName());
            int empty = 0;
            for (int slot = 0; slot < chest.inventory().getSlots(); slot++)
                if (chest.inventory().getStackInSlot(slot).isEmpty()) empty++;
            report(player, "radiant_chest.slots", empty);
        } else if (device instanceof com.aranaira.arcanearchives.tileentities.RadiantResonatorBlockEntity resonator) {
            report(player, "radiant_resonator.progress", resonator.progressPercentage());
            report(player, "radiant_resonator.status", resonator.statusMessage());
        } else if (device instanceof com.aranaira.arcanearchives.tileentities.MonitoringCrystalBlockEntity crystal) {
            report(player, "monitoring_crystal.facing", crystal.getBlockState().getValue(com.aranaira.arcanearchives.blocks.MonitoringCrystal.FACING).getOpposite().getName());
        }
        return true;
    }
    private static void report(Player player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("arcanearchives.data.scepter." + key, args)
            .withStyle(ChatFormatting.GOLD), false);
    }
    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> text, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> text, TooltipFlag flag) {
    *///?}
        text.add(Component.translatable("arcanearchives.tooltip.item.scepter_" + kind.name().toLowerCase(java.util.Locale.ROOT))
            .withStyle(ChatFormatting.GOLD));
    }
}
