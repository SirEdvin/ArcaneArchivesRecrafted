package com.aranaira.arcanearchives.items;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.Test;

class AmphoraTooltipTest {
    @Test void missingClientWorldReportsUnknownWithoutMutatingLink() {
        var stack = new ItemStack(ContentRegistry.RADIANT_AMPHORA.get());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putLong("homeTank", new BlockPos(-3, 64, 7).asLong());
            tag.putString("homeTankDim", "minecraft:the_nether");
            tag.putInt("mode", 0);
        });
        var before = RadiantAmphoraItem.data(stack);
        var text = new ArrayList<Component>();
        RadiantAmphoraItem.appendLinkedTooltip(stack, null, text);
        assertEquals(2, text.size());
        assertEquals("", text.getFirst().getString());
        assertEquals(ChatFormatting.GOLD.getColor(), text.get(1).getStyle().getColor().getValue());
        var line = assertInstanceOf(TranslatableContents.class, text.get(1).getContents());
        assertEquals("arcanearchives.tooltip.amphora.linked", line.getKey());
        assertArrayEquals(new Object[]{-3, 64, 7, "minecraft:the_nether", Component.literal("Unknown fluid")}, line.getArgs());
        assertEquals(before, RadiantAmphoraItem.data(stack));
    }

    @Test void unlinkedOrMalformedItemsHaveNoLinkedLineAndStayUnmodified() {
        var stack = new ItemStack(ContentRegistry.RADIANT_AMPHORA.get());
        var text = new ArrayList<Component>();
        RadiantAmphoraItem.appendLinkedTooltip(stack, null, text);
        assertTrue(text.isEmpty());
        assertFalse(stack.has(DataComponents.CUSTOM_DATA));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putLong("homeTank", 0);
            tag.putString("homeTankDim", "not a dimension");
        });
        var before = RadiantAmphoraItem.data(stack);
        RadiantAmphoraItem.appendLinkedTooltip(stack, null, text);
        assertTrue(text.isEmpty());
        assertEquals(before, RadiantAmphoraItem.data(stack));
    }
}
*///?}
