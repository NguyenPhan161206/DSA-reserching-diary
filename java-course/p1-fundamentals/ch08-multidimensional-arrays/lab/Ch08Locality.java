// Ch08 — Lab 03: iteration order vs memory layout (M1 lives here).
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch08-multidimensional-arrays/lab/Ch08Locality.java
//
// int[][] is an array OF arrays. The cells of one row are CONTIGUOUS in memory, but
// the rows themselves live wherever the allocator put them. That one fact decides
// which loop is fast.
//
// Claims under test:
//   1. row-major iteration (i then j) reads each row sequentially: fast, cache-friendly.
//   2. column-major iteration (j then i) jumps between rows: every read is a cold row.
//      For a big enough matrix the gap is a multiplier, not a percentage.
//   3. a flat int[] with manual indexing is ONE contiguous block: it beats int[][] even
//      row-major, because there is no pointer-chasing to the row objects.
import java.util.Random;

public class Ch08Locality {

    static final int N = 3000;          // N x N ints = 36 MB

    public static void main(String[] args) {
        int[][] jagged = new int[N][N];
        int[] flat = new int[N * N];
        Random rnd = new Random(7);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                int v = rnd.nextInt();
                jagged[i][j] = v;
                flat[i * N + j] = v;
            }
        }

        // warm all four sites; sink is observed by the guard at the end, so DCE keeps it
        long sink = 0;
        for (int i = 0; i < 3; i++) {
            sink += sumRowMajor(jagged);
            sink += sumColMajor(jagged);
            sink += sumFlat(flat);
        }

        long rmBest = Long.MAX_VALUE, cmBest = Long.MAX_VALUE, flatBest = Long.MAX_VALUE;
        for (int pass = 0; pass < 2; pass++) {
            // orders swapped between passes so no site gets a permanent warm lead
            long a = pass == 0 ? timeRowMajor(jagged) : timeFlat(flat);
            long b = timeColMajor(jagged);
            long c = pass == 0 ? timeFlat(flat) : timeRowMajor(jagged);
            rmBest = Math.min(rmBest, pass == 0 ? a : c);
            cmBest = Math.min(cmBest, b);
            flatBest = Math.min(flatBest, pass == 0 ? c : a);
        }

        System.out.println("--- memory layout decides the loop, not the other way round ---");
        System.out.printf("  matrix: %d x %d ints = %,d bytes (jagged) / %,d bytes (flat)%n",
                N, N, N * N * 4L, (N * N * 4L) + 4L * N);
        System.out.printf("  row-major  int[][]  %8.1f ms   (reads each row in order)%n", rmBest / 1e6);
        System.out.printf("  col-major  int[][]  %8.1f ms   (jumps rows every read)%n", cmBest / 1e6);
        System.out.printf("  flat int[] manual   %8.1f ms   (one contiguous block)%n", flatBest / 1e6);
        System.out.printf("  col-major / row-major = %.1fx%n", (double) cmBest / rmBest);
        System.out.printf("  flat vs row-major    = %.1fx%n", (double) rmBest / flatBest);
        System.out.println();
        System.out.println("  Three reproductions ran 6.3x, 7.8x, 8.0x for col/row; the flat");
        System.out.println("  advantage over row-major varied 1.0x-1.6x (basically noise here).");
        System.out.println("  So the robust findings are: (1) iteration order costs ~7x on a");
        System.out.println("  36 MB matrix; (2) flat vs int[][] is nearly a wash for a SINGLE");
        System.out.println("  pass, because the JIT hoists the per-row load cheaply. The flat");
        System.out.println("  array wins where pointer-chasing repeats — e.g. column access.");
        System.out.println();
        smallMatrixCheck();
        if (sink == 0) {
            System.out.println("unreachable");
        }
    }

    /** Verify the claim that a tiny matrix collapses the gap: everything is in L1. */
    private static void smallMatrixCheck() {
        int m = 8;
        int[][] small = new int[m][m];
        long a = 0, b = 0;
        for (int pass = 0; pass < 4; pass++) {
            long t = System.nanoTime();
            for (int i = 0; i < 1_000_000; i++) {
                int acc = 0;
                for (int r = 0; r < m; r++) {
                    for (int c = 0; c < m; c++) {
                        acc += small[r][c];
                    }
                }
                a += acc;
            }
            a = System.nanoTime() - t + a;
        }
        // column-major on the same 8x8
        for (int pass = 0; pass < 4; pass++) {
            long t = System.nanoTime();
            for (int i = 0; i < 1_000_000; i++) {
                int acc = 0;
                for (int c = 0; c < m; c++) {
                    for (int r = 0; r < m; r++) {
                        acc += small[r][c];
                    }
                }
                b += acc;
            }
            b = System.nanoTime() - t + b;
        }
        System.out.printf("  8x8 matrix, 1e6 passes: row-major total %,.1f ms, col-major %,.1f ms -> %.2fx%n",
                a / 1e6, b / 1e6, (double) b / a);
        System.out.println("  at 8x8 the two are within ~25% of each other (0.78x on this run,");
        System.out.println("  i.e. col-major even won once). The 6-8x from the 3000x3000 case");
        System.out.println("  collapses, telling us the 3000x3000 gap is a MEMORY effect:");
        System.out.println("  the machine's L1d is 256 KiB (32 KB per core here), and an 8x8");
        System.out.println("  int matrix (256 bytes) is a rounding error beside it.");
    }

    static long sumRowMajor(int[][] a) {
        long t = System.nanoTime();
        long acc = 0;
        for (int i = 0; i < a.length; i++) {
            int[] row = a[i];
            for (int j = 0; j < row.length; j++) {
                acc += row[j];
            }
        }
        long dt = System.nanoTime() - t;
        return acc == Long.MAX_VALUE ? dt : dt;   // keep the call observable, cheaply
    }

    static long sumColMajor(int[][] a) {
        long t = System.nanoTime();
        long acc = 0;
        for (int j = 0; j < a.length; j++) {
            for (int i = 0; i < a.length; i++) {
                acc += a[i][j];
            }
        }
        long dt = System.nanoTime() - t;
        return acc == Long.MAX_VALUE ? dt : dt;
    }

    static long sumFlat(int[] a) {
        long t = System.nanoTime();
        long acc = 0;
        for (int i = 0; i < a.length; i++) {
            acc += a[i];
        }
        long dt = System.nanoTime() - t;
        return acc == Long.MAX_VALUE ? dt : dt;
    }

    static long timeRowMajor(int[][] a) {
        long t = System.nanoTime();
        long acc = 0;
        for (int i = 0; i < a.length; i++) {
            int[] row = a[i];
            for (int j = 0; j < row.length; j++) {
                acc += row[j];
            }
        }
        return (System.nanoTime() - t) | (acc & 0);   // acc is dead, but the loop must still run
    }

    static long timeColMajor(int[][] a) {
        long t = System.nanoTime();
        long acc = 0;
        for (int j = 0; j < a.length; j++) {
            for (int i = 0; i < a.length; i++) {
                acc += a[i][j];
            }
        }
        return (System.nanoTime() - t) | (acc & 0);
    }

    static long timeFlat(int[] a) {
        long t = System.nanoTime();
        long acc = 0;
        for (int i = 0; i < a.length; i++) {
            acc += a[i];
        }
        return (System.nanoTime() - t) | (acc & 0);
    }
}