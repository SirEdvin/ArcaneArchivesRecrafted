package com.aranaira.arcanearchives.items;

//? if neoforge {
/*import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class GemSneakUseTest {
    private static final List<String> GEMS = List.of("agegleam", "switchgleam", "salvegleam", "cleansegleam",
        "rivertear", "parchtear", "mountaintear", "phoenixway", "murdergleam", "slaughtergleam",
        "stormway", "mindspindle", "elixirspindle", "munchstone", "orderstone");
    private static final Set<String> BYPASS = Set.of("agegleam", "switchgleam", "salvegleam", "cleansegleam",
        "rivertear", "parchtear", "mountaintear", "phoenixway");
    private static ItemStack stack(String name) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse("arcanearchives:" + name)));
    }

    @Test void registeredGemsKeepOriginalConstantWithoutMutatingState() {
        for (var name : GEMS) {
            var stack = stack(name);
            var gem = assertInstanceOf(ArcaneGemItem.class, stack.getItem());
            for (int charge : new int[] {0, ArcaneGemItem.maximumCharge(stack)}) {
                ArcaneGemItem.setCharge(stack, charge);
                ArcaneGemItem.updateData(stack, tag -> {
                    tag.putBoolean("toggle", true);
                    tag.putByte("upgrades", (byte) 15);
                });
                var before = stack.copy();
                assertEquals(BYPASS.contains(name), gem.bypassesSneakUse(), name);
                assertEquals(BYPASS.contains(name), stack.doesSneakBypassUse(null, null, null), name);
                assertTrue(ItemStack.matches(before, stack), name);
            }
        }
    }

    @Test void mixedHandsMatchNativeHooksForAllRegisteredGemsAndScepters() {
        var hands = new ArrayList<ItemStack>();
        hands.add(ItemStack.EMPTY);
        hands.add(new ItemStack(Items.STONE));
        for (var name : GEMS) hands.add(stack(name));
        for (var name : List.of("scepter_revelation", "scepter_manipulation", "scepter_translocation")) {
            var scepter = stack(name);
            assertInstanceOf(StorageScepterItem.class, scepter.getItem());
            hands.add(scepter);
        }
        for (var main : hands) for (var off : hands) {
            boolean expected = main.doesSneakBypassUse(null, null, null) && off.doesSneakBypassUse(null, null, null);
            assertEquals(expected, StorageScepterItem.handsBypassSneakUse(main, off));
        }
    }
}
*///?}
