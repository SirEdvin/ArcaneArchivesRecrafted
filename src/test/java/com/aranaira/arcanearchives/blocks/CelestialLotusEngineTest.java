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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

class CelestialLotusEngineTest {
    private static Block block() {
        Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse("arcanearchives:celestial_lotus_engine"));
        assertNotSame(Blocks.AIR, block);
        return block;
    }

    @Test void preservesGlassMaterialHarvestingAndFullCollisionWithoutMachinery() {
        Block block = block();
        var state = block.defaultBlockState();
        assertEquals(15, state.getLightEmission());
        assertEquals(0.3F, state.getDestroySpeed(null, BlockPos.ZERO));
        assertFalse(state.requiresCorrectToolForDrops());
        assertFalse(state.hasBlockEntity());
        assertFalse(state.canOcclude());
        assertTrue(state.getProperties().isEmpty());
        assertSame(SoundType.STONE, block.getSoundType(state, null, BlockPos.ZERO, null));
        assertEquals(new AABB(0, 0, 0, 1, 1, 1), state.getCollisionShape(null, BlockPos.ZERO).bounds());
    }

    @Test void preservesBlockItemStackAndOriginalGoldTooltip() {
        Block block = block();
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse("arcanearchives:celestial_lotus_engine"));
        assertInstanceOf(BlockItem.class, item);
        assertSame(block, ((BlockItem) item).getBlock());
        ItemStack stack = new ItemStack(item);
        var lines = new ArrayList<Component>();
        item.appendHoverText(stack, Item.TooltipContext.EMPTY, lines, TooltipFlag.NORMAL);
        assertEquals(64, stack.getMaxStackSize());
        assertEquals(1, lines.size());
        assertEquals("arcanearchives.tooltip.device.celestial_lotus_engine", ((TranslatableContents) lines.get(0).getContents()).getKey());
        assertEquals(ChatFormatting.GOLD.getColor().intValue(), lines.get(0).getStyle().getColor().getValue());
    }
}
*///?}
