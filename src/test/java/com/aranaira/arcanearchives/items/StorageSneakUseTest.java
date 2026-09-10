package com.aranaira.arcanearchives.items;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class StorageSneakUseTest {
    @Test void originalStorageChoicesAndMixedHandsMatchNativeHooks() {
        var enabled = List.of(ContentRegistry.DEVOURING_CHARM.get(), ContentRegistry.CONTAINMENT_FIELD.get(),
            ContentRegistry.MATERIAL_INTERFACE.get());
        for (var item : enabled) assertTrue(new ItemStack(item).doesSneakBypassUse(null, null, null));
        assertFalse(new ItemStack(ContentRegistry.MATRIX_BRACE.get()).doesSneakBypassUse(null, null, null));
        var stacks = List.of(ItemStack.EMPTY, new ItemStack(Items.STONE),
            new ItemStack(ContentRegistry.DEVOURING_CHARM.get()), new ItemStack(ContentRegistry.CONTAINMENT_FIELD.get()),
            new ItemStack(ContentRegistry.MATERIAL_INTERFACE.get()), new ItemStack(ContentRegistry.MATRIX_BRACE.get()),
            new ItemStack(ContentRegistry.SCEPTER_REVELATION.get()), new ItemStack(ContentRegistry.AGEGLEAM.get()),
            new ItemStack(ContentRegistry.MURDERGLEAM.get()));
        for (var main : stacks) for (var off : stacks) {
            var beforeMain = main.copy();
            var beforeOff = off.copy();
            assertEquals(main.doesSneakBypassUse(null, null, null) && off.doesSneakBypassUse(null, null, null),
                StorageScepterItem.handsBypassSneakUse(main, off));
            assertTrue(ItemStack.matches(beforeMain, main));
            assertTrue(ItemStack.matches(beforeOff, off));
        }
    }
}
*///?}
