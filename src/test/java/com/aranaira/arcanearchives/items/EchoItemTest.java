package com.aranaira.arcanearchives.items;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.ArrayList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

class EchoItemTest {
    private static final RegistryAccess REGISTRIES = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    @Test void sourceAndEchoAreNotConsumedAndDecodedItemsAreIndependent() {
        ItemStack source = new ItemStack(Items.DIAMOND, 12);
        source.set(DataComponents.CUSTOM_NAME, Component.literal("Original"));
        ItemStack before = source.copy();
        ItemStack echo = EchoItem.echoFromItem(source);
        assertTrue(ItemStack.matches(before, source));
        assertEquals(1, echo.getCount());
        ItemStack contained = EchoItem.itemFromEcho(echo);
        assertEquals(1, contained.getCount());
        assertTrue(ItemStack.isSameItemSameComponents(source, contained));
        contained.set(DataComponents.CUSTOM_NAME, Component.literal("Changed"));
        contained.shrink(1);
        assertEquals(Component.literal("Original"), EchoItem.itemFromEcho(echo).getHoverName());
        assertEquals(1, echo.getCount());
        assertTrue(ItemStack.matches(before, source));
    }

    @Test void nativeSaveAndNetworkRoundTripsRetainSourceComponents() {
        ItemStack source = new ItemStack(Items.DIAMOND_SWORD);
        source.setDamageValue(7);
        source.set(DataComponents.CUSTOM_NAME, Component.literal("Remember me"));
        ItemStack echo = EchoItem.echoFromItem(source);
        ItemStack loaded = ItemStack.parse(REGISTRIES, echo.save(REGISTRIES)).orElseThrow();
        assertTrue(ItemStack.matches(source, EchoItem.itemFromEcho(loaded)));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), REGISTRIES);
        try {
            ItemStack.STREAM_CODEC.encode(buffer, echo);
            assertTrue(ItemStack.matches(echo, ItemStack.STREAM_CODEC.decode(buffer)));
        } finally { buffer.release(); }
    }

    @Test void emptyAndTypedNamesAndTooltipsPreserveReadOnlyBehavior() {
        ItemStack empty = new ItemStack(ContentRegistry.ECHO.get());
        ItemStack before = empty.copy();
        var lines = new ArrayList<Component>();
        empty.getItem().appendHoverText(empty, Item.TooltipContext.of(REGISTRIES), lines, TooltipFlag.NORMAL);
        assertEquals("arcanearchives.tooltip.invalid_echo", ((TranslatableContents) lines.get(0).getContents()).getKey());
        assertTrue(ItemStack.matches(before, empty));
        assertTrue(EchoItem.itemFromEcho(EchoItem.echoFromItem(ItemStack.EMPTY)).isEmpty());
        ItemStack echo = EchoItem.echoFromItem(new ItemStack(Items.DIAMOND));
        assertEquals("item.echo_typed.name", ((TranslatableContents) echo.getHoverName().getContents()).getKey());
        lines.clear();
        echo.getItem().appendHoverText(echo, Item.TooltipContext.of(REGISTRIES), lines, TooltipFlag.NORMAL);
        assertEquals("arcanearchives.tooltip.echo_contained", ((TranslatableContents) lines.get(0).getContents()).getKey());
    }
}
*///?}
