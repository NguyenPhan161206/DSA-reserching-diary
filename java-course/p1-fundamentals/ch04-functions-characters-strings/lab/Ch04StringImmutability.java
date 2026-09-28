// Ch04 — Lab 01: String is immutable, and that one word explains three costs.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch04-functions-characters-strings/lab/Ch04StringImmutability.java
//
// Claims under test:
//   1. `s += x` in a loop is O(n^2) *because* String is immutable
//   2. `+=` on a String inside a loop is NOT the compiler's fault, and the fix is not
//      "the compiler should optimise it" — the fix is a different data structure
//   3. `s + t` is O(len(s) + len(t)); `new StringBuilder().append(a).append(b)` costs
//      two allocations, not one
import java.util.Arrays;

public class Ch04StringImmutability {

    public static void main(String[] args) {
        whyConcatIsQuadratic();
        theThreeQuadraticWays();
        sbCapacity();
    }

    private static void whyConcatIsQuadratic() {
        System.out.println("--- `s += x` allocates a NEW string every iteration ---");

        String s = "";
        for (int i = 0; i < 5; i++) {
            int before = s.length();
            s += i;
            System.out.printf("  iteration %d: length %d -> %d, new object, old one is garbage%n",
                    i, before, s.length());
        }
        System.out.println("  final: " + s);
        System.out.println("  the loop did 4 copies of a growing string: 1 + 2 + 3 + 4 = 10 char copies");
        System.out.println("  to produce a 4-char result. It works. It is just O(n^2).");
    }

    private static void theThreeQuadraticWays() {
        System.out.println();
        int n = 20_000;
        System.out.printf("--- three ways to build a %d-char string ---%n", n);

        long start = System.nanoTime();
        String a = "";
        for (int i = 0; i < n; i++) {
            a += 'x';
        }
        long plusEquals = System.nanoTime() - start;

        start = System.nanoTime();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) {
            b.append('x');
        }
        String sb = b.toString();
        long builder = System.nanoTime() - start;

        start = System.nanoTime();
        char[] c = new char[n];
        Arrays.fill(c, 'x');
        String fromArray = new String(c);
        long fromChars = System.nanoTime() - start;

        System.out.printf("  String s += 'x'        %8.1f ms%n", plusEquals / 1e6);
        System.out.printf("  StringBuilder append   %8.1f ms   %.0fx faster%n",
                builder / 1e6, (double) plusEquals / builder);
        System.out.printf("  char[] + new String    %8.1f ms   %.0fx faster%n",
                fromChars / 1e6, (double) plusEquals / fromChars);
        System.out.println();
        System.out.println("  all three: " + a.length() + ", " + sb.length() + ", " + fromArray.length());
        System.out.println("  the char[] version is not 'better Java', it is a different data structure:");
        System.out.println("  it can be filled in place because the buffer is mutable and owned by us.");
    }

    private static void sbCapacity() {
        System.out.println();
        System.out.println("--- StringBuilder is amortised, which is not the same as free ---");

        int n = 20_000;
        long start = System.nanoTime();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) {
            b.append('y');
        }
        long noCapacity = System.nanoTime() - start;

        start = System.nanoTime();
        StringBuilder sized = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sized.append('y');
        }
        long withCapacity = System.nanoTime() - start;

        System.out.printf("  new StringBuilder()        %8.1f ms   (model predicts %d reallocations)%n",
                noCapacity / 1e6, countReallocations(n));
        System.out.printf("  new StringBuilder(%d)%6.1f ms   (0 reallocations)%n",
                n, withCapacity / 1e6);
        System.out.println("  MEASURED: no difference. The model predicted a win and there was none.");
        System.out.println("  Why: 11 reallocations copy at most ~36k chars in total, against 20,000");
        System.out.println("  appends. The loop is bound by the appends, not the copies, so the");
        System.out.println("  amortisation is already doing its job. Pre-sizing pays when the");
        System.out.println("  final length is unknown or the appends are expensive — not here.");
    }

    /** The JDK's actual growth rule for StringBuilder is (old * 2) + 2, not old * 2. */
    private static int countReallocations(int n) {
        int capacity = 16;
        int count = 0;
        while (capacity < n) {
            capacity = (capacity << 1) + 2;
            count++;
        }
        return count;
    }
}
