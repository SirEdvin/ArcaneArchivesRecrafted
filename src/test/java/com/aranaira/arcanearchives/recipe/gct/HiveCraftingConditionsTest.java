package com.aranaira.arcanearchives.recipe.gct;

import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HiveCraftingConditionsTest {
    private static final UUID PLAYER = new UUID(0, 1);
    private static final UUID OTHER = new UUID(0, 2);

    @Test
    void unaffiliatedPlayerCanOnlyCraftInvitation() {
        assertTrue(HiveCraftingConditions.canCraftInvitation(PLAYER, null));
        assertFalse(HiveCraftingConditions.canCraftResignation(PLAYER, null));
        assertFalse(HiveCraftingConditions.canCraftExpulsion(PLAYER, null));
    }

    @Test
    void ordinaryMemberCanOnlyCraftResignation() {
        assertFalse(HiveCraftingConditions.canCraftInvitation(PLAYER, OTHER));
        assertTrue(HiveCraftingConditions.canCraftResignation(PLAYER, OTHER));
        assertFalse(HiveCraftingConditions.canCraftExpulsion(PLAYER, OTHER));
    }

    @Test
    void ownerCanCraftAllThreeIncludingResignation() {
        UUID sameIdentity = UUID.fromString(PLAYER.toString());
        assertTrue(HiveCraftingConditions.canCraftInvitation(PLAYER, sameIdentity));
        assertTrue(HiveCraftingConditions.canCraftResignation(PLAYER, sameIdentity));
        assertTrue(HiveCraftingConditions.canCraftExpulsion(PLAYER, sameIdentity));
    }

    @Test
    void membershipChangesAreNotCached() {
        assertTrue(HiveCraftingConditions.canCraftInvitation(PLAYER, null));
        assertFalse(HiveCraftingConditions.canCraftInvitation(PLAYER, OTHER));
        assertTrue(HiveCraftingConditions.canCraftInvitation(PLAYER, PLAYER));
    }

    @Test
    void missingPlayerIsRejectedEvenWithoutMembership() {
        assertThrows(NullPointerException.class, () -> HiveCraftingConditions.canCraftInvitation(null, null));
        assertThrows(NullPointerException.class, () -> HiveCraftingConditions.canCraftResignation(null, OTHER));
        assertThrows(NullPointerException.class, () -> HiveCraftingConditions.canCraftExpulsion(null, null));
    }
}
