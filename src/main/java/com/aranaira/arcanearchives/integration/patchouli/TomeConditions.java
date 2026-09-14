package com.aranaira.arcanearchives.integration.patchouli;

import com.aranaira.arcanearchives.config.ArsenalConfig;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import vazkii.patchouli.api.PatchouliAPI;

/** Original Tome predicates exposed through Patchouli's entry/page/component flag API. */
public final class TomeConditions {
    private TomeConditions() {}

    public static void initialize() {
        flags(ArsenalConfig.current().enableArsenal(), TomeConditions::loaded)
            .forEach((name, enabled) -> PatchouliAPI.get().setConfigFlag(name, enabled));
    }

    /** Pure projection: book visibility never registers items or enables integrations. */
    public static Map<String, Boolean> flags(boolean arsenal, Predicate<String> loaded) {
        Map<String, Boolean> flags = new LinkedHashMap<>();
        flags.put("arcanearchives:arsenal_enabled", arsenal);
        flags.put("arcanearchives:arsenal_disabled", !arsenal);
        for (String mod : new String[]{"thaumcraft", "astralsorcery", "botania", "potioncore", "quark"}) {
            boolean present = loaded.test(mod);
            flags.put("arcanearchives:ml_" + mod, present);
            flags.put("arcanearchives:no_" + mod, !present);
        }
        flags.put("arcanearchives:true", true);
        flags.put("arcanearchives:false", false);
        return Map.copyOf(flags);
    }

    private static boolean loaded(String mod) {
        //? if fabric {
        return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(mod);
        //?} else if forge {
        /*return net.minecraftforge.fml.ModList.get().isLoaded(mod);
        *///?} else {
        /*return net.neoforged.fml.ModList.get().isLoaded(mod);
        *///?}
    }
}
