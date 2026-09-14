package com.aranaira.arcanearchives.client;

/** Transient range-effect lifetime, matching the original particle's update ordering. */
public final class BrazierRangeState {
    private boolean showing;
    private int age;
    public boolean showing() { return showing; }
    public int age() { return age; }
    public void toggle() {
        showing = !showing;
        if (showing) age = 0;
    }
    public void tick(boolean ownerValid) {
        if (!showing) return;
        if (!ownerValid || age >= 20 * 60 * 10) showing = false;
        age++;
    }
}
