package com.aranaira.arcanearchives.integration;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;

import com.aranaira.arcanearchives.init.ContentRegistry;

import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.Test;

class ViewerHiddenItemsTest {
    @Test void disabledArsenalMatchesPinnedUpstreamExclusionsExactly() {
        assertEquals(Set.of("slaughtergleam", "murdergleam", "agegleam", "cleansegleam", "switchgleam",
            "salvegleam", "munchstone", "orderstone", "mindspindle", "elixirspindle", "mountaintear",
            "rivertear", "parchtear", "phoenixway", "stormway", "chromatic_powder", "full_spectrum_chromatic_powder"),
            ViewerHiddenItems.items(false).stream().map(item -> BuiltInRegistries.ITEM.getKey(item).getPath()).collect(Collectors.toSet()));
    }

    @Test void enabledArsenalHidesOnlyPowdersAndNeverOrdinaryItemsOrSocket() {
        assertEquals(Set.of(ContentRegistry.CHROMATIC_POWDER.get(), ContentRegistry.RAINBOW_CHROMATIC_POWDER.get()), ViewerHiddenItems.items(true));
        for (boolean enabled : new boolean[]{false, true}) {
            var hidden = ViewerHiddenItems.items(enabled);
            assertFalse(hidden.contains(ContentRegistry.RAW_QUARTZ.get()));
            assertFalse(hidden.contains(ContentRegistry.GEM_SOCKET.get()));
            assertFalse(hidden.contains(Items.DIAMOND));
            assertThrows(UnsupportedOperationException.class, () -> hidden.add(Items.DIAMOND));
        }
    }

    @Test void allPowderColorVariantsAreHiddenWithoutChangingStackData() {
        for (boolean enabled : new boolean[]{false, true}) {
            var hidden = ViewerHiddenItems.items(enabled);
            for (int color = 0; color <= 10; color++) {
                ItemStack stack = new ItemStack(ContentRegistry.CHROMATIC_POWDER.get(), 7);
                int tint = color;
                CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt("color", tint));
                ItemStack before = stack.copy();
                assertTrue(hidden.contains(stack.getItem()));
                assertTrue(ItemStack.matches(before, stack));
            }
        }
    }
}
*///?}
