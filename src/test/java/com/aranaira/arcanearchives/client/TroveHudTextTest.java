package com.aranaira.arcanearchives.client;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TroveHudTextTest {
    @Test void pinnedDecimalAbbreviationsPreserveZeroAndBoundaries() {
        Locale original = Locale.getDefault(Locale.Category.FORMAT);
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.US);
            assertEquals("0", TroveHudText.count(0));
            assertEquals("999", TroveHudText.count(999));
            assertEquals("~1.0k", TroveHudText.count(1000));
            assertEquals("~32.8k", TroveHudText.count(32768));
            assertEquals("~1.0M", TroveHudText.count(1000000));
            assertEquals("~2.1B", TroveHudText.count(Integer.MAX_VALUE));
        } finally { Locale.setDefault(Locale.Category.FORMAT, original); }
    }
}
