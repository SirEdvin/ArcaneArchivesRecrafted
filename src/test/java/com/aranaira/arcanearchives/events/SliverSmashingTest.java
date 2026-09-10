package com.aranaira.arcanearchives.events;

import com.aranaira.arcanearchives.config.ServerSideConfig;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SliverSmashingTest {
    @Test
    void everyDefaultRollPreservesInclusiveThresholdsAndFreeSingleBranch() {
        int clusters = 0, singles = 0, misses = 0;
        for (int value = 0; value < 100; value++) {
            final int roll = value;
            var outcome = SliverSmashing.roll(ServerSideConfig.DEFAULTS, bound -> bound == 100 ? roll : 0);
            if (value <= 20) {
                assertEquals(new SliverSmashing.Outcome(8, true), outcome);
                clusters++;
            } else if (value <= 60) {
                assertEquals(new SliverSmashing.Outcome(1, false), outcome);
                singles++;
            } else {
                assertEquals(new SliverSmashing.Outcome(0, false), outcome);
                misses++;
            }
        }
        assertEquals(21, clusters);
        assertEquals(40, singles);
        assertEquals(39, misses);
    }

    @Test
    void clusterMaximumIsExclusiveAndEqualBoundsNeedNoSecondRoll() {
        for (int offset = 0; offset < 16; offset++) {
            final int number = offset;
            assertEquals(new SliverSmashing.Outcome(8 + offset, true),
                SliverSmashing.roll(ServerSideConfig.DEFAULTS, bound -> bound == 100 ? 0 : number));
        }
        var fixed = new ServerSideConfig(3, 6000, 4, true, true, 0, 0, 64, 64);
        assertEquals(new SliverSmashing.Outcome(64, true), SliverSmashing.roll(fixed, bound -> {
            assertEquals(100, bound);
            return 0;
        }));
        assertEquals(new SliverSmashing.Outcome(0, false), SliverSmashing.roll(fixed, bound -> 1));
    }

    @Test
    void oldFilesUseDefaultsAndExplicitValuesRoundTripWithoutClamping() {
        assertEquals(ServerSideConfig.DEFAULTS, ServerSideConfig.fromProperties(new Properties()));
        var custom = new ServerSideConfig(3, 6000, 4, true, true, 100, 100, 1, 64);
        assertEquals(custom, ServerSideConfig.fromProperties(custom.toProperties()));
        assertTrue(SliverSmashing.roll(custom, bound -> 0).consumesQuartz());
        for (int[] invalid : new int[][]{{-1, 40, 8, 24}, {101, 40, 8, 24}, {20, -1, 8, 24},
                {20, 101, 8, 24}, {20, 40, 0, 24}, {20, 40, 24, 8}, {20, 40, 8, 65}}) {
            assertThrows(IllegalArgumentException.class,
                () -> new ServerSideConfig(3, 6000, 4, true, true, invalid[0], invalid[1], invalid[2], invalid[3]));
        }
    }
}
