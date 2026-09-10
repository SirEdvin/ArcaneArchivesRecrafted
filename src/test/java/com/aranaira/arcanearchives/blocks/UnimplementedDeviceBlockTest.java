package com.aranaira.arcanearchives.blocks;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

class UnimplementedDeviceBlockTest {
    private static final String[] NAMES = {"verdant_censer", "spellbook_library", "immanent_incubator",
        "echoing_conformance_chamber", "echoing_reverberation_chamber"};

    private static Block block(String name) {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse("arcanearchives:" + name));
        assertNotSame(Blocks.AIR, block, name);
        return block;
    }

    @Test void registeredDevicesPreserveLightHardnessAndFullCollisionWithoutMachines() {
        for (String name : NAMES) {
            var state = block(name).defaultBlockState();
            assertEquals(15, state.getLightEmission(), name);
            assertEquals(1.7F, state.getDestroySpeed(null, BlockPos.ZERO), name);
            assertFalse(state.hasBlockEntity(), name);
            assertEquals(new AABB(0, 0, 0, 1, 1, 1), state.getCollisionShape(null, BlockPos.ZERO).bounds(), name);
        }
    }

    @Test void blockItemsPreserveStackSizeAndBothOriginalWarningStyles() {
        for (String name : NAMES) {
            Block block = block(name);
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse("arcanearchives:" + name));
            assertInstanceOf(BlockItem.class, item);
            assertSame(block, ((BlockItem) item).getBlock());
            ItemStack stack = new ItemStack(item);
            ItemStack before = stack.copy();
            var lines = new ArrayList<Component>();
            item.appendHoverText(stack, Item.TooltipContext.EMPTY, lines, TooltipFlag.NORMAL);
            assertEquals(64, stack.getMaxStackSize());
            assertEquals(2, lines.size());
            assertEquals("arcanearchives.tooltip.notimplemented1", ((TranslatableContents) lines.get(0).getContents()).getKey());
            assertEquals("arcanearchives.tooltip.notimplemented2", ((TranslatableContents) lines.get(1).getContents()).getKey());
            assertEquals(ChatFormatting.RED.getColor().intValue(), lines.get(0).getStyle().getColor().getValue());
            assertEquals(ChatFormatting.RED.getColor().intValue(), lines.get(1).getStyle().getColor().getValue());
            assertTrue(lines.get(0).getStyle().isBold());
            assertTrue(lines.get(1).getStyle().isItalic());
            assertTrue(ItemStack.matches(before, stack));
        }
    }
}
*///?}
