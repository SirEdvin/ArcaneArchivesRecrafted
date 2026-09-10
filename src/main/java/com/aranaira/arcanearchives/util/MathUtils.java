package com.aranaira.arcanearchives.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Legacy Manifest/HUD helpers; see docs/migration/FOUNDATION.md for compatibility contracts. */
public class MathUtils {
	// Keep the 1.12 X/Y/Z layout: modern BlockPos packing is not byte-compatible.
	private static final int NUM_X_BITS = 1 + Mth.log2(Mth.smallestEncompassingPowerOfTwo(30000000));
	private static final int NUM_Z_BITS = NUM_X_BITS;
	private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
	private static final int Y_SHIFT = 0 + NUM_Z_BITS;
	private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
	private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
	private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
	private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

	/**
	 * Legacy integer division truncates toward zero, including for negative inputs.
	 *
	 * @param a
	 * @param b
	 * @return (int)Math.floor(a / b)
	 */
	public static int intDivisionFloor (int a, int b) {
		return (a / b);
	}

	/**
	 * Legacy positive-count ceiling division used by Manifest scrolling.
	 *
	 * @param a
	 * @param b
	 * @return (int)Math.floor(a / b)
	 */
	public static int intDivisionCeiling (int a, int b) {
		return ((a + (b - 1)) / b);
	}

	public static String format (long v) {
		if (v < 1024) {
			return v + "";
		}
		int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
		return String.format("%.1f%s", (double) v / (1L << (z * 10)), " kmgtpe".charAt(z));
	}

	public static Vec3 vec3dFromLong (long serialized) {
		int i = (int) (serialized << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
		int j = (int) (serialized << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
		int k = (int) (serialized << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
		return new Vec3(i, j, k);
	}

	public static long vec3dToLong (Vec3 pos) {
		return ((long) pos.x & X_MASK) << X_SHIFT | ((long) pos.y & Y_MASK) << Y_SHIFT | ((long) pos.z & Z_MASK);
	}
}
