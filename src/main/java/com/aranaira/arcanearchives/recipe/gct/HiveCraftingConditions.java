package com.aranaira.arcanearchives.recipe.gct;

import java.util.Objects;
import java.util.UUID;

/**
 * Letter recipe policy, not authorization. Resolve membership from authoritative server data
 * at craft time. A null hive owner means confirmed nonmembership, never a failed lookup.
 */
public final class HiveCraftingConditions {
    private HiveCraftingConditions() {}

    public static boolean canCraftInvitation(UUID player, UUID hiveOwner) {
        Objects.requireNonNull(player, "player");
        return hiveOwner == null || hiveOwner.equals(player);
    }

    public static boolean canCraftResignation(UUID player, UUID hiveOwner) {
        Objects.requireNonNull(player, "player");
        return hiveOwner != null;
    }

    public static boolean canCraftExpulsion(UUID player, UUID hiveOwner) {
        Objects.requireNonNull(player, "player");
        return player.equals(hiveOwner);
    }
}
