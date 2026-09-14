package com.aranaira.arcanearchives.client;

import java.text.DecimalFormat;

/** Pinned Trove HUD decimal abbreviations, separate from legacy binary MathUtils formatting. */
public final class TroveHudText {
    private TroveHudText() {}
    public static String count(long value) {
        int exponent = (int) Math.floor(Math.log10(value));
        int base = exponent / 3;
        if (exponent >= 3 && base < 7)
            return new DecimalFormat("~#0.0").format(value / Math.pow(10, base * 3)) + " kMBTPE".charAt(base);
        return new DecimalFormat("#,##0").format(value);
    }
}
