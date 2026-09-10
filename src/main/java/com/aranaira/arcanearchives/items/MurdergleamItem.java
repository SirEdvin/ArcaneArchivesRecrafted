package com.aranaira.arcanearchives.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public final class MurdergleamItem extends ArcaneGemItem {
    public MurdergleamItem() { super("murdergleam", 30, 150); }
    @Override public boolean hasToggleMode() { return true; }
    public static boolean criticalHit(Player player, boolean vanillaCritical) {
        if (!(player.level() instanceof ServerLevel level) || !level.getServer().isSameThread()
                || !player.isAlive() || player.isSpectator()) return false;
        boolean force = false;
        for (var gem : AvailableGems.get(player)) {
            if (!(gem.getItem() instanceof MurdergleamItem)) continue;
            if (isToggledOn(gem) && charge(gem) > 0) {
                force = true;
                GemRecharge.consume(player, gem, 1);
                if (charge(gem) == 0) setToggle(gem, !isToggledOn(gem));
            } else if (vanillaCritical) {
                setCharge(gem, charge(gem) + 3);
                // Preserve the original full-charge toggle, including already-full gems.
                if (charge(gem) == maximumCharge(gem)) setToggle(gem, !isToggledOn(gem));
            } else continue;
            AvailableGems.changed(player);
        }
        return force;
    }
}
