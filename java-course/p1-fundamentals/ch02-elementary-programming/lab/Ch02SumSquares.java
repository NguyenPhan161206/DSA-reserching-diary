// Ch02 — Lab 03 (exercises L3): sum of the first n squares, judged exactly.
// Five accumulators against the exact long-loop reference; the crossover n where
// each one first disagrees is the Ch 02 claim made measurable.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch02-elementary-programming/lab/Ch02SumSquares.java
public class Ch02SumSquares {

    // exact reference: O(n) loop, long accumulator
    static long loopLong(int n) {
        long s = 0;
        for (int i = 1; i <= n; i++) {
            s += (long) i * i;
        }
        return s;
    }

    static int loopInt(int n) {
        int s = 0;
        for (int i = 1; i <= n; i++) {
            s += i * i;
        }
        return s;
    }

    static float loopFloat(int n) {
        float s = 0f;
        for (int i = 1; i <= n; i++) {
            s += (float) i * i;          // widen FIRST (the i*i-in-int form overflows: honest bug found while building)
        }
        return s;
    }

    static double loopDouble(int n) {
        double s = 0.0;
        for (int i = 1; i <= n; i++) {
            s += (double) i * i;
        }
        return s;
    }

    static long formulaLong(int n) {
        return (long) n * (n + 1) * (2 * n + 1) / 6;
    }

    static int firstTrueIntMismatch(int limit) {
        // NOTE: loopInt(n) == (int) loopLong(n) is ALWAYS true by construction —
        // int arithmetic IS long arithmetic mod 2^32, so the "wrap" is exact. The
        // meaningful first failure is when the true sum stops fitting in an int.
        for (int n = 0; n <= limit; n++) {
            if (loopInt(n) != loopLong(n)) return n;
        }
        return -1;
    }

    static int firstMismatchFloat(int limit) {
        for (int n = 0; n <= limit; n++) {
            if (loopFloat(n) != (float) loopLong(n)) return n;
        }
        return -1;
    }

    static int firstMismatchDouble(int limit) {
        for (int n = 0; n <= limit; n++) {
            if (loopDouble(n) != (double) loopLong(n)) return n;
        }
        return -1;
    }

    public static void main(String[] args) {
        int[] table = {0, 1, 5, 1848, 300000, 1_000_000};

        System.out.println("crossovers (first n where each accumulator disagrees with the EXACT long sum):");
        System.out.println("  int:    n=" + firstTrueIntMismatch(300_000) + "   (first n where the true sum no longer fits an int)");
        System.out.println("  float:  n=" + firstMismatchFloat(300_000) + "   (float sum rounds; error becomes visible)");
        System.out.println("  double: n=" + firstMismatchDouble(1_000_000) + " (double sum crosses 2^53)");
        System.out.println("  note: comparing int sum against (int)longSum NEVER mismatches, because int");
        System.out.println("  arithmetic is exactly long arithmetic mod 2^32 — wrapping is not a bug, it is");
        System.out.println("  the type's own arithmetic.");

        System.out.println();
        System.out.printf("%-10s %-24s %-12s %-12s %-13s%n", "n", "exact", "int", "float", "double");
        System.out.println("  " + "-".repeat(70));
        for (int n : table) {
            long exact = loopLong(n);
            boolean iOk = loopInt(n) == exact;
            boolean fOk = loopFloat(n) == (float) exact;
            boolean dOk = loopDouble(n) == (double) exact;
            System.out.printf("%-10s %-24d %-12s %-12s %-13s  formula=%s%n",
                    n, exact,
                    iOk ? "ok" : loopInt(n) + " WRONG",
                    fOk ? "ok" : loopFloat(n) + " WRONG",
                    dOk ? "ok" : loopDouble(n) + " WRONG",
                    formulaLong(n) == exact ? "ok" : "MISMATCH");
        }

        int mismatch = 0;
        for (int n = 0; n <= 1_000_000; n++) {
            if (formulaLong(n) != loopLong(n)) mismatch++;
        }
        System.out.println();
        System.out.println("formula vs loop for every n in [0, 1000000]: " + (mismatch == 0 ? "0 mismatches" : mismatch + " mismatches"));
    }
}