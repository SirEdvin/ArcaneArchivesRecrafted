package com.aranaira.arcanearchives.items;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/** Native charge state shared by the migrated Arsenal gems. Reads never mutate item copies. */
public abstract class ArcaneGemItem extends Item {
    public static final byte UPGRADE_MATTER = 1, UPGRADE_POWER = 2, UPGRADE_SPACE = 4, UPGRADE_TIME = 8;
    private final int normalCharge;
    private final int upgradedCharge;
    private final String name;
    protected ArcaneGemItem(String name, int normalCharge, int upgradedCharge) {
        this(name, normalCharge, upgradedCharge, new Properties());
    }
    protected ArcaneGemItem(String name, int normalCharge, int upgradedCharge, Properties properties) {
        super(properties.stacksTo(1));
        this.name = name;
        this.normalCharge = normalCharge;
        this.upgradedCharge = upgradedCharge;
    }
    protected static CompoundTag data(ItemStack stack) {
        //? if >=1.21 {
        return stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
            net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        //?} else {
        /*return stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
        *///?}
    }
    protected static void updateData(ItemStack stack, java.util.function.Consumer<CompoundTag> change) {
        //? if >=1.21 {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA, stack, change);
        //?} else {
        /*change.accept(stack.getOrCreateTag());
        *///?}
    }
    public boolean hasToggleMode() { return false; }
    /** Upstream per-gem constant; independent of charge, upgrades and toggle state. */
    public boolean bypassesSneakUse() {
        return switch (name) {
            case "agegleam", "switchgleam", "salvegleam", "cleansegleam",
                 "rivertear", "parchtear", "mountaintear", "phoenixway" -> true;
            default -> false;
        };
    }
    //? if !fabric {
    /*@Override public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.level.LevelReader level,
            net.minecraft.core.BlockPos pos, net.minecraft.world.entity.player.Player player) {
        return bypassesSneakUse();
    }
    *///?}
    public static boolean isToggledOn(ItemStack stack) {
        return stack.getItem() instanceof ArcaneGemItem gem && gem.hasToggleMode() && data(stack).getBoolean("toggle");
    }
    protected static void setToggle(ItemStack stack, boolean enabled) {
        if (stack.getItem() instanceof ArcaneGemItem gem && gem.hasToggleMode()) updateData(stack, tag -> tag.putBoolean("toggle", enabled));
    }
    public static void toggle(net.minecraft.world.entity.player.Player player) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level) || !level.getServer().isSameThread()
                || !player.isAlive() || player.isSpectator()) return;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ArcaneGemItem gem) || !gem.hasToggleMode()) return;
        setToggle(stack, !isToggledOn(stack));
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
    }
    public static int maximumCharge(ItemStack stack) {
        if (!(stack.getItem() instanceof ArcaneGemItem gem)) return 0;
        return (upgrades(stack) & UPGRADE_POWER) != 0 ? gem.upgradedCharge : gem.normalCharge;
    }
    public static byte upgrades(ItemStack stack) {
        return stack.getItem() instanceof ArcaneGemItem ? data(stack).getByte("upgrades") : 0;
    }
    public static boolean hasUnlimitedCharge(ItemStack stack) {
        if (!(stack.getItem() instanceof ArcaneGemItem)) return false;
        CompoundTag tag = data(stack);
        // The pinned source tests "infinite" but reads "infinity". Preserve that distinction.
        return maximumCharge(stack) == 0 || tag.contains("infinite") && tag.getBoolean("infinity");
    }
    public static boolean isChargeEmpty(ItemStack stack) {
        return !hasUnlimitedCharge(stack) && charge(stack) == 0;
    }
    public static int charge(ItemStack stack) {
        CompoundTag data = data(stack);
        return data.contains("charge") ? Math.max(0, Math.min(maximumCharge(stack), data.getInt("charge"))) : maximumCharge(stack);
    }
    public static void setCharge(ItemStack stack, int amount) {
        if (!(stack.getItem() instanceof ArcaneGemItem)) return;
        int charge = Math.max(0, Math.min(maximumCharge(stack), amount));
        //? if >=1.21 {
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
            stack, data -> data.putInt("charge", charge));
        //?} else {
        /*stack.getOrCreateTag().putInt("charge", charge);
        *///?}
    }
    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        tooltip.add(hasUnlimitedCharge(stack) ? Component.literal("[Unlimited]")
            : Component.translatable("arcanearchives.gem.charge", charge(stack), maximumCharge(stack)));
        byte upgrades = upgrades(stack);
        if ((upgrades & UPGRADE_MATTER) != 0) tooltip.add(Component.translatable("arcanearchives.tooltip.gemupgrade.matter").withStyle(ChatFormatting.GREEN));
        if ((upgrades & UPGRADE_POWER) != 0) tooltip.add(Component.translatable("arcanearchives.tooltip.gemupgrade.power").withStyle(ChatFormatting.RED));
        if ((upgrades & UPGRADE_SPACE) != 0) tooltip.add(Component.translatable("arcanearchives.tooltip.gemupgrade.space").withStyle(ChatFormatting.BLUE));
        if ((upgrades & UPGRADE_TIME) != 0) tooltip.add(Component.translatable("arcanearchives.tooltip.gemupgrade.time").withStyle(ChatFormatting.RED));
        if (hasToggleMode()) {
            tooltip.add(Component.translatable(isToggledOn(stack) ? "arcanearchives.gem.on" : "arcanearchives.gem.off"));
            tooltip.add(Component.translatable("arcanearchives.gem.toggle_controls"));
        }
        tooltip.add(Component.translatable("arcanearchives.tooltip.gem." + name).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("arcanearchives.tooltip.gem.recharge." + name).withStyle(ChatFormatting.GOLD));
    }
}
