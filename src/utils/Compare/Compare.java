package utils.Compare;

public class Compare {
	/** Offset to order signed double numbers lexicographically. */
	private static final long SGN_MASK = 0x8000000000000000L;
	/** Positive zero bits. */
	private static final long POSITIVE_ZERO_DOUBLE_BITS = Double.doubleToRawLongBits(+0.0);
	/** Negative zero bits. */
	private static final long NEGATIVE_ZERO_DOUBLE_BITS = Double.doubleToRawLongBits(-0.0);
	/** Mask used to clear the non-sign part of a long. */
	private static final long MASK_NON_SIGN_LONG = 0x7fffffffffffffffl;
	/** Exponent offset in IEEE754 representation. */
	private static final long EXPONENT_OFFSET = 1023l;
	/**
	 * <p>
	 * Largest double-precision floating-point number such that {@code 1 + EPSILON}
	 * is numerically equal to 1. This value is an upper bound on the relative error
	 * due to rounding real numbers to double precision floating-point numbers.
	 * </p>
	 * <p>
	 * In IEEE 754 arithmetic, this is 2<sup>-53</sup>.
	 * </p>
	 *
	 * @see <a href="http://en.wikipedia.org/wiki/Machine_epsilon">Machine
	 *      epsilon</a>
	 */
	public static double EPSILON = Double.longBitsToDouble((EXPONENT_OFFSET - 53l) << 52);

	public static int compareTo(double x, double y) {
		if (equals(x, y, EPSILON)) {
			return 0;
		} else if (x < y) {
			return -1;
		}
		return 1;
	}

	public static boolean equals(double d1, double d2, double epsilon) {
		return equals(d1, d2, 1) || abs(d2 - d1) <= epsilon;
	}

	public static double abs(double x) {
		return Double.longBitsToDouble(MASK_NON_SIGN_LONG & Double.doubleToRawLongBits(x));
	}

	public static boolean equals(final double x, final double y, final int maxUlps) {

		final long xInt = Double.doubleToRawLongBits(x);
		final long yInt = Double.doubleToRawLongBits(y);

		final boolean isEqual;
		if (((xInt ^ yInt) & SGN_MASK) == 0l) {
			// number have same sign, there is no risk of overflow
			isEqual = abs(xInt - yInt) <= maxUlps;
		} else {
			// number have opposite signs, take care of overflow
			final long deltaPlus;
			final long deltaMinus;
			if (xInt < yInt) {
				deltaPlus = yInt - POSITIVE_ZERO_DOUBLE_BITS;
				deltaMinus = xInt - NEGATIVE_ZERO_DOUBLE_BITS;
			} else {
				deltaPlus = xInt - POSITIVE_ZERO_DOUBLE_BITS;
				deltaMinus = yInt - NEGATIVE_ZERO_DOUBLE_BITS;
			}

			if (deltaPlus > maxUlps) {
				isEqual = false;
			} else {
				isEqual = deltaMinus <= (maxUlps - deltaPlus);
			}

		}

		return isEqual && !Double.isNaN(x) && !Double.isNaN(y);

	}
}
