// Ch08 — Lab 04 (exercises L3): blocked matrix multiplication derived from Ch08's
// chapter claim. Same nested loops, same O(n^3); the only difference is the ORDER
// memory is touched in.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch08-multidimensional-arrays/lab/Ch08Matmul.java
//
// Verifies:
//   1. correctness: multiplyBlocked == definitional multiply, including ragged b0
//      edges and a 200-case fuzz
//   2. wall-clock: blocking vs definition at n=1024, JIT-warmed, best of 3
import java.util.Random;

public class Ch08Matmul {

    static int[][] multiply(int[][] a, int[][] b) {
        int n = a.length;
        int[][] c = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += a[i][k] * b[k][j];
                }
                c[i][j] = sum;
            }
        }
        return c;
    }

    static int[][] multiplyBlocked(int[][] a, int[][] b, int b0) {
        int n = a.length;
        int[][] c = new int[n][n];
        for (int i0 = 0; i0 < n; i0 += b0) {
            for (int j0 = 0; j0 < n; j0 += b0) {
                for (int k0 = 0; k0 < n; k0 += b0) {
                    for (int i = i0; i < Math.min(i0 + b0, n); i++) {
                        for (int j = j0; j < Math.min(j0 + b0, n); j++) {
                            int sum = 0;
                            for (int k = k0; k < Math.min(k0 + b0, n); k++) {
                                sum += a[i][k] * b[k][j];
                            }
                            c[i][j] += sum;
                        }
                    }
                }
            }
        }
        return c;
    }

    static boolean equals(int[][] x, int[][] y) {
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; i++) {
            if (!java.util.Arrays.equals(x[i], y[i])) {
                return false;
            }
        }
        return true;
    }

    static int[][] randMat(int n, Random r, int max) {
        int[][] m = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                m[i][j] = r.nextInt(2 * max + 1) - max;
            }
        }
        return m;
    }

    public static void main(String[] args) {
        Random rnd = new Random(42);

        System.out.println("--- correctness: blocked == definition, including ragged edges ---");
        int[][] cases = {{1, 1}, {2, 1}, {3, 2}, {4, 3}, {5, 16}, {16, 4}};
        int fail = 0;
        for (int[] c : cases) {
            int n = c[0], b0 = c[1];
            int[][] a = randMat(n, rnd, 3), b = randMat(n, rnd, 3);
            boolean ok = equals(multiply(a, b), multiplyBlocked(a, b, b0));
            if (!ok) {
                fail++;
            }
            System.out.printf("  n=%2d b0=%2d -> %s%n", n, b0, ok ? "ok" : "MISMATCH");
        }

        System.out.println("  fuzz: 200 random cases, n in [1..7], b0 in [1..5]:");
        int fuzzFail = 0;
        for (int t = 0; t < 200; t++) {
            int n = 1 + rnd.nextInt(7);
            int b0 = 1 + rnd.nextInt(5);
            int[][] a = randMat(n, rnd, 10), b2 = randMat(n, rnd, 10);
            if (!equals(multiply(a, b2), multiplyBlocked(a, b2, b0))) {
                fuzzFail++;
            }
        }
        System.out.println("  " + (fuzzFail == 0 ? "0/200 mismatches" : fuzzFail + "/200 mismatches"));
        fail += fuzzFail;

        System.out.println();
        System.out.println("--- wall clock at n=1024, warmed, best of 3 ---");
        int n = 1024;
        Random r2 = new Random(7);
        int[][] a = randMat(n, r2, 5), b = randMat(n, r2, 5);

        // warm all sites
        int[][] warm = multiplyBlocked(a, b, 64);      // correctness is checked above
        if (warm[0][0] == 0) {
            System.out.print("");
        }

        long defBest = Long.MAX_VALUE;
        int reps = 3;
        for (int r = 0; r < reps; r++) {
            defBest = Math.min(defBest, timeMult(a, b));
        }
        System.out.printf("  definition (i,j,k)      %8.1f ms%n", defBest / 1e6);

        int[] blockSizes = {8, 32, 128, 512};
        for (int b0 : blockSizes) {
            long best = Long.MAX_VALUE;
            for (int r = 0; r < reps; r++) {
                best = Math.min(best, timeBlocked(a, b, b0));
            }
            System.out.printf("  blocked b0=%-4d          %8.1f ms   %.1fx %s%n",
                    b0, best / 1e6, (double) defBest / best,
                    best < defBest ? "faster" : "");
        }

        System.exit(fail == 0 ? 0 : 1);
    }

    static long timeMult(int[][] a, int[][] b) {
        long t = System.nanoTime();
        int[][] c = multiply(a, b);
        long dt = System.nanoTime() - t;
        return c[0][0] == Integer.MIN_VALUE ? dt : dt;
    }

    static long timeBlocked(int[][] a, int[][] b, int b0) {
        long t = System.nanoTime();
        int[][] c = multiplyBlocked(a, b, b0);
        long dt = System.nanoTime() - t;
        return c[0][0] == Integer.MIN_VALUE ? dt : dt;
    }
}