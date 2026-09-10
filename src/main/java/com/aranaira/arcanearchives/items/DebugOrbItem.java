package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Local-only storage diagnostics. Never uploads world data. */
public final class DebugOrbItem extends Item {
    public DebugOrbItem() { super(new Properties().stacksTo(1)); }

    public static boolean canModify(boolean creative, boolean operator, boolean spectator) {
        return creative && operator && !spectator;
    }
    private static boolean accessible(Player player, Level level, BlockPos pos) {
        return player != null && player.isAlive() && !player.isSpectator() && player.level() == level
            && level.hasChunkAt(pos) && level.mayInteract(player, pos)
            && player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(pos)) <= 36;
    }
    public static InteractionResult inspect(Player player, Level level, InteractionHand hand, BlockPos pos, boolean attack) {
        if (!accessible(player, level, pos) || !(player.getItemInHand(hand).getItem() instanceof DebugOrbItem))
            return InteractionResult.PASS;
        if (!attack && player.isShiftKeyDown()) return InteractionResult.PASS;
        BlockEntity device = level.getBlockEntity(pos);
        if (attack && (hand != InteractionHand.MAIN_HAND || !(device instanceof RadiantChestBlockEntity)))
            return InteractionResult.PASS;
        List<Component> report = attack ? chestDiagnostics((RadiantChestBlockEntity) device) : diagnostics(device);
        if (report.isEmpty()) return InteractionResult.PASS;
        if ((level.isClientSide && attack) || (!level.isClientSide && level.getServer().isSameThread())) {
            player.displayClientMessage(Component.literal((level.isClientSide ? "Client-side data: " : "Server-side data: ") + pos.toShortString()), false);
            for (Component line : report) player.displayClientMessage(line, false);
        }
        return InteractionResult.SUCCESS;
    }
    // Native early use retains block-interaction precedence; mutations stay in ordinary useOn.
    //? if !fabric {
    /*@Override public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return inspect(context.getPlayer(), context.getLevel(), context.getHand(), context.getClickedPos(), false);
    }
    *///?}
    @Override public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!accessible(player, level, pos)) return InteractionResult.FAIL;
        if (!player.isShiftKeyDown()) return inspect(player, level, context.getHand(), pos, false);
        if (!(level.getBlockEntity(pos) instanceof RadiantTroveBlockEntity trove)) return InteractionResult.PASS;
        if (!canModify(player.isCreative(), player.hasPermissions(2), player.isSpectator())) return InteractionResult.FAIL;
        if (!level.isClientSide) {
            if (!trove.canUse(player)) return InteractionResult.FAIL;
            boolean filled = toggleContents(trove);
            player.displayClientMessage(Component.literal(filled ? "Filled your empty trove!" : "Hope you didn't need what was in there!")
                .withStyle(filled ? ChatFormatting.GOLD : ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    // Called only after live server/access/creative/operator checks in useOn.
    static boolean toggleContents(RadiantTroveBlockEntity trove) {
        boolean fill = trove.inventory().getStackInSlot(0).isEmpty();
        ItemStack replacement = fill ? new ItemStack(Items.SNOWBALL) : ItemStack.EMPTY;
        if (fill) replacement.setCount(trove.inventory().getStackLimit(0, replacement));
        trove.restoreLockReference(ItemStack.EMPTY);
        trove.inventory().setStackInSlot(0, replacement);
        return fill;
    }
    static List<Component> chestDiagnostics(RadiantChestBlockEntity chest) {
        ItemStack display = chest.displayStack();
        return List.of(Component.literal("Radiant chest is named: " + chest.chestName()),
            Component.literal(display.isEmpty() ? "Radiant chest has no item stack on display."
                : "Radiant chest has a display item facing " + chest.displayFacing() + " which is: " + display));
    }
    public static List<Component> diagnostics(BlockEntity device) {
        List<Component> lines = new ArrayList<>();
        if (device instanceof RadiantChestBlockEntity chest) {
            lines.addAll(chestDiagnostics(chest));
            for (int slot = 0; slot < chest.inventory().getSlots(); slot++) {
                ItemStack stored = chest.inventory().getStackInSlot(slot);
                if (!stored.isEmpty()) lines.add(Component.literal("Slot " + slot + ": " + stored));
            }
        } else if (device instanceof RadiantTroveBlockEntity trove) {
            lines.add(Component.literal("Radiant Trove: " + trove.inventory().getStackInSlot(0)));
            lines.add(Component.literal("Size upgrades: " + trove.upgrades().getUpgradesCount() + "; void: " + trove.optionals().isVoiding()));
        } else if (device instanceof RadiantTankBlockEntity tank) {
            lines.add(Component.literal("Radiant Tank"));
            //? if fabric {
            long amount = tank.inventory().storedAmount() * 1000 / net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.BUCKET;
            Component name = net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes.getName(tank.inventory().getResource());
            //?} else {
            /*long amount = tank.inventory().storedAmount();
            Component name = tank.inventory().getFluid().getDisplayName();
            *///?}
            lines.add(Component.literal("Fluid: ").append(name).append("; " + amount + "mb"));
            lines.add(Component.literal("Size upgrades: " + tank.upgrades().getUpgradesCount() + "; void: " + tank.optionals().isVoiding()));
        }
        if (!lines.isEmpty()) lines.add(Component.literal("Network diagnostics are not yet migrated.").withStyle(ChatFormatting.GRAY));
        return lines;
    }
    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> text, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> text, TooltipFlag flag) {
    *///?}
        text.add(Component.translatable("arcanearchives.tooltip.item.debugorb").withStyle(ChatFormatting.GOLD));
        text.add(Component.translatable("arcanearchives.tooltip.creativeonly").withStyle(ChatFormatting.RED));
    }
}
