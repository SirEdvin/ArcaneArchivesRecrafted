package com.aranaira.arcanearchives.items;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.aranaira.arcanearchives.init.ContentRegistry;
import com.aranaira.arcanearchives.tileentities.RadiantTroveBlockEntity;
import com.aranaira.arcanearchives.tileentities.RadiantChestBlockEntity;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.junit.jupiter.api.Test;

class DebugOrbTest {
    @Test void leftClickReportContainsOnlyOriginalChestPresentationFields() {
        var chest = new RadiantChestBlockEntity(BlockPos.ZERO, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
        chest.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 12));
        var report = DebugOrbItem.chestDiagnostics(chest);
        assertEquals(2, report.size());
        assertEquals("Radiant chest is named: " + chest.chestName(), report.get(0).getString());
        assertEquals("Radiant chest has no item stack on display.", report.get(1).getString());
        assertFalse(report.stream().anyMatch(line -> line.getString().contains("Slot ")));
        assertTrue(DebugOrbItem.diagnostics(chest).stream().anyMatch(line -> line.getString().contains("Slot 0:")));
        assertEquals(12, chest.inventory().getStackInSlot(0).getCount());
    }
    @Test void destructivePolicyRequiresBothPrivilegesAndRejectsSpectators() {
        for (boolean creative : new boolean[]{false, true})
            for (boolean operator : new boolean[]{false, true})
                for (boolean spectator : new boolean[]{false, true})
                    assertEquals(creative && operator && !spectator, DebugOrbItem.canModify(creative, operator, spectator));
    }
    @Test void deliberateFillAndClearUseExtendedCapacityAndLeaveUpgrades() {
        var trove = new RadiantTroveBlockEntity(BlockPos.ZERO, ContentRegistry.RADIANT_TROVE.get().defaultBlockState());
        trove.upgrades().setStackInSlot(0, new ItemStack(ContentRegistry.MATRIX_BRACE.get()));
        assertTrue(DebugOrbItem.toggleContents(trove));
        var contents = trove.inventory().getStackInSlot(0);
        assertTrue(contents.is(Items.SNOWBALL));
        assertEquals(trove.inventory().getStackLimit(0, contents), contents.getCount());
        assertFalse(DebugOrbItem.toggleContents(trove));
        assertTrue(trove.inventory().getStackInSlot(0).isEmpty());
        assertTrue(trove.upgrades().getStackInSlot(0).is(ContentRegistry.MATRIX_BRACE.get()));
    }
    @Test void registeredItemAndLocalReportsAreReadOnly() {
        var stack = new ItemStack(ContentRegistry.DEBUG_ORB.get());
        assertEquals(1, stack.getMaxStackSize());
        var text = new ArrayList<Component>();
        ContentRegistry.DEBUG_ORB.get().appendHoverText(stack, Item.TooltipContext.EMPTY, text, TooltipFlag.NORMAL);
        assertEquals(2, text.size());
        var chest = new RadiantChestBlockEntity(BlockPos.ZERO, ContentRegistry.RADIANT_CHEST.get().defaultBlockState());
        chest.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 12));
        var before = chest.inventory().getStackInSlot(0).copy();
        assertFalse(DebugOrbItem.diagnostics(chest).isEmpty());
        assertTrue(ItemStack.matches(before, chest.inventory().getStackInSlot(0)));
        assertTrue(DebugOrbItem.diagnostics(null).isEmpty());
        assertTrue(stack.getComponents().equals(new ItemStack(ContentRegistry.DEBUG_ORB.get()).getComponents()));
    }
}
*///?}
