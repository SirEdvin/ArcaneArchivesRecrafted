package com.aranaira.arcanearchives.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if <1.21 {
/*import net.minecraft.world.level.Level;
*///?}

/** Craftable storage components and native sneak-use upgrade installation. */
public final class StorageComponentItem extends Item {
    private final String tooltip;
    private final boolean bypassesSneakUse;

    public StorageComponentItem(String name) {
        super(new Properties());
        tooltip = "arcanearchives.tooltip.item." + name;
        bypassesSneakUse = name.equals("containment_field") || name.equals("material_interface");
    }

    public boolean bypassesSneakUse() { return bypassesSneakUse; }
    //? if !fabric {
    /*@Override public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader level,
            net.minecraft.core.BlockPos pos, net.minecraft.world.entity.player.Player player) { return bypassesSneakUse(); }
    *///?}

    @Override public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext context) {
        if (context.getPlayer() != null && context.getLevel().getBlockEntity(context.getClickedPos())
                instanceof com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity trove) {
            if (context.getLevel().isClientSide) return net.minecraft.world.InteractionResult.SUCCESS;
            return trove.installUpgrade(context.getPlayer(), context.getHand())
                ? net.minecraft.world.InteractionResult.CONSUME : net.minecraft.world.InteractionResult.PASS;
        }
        if (context.getPlayer() != null && context.getLevel().getBlockEntity(context.getClickedPos())
                instanceof com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity tank) {
            if (context.getLevel().isClientSide) return net.minecraft.world.InteractionResult.SUCCESS;
            return tank.installUpgrade(context.getPlayer(), context.getHand())
                ? net.minecraft.world.InteractionResult.CONSUME : net.minecraft.world.InteractionResult.PASS;
        }
        return super.useOn(context);
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> text, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> text, TooltipFlag flag) {
    *///?}
        text.add(Component.translatable(tooltip).withStyle(ChatFormatting.GOLD));
    }
}
