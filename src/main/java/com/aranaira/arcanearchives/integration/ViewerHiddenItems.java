package com.aranaira.arcanearchives.integration;

import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.Set;
import net.minecraft.world.item.Item;

/** Upstream JEI index exclusions, shared with EMI; does not disable items or recipes. */
public final class ViewerHiddenItems {
    private ViewerHiddenItems() {}

    public static Set<Item> items(boolean arsenalEnabled) {
        if (arsenalEnabled) return Set.of(ContentRegistry.CHROMATIC_POWDER.get(), ContentRegistry.RAINBOW_CHROMATIC_POWDER.get());
        return Set.of(ContentRegistry.SLAUGHTERGLEAM.get(), ContentRegistry.MURDERGLEAM.get(),
            ContentRegistry.AGEGLEAM.get(), ContentRegistry.CLEANSEGLEAM.get(), ContentRegistry.SWITCHGLEAM.get(),
            ContentRegistry.SALVEGLEAM.get(), ContentRegistry.MUNCHSTONE.get(), ContentRegistry.ORDERSTONE.get(),
            ContentRegistry.MINDSPINDLE.get(), ContentRegistry.ELIXIRSPINDLE.get(), ContentRegistry.MOUNTAINTEAR.get(),
            ContentRegistry.RIVERTEAR.get(), ContentRegistry.PARCHTEAR.get(), ContentRegistry.PHOENIXWAY.get(),
            ContentRegistry.STORMWAY.get(), ContentRegistry.CHROMATIC_POWDER.get(), ContentRegistry.RAINBOW_CHROMATIC_POWDER.get());
    }
}
