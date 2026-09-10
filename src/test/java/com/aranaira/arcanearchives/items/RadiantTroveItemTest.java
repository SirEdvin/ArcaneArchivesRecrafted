package com.aranaira.arcanearchives.items;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.*;
import org.junit.jupiter.api.Test;

class RadiantTroveItemTest {
    private static final RegistryAccess REGISTRIES = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    private static RadiantTroveBlockEntity trove() {
        return new RadiantTroveBlockEntity(BlockPos.ZERO, ContentRegistry.RADIANT_TROVE.get().defaultBlockState());
    }
    private static ItemStack packed(RadiantTroveBlockEntity trove) {
        var stack = new ItemStack(ContentRegistry.RADIANT_TROVE_ITEM.get());
        trove.saveToItem(stack, REGISTRIES);
        return stack;
    }
    private static List<Component> tooltip(ItemStack stack) {
        var before = stack.copy();
        List<Component> lines = new ArrayList<>();
        stack.getItem().appendHoverText(stack, Item.TooltipContext.of(REGISTRIES), lines, TooltipFlag.NORMAL);
        assertTrue(ItemStack.matches(before, stack));
        return lines;
    }
    @Test void packedContentsPreserveExtendedCountNameRarityAndVoiding() {
        var trove = trove();
        assertTrue(trove.upgrades().insertItem(0, new ItemStack(ContentRegistry.MATRIX_BRACE.get()), false).isEmpty());
        assertTrue(trove.optionals().insertItem(0, new ItemStack(ContentRegistry.DEVOURING_CHARM.get()), false).isEmpty());
        var contents = new ItemStack(Items.DIAMOND, 12000);
        contents.set(DataComponents.CUSTOM_NAME, Component.literal("Stored treasure"));
        contents.set(DataComponents.RARITY, Rarity.EPIC);
        trove.inventory().setStackInSlot(0, contents);
        var lines = tooltip(packed(trove));
        var item = (TranslatableContents) lines.get(0).getContents();
        assertEquals("arcanearchives.tooltip.trove.items", item.getKey());
        var name = (Component) item.getArgs()[0];
        assertEquals("Stored treasure", name.getString());
        assertTrue(name.getStyle().isItalic());
        assertEquals(contents.getDisplayName().getStyle().getColor(), name.getStyle().getColor());
        assertArrayEquals(new Object[]{12000, RadiantTroveBlockEntity.capacity(contents, 2)},
            ((TranslatableContents) lines.get(1).getContents()).getArgs());
        assertEquals("arcanearchives.tooltip.trove.voiding", ((TranslatableContents) lines.get(2).getContents()).getKey());
        assertTrue(lines.get(2).getStyle().isBold());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE), lines.get(2).getStyle().getColor());
    }
    @Test void emptyLockReportsReferenceWithoutInventingContents() {
        var trove = trove();
        assertTrue(trove.optionals().insertItem(0, new ItemStack(ContentRegistry.RADIANT_KEY.get()), false).isEmpty());
        trove.restoreLockReference(new ItemStack(Items.DIAMOND));
        var lines = tooltip(packed(trove));
        var name = (Component) ((TranslatableContents) lines.get(0).getContents()).getArgs()[0];
        assertEquals(new ItemStack(Items.DIAMOND).getHoverName().getString(), name.getString());
        assertArrayEquals(new Object[]{0, RadiantTroveBlockEntity.capacity(ItemStack.EMPTY, 0)},
            ((TranslatableContents) lines.get(1).getContents()).getArgs());
        assertEquals(4, lines.size());
    }
    @Test void unpackedAndMalformedStateRemainDistinctAndReadOnly() {
        assertEquals(2, tooltip(new ItemStack(ContentRegistry.RADIANT_TROVE_ITEM.get())).size());
        assertEquals(4, tooltip(packed(trove())).size());
        var invalid = new ItemStack(ContentRegistry.RADIANT_TROVE_ITEM.get());
        var data = new CompoundTag();
        data.put("inventory", new CompoundTag());
        BlockItem.setBlockEntityData(invalid, ContentRegistry.RADIANT_TROVE_ENTITY.get(), data);
        var lines = tooltip(invalid);
        assertEquals("arcanearchives.tooltip.trove.invalid", ((TranslatableContents) lines.get(0).getContents()).getKey());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.RED), lines.get(0).getStyle().getColor());
    }
}
*///?}
