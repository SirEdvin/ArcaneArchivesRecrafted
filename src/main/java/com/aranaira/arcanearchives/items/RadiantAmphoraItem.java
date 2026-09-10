package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantTankBlockEntity;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
//? if >=1.21 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
//?}

/** A link to a live Tank, not an independent copy of its fluid. */
public final class RadiantAmphoraItem extends Item {
    private static MinecraftServer server;
    public RadiantAmphoraItem() {
        super(new Properties().stacksTo(1));
        net.minecraft.world.level.block.DispenserBlock.registerBehavior(this, new DispenseAmphora());
    }
    public static void serverStarted(MinecraftServer value) { server = value; }
    public static void serverStopped(MinecraftServer value) { if (server == value) server = null; }

    public static CompoundTag data(ItemStack stack) {
        //? if >=1.21 {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        //?} else {
        /*return stack.hasTag() ? stack.getTag().copy() : new CompoundTag();
        *///?}
    }
    private static void update(ItemStack stack, Consumer<CompoundTag> change) {
        //? if >=1.21 {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, change);
        //?} else {
        /*change.accept(stack.getOrCreateTag());
        *///?}
    }
    public static boolean linked(ItemStack stack) {
        CompoundTag tag = data(stack);
        return tag.contains("homeTank", Tag.TAG_LONG) && ResourceLocation.tryParse(tag.getString("homeTankDim")) != null;
    }
    public static boolean filling(ItemStack stack) {
        CompoundTag tag = data(stack);
        return !tag.contains("mode", Tag.TAG_INT) || tag.getInt("mode") != 0;
    }
    public static void toggle(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (server == null || !server.isSameThread() || player.level().isClientSide || !player.isAlive() || player.isSpectator()
                || !stack.is(ContentRegistry.RADIANT_AMPHORA.get())) return;
        boolean fill = !filling(stack);
        update(stack, tag -> tag.putInt("mode", fill ? 1 : 0));
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        player.displayClientMessage(Component.translatable("arcanearchives.amphora.mode." + (fill ? "fill" : "drain")), true);
    }
    public static RadiantTankBlockEntity target(ItemStack stack) {
        if (server == null || !server.isSameThread() || stack.getCount() != 1
                || !stack.is(ContentRegistry.RADIANT_AMPHORA.get()) || !linked(stack)) return null;
        CompoundTag tag = data(stack);
        ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("homeTankDim"));
        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
        BlockPos pos = BlockPos.of(tag.getLong("homeTank"));
        if (level == null || !level.hasChunkAt(pos)) return null;
        return level.getBlockEntity(pos) instanceof RadiantTankBlockEntity tank && tank.isLiveServerStorage() ? tank : null;
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()
                || !(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof RadiantTankBlockEntity tank)) return InteractionResult.PASS;
        if (!context.getLevel().isClientSide) {
            if (!tank.canUse(player) || !player.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), context.getItemInHand())) return InteractionResult.FAIL;
            update(context.getItemInHand(), tag -> {
                tag.putLong("homeTank", context.getClickedPos().asLong());
                tag.putString("homeTankDim", context.getLevel().dimension().location().toString());
            });
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSpectator()) return InteractionResultHolder.pass(stack);
        var hit = getPlayerPOVHitResult(level, player, filling(stack) ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(stack);
        BlockPos targetPos = filling(stack) ? hit.getBlockPos() : hit.getBlockPos().relative(hit.getDirection());
        if (!level.mayInteract(player, hit.getBlockPos()) || !level.mayInteract(player, targetPos)
                || !player.mayUseItemAt(targetPos, hit.getDirection(), stack)) return InteractionResultHolder.fail(stack);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        RadiantTankBlockEntity tank = target(stack);
        if (tank == null || !tank.getLevel().mayInteract(player, tank.getBlockPos())) return InteractionResultHolder.fail(stack);
        if (!com.aranaira.arcanearchives.inventory.AmphoraFluidStorage.worldTransfer(stack, tank, (ServerLevel) level,
                targetPos, hit.getDirection(), player, hand, filling(stack))) return InteractionResultHolder.fail(stack);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.success(stack);
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> text, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, Level level, List<Component> text, TooltipFlag flag) {
    *///?}
        text.add(Component.translatable("arcanearchives.tooltip.item.radiant_amphora").withStyle(ChatFormatting.GOLD));
        text.add(Component.translatable("arcanearchives.amphora.controls"));
        text.add(Component.translatable("arcanearchives.amphora.mode." + (filling(stack) ? "fill" : "drain")));
    }

    /** Read only the supplied world; never consult the live server for client presentation. */
    public static void appendLinkedTooltip(ItemStack stack, Level level, List<Component> text) {
        if (!stack.is(ContentRegistry.RADIANT_AMPHORA.get()) || !linked(stack)) return;
        CompoundTag tag = data(stack);
        BlockPos pos = BlockPos.of(tag.getLong("homeTank"));
        String dimension = tag.getString("homeTankDim");
        Component fluid = Component.literal("Unknown fluid");
        if (level != null && level.dimension().location().toString().equals(dimension)
                && level.hasChunkAt(pos) && level.getBlockEntity(pos) instanceof RadiantTankBlockEntity tank
                && tank.inventory().storedAmount() > 0) {
            //? if fabric {
            fluid = net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes.getName(tank.inventory().getResource());
            //?} else {
            /*fluid = tank.inventory().getFluid().getDisplayName();
            *///?}
        }
        text.add(Component.empty());
        text.add(Component.translatable("arcanearchives.tooltip.amphora.linked", pos.getX(), pos.getY(), pos.getZ(), dimension, fluid)
            .withStyle(ChatFormatting.GOLD));
    }
}
