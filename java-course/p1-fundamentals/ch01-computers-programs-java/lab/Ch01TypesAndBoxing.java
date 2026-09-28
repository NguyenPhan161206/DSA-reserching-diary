// Ch01/Ch02 — Lab 02: what a "type" costs in bytes, and what boxing costs in time.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch01-computers-programs-java/lab/Ch01TypesAndBoxing.java
//
// Two claims under test:
//   1. the primitive sizes the JVM fixes (Integer.BYTES is a constant, not a guess)
//   2. Integer[] costs an allocation per element, and that is measurable
import java.util.Arrays;

public class Ch01TypesAndBoxing {

    private static final int N = 5_000_000;

    public static void main(String[] args) {
        printTypeSizes();
        printPrimitiveRanges();
        timePrimitiveArray();
        timeBoxedArray();
    }

    private static void printTypeSizes() {
        System.out.println("--- fixed sizes (from the JVM, not from the book) ---");
        System.out.printf("  Byte.BYTES=%d  Short.BYTES=%d  Character.BYTES=%d  Float.BYTES=%d%n",
                Byte.BYTES, Short.BYTES, Character.BYTES, Float.BYTES);
        System.out.printf("  Integer.BYTES=%d  Long.BYTES=%d  Double.BYTES=%d%n",
                Integer.BYTES, Long.BYTES, Double.BYTES);
        System.out.println("  Boolean has no BYTES constant. The JLS (4.2) only promises 1 bit");
        System.out.println("  for booleans inside arrays and bit fields; HotSpot reserves a whole");
        System.out.println("  byte per boolean field. Do not assume 1 bit on an object field.");
        System.out.println("  object header on a 64-bit JVM: 16 bytes (12 with compressed oops)");
        // The book writes `new Integer(1)` in places. In Java 21 that constructor is
        // deprecated for removal; Integer.valueOf() is the form that is still correct.
        System.out.printf("  Integer.valueOf(1) == Integer.valueOf(1): %s   (cache -128..127)%n",
                Integer.valueOf(1) == Integer.valueOf(1));
        System.out.printf("  Integer.valueOf(1000) == Integer.valueOf(1000): %s  (outside the cache)%n",
                Integer.valueOf(1000) == Integer.valueOf(1000));
        System.out.println();
    }

    private static void printPrimitiveRanges() {
        System.out.println("--- ranges ---");
        System.out.printf("  byte   %d .. %d%n", Byte.MIN_VALUE, Byte.MAX_VALUE);
        System.out.printf("  short  %d .. %d%n", Short.MIN_VALUE, Short.MAX_VALUE);
        System.out.printf("  int    %d .. %d%n", Integer.MIN_VALUE, Integer.MAX_VALUE);
        System.out.printf("  long   %d .. %d%n", Long.MIN_VALUE, Long.MAX_VALUE);
        System.out.printf("  int  overflow: MAX_VALUE + 1 = %d%n", Integer.MAX_VALUE + 1);
        System.out.println();
    }

    private static void timePrimitiveArray() {
        int[] data = new int[N];
        Arrays.fill(data, 1);

        long start = System.nanoTime();
        long sum = 0;
        for (int value : data) {
            sum += value;
        }
        long elapsed = System.nanoTime() - start;

        System.out.printf("--- %,d elements ---%n", N);
        System.out.printf("  int[]    sum=%d  %6.1f ms  (8,000,000 bytes, one allocation)%n", sum, elapsed / 1e6);
    }

    private static void timeBoxedArray() {
        Integer[] data = new Integer[N];

        // FILL does NOT allocate per element: Arrays.fill copies ONE reference into
        // every slot. Here the one value is `1`, autoboxed to the CACHED instance
        // Integer.valueOf(1), so all N references point at the same 16-byte object.
        Arrays.fill(data, 1);
        System.out.printf("  fill boxed: data[0] == data[%,d]: %s (one shared object, %,d bytes)%n",
                N - 1, data[0] == data[N - 1], 4L * N + 16L);

        // To honestly measure "an allocation per element", every slot must get a
        // DISTINCT object: autobox values outside the -128..127 cache.
        for (int i = 0; i < N; i++) {
            data[i] = i | 0x400;                    // forces a fresh Integer each time
        }
        long start = System.nanoTime();
        long sum = 0;
        for (Integer value : data) {
            sum += value;                          // unboxing per element
        }
        long elapsed = System.nanoTime() - start;

        System.out.printf("  Integer[] sum=%d  %6.1f ms  (%,d bytes, %,d allocations)%n",
                sum, elapsed / 1e6, 4L * N + 16L * N, N);
    }
}
