package com.aranaira.arcanearchives.client;

/** Pixel geometry shared by Manifest input, drawing and tooltip hit testing. */
final class ManifestScroll {
    private int offset;
    private int size;

    void reset(int entries) { size = entries; offset = 0; }
    void move(int pixels) { offset = Math.max(0, Math.min(maximum(), offset + pixels)); }
    int maximum() { return Math.max(0, (size + 8) / 9 - 9) * 18; }
    int first() { return offset / 18 * 9; }
    int thumb() { return maximum() == 0 ? 0 : Math.round(150F * offset / maximum()); }
    void drag(double trackY) {
        double fraction = Math.max(0, Math.min(1, (trackY - 6) / 150));
        offset = (int) Math.round(fraction * (maximum() / 6)) * 6;
    }
    int y(int index) { return index / 9 * 18 - offset; }
    int index(int x, int y) {
        if (x < 0 || x >= 162 || y < 0 || y >= 162) return -1;
        int index = (y + offset) / 18 * 9 + x / 18;
        return index < size ? index : -1;
    }
}
