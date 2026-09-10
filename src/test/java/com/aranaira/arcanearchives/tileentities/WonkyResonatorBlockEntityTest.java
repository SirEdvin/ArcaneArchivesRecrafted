package com.aranaira.arcanearchives.tileentities;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class WonkyResonatorBlockEntityTest {
    @Test void reachesDurationBeforeResettingOnFollowingTick() {
        assertEquals(6000, WonkyResonatorBlockEntity.nextGrowth(5999, true, 6000));
        assertEquals(0, WonkyResonatorBlockEntity.nextGrowth(6000, true, 6000));
        assertEquals(1, WonkyResonatorBlockEntity.nextGrowth(0, true, 6000));
    }
    @Test void obstructionPausesInsteadOfResetting() {
        assertEquals(777, WonkyResonatorBlockEntity.nextGrowth(777, false, 6000));
        assertEquals(6000, WonkyResonatorBlockEntity.nextGrowth(6000, false, 6000));
    }
    @Test void usesConfiguredDurationAndPreservesExistingOutOfRangeSemantics() {
        assertEquals(2, WonkyResonatorBlockEntity.nextGrowth(1, true, 2));
        assertEquals(0, WonkyResonatorBlockEntity.nextGrowth(2, true, 2));
        assertEquals(0, WonkyResonatorBlockEntity.nextGrowth(8, true, 2));
        assertEquals(-1, WonkyResonatorBlockEntity.nextGrowth(-2, true, 2));
        assertEquals(0, WonkyResonatorBlockEntity.nextGrowth(0, true, 0));
    }
}
