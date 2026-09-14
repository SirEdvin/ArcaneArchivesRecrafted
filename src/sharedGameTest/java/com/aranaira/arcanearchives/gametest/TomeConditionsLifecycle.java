package com.aranaira.arcanearchives.gametest;

import com.aranaira.arcanearchives.integration.patchouli.TomeConditions;
import com.aranaira.arcanearchives.config.ArsenalConfig;
import java.util.List;
import vazkii.patchouli.api.PatchouliAPI;

/** Complete predicate truth table plus installed API state; not rendered-book acceptance. */
public final class TomeConditionsLifecycle {
    public static void run() {
        var mods = List.of("thaumcraft", "astralsorcery", "botania", "potioncore", "quark");
        for (int mask = 0; mask < (1 << (mods.size() + 1)); mask++) {
            final int bits = mask;
            var calls = new java.util.ArrayList<String>();
            var flags = TomeConditions.flags((bits & 1) != 0, mod -> {
                calls.add(mod);
                int index = mods.indexOf(mod);
                require(index >= 0, "Tome queried an unaudited mod");
                return (bits & (1 << (index + 1))) != 0;
            });
            require(calls.equals(mods), "Tome loader predicates repeated or omitted");
            require(flags.size() == 14 && flags.keySet().stream().allMatch(s -> s.startsWith("arcanearchives:")),
                "Tome condition inventory/namespace changed");
            require(flags.get("arcanearchives:arsenal_enabled") == ((bits & 1) != 0)
                && flags.get("arcanearchives:arsenal_disabled") == ((bits & 1) == 0), "Arsenal visibility changed");
            for (int i = 0; i < mods.size(); i++) {
                boolean present = (bits & (1 << (i + 1))) != 0;
                require(flags.get("arcanearchives:ml_" + mods.get(i)) == present
                    && flags.get("arcanearchives:no_" + mods.get(i)) != present, "Optional visibility pair changed");
            }
            require(flags.get("arcanearchives:true") && !flags.get("arcanearchives:false"), "Constant visibility changed");
        }
        var api = PatchouliAPI.get();
        require(!api.isStub(), "Tome visibility used Patchouli's no-op stub");
        require(api.getConfigFlag("arcanearchives:arsenal_enabled") == ArsenalConfig.current().enableArsenal()
            && api.getConfigFlag("arcanearchives:arsenal_disabled") != ArsenalConfig.current().enableArsenal(),
            "Initialized Patchouli Arsenal flags differ from configured gate");
        require(api.getConfigFlag("arcanearchives:true") && !api.getConfigFlag("arcanearchives:false"),
            "Initialized Patchouli constant flags missing");
        for (String mod : mods) require(api.getConfigFlag("arcanearchives:ml_" + mod)
            != api.getConfigFlag("arcanearchives:no_" + mod), "Initialized optional flag pair missing");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
