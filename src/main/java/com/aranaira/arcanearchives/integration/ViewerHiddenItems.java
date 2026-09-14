package com.aranaira.arcanearchives.integration;

import com.aranaira.arcanearchives.init.ContentRegistry;
import java.util.Set;
import net.minecraft.world.item.Item;

/** Upstream JEI index exclusions, shared with EMI; does not disable items or recipes. */
public final class ViewerHiddenItems {
    private ViewerHiddenItems() {}

    public static Set<Item> unfinished() {
        return Set.of(ContentRegistry.VERDANT_CENSER_ITEM.get(), ContentRegistry.SPELLBOOK_LIBRARY_ITEM.get(),
            ContentRegistry.IMMANENT_INCUBATOR_ITEM.get(), ContentRegistry.ECHOING_CONFORMANCE_CHAMBER_ITEM.get(),
            ContentRegistry.ECHOING_REVERBERATION_CHAMBER_ITEM.get(), ContentRegistry.CELESTIAL_LOTUS_ENGINE_ITEM.get(),
            ContentRegistry.MATRIX_RESERVOIR_ITEM.get(), ContentRegistry.MATRIX_DISTILLATE_ITEM.get(),
            ContentRegistry.MATRIX_BRACE.get(), ContentRegistry.OBSTRUCTION_CHARM.get(), ContentRegistry.SERENITY_CHARM.get());
    }

    public static Set<Item> items(boolean arsenalEnabled) {
        var hidden = new java.util.HashSet<>(unfinished());
        hidden.add(ContentRegistry.CHROMATIC_POWDER.get());
        hidden.add(ContentRegistry.RAINBOW_CHROMATIC_POWDER.get());
        if (!arsenalEnabled) hidden.addAll(Set.of(ContentRegistry.SLAUGHTERGLEAM.get(), ContentRegistry.MURDERGLEAM.get(),
            ContentRegistry.AGEGLEAM.get(), ContentRegistry.CLEANSEGLEAM.get(), ContentRegistry.SWITCHGLEAM.get(),
            ContentRegistry.SALVEGLEAM.get(), ContentRegistry.MUNCHSTONE.get(), ContentRegistry.ORDERSTONE.get(),
            ContentRegistry.MINDSPINDLE.get(), ContentRegistry.ELIXIRSPINDLE.get(), ContentRegistry.MOUNTAINTEAR.get(),
            ContentRegistry.RIVERTEAR.get(), ContentRegistry.PARCHTEAR.get(), ContentRegistry.PHOENIXWAY.get(),
            ContentRegistry.STORMWAY.get()));
        return Set.copyOf(hidden);
    }
}
