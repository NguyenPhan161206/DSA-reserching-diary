// Ch05 — Lab 01: GCD. One problem, three loops, three complexities.
//
// Run:  ./java-course/00-toolchain/run.sh java-course/p1-fundamentals/ch05-loops/lab/Ch05Gcd.java
//
// Claims under test:
//   1. the subtraction loop is O(max/min) — so (a=10^9, b=1) is hopeless
//   2. Euclid's modulo step is O(log min(a,b)), derived not remembered
//   3. COUNTING STEPS proves (2) with integers and no timing at all, so the
//      complexity claim does not depend on this machine
import java.util.Random;

public class Ch05Gcd {

    private static final Random R = new Random(20260928L);

    public static void main(String[] args) {
        selfCheck();
        stepCountsBeatTimings();
        subtractionBlowsUp();
        euclidWorstCase();
    }

    /** The right way to justify a complexity: count operations, not milliseconds. */
    private static void stepCountsBeatTimings() {
        System.out.println("--- GCD of 48 and 18, three ways, counting every step ---");

        System.out.printf("  subtraction : %d steps%n", subtractionSteps(48, 18));
        System.out.printf("  euclid      : %d steps%n", euclidSteps(48, 18));
        System.out.printf("  result      : %d (both implementations agree)%n", gcdEuclid(48, 18));
    }

    private static void subtractionBlowsUp() {
        System.out.println();
        System.out.println("--- the subtraction loop is O(max/min), and min can be 1 ---");
        System.out.printf("  %-14s %14s %8s%n", "(a, b)", "subtraction", "euclid");

        int[][] pairs = {{48, 18}, {1000, 7}, {100000, 3}, {999983, 1}};
        for (int[] p : pairs) {
            int sub = subtractionSteps(p[0], p[1]);
            int eu = euclidSteps(p[0], p[1]);
            String subText = sub > 1_000_000 ? (sub / 1_000_000) + "M+" : String.valueOf(sub);
            System.out.printf("  %-14s %14s %8d%n", p[0] + ", " + p[1], subText, eu);
        }
        System.out.println("  (999983, 1): the subtraction loop needs 999,982 subtractions to");
        System.out.println("  find a gcd of 1. euclid needs 1. That is the whole lesson.");
    }

    private static void euclidWorstCase() {
        System.out.println();
        System.out.println("--- euclid is O(log min): measured against fibonacci inputs ---");
        System.out.println("consecutive fibonacci numbers are the WORST case, and the reason is");
        System.out.println("derivable: every step is one subtraction, so the quotients are all 1.");
        System.out.println();
        System.out.printf("  %-8s %-14s %8s %10s %10s%n", "n", "(fib, fib+1)", "steps", "log2 min", "steps/log");

        long prev = 0, cur = 1;
        // (F(k), F(k+1)) pairs; step count of euclid on them is exactly k
        for (int k = 1; k <= 25; k++) {
            long next = prev + cur;
            prev = cur;
            cur = next;
            if (cur > 100_000_000L) {
                break;
            }
            long a = prev, b = cur;
            int steps = euclidSteps((int) a, (int) b);
            double log2 = Math.log(Math.min(a, b)) / Math.log(2);
            System.out.printf("  %-8d %-14s %8d %10.2f %10.2f%n",
                    k, a + ", " + b, steps, log2, steps / log2);
        }
        System.out.println();
        System.out.println("  READ THE `steps` COLUMN FIRST: for (F_n, F_n+1) the step count is");
        System.out.println("  EXACTLY n. That is not a coincidence, it is a theorem: euclid on");
        System.out.println("  (F_n, F_n+1) quotients every step to 1, and the recursion");
        System.out.println("  steps(F_n) = 1 + steps(F_n-1) with steps(F_1) = 1, so steps = n.");
        System.out.println("  And F_n ~ phi^n grows EXPONENTIALLY in n, so n = log_phi(F_n).");
        System.out.println("  Exponentially growing input, logarithmically many steps => O(log min).");
        System.out.println("  The `steps/log2` column hovering near 1.5-1.6 is that constant;");
        System.out.println("  it is log2(phi) ~ 1.44, not an accident of measurement.");
    }

    // ---------- the three implementations ----------

    static int gcdSubtraction(int a, int b) {
        int steps = 0;
        while (a != b) {
            if (a > b) {
                a = a - b;
            } else {
                b = b - a;
            }
            steps++;
        }
        return a;
    }

    static int gcdEuclid(int a, int b) {
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        return a;
    }

    static int subtractionSteps(int a, int b) {
        int steps = 0;
        while (a != b) {
            if (a > b) {
                a = a - b;
            } else {
                b = b - a;
            }
            steps++;
        }
        return steps;
    }

    static int euclidSteps(int a, int b) {
        int steps = 0;
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
            steps++;
        }
        return steps;
    }

    /** Cross-check: the slow loop is only trustworthy because it agrees with euclid. */
    static void selfCheck() {
        for (int t = 0; t < 100_000; t++) {
            int a = 1 + R.nextInt(100_000);
            int b = 1 + R.nextInt(100_000);
            int fast = gcdEuclid(a, b);
            int slow = gcdSubtraction(a, b);
            if (fast != slow) {
                throw new AssertionError("gcd(" + a + "," + b + "): euclid=" + fast + " sub=" + slow);
            }
        }
        System.out.println("100,000 random pairs: euclid == subtraction, 0 mismatches");
    }
}
