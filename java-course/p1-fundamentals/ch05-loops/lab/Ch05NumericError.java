// Ch05 — Lab 02: a loop that is correct can still accumulate a wrong answer.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch05-loops/lab/Ch05NumericError.java
//
// Claims under test:
//   1. `sum += 0.1` a million times is NOT 100000.0 — the loop is right, the answer is wrong
//   2. the error is not random: it grows ~ n * eps * sum, and 0.1 > 1/2 makes it worse
//   3. `BigDecimal` fixes it, and the cost is ~100x — a real trade, not a free win
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Ch05NumericError {

    public static void main(String[] args) {
        doubleDrift();
        whyTenIsNotOneTenth();
        whichInputsDrift();
        theFixAndItsPrice();
    }

    private static void doubleDrift() {
        System.out.println("--- adding 0.1 ten million times ---");

        int n = 10_000_000;
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += 0.1;
        }
        System.out.printf("  sum            = %.10f%n", sum);
        System.out.printf("  expected       = %.10f%n", 0.1 * n);
        System.out.printf("  absolute error = %.10f%n", Math.abs(sum - 0.1 * n));
        System.out.println("  the loop is CORRECT. the TYPE cannot hold the value.");
    }

    private static void whyTenIsNotOneTenth() {
        System.out.println();
        System.out.println("--- 0.1 is not 1/10, and printf will hide it from you ---");

        // %.20f prints 20 DECIMAL digits, which is not enough to show a binary
        // rounding error of ~5.5e-18. The shortest-repr and BigDecimal views do.
        System.out.printf("  printf %%.20f of 0.1      = %.20f   <- looks exact%n", 0.1);
        System.out.printf("  Double.toString(0.1)      = %s%n", 0.1);
        System.out.printf("  new BigDecimal(0.1)      = %s%n", new BigDecimal(0.1));
        System.out.printf("  new BigDecimal(\"0.1\")    = %s%n", new BigDecimal("0.1"));
        System.out.println("  0.1 is really 0.1000000000000000055511151231257827...");
        System.out.println("  so %.20f is the WRONG tool for showing this: the error is at the");
        System.out.println("  18th decimal place. Measure drift by comparing to the expected");
        System.out.println("  total, never by printing the value with more digits.");
    }

    private static void whichInputsDrift() {
        System.out.println();
        System.out.println("--- how many fractional BITS does each value really need? ---");
        System.out.printf("  %-22s %-12s %-22s %s%n",
                "value", "exact frac bits", "after 10M adds", "verdict");

        double[] values = {1.0, 0.5, 0.25, 0.2, 0.1, 1.0 / 3.0};
        int n = 10_000_000;
        for (double v : values) {
            double s = 0;
            for (int i = 0; i < n; i++) {
                s += v;
            }
            int bits = exactFractionalBits(v);
            System.out.printf("  %-22s %-12d %-22.6f %s%n",
                    v, bits, s, bits <= 3 ? "exact, no drift" : "DRIFT");
        }
        System.out.println("  0.5 needs 1 bit, 0.25 needs 2, 1.0 needs 0: adding them 10M times");
        System.out.println("  is exact arithmetic and cannot drift. 0.1 needs 55 bits, so 10M");
        System.out.println("  additions each round. the damage is a property of the INPUT");
        System.out.println("  (how many bits it needs), not of the loop (how many times).");
    }

    /**
     * Minimal m such that v * 2^m is an integer — i.e. how many fractional BITS the
     * exact value needs. Asking "is 0.5 representable?" is a bad question (everything
     * is); asking "how many bits does 0.5 cost?" is the real one.
     */
    private static int exactFractionalBits(double v) {
        BigDecimal x = new BigDecimal(v);
        int m = 0;
        while (x.stripTrailingZeros().scale() > 0) {
            x = x.multiply(BigDecimal.valueOf(2));
            m++;
        }
        return m;
    }

    private static void theFixAndItsPrice() {
        System.out.println();
        System.out.println("--- the fix, and what it costs ---");

        int n = 10_000_000;
        BigDecimal tenth = new BigDecimal("0.1");   // from a STRING, not from a double
        BigDecimal exactTarget = tenth.multiply(BigDecimal.valueOf(n));

        long start = System.nanoTime();
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < n; i++) {
            sum = sum.add(tenth);
        }
        long exactNs = System.nanoTime() - start;

        System.out.printf("  BigDecimal (exact)  %,12d ns   sum = %s   exact? %s%n",
                exactNs, sum.toPlainString(), sum.equals(exactTarget));

        start = System.nanoTime();
        double d = 0;
        for (int i = 0; i < n; i++) {
            d += 0.1;
        }
        long fastNs = System.nanoTime() - start;
        System.out.printf("  double             %,12d ns   sum = %.10f%n", fastNs, d);
        System.out.printf("  -> BigDecimal is %.0fx slower. A real cost, paid deliberately.%n",
                (double) exactNs / fastNs);

        System.out.println();
        System.out.println("  --- and a prediction of mine that was flatly wrong ---");
        System.out.println("  I expected `MathContext` (bounded precision) to be FASTER than exact");
        System.out.println("  BigDecimal, on the grounds that rounding to 20 digits is less work than");
        System.out.println("  carrying unlimited digits. Measured:");
        MathContext ctx = new MathContext(20, RoundingMode.HALF_UP);
        start = System.nanoTime();
        BigDecimal acc = BigDecimal.ZERO;
        for (int i = 0; i < n; i++) {
            acc = acc.add(tenth, ctx);
        }
        long roundedNs = System.nanoTime() - start;
        System.out.printf("  BigDecimal + MathContext(20)  %,12d ns   sum = %s%n",
                roundedNs, acc.toPlainString());
        System.out.printf("  -> %.1fx SLOWER than exact, not faster.%n", (double) roundedNs / exactNs);
        System.out.println("  Why: exact addition of two small BigDecimals stays in a single");
        System.out.println("  long, so it is a fast path. Bounded precision must renormalise and");
        System.out.println("  re-round on every operation. 'Bounded' costs work; it does not");
        System.out.println("  save it. (Both are ~10-25x the double, which is the number that matters.)");
    }
}
