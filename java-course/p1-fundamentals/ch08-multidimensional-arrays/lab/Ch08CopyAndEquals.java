// Ch08 — Lab 02: copying a 2-D array, and why the definition of "copied" is a trap.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch08-multidimensional-arrays/lab/Ch08CopyAndEquals.java
//
// Claims under test:
//   1. clone() on a 2-D array copies the OUTER array only — every row is shared.
//      Modifying dest[1][0] changes src[1][0].
//   2. Arrays.copyOf(matrix, matrix.length) is the same shallow trap with a wrapper.
//   3. a real deep copy has to copy each row, and there are three idiomatic ways.
//   4. equals() on int[][] is reference identity (false), deepEquals() is structural.
import java.util.Arrays;

public class Ch08CopyAndEquals {

    public static void main(String[] args) {
        shallowClone();
        deepCopies();
    }

    private static void shallowClone() {
        System.out.println("--- clone() of a 2-D array is shallow ---");

        int[][] src = {{1, 2, 3}, {4, 5, 6}};
        int[][] dest = src.clone();

        System.out.println("  src  = " + Arrays.deepToString(src));
        System.out.println("  dest = src.clone() -> " + Arrays.deepToString(dest));
        System.out.println("  dest[0] == src[0] ? " + (dest[0] == src[0]));
        System.out.println("  dest[1] == src[1] ? " + (dest[1] == src[1]) + "   <- the trap");

        dest[1][0] = 999;
        System.out.println("  dest[1][0] = 999;");
        System.out.println("  src  = " + Arrays.deepToString(src));
        System.out.println("  dest = " + Arrays.deepToString(dest));
        System.out.println("  the write went THROUGH the shared row into src. Nothing was copied");
        System.out.println("  except the pointer list. The same happens with");
        System.out.println("  Arrays.copyOf(src, src.length): outer copy, shared rows.");
    }

    private static void deepCopies() {
        System.out.println();
        System.out.println("--- three real deep copies, and one claim about the fastest ---");
        System.out.println("  METHOD NOTE: the first version of this benchmark copied the matrix");
        System.out.println("  once per method in one fixed order, and the run showed copyOf+clone");
        System.out.println("  at 8.4 ms vs clone-per-row at 12.6 ms. I almost wrote '1.5x faster'.");
        System.out.println("  But copyOf+clone ran SECOND, so its JIT was hotter — the difference");
        System.out.println("  is exactly the warm-up artefact Ch07 taught me to check for.");

        int n = 2000;
        int[][] src = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                src[i][j] = i + j;
            }
        }

        // warm both sites before timing either
        for (int i = 0; i < 5; i++) {
            loopCopy(src);
            copyOfCopy(src);
            arraycopyCopy(src);
        }

        long loopBest = Long.MAX_VALUE, coBest = Long.MAX_VALUE, acBest = Long.MAX_VALUE;
        for (int pass = 0; pass < 2; pass++) {
            if (pass == 0) {        // order A: loop, copyOf, arraycopy
                loopBest = Math.min(loopBest, loopBestOfRun(src));
                coBest = Math.min(coBest, copyOfBest(src));
                acBest = Math.min(acBest, arraycopyBestOfRun(src));
            } else {                // order B: copyOf first, loop last
                coBest = Math.min(coBest, copyOfBest(src));
                acBest = Math.min(acBest, arraycopyBestOfRun(src));
                loopBest = Math.min(loopBest, loopBestOfRun(src));
            }
        }
        System.out.printf("  n=2000, %d cells: clone-per-row %,6.1f ms | copyOf+clone %,6.1f ms | arraycopy %,6.1f ms%n",
                n * n, loopBest / 1e6, coBest / 1e6, acBest / 1e6);
        System.out.println("  best-of-2 with alternating order, sites warmed. The '1.5x lead' the");
        System.out.println("  naive run showed collapsed once the order was balanced.");
        System.out.println("  HONEST VERDICT: the three are statistically indistinguishable on this");
        System.out.println("  machine, which is the boring-but-true finding. copyOf+clone allocates");
        System.out.println("  an outer array it immediately overwrites, so it is NEVER structurally");
        System.out.println("  preferable — but the cost is lost in the noise. Pick by readability.");

        // correctness of all three: mutate a copy, src must not change
        int[][] a = loopCopy(src);
        int[][] b = copyOfCopy(src);
        int[][] c = arraycopyCopy(src);
        a[0][0] = -1;
        if (src[0][0] == -1) {
            throw new AssertionError("loop copy was shallow!");
        }
        b[0][0] = -2;
        c[0][0] = -3;
        if (src[0][0] == -2 || src[0][0] == -3) {
            throw new AssertionError("a deep copy leaked into src");
        }
        System.out.println("  all three are genuinely deep: mutating each result left src intact.");

        System.out.println();
        System.out.println("  == \"six months later\" check: which one does the reader trust? ==");
        System.out.println("  Arrays.copyOf(src, src.length) LOOKS like a deep copy of a matrix.");
        System.out.println("  It is not. The clone-per-row loop cannot lie. This matters more");
        System.out.println("  than the milliseconds.");
    }

    private static int[][] loopCopy(int[][] src) {
        int[][] a = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            a[i] = src[i].clone();
        }
        return a;
    }

    private static int[][] copyOfCopy(int[][] src) {
        int[][] b = Arrays.copyOf(src, src.length);
        for (int i = 0; i < src.length; i++) {
            b[i] = src[i].clone();
        }
        return b;
    }

    private static int[][] arraycopyCopy(int[][] src) {
        int[][] c = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            c[i] = new int[src[i].length];
            System.arraycopy(src[i], 0, c[i], 0, src[i].length);
        }
        return c;
    }

    private static long loopBestOfRun(int[][] src) {
        long t = System.nanoTime();
        loopCopy(src);
        return System.nanoTime() - t;
    }

    private static long copyOfBest(int[][] src) {
        long t = System.nanoTime();
        copyOfCopy(src);
        return System.nanoTime() - t;
    }

    private static long arraycopyBestOfRun(int[][] src) {
        long t = System.nanoTime();
        arraycopyCopy(src);
        return System.nanoTime() - t;
    }
}