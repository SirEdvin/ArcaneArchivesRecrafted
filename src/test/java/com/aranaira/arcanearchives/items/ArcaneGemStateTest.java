package com.aranaira.arcanearchives.items;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;

import com.aranaira.arcanearchives.init.ContentRegistry;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class ArcaneGemStateTest {
    private static ItemStack gem() { return new ItemStack(ContentRegistry.AGEGLEAM.get()); }

    @Test void onlyPowerChangesCapacityAndCopyKeepsUpgradeData() {
        ItemStack stack = gem();
        int normal = ArcaneGemItem.maximumCharge(stack);
        ArcaneGemItem.updateData(stack, tag -> tag.putByte("upgrades", (byte) 13));
        assertEquals(normal, ArcaneGemItem.maximumCharge(stack));
        ArcaneGemItem.updateData(stack, tag -> tag.putByte("upgrades", (byte) 15));
        assertTrue(ArcaneGemItem.maximumCharge(stack) > normal);
        assertEquals(15, ArcaneGemItem.upgrades(stack.copy()));
    }

    @Test void unlimitedKeepsOriginalTwoKeyQuirkAndNumericCharge() {
        ItemStack stack = gem();
        ArcaneGemItem.setCharge(stack, 0);
        assertTrue(ArcaneGemItem.isChargeEmpty(stack));
        ArcaneGemItem.updateData(stack, tag -> tag.putBoolean("infinity", true));
        assertFalse(ArcaneGemItem.hasUnlimitedCharge(stack));
        ArcaneGemItem.updateData(stack, tag -> tag.putBoolean("infinite", false));
        assertTrue(ArcaneGemItem.hasUnlimitedCharge(stack));
        assertFalse(ArcaneGemItem.isChargeEmpty(stack));
        assertEquals(0, ArcaneGemItem.charge(stack));
        ArcaneGemItem.updateData(stack, tag -> {
            tag.putBoolean("infinite", true);
            tag.putBoolean("infinity", false);
        });
        assertFalse(ArcaneGemItem.hasUnlimitedCharge(stack));
    }

    @Test void presentationReadsDoNotWriteMissingTags() {
        ItemStack stack = gem();
        var before = ArcaneGemItem.data(stack);
        assertFalse(ArcaneGemItem.hasUnlimitedCharge(stack));
        assertFalse(ArcaneGemItem.isChargeEmpty(stack));
        assertEquals(0, ArcaneGemItem.upgrades(stack));
        assertEquals(before, ArcaneGemItem.data(stack));
    }
}
*///?}
