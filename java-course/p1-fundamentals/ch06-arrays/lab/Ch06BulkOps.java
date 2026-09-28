// Ch06 — Lab 02: bulk operations, and what `Arrays.sort` actually does.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch06-arrays/lab/Ch06BulkOps.java
//
// Claims under test:
//   1. System.arraycopy is one native memcpy — measurably faster than a Java loop per element
//   2. System.arraycopy is NOT a general copy: it has no type check, and a bad range
//      throws at RUNTIME, not compile time
//   3. Arrays.sort(int[]) is dual-pivot quicksort, NOT java.util.Arrays.TimSort (that is
//      for objects) — so it is O(n log n) worst case, and the "sort is O(n log n) so it
//      is safe" argument has to be made per-type
//   4. sorting objects with the same comparator is stable; primitives cannot be, so the
//      stable sort is not "for objects", it is "for the timsort algorithm"
import java.util.Arrays;
import java.util.Random;

public class Ch06BulkOps {

    private static final int N = 4_000_000;

    public static void main(String[] args) {
        arraycopyBeatsALoop();
        arraycopyHasNoTypeCheck();
        whatSortDoes();
        stabilityIsAPropertyOfTheAlgorithm();
    }

    private static void arraycopyBeatsALoop() {
        System.out.printf("--- copying %,d ints ---%n", N);

        int[] src = new int[N];
        Random r = new Random(1);
        for (int i = 0; i < N; i++) {
            src[i] = r.nextInt();
        }

        int[] viaLoop = new int[N];
        long t0 = System.nanoTime();
        for (int i = 0; i < N; i++) {
            viaLoop[i] = src[i];
        }
        long loopNs = System.nanoTime() - t0;

        int[] viaArraycopy = new int[N];
        t0 = System.nanoTime();
        System.arraycopy(src, 0, viaArraycopy, 0, N);
        long copyNs = System.nanoTime() - t0;

        System.out.printf("  Java for loop     %,10.1f ms%n", loopNs / 1e6);
        System.out.printf("  System.arraycopy  %,10.1f ms   %.1fx faster%n",
                copyNs / 1e6, (double) loopNs / copyNs);
        System.out.println("  same result: " + Arrays.equals(viaLoop, viaArraycopy));
        System.out.println("  the loop is bounds-checked per element; arraycopy checks once.");
    }

    private static void arraycopyHasNoTypeCheck() {
        System.out.println();
        System.out.println("--- System.arraycopy is untyped: the compiler cannot help you ---");

        int[] ints = {1, 2, 3};
        Object[] objects = new Object[3];
        try {
            System.arraycopy(ints, 0, objects, 0, 3);
            System.out.println("  copied int[] into Object[]: " + Arrays.toString(objects));
        } catch (ArrayStoreException e) {
            System.out.println("  int[] into Object[] -> " + e.getClass().getSimpleName());
            System.out.println("    message: " + e.getMessage());
        }

        String[] strings = {"a", "b", "c"};
        Object[] objs = new Object[3];
        System.arraycopy(strings, 0, objs, 0, 3);
        System.out.println("  but String[] into Object[] IS allowed, and is silent: "
                + Arrays.toString(objs));

        int[] dst = new int[3];
        try {
            System.arraycopy(ints, 0, dst, 0, 10);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("  length 10 into a 3-element array -> "
                    + e.getClass().getSimpleName() + " (a RUNTIME failure)");
        }
        System.out.println("  'a[i] = b[i]' would have been a compile error. arraycopy is not.");
    }

    private static void whatSortDoes() {
        System.out.println();
        System.out.println("--- which sort is Arrays.sort? it depends on the type ---");

        int n = 20_000_000;
        int[] data = new int[n];
        Random r = new Random(2);
        for (int i = 0; i < n; i++) {
            data[i] = r.nextInt();
        }

        int[] work = data.clone();
        long t0 = System.nanoTime();
        Arrays.sort(work);                       // dual-pivot quicksort for int[]
        long primNs = System.nanoTime() - t0;

        Integer[] boxed = new Integer[n];
        for (int i = 0; i < n; i++) {
            boxed[i] = data[i];
        }
        t0 = System.nanoTime();
        Arrays.sort(boxed);
        long boxedNs = System.nanoTime() - t0;

        System.out.printf("  int[]     %,d elements  %,8.1f ms%n", n, primNs / 1e6);
        System.out.printf("  Integer[] %,d elements  %,8.1f ms   %.1fx slower%n",
                n, boxedNs / 1e6, (double) boxedNs / primNs);
        System.out.println("  int[]     -> dual-pivot quicksort, in place, O(n log n) worst case");
        System.out.println("  Integer[] -> TimSort, which is STABLE, and needs n objects to exist");
        System.out.println("  the same call name, two different algorithms, two different guarantees.");
    }

    private static void stabilityIsAPropertyOfTheAlgorithm() {
        System.out.println();
        System.out.println("--- stability is a property of the ALGORITHM, not of the type ---");

        // 10 elements: quicksort below this size uses insertion sort, so build a bigger case.
        int n = 1000;
        Random r = new Random(3);
        int[] keys = new int[n];
        for (int i = 0; i < n; i++) {
            keys[i] = r.nextInt(3);              // only 3 distinct values -> many ties
        }

        int[] primitives = keys.clone();
        Arrays.sort(primitives);

        record Pair(int key, int index) { }
        Pair[] pairs = new Pair[n];
        for (int i = 0; i < n; i++) {
            pairs[i] = new Pair(keys[i], i);
        }
        Arrays.sort(pairs, (x, y) -> Integer.compare(x.key(), y.key()));

        boolean stable = true;
        for (int i = 1; i < n; i++) {
            if (pairs[i - 1].key() == pairs[i].key()
                    && pairs[i - 1].index() > pairs[i].index()) {
                stable = false;
                break;
            }
        }
        System.out.printf("  %d elements, keys in {0,1,2} so there are many ties%n", n);
        System.out.println("  Integer[] + comparator preserves the original order of ties: " + stable);
        System.out.println("  an int[] cannot express ties at all — the values are equal, and");
        System.out.println("  there is nothing left to keep in order. Stability is not a property");
        System.out.println("  of 'objects'; it is a property of the timsort the JDK picked.");
    }
}
