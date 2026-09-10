package com.aranaira.arcanearchives.items;

import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
//? if >=1.21 {
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.component.ItemContainerContents;
import java.util.function.Supplier;
//?}

/** Registered upstream Echo state; no new acquisition or item-conversion action. */
public final class EchoItem extends Item {
    //? if >=1.21 {
    //? if fabric {
    private static final DataComponentType<ItemContainerContents> TYPE = net.minecraft.core.Registry.register(
        net.minecraft.core.registries.BuiltInRegistries.DATA_COMPONENT_TYPE, ContentRegistry.id("echo_source"), sourceType());
    private static final Supplier<DataComponentType<ItemContainerContents>> SOURCE = () -> TYPE;
    //?} else {
    /*private static final net.neoforged.neoforge.registries.DeferredRegister<DataComponentType<?>> COMPONENTS =
        net.neoforged.neoforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, "arcanearchives");
    private static final Supplier<DataComponentType<ItemContainerContents>> SOURCE = COMPONENTS.register("echo_source", EchoItem::sourceType);
    public static void registerComponents(net.neoforged.bus.api.IEventBus bus) { COMPONENTS.register(bus); }
    *///?}
    private static DataComponentType<ItemContainerContents> sourceType() {
        return DataComponentType.<ItemContainerContents>builder().persistent(ItemContainerContents.CODEC)
            .networkSynchronized(ItemContainerContents.STREAM_CODEC).build();
    }
    //?}

    public EchoItem() { super(new Properties()); }

    public static ItemStack echoFromItem(ItemStack source) {
        ItemStack copy = source.copy();
        copy.setCount(1);
        ItemStack echo = new ItemStack(ContentRegistry.ECHO.get());
        //? if >=1.21 {
        echo.set(SOURCE.get(), ItemContainerContents.fromItems(List.of(copy)));
        //?} else {
        /*echo.getOrCreateTag().put("source", copy.save(new net.minecraft.nbt.CompoundTag()));
        *///?}
        return echo;
    }

    public static ItemStack itemFromEcho(ItemStack echo) {
        //? if >=1.21 {
        return echo.getOrDefault(SOURCE.get(), ItemContainerContents.EMPTY).copyOne();
        //?} else {
        /*return echo.hasTag() ? ItemStack.of(echo.getTag().getCompound("source")) : ItemStack.EMPTY;
        *///?}
    }

    @Override public Component getName(ItemStack stack) {
        ItemStack contained = itemFromEcho(stack);
        return contained.isEmpty() ? super.getName(stack)
            : Component.translatable("item.echo_typed.name", contained.getHoverName());
    }

    @Override
    //? if >=1.21 {
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    //?} else {
    /*public void appendHoverText(ItemStack stack, net.minecraft.world.level.Level context, List<Component> tooltip, TooltipFlag flag) {
    *///?}
        ItemStack contained = itemFromEcho(stack);
        tooltip.add(contained.isEmpty()
            ? Component.translatable("arcanearchives.tooltip.invalid_echo").withStyle(ChatFormatting.RED)
            : Component.translatable("arcanearchives.tooltip.echo_contained", contained.getHoverName()).withStyle(ChatFormatting.GOLD));
    }
}
